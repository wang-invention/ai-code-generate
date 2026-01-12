package com.wang.wangaicodemother.interceptor;

import com.wang.wangaicodemother.config.JwtProperties;
import com.wang.wangaicodemother.exception.BusinessException;
import com.wang.wangaicodemother.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;
import java.util.Set;

@Component
public class JwtInterceptor implements HandlerInterceptor {


    private final JwtProperties jwtProperties;

    private static final String SECRET = "wangfaming666wangfaming666wangfaming66"; // 必须 >=32 字节
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

        String authHeader = request.getHeader("Authorization");
        if (StringUtils.isBlank(authHeader)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "token???");
        }

        String token = authHeader.replace("Bearer ", "").trim();
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
