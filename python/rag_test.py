import re

import markdown
from bs4 import BeautifulSoup
from elasticsearch import ApiError, TransportError, ConnectionError
from elasticsearch import Elasticsearch
from sentence_transformers import SentenceTransformer


# ---------------------- 1. 初始化 ES 客户端 ----------------------
def init_es_client():
    """
    初始化 Elasticsearch 客户端（兼容 ES 7.x / 8.x）
    """
    try:
        # 本地 ES 配置（如果是远程集群，替换为对应地址）
        es = Elasticsearch(
            ["http://129.211.0.210:9200"],  # ES 地址
            # 如果设置了账号密码，取消下面注释
            # basic_auth=("elastic", "你的密码"),
            request_timeout=30  # 请求超时时间
        )

        # 测试连接
        if es.ping():
            print("✅ Elasticsearch 连接成功")
            return es
        else:
            raise ConnectionError("❌ Elasticsearch 连接失败")

    except (ApiError, TransportError, ConnectionError) as e:
        print(f"❌ Elasticsearch 初始化失败: {e}")
        return None

# ---------------------- 2. 加载并解析 Markdown 文档 ----------------------
def load_and_parse_markdown(file_path):
    """加载 Markdown 文件，返回纯文本和原始内容"""
    with open(file_path, 'r', encoding='utf-8') as f:
        raw_md = f.read()

    # 转换 Markdown 为 HTML 并提取纯文本
    html = markdown.markdown(raw_md, extensions=['extra'])
    soup = BeautifulSoup(html, 'html.parser')
    pure_text = soup.get_text(separator='\n', strip=True)

    return pure_text, raw_md

# ---------------------- 3. 按标题分块（保持语义完整性） ----------------------
def chunk_by_markdown_headers(raw_md):
    """按 Markdown 标题切分文档，生成语义块"""
    header_pattern = re.compile(r'^(#{1,6})\s+(.+)$', re.MULTILINE)
    parts = header_pattern.split(raw_md)
    chunks = []

    # 处理文档开头无标题的内容
    if parts[0].strip():
        chunks.append({
            "title": "文档简介",
            "content": parts[0].strip()
        })

    # 遍历标题和内容，组合成结构化块
    for i in range(1, len(parts), 3):
        header_level = parts[i]
        header_title = parts[i+1]
        content = parts[i+2].strip()
        if content:  # 跳过空内容
            chunks.append({
                "title": header_title,
                "content": f"{header_level} {header_title}\n{content}"
            })

    return chunks

# ---------------------- 4. 创建 ES 索引（包含 dense_vector 字段） ----------------------
def create_es_index(es, index_name="vue_knowledge"):
    """创建 ES 索引，定义 dense_vector 字段存储向量"""
    # 索引映射：text 字段用于全文检索，vector 字段存储向量
    index_mapping = {
        "mappings": {
            "properties": {
                "title": {  # 知识块标题
                    "type": "text",
                    "analyzer": "ik_max_word"  # 中文分词（需安装 ik 分词器，否则用 standard）
                },
                "content": {  # 知识块完整内容
                    "type": "text",
                    "analyzer": "ik_max_word"
                },
                "vector": {  # 向量字段（维度需和模型输出一致）
                    "type": "dense_vector",
                    "dims": 384,  # paraphrase-multilingual-MiniLM-L12-v2 输出 384 维
                    "index": True,  # 开启向量索引，支持检索
                    "similarity": "cosine"  # 相似度计算方式：余弦相似度
                }
            }
        }
    }

    # 如果索引已存在，先删除（测试用，生产环境谨慎）
    if es.indices.exists(index=index_name):
        es.indices.delete(index=index_name)
        print(f"📌 已删除原有索引：{index_name}")

    # 创建新索引
    es.indices.create(index=index_name, body=index_mapping)
    print(f"✅ 索引 {index_name} 创建成功")

