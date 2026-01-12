package com.wang.wangaicodemother;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.wang.wangaicodemother.mapper")
public class WangAiCodeMotherApplication {

    public static void main(String[] args) {
        SpringApplication.run(WangAiCodeMotherApplication.class, args);
    }

}
