package com.aura.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Aura 推荐响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuraResponse {

    /**
     * 会话 ID
     */
    private String sessionId;

    /**
     * 对话 ID（用于多轮对话上下文）
     */
    private String conversationId;

    /**
     * 推荐标题（故事化）
     * 示例："秋日午后的热红茶"
     */
    private String title;

    /**
     * 推荐文案（故事化描述）
     */
    private String storyText;

    /**
     * 推荐的穿搭方案列表
     */
    private List<OutfitRecommendation> outfits;

    /**
     * ReAct 推理分析（可选）
     */
    private ReactAnalysis analysis;

    /**
     * 推荐置信度 (0-1)
     */
    private Double confidence;

    /**
     * Token 消耗量
     */
    private Integer tokenConsumed;

    /**
     * 处理耗时（毫秒）
     */
    private Integer latencyMs;

    /**
     * 穿搭推荐
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OutfitRecommendation {
        /**
         * 推荐名称
         */
        private String name;

        /**
         * 推荐描述
         */
        private String description;

        /**
         * 衣物列表
         */
        private List<ClothingItem> items;

        /**
         * 适合场合
         */
        private List<String> occasions;

        /**
         * 适合季节
         */
        private List<String> seasons;

        /**
         * 整体风格
         */
        private String vibe;

        /**
         * 匹配分数 (0-1)
         */
        private Double score;
    }

    /**
     * 衣物项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClothingItem {
        private String id;
        private String name;
        private String imageUrl;
        private String category;
        private String color;
        private String material;
        private List<String> styleTags;
    }

    /**
     * ReAct 推理分析
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReactAnalysis {
        /**
         * 风格检查
         */
        private Map<String, Object> vibeCheck;

        /**
         * 美学检索过程
         */
        private Map<String, Object> aestheticSearch;

        /**
         * 搭配原理解释
         */
        private Map<String, Object> theWhy;

        /**
         * ReAct 步骤数
         */
        private Integer stepCount;

        /**
         * 使用的插件
         */
        private List<String> pluginsUsed;
    }
}
