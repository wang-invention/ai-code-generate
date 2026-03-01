package com.wang.wangaicodemother.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 爬虫任务日志 实体类。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("crawl_task_logs")
public class CrawlTaskLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日志唯一标识
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 爬取任务日期
     */
    @Column("task_date")
    private LocalDate taskDate;

    /**
     * 任务开始时间
     */
    @Column("start_time")
    private LocalDateTime startTime;

    /**
     * 任务结束时间
     */
    @Column("end_time")
    private LocalDateTime endTime;

    /**
     * 当日爬取总数量
     */
    @Column("total_crawl")
    private Integer totalCrawl;

    /**
     * 成功入库数量
     */
    @Column("success_count")
    private Integer successCount;

    /**
     * 失败数量
     */
    @Column("fail_count")
    private Integer failCount;

    /**
     * 失败原因
     */
    @Column("fail_reason")
    private String failReason;

    /**
     * 任务状态 running/finished/failed
     */
    @Column("task_status")
    private String taskStatus;

    /**
     * 执行人 auto/管理员账号
     */
    private String operator;
}
