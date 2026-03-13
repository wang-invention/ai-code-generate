package com.wang.wangaicodemother.rag.impl;


import com.wang.wangaicodemother.rag.AIRagService;
import com.wang.wangaicodemother.rag.VueRagService;
import com.wang.wangaicodemother.rag.ai.AiChatService;
import com.wang.wangaicodemother.rag.domain.RagDocsXiao;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AIRagServiceImpl implements AIRagService {
    @Autowired
    private VueRagService vueRagService;
    @Resource
    private AiChatService aiChatService;
    @Resource
    private RestTemplate restTemplate;

    @Override
    public Flux<String> ragAndAI(String userQuestion) {
        //将用户的问题向量化
        Double[] vector = getVector(userQuestion);
        List<Float> queryVector = Arrays.stream(vector)
                .map(Number::floatValue)
                .toList();
        // 2. 执行向量搜索
        List<RagDocsXiao> result = vueRagService.searchByVector(queryVector);
        // 3. 输出
        if (result != null && !result.isEmpty()) {
            log.info("---------------查询向量成功-------------");
        } else {
            log.info("---------------未查询到相似数据-------------");
        }

        String prompt = buildRagPrompt(userQuestion, result);
        String sanitize = sanitize(prompt);
        // AI 回答
        return aiChatService.streamChat(sanitize);
    }
    private String sanitize(String text) {
        return text
                .replace("{{", "\\{\\{")
                .replace("}}", "\\}\\}");
    }


    /**
     * 构建 RAG 提示词：把ES查到的内容塞进AI提示词
     */
    private String buildRagPrompt(String userQuestion, List<RagDocsXiao> knowledgeList) {
        // 1. 拼接参考资料
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < knowledgeList.size(); i++) {
            RagDocsXiao doc = knowledgeList.get(i);
            context.append("参考资料").append(i + 1).append("：")
                    .append(doc.getContent()).append("\n");
        }

        // 2. 最终提示词（ instruct 模板 ）
        return """
                你是一个基于知识库的智能助手，请根据提供的参考资料回答用户问题。
                规则：
                1. 参考资料仅作为辅助，回答应结合用户的问题和知识库内容。
                2. 如果参考资料中没有相关信息，可以自由发挥，但回答必须简洁、准确。
                3. 不知道的部分可以说“在知识库中未找到相关信息”。
                 \s
                参考资料：
                %s
                
                用户问题：%s
                """.formatted(context.toString(), userQuestion);
    }

    private Double[] getVector(String title) {
        //调用python方法将用户问题向量化
        HashMap<String, String> ragRequest = new HashMap<>();
        ragRequest.put("text", title);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(ragRequest, headers);
        Double[] response = restTemplate.postForObject("http://localhost:8000/embed", requestEntity, Double[].class);
        System.out.println("向量长度：" + response.length);
//        System.out.println("向量：" + Arrays.toString(response));
        return response;
    }
}
