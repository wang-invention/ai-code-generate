package com.wang.wangaicodemother.service;

import com.wang.wangaicodemother.model.entity.CrawlTaskLog;

/**
 * 爬虫任务 服务层。
 */
public interface CrawlerService {

    /**
     * 触发爬取任务
     *
     * @param operator    操作人
     * @param triggerType auto/manual
     * @return 任务日志
     */
    CrawlTaskLog triggerCrawl(String operator, String triggerType);
}
