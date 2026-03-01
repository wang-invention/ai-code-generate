package com.wang.wangaicodemother.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.wang.wangaicodemother.exception.BusinessException;
import com.wang.wangaicodemother.exception.ErrorCode;
import com.wang.wangaicodemother.mapper.AiArticleMapper;
import com.wang.wangaicodemother.model.dto.ArticleQueryRequest;
import com.wang.wangaicodemother.model.entity.AiArticle;
import com.wang.wangaicodemother.model.vo.ArticleDetailVO;
import com.wang.wangaicodemother.model.vo.ArticleListVO;
import com.wang.wangaicodemother.service.AiArticleService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * AI文章 服务层实现。
 */
@Service
public class AiArticleServiceImpl extends ServiceImpl<AiArticleMapper, AiArticle> implements AiArticleService {

    @Override
    public QueryWrapper getQueryWrapper(ArticleQueryRequest articleQueryRequest) {
        if (articleQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        String category = articleQueryRequest.getCategory();
        String keyword = articleQueryRequest.getKeyword();
        String status = articleQueryRequest.getStatus();
        String sort = articleQueryRequest.getSort();
        LocalDateTime startTime = parseDateTime(articleQueryRequest.getStartTime(), true);
        LocalDateTime endTime = parseDateTime(articleQueryRequest.getEndTime(), false);

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("category", category)
                .eq("status", status);

        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.and((java.util.function.Consumer<QueryWrapper>) q -> q.like(AiArticle::getTitle, keyword)
                    .or(AiArticle::getContent).like(keyword)
                    .or(AiArticle::getAuthor).like(keyword));
        }
        if (startTime != null) {
            queryWrapper.ge("publish_time", startTime);
        }
        if (endTime != null) {
            queryWrapper.le("publish_time", endTime);
        }
        if ("hot".equalsIgnoreCase(sort)) {
            queryWrapper.orderBy("views", false);
        } else {
            queryWrapper.orderBy("publish_time", false);
        }
        return queryWrapper;
    }

    @Override
    public Page<ArticleListVO> buildArticleListVOPage(Page<AiArticle> page) {
        Page<ArticleListVO> voPage = new Page<>(page.getPageNumber(), page.getPageSize(), page.getTotalRow());
        List<ArticleListVO> records = page.getRecords().stream().map(this::buildArticleListVO).collect(Collectors.toList());
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public ArticleDetailVO buildArticleDetailVO(AiArticle article) {
        if (article == null) {
            return null;
        }
        ArticleDetailVO vo = new ArticleDetailVO();
        vo.setArticleId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setSourceUrl(article.getSourceUrl());
        vo.setSourcePlatform(article.getSourcePlatform());
        vo.setAuthor(article.getAuthor());
        vo.setPublishTime(article.getPublishTime());
        vo.setContent(article.getContent());
        vo.setCategory(article.getCategory());
        vo.setIsFeatured(article.getIsFeatured());
        vo.setCrawlTime(article.getCrawlTime());
        vo.setStatus(article.getStatus());
        vo.setUniqueKey(article.getUniqueKey());
        vo.setViews(article.getViews());
        return vo;
    }

    @Override
    public boolean existsByUniqueKey(String uniqueKey) {
        if (StrUtil.isBlank(uniqueKey)) {
            return false;
        }
        long count = this.count(QueryWrapper.create().eq("unique_key", uniqueKey));
        return count > 0;
    }

    @Override
    public boolean existsBySourceUrl(String sourceUrl) {
        if (StrUtil.isBlank(sourceUrl)) {
            return false;
        }
        long count = this.count(QueryWrapper.create().eq("source_url", sourceUrl));
        return count > 0;
    }

    private ArticleListVO buildArticleListVO(AiArticle article) {
        ArticleListVO vo = new ArticleListVO();
        vo.setArticleId(article.getId());
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setSourcePlatform(article.getSourcePlatform());
        vo.setSourceName(article.getSourcePlatform());
        vo.setSourceUrl(article.getSourceUrl());
        vo.setOriginalUrl(article.getSourceUrl());
        vo.setAuthor(article.getAuthor());
        vo.setPublishTime(article.getPublishTime());
        vo.setCategory(article.getCategory());
        vo.setIsFeatured(article.getIsFeatured());
        vo.setViews(article.getViews());
        vo.setStatus(article.getStatus());
        vo.setSummary(buildSummary(article.getContent()));
        return vo;
    }

    private String buildSummary(String content) {
        if (StrUtil.isBlank(content)) {
            return "";
        }
        String normalized = content.replaceAll("\\s+", " ").trim();
        int maxLen = Math.min(200, normalized.length());
        return normalized.substring(0, maxLen);
    }

    private LocalDateTime parseDateTime(String input, boolean start) {
        if (StrUtil.isBlank(input)) {
            return null;
        }
        String trimmed = input.trim();
        if (trimmed.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            try {
                LocalDate date = LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.CHINA));
                return start ? date.atStartOfDay() : date.atTime(23, 59, 59);
            } catch (Exception ignored) {
            }
        }
        DateTimeFormatter[] formatters = new DateTimeFormatter[]{
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        };
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(trimmed, formatter);
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
