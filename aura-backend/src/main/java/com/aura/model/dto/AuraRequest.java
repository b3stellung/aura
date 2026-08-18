package com.aura.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Aura 推荐请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuraRequest {

    /**
     * 用户输入文本
     * 示例："今晚有个约会，我穿了这件燕麦色风衣"
     */
    @NotBlank(message = "输入文本不能为空")
    @Size(max = 2000, message = "输入文本长度不能超过2000个字符")
    private String text;

    /**
     * 用户上传的图片 URL 列表
     */
    @Size(max = 10, message = "最多上传10张图片")
    private List<String> images;

    /**
     * 用户所在位置（可选，用于天气查询）
     */
    @Size(max = 200, message = "位置信息长度不能超过200个字符")
    private String location;

    /**
     * 场景类型（可选）
     * - date: 约会
     * - work: 职场
     * - casual: 休闲
     * - party: 派对
     * - wedding: 婚礼
     */
    @Size(max = 50, message = "场景类型长度不能超过50个字符")
    private String occasion;

    /**
     * 是否需要详细解析（ReAct 过程可视化）
     */
    @Builder.Default
    private boolean needAnalysis = true;
}
