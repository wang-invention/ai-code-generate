package com.wang.wangaicodemother.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.wang.wangaicodemother.config.CrawlerProperties;
import com.wang.wangaicodemother.model.entity.AiArticle;
import com.wang.wangaicodemother.model.entity.CrawlTaskLog;
import com.wang.wangaicodemother.service.AiArticleService;
import com.wang.wangaicodemother.service.CrawlTaskLogService;
import com.wang.wangaicodemother.service.CrawlerService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 爬虫任务 服务层实现。
 */
@Service
@Slf4j
public class CrawlerServiceImpl implements CrawlerService {

    @Resource
    private AiArticleService aiArticleService;

    @Resource
    private CrawlTaskLogService crawlTaskLogService;

    @Resource
    private CrawlerProperties crawlerProperties;

    private final Map<String, List<String>> robotsCache = new ConcurrentHashMap<>();

    @Override
    public CrawlTaskLog triggerCrawl(String operator, String triggerType) {
        CrawlTaskLog log = CrawlTaskLog.builder()
                .taskDate(LocalDate.now())
                .startTime(LocalDateTime.now())
                .taskStatus("running")
                .totalCrawl(0)
                .successCount(0)
                .failCount(0)
                .operator(StrUtil.blankToDefault(operator, "auto"))
                .build();
        crawlTaskLogService.save(log);

        int total = 0;
        int success = 0;
        int fail = 0;
        List<String> failReasons = new ArrayList<>();

        for (CrawlerProperties.Source source : crawlerProperties.getSources()) {
            try {
                SourceResult result = crawlSource(source);
                total += result.getTotal();
                success += result.getSuccess();
                fail += result.getFail();
                if (StrUtil.isNotBlank(result.getFailReason())) {
                    failReasons.add(result.getFailReason());
                }
            } catch (Exception e) {
                fail++;
                failReasons.add(source.getName() + " 抓取失败：" + e.getMessage());
            }
        }

        log.setEndTime(LocalDateTime.now());
        log.setTotalCrawl(total);
        log.setSuccessCount(success);
        log.setFailCount(fail);
        log.setFailReason(JSONUtil.toJsonStr(failReasons));
        log.setTaskStatus(success > 0 ? "finished" : (fail > 0 ? "failed" : "finished"));
        crawlTaskLogService.updateById(log);
        return log;
    }

    private SourceResult crawlSource(CrawlerProperties.Source source) {
        SourceResult result = new SourceResult();
        if (source == null || StrUtil.isBlank(source.getName())) {
            result.setFailReason("来源配置为空");
            return result;
        }
        List<ArticleCandidate> candidates = new ArrayList<>();
        if (StrUtil.isNotBlank(source.getRssUrl())) {
            candidates = fetchFromRss(source);
        } else if (StrUtil.isNotBlank(source.getListUrl())) {
            candidates = fetchFromHtmlList(source);
        }

        int maxItems = crawlerProperties.getMaxItemsPerSource();
        int count = 0;
        for (ArticleCandidate candidate : candidates) {
            if (count >= maxItems) {
                break;
            }
            count++;
            result.total++;
            try {
                if (StrUtil.isBlank(candidate.getTitle()) || StrUtil.isBlank(candidate.getSourceUrl())) {
                    continue;
                }
                if (!isAllowed(candidate.getSourceUrl())) {
                    result.fail++;
                    result.failReason = source.getName() + " 被 robots 限制：" + candidate.getSourceUrl();
                    continue;
                }
                String contentText = candidate.getContent();
                if (StrUtil.isBlank(contentText)) {
                    contentText = fetchDetailContent(candidate.getSourceUrl(), source);
                }
                if (StrUtil.isBlank(contentText) || contentText.length() < crawlerProperties.getMinContentLength()) {
                    continue;
                }
                String summary = contentText.length() > 200 ? contentText.substring(0, 200) : contentText;
                String uniqueKey = DigestUtil.md5Hex(candidate.getTitle() + summary);
                if (aiArticleService.existsByUniqueKey(uniqueKey) || aiArticleService.existsBySourceUrl(candidate.getSourceUrl())) {
                    continue;
                }
                AiArticle article = AiArticle.builder()
                        .title(cleanTitle(candidate.getTitle()))
                        .sourceUrl(candidate.getSourceUrl())
                        .sourcePlatform(source.getName())
                        .author(StrUtil.blankToDefault(candidate.getAuthor(), source.getName()))
                        .publishTime(candidate.getPublishTime() == null ? LocalDateTime.of(1970, 1, 1, 0, 0) : candidate.getPublishTime())
                        .content(contentText)
                        .category(StrUtil.blankToDefault(candidate.getCategory(), detectCategory(candidate.getTitle(), contentText)))
                        .isFeatured(0)
                        .crawlTime(LocalDateTime.now())
                        .status("normal")
                        .uniqueKey(uniqueKey)
                        .views(0)
                        .build();
                aiArticleService.save(article);
                result.success++;
                sleepDelay();
            } catch (Exception e) {
                result.fail++;
                result.failReason = source.getName() + " 入库失败：" + e.getMessage();
            }
        }
        return result;
    }

