package com.wang.wangaicodemother.rag.config;

import com.wang.wangaicodemother.rag.ai.AiChatService;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public AiChatService aiChatService() {
        // 1. 创建模型（这里用 OpenAI 格式，国内模型都兼容）
        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey("sk-9206b7b2966d47f9965c5e56ff2a6439")
                .baseUrl("https://api.deepseek.com")  // 国内模型改这里
                .modelName("deepseek-chat")             // 模型名
                .temperature(0.7)                        // 创意程度
                .build();

        // 2. 绑定接口
        return AiServices.create(AiChatService.class, model);
    }
}