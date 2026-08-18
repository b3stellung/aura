package com.aura.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天消息 DTO
 * 用于在内存中存储对话历史
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    /**
     * 消息角色: user / assistant
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 时间戳
     */
    @Builder.Default
    private long timestamp = System.currentTimeMillis();
}
