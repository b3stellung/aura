package com.aura.react;

import com.aura.model.dto.ChatMessage;
import com.aura.plugin.PluginManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Aura ReAct 引擎
 * 
 * 实现 Reasoning + Acting 循环
 */
@Slf4j
@Service
public class AuraReActEngine {

    private final ChatModel chatModel;
    private final PluginManager pluginManager;
    private final ObjectMapper objectMapper;

    /**
     * 进行中的会话状态
     */
    private final Map<UUID, ReActState> activeSessions = new ConcurrentHashMap<>();

    public AuraReActEngine(ChatModel chatModel, PluginManager pluginManager, ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.pluginManager = pluginManager;
        this.objectMapper = objectMapper;
    }

    /**
     * 执行 ReAct 循环
     */
    public ReActState executeReActLoop(String userId, String userInput, List<String> userImages,
                                        String conversationId, List<ChatMessage> chatHistory) {
        UUID sessionId = UUID.randomUUID();
        log.info("开始 ReAct 循环: sessionId={}, userId={}", sessionId, userId);

        // 初始化状态
        ReActState state = ReActState.builder()
                .sessionId(sessionId)
                .conversationId(conversationId)
                .userId(userId)
                .userInput(userInput)
                .userImages(userImages)
                .chatHistory(chatHistory != null ? chatHistory : new java.util.ArrayList<>())
                .availablePlugins(pluginManager.getAvailablePlugins().stream()
                        .map(p -> p.getName())
                        .collect(Collectors.toList()))
                .build();

        activeSessions.put(sessionId, state);

        try {
            // ReAct 循环
            int toolCallCount = 0;
            while (state.shouldContinue()) {
                log.info("ReAct 步骤: {}/{}", state.getStepCount() + 1, state.getMaxSteps());

                // Thought: 推理下一步行动
                String thought = generateThought(state);
                state.addMessage(ReActState.Message.thought(thought));
                log.info("Thought: {}", thought);

                // 检查是否结束（支持markdown代码块和裸JSON）
                if (containsJsonKey(thought, "finish")) {
                    state.setFinalResult(parseFinalResult(thought));
                    break;
                }

                // 检查是否需要调用工具（支持markdown代码块和裸JSON）
                if (containsJsonKey(thought, "tool")) {
                    // 强制限制：最多2次工具调用
                    toolCallCount++;
                    if (toolCallCount > 2) {
                        log.warn("工具调用次数超限({}), 强制生成最终结果", toolCallCount);
                        // 用已有信息生成降级结果
                        Map<String, Object> fallback = new java.util.HashMap<>();
                        fallback.put("title", "今日穿搭建议");
                        fallback.put("story_text", "根据已有信息为您提供建议。");
                        fallback.put("outfits", java.util.Collections.emptyList());
                        fallback.put("confidence", 0.6);
                        state.setFinalResult(fallback);
                        break;
                    }
                    
                    // Action: 调用插件
                    ReActState.ToolCall toolCall = parseToolCall(thought);
                    state.addMessage(ReActState.Message.action(toolCall));

                    // 执行工具
                    Object observation = pluginManager.executePlugin(
                            toolCall.getToolName(), 
                            toolCall.getParams()
                    );
                    state.addMessage(ReActState.Message.observation(observation));
                    log.info("Observation: {}", observation);
                }

                state.incrementStep();
            }

            // 检查是否超时
            if (state.getFinalResult() == null) {
                log.warn("ReAct 循环达到最大步数: sessionId={}, steps={}", sessionId, state.getStepCount());
                // 优雅降级：用最后一条Thought作为结果，而不是抛异常
                String lastThought = state.getMessages().stream()
                        .filter(m -> "thought".equals(m.getType()))
                        .reduce((a, b) -> b)
                        .map(ReActState.Message::getContent)
                        .orElse("抱歉，无法生成穿搭推荐");
                
                Map<String, Object> fallbackResult = new java.util.HashMap<>();
                fallbackResult.put("title", "今日穿搭建议");
                fallbackResult.put("story_text", lastThought);
                fallbackResult.put("outfits", java.util.Collections.emptyList());
                fallbackResult.put("confidence", 0.5);
                state.setFinalResult(fallbackResult);
            }

            log.info("ReAct 循环完成: sessionId={}, steps={}, tokens={}", 
                    sessionId, state.getStepCount(), state.getTokenConsumed());
            return state;

        } finally {
            activeSessions.remove(sessionId);
        }
    }

