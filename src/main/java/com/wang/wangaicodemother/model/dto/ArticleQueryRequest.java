package com.wang.wangaicodemother.model.dto;

import com.wang.wangaicodemother.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 文章查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ArticleQueryRequest extends PageRequest implements Serializable {

    /**
     * 文章分类
     */
    private String category;

    /**
     * 起始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 关键词（标题/正文/作者）
     */
    private String keyword;

    /**
     * 状态 normal/down/pending
     */
    private String status;

    /**
     * 排序 time/hot
     */
    private String sort;

    private static final long serialVersionUID = 1L;
}
