package com.wang.wangaicodemother.controller;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.wang.wangaicodemother.annotation.AuthCheck;
import com.wang.wangaicodemother.common.BaseResponse;
import com.wang.wangaicodemother.common.ResultUtils;
import com.wang.wangaicodemother.constants.UserConstant;
import com.wang.wangaicodemother.model.dto.CrawlLogQueryRequest;
import com.wang.wangaicodemother.model.entity.CrawlTaskLog;
import com.wang.wangaicodemother.model.entity.User;
import com.wang.wangaicodemother.service.CrawlTaskLogService;
import com.wang.wangaicodemother.service.CrawlerService;
import com.wang.wangaicodemother.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

/**
 * 爬虫 控制层。
 */
@RestController
@RequestMapping("/crawl")
public class CrawlController {

    @Resource
    private CrawlerService crawlerService;

    @Resource
    private CrawlTaskLogService crawlTaskLogService;

    @Resource
    private UserService userService;

    /**
     * 手动触发爬取
     *
     * @param request 请求
     * @return 任务日志
     */
    @PostMapping("/trigger")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<CrawlTaskLog> triggerCrawl(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        CrawlTaskLog log = crawlerService.triggerCrawl(loginUser.getUserAccount(), "manual");
        return ResultUtils.success(log);
    }

    /**
     * 爬取日志查询
     *
     * @param queryRequest 查询请求
     * @return 日志列表
     */
    @GetMapping("/logs")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<CrawlTaskLog>> listLogs(CrawlLogQueryRequest queryRequest) {
        long pageNum = queryRequest.getPageNum();
        long pageSize = queryRequest.getPageSize();
        QueryWrapper queryWrapper = crawlTaskLogService.getQueryWrapper(queryRequest);
        Page<CrawlTaskLog> page = crawlTaskLogService.page(Page.of(pageNum, pageSize), queryWrapper);
        return ResultUtils.success(page);
    }
}
