package com.wang.wangaicodemother.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.wang.wangaicodemother.annotation.AuthCheck;
import com.wang.wangaicodemother.common.BaseResponse;
import com.wang.wangaicodemother.common.DeleteRequest;
import com.wang.wangaicodemother.common.ResultUtils;
import com.wang.wangaicodemother.constants.AppConstant;
import com.wang.wangaicodemother.constants.UserConstant;
import com.wang.wangaicodemother.enums.ChatHistoryMessageTypeEnum;
import com.wang.wangaicodemother.enums.CodeGenTypeEnum;
import com.wang.wangaicodemother.exception.BusinessException;
import com.wang.wangaicodemother.exception.ErrorCode;
import com.wang.wangaicodemother.exception.ThrowUtils;
import com.wang.wangaicodemother.model.dto.*;
import com.wang.wangaicodemother.model.entity.App;
import com.wang.wangaicodemother.model.entity.User;
import com.wang.wangaicodemother.model.vo.AppVO;
import com.wang.wangaicodemother.service.AppService;
import com.wang.wangaicodemother.service.ChatHistoryService;
import com.wang.wangaicodemother.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 应用 控制层。
 *
 * @author <a href="https://github.com">wangInvention</a>
 */
@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    @Resource
    private ChatHistoryService chatHistoryService;



    /**
     * 应用部署
     *
     * @param appDeployRequest 部署请求
     * @param request          请求
     * @return 部署 URL
     */
    @PostMapping("/deploy")
    public BaseResponse<String> deployApp(@RequestBody AppDeployRequest appDeployRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appDeployRequest == null, ErrorCode.PARAMS_ERROR);
        Long appId = appDeployRequest.getAppId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务部署应用
        String deployUrl = appService.deployApp(appId, loginUser);
        return ResultUtils.success(deployUrl);
    }


    /**
     * 、
     * 聊天生成代码
     *
     * @param appId
     * @param message
     * @param request
     * @return
     */

