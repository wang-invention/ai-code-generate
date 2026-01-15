package com.wang.wangaicodemother.core.parser;

/**
 * 代码解析器接口
 * @param <T>
 */
public interface Parse<T> {
    T codeParse(String codeContent);
}
