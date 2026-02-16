package com.wang.wangaicodemother.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.wang.wangaicodemother.exception.BusinessException;
import com.wang.wangaicodemother.exception.ErrorCode;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.UUID;

@Slf4j
public class WebScreenshotUtils {

    private static final WebDriver webDriver;

    static {
        final int DEFAULT_WIDTH = 1600;
        final int DEFAULT_HEIGHT = 900;
        webDriver = initChromeDriver(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @PreDestroy
    public void destroy() {
        webDriver.quit();
    }


    /**
     * 保存网页截图
     *
     * @param url
     * @return
     */
    public static String saveWebPageScreenshot(String url) {
        //非空校验
        if (StrUtil.isBlank(url)) {
            log.error("url不能为空");
            return null;
        }
        //创建临时目录
        String tempDir = System.getProperty("user.dir") + "/tmp/screenshot_output" + UUID.randomUUID().toString().substring(0, 8);
        FileUtil.mkdir(tempDir);
        final String IMAGE_SUFFIX = ".png";
        String imagePath = tempDir + File.separator + RandomUtil.randomNumbers(6) + IMAGE_SUFFIX;
        webDriver.get(url);
        waitForPageLoad(webDriver);
        byte[] screenshotAs = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);
        saveImage(screenshotAs, imagePath);
        log.info("保存图片成功：" + imagePath);
        final String COMPRESS_IMAGE_SUFFIX = ".jpg";
        String compressedImagePath = tempDir + File.separator + RandomUtil.randomNumbers(6) + COMPRESS_IMAGE_SUFFIX;
        compressImage(imagePath, compressedImagePath);
        log.info("压缩保存图片成功：" + compressedImagePath);
        FileUtil.del(imagePath);
        return compressedImagePath;
    }

    /**
     * 初始化 Chrome 浏览器驱动
     */
    private static WebDriver initChromeDriver(int width, int height) {
        try {
            // 自动管理 ChromeDriver
            WebDriverManager.chromedriver().setup();
            // 配置 Chrome 选项
            ChromeOptions options = new ChromeOptions();
            // 无头模式
            options.addArguments("--headless");
            // 禁用GPU（在某些环境下避免问题）
            options.addArguments("--disable-gpu");
            // 禁用沙盒模式（Docker环境需要）
            options.addArguments("--no-sandbox");
            // 禁用开发者shm使用
            options.addArguments("--disable-dev-shm-usage");
            // 设置窗口大小
            options.addArguments(String.format("--window-size=%d,%d", width, height));
            // 禁用扩展
            options.addArguments("--disable-extensions");
            // 设置用户代理
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            // 创建驱动
            WebDriver driver = new ChromeDriver(options);
            // 设置页面加载超时
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            // 设置隐式等待
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            return driver;
        } catch (Exception e) {
            log.error("初始化 Chrome 浏览器失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 Chrome 浏览器失败");
        }
    }

    /**
     * 保存图片路径
     *
     * @param imageBytes
     * @param filePath
     */
    public static void saveImage(byte[] imageBytes, String filePath) {

        try {
            FileUtil.writeBytes(imageBytes, filePath);
        } catch (Exception e) {
            log.error("保存图片失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
        }
    }

    /**
     * 压缩图片
     *
     * @param originImagePath
     * @param compressedImagePath
     */
    private static void compressImage(String originImagePath, String compressedImagePath) {
        try {
            ImgUtil.compress(FileUtil.file(originImagePath), FileUtil.file(compressedImagePath), 0.3f);
        } catch (IORuntimeException e) {
            log.error("压缩图片失败", e);
            throw new RuntimeException(e);
        }
    }


    /**
     * 等待页面加载完成
     *
     * @param driver
     */
    private static void waitForPageLoad(WebDriver driver) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState")
                            .equals("complete")
            );
            log.info("页面加载完成");
        } catch (Exception e) {
            log.error("等待页面加载完成失败", e);
            throw new RuntimeException(e);
        }
    }

}
