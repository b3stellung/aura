package com.aura.service;

import com.aura.model.dto.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 对话历史上下文压缩器
 *
 * 策略：
 * 1. 保留最近 N 条消息（滑动窗口）
 * 2. 对更早的消息进行摘要式压缩
 * 3. 提取关键信息（用户偏好、已推荐方案、场景需求）保留为系统摘要
 * 4. 单条消息内容截断（防止单条过长）
 * 5. 总字符预算控制（防止总体超限）
 */
@Slf4j
@Component
public class ChatHistoryCompressor {

    /** 保留最近的完整消息轮数（user+assistant 各算一条） */
    private static final int RECENT_MESSAGE_LIMIT = 10;

    /** 每条消息最大字符数 */
    private static final int MAX_SINGLE_MESSAGE_LENGTH = 800;

    /** 历史摘要（压缩部分）的最大字符数 */
    private static final int MAX_SUMMARY_CHAR_LENGTH = 600;

    /** 整个历史上下文的总字符预算 */
    private static final int TOTAL_CHAR_BUDGET = 4000;

    /** 触发压缩的最少消息数（少于此数直接返回） */
    private static final int COMPRESS_THRESHOLD = 12;

    /**
     * 压缩对话历史
     *
     * @param rawHistory 原始历史消息列表
     * @return 压缩后的消息列表
     */
    public List<ChatMessage> compress(List<ChatMessage> rawHistory) {
        if (rawHistory == null || rawHistory.isEmpty()) {
            return Collections.emptyList();
        }

        log.info("开始压缩对话历史: 原始消息数={}", rawHistory.size());

        // 策略1：消息数不多时，只做单条截断
        if (rawHistory.size() <= COMPRESS_THRESHOLD) {
            List<ChatMessage> result = rawHistory.stream()
                    .map(this::truncateSingleMessage)
                    .collect(Collectors.toList());
            log.info("消息数较少，仅做单条截断: 结果消息数={}", result.size());
            return applyBudgetLimit(result);
        }

        // 策略2：消息数较多时，分两部分处理
        //  - 旧消息 → 摘要压缩
        //  - 新消息 → 完整保留（截断单条）
        int splitIndex = rawHistory.size() - RECENT_MESSAGE_LIMIT;
        List<ChatMessage> oldMessages = rawHistory.subList(0, splitIndex);
        List<ChatMessage> recentMessages = rawHistory.subList(splitIndex, rawHistory.size());

        // 从旧消息中提取关键信息，生成摘要消息
        String summary = buildSummary(oldMessages);
        ChatMessage summaryMessage = ChatMessage.builder()
                .role("system")
                .content(summary)
                .timestamp(oldMessages.get(0).getTimestamp())
                .build();

        // 最近消息做单条截断
        List<ChatMessage> compressedRecent = recentMessages.stream()
                .map(this::truncateSingleMessage)
                .collect(Collectors.toList());

        // 组合：[摘要] + [最近N条]
        List<ChatMessage> result = new ArrayList<>();
        result.add(summaryMessage);
        result.addAll(compressedRecent);

        log.info("对话历史压缩完成: 原始={}, 旧消息={}条→摘要, 保留最近={}条, 总结果={}条",
                rawHistory.size(), oldMessages.size(), recentMessages.size(), result.size());

        return applyBudgetLimit(result);
    }

