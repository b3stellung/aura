package com.aura.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 衣橱物品请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WardrobeItemRequest {

    /**
     * 衣物图片URL
     */
    @NotBlank(message = "图片URL不能为空")
    @Size(max = 500, message = "图片URL长度不能超过500个字符")
    private String imageUrl;

    /**
     * 分类：外套/内搭/下装/鞋包/配饰
     */
    @NotBlank(message = "分类不能为空")
    @Size(max = 50, message = "分类长度不能超过50个字符")
    private String category;

    /**
     * 颜色
     */
    @Size(max = 50, message = "颜色长度不能超过50个字符")
    private String color;

    /**
     * 材质
     */
    @Size(max = 50, message = "材质长度不能超过50个字符")
    private String material;

    /**
     * 风格标签
     */
    @Size(max = 20, message = "最多20个风格标签")
    private List<String> styleTags;

    /**
     * 季节标签
     */
    @Size(max = 10, message = "最多10个季节标签")
    private List<String> seasonTags;

    /**
     * 场合标签
     */
    @Size(max = 10, message = "最多10个场合标签")
    private List<String> occasionTags;
}
