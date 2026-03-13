package com.wang.wangaicodemother.rag.config;

import com.wang.wangaicodemother.rag.ai.AiChatService;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

//    @Bean
//    public AiChatService aiChatService() {
//        // 1. 创建模型（这里用 OpenAI 格式，国内模型都兼容）
//        OpenAiChatModel model = OpenAiChatModel.builder()
//                .apiKey("sk-9206b7b2966d47f9965c5e56ff2a6439")
//                .baseUrl("https://api.deepseek.com")  // 国内模型改这里
//                .modelName("deepseek-chat")             // 模型名
//                .temperature(0.7)                        // 创意程度
//                .build();
//
//        // 2. 绑定接口
//        return AiServices.create(AiChatService.class, model);
//    }


    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.base-url}")
    private String baseUrl;

    @Value("${langchain4j.open-ai.chat-model.model-name}")
    private String modelName;


    @Bean
    public AiChatService streamingAiChatService(ChatMemory chatMemory) {
        // 流式模型！！！ Flux<String> 必须用这个
        OpenAiStreamingChatModel streamingModel = OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(0.3)
                .build();

        // 绑定流式模型
        return AiServices.builder(AiChatService.class)
                .streamingChatModel(streamingModel)
                .chatMemory(chatMemory)
                .build();
    }
    @Bean
    public ChatMemory chatMemory() {
        // 记住最近 10 条消息（可自己改大小）
        return MessageWindowChatMemory.withMaxMessages(10);
    }


}