    /**
     * 执行 ReAct 循环（带 SSE 回调）
     */
    public ReActState executeReActLoopWithCallback(String userId, String userInput,
                                                    List<String> userImages,
                                                    String conversationId,
                                                    List<ChatMessage> chatHistory,
                                                    BiConsumer<String, Object> callback) {
        UUID sessionId = UUID.randomUUID();
        log.info("开始 ReAct 循环（带回调）: sessionId={}", sessionId);

        // 初始化状态
        ReActState state = ReActState.builder()
                .sessionId(sessionId)
                .conversationId(conversationId)
                .userId(userId)
                .userInput(userInput)
                .userImages(userImages)
                .chatHistory(chatHistory != null ? chatHistory : new java.util.ArrayList<>())
                .availablePlugins(pluginManager.getAvailablePlugins().stream()
                        .map(p -> p.getName())
                        .collect(Collectors.toList()))
                .build();

        activeSessions.put(sessionId, state);

        try {
            // 发送开始事件
            callback.accept("start", Map.of("sessionId", sessionId.toString()));

            // ReAct 循环
            int toolCallCount = 0;
            while (state.shouldContinue()) {
                // Thought
                String thought = generateThought(state);
                state.addMessage(ReActState.Message.thought(thought));
                callback.accept("thought", Map.of(
                        "step", state.getStepCount() + 1,
                        "content", thought
                ));

                // 检查是否结束（支持markdown代码块和裸JSON）
                if (containsJsonKey(thought, "finish")) {
                    state.setFinalResult(parseFinalResult(thought));
                    break;
                }

                // 检查是否需要调用工具（支持markdown代码块和裸JSON）
                if (containsJsonKey(thought, "tool")) {
                    // 强制限制：最多2次工具调用
                    toolCallCount++;
                    if (toolCallCount > 2) {
                        log.warn("工具调用次数超限({}), 强制生成最终结果", toolCallCount);
                        Map<String, Object> fallback = new java.util.HashMap<>();
                        fallback.put("title", "今日穿搭建议");
                        fallback.put("story_text", "根据已有信息为您提供建议。");
                        fallback.put("outfits", java.util.Collections.emptyList());
                        fallback.put("confidence", 0.6);
                        state.setFinalResult(fallback);
                        callback.accept("fallback", Map.of("message", "工具调用次数超限，返回已有分析"));
                        break;
                    }
                    
                    ReActState.ToolCall toolCall = parseToolCall(thought);
                    state.addMessage(ReActState.Message.action(toolCall));
                    callback.accept("action", Map.of(
                            "step", state.getStepCount() + 1,
                            "tool", toolCall.getToolName(),
                            "params", toolCall.getParams()
                    ));

                    Object observation = pluginManager.executePlugin(
                            toolCall.getToolName(), 
                            toolCall.getParams()
                    );
                    state.addMessage(ReActState.Message.observation(observation));
                    callback.accept("observation", Map.of(
                            "step", state.getStepCount() + 1,
                            "result", observation
                    ));
                }

                state.incrementStep();
            }

            // 如果循环结束但没有finalResult，优雅降级
            if (state.getFinalResult() == null) {
                String lastThought = state.getMessages().stream()
                        .filter(m -> "thought".equals(m.getType()))
                        .reduce((a, b) -> b)
                        .map(ReActState.Message::getContent)
                        .orElse("抱歉，无法生成穿搭推荐");
                
                Map<String, Object> fallbackResult = new java.util.HashMap<>();
                fallbackResult.put("title", "今日穿搭建议");
                fallbackResult.put("story_text", lastThought);
                fallbackResult.put("outfits", java.util.Collections.emptyList());
                fallbackResult.put("confidence", 0.5);
                state.setFinalResult(fallbackResult);
                callback.accept("fallback", Map.of("message", "达到最大步数，返回已有分析"));
            }

            // 发送完成事件
            callback.accept("done", Map.of(
                    "sessionId", sessionId.toString(),
                    "steps", state.getStepCount(),
                    "tokens", state.getTokenConsumed()
            ));

            return state;

        } finally {
            activeSessions.remove(sessionId);
        }
    }

    private static final int MAX_RETRY_COUNT = 3;
    private static final long RETRY_DELAY_MS = 1000;

