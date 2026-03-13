# 导入模型和Flask
from sentence_transformers import SentenceTransformer
from flask import Flask, request, jsonify

# 创建Flask服务
app = Flask(__name__)

# 加载模型（第一次运行会自动下载模型，耐心等一下）
print("正在加载模型...")
model = SentenceTransformer("paraphrase-multilingual-MiniLM-L12-v2")
print("模型加载完成！")

# 定义接口：接收文本，返回向量
@app.route("/embed", methods=["POST"])
def embed():
    try:
        # 获取前端传来的文本
        text = request.json["text"]

        # 生成向量
        vector = model.encode(text).tolist()

        # 返回向量
        return jsonify(vector)

    except Exception as e:
        return jsonify({"error": str(e)}), 400

# 启动服务，端口8000
if __name__ == '__main__':
    app.run(host="0.0.0.0", port=8000, debug=True)