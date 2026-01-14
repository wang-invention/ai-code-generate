package com.wang.wangaicodemother.ai;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AssistantTest {

    @Resource
    private Assistant assistant;

    @Test
    void generateHtmlCode() {
        String s = assistant.generateHtmlCode("生成一个html页面，不超过50行");
    }

    @Test
    void generateMultiCode() {
    }
}