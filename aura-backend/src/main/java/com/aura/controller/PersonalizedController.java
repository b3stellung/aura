package com.aura.controller;

import com.aura.cf.CFEmbeddingService;
import com.aura.cf.FusionService;
import com.aura.model.dto.AuraRequest;
import com.aura.model.dto.AuraResponse;
import com.aura.service.PersonalizedRecommendationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 个性化推荐接口
 * 
 * 基于 CFALR (CF嵌入 + LLM语义融合) 的个性化穿搭推荐
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/personalized")
@CrossOrigin(origins = "*")
public class PersonalizedController {

    private final PersonalizedRecommendationService personalizedService;
    private final CFEmbeddingService cfService;
    private final FusionService fusionService;

    public PersonalizedController(PersonalizedRecommendationService personalizedService,
                                   CFEmbeddingService cfService,
                                   FusionService fusionService) {
        this.personalizedService = personalizedService;
        this.cfService = cfService;
        this.fusionService = fusionService;
    }

    /**
     * 个性化穿搭推荐（同步）
     */
    @PostMapping("/recommend")
    public ResponseEntity<AuraResponse> personalizedRecommend(
            @RequestHeader(value = "X-User-Id", defaultValue = "default-user") String userId,
            @Valid @RequestBody AuraRequest request) {
        
        if (userId == null || userId.isBlank() || "null".equals(userId)) {
            throw new com.aura.exception.BusinessException("X-User-Id不能为空，请在请求头中提供用户标识");
        }
        
        log.info("个性化推荐请求: userId={}, text={}", userId, request.getText());
        
        try {
            AuraResponse response = personalizedService.personalizedRecommend(userId, request);
            return ResponseEntity.ok(response);
        } catch (com.aura.exception.BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("个性化推荐处理失败: userId={}", userId, e);
            throw new com.aura.exception.BusinessException("个性化推荐处理失败: " + e.getMessage());
        }
    }

    /**
     * 个性化穿搭推荐（SSE流式）
     */
    @PostMapping(value = "/recommend/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter personalizedRecommendStream(
            @RequestHeader(value = "X-User-Id", defaultValue = "default-user") String userId,
            @Valid @RequestBody AuraRequest request) {
        
        log.info("个性化推荐请求（SSE）: userId={}", userId);
        
        SseEmitter emitter = new SseEmitter(60000L);  // 60秒超时

        CompletableFuture.runAsync(() -> {
            try {
                personalizedService.personalizedRecommendWithSSE(userId, request, (event, data) -> {
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
                log.error("个性化推荐失败（SSE）", e);
                emitter.completeWithError(e);
            }
        });

        emitter.onTimeout(() -> {
            log.warn("SSE 超时");
            emitter.complete();
        });

        return emitter;
    }

    /**
     * 获取用户CF嵌入状态
     */
    @GetMapping("/cf/status")
    public ResponseEntity<Map<String, Object>> getCFStatus() {
        log.info("获取 CF 模型状态");
        
        Map<String, Object> status = cfService.getModelStatus();
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "cfModel", status
        ));
    }

    /**
     * 手动触发CF训练
     */
    @PostMapping("/cf/train")
    public ResponseEntity<Map<String, Object>> triggerCFTraining() {
        log.info("手动触发 CF 训练");
        
        try {
            cfService.triggerTraining();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "CF 训练已触发",
                    "modelStatus", cfService.getModelStatus()
            ));
        } catch (Exception e) {
            log.error("CF 训练失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 获取融合配置
     */
    @GetMapping("/fusion/config")
    public ResponseEntity<Map<String, Object>> getFusionConfig() {
        log.info("获取融合配置");
        
        Map<String, Object> config = fusionService.getFusionConfig();
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "fusionConfig", config
        ));
    }

    /**
     * 测试融合向量生成
     */
    @PostMapping("/fusion/test")
    public ResponseEntity<Map<String, Object>> testFusion(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Map<String, String> request) {
        
        log.info("测试融合向量: userId={}", userId);
        
        try {
            String query = request.get("query");
            
            // 生成融合向量
            float[] fused = fusionService.fuseUserQuery(
                    java.util.UUID.nameUUIDFromBytes(userId.getBytes()),
                    query
            );
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "userId", userId,
                    "query", query,
                    "fusedDimension", fused.length,
                    "fusedPreview", java.util.Arrays.copyOf(fused, 5)
            ));
        } catch (Exception e) {
            log.error("融合测试失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Personalized Recommendation Service",
                "cfModelStatus", cfService.getModelStatus(),
                "fusionConfig", fusionService.getFusionConfig()
        ));
    }
}
