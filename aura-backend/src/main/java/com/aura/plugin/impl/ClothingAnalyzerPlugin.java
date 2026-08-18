package com.aura.plugin.impl;

import com.aura.plugin.AuraPlugin;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * 衣物分析插件
 * 
 * 使用 Qwen3.7-Plus 分析衣物图片，提取属性
 */
@Slf4j
@Component
public class ClothingAnalyzerPlugin implements AuraPlugin {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    public ClothingAnalyzerPlugin(ChatModel chatModel, ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "ClothingAnalyzer";
    }

    @Override
    public String getDescription() {
        return "分析衣物图片，提取颜色、材质、风格等属性";
    }

    @Override
    public String getToolSchema() {
        return """
                {
                    "name": "ClothingAnalyzer",
                    "description": "分析衣物图片，提取颜色、材质、风格等属性",
                    "parameters": {
                        "type": "object",
                        "properties": {
                            "image_url": {
                                "type": "string",
                                "description": "衣物图片的 URL"
                            }
                        },
                        "required": ["image_url"]
                    }
                }
                """;
    }

    @Override
    public Object execute(Map<String, Object> params) {
        String imageUrl = (String) params.get("image_url");
        
        // 如果没有图片URL，返回提示信息而不是崩溃
        if (imageUrl == null || imageUrl.isBlank()) {
            log.warn("未提供图片URL，返回默认分析结果");
            return Map.of(
                "color", "未知",
                "material", "未知",
                "style", List.of("休闲"),
                "category", "未分类",
                "season", List.of("四季"),
                "occasion", List.of("日常"),
                "vibe", "自然舒适",
                "pattern", "未知",
                "fit", "未知",
                "note", "请提供衣物图片以获得精准分析"
            );
        }
        
        log.info("分析衣物图片: {}", imageUrl);

        try {
            // 构造 Prompt
            String promptText = """
                    请详细分析这张衣物图片，输出严格的 JSON 格式，不要包含其他内容：
                    
                    {
                        "color": "主色调（如：燕麦色、藏青色、米白色）",
                        "material": "材质（如：亚麻、羊毛、丝绸、棉）",
                        "style": ["风格标签1", "风格标签2"],
                        "category": "类别（外套/内搭/下装/鞋包/配饰）",
                        "season": ["适用季节"],
                        "occasion": ["适用场合"],
                        "vibe": "整体氛围（如：松弛感、高级感、复古、简约）",
                        "pattern": "图案（如：纯色、条纹、格纹、碎花）",
                        "fit": "版型（如：修身、宽松、直筒）"
                    }
                    """;

            // 构造多模态消息
            var userMessage = org.springframework.ai.chat.messages.UserMessage.builder()
                    .text(promptText)
                    .media(new Media(MimeTypeUtils.IMAGE_PNG, URI.create(imageUrl)))
                    .build();

            // 调用模型
            var response = chatModel.call(new Prompt(List.of(userMessage)));
            String content = response.getResult().getOutput().getText();

            // 提取 JSON（去掉可能的 markdown 格式）
            String json = content.replaceAll("```json\\s*", "")
                                 .replaceAll("```\\s*", "")
                                 .trim();

            log.info("衣物分析结果: {}", json);

            // 解析 JSON 返回
            return objectMapper.readValue(json, Map.class);

        } catch (Exception e) {
            log.error("衣物分析失败", e);
            throw new RuntimeException("衣物分析失败: " + e.getMessage(), e);
        }
    }
}
