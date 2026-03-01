package com.wang.wangaicodemother.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章详情视图对象
 */
@Data
public class ArticleDetailVO implements Serializable {

    /**
     * 文章唯一标识
     */
    private Long articleId;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 原文链接
     */
    private String sourceUrl;

    /**
     * 爬取来源平台
     */
    private String sourcePlatform;

    /**
     * 文章作者
     */
    private String author;

    /**
     * 文章发布时间
     */
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
     * 是否精选
     */
    private Integer isFeatured;

    /**
     * 爬取时间
     */
    private LocalDateTime crawlTime;

    /**
     * 状态
     */
    private String status;

    /**
     * 去重MD5
     */
    private String uniqueKey;

    /**
     * 阅读量
     */
    private Integer views;

    private static final long serialVersionUID = 1L;
}
