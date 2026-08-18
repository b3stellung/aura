package com.aura.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 衣橱物品响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WardrobeItemResponse {

    /**
     * 物品ID
     */
    private UUID id;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 分类
     */
    private String category;

    /**
     * 颜色
     */
    private String color;

    /**
     * 材质
     */
    private String material;

    /**
     * 风格标签
     */
    private List<String> styleTags;

    /**
     * 季节标签
     */
    private List<String> seasonTags;

    /**
     * 场合标签
     */
    private List<String> occasionTags;

    /**
     * 穿搭次数
     */
    private Integer wearCount;

    /**
     * 最后穿搭时间
     */
    private LocalDateTime lastWornAt;

    /**
     * 扩展属性（JSON字符串）
     */
    private String attributes;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
