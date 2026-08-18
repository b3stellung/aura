package com.aura.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 聊天消息实体
 * 存储对话中的单条消息
 */
@Entity
@Table(name = "chat_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * 所属会话
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    /**
     * 消息角色
     * - user: 用户消息
     * - assistant: AI 回复
     * - system: 系统消息
     */
    @Column(nullable = false, length = 20)
    private String role;

    /**
     * 消息内容
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 消息类型
     * - text: 纯文本
     * - recommendation: 穿搭推荐
     * - thought: 思考过程
     * - tool_call: 工具调用
     */
    @Column(name = "message_type", length = 30)
    @Builder.Default
    private String messageType = "text";

    /**
     * 额外数据（JSON 格式）
     * 存储推荐结果、工具调用等结构化数据
     */
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String metadata;

    /**
     * 消息在会话中的序号（用于排序）
     */
    @Column(name = "sequence_order")
    private Integer sequenceOrder;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