# ---------------------- 5. 批量插入数据到 ES ----------------------
def bulk_insert_to_es(es, chunks, vectors, index_name="vue_knowledge"):
    """批量插入分块文本和向量到 ES"""
    bulk_operations = []
    for idx, (chunk, vec) in enumerate(zip(chunks, vectors)):
        # 构造批量操作指令
        bulk_operations.append({
            "index": {
                "_index": index_name,
                "_id": idx + 1  # 自定义文档 ID
            }
        })
        bulk_operations.append({
            "title": chunk["title"],
            "content": chunk["content"],
            "vector": vec
        })

    # 执行批量插入
    try:
        response = es.bulk(body=bulk_operations)
        if response["errors"]:
            print("❌ 批量插入失败：", response["errors"])
        else:
            print(f"✅ 成功插入 {len(chunks)} 条数据到 Elasticsearch")
    except (ApiError, TransportError, ConnectionError) as e:
        raise Exception(f"ES 插入失败：{str(e)}")

# ---------------------- 6. 向量检索示例（根据问题找相似内容） ----------------------
def vector_search(es, query, model, index_name="vue_knowledge", top_k=3):
    """
    向量检索：将问题向量化，检索 ES 中最相似的知识块
    :param es: ES 客户端
    :param query: 用户问题（如 "Vue 响应式原理是什么？"）
    :param model: 嵌入模型
    :param top_k: 返回前 k 条结果
    :return: 相似结果列表
    """
    # 将问题转为向量
    query_vector = model.encode(query).tolist()

    # 构造向量检索 DSL
    search_body = {
        "size": top_k,
        "query": {
            "knn": {
                "field": "vector",
                "query_vector": query_vector,
                "k": top_k,
                "num_candidates": 100  # 候选集大小（大于 k 即可）
            }
        },
        "_source": ["title", "content"]  # 只返回标题和内容，不返回向量
    }

    # 执行检索
    response = es.search(index=index_name, body=search_body)
    hits = response["hits"]["hits"]

    # 整理结果
    results = []
    for hit in hits:
        results.append({
            "score": hit["_score"],  # 相似度分数
            "title": hit["_source"]["title"],
            "content": hit["_source"]["content"]
        })

    return results

# ---------------------- 主流程 ----------------------
if __name__ == "__main__":
    # 配置参数
    MD_FILE_PATH = "vue_knowledge.md"  # 你的 Vue 知识库 MD 文件路径
    ES_INDEX_NAME = "vue_knowledge"    # ES 索引名

    try:
        # 1. 初始化 ES 客户端
        es_client = init_es_client()

        # 2. 加载并解析 MD 文档
        pure_text, raw_md = load_and_parse_markdown(MD_FILE_PATH)

        # 3. 按标题分块
        knowledge_chunks = chunk_by_markdown_headers(raw_md)
        print(f"📌 文档分块完成，共生成 {len(knowledge_chunks)} 个知识块")

        # 4. 加载嵌入模型
        model = SentenceTransformer('paraphrase-multilingual-MiniLM-L12-v2')

        # 5. 知识块向量化
        chunk_contents = [chunk["content"] for chunk in knowledge_chunks]
        vectors = model.encode(chunk_contents).tolist()
        print(f"📌 向量生成完成，向量维度：{len(vectors[0])}")

        # 6. 创建 ES 索引
        create_es_index(es_client, ES_INDEX_NAME)

        # 7. 批量插入数据到 ES
        bulk_insert_to_es(es_client, knowledge_chunks, vectors, ES_INDEX_NAME)

        # ---------------------- 测试向量检索 ----------------------
        print("\n" + "-"*80)
        test_query = "Vue 响应式原理是什么？"  # 测试问题
        print(f"🔍 测试检索：{test_query}")
        search_results = vector_search(es_client, test_query, model, ES_INDEX_NAME)

        # 打印检索结果
        for idx, res in enumerate(search_results):
            print(f"\n【相似结果 {idx+1}】")
            print(f"相似度分数：{res['score']:.4f}")
            print(f"标题：{res['title']}")
            print(f"内容：{res['content'][:200]}...")  # 只打印前 200 字

    except Exception as e:
        print(f"❌ 执行失败：{str(e)}")