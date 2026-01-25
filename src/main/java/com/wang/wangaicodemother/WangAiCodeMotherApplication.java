package com.wang.wangaicodemother;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.wang.wangaicodemother.mapper")
@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
public class WangAiCodeMotherApplication {

    public static void main(String[] args) {
        SpringApplication.run(WangAiCodeMotherApplication.class, args);
    }

}
