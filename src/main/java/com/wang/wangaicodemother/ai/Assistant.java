package com.wang.wangaicodemother.ai;

import com.wang.wangaicodemother.ai.model.HtmlCodeResult;
import com.wang.wangaicodemother.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.SystemMessage;

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


}