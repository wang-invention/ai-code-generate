package com.wang.wangaicodemother.langgraph4j.controller;

import com.wang.wangaicodemother.langgraph4j.CodeGenWorkflow;
import com.wang.wangaicodemother.langgraph4j.state.WorkflowContext;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/work/flow")
public class TestWorkFlow {

    @Resource
    private CodeGenWorkflow codeGenWorkflow;


    @GetMapping("/execute")
    public WorkflowContext executeWorkflow(@RequestParam("prompt") String originalPrompt) {
        WorkflowContext workflowContext = codeGenWorkflow.executeWorkflow(originalPrompt);
        return workflowContext;
    }
}
