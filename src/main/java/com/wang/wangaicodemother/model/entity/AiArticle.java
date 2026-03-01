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
import java.time.LocalDateTime;

/**
 * AI文章 实体类。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("ai_articles")
public class AiArticle implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文章唯一标识
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 原文链接
     */
    @Column("source_url")
    private String sourceUrl;

    /**
     * 爬取来源平台
     */
    @Column("source_platform")
    private String sourcePlatform;

    /**
     * 文章作者
     */
    private String author;

    /**
     * 文章发布时间
     */
    @Column("publish_time")
    private LocalDateTime publishTime;

    /**
     * 正文内容
     */
    private String content;

    /**
     * 文章分类
     */
    private String category;

    /**
     * 是否精选 0/1
     */
    @Column("is_featured")
    private Integer isFeatured;

    /**
     * 爬取时间
     */
    @Column("crawl_time")
    private LocalDateTime crawlTime;

    /**
     * 状态 normal/down/pending
     */
    private String status;

    /**
     * 去重MD5
     */
    @Column("unique_key")
    private String uniqueKey;

    /**
     * 阅读量
     */
    private Integer views;
}
