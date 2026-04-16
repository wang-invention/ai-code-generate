import re
import markdown
from bs4 import BeautifulSoup
from elasticsearch import ApiError, TransportError, ConnectionError
from elasticsearch import Elasticsearch
from sentence_transformers import SentenceTransformer
from fastapi import FastAPI, UploadFile, File, Form, HTTPException
from pydantic import BaseModel
import shutil
import os

# ---------------------- 初始化FastAPI ----------------------
app = FastAPI(title="多用户专属知识库检索服务", version="1.0")

# 全局模型（只加载一次，提升性能）
MODEL = None

# ---------------------- 1. 初始化 ES客户端 ----------------------
def init_es_client():
    try:
        es = Elasticsearch(
            ["http://129.211.0.210:9200"],
            request_timeout=60
        )
        if es.ping():
            return es
        else:
            raise ConnectionError("Elasticsearch 连接失败")
    except Exception as e:
        raise Exception(f"ES连接异常: {str(e)}")

# ---------------------- 2. 加载向量模型 ----------------------
def get_embedding_model():
    global MODEL
    if MODEL is None:
        MODEL = SentenceTransformer('paraphrase-multilingual-MiniLM-L12-v2')
    return MODEL

# ---------------------- 3. MD文档解析 + 分块 ----------------------
def load_and_parse_markdown(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        raw_md = f.read()
    html = markdown.markdown(raw_md, extensions=['extra'])
    soup = BeautifulSoup(html, 'html.parser')
    pure_text = soup.get_text(separator='\n', strip=True)
    return pure_text, raw_md

def chunk_by_markdown_headers(raw_md):
    header_pattern = re.compile(r'^(#{1,6})\s+(.+)$', re.MULTILINE)
    parts = header_pattern.split(raw_md)
    chunks = []
    if parts[0].strip():
        chunks.append({"title": "文档简介", "content": parts[0].strip()})
    for i in range(1, len(parts), 3):
        level = parts[i]
        title = parts[i+1]
        content = parts[i+2].strip()
        if content:
            chunks.append({"title": title, "content": f"{level} {title}\n{content}"})
    return chunks

# ---------------------- 4. ES索引管理（用户隔离） ----------------------
def create_user_index(es, user_id: str):
    """按user_id创建独立索引，实现多用户隔离"""
    index_name = f"user_kb_{user_id}"
    mapping = {
        "mappings": {
            "properties": {
                "title": {"type": "text", "analyzer": "ik_max_word"},
                "content": {"type": "text", "analyzer": "ik_max_word"},
                "vector": {
                    "type": "dense_vector",
                    "dims": 384,
                    "index": True,
                    "similarity": "cosine"
                }
            }
        }
    }
    if es.indices.exists(index=index_name):
        es.indices.delete(index=index_name)
    es.indices.create(index=index_name, body=mapping)
    return index_name

# ---------------------- 5. 批量插入数据 ----------------------
def bulk_insert(es, chunks, vectors, index_name):
    operations = []
    for idx, (chunk, vec) in enumerate(zip(chunks, vectors)):
        operations.append({"index": {"_index": index_name, "_id": idx+1}})
        operations.append({
            "title": chunk["title"],
            "content": chunk["content"],
            "vector": vec
        })
    res = es.bulk(body=operations)
    return not res["errors"]

# ---------------------- 6. 向量检索 ----------------------
def user_vector_search(es, user_id: str, query: str, top_k: int = 3):
    index_name = f"user_kb_{user_id}"
    if not es.indices.exists(index=index_name):
        raise HTTPException(status_code=404, detail="用户知识库不存在，请先创建")

    model = get_embedding_model()
    query_vec = model.encode(query).tolist()

    body = {
        "size": top_k,
        "query": {
            "knn": {
                "field": "vector",
                "query_vector": query_vec,
                "k": top_k,
                "num_candidates": 100
            }
        },
        "_source": ["title", "content"]
    }
    response = es.search(index=index_name, body=body)
    hits = response["hits"]["hits"]

    results = []
    for hit in hits:
        results.append({
            "score": round(hit["_score"], 4),
            "title": hit["_source"]["title"],
            "content": hit["_source"]["content"]
        })
    return results

# ====================== 对外接口 ======================
# 提问请求体
class QueryRequest(BaseModel):
    user_id: str
    question: str
    top_k: int = 3

# 1. 健康检查
@app.get("/")
def health():
    return {"status": "ok", "msg": "知识库服务运行正常"}

# 2. 创建用户知识库（初始化空索引）
@app.post("/api/user/create_kb")
def create_user_kb(user_id: str = Form(...)):
    try:
        es = init_es_client()
        create_user_index(es, user_id)
        return {"code": 200, "msg": f"用户{user_id}知识库创建成功"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# 3. 上传MD文件构建知识库（核心接口）
@app.post("/api/user/upload_kb")
async def upload_md(
        user_id: str = Form(...),
        file: UploadFile = File(...)
):
    try:
        # 保存临时文件
        temp_path = f"temp_{user_id}.md"
        with open(temp_path, "wb") as f:
            shutil.copyfileobj(file.file, f)

        # 解析 + 分块
        pure_text, raw_md = load_and_parse_markdown(temp_path)
        chunks = chunk_by_markdown_headers(raw_md)
        if not chunks:
            raise HTTPException(status_code=400, detail="文档内容为空")

        # 向量化
        model = get_embedding_model()
        contents = [c["content"] for c in chunks]
        vectors = model.encode(contents).tolist()

        # 写入ES
        es = init_es_client()
        index_name = create_user_index(es, user_id)
        success = bulk_insert(es, chunks, vectors, index_name)

        # 删除临时文件
        os.remove(temp_path)

        if success:
            return {
                "code": 200,
                "msg": "知识库构建完成",
                "chunk_count": len(chunks)
            }
        else:
            raise HTTPException(status_code=500, detail="数据写入失败")
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# 4. 提问检索（Java核心调用接口）
@app.post("/api/query")
def query(req: QueryRequest):
    try:
        es = init_es_client()
        results = user_vector_search(es, req.user_id, req.question, req.top_k)
        return {
            "code": 200,
            "question": req.question,
            "results": results
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# 5. 删除用户知识库
@app.post("/api/user/delete_kb")
def delete_kb(user_id: str = Form(...)):
    try:
        es = init_es_client()
        index_name = f"user_kb_{user_id}"
        if es.indices.exists(index=index_name):
            es.indices.delete(index=index_name)
        return {"code": 200, "msg": f"用户{user_id}知识库已删除"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# ---------------------- 启动服务 ----------------------
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)