    private List<ArticleCandidate> fetchFromRss(CrawlerProperties.Source source) {
        List<ArticleCandidate> results = new ArrayList<>();
        try {
            String xml = fetchUrl(source.getRssUrl());
            Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
            Elements items = doc.select("item");
            for (Element item : items) {
                String title = item.selectFirst("title") != null ? item.selectFirst("title").text() : "";
                String link = item.selectFirst("link") != null ? item.selectFirst("link").text() : "";
                String author = item.selectFirst("dc|creator") != null ? item.selectFirst("dc|creator").text() : "";
                String pubDate = item.selectFirst("pubDate") != null ? item.selectFirst("pubDate").text() : "";
                String content = "";
                Element encoded = item.selectFirst("content|encoded");
                if (encoded != null) {
                    content = cleanContent(encoded.text());
                } else if (item.selectFirst("description") != null) {
                    content = cleanContent(item.selectFirst("description").text());
                }
                ArticleCandidate candidate = new ArticleCandidate();
                candidate.setTitle(title);
                candidate.setSourceUrl(link);
                candidate.setAuthor(author);
                candidate.setPublishTime(parsePublishTime(pubDate));
                candidate.setContent(content);
                candidate.setCategory(source.getCategory());
                results.add(candidate);
            }
        } catch (Exception e) {
            log.warn("RSS抓取失败: {}", source.getRssUrl(), e);
        }
        return results;
    }

    private List<ArticleCandidate> fetchFromHtmlList(CrawlerProperties.Source source) {
        List<ArticleCandidate> results = new ArrayList<>();
        try {
            String html = fetchUrl(source.getListUrl());
            Document doc = Jsoup.parse(html);
            Elements items = StrUtil.isNotBlank(source.getItemSelector()) ? doc.select(source.getItemSelector()) : doc.select("article");
            for (Element item : items) {
                String title = selectText(item, source.getTitleSelector());
                String url = selectAttr(item, source.getUrlSelector(), "href");
                if (StrUtil.isBlank(url)) {
                    continue;
                }
                ArticleCandidate candidate = new ArticleCandidate();
                candidate.setTitle(title);
                candidate.setSourceUrl(normalizeUrl(source.getListUrl(), url));
                candidate.setCategory(source.getCategory());
                results.add(candidate);
            }
        } catch (Exception e) {
            log.warn("列表抓取失败: {}", source.getListUrl(), e);
        }
        return results;
    }

    private String fetchDetailContent(String url, CrawlerProperties.Source source) {
        try {
            String html = fetchUrl(url);
            Document doc = Jsoup.parse(html);
            Element contentElement = StrUtil.isNotBlank(source.getContentSelector())
                    ? doc.selectFirst(source.getContentSelector())
                    : doc.body();
            if (contentElement == null) {
                return "";
            }
            removeNoise(contentElement);
            return cleanContent(contentElement.text());
        } catch (Exception e) {
            return "";
        }
    }

