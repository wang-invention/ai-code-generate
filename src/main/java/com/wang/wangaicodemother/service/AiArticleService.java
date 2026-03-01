package com.wang.wangaicodemother.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.wang.wangaicodemother.model.dto.ArticleQueryRequest;
import com.wang.wangaicodemother.model.entity.AiArticle;
import com.wang.wangaicodemother.model.vo.ArticleDetailVO;
import com.wang.wangaicodemother.model.vo.ArticleListVO;

/**
 * AI文章 服务层。
 */
public interface AiArticleService extends IService<AiArticle> {

    /**
     * 获取查询条件
     *
     * @param articleQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(ArticleQueryRequest articleQueryRequest);

    /**
     * 构建文章列表VO分页
     *
     * @param page 文章分页
     * @return 文章列表VO分页
     */
    Page<ArticleListVO> buildArticleListVOPage(Page<AiArticle> page);

    /**
     * 构建文章详情VO
     *
     * @param article 文章实体
     * @return 文章详情VO
     */
    ArticleDetailVO buildArticleDetailVO(AiArticle article);

    /**
     * 是否存在唯一标识
     *
     * @param uniqueKey 去重MD5
     * @return 是否存在
     */
    boolean existsByUniqueKey(String uniqueKey);

    /**
     * 是否存在原文链接
     *
     * @param sourceUrl 原文链接
     * @return 是否存在
     */
    boolean existsBySourceUrl(String sourceUrl);
}
