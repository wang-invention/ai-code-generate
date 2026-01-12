package com.wang.wangaicodemother.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private List<String> whileList;

    public List<String> getWhileList() {
        return whileList;
    }

    public void setWhileList(List<String> whileList) {
        this.whileList = whileList;
    }
}
