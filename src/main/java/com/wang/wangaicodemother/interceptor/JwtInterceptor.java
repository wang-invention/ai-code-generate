package com.wang.wangaicodemother.interceptor;

import com.wang.wangaicodemother.config.JwtProperties;
import com.wang.wangaicodemother.exception.BusinessException;
import com.wang.wangaicodemother.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class JwtInterceptor implements HandlerInterceptor {


    private final JwtProperties jwtProperties;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String SECRET = "wangfaming666wangfaming666wangfaming66";

    private static final Key KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    public JwtInterceptor(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        // 1. 白名单直接放行
        List<String> whiteList = jwtProperties.getWhileList();
        if (whiteList.stream().anyMatch(uri::startsWith)) {
            return true;
        }

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true; // 直接放行
        }
        log.info("method = {}", request.getMethod());
        log.info("Authorization = {}", request.getHeader("Authorization"));

        String authHeader = request.getHeader("Authorization");
        if (StringUtils.isBlank(authHeader)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "token???");
        }


        String token = authHeader.replace("Bearer ", "").trim();

        // 校验是否在黑名单
        if (Boolean.TRUE.equals(
                redisTemplate.hasKey("jwt:blacklist:" + token))) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "token已失效");
        }

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Long userId = Long.parseLong(claims.getSubject());
            request.setAttribute("userId", userId);
            request.setAttribute("userRole", claims.get("userRole"));

            return true;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "token invalid");
        }
    }
}
