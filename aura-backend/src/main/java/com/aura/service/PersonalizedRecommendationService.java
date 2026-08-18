package com.aura.service;

import com.aura.cf.CFEmbeddingService;
import com.aura.cf.FusionService;
import com.aura.model.dto.AuraRequest;
import com.aura.model.dto.AuraResponse;
import com.aura.rag.AestheticKnowledgeService;
import com.aura.rag.AestheticKnowledgeService.KnowledgeResult;
import com.aura.react.AuraReActEngine;
import com.aura.react.ReActState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * 个性化推荐服务
 * 
 * 整合 CF 嵌入 + LLM 语义融合
 * 实现 CFALR (Collaborative Filtering-Augmented LLM Recommendation)
 */
@Slf4j
@Service
public class PersonalizedRecommendationService {

    private final AuraReActEngine reActEngine;
    private final CFEmbeddingService cfService;
    private final FusionService fusionService;
    private final AestheticKnowledgeService knowledgeService;

    public PersonalizedRecommendationService(AuraReActEngine reActEngine,
                                              CFEmbeddingService cfService,
                                              FusionService fusionService,
                                              AestheticKnowledgeService knowledgeService) {
        this.reActEngine = reActEngine;
        this.cfService = cfService;
        this.fusionService = fusionService;
        this.knowledgeService = knowledgeService;
    }