    /**
     * 从旧消息中提取关键信息构建摘要
     * 提取：用户偏好、场景需求、已推荐方案
     */
    private String buildSummary(List<ChatMessage> oldMessages) {
        StringBuilder summary = new StringBuilder();
        summary.append("[历史对话摘要]\n");

        // 提取用户偏好
        List<String> userPreferences = new ArrayList<>();
        // 提取已推荐方案
        List<String> recommendedOutfits = new ArrayList<>();
        // 提取场景需求
        List<String> sceneRequirements = new ArrayList<>();

        for (ChatMessage msg : oldMessages) {
            String content = msg.getContent();
            if (content == null || content.isBlank()) continue;

            if ("user".equals(msg.getRole())) {
                // 提取用户偏好关键词
                extractPreferences(content, userPreferences);
                // 提取场景需求
                extractScenes(content, sceneRequirements);
            } else if ("assistant".equals(msg.getRole())) {
                // 提取推荐方案名称
                extractRecommendations(content, recommendedOutfits);
            }
        }

        if (!userPreferences.isEmpty()) {
            summary.append("用户偏好: ");
            // 去重并限制数量
            List<String> uniquePrefs = userPreferences.stream().distinct().limit(10).collect(Collectors.toList());
            summary.append(String.join("、", uniquePrefs));
            summary.append("\n");
        }

        if (!sceneRequirements.isEmpty()) {
            summary.append("场景需求: ");
            List<String> uniqueScenes = sceneRequirements.stream().distinct().limit(5).collect(Collectors.toList());
            summary.append(String.join("；", uniqueScenes));
            summary.append("\n");
        }

        if (!recommendedOutfits.isEmpty()) {
            summary.append("已推荐: ");
            List<String> uniqueRecs = recommendedOutfits.stream().distinct().limit(5).collect(Collectors.toList());
            summary.append(String.join("、", uniqueRecs));
            summary.append("\n");
        }

        // 如果没提取到任何关键信息，做一个简单概括
        if (userPreferences.isEmpty() && sceneRequirements.isEmpty() && recommendedOutfits.isEmpty()) {
            // 取最后几条用户消息的前50字做概括
            List<String> userSnippets = oldMessages.stream()
                    .filter(m -> "user".equals(m.getRole()) && m.getContent() != null)
                    .map(m -> {
                        String c = m.getContent();
                        return c.length() > 50 ? c.substring(0, 50) + "..." : c;
                    })
                    .collect(Collectors.toList());
            if (!userSnippets.isEmpty()) {
                summary.append("历史话题: ").append(String.join(" → ", userSnippets));
                summary.append("\n");
            }
        }

        summary.append("（以上为早期对话的压缩摘要，保留关键偏好和推荐信息）");

        String result = summary.toString();
        if (result.length() > MAX_SUMMARY_CHAR_LENGTH) {
            result = result.substring(0, MAX_SUMMARY_CHAR_LENGTH) + "...]";
        }
        return result;
    }

    /**
     * 从用户消息中提取偏好关键词
     */
    private void extractPreferences(String content, List<String> preferences) {
        // 偏好相关关键词
        String[] prefPatterns = {
                "喜欢", "偏好", "想要", "希望", "爱穿", "不爱穿",
                "不要", "不适合", "过敏", "讨厌", "风格"
        };
        for (String pattern : prefPatterns) {
            if (content.contains(pattern)) {
                // 提取包含该关键词的短句
                String snippet = extractSentence(content, pattern);
                if (snippet != null && snippet.length() <= 60) {
                    preferences.add(snippet);
                }
            }
        }
        // 提取颜色偏好
        String[] colors = {"黑色", "白色", "红色", "蓝色", "绿色", "粉色", "灰色", "米色", "卡其", "深色", "浅色"};
        for (String color : colors) {
            if (content.contains(color)) {
                preferences.add(color + "系");
            }
        }
        // 提取风格偏好
        String[] styles = {"简约", "休闲", "商务", "运动", "复古", "街头", "甜美", "知性", "优雅", "学院", "日系", "法式", "韩系"};
        for (String style : styles) {
            if (content.contains(style)) {
                preferences.add(style + "风格");
            }
        }
    }

