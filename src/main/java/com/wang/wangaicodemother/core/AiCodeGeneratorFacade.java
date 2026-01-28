package com.wang.wangaicodemother.core;

import cn.hutool.json.JSONUtil;
import com.wang.wangaicodemother.ai.AICodeServiceFactory;
import com.wang.wangaicodemother.ai.Assistant;
import com.wang.wangaicodemother.ai.model.HtmlCodeResult;
import com.wang.wangaicodemother.ai.model.MultiFileCodeResult;
import com.wang.wangaicodemother.ai.model.message.AiResponseMessage;
import com.wang.wangaicodemother.ai.model.message.ToolExecutedMessage;
import com.wang.wangaicodemother.ai.model.message.ToolRequestMessage;
import com.wang.wangaicodemother.core.parser.ParseActuator;
import com.wang.wangaicodemother.core.saver.CodeFileSaverExecutor;
import com.wang.wangaicodemother.enums.CodeGenTypeEnum;
import com.wang.wangaicodemother.exception.BusinessException;
import com.wang.wangaicodemother.exception.ErrorCode;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI 代码生成外观类，组合生成和保存功能
 */
@Slf4j
@Service
public class AiCodeGeneratorFacade {

    @Resource
    private AICodeServiceFactory aiCodeServiceFactory;

    /**
     * 统一入口：根据类型生成并保存代码
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, String appId) {
        log.info("开始生成代码，用户提示词：{}，生成类型：{}", userMessage, codeGenTypeEnum);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        Assistant aiCodeGeneratorService = aiCodeServiceFactory.createAICodeService(Long.parseLong(appId), codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(htmlCodeResult, codeGenTypeEnum, appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult htmlCodeResult = aiCodeGeneratorService.generateMultiCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(htmlCodeResult, codeGenTypeEnum, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 统一入口：根据类型生成并保存代码(流式)
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, String appId) {
        log.info("开始生成代码，用户提示词：{}，生成类型：{}", userMessage, codeGenTypeEnum);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        Assistant aiCodeGeneratorService = aiCodeServiceFactory.createAICodeService(Long.parseLong(appId),codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                yield processCodeStream(aiCodeGeneratorService.generateHtmlCodeStream(userMessage), codeGenTypeEnum, appId);
            }
            case MULTI_FILE -> {
                yield processCodeStream(aiCodeGeneratorService.generateMultiCodeStream(userMessage), codeGenTypeEnum, appId);
            }
            case VUE_PROJECT -> {
                TokenStream tokenStream = aiCodeGeneratorService.generateVueProjectCodeStream(Long.parseLong(appId), userMessage);
                yield processTokenStream(tokenStream);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }


    /**
     * 将 TokenStream 转换为 Flux<String>，并传递工具调用信息
     *
     * @param tokenStream TokenStream 对象
     * @return Flux<String> 流式响应
     */
    private Flux<String> processTokenStream(TokenStream tokenStream) {
        return Flux.create(sink -> {
            tokenStream.onPartialResponse((String partialResponse) -> {
                        AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                    })
                    .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                    })
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                        sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                    })
                    .onCompleteResponse((ChatResponse response) -> {
                        sink.complete();
                    })
                    .onError((Throwable error) -> {
                        error.printStackTrace();
                        sink.error(error);
                    })
                    .start();
        });
    }



    /**
     * 生成并保存代码流(通用)
     *
     * @param codeStream
     * @param codeGenTypeEnum
     * @return
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenTypeEnum, String appId) {

        StringBuilder codeBuilder = new StringBuilder();

        return codeStream.doOnNext(code -> {
            //实时拼接代码
            codeBuilder.append(code);
        }).doOnComplete(() -> {
            try {
                String completeResult = codeBuilder.toString();
                //解析代码
                Object parse = ParseActuator.parse(completeResult, codeGenTypeEnum);
                CodeFileSaverExecutor.executeSaver(parse, codeGenTypeEnum, appId);
            } catch (Exception e) {
                log.info("保存代码文件失败：{}" + e.getMessage());
            }
        });
    }
}
