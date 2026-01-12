package com.wang.wangaicodemother.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

public class JwtUtil {

    // HS256 对称加密 key，长度 >= 256 bit（32 字节）
    private static final String SECRET = "wangfaming666wangfaming666wangfaming66"; // 最少32个字符
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    /**
     * 生成 JWT token
     */
    public static String generateToken(Long userId, String userRole) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("userRole", userRole)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24)) // 24小时
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }
    /**
     * 解析 JWT token
     */
    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static Date getExpiration(String token) {
        return parseToken(token).getExpiration();
    }

}