    /**
     * 从用户消息中提取场景需求
     */
    private void extractScenes(String content, List<String> scenes) {
        String[] sceneKeywords = {
                "上班", "约会", "面试", "聚会", "旅行", "逛街", "运动", "婚礼",
                "会议", "商务", "度假", "郊游", "毕业", "年会", "晚宴", "通勤",
                "见家长", "相亲", "拍照", "演唱会", "野餐"
        };
        for (String keyword : sceneKeywords) {
            if (content.contains(keyword)) {
                String snippet = extractSentence(content, keyword);
                if (snippet != null && snippet.length() <= 40) {
                    scenes.add(snippet);
                }
            }
        }
    }

    /**
     * 从助手消息中提取推荐方案名称
     */
    private void extractRecommendations(String content, List<String> recommendations) {
        // 尝试匹配常见方案名称模式
        Pattern[] patterns = {
                Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\""),
                Pattern.compile("【(.+?)】"),
                Pattern.compile("方案[一二三四五六七八九十\\d]+[：:]\\s*(.+?)(?:\\n|$)"),
                Pattern.compile("穿搭方案[：:]\\s*(.+?)(?:\\n|$)")
        };
        for (Pattern p : patterns) {
            Matcher m = p.matcher(content);
            while (m.find() && recommendations.size() < 8) {
                String name = m.group(1).trim();
                if (name.length() <= 20 && !name.isEmpty()) {
                    recommendations.add(name);
                }
            }
        }
    }

    /**
     * 提取包含关键词的短句
     */
    private String extractSentence(String content, String keyword) {
        int idx = content.indexOf(keyword);
        if (idx < 0) return null;

        // 向前找标点或开头
        int start = Math.max(0, idx - 15);
        for (int i = idx - 1; i >= start; i--) {
            char c = content.charAt(i);
            if (c == '。' || c == '，' || c == '！' || c == '？' || c == '\n' || c == '；') {
                start = i + 1;
                break;
            }
        }

        // 向后找标点或结尾
        int end = Math.min(content.length(), idx + keyword.length() + 20);
        for (int i = idx + keyword.length(); i < end; i++) {
            char c = content.charAt(i);
            if (c == '。' || c == '，' || c == '！' || c == '？' || c == '\n' || c == '；') {
                end = i;
                break;
            }
        }

        return content.substring(start, end).trim();
    }

    /**
     * 截断单条消息
     */
    private ChatMessage truncateSingleMessage(ChatMessage msg) {
        if (msg.getContent() == null || msg.getContent().length() <= MAX_SINGLE_MESSAGE_LENGTH) {
            return msg;
        }
        return ChatMessage.builder()
                .role(msg.getRole())
                .content(msg.getContent().substring(0, MAX_SINGLE_MESSAGE_LENGTH) + "...")
                .timestamp(msg.getTimestamp())
                .build();
    }

    /**
     * 总字符预算控制：如果总内容超过预算，从最前面的消息开始缩短
     */
    private List<ChatMessage> applyBudgetLimit(List<ChatMessage> messages) {
        int totalChars = messages.stream()
                .mapToInt(m -> m.getContent() != null ? m.getContent().length() : 0)
                .sum();

        if (totalChars <= TOTAL_CHAR_BUDGET) {
            return messages;
        }

        log.info("总字符数({})超过预算({}), 开始裁剪", totalChars, TOTAL_CHAR_BUDGET);

        // 从第一条开始缩短，保留最后几条完整
        List<ChatMessage> result = new ArrayList<>(messages);
        int overBudget = totalChars - TOTAL_CHAR_BUDGET;

        for (int i = 0; i < result.size() - 2 && overBudget > 0; i++) {
            ChatMessage msg = result.get(i);
            String content = msg.getContent();
            if (content == null) continue;

            if (content.length() > 100) {
                int cutLen = Math.min(overBudget, content.length() - 100);
                String newContent = content.substring(0, content.length() - cutLen) + "...(已压缩)";
                result.set(i, ChatMessage.builder()
                        .role(msg.getRole())
                        .content(newContent)
                        .timestamp(msg.getTimestamp())
                        .build());
                overBudget -= cutLen;
            }
        }

        return result;
    }
}
