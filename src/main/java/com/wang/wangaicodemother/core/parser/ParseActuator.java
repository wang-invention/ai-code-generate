package com.wang.wangaicodemother.core.parser;

import com.wang.wangaicodemother.ai.model.HtmlCodeResult;
import com.wang.wangaicodemother.ai.model.MultiFileCodeResult;
import com.wang.wangaicodemother.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * 解析代码的执行器
 */
public class ParseActuator {

    private static final Parse<HtmlCodeResult> HTML_PARSE = new HtmlParse();
    private static final Parse<MultiFileCodeResult> MULTI_FILE_PARSE = new MultiFileParse();


    /**
     * 根据传入的类型解析代码
     * @param codeContent
     * @param codeGenTypeEnum
     * @return
     */
    public static Object parse(String codeContent, CodeGenTypeEnum codeGenTypeEnum) {
        return switch (codeGenTypeEnum) {
            case HTML:
                yield HTML_PARSE.codeParse(codeContent);
            case MULTI_FILE:
                yield MULTI_FILE_PARSE.codeParse(codeContent);
            default:
                throw new RuntimeException("不支持的生成类型");
        };
    }
}
