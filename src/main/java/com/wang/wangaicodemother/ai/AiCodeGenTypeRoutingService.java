package com.wang.wangaicodemother.ai;

import com.wang.wangaicodemother.enums.CodeGenTypeEnum;
import dev.langchain4j.service.SystemMessage;

/**
 * AI代码生成类型路由服务
 */
public interface AiCodeGenTypeRoutingService {


    /**
     * 根据用户输入提示词自动选择生成代码类型
     *
     * @param userMessage
     * @return 生成的代码类型枚举
     */
    @SystemMessage(fromResource = "prompt/codegen-routing.txt")
    CodeGenTypeEnum routing(String userMessage);
}
