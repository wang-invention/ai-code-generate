package com.wang.wangaicodemother.config;

import com.wang.wangaicodemother.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    private static final String CODE_OUTPUT_DIR =
            System.getProperty("user.dir") + "/tmp/code_output/";
    private static final String CODE_DEPLOY_DIR =
            System.getProperty("user.dir") + "/tmp/code_deploy/";

    public WebConfig() {
        // 确保两个目录都存在
        File outputDir = new File(CODE_OUTPUT_DIR);
        File deployDir = new File(CODE_DEPLOY_DIR);
        
        if (!outputDir.exists()) {
            outputDir.mkdirs();
            System.out.println("Created code_output directory: " + CODE_OUTPUT_DIR);
        }
        if (!deployDir.exists()) {
            deployDir.mkdirs();
            System.out.println("Created code_deploy directory: " + CODE_DEPLOY_DIR);
        }
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // ===== Swagger UI =====
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/swagger-resources",
                        "/swagger-resources/**",

                        // ===== Knife4j =====
                        "/doc.html",
                        "/webjars/**",

                        // ===== 静态资源 =====
                        "/webjars/**",
                        "/favicon.ico",
                        "/static/**",

                        // ===== 你自己的白名单接口（示例）=====
                        "/api/user/login",
                        "/api/user/register"
                )
                .order(1);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        System.out.println("Code output dir = " + CODE_OUTPUT_DIR);
        System.out.println("Code deploy dir = " + CODE_DEPLOY_DIR);

        // 同时注册两个目录的静态资源映射
        registry.addResourceHandler("/static/output/**")
                .addResourceLocations("file:" + CODE_OUTPUT_DIR)
                .setCachePeriod(0); // 开发期关闭缓存
        
        registry.addResourceHandler("/static/deploy/**")
                .addResourceLocations("file:" + CODE_DEPLOY_DIR)
                .setCachePeriod(0); // 开发期关闭缓存
        
        // 保持原有的 /static/** 映射指向 code_output 目录（向后兼容）
        registry.addResourceHandler("/static/**")
                .addResourceLocations("file:" + CODE_OUTPUT_DIR)
                .setCachePeriod(0); // 开发期关闭缓存
    }

}