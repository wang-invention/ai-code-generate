package com.wang.wangaicodemother.rag.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    // 把 RestTemplate 交给 Spring 管理
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}