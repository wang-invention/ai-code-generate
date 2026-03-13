package com.wang.wangaicodemother.rag.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import reactor.core.publisher.Flux;

public interface AiChatService {

//    // 给AI设定角色
//    @SystemMessage("你是一个乐于助人的智能助手，回答简洁、专业、友好。")
//
//    // 接收用户问题，返回回答
//    String chat(@UserMessage("用户问题：{{question}}") @V("question") String question);


    // 流式输出
    @SystemMessage("你是一个乐于助人的智能助手，回答简洁、专业、友好。")
    Flux<String> streamChat(
            @UserMessage("{{prompt}}")
            @V("prompt") String prompt
    );



}