    /**
     * 生成 Thought（带重试机制）
     */
    private String generateThought(ReActState state) {
        String prompt = buildThoughtPrompt(state);
        log.info("调用AI模型, prompt长度={}", prompt.length());
        
        Exception lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRY_COUNT; attempt++) {
            try {
                var response = chatModel.call(new Prompt(prompt));
                String content = response.getResult().getOutput().getText();
                log.info("AI返回内容长度={}, 尝试次数={}", content != null ? content.length() : 0, attempt);
                
                // 清理AI回复：去除markdown代码块标记
                content = cleanAiResponse(content);
                
                // 验证回复是否包含有效JSON
                if (!containsValidJson(content)) {
                    log.warn("AI回复不含有效JSON，重试 (attempt={}/{}): {}", attempt, MAX_RETRY_COUNT, 
                            content.substring(0, Math.min(200, content.length())));
                    if (attempt < MAX_RETRY_COUNT) {
                        Thread.sleep(RETRY_DELAY_MS * attempt);
                        continue;
                    }
                }
                
                // 统计 Token（简化）
                state.addTokens(content.length() / 4);
                return content;
                
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.error("重试等待被中断", ie);
                break;
            } catch (Exception e) {
                lastException = e;
                log.warn("AI模型调用失败 (attempt={}/{}): {}", attempt, MAX_RETRY_COUNT, e.getMessage());
                if (attempt < MAX_RETRY_COUNT) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        // 所有重试失败，返回降级响应
        String errorMsg = lastException != null ? lastException.getMessage() : "AI回复格式无效";
        log.error("AI模型调用最终失败（已重试{}次）: {}", MAX_RETRY_COUNT, errorMsg);
        return "{\"finish\": true, \"result\": {\"title\": \"今日推荐\", \"story_text\": \"AI服务暂时不可用，请稍后重试。\", \"outfits\": [], \"confidence\": 0.1}}";
    }
    
    /**
     * 清理AI回复：去除markdown代码块标记和前后空白
     */
    private String cleanAiResponse(String content) {
        if (content == null) return "";
        content = content.trim();
        // 去除 ```json ... ``` 或 ``` ... ``` 包裹
        if (content.startsWith("```")) {
            int firstNewline = content.indexOf('\n');
            if (firstNewline > 0) {
                content = content.substring(firstNewline + 1);
            }
            if (content.endsWith("```")) {
                content = content.substring(0, content.length() - 3);
            }
            content = content.trim();
        }
        return content;
    }
    
    /**
     * 验证文本中是否包含有效JSON对象
     */
    private boolean containsValidJson(String content) {
        if (content == null) return false;
        return content.contains("{") && content.contains("}") && content.contains("\"");
    }
    
    /**
     * 检查文本中是否包含指定JSON键（支持markdown代码块和裸JSON）
     */
    private boolean containsJsonKey(String text, String key) {
        if (text == null) return false;
        String keyPattern = "\"" + key + "\"";
        // 直接检查（裸JSON或代码块内都包含此字符串）
        return text.contains(keyPattern);
    }

    /**
     * 构造 Thought Prompt（精简版）
     */
    private String buildThoughtPrompt(ReActState state) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("你是Aura，专业时尚造型师。根据用户需求提供穿搭建议。\n\n");
        
        // 当前状态
        sb.append("## 当前状态\n");
        sb.append("- 用户输入：").append(state.getUserInput()).append("\n");
        
        // 历史对话
        List<ChatMessage> chatHistory = state.getChatHistory();
        if (chatHistory != null && !chatHistory.isEmpty()) {
            sb.append("- 历史对话：\n");
            for (ChatMessage msg : chatHistory) {
                String role = "system".equals(msg.getRole()) ? "摘要" : ("user".equals(msg.getRole()) ? "用户" : "助手");
                sb.append("  ").append(role).append(": ").append(msg.getContent(), 0, Math.min(200, msg.getContent().length())).append("\n");
            }
        }
        
        // 已完成步骤
        if (!state.getMessages().isEmpty()) {
            sb.append("- 已完成步骤：\n");
            for (ReActState.Message msg : state.getMessages()) {
                String preview = msg.getContent() != null ? msg.getContent().substring(0, Math.min(80, msg.getContent().length())) : "";
                sb.append("  [").append(msg.getType()).append("] ").append(preview).append("\n");
            }
        }
        
        // 已调用工具
        List<String> calledTools = state.getMessages().stream()
            .filter(m -> m.getType() == ReActState.MessageType.ACTION && m.getToolCall() != null)
            .map(m -> m.getToolCall().getToolName())
            .distinct()
            .collect(Collectors.toList());
        if (!calledTools.isEmpty()) {
            sb.append("- 已调用工具（禁止重复）：").append(String.join(", ", calledTools)).append("\n");
        }
        
        sb.append("- 可用工具：").append(String.join(", ", state.getAvailablePlugins())).append("\n\n");
        
        // 规则
        sb.append("## 规则\n");
        sb.append("1. 需要天气信息时调用WeatherPlugin，需要穿搭知识时调用RAGSearch\n");
        sb.append("2. 最多调用2次工具，禁止重复调用同一工具\n");
        sb.append("3. 获得工具结果后，直接输出finish JSON\n");
        sb.append("4. 即使信息不完整，也要给出穿搭建议\n\n");
        
        // 输出格式
        sb.append("## 输出格式\n");
        sb.append("工具调用：{\"tool\":\"工具名\",\"params\":{}}\n");
        sb.append("最终结果：{\"finish\":true,\"result\":{\"title\":\"标题\",\"story_text\":\"100字文案\",\"outfits\":[{\"name\":\"方案名\",\"description\":\"含具体单品颜色材质的描述\",\"vibe\":\"风格\",\"score\":0.9}],\"confidence\":0.8}}\n\n");
        
        sb.append("请输出JSON：");
        
        return sb.toString();
    }

