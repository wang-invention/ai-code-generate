package com.wang.wangaicodemother.config;

import com.wang.wangaicodemother.service.CrawlerService;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 爬虫定时任务
 */
@Component
public class CrawlSchedule {

    @Resource
    private CrawlerService crawlerService;

    /**
     * 每日定时执行
     */
    @Scheduled(cron = "${crawler.cron:0 0 1 * * ?}")
    public void runDailyCrawl() {
        crawlerService.triggerCrawl("auto", "auto");
    }
}
