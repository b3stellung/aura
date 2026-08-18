package com.aura.service;

import com.aura.model.dto.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话记忆服务
 * 
 * 基于内存的对话历史管理，支持多轮对话上下文。
 * 每个 conversationId 对应一个独立的对话线程。
 */
@Slf4j
@Service
public class ConversationMemory {

    /**
     * 会话历史存储: conversationId -> List<ChatMessage>
     */
    private final Map<String, List<ChatMessage>> conversations = new ConcurrentHashMap<>();

    /**
     * 最大保留的历史消息轮数（每轮 = 1条user + 1条assistant）
     */
    @Value("${aura.chat.max-history-rounds:10}")
    private int maxHistoryRounds;

    /**
     * 单条消息最大字符数（超过则截断）
     */
    @Value("${aura.chat.max-message-length:500}")
    private int maxMessageLength;

    /**
     * 会话过期时间（毫秒），默认2小时
     */
    @Value("${aura.chat.session-expire-ms:7200000}")
    private long sessionExpireMs;

    /**
     * 获取对话历史
     */
    public List<ChatMessage> getHistory(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return Collections.emptyList();
        }

        List<ChatMessage> messages = conversations.get(conversationId);
        if (messages == null) {
            return Collections.emptyList();
        }

        // 检查是否过期
        if (!messages.isEmpty()) {
            long lastTimestamp = messages.get(messages.size() - 1).getTimestamp();
            if (System.currentTimeMillis() - lastTimestamp > sessionExpireMs) {
                log.info("会话已过期，清除历史: conversationId={}", conversationId);
                conversations.remove(conversationId);
                return Collections.emptyList();
            }
        }

        return new ArrayList<>(messages);
    }

    /**
     * 添加用户消息
     */
    public void addUserMessage(String conversationId, String content) {
        if (conversationId == null || conversationId.isBlank()) {
            return;
        }

        List<ChatMessage> messages = conversations.computeIfAbsent(
                conversationId, k -> Collections.synchronizedList(new ArrayList<>())
        );

        messages.add(ChatMessage.builder()
                .role("user")
                .content(truncateContent(content))
                .timestamp(System.currentTimeMillis())
                .build());

        trimHistory(messages);
        log.debug("添加用户消息: conversationId={}, 总消息数={}", conversationId, messages.size());
    }

    /**
     * 添加助手回复
     */
    public void addAssistantMessage(String conversationId, String content) {
        if (conversationId == null || conversationId.isBlank()) {
            return;
        }

        List<ChatMessage> messages = conversations.computeIfAbsent(
                conversationId, k -> Collections.synchronizedList(new ArrayList<>())
        );

        messages.add(ChatMessage.builder()
                .role("assistant")
                .content(truncateContent(content))
                .timestamp(System.currentTimeMillis())
                .build());

        trimHistory(messages);
        log.debug("添加助手消息: conversationId={}, 总消息数={}", conversationId, messages.size());
    }

    /**
     * 清除指定会话的历史
     */
    public void clearHistory(String conversationId) {
        if (conversationId != null) {
            conversations.remove(conversationId);
            log.info("清除会话历史: conversationId={}", conversationId);
        }
    }

    /**
     * 获取当前活跃会话数
     */
    public int getActiveSessionCount() {
        return conversations.size();
    }

    /**
     * 清理过期会话
     */
    public void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();
        int before = conversations.size();
        conversations.entrySet().removeIf(entry -> {
            List<ChatMessage> messages = entry.getValue();
            if (messages.isEmpty()) return true;
            long lastTimestamp = messages.get(messages.size() - 1).getTimestamp();
            return now - lastTimestamp > sessionExpireMs;
        });
        int cleaned = before - conversations.size();
        if (cleaned > 0) {
            log.info("清理过期会话: 清除{}个会话", cleaned);
        }
    }

    /**
     * 截断过长的消息内容
     */
    private String truncateContent(String content) {
        if (content == null) return "";
        if (content.length() <= maxMessageLength) {
            return content;
        }
        return content.substring(0, maxMessageLength) + "...";
    }

    /**
     * 裁剪历史，保留最近的 maxHistoryRounds 轮对话
     */
    private void trimHistory(List<ChatMessage> messages) {
        int maxMessages = maxHistoryRounds * 2; // 每轮 = user + assistant
        while (messages.size() > maxMessages) {
            messages.remove(0);
        }
    }
}
