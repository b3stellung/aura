package com.aura.controller;

import com.aura.model.dto.ConversationDetailResponse;
import com.aura.model.dto.ConversationResponse;
import com.aura.model.dto.AuraRequest;
import com.aura.model.dto.ApiResponse;
import com.aura.service.AuraService;
import com.aura.service.ConversationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Chat API 控制器
 * 
 * 兼容前端MaaS接口格式
 * 支持对话历史持久化
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    private final AuraService auraService;
    private final ConversationService conversationService;
    private final ObjectMapper objectMapper;

    /**
     * 流式聊天接口（SSE）
     * 
     * 请求格式：
     * {
     *     "content": "用户消息",
     *     "sessionId": "可选会话ID"
     * }
     * 
     * SSE事件格式：
     * - event: delta      data: {"type":"delta","delta":"内容片段"}
     * - event: thought    data: {"type":"thought","content":"思考过程"}
     * - event: tool       data: {"type":"tool","tool":{...}}
     * - event: done       data: {"type":"done","message":{...}}
     * - event: error      data: {"type":"error","error":"错误信息"}
     */
    @PostMapping(value = "/messages/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(
            @RequestHeader(value = "X-User-Id", defaultValue = "default-user") String userId,
            @RequestBody Map<String, Object> request) {
        
        String content = (String) request.get("content");
        String sessionIdStr = (String) request.get("sessionId");
        
        log.info("收到聊天请求: userId={}, content={}, sessionId={}", userId, content, sessionIdStr);
        
        // 获取或创建会话
        UUID sessionId = sessionIdStr != null ? UUID.fromString(sessionIdStr) : null;
        UUID conversationId = conversationService.getOrCreateSession(userId, sessionId);
        
        // 保存用户消息
        conversationService.saveUserMessage(conversationId, content);
        
        SseEmitter emitter = new SseEmitter(60000L);  // 60秒超时
        
        // 发送开始事件
        try {
            emitter.send(SseEmitter.event()
                    .name("start")
                    .data(Map.of("type", "start", "sessionId", conversationId.toString())));
        } catch (IOException e) {
            log.error("发送开始事件失败", e);
        }
        
        // 异步执行
        final UUID finalConversationId = conversationId;
        CompletableFuture.runAsync(() -> {
            try {
                // 构造Aura请求
                AuraRequest auraRequest = AuraRequest.builder()
                        .text(content)
                        .build();
                
                // 用于收集AI回复内容
                StringBuilder responseContent = new StringBuilder();
                
                // 调用Aura服务（SSE模式）
                auraService.recommendWithSSE(userId, auraRequest, finalConversationId.toString(), (event, data) -> {
                    try {
                        log.info("SSE回调: event={}, data={}", event, data);
                        // 转换为MaaS格式
                        Map<String, Object> maasEvent = convertToMaaSFormat(event, data);
                        log.info("转换后: maasEvent={}", maasEvent);
                        if (maasEvent != null) {
                            String json = objectMapper.writeValueAsString(maasEvent);
                            log.info("发送SSE: event={}, json={}", event, json);
                            emitter.send(SseEmitter.event()
                                    .name(event)
                                    .data(json));
                            log.info("SSE发送成功: event={}", event);

                            // 收集delta内容用于持久化
                            if ("delta".equals(event) && maasEvent.containsKey("delta")) {
                                responseContent.append(maasEvent.get("delta"));
                            }
                        } else {
                            log.info("SSE事件被过滤: event={}", event);
                        }
                    } catch (Exception e) {
                        log.error("发送SSE事件失败: event={}", event, e);
                    }
                });
                
                // 保存AI回复到数据库
                String aiResponse = responseContent.toString();
                if (!aiResponse.isEmpty()) {
                    conversationService.saveAssistantMessage(finalConversationId, aiResponse, null);
                }
                
                // 发送完成事件
                emitter.send(SseEmitter.event()
                        .name("done")
                        .data(Map.of(
                                "type", "done",
                                "message", Map.of(
                                        "content", "推荐完成",
                                        "sessionId", finalConversationId.toString()
                                ),
                                "status", "completed"
                        )));
                
                emitter.complete();
                
            } catch (Exception e) {
                log.error("聊天处理失败", e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(Map.of(
                                    "type", "error",
                                    "error", e.getMessage() != null ? e.getMessage() : "处理失败"
                            )));
                } catch (IOException ex) {
                    log.error("发送错误事件失败", ex);
                }
                emitter.completeWithError(e);
            }
        });
        
        // 设置超时回调
        emitter.onTimeout(() -> {
            log.warn("SSE超时");
            emitter.complete();
        });
        
        // 设置完成回调
        emitter.onCompletion(() -> {
            log.info("SSE完成");
        });
        
        return emitter;
    }

    /**
     * 同步聊天接口
     */
    @PostMapping("/messages")
    public ResponseEntity<Map<String, Object>> sendMessage(
            @RequestHeader(value = "X-User-Id", defaultValue = "default-user") String userId,
            @RequestBody Map<String, Object> request) {
        
        String content = (String) request.get("content");
        String sessionIdStr = (String) request.get("sessionId");
        
        log.info("收到同步聊天请求: userId={}, content={}", userId, content);
        
        try {
            // 获取或创建会话
            UUID sessionId = sessionIdStr != null ? UUID.fromString(sessionIdStr) : null;
            UUID conversationId = conversationService.getOrCreateSession(userId, sessionId);
            
            // 保存用户消息
            conversationService.saveUserMessage(conversationId, content);
            
            AuraRequest auraRequest = AuraRequest.builder()
                    .text(content)
                    .build();
            
            var response = auraService.recommend(userId, auraRequest, conversationId.toString());
            
            // 保存AI回复
            String aiContent = response.getStoryText() != null ? response.getStoryText() : "推荐完成";
            String metadata = objectMapper.writeValueAsString(response);
            conversationService.saveAssistantMessage(conversationId, aiContent, metadata);
            
            return ResponseEntity.ok(Map.of(
                    "message", Map.of(
                            "id", UUID.randomUUID().toString(),
                            "role", "assistant",
                            "content", aiContent,
                            "timestamp", System.currentTimeMillis(),
                            "sessionId", conversationId.toString()
                    ),
                    "recommendation", response
            ));
            
        } catch (Exception e) {
            log.error("同步聊天失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage() != null ? e.getMessage() : "处理失败"
            ));
        }
    }

    /**
     * 获取会话列表
     * GET /api/chat/sessions
     */
    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getSessions(
            @RequestHeader(value = "X-User-Id", defaultValue = "default-user") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("获取会话列表: userId={}, page={}, size={}", userId, page, size);
        
        List<ConversationResponse> sessions = conversationService.getSessions(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success("获取会话列表成功", sessions));
    }

    /**
     * 获取会话详情（包含所有消息）
     * GET /api/chat/sessions/{id}
     */
    @GetMapping("/sessions/{id}")
    public ResponseEntity<ApiResponse<ConversationDetailResponse>> getSessionDetail(
            @RequestHeader(value = "X-User-Id", defaultValue = "default-user") String userId,
            @PathVariable UUID id) {
        
        log.info("获取会话详情: userId={}, conversationId={}", userId, id);
        
        ConversationDetailResponse detail = conversationService.getSessionDetail(userId, id);
        return ResponseEntity.ok(ApiResponse.success("获取会话详情成功", detail));
    }

    /**
     * 删除会话
     * DELETE /api/chat/sessions/{id}
     */
    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSession(
            @RequestHeader(value = "X-User-Id", defaultValue = "default-user") String userId,
            @PathVariable UUID id) {
        
        log.info("删除会话: userId={}, conversationId={}", userId, id);
        
        conversationService.deleteSession(userId, id);
        return ResponseEntity.ok(ApiResponse.success("会话已删除"));
    }

    /**
     * 转换为MaaS格式
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> convertToMaaSFormat(String event, Object data) {
        switch (event) {
            case "thought": {
                // 发送思考事件，保持thought类型
                if (data instanceof Map) {
                    Map<String, Object> dataMap = (Map<String, Object>) data;
                    String content = dataMap.getOrDefault("content", "").toString();
                    return Map.of("type", "thought", "content", content);
                }
                return Map.of("type", "thought", "content", data.toString());
            }
            case "action":
                // 发送工具调用事件
                if (data instanceof Map) {
                    Map<String, Object> dataMap = (Map<String, Object>) data;
                    return Map.of("type", "tool", "tool", Map.of(
                            "name", dataMap.getOrDefault("tool", "unknown"),
                            "arguments", dataMap.getOrDefault("params", Map.of())
                    ));
                }
                return Map.of("type", "tool", "tool", data);
            case "observation": {
                // 发送观察结果事件
                if (data instanceof Map) {
                    Map<String, Object> dataMap = (Map<String, Object>) data;
                    return Map.of("type", "observation", "result", dataMap.getOrDefault("result", ""));
                }
                return Map.of("type", "observation", "result", data.toString());
            }
            case "fallback": {
                String message = data instanceof Map ? ((Map<String, Object>) data).getOrDefault("message", "").toString() : data.toString();
                return Map.of("type", "delta", "delta", "\n\n" + message);
            }
            case "start":
            case "done":
                return null;
            default:
                return Map.of("type", "delta", "delta", data.toString());
        }
    }
}
