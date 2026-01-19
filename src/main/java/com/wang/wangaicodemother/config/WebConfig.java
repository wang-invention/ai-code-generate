package com.wang.wangaicodemother.config;

import com.wang.wangaicodemother.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    private static final String PREVIEW_ROOT_DIR =
            System.getProperty("user.dir") + "/tmp/code_output/";

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
        System.out.println("Static preview dir = " + PREVIEW_ROOT_DIR);

        registry.addResourceHandler("/static/**")
                .addResourceLocations("file:" + PREVIEW_ROOT_DIR)
                .setCachePeriod(0); // 开发期关闭缓存
    }

}
