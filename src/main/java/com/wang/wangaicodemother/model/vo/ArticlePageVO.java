package com.wang.wangaicodemother.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 文章分页视图对象
 */
@Data
public class ArticlePageVO implements Serializable {

    /**
     * 列表数据
     */
    private List<ArticleListVO> records;

    /**
     * 总数
     */
    private long total;

    /**
     * 当前页
     */
    private long pageNum;

    /**
     * 页面大小
     */
    private long pageSize;

    private static final long serialVersionUID = 1L;
}
