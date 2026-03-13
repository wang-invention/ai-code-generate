package com.wang.wangaicodemother.rag.controller;


import com.wang.wangaicodemother.rag.AIRagService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/rag")
public class RagController {


    @Resource
    private AIRagService aiRagService;


    @GetMapping(value = "/chat/{userQuestion}",produces = "text/event-stream;charset=utf-8")
    public SseEmitter chatRagAndAI(@PathVariable("userQuestion") String userQuestion) {

        SseEmitter emitter = new SseEmitter(0L);
        aiRagService.ragAndAI(userQuestion).subscribe(token -> {
                    try {
                        emitter.send(token);
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                },
                emitter::completeWithError,
                emitter::complete);
        return emitter;
    }
}
