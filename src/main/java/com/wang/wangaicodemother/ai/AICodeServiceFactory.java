package com.wang.wangaicodemother.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AICodeServiceFactory {

    @Resource
    private ChatModel chatModel;

    /**
     * 创建AI服务
     *
     * @return
     */
    @Bean
    public Assistant createAICodeService() {
        return AiServices.create(Assistant.class, chatModel);
    }

}
