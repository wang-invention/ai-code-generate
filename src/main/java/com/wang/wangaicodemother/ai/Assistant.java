package com.wang.wangaicodemother.ai;

import dev.langchain4j.service.SystemMessage;

/**
 * AI 对话接口(输入用户信息，返回AI输出的内容)
 *
 * @author wang
 */
interface Assistant {

    /**
     * 生成html
     * SystemMessage(提示词)
     *
     * @param userMessage
     * @return
     */
    @SystemMessage(fromResource = "prompt/html.txt")
    String generateHtmlCode(String userMessage);

    /**
     * 生成html，css,js多文件
     * SystemMessage(提示词)
     *
     * @param userMessage
     * @return
     */
    @SystemMessage(fromResource = "prompt/multi-file.txt")
    String generateMultiCode(String userMessage);


}