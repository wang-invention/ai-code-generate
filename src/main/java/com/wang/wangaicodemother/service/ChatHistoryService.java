package com.wang.wangaicodemother.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.wang.wangaicodemother.model.dto.ChatHistoryQueryRequest;
import com.wang.wangaicodemother.model.entity.ChatHistory;
import com.wang.wangaicodemother.model.entity.User;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author <a href="https://github.com">wangInvention</a>
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 添加对话历史
     * @param userMessage
     * @param appId
     * @param userId
     * @param chatType
     * @return
     */
    boolean addChatHistoryMessage(String userMessage, Long appId, Long userId,String chatType);


    /**
     * 获取查询条件
     * @param chatHistoryQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);


    /**
     * 游标查询对话历史
     * @param appId
     * @param loginUser
     * @param pageSize
     * @param lastCreateTime
     * @return
     */
    Page<ChatHistory> listAppChatHistoryByPage(Long appId, User loginUser, Integer pageSize, LocalDateTime lastCreateTime);

    /**
     * 删除指定appId的对话历史
     * @param appId
     */
    boolean deleteByAppId(long appId);

    /**
     * 加载数据库中的历史记录到内存中
     * @param appId
     * @param chatMemory
     * @param maxCount
     * @return
     */
    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);
}