    /**
     * 解析 Tool Call（增强版：支持多种JSON格式）
     */
    private ReActState.ToolCall parseToolCall(String thought) {
        try {
            String json = extractJsonObject(thought, "\"tool\"");
            if (json == null) {
                throw new RuntimeException("无法从AI回复中提取tool JSON: " + thought.substring(0, Math.min(200, thought.length())));
            }
            Map<String, Object> map = objectMapper.readValue(json, new TypeReference<>() {});
            
            String toolName = (String) map.get("tool");
            if (toolName == null || toolName.isBlank()) {
                throw new RuntimeException("tool名称为空");
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> params = (Map<String, Object>) map.get("params");
            
            return ReActState.ToolCall.builder()
                    .toolName(toolName)
                    .params(params != null ? params : Map.of())
                    .build();
        } catch (Exception e) {
            log.error("解析 Tool Call 失败: {}", thought, e);
            throw new RuntimeException("解析 Tool Call 失败", e);
        }
    }

    /**
     * 解析最终结果（增强版：支持多种JSON格式）
     */
    private Map<String, Object> parseFinalResult(String thought) {
        try {
            String json = extractJsonObject(thought, "\"finish\"");
            if (json == null) {
                log.warn("无法提取标准JSON，尝试容错解析: {}", thought.substring(0, Math.min(200, thought.length())));
                return buildFallbackResult(thought);
            }
            
            Map<String, Object> map = objectMapper.readValue(json, new TypeReference<>() {});
            Object result = map.get("result");
            
            if (result instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> resultMap = (Map<String, Object>) result;
                return resultMap;
            }
            
            log.warn("result字段不是Map类型: {}", result);
            return buildFallbackResult(thought);
            
        } catch (Exception e) {
            log.error("解析最终结果失败: {}", thought, e);
            return buildFallbackResult(thought);
        }
    }
    
    /**
     * 从文本中提取JSON对象（支持markdown代码块和裸JSON）
     */
    private String extractJsonObject(String text, String requiredKey) {
        if (text == null) return null;
        
        // 1. 先尝试从markdown代码块中提取
        if (text.contains("```")) {
            int start = text.indexOf("```");
            start = text.indexOf('\n', start);
            if (start > 0) {
                start++;
                int end = text.indexOf("```", start);
                if (end > start) {
                    String candidate = text.substring(start, end).trim();
                    if (candidate.contains(requiredKey)) {
                        return candidate;
                    }
                }
            }
        }
        
        // 2. 尝试用正则提取最外层的JSON对象
        int braceStart = text.indexOf('{');
        while (braceStart >= 0) {
            // 从这个 { 开始，找到匹配的 }
            int depth = 0;
            boolean inString = false;
            boolean escaped = false;
            for (int i = braceStart; i < text.length(); i++) {
                char c = text.charAt(i);
                if (escaped) {
                    escaped = false;
                    continue;
                }
                if (c == '\\') {
                    escaped = true;
                    continue;
                }
                if (c == '"') {
                    inString = !inString;
                    continue;
                }
                if (inString) continue;
                if (c == '{') depth++;
                if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        String candidate = text.substring(braceStart, i + 1);
                        if (candidate.contains(requiredKey)) {
                            return candidate;
                        }
                        break;
                    }
                }
            }
            // 找下一个 {
            braceStart = text.indexOf('{', braceStart + 1);
        }
        
        return null;
    }
    
    /**
     * 构建降级结果（当JSON解析完全失败时）
     */
    private Map<String, Object> buildFallbackResult(String rawText) {
        Map<String, Object> fallback = new java.util.HashMap<>();
        fallback.put("title", "今日穿搭建议");
        // 尝试提取有意义的文本
        String cleanText = rawText.replaceAll("[\\{\\}\\[\\]\"]", "").trim();
        if (cleanText.length() > 300) {
            cleanText = cleanText.substring(0, 300) + "...";
        }
        fallback.put("story_text", cleanText.isEmpty() ? "暂时无法生成详细的穿搭建议，请稍后重试。" : cleanText);
        fallback.put("outfits", java.util.Collections.emptyList());
        fallback.put("confidence", 0.3);
        return fallback;
    }
}