//  @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//public Flux<ServerSentEvent<String>> chatToGenCode(@RequestParam Long appId,
//                                                   @RequestParam String message,
//                                                   HttpServletRequest request) {
//    // 参数校验
//    ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
//    ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
//    // 获取当前登录用户
//    User loginUser = userService.getLoginUser(request);
//    // 保存用户的消息
//    chatHistoryService.addChatHistoryMessage(message, appId, loginUser.getId(), ChatHistoryMessageTypeEnum.USER.getValue());
//
//    // 模拟假消息流
//    Flux<String> contentFlux = Flux.just("假消息 1", "假消息 2", "假消息 3", "假消息 4")
//                                    .delayElements(Duration.ofSeconds(1));  // 每1秒发送一个假消息
//
//    // 保存AI聊天记录
//    StringBuilder AiChatContent = new StringBuilder();
//
//    // 转换为 ServerSentEvent 格式
//    return contentFlux
//            .map(chunk -> {
//                // 收集AI返回的消息
//                AiChatContent.append(chunk);
//                // 将内容包装成JSON对象
//                Map<String, String> wrapper = Map.of("d", chunk);
//                String jsonData = JSONUtil.toJsonStr(wrapper);
//                return ServerSentEvent.<String>builder()
//                        .data(jsonData)
//                        .build();
//            })
//            .concatWith(Mono.just(ServerSentEvent.<String>builder()
//                    .event("done")
//                    .data("")
//                    .build()))
//            .doOnComplete(
//                    () -> {
//                        String string = AiChatContent.toString();
//                        if (StrUtil.isNotBlank(string)) {
//                            // 收集完成后保存AI返回的消息
//                            chatHistoryService.addChatHistoryMessage(string, appId, loginUser.getId(), ChatHistoryMessageTypeEnum.AI.getValue());
//                        }
//                    }
//            )
//            .doOnError(
//                    error -> {
//                        String errMsg = "AI回复失败" + error.getMessage();
//                        chatHistoryService.addChatHistoryMessage(errMsg, appId, loginUser.getId(), ChatHistoryMessageTypeEnum.AI.getValue());
//                    }
//            );
//}

    @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatToGenCode(@RequestParam Long appId,
                                                       @RequestParam String message,
                                                       HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 保存用户的消息
        chatHistoryService.addChatHistoryMessage(message, appId, loginUser.getId(), ChatHistoryMessageTypeEnum.USER.getValue());

        // 模拟假消息流
        Flux<String> contentFlux = Flux.just("我将为您创建一个简洁优雅的个人博客，包含文章发布、评论和标签分类功能。这个博客将完全使用原生HTML、CSS和JavaScript实现。\n" +
                        "\n" +
                        "```html\n" +
                        "<!DOCTYPE html>\n" +
                        "<html lang=\"zh-CN\">\n" +
                        "<head>\n" +
                        "    <meta charset=\"UTF-8\">\n" +
                        "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                        "    <title>清雅博客 - 个人写作空间</title>\n" +
                        "    <link rel=\"stylesheet\" href=\"style.css\">\n" +
                        "    <link rel=\"icon\" type=\"image/x-icon\" href=\"data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 100 100%22><text y=%22.9em%22 font-size=%2290%22>✍\uFE0F</text></svg>\">\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "    <!-- 导航栏 -->\n" +
                        "    <header class=\"header\">\n" +
                        "        <div class=\"container\">\n" +
                        "            <div class=\"logo\">\n" +
                        "                <h1>清雅博客</h1>\n" +
                        "                <p class=\"tagline\">记录思考，分享生活</p>\n" +
                        "            </div>\n" +
                        "            <nav class=\"nav\">\n" +
                        "                <button class=\"nav-toggle\" aria-label=\"菜单\">\n" +
                        "                    <span></span>\n" +
                        "                    <span></span>\n" +
                        "                    <span></span>\n" +
                        "                </button>\n" +
                        "                <ul class=\"nav-menu\">\n" +
                        "                    <li><a href=\"#home\" class=\"active\">首页</a></li>\n" +
                        "                    <li><a href=\"#articles\">文章</a></li>\n" +
                        "                    <li><a href=\"#tags\">标签</a></li>\n" +
                        "                    <li><a href=\"#about\">关于</a></li>\n" +
                        "                </ul>\n" +
                        "            </nav>\n" +
                        "        </div>\n" +
                        "    </header>\n" +
                        "\n" +
                        "    <!-- 主要内容区域 -->\n" +
                        "    <main class=\"main-content\">\n" +
                        "        <div class=\"container\">\n" +
                        "            <!-- 欢迎区域 -->\n" +
                        "            <section id=\"home\" class=\"welcome-section\">\n" +
                        "                <div class=\"welcome-card\">\n" +
                        "                    <h2>欢迎来到我的博客</h2>\n" +
                        "                    <p>这里是我分享技术思考、生活感悟和个人项目的地方。每一篇文章都记录了我的学习和成长历程。</p>\n" +
                        "                    <button id=\"new-post-btn\" class=\"btn-primary\">发布新文章</button>\n" +
                        "                </div>\n" +
                        "            </section>\n" +
                        "\n" +
                        "            <!-- 文章发布表单 -->\n" +
                        "            <section id=\"post-form\" class=\"post-form-section\" style=\"display: none;\">\n" +
                        "                <div class=\"form-container\">\n" +
                        "                    <h2>发布新文章</h2>\n" +
                        "                    <form id=\"article-form\">\n" +
                        "                        <div class=\"form-group\">\n" +
                        "                            <label for=\"article-title\">文章标题</label>\n" +
                        "                            <input type=\"text\" id=\"article-title\" required placeholder=\"请输入文章标题\">\n" +
                        "                        </div>\n" +
                        "                        <div class=\"form-group\">\n" +
                        "                            <label for=\"article-content\">文章内容</label>\n" +
                        "                            <textarea id=\"article-content\" rows=\"10\" required placeholder=\"请输入文章内容...\"></textarea>\n" +
                        "                        </div>\n" +
                        "                        <div class=\"form-group\">\n" +
                        "                            <label for=\"article-tags\">标签（用逗号分隔）</label>\n" +
                        "                            <input type=\"text\" id=\"article-tags\" placeholder=\"例如：技术, 生活, 学习\">\n" +
                        "                        </div>\n" +
                        "                        <div class=\"form-actions\">\n" +
                        "                            <button type=\"submit\" class=\"btn-primary\">发布文章</button>\n" +
                        "                            <button type=\"button\" id=\"cancel-post\" class=\"btn-secondary\">取消</button>\n" +
                        "                        </div>\n" +
                        "                    </form>\n" +
                        "                </div>\n" +
                        "            </section>\n" +
                        "\n" +
                        "            <!-- 文章列表和标签侧边栏 -->\n" +
                        "            <div class=\"content-wrapper\">\n" +
                        "                <!-- 文章列表 -->\n" +
                        "                <section id=\"articles\" class=\"articles-section\">\n" +
                        "                    <h2 class=\"section-title\">最新文章</h2>\n" +
                        "                    <div id=\"articles-container\" class=\"articles-container\">\n" +
                        "                        <!-- 文章将通过JavaScript动态加载 -->\n" +
                        "                    </div>\n" +
                        "                </section>\n" +
                        "\n" +
                        "                <!-- 侧边栏 -->\n" +
                        "                <aside class=\"sidebar\">\n" +
                        "                    <!-- 标签分类 -->\n" +
                        "                    <section id=\"tags\" class=\"tags-section\">\n" +
                        "                        <h3>标签分类</h3>\n" +
                        "                        <div id=\"tags-container\" class=\"tags-container\">\n" +
                        "                            <!-- 标签将通过JavaScript动态加载 -->\n" +
                        "                        </div>\n" +
                        "                    </section>\n" +
                        "\n" +
                        "                    <!-- 关于我 -->\n" +
                        "                    <section id=\"about\" class=\"about-section\">\n" +
                        "                        <h3>关于我</h3>\n" +
                        "                        <div class=\"about-card\">\n" +
                        "                            <img src=\"https://picsum.photos/150/150?random=1\" alt=\"作者头像\" class=\"author-avatar\">\n" +
                        "                            <p>一名热爱技术、喜欢写作的前端开发者。在这里记录学习笔记，分享生活感悟。</p>\n" +
                        "                            <div class=\"contact-info\">\n" +
                        "                                <span>\uD83D\uDCE7 contact@example.com</span>\n" +
                        "                            </div>\n" +
                        "                        </div>\n" +
                        "                    </section>\n" +
                        "                </aside>\n" +
                        "            </div>\n" +
                        "        </div>\n" +
                        "    </main>\n" +
                        "\n" +
                        "    <!-- 文章详情模态框 -->\n" +
                        "    <div id=\"article-modal\" class=\"modal\">\n" +
                        "        <div class=\"modal-content\">\n" +
                        "            <button class=\"modal-close\">&times;</button>\n" +
                        "            <article id=\"article-detail\">\n" +
                        "                <!-- 文章详情将通过JavaScript动态加载 -->\n" +
                        "            </article>\n" +
                        "            \n" +
                        "            <!-- 评论区域 -->\n" +
                        "            <section class=\"comments-section\">\n" +
                        "                <h3>评论</h3>\n" +
                        "                <div id=\"comments-container\">\n" +
                        "                    <!-- 评论将通过JavaScript动态加载 -->\n" +
                        "                </div>\n" +
                        "                <form id=\"comment-form\" class=\"comment-form\">\n" +
                        "                    <div class=\"form-group\">\n" +
                        "                        <label for=\"comment-author\">昵称</label>\n" +
                        "                        <input type=\"text\" id=\"comment-author\" required placeholder=\"请输入您的昵称\">\n" +
                        "                    </div>\n" +
                        "                    <div class=\"form-group\">\n" +
                        "                        <label for=\"comment-content\">评论内容</label>\n" +
                        "                        <textarea id=\"comment-content\" rows=\"3\" required placeholder=\"请输入您的评论...\"></textarea>\n" +
                        "                    </div>\n" +
                        "                    <button type=\"submit\" class=\"btn-primary\">提交评论</button>\n" +
                        "                </form>\n" +
                        "            </section>\n" +
                        "        </div>\n" +
                        "    </div>\n" +
                        "\n" +
                        "    <!-- 页脚 -->\n" +
                        "    <footer class=\"footer\">\n" +
                        "        <div class=\"container\">\n" +
                        "            <p>&copy; 2023 清雅博客. 保留所有权利.</p>\n" +
                        "            <p>用心写作，真诚分享</p>\n" +
                        "        </div>\n" +
                        "    </footer>\n" +
                        "\n" +
                        "    <script src=\"script.js\"></script>\n" +
                        "</body>\n" +
                        "</html>\n" +
                        "```\n" +
                        "\n" +
                        "```css\n" +
                        "/* 重置和基础样式 */\n" +
                        "* {\n" +
                        "    margin: 0;\n" +
                        "    padding: 0;\n" +
                        "    box-sizing: border-box;\n" +
                        "}\n" +
                        "\n" +
                        ":root {\n" +
                        "    --primary-color: #4a6fa5;\n" +
                        "    --secondary-color: #6b8cbc;\n" +
                        "    --accent-color: #ff7e5f;\n" +
                        "    --light-color: #f8f9fa;\n" +
                        "    --dark-color: #343a40;\n" +
                        "    --gray-color: #6c757d;\n" +
                        "    --light-gray: #e9ecef;\n" +
                        "    --border-radius: 8px;\n" +
                        "    --box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);\n" +
                        "    --transition: all 0.3s ease;\n" +
                        "}\n" +
                        "\n" +
                        "body {\n" +
                        "    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;\n" +
                        "    line-height: 1.6;\n" +
                        "    color: var(--dark-color);\n" +
                        "    background-color: #f5f7fa;\n" +
                        "}\n" +
                        "\n" +
                        ".container {\n" +
                        "    width: 100%;\n" +
                        "    max-width: 1200px;\n" +
                        "    margin: 0 auto;\n" +
                        "    padding: 0 20px;\n" +
                        "}\n" +
                        "\n" +
                        "/* 头部样式 */\n" +
                        ".header {\n" +
                        "    background-color: white;\n" +
                        "    box-shadow: var(--box-shadow);\n" +
                        "    position: sticky;\n" +
                        "    top: 0;\n" +
                        "    z-index: 1000;\n" +
                        "}\n" +
                        "\n" +
                        ".header .container {\n" +
                        "    display: flex;\n" +
                        "    justify-content: space-between;\n" +
                        "    align-items: center;\n" +
                        "    padding: 1rem 20px;\n" +
                        "}\n" +
                        "\n" +
                        ".logo h1 {\n" +
                        "    font-size: 1.8rem;\n" +
                        "    color: var(--primary-color);\n" +
                        "    margin-bottom: 0.2rem;\n" +
                        "}\n" +
                        "\n" +
                        ".tagline {\n" +
                        "    font-size: 0.9rem;\n" +
                        "    color: var(--gray-color);\n" +
                        "}\n" +
                        "\n" +
                        "/* 导航样式 */\n" +
                        ".nav-menu {\n" +
                        "    display: flex;\n" +
                        "    list-style: none;\n" +
                        "    gap: 2rem;\n" +
                        "}\n" +
                        "\n" +
                        ".nav-menu a {\n" +
                        "    text-decoration: none;\n" +
                        "    color: var(--dark-color);\n" +
                        "    font-weight: 500;\n" +
                        "    padding: 0.5rem 0;\n" +
                        "    position: relative;\n" +
                        "    transition: var(--transition);\n" +
                        "}\n" +
                        "\n" +
                        ".nav-menu a:hover,\n" +
                        ".nav-menu a.active {\n" +
                        "    color: var(--primary-color);\n" +
                        "}\n" +
                        "\n" +
                        ".nav-menu a.active::after {\n" +
                        "    content: '';\n" +
                        "    position: absolute;\n" +
                        "    bottom: 0;\n" +
                        "    left: 0;\n" +
                        "    width: 100%;\n" +
                        "    height: 2px;\n" +
                        "    background-color: var(--primary-color);\n" +
                        "}\n" +
                        "\n" +
                        ".nav-toggle {\n" +
                        "    display: none;\n" +
                        "    background: none;\n" +
                        "    border: none;\n" +
                        "    cursor: pointer;\n" +
                        "    padding: 0.5rem;\n" +
                        "}\n" +
                        "\n" +
                        ".nav-toggle span {\n" +
                        "    display: block;\n" +
                        "    width: 25px;\n" +
                        "    height: 3px;\n" +
                        "    background-color: var(--dark-color);\n" +
                        "    margin: 4px 0;\n" +
                        "    transition: var(--transition);\n" +
                        "}\n" +
                        "\n" +
                        "/* 主要内容区域 */\n" +
                        ".main-content {\n" +
                        "    padding: 2rem 0;\n" +
                        "}\n" +
                        "\n" +
                        "/* 欢迎区域 */\n" +
                        ".welcome-section {\n" +
                        "    margin-bottom: 3rem;\n" +
                        "}\n" +
                        "\n" +
                        ".welcome-card {\n" +
                        "    background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));\n" +
                        "    color: white;\n" +
                        "    padding: 3rem;\n" +
                        "    border-radius: var(--border-radius);\n" +
                        "    text-align: center;\n" +
                        "    box-shadow: var(--box-shadow);\n" +
                        "}\n" +
                        "\n" +
                        ".welcome-card h2 {\n" +
                        "    font-size: 2.2rem;\n" +
                        "    margin-bottom: 1rem;\n" +
                        "}\n" +
                        "\n" +
                        ".welcome-card p {\n" +
                        "    font-size: 1.1rem;\n" +
                        "    margin-bottom: 2rem;\n" +
                        "    opacity: 0.9;\n" +
                        "}\n" +
                        "\n" +
                        "/* 按钮样式 */\n" +
                        ".btn-primary, .btn-secondary {\n" +
                        "    padding: 0.8rem 1.8rem;\n" +
                        "    border: none;\n" +
                        "    border-radius: var(--border-radius);\n" +
                        "    font-size: 1rem;\n" +
                        "    font-weight: 600;\n" +
                        "    cursor: pointer;\n" +
                        "    transition: var(--transition);\n" +
                        "}\n" +
                        "\n" +
                        ".btn-primary {\n" +
                        "    background-color: var(--accent-color);\n" +
                        "    color: white;\n" +
                        "}\n" +
                        "\n" +
                        ".btn-primary:hover {\n" +
                        "    background-color: #ff6b4a;\n" +
                        "    transform: translateY(-2px);\n" +
                        "    box-shadow: 0 6px 12px rgba(255, 126, 95, 0.3);\n" +
                        "}\n" +
                        "\n" +
                        ".btn-secondary {\n" +
                        "    background-color: var(--light-gray);\n" +
                        "    color: var(--dark-color);\n" +
                        "}\n" +
                        "\n" +
                        ".btn-secondary:hover {\n" +
                        "    background-color: #dde1e6;\n" +
                        "}\n" +
                        "\n" +
                        "/* 文章发布表单 */\n" +
                        ".post-form-section {\n" +
                        "    margin-bottom: 3rem;\n" +
                        "}\n" +
                        "\n" +
                        ".form-container {\n" +
                        "    background-color: white;\n" +
                        "    padding: 2rem;\n" +
                        "    border-radius: var(--border-radius);\n" +
                        "    box-shadow: var(--box-shadow);\n" +
                        "}\n" +
                        "\n" +
                        ".form-container h2 {\n" +
                        "    margin-bottom: 1.5rem;\n" +
                        "    color: var(--primary-color);\n" +
                        "}\n" +
                        "\n" +
                        ".form-group {\n" +
                        "    margin-bottom: 1.5rem;\n" +
                        "}\n" +
                        "\n" +
                        ".form-group label {\n" +
                        "    display: block;\n" +
                        "    margin-bottom: 0.5rem;\n" +
                        "    font-weight: 600;\n" +
                        "    color: var(--dark-color);\n" +
                        "}\n" +
                        "\n" +
                        ".form-group input,\n" +
                        ".form-group textarea {\n" +
                        "    width: 100%;\n" +
                        "    padding: 0.8rem;\n" +
                        "    border: 1px solid var(--light-gray);\n" +
                        "    border-radius: var(--border-radius);\n" +
                        "    font-size: 1rem;\n" +
                        "    font-family: inherit;\n" +
                        "    transition: var(--transition);\n" +
                        "}\n" +
                        "\n" +
                        ".form-group input:focus,\n" +
                        ".form-group textarea:focus {\n" +
                        "    outline: none;\n" +
                        "    border-color: var(--primary-color);\n" +
                        "    box-shadow: 0 0 0 3px rgba(74, 111, 165, 0.1);\n" +
                        "}\n" +
                        "\n" +
                        ".form-actions {\n" +
                        "    display: flex;\n" +
                        "    gap: 1rem;\n" +
                        "    justify-content: flex-end;\n" +
                        "}\n" +
                        "\n" +
                        "/* 内容包装器 */\n" +
                        ".content-wrapper {\n" +
                        "    display: grid;\n" +
                        "    grid-template-columns: 2fr 1fr;\n" +
                        "    gap: 2rem;\n" +
                        "}\n" +
                        "\n" +
                        "/* 文章列表 */\n" +
                        ".section-title {\n" +
                        "    font-size: 1.8rem;\n" +
                        "    color: var(--primary-color);\n" +
                        "    margin-bottom: 1.5rem;\n" +
                        "    padding-bottom: 0.5rem;\n" +
                        "    border-bottom: 2px solid var(--light-gray);\n" +
                        "}\n" +
                        "\n" +
                        ".articles-container {\n" +
                        "    display: grid;\n" +
                        "    gap: 1.5rem;\n" +
                        "}\n" +
                        "\n" +
                        ".article-card {\n" +
                        "    background-color: white;\n" +
                        "    border-radius: var(--border-radius);\n" +
                        "    padding: 1.5rem;\n" +
                        "    box-shadow: var(--box-shadow);\n" +
                        "    transition: var(--transition);\n" +
                        "    cursor: pointer;\n" +
                        "}\n" +
                        "\n" +
                        ".article-card:hover {\n" +
                        "    transform: translateY(-5px);\n" +
                        "    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);\n" +
                        "}\n" +
                        "\n" +
                        ".article-header {\n" +
                        "    display: flex;\n" +
                        "    justify-content: space-between;\n" +
                        "    align-items: flex-start;\n" +
                        "    margin-bottom: 1rem;\n" +
                        "}\n" +
                        "\n" +
                        ".article-title {\n" +
                        "    font-size: 1.3rem;\n" +
                        "    color: var(--dark-color);\n" +
                        "    margin-bottom: 0.5rem;\n" +
                        "}\n" +
                        "\n" +
                        ".article-date {\n" +
                        "    color: var(--gray-color);\n" +
                        "    font-size: 0.9rem;\n" +
                        "}\n" +
                        "\n" +
                        ".article-excerpt {\n" +
                        "    color: var(--gray-color);\n" +
                        "    margin-bottom: 1rem;\n" +
                        "    line-height: 1.6;\n" +
                        "}\n" +
                        "\n" +
                        ".article-tags {\n" +
                        "    display: flex;\n" +
                        "    flex-wrap: wrap;\n" +
                        "    gap: 0.5rem;\n" +
                        "}\n" +
                        "\n" +
                        ".tag {\n" +
                        "    background-color: var(--light-gray);\n" +
                        "    color: var(--primary-color);\n" +
                        "    padding: 0.3rem 0.8rem;\n" +
                        "    border-radius: 20px;\n" +
                        "    font-size: 0.85rem;\n" +
                        "    font-weight: 500;\n" +
                        "}\n" +
                        "\n" +
                        "/* 侧边栏 */\n" +
                        ".sidebar {\n" +
                        "    display: flex;\n" +
                        "    flex-direction: column;\n" +
                        "    gap: 2rem;\n" +
                        "}\n" +
                        "\n" +
                        ".tags-section h3,\n" +
                        ".about-section h3 {\n" +
                        "    font-size: 1.3rem;\n" +
                        "    color: var(--primary-color);\n" +
                        "    margin-bottom: 1rem;\n" +
                        "}\n" +
                        "\n" +
                        ".tags-container {\n" +
                        "    display: flex;\n" +
                        "    flex-wrap: wrap;\n" +
                        "    gap: 0.8rem;\n" +
                        "}\n" +
                        "\n" +
                        ".tag-item {\n" +
                        "    background-color: white;\n" +
                        "    padding: 0.5rem 1rem;\n" +
                        "    border-radius: var(--border-radius);\n" +
                        "    box-shadow: var(--box-shadow);\n" +
                        "    cursor: pointer;\n" +
                        "    transition: var(--transition);\n" +
                        "}\n" +
                        "\n" +
                        ".tag-item:hover {\n" +
                        "    background-color: var(--primary-color);\n" +
                        "    color: white;\n" +
                        "    transform: translateY(-2px);\n" +
                        "}\n" +
                        "\n" +
                        ".tag-count {\n" +
                        "    font-size: 0.8rem;\n" +
                        "    color: var(--gray-color);\n" +
                        "    margin-left: 0.3rem;\n" +
                        "}\n" +
                        "\n" +
                        ".about-card {\n" +
                        "    background-color: white;\n" +
                        "    padding: 1.5rem;\n" +
                        "    border-radius: var(--border-radius);\n" +
                        "    box-shadow: var(--box-shadow);\n" +
                        "    text-align: center;\n" +
                        "}\n" +
                        "\n" +
                        ".author-avatar {\n" +
                        "    width: 100px;\n" +
                        "    height: 100px;\n" +
                        "    border-radius: 50%;\n" +
                        "    margin: 0 auto 1rem;\n" +
                        "    display: block;\n" +
                        "    object-fit: cover;\n" +
                        "}\n" +
                        "\n" +
                        ".contact-info {\n" +
                        "    margin-top: 1rem;\n" +
                        "    padding-top: 1rem;\n" +
                        "    border-top: 1px solid var(--light-gray);\n" +
                        "}\n" +
                        "\n" +
                        "/* 模态框 */\n" +
                        ".modal {\n" +
                        "    display: none;\n" +
                        "    position: fixed;\n" +
                        "    top: 0;\n" +
                        "    left: 0;\n" +
                        "    width: 100%;\n" +
                        "    height: 100%;\n" +
                        "    background-color: rgba(0, 0, 0, 0.7);\n" +
                        "    z-index: 2000;\n" +
                        "    overflow-y: auto;\n" +
                        "}\n" +
                        "\n" +
                        ".modal-content {\n" +
                        "    background-color: white;\n" +
                        "    margin: 2rem auto;\n" +
                        "    padding: 2rem;\n" +
                        "    border-radius: var(--border-radius);\n" +
                        "    max-width: 800px;\n" +
                        "    position: relative;\n" +
                        "}\n" +
                        "\n" +
                        ".modal-close {\n" +
                        "    position: absolute;\n" +
                        "    top: 1rem;\n" +
                        "    right: 1rem;\n" +
                        "    background: none;\n" +
                        "    border: none;\n" +
                        "    font-size: 1.5rem;\n" +
                        "    cursor: pointer;\n" +
                        "    color: var(--gray-color);\n" +
                        "}\n" +
                        "\n" +
                        "#article-detail {\n" +
                        "    margin-bottom: 2rem;\n" +
                        "}\n" +
                        "\n" +
                        ".article-full-content {\n" +
                        "    line-height: 1.8;\n" +
                        "    margin: 1.5rem 0;\n" +
                        "}\n" +
                        "\n" +
                        "/* 评论区域 */\n" +
                        ".comments-section h3 {\n" +
                        "    color: var(--primary-color);\n" +
                        "    margin-bottom: 1.5rem;\n" +
                        "}\n" +
                        "\n" +
                        ".comment {\n" +
                        "    background-color: var(--light-gray);\n" +
                        "    padding: 1rem;\n" +
                        "    border-radius: var(--border-radius);\n" +
                        "    margin-bottom: 1rem;\n" +
                        "}\n" +
                        "\n" +
                        ".comment-header {\n" +
                        "    display: flex;\n" +
                        "    justify-content: space-between;\n" +
                        "    margin-bottom: 0.5rem;\n" +
                        "}\n" +
                        "\n" +
                        ".comment-author {\n" +
                        "    font-weight: 600;\n" +
                        "    color: var(--primary-color);\n" +
                        "}\n" +
                        "\n" +
                        ".comment-date {\n" +
                        "    color: var(--gray-color);\n" +
                        "    font-size: 0.9rem;\n" +
                        "}\n" +
                        "\n" +
                        ".comment-form {\n" +
                        "    margin-top: 2rem;\n" +
                        "    padding-top: 2rem;\n" +
                        "    border-top: 1px solid var(--light-gray);\n" +
                        "}\n" +
                        "\n" +
                        "/* 页脚 */\n" +
                        ".footer {\n" +
                        "    background-color: var(--dark-color);\n" +
                        "    color: white;\n" +
                        "    text-align: center;\n" +
                        "    padding: 2rem 0;\n" +
                        "    margin-top: 3rem;\n" +
                        "}\n" +
                        "\n" +
                        ".footer p {\n" +
                        "    margin-bottom: 0.5rem;\n" +
                        "}\n" +
                        "\n" +
                        ".footer p:last-child {\n" +
                        "    color: var(--light-gray);\n" +
                        "    font-size: 0.9rem;\n" +
                        "}\n" +
                        "\n" +
                        "/* 响应式设计 */\n" +
                        "@media (max-width: 992px) {\n" +
                        "    .content-wrapper {\n" +
                        "        grid-template-columns: 1fr;\n" +
                        "    }\n" +
                        "    \n" +
                        "    .sidebar {\n" +
                        "        order: -1;\n" +
                        "    }\n" +
                        "}\n" +
                        "\n" +
                        "@media (max-width: 768px) {\n" +
                        "    .nav-toggle {\n" +
                        "        display: block;\n" +
                        "    }\n" +
                        "    \n" +
                        "    .nav-menu {\n" +
                        "        position: fixed;\n" +
                        "        top: 70px;\n" +
                        "        left: 0;\n" +
                        "        width: 100%;\n" +
                        "        background-color: white;\n" +
                        "        flex-direction: column;\n" +
                        "        padding: 1rem;\n" +
                        "        box-shadow: var(--box-shadow);\n" +
                        "        transform: translateY(-100%);\n" +
                        "        opacity: 0;\n" +
                        "        visibility: hidden;\n" +
                        "        transition: var(--transition);\n" +
                        "    }\n" +
                        "    \n" +
                        "    .nav-menu.active {\n" +
                        "        transform: translateY(0);\n" +
                        "        opacity: 1;\n" +
                        "        visibility: visible;\n" +
                        "    }\n" +
                        "    \n" +
                        "    .welcome-card {\n" +
                        "        padding: 2rem 1rem;\n" +
                        "    }\n" +
                        "    \n" +
                        "    .welcome-card h2 {\n" +
                        "        font-size: 1.8rem;\n" +
                        "    }\n" +
                        "    \n" +
                        "    .form-actions {\n" +
                        "        flex-direction: column;\n" +
                        "    }\n" +
                        "    \n" +
                        "    .modal-content {\n" +
                        "        margin: 1rem;\n" +
                        "        padding: 1.5rem;\n" +
                        "    }\n")
                .delayElements(Duration.ofSeconds(1));  // 每1秒发送一个假消息

        // 保存AI聊天记录
        StringBuilder AiChatContent = new StringBuilder();

        // 转换为 ServerSentEvent 格式
        return contentFlux
                .map(chunk -> {
                    // 收集AI返回的消息
                    AiChatContent.append(chunk);
                    // 将内容包装成JSON对象
                    Map<String, String> wrapper = Map.of("d", chunk);
                    String jsonData = JSONUtil.toJsonStr(wrapper);
                    return ServerSentEvent.<String>builder()
                            .data(jsonData)
                            .build();
                })
                .concatWith(Mono.just(ServerSentEvent.<String>builder()
                        .event("done")
                        .data("")
                        .build()))
                .doOnComplete(
                        () -> {
                            String string = AiChatContent.toString();
                            if (StrUtil.isNotBlank(string)) {
                                // 收集完成后保存AI返回的消息
                                chatHistoryService.addChatHistoryMessage(string, appId, loginUser.getId(), ChatHistoryMessageTypeEnum.AI.getValue());
                            }
                        }
                )
                .doOnError(
                        error -> {
                            String errMsg = "AI回复失败" + error.getMessage();
                            chatHistoryService.addChatHistoryMessage(errMsg, appId, loginUser.getId(), ChatHistoryMessageTypeEnum.AI.getValue());
                        }
                );
    }

    /**
     * 管理员根据 id 获取应用详情
     *
     * @param id 应用 id
     * @return 应用详情
     */
    @GetMapping("/admin/get/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<AppVO> getAppVOByIdByAdmin(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(appService.getAppVO(app));
    }

    /**
     * 管理员分页获取应用列表
     *
     * @param appQueryRequest 查询请求
     * @return 应用列表
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AppVO>> listAppVOByPageByAdmin(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
        // 数据封装
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }


    /**
     * 管理员更新应用
     *
     * @param appAdminUpdateRequest 更新请求
     * @return 更新结果
     */
    @PostMapping("/admin/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateAppByAdmin(@RequestBody AppAdminUpdateRequest appAdminUpdateRequest) {
        if (appAdminUpdateRequest == null || appAdminUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = appAdminUpdateRequest.getId();
        // 判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        App app = new App();
        BeanUtil.copyProperties(appAdminUpdateRequest, app);
        // 设置编辑时间
        app.setEditTime(LocalDateTime.now());
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    /**
     * 管理员删除应用
     *
     * @param deleteRequest 删除请求
     * @return 删除结果
     */
    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteAppByAdmin(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        // 判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = appService.removeById(id);
        return ResultUtils.success(result);
    }


    /**
     * 分页获取当前用户创建的应用列表
     *
     * @param appQueryRequest 查询请求
     * @param request         请求
     * @return 应用列表
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<AppVO>> listMyAppVOByPage(@RequestBody AppQueryRequest appQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        // 限制每页最多 20 个
        long pageSize = appQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个应用");
        long pageNum = appQueryRequest.getPageNum();
        // 只查询当前用户的应用
        appQueryRequest.setUserId(loginUser.getId());
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
        // 数据封装
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }


    /**
     * 获取精选应用列表
     *
     * @param appQueryRequest
     * @return
     */
    @PostMapping("/good/list/page/vo")
    public BaseResponse<Page<AppVO>> listGoodAppVOByPage(@RequestBody AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long pageSize = appQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个应用");
        long pageNum = appQueryRequest.getPageNum();
        appQueryRequest.setPriority(AppConstant.GOOD_APP_PRIORITY);
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }


    /**
     * 根据 id 获取应用详情
     *
     * @param id 应用 id
     * @return 应用详情
     */
    @GetMapping("/get/vo")
    public BaseResponse<AppVO> getAppVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        System.err.println("id: " + id);
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类（包含用户信息）
        return ResultUtils.success(appService.getAppVO(app));
    }


    /**
     * 更新应用（用户只能更新自己的应用名称）
     *
     * @param appUpdateRequest 更新请求
     * @param request          请求
     * @return 更新结果
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        if (appUpdateRequest == null || appUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long id = appUpdateRequest.getId();
        // 判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人可更新
        if (!oldApp.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        App app = new App();
        app.setId(id);
        app.setAppName(appUpdateRequest.getAppName());
        // 设置编辑时间
        app.setEditTime(LocalDateTime.now());
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    /**
     * 删除应用（用户只能删除自己的应用）
     *
     * @param deleteRequest 删除请求
     * @param request       请求
     * @return 删除结果
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldApp.getUserId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 删除相关聊天记录
        chatHistoryService.deleteByAppId(id);
        boolean result = appService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 创建应用
     *
     * @param appAddRequest 创建应用请求
     * @param request       请求
     * @return 应用 id
     */
    @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
        // 参数校验
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化 prompt 不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 构造入库对象
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        // 应用名称暂时为 initPrompt 前 12 位
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
        // 暂时设置为多文件生成
        app.setCodeGenType(CodeGenTypeEnum.MULTI_FILE.getValue());
        // 插入数据库
        boolean result = appService.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(app.getId());
    }

}
