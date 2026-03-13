package com.wang.wangaicodemother.rag;

import reactor.core.publisher.Flux;

public interface AIRagService {

    /**
     * rag + AI知识问答系统
     *
     * @param userQuestion
     * @return
     */
    Flux<String> ragAndAI(String userQuestion);
}
