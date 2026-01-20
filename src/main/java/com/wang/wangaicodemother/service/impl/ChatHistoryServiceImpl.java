package com.wang.wangaicodemother.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import com.wang.wangaicodemother.enums.ChatHistoryMessageTypeEnum;
import com.wang.wangaicodemother.exception.BusinessException;
import com.wang.wangaicodemother.exception.ErrorCode;
import com.wang.wangaicodemother.mapper.ChatHistoryMapper;
import com.wang.wangaicodemother.model.dto.ChatHistoryQueryRequest;
import com.wang.wangaicodemother.model.entity.App;
import com.wang.wangaicodemother.model.entity.ChatHistory;
import com.wang.wangaicodemother.model.entity.User;
import com.wang.wangaicodemother.service.AppService;
import com.wang.wangaicodemother.service.ChatHistoryService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层实现。
 *
 * @author <a href="https://github.com">wangInvention</a>
 */
@Service
@Lazy
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {


    @Resource
    private AppService appService;

    @Override
    public boolean addChatHistoryMessage(String userMessage, Long appId, Long userId, String chatType) {
        if (StrUtil.isBlank(userMessage)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户消息为空");
        }
        if (StrUtil.isBlank(chatType)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "聊天类型为空");
        }
        if (appId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用ID为空");
        }
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID为空");
        }
        //验证聊天类型
        if (ChatHistoryMessageTypeEnum.getEnumByValue(chatType) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的聊天类型");
        }
        ChatHistory chatHistory = ChatHistory.builder()
                .message(userMessage)
                .messageType(chatType)
                .appId(appId)
                .userId(userId)
                .build();
        return save(chatHistory);
    }

    /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (chatHistoryQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq("id", id)
                .like("message", message)
                .eq("messageType", messageType)
                .eq("appId", appId)
                .eq("userId", userId);
        // 游标查询逻辑 - 只使用 createTime 作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt("createTime", lastCreateTime);
        }
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按创建时间降序排列
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }

    @Override
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, User loginUser, Integer pageSize, LocalDateTime time) {
        if (appId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "appId不能为空");
        }
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        //校验权限
        App app = appService.getById(appId);
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        ChatHistoryQueryRequest chatHistoryQueryRequest = new ChatHistoryQueryRequest();
        chatHistoryQueryRequest.setAppId(appId);
        chatHistoryQueryRequest.setLastCreateTime(time);
        QueryWrapper queryWrapper = getQueryWrapper(chatHistoryQueryRequest);
        return this.page(Page.of(1, pageSize), queryWrapper);
    }

    @Override
    public boolean deleteByAppId(long appId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.where("appId", appId);
        return remove(queryWrapper);
    }
}