    /**
     * 个性化穿搭推荐（同步）
     */
    public AuraResponse personalizedRecommend(String userId, AuraRequest request) {
        log.info("个性化推荐: userId={}, text={}", userId, request.getText());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 获取用户画像
            UserProfile profile = getUserProfile(userId);
            
            // 2. 融合查询（CF + 语义）
            float[] fusedQuery = fusionService.fuseUserQuery(
                    UUID.nameUUIDFromBytes(userId.getBytes()), 
                    request.getText()
            );
            
            // 3. 检索个性化知识
            List<KnowledgeResult> personalizedKnowledge = retrievePersonalizedKnowledge(
                    userId, request.getText(), profile);
            
            // 4. 增强查询（注入个性化上下文）
            String enhancedQuery = enhanceQueryWithProfile(request.getText(), profile, personalizedKnowledge);
            
            // 5. 调用 ReAct 引擎
            AuraRequest enhancedRequest = AuraRequest.builder()
                    .text(enhancedQuery)
                    .images(request.getImages())
                    .location(request.getLocation())
                    .occasion(request.getOccasion())
                    .needAnalysis(request.isNeedAnalysis())
                    .build();
            
            ReActState state = reActEngine.executeReActLoop(userId, enhancedQuery, request.getImages(), null, null);
            
            // 6. 构造响应
            long latency = System.currentTimeMillis() - startTime;
            AuraResponse response = buildResponse(state, latency, profile);
            
            log.info("个性化推荐完成: userId={}, latency={}ms", userId, latency);
            return response;
            
        } catch (Exception e) {
            log.error("个性化推荐失败", e);
            throw e;
        }
    }

    /**
     * 个性化穿搭推荐（SSE流式）
     */
    public void personalizedRecommendWithSSE(String userId, AuraRequest request,
                                              BiConsumer<String, Object> callback) {
        log.info("个性化推荐（SSE）: userId={}", userId);
        
        try {
            // 1. 获取用户画像
            UserProfile profile = getUserProfile(userId);
            
            // 2. 融合查询
            float[] fusedQuery = fusionService.fuseUserQuery(
                    UUID.nameUUIDFromBytes(userId.getBytes()), 
                    request.getText()
            );
            
            // 3. 检索个性化知识
            List<KnowledgeResult> personalizedKnowledge = retrievePersonalizedKnowledge(
                    userId, request.getText(), profile);
            
            // 4. 增强查询
            String enhancedQuery = enhanceQueryWithProfile(request.getText(), profile, personalizedKnowledge);
            
            // 5. 发送个性化信息
            callback.accept("personalization", Map.of(
                    "userId", userId,
                    "profile", profile.toMap(),
                    "knowledgeCount", personalizedKnowledge.size()
            ));
            
            // 6. 调用 ReAct 引擎
            reActEngine.executeReActLoopWithCallback(userId, enhancedQuery, request.getImages(), null, null, callback);
            
        } catch (Exception e) {
            log.error("个性化推荐失败（SSE）", e);
            callback.accept("error", Map.of("message", e.getMessage()));
        }
    }

    /**
     * 获取用户画像
     */
    private UserProfile getUserProfile(String userId) {
        UUID userUUID = UUID.nameUUIDFromBytes(userId.getBytes());
        
        // 获取用户CF嵌入
        float[] cfEmbedding = cfService.getUserEmbedding(userUUID);
        
        // 分析用户偏好（从行为数据推断）
        Map<String, Double> stylePreferences = analyzeStylePreferences(userId);
        Map<String, Double> occasionPreferences = analyzeOccasionPreferences(userId);
        
        return new UserProfile(
                userId,
                cfEmbedding,
                stylePreferences,
                occasionPreferences,
                cfService.getModelStatus()
        );
    }

    /**
     * 分析用户风格偏好
     */
    private Map<String, Double> analyzeStylePreferences(String userId) {
        // 简化版：从穿搭记录推断
        // 实际应该从用户行为数据中学习
        Map<String, Double> preferences = new HashMap<>();
        preferences.put("elegant", 0.3);
        preferences.put("casual", 0.4);
        preferences.put("minimal", 0.3);
        return preferences;
    }

    /**
     * 分析用户场合偏好
     */
    private Map<String, Double> analyzeOccasionPreferences(String userId) {
        // 简化版
        Map<String, Double> preferences = new HashMap<>();
        preferences.put("date", 0.3);
        preferences.put("work", 0.4);
        preferences.put("casual", 0.3);
        return preferences;
    }

    /**
     * 检索个性化知识
     */
    private List<KnowledgeResult> retrievePersonalizedKnowledge(String userId, String query, UserProfile profile) {
        // 1. 基础语义检索
        List<KnowledgeResult> semanticResults = knowledgeService.searchKnowledge(query, 5);
        
        // 2. 根据用户偏好过滤和重排序
        return semanticResults.stream()
                .sorted((a, b) -> {
                    double scoreA = calculatePersonalizedScore(a, profile);
                    double scoreB = calculatePersonalizedScore(b, profile);
                    return Double.compare(scoreB, scoreA);
                })
                .limit(3)
                .collect(Collectors.toList());
    }

    /**
     * 计算个性化得分
     */
    private double calculatePersonalizedScore(KnowledgeResult result, UserProfile profile) {
        double baseScore = result.score();
        
        // 根据用户偏好加权
        String category = (String) result.metadata().getOrDefault("category", "");
        double preferenceBoost = 0;
        
        if (profile.stylePreferences().containsKey(category)) {
            preferenceBoost = profile.stylePreferences().get(category) * 0.2;
        }
        
        return baseScore + preferenceBoost;
    }

    /**
     * 增强查询（注入个性化上下文）
     */
    private String enhanceQueryWithProfile(String originalQuery, UserProfile profile, 
                                            List<KnowledgeResult> knowledge) {
        StringBuilder enhanced = new StringBuilder();
        enhanced.append(originalQuery);
        
        // 添加用户偏好上下文
        if (!profile.stylePreferences().isEmpty()) {
            enhanced.append("\n\n[用户偏好] ");
            profile.stylePreferences().entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(3)
                    .forEach(entry -> enhanced.append(entry.getKey())
                            .append("(").append(String.format("%.0f", entry.getValue() * 100))
                            .append("%) "));
        }
        
        // 添加个性化知识
        if (!knowledge.isEmpty()) {
            enhanced.append("\n\n[个性化知识] ");
            knowledge.forEach(k -> enhanced.append(k.content()).append(" "));
        }
        
        return enhanced.toString();
    }

    /**
     * 构造响应
     */
    private AuraResponse buildResponse(ReActState state, long latency, UserProfile profile) {
        Map<String, Object> result = state.getFinalResult();
        
        return AuraResponse.builder()
                .sessionId(state.getSessionId().toString())
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

    /**
     * 用户画像
     */
    public record UserProfile(
            String userId,
            float[] cfEmbedding,
            Map<String, Double> stylePreferences,
            Map<String, Double> occasionPreferences,
            Map<String, Object> cfModelStatus
    ) {
        public Map<String, Object> toMap() {
            return Map.of(
                    "userId", userId,
                    "cfEmbeddingDim", cfEmbedding.length,
                    "stylePreferences", stylePreferences,
                    "occasionPreferences", occasionPreferences
            );
        }
    }
}
