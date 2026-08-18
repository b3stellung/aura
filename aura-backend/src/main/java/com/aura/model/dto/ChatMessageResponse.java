package com.aura.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 聊天消息 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {

    /**
     * 消息 ID
     */
    private UUID id;

    /**
     * 消息角色 (user/assistant/system)
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型 (text/recommendation/thought/tool_call)
     */
    private String messageType;

    /**
     * 额外数据（JSON 字符串）
     */
    private String metadata;

    /**
     * 消息序号
     */
    private Integer sequenceOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
