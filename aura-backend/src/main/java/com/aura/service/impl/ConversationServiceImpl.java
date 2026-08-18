package com.aura.service.impl;

import com.aura.exception.BusinessException;
import com.aura.exception.ResourceNotFoundException;
import com.aura.model.dto.ChatMessageResponse;
import com.aura.model.dto.ConversationDetailResponse;
import com.aura.model.dto.ConversationResponse;
import com.aura.model.entity.ChatMessage;
import com.aura.model.entity.Conversation;
import com.aura.repository.ConversationRepository;
import com.aura.service.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 对话会话服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;

    @Override
    public List<ConversationResponse> getSessions(String userId, int page, int size) {
        log.debug("获取会话列表: userId={}, page={}, size={}", userId, page, size);

        List<Conversation> conversations = conversationRepository
                .findByUserIdOrderByUpdatedAtDesc(userId, PageRequest.of(page, size))
                .getContent();

        return conversations.stream()
                .map(this::toConversationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationDetailResponse getSessionDetail(String userId, UUID conversationId) {
        log.debug("获取会话详情: userId={}, conversationId={}", userId, conversationId);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("会话不存在: " + conversationId));

        // 验证会话属于该用户
        if (!conversation.getUserId().equals(userId)) {
            throw new BusinessException("无权访问该会话");
        }

        return toConversationDetailResponse(conversation);
    }

    @Override
    @Transactional
    public void deleteSession(String userId, UUID conversationId) {
        log.info("删除会话: userId={}, conversationId={}", userId, conversationId);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("会话不存在: " + conversationId));

        // 验证会话属于该用户
        if (!conversation.getUserId().equals(userId)) {
            throw new BusinessException("无权删除该会话");
        }

        conversationRepository.delete(conversation);
    }

    @Override
    @Transactional
    public UUID getOrCreateSession(String userId, UUID sessionId) {
        if (sessionId != null) {
            // 验证会话存在且属于该用户
            Conversation existing = conversationRepository.findById(sessionId)
                    .orElse(null);
            if (existing != null && existing.getUserId().equals(userId)) {
                return existing.getId();
            }
        }

        // 创建新会话
        Conversation conversation = Conversation.builder()
                .userId(userId)
                .title("新对话")
                .build();
        conversation = conversationRepository.save(conversation);
        log.info("创建新会话: userId={}, conversationId={}", userId, conversation.getId());
        return conversation.getId();
    }

    @Override
    @Transactional
    public void saveUserMessage(UUID conversationId, String content) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("会话不存在: " + conversationId));

        ChatMessage message = ChatMessage.builder()
                .role("user")
                .content(content)
                .messageType("text")
                .build();

        conversation.addMessage(message);
        conversation.autoGenerateTitle();
        conversationRepository.save(conversation);

        log.debug("保存用户消息: conversationId={}, contentLength={}", conversationId, content.length());
    }

    @Override
    @Transactional
    public void saveAssistantMessage(UUID conversationId, String content, String metadata) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("会话不存在: " + conversationId));

        ChatMessage message = ChatMessage.builder()
                .role("assistant")
                .content(content)
                .messageType(metadata != null ? "recommendation" : "text")
                .metadata(metadata)
                .build();

        conversation.addMessage(message);
        conversationRepository.save(conversation);

        log.debug("保存AI回复: conversationId={}, contentLength={}", conversationId,
                content != null ? content.length() : 0);
    }

    // ==================== DTO 转换 ====================

    private ConversationResponse toConversationResponse(Conversation conversation) {
        return ConversationResponse.builder()
                .id(conversation.getId())
                .title(conversation.getTitle())
                .lastMessagePreview(conversation.getLastMessagePreview())
                .messageCount(conversation.getMessageCount())
                .status(conversation.getStatus())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }

    private ConversationDetailResponse toConversationDetailResponse(Conversation conversation) {
        List<ChatMessageResponse> messageResponses = conversation.getMessages().stream()
                .map(this::toChatMessageResponse)
                .collect(Collectors.toList());

        return ConversationDetailResponse.builder()
                .id(conversation.getId())
                .title(conversation.getTitle())
                .userId(conversation.getUserId())
                .status(conversation.getStatus())
                .messages(messageResponses)
                .messageCount(conversation.getMessageCount())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }

    private ChatMessageResponse toChatMessageResponse(ChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .role(message.getRole())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .metadata(message.getMetadata())
                .sequenceOrder(message.getSequenceOrder())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
