package com.aura.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 会话详情 DTO
 * 用于 GET /api/chat/sessions/{id} 响应
 * 包含完整的会话信息和所有消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDetailResponse {

    /**
     * 会话 ID
     */
    private UUID id;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 用户 ID
     */
    private String userId;

    /**
     * 会话状态
     */
    private String status;

    /**
     * 消息列表
     */
    private List<ChatMessageResponse> messages;

    /**
     * 消息数量
     */
    private Integer messageCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 最后更新时间
     */
    private LocalDateTime updatedAt;
}
