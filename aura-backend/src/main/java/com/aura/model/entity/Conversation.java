package com.aura.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 对话会话实体
 * 代表一次完整的对话，包含多条消息
 */
@Entity
@Table(name = "conversations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * 用户 ID（来自 X-User-Id 请求头）
     */
    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    /**
     * 会话标题
     * 可自动生成（取用户第一条消息的前30个字符）
     */
    @Column(length = 200)
    private String title;

    /**
     * 会话中的消息列表
     */
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceOrder ASC")
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    /**
     * 会话状态
     * - active: 活跃
     * - archived: 已归档
     */
    @Column(length = 20)
    @Builder.Default
    private String status = "active";

    /**
     * 消息数量（冗余字段，方便查询）
     */
    @Column(name = "message_count")
    @Builder.Default
    private Integer messageCount = 0;

    /**
     * 最后一条消息的预览（截取前100个字符）
     */
    @Column(name = "last_message_preview", length = 200)
    private String lastMessagePreview;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 添加消息到会话
     */
    public void addMessage(ChatMessage message) {
        message.setConversation(this);
        message.setSequenceOrder(this.messages.size());
        this.messages.add(message);
        this.messageCount = this.messages.size();
        this.lastMessagePreview = truncate(message.getContent(), 100);
    }

    /**
     * 自动生成标题（取第一条用户消息）
     */
    public void autoGenerateTitle() {
        if (this.title == null || this.title.isBlank()) {
            this.messages.stream()
                    .filter(m -> "user".equals(m.getRole()))
                    .findFirst()
                    .ifPresent(m -> this.title = truncate(m.getContent(), 30));
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return null;
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
}
