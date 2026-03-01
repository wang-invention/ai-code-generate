package com.wang.wangaicodemother.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 爬虫配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "crawler")
public class CrawlerProperties {

    /**
     * 定时任务Cron
     */
    private String cron = "0 0 1 * * ?";

    /**
     * 请求间隔毫秒
     */
    private int minDelayMs = 2000;

    /**
     * 正文最小长度
     */
    private int minContentLength = 300;

    /**
     * 每个来源最大抓取数量
     */
    private int maxItemsPerSource = 50;

    /**
     * 来源列表
     */
    private List<Source> sources = new ArrayList<>();

    @Data
    public static class Source {
        /**
         * 来源名称
         */
        private String name;

        /**
         * RSS地址
         */
        private String rssUrl;

        /**
         * 列表页地址
         */
        private String listUrl;

        /**
         * 分类
         */
        private String category;

        /**
         * 列表项选择器
         */
        private String itemSelector;

        /**
         * 列表项标题选择器
         */
        private String titleSelector;

        /**
         * 列表项链接选择器
         */
        private String urlSelector;

        /**
         * 详情页正文选择器
         */
        private String contentSelector;

        /**
         * 详情页时间选择器
         */
        private String timeSelector;

        /**
         * 详情页作者选择器
         */
        private String authorSelector;
    }
}