    private String fetchUrl(String url) {
        return HttpRequest.get(url)
                .header("User-Agent", "WangAiCodeMotherCrawler/1.0 (+contact: admin@wang-ai-code-mother)")
                .timeout(15000)
                .execute()
                .body();
    }

    private boolean isAllowed(String url) {
        try {
            URI uri = URI.create(url);
            String host = uri.getScheme() + "://" + uri.getHost();
            List<String> disallow = robotsCache.computeIfAbsent(host, key -> loadRobots(key));
            String path = uri.getPath() == null ? "/" : uri.getPath();
            for (String rule : disallow) {
                if (path.startsWith(rule)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    private List<String> loadRobots(String host) {
        List<String> disallow = new ArrayList<>();
        try {
            String robots = fetchUrl(host + "/robots.txt");
            String[] lines = robots.split("\n");
            boolean matchAll = false;
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.startsWith("User-agent:")) {
                    matchAll = trimmed.toLowerCase(Locale.ROOT).contains("*");
                } else if (matchAll && trimmed.startsWith("Disallow:")) {
                    String rule = trimmed.replace("Disallow:", "").trim();
                    if (StrUtil.isNotBlank(rule)) {
                        disallow.add(rule);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return disallow;
    }

    private void removeNoise(Element root) {
        root.select("script,style,nav,header,footer,aside,form,iframe,button,ads,advertisement,.advertisement,.ad,.banner,.sponsor,.recommend").remove();
    }

    private String cleanTitle(String title) {
        if (StrUtil.isBlank(title)) {
            return "";
        }
        return title.replaceAll("\\s+", " ").replaceAll("[\\r\\n\\t]", "").trim();
    }

    private String cleanContent(String content) {
        if (StrUtil.isBlank(content)) {
            return "";
        }
        return content.replaceAll("\\s+", " ").trim();
    }

    private String detectCategory(String title, String content) {
        String text = (StrUtil.blankToDefault(title, "") + " " + StrUtil.blankToDefault(content, "")).toLowerCase(Locale.ROOT);
        if (text.contains("政策") || text.contains("法规")) {
            return "政策解读";
        }
        if (text.contains("教程") || text.contains("实战") || text.contains("指南")) {
            return "工具教程";
        }
        if (text.contains("行业") || text.contains("融资") || text.contains("公司")) {
            return "行业动态";
        }
        return "AI技术";
    }

    private LocalDateTime parsePublishTime(String raw) {
        if (StrUtil.isBlank(raw)) {
            return null;
        }
        String trimmed = raw.trim();
        try {
            return ZonedDateTime.parse(trimmed, DateTimeFormatter.RFC_1123_DATE_TIME).toLocalDateTime();
        } catch (Exception ignored) {
        }
        if (trimmed.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            try {
                return java.time.LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
            } catch (Exception ignored) {
            }
        }
        DateTimeFormatter[] formatters = new DateTimeFormatter[]{
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
        };
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(trimmed, formatter);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private String selectText(Element element, String selector) {
        if (StrUtil.isBlank(selector)) {
            return "";
        }
        Element target = element.selectFirst(selector);
        return target == null ? "" : target.text();
    }

    private String selectAttr(Element element, String selector, String attr) {
        if (StrUtil.isBlank(selector)) {
            return "";
        }
        Element target = element.selectFirst(selector);
        return target == null ? "" : target.attr(attr);
    }

    private String normalizeUrl(String baseUrl, String href) {
        try {
            URI base = URI.create(baseUrl);
            return base.resolve(href).toString();
        } catch (Exception e) {
            return href;
        }
    }

    private void sleepDelay() {
        try {
            Thread.sleep(crawlerProperties.getMinDelayMs());
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    @Data
    private static class ArticleCandidate {
        private String title;
        private String sourceUrl;
        private String author;
        private LocalDateTime publishTime;
        private String content;
        private String category;
    }

    @Data
    private static class SourceResult {
        private int total;
        private int success;
        private int fail;
        private String failReason;
    }
}
