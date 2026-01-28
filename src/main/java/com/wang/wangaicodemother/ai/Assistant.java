package com.wang.wangaicodemother.ai;

import com.wang.wangaicodemother.ai.model.HtmlCodeResult;
import com.wang.wangaicodemother.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

/**
 * AI 对话接口(输入用户信息，返回AI输出的内容)
 *
 * @author wang
 */
public interface Assistant {

    /**
     * 生成html
     * SystemMessage(提示词)
     *
     * @param userMessage
     * @return
     */
    @SystemMessage(fromResource = "prompt/html.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);

    /**
     * 生成html，css,js多文件
     * SystemMessage(提示词)
     *
     * @param userMessage
     * @return
     */
    @SystemMessage(fromResource = "prompt/multi-file.txt")
    MultiFileCodeResult generateMultiCode(String userMessage);


    /**
     * 生成html
     * SystemMessage(提示词)
     *
     * @param userMessage
     * @return
     */
    @SystemMessage(fromResource = "prompt/html.txt")
    Flux<String> generateHtmlCodeStream(String userMessage);

    /**
     * 生成html，css,js多文件
     * SystemMessage(提示词)
     *
     * @param userMessage
     * @return
     */
    @SystemMessage(fromResource = "prompt/multi-file.txt")
    Flux<String> generateMultiCodeStream(String userMessage);


    /**
     * 生成 Vue 项目代码（流式）
     *
     * @param userMessage 用户消息
     * @return 生成过程的流式响应
     */
    @SystemMessage(fromResource = "prompt/vue.txt")
    TokenStream generateVueProjectCodeStream(@MemoryId long appId, @UserMessage String userMessage);

}