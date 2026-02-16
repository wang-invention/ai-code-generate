package com.wang.wangaicodemother.utils;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WebScreenshotUtilsTest {
    @Test
    void getWebScreenShot() {
        WebScreenshotUtils.saveWebPageScreenshot("https://www.baidu.com");
    }

}