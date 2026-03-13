package com.wang.wangaicodemother.rag;

import com.wang.wangaicodemother.model.entity.VueKnowledge;
import com.wang.wangaicodemother.rag.ai.AiChatService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class VueRagServiceTest {


    @Autowired
    private VueRagService vueRagService;
    @Resource
    private AiChatService aiChatService;
    @Resource
    private RestTemplate restTemplate;

    /**
     * 测试RAG增强AI的输出功能
     */
    @Test
    public void testRagAndAI() {
        //查询es中最相似的文档
        Double[] vector = getVector("什么是Vue");
        List<Float> queryVector = Arrays.stream(vector)
                .map(Number::floatValue)
                .toList();
        // 2. 执行向量搜索
        List<VueKnowledge> result = vueRagService.searchByVector(queryVector);

        // 3. 输出
        if (result != null && !result.isEmpty()) {
            System.err.println("---------------查询向量成功-------------");
        } else {
            System.err.println("---------------未查询到相似数据-------------");
        }

        String prompt = buildRagPrompt("Vue.js 定义", result);
        // AI 回答
        String answer = aiChatService.chat(prompt);
        // 输出结果
        System.out.println("AI：" + answer);


    }


    /**
     * 构建 RAG 提示词：把ES查到的内容塞进AI提示词
     */
    private String buildRagPrompt(String userQuestion, List<VueKnowledge> knowledgeList) {
        // 1. 拼接参考资料
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < knowledgeList.size(); i++) {
            VueKnowledge doc = knowledgeList.get(i);
            context.append("参考资料").append(i + 1).append("：")
                    .append(doc.getContent()).append("\n");
        }

        // 2. 最终提示词（ instruct 模板 ）
        return """
                你是一个基于知识库的智能助手，请根据提供的参考资料回答用户问题。
                规则：
                1. 只使用参考资料里的内容回答
                2. 不知道就说“在知识库中未找到相关信息”
                3. 回答简洁、准确
                
                参考资料：
                %s
                
                用户问题：%s
                """.formatted(context.toString(), userQuestion);
    }

    public Double[] getVector(String title) {
        //调用python方法将用户问题向量化
        HashMap<String, String> ragRequest = new HashMap<>();
        ragRequest.put("text", title);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(ragRequest, headers);
        Double[] response = restTemplate.postForObject("http://localhost:8000/embed", requestEntity, Double[].class);
        System.out.println("向量长度：" + response.length);
        System.out.println("向量：" + Arrays.toString(response));
        return response;
    }


}