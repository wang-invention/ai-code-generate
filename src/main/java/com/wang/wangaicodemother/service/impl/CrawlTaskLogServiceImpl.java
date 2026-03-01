package com.wang.wangaicodemother.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.wang.wangaicodemother.exception.BusinessException;
import com.wang.wangaicodemother.exception.ErrorCode;
import com.wang.wangaicodemother.mapper.CrawlTaskLogMapper;
import com.wang.wangaicodemother.model.dto.CrawlLogQueryRequest;
import com.wang.wangaicodemother.model.entity.CrawlTaskLog;
import com.wang.wangaicodemother.service.CrawlTaskLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 爬虫任务日志 服务层实现。
 */
@Service
public class CrawlTaskLogServiceImpl extends ServiceImpl<CrawlTaskLogMapper, CrawlTaskLog> implements CrawlTaskLogService {

    @Override
    public QueryWrapper getQueryWrapper(CrawlLogQueryRequest queryRequest) {
        if (queryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        LocalDate startDate = parseDate(queryRequest.getStartDate());
        LocalDate endDate = parseDate(queryRequest.getEndDate());
        String taskStatus = queryRequest.getTaskStatus();

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("task_status", taskStatus);
        if (startDate != null) {
            queryWrapper.ge("task_date", startDate);
        }
        if (endDate != null) {
            queryWrapper.le("task_date", endDate);
        }
        queryWrapper.orderBy("task_date", false);
        return queryWrapper;
    }

    private LocalDate parseDate(String input) {
        if (StrUtil.isBlank(input)) {
            return null;
        }
        try {
            return LocalDate.parse(input.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception ignored) {
            return null;
        }
    }
}
