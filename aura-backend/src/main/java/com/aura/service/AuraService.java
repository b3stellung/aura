package com.aura.service;

import com.aura.model.dto.AuraRequest;
import com.aura.model.dto.AuraResponse;
import com.aura.model.dto.ChatMessage;
import com.aura.model.entity.Conversation;
import com.aura.repository.ConversationRepository;
import com.aura.react.AuraReActEngine;
import com.aura.react.ReActState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Aura 核心服务
 * 
 * 协调 ReAct 引擎、插件、RAG 等模块
 * 支持多轮对话上下文
 */
@Slf4j
@Service
public class AuraService {

    private final AuraReActEngine reActEngine;
    private final ConversationMemory conversationMemory;
    private final ConversationRepository conversationRepository;
    private final ChatHistoryCompressor chatHistoryCompressor;

    public AuraService(AuraReActEngine reActEngine, 
                       ConversationMemory conversationMemory,
                       ConversationRepository conversationRepository,
                       ChatHistoryCompressor chatHistoryCompressor) {
        this.reActEngine = reActEngine;
        this.conversationMemory = conversationMemory;
        this.conversationRepository = conversationRepository;
        this.chatHistoryCompressor = chatHistoryCompressor;
    }

    /**
     * 获取穿搭推荐（同步）
     */
    public AuraResponse recommend(String userId, AuraRequest request, String conversationId) {
        log.info("开始穿搭推荐: userId={}, text={}, conversationId={}", userId, request.getText(), conversationId);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 加载历史对话上下文（从数据库）
            List<ChatMessage> chatHistory = loadChatHistory(conversationId);
            log.info("加载对话历史: conversationId={}, 历史消息数={}", conversationId, chatHistory.size());
            
            // 2. 执行 ReAct 循环（带上下文）
            ReActState state = reActEngine.executeReActLoop(
                    userId,
                    request.getText(),
                    request.getImages(),
                    conversationId,
                    chatHistory
            );
            
            long latency = System.currentTimeMillis() - startTime;
            
            // 3. 构造响应
            return buildResponse(state, latency, conversationId);
            
        } catch (Exception e) {
            log.error("穿搭推荐失败", e);
            throw e;
        }
    }

    /**
     * 获取穿搭推荐（SSE 流式）
     */
    @Transactional
    public void recommendWithSSE(String userId, AuraRequest request, 
                                  String conversationId,
                                  BiConsumer<String, Object> callback) {
        log.info("开始穿搭推荐（SSE）: userId={}, conversationId={}", userId, conversationId);
        
        try {
            // 1. 加载历史对话上下文（从数据库）
            List<ChatMessage> chatHistory = loadChatHistory(conversationId);
            log.info("加载对话历史: conversationId={}, 历史消息数={}", conversationId, chatHistory.size());
            
            // 2. 执行 ReAct 循环（带上下文）
            reActEngine.executeReActLoopWithCallback(
                    userId,
                    request.getText(),
                    request.getImages(),
                    conversationId,
                    chatHistory,
                    callback
            );
            
        } catch (Exception e) {
            log.error("穿搭推荐失败（SSE）", e);
            callback.accept("error", Map.of("message", e.getMessage()));
        }
    }

    /**
     * 从数据库加载对话历史
     * 将数据库实体转换为DTO供ReAct引擎使用
     */
    public List<ChatMessage> loadChatHistory(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return Collections.emptyList();
        }
        
        try {
            UUID convId = UUID.fromString(conversationId);
            Optional<Conversation> convOpt = conversationRepository.findById(convId);
            
            if (convOpt.isEmpty()) {
                log.debug("会话不存在: conversationId={}", conversationId);
                return Collections.emptyList();
            }
            
            Conversation conversation = convOpt.get();
            
            // 转换数据库实体为DTO
            List<ChatMessage> rawHistory = conversation.getMessages().stream()
                    .filter(msg -> "user".equals(msg.getRole()) || "assistant".equals(msg.getRole()))
                    .map(msg -> ChatMessage.builder()
                            .role(msg.getRole())
                            .content(truncateForContext(msg.getContent(), 500))
                            .timestamp(msg.getCreatedAt() != null ? 
                                    msg.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() 
                                    : System.currentTimeMillis())
                            .build())
                    .collect(Collectors.toList());
            
            // 智能压缩对话历史
            List<ChatMessage> compressed = chatHistoryCompressor.compress(rawHistory);
            log.info("对话历史压缩: 原始={}条 → 压缩后={}条", rawHistory.size(), compressed.size());
            return compressed;
                    
        } catch (IllegalArgumentException e) {
            log.warn("无效的conversationId格式: {}", conversationId);
            return Collections.emptyList();
        }
    }

    /**
     * 截断内容用于上下文（避免token超限）
     */
    private String truncateForContext(String content, int maxLength) {
        if (content == null) return "";
        if (content.length() <= maxLength) return content;
        return content.substring(0, maxLength) + "...";
    }

    /**
     * 构造响应
     */
    private AuraResponse buildResponse(ReActState state, long latency, String conversationId) {
        Map<String, Object> result = state.getFinalResult();
        
        return AuraResponse.builder()
                .sessionId(state.getSessionId().toString())
                .conversationId(conversationId)
                .title(result != null ? (String) result.getOrDefault("title", "今日推荐") : "今日推荐")
                .storyText(result != null ? (String) result.getOrDefault("story_text", "") : "")
                .outfits(buildOutfits(result))
                .analysis(buildAnalysis(state))
                .confidence(result != null ? (Double) result.getOrDefault("confidence", 0.8) : 0.8)
                .tokenConsumed(state.getTokenConsumed())
                .latencyMs((int) latency)
                .build();
    }

    /**
     * 构造穿搭推荐列表
     */
    private List<AuraResponse.OutfitRecommendation> buildOutfits(Map<String, Object> result) {
        if (result == null || !result.containsKey("outfits")) {
            return Collections.emptyList();
        }
        
        List<Map<String, Object>> outfitsData = (List<Map<String, Object>>) result.get("outfits");
        List<AuraResponse.OutfitRecommendation> outfits = new ArrayList<>();
        
        for (Map<String, Object> outfitData : outfitsData) {
            outfits.add(AuraResponse.OutfitRecommendation.builder()
                    .name((String) outfitData.get("name"))
                    .description((String) outfitData.get("description"))
                    .vibe((String) outfitData.get("vibe"))
                    .score((Double) outfitData.get("score"))
                    .build());
        }
        
        return outfits;
    }

    /**
     * 构造 ReAct 分析
     */
    private AuraResponse.ReactAnalysis buildAnalysis(ReActState state) {
        return AuraResponse.ReactAnalysis.builder()
                .stepCount(state.getStepCount())
                .pluginsUsed(state.getAvailablePlugins())
                .build();
    }
}
