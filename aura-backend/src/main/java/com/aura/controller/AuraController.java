package com.aura.controller;

import com.aura.exception.BusinessException;
import com.aura.model.dto.AuraRequest;
import com.aura.model.dto.AuraResponse;
import com.aura.service.AuraService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Aura API 控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/aura")
@CrossOrigin(origins = "*")
public class AuraController {

    private final AuraService auraService;

    public AuraController(AuraService auraService) {
        this.auraService = auraService;
    }

    /**
     * 验证X-User-Id header
     */
    private void validateUserId(String userId) {
        if (!StringUtils.hasText(userId) || "null".equals(userId)) {
            throw new BusinessException("X-User-Id不能为空，请在请求头中提供用户标识");
        }
        if (userId.length() > 100) {
            throw new BusinessException("X-User-Id长度不能超过100个字符");
        }
    }

    /**
     * 穿搭推荐（同步）
     */
    @PostMapping("/recommend")
    public ResponseEntity<AuraResponse> recommend(
            @RequestHeader(value = "X-User-Id", defaultValue = "default-user") String userId,
            @Valid @RequestBody AuraRequest request) {
        
        validateUserId(userId);
        
        log.info("收到穿搭推荐请求: userId={}, text={}", userId, request.getText());
        
        try {
            AuraResponse response = auraService.recommend(userId, request, null);
            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("穿搭推荐处理失败: userId={}, text={}", userId, request.getText(), e);
            throw new BusinessException("穿搭推荐处理失败: " + e.getMessage());
        }
    }

    /**
     * 穿搭推荐（SSE 流式）
     */
    @PostMapping(value = "/recommend/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter recommendStream(
            @RequestHeader(value = "X-User-Id", defaultValue = "default-user") String userId,
            @Valid @RequestBody AuraRequest request) {
        
        validateUserId(userId);
        
        log.info("收到穿搭推荐请求（SSE）: userId={}", userId);
        
        SseEmitter emitter = new SseEmitter(30000L);  // 30 秒超时

        // 异步执行
        CompletableFuture.runAsync(() -> {
            try {
                auraService.recommendWithSSE(userId, request, null, (event, data) -> {
                    try {
                        emitter.send(SseEmitter.event()
                                .name(event)
                                .data(data));
                    } catch (IOException e) {
                        log.error("发送 SSE 事件失败", e);
                        emitter.completeWithError(e);
                    }
                });
                emitter.complete();
            } catch (Exception e) {
                log.error("SSE 推荐失败", e);
                emitter.completeWithError(new BusinessException("穿搭推荐处理失败: " + e.getMessage()));
            }
        });

        // 设置超时回调
        emitter.onTimeout(() -> {
            log.warn("SSE 超时");
            emitter.complete();
        });

        // 设置完成回调
        emitter.onCompletion(() -> {
            log.info("SSE 完成");
        });

        return emitter;
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Aura API",
                "timestamp", System.currentTimeMillis()
        ));
    }
}
