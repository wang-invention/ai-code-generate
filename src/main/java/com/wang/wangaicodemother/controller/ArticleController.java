package com.wang.wangaicodemother.controller;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.wang.wangaicodemother.common.BaseResponse;
import com.wang.wangaicodemother.common.ResultUtils;
import com.wang.wangaicodemother.exception.ErrorCode;
import com.wang.wangaicodemother.exception.ThrowUtils;
import com.wang.wangaicodemother.model.dto.ArticleQueryRequest;
import com.wang.wangaicodemother.model.entity.AiArticle;
import com.wang.wangaicodemother.model.vo.ArticleDetailVO;
import com.wang.wangaicodemother.model.vo.ArticleListVO;
import com.wang.wangaicodemother.model.vo.ArticlePageVO;
import com.wang.wangaicodemother.service.AiArticleService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 文章 控制层。
 */
@RestController
@RequestMapping("/articles")
public class ArticleController {

    @Resource
    private AiArticleService aiArticleService;

    /**
     * 文章列表查询
     *
     * @param queryRequest 查询条件
     * @param current      兼容前端参数
     * @param pageSize     兼容前端参数
     * @return 文章分页列表
     */
    @GetMapping("/list")
    public BaseResponse<ArticlePageVO> listArticles(ArticleQueryRequest queryRequest,
                                                    @RequestParam(required = false) Integer current,
                                                    @RequestParam(required = false) Integer pageSize) {
        if (current != null) {
            queryRequest.setPageNum(current);
        }
        if (pageSize != null) {
            queryRequest.setPageSize(pageSize);
        }
        long pageNum = queryRequest.getPageNum();
        long size = queryRequest.getPageSize();
        QueryWrapper queryWrapper = aiArticleService.getQueryWrapper(queryRequest);
        Page<AiArticle> page = aiArticleService.page(Page.of(pageNum, size), queryWrapper);
        Page<ArticleListVO> voPage = aiArticleService.buildArticleListVOPage(page);
        ArticlePageVO result = new ArticlePageVO();
        result.setRecords(voPage.getRecords());
        result.setTotal(voPage.getTotalRow());
        result.setPageNum(voPage.getPageNumber());
        result.setPageSize(voPage.getPageSize());
        return ResultUtils.success(result);
    }

    /**
     * 文章详情
     *
     * @param articleId 文章ID
     * @return 文章详情
     */
    @GetMapping("/detail/{articleId}")
    public BaseResponse<ArticleDetailVO> getArticleDetail(@PathVariable Long articleId) {
        ThrowUtils.throwIf(articleId == null || articleId <= 0, ErrorCode.PARAMS_ERROR);
        AiArticle article = aiArticleService.getById(articleId);
        ThrowUtils.throwIf(article == null, ErrorCode.NOT_FOUND_ERROR);
        AiArticle update = new AiArticle();
        update.setId(articleId);
        update.setViews(article.getViews() == null ? 1 : article.getViews() + 1);
        aiArticleService.updateById(update);
        ArticleDetailVO vo = aiArticleService.buildArticleDetailVO(article);
        return ResultUtils.success(vo);
    }
}
