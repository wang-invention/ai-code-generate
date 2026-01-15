package com.wang.wangaicodemother.ai;

import com.wang.wangaicodemother.ai.model.HtmlCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AssistantTest {

    @Resource
    private Assistant assistant;

    @Test
    void generateHtmlCode() {
        HtmlCodeResult htmlCode = assistant.generateHtmlCode("请生成一个HTML代码，内容是：<h1>Hello World</h1>");
        System.err.println(htmlCode);
    }

    @Test
    void generateMultiCode() {
    }
}