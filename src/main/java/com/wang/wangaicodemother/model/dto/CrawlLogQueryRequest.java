package com.wang.wangaicodemother.model.dto;

import com.wang.wangaicodemother.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 爬取日志查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CrawlLogQueryRequest extends PageRequest implements Serializable {

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;

    /**
     * 任务状态 running/finished/failed
     */
    private String taskStatus;

    private static final long serialVersionUID = 1L;
}
