package com.aura.service;

import com.aura.model.dto.ConversationDetailResponse;
import com.aura.model.dto.ConversationResponse;

import java.util.List;
import java.util.UUID;

/**
 * 对话会话服务接口
 */
public interface ConversationService {

    /**
     * 获取用户的会话列表
     *
     * @param userId 用户 ID
     * @param page   页码（从0开始）
     * @param size   每页大小
     * @return 会话列表
     */
    List<ConversationResponse> getSessions(String userId, int page, int size);

    /**
     * 获取会话详情（包含所有消息）
     *
     * @param userId       用户 ID
     * @param conversationId 会话 ID
     * @return 会话详情
     */
    ConversationDetailResponse getSessionDetail(String userId, UUID conversationId);

    /**
     * 删除会话
     *
     * @param userId       用户 ID
     * @param conversationId 会话 ID
     */
    void deleteSession(String userId, UUID conversationId);

    /**
     * 创建新会话或获取已有会话
     * 如果 sessionId 为 null，则创建新会话
     *
     * @param userId    用户 ID
     * @param sessionId 会话 ID（可选）
     * @return 会话 ID
     */
    UUID getOrCreateSession(String userId, UUID sessionId);

    /**
     * 保存用户消息到会话
     *
     * @param conversationId 会话 ID
     * @param content        消息内容
     */
    void saveUserMessage(UUID conversationId, String content);

    /**
     * 保存 AI 回复到会话
     *
     * @param conversationId 会话 ID
     * @param content        回复内容
     * @param metadata       额外数据（JSON）
     */
    void saveAssistantMessage(UUID conversationId, String content, String metadata);
}
