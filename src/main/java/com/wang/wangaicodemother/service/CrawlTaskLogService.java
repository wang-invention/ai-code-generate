package com.wang.wangaicodemother.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.wang.wangaicodemother.model.dto.CrawlLogQueryRequest;
import com.wang.wangaicodemother.model.entity.CrawlTaskLog;

/**
 * 爬虫任务日志 服务层。
 */
public interface CrawlTaskLogService extends IService<CrawlTaskLog> {

    /**
     * 获取查询条件
     *
     * @param queryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(CrawlLogQueryRequest queryRequest);
}
