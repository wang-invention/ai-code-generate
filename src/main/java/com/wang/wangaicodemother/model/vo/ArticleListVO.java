package com.wang.wangaicodemother.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章列表视图对象
 */
@Data
public class ArticleListVO implements Serializable {

    /**
     * 文章唯一标识
     */
    private Long articleId;

    /**
     * 兼容前端字段
     */
    private Long id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 爬取来源平台
     */
    private String sourcePlatform;

    /**
     * 兼容前端字段
     */
    private String sourceName;

    /**
     * 原文链接
     */
    private String sourceUrl;

    /**
     * 兼容前端字段
     */
    private String originalUrl;

    /**
     * 文章作者
     */
    private String author;

    /**
     * 文章发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 文章分类
     */
    private String category;

    /**
     * 是否精选
     */
    private Integer isFeatured;

    /**
     * 阅读量
     */
    private Integer views;

    /**
     * 状态
     */
    private String status;

    /**
     * 摘要
     */
    private String summary;

    private static final long serialVersionUID = 1L;
}
