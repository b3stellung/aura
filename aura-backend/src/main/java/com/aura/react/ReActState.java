package com.aura.react;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.aura.model.dto.ChatMessage;

/**
 * ReAct 状态
 * 
 * 记录 ReAct 推理过程中的所有状态信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReActState {

    /**
     * 会话 ID
     */
    private UUID sessionId;

    /**
     * 对话 ID（用于多轮对话上下文追踪）
     * 前端传入，代表一个持续的对话线程
     */
    private String conversationId;

    /**
     * 用户 ID
     */
    private String userId;

    /**
     * 用户输入文本
     */
    private String userInput;

    /**
     * 用户输入图片
     */
    private List<String> userImages;

    /**
     * 消息历史
     */
    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    /**
     * 对话历史上下文（来自之前的对话轮次）
     * 仅用于构建prompt，不参与当前轮次的ReAct推理
     */
    @Builder.Default
    private List<ChatMessage> chatHistory = new ArrayList<>();

    /**
     * 可用插件列表
     */
    @Builder.Default
    private List<String> availablePlugins = new ArrayList<>();

    /**
     * 当前步骤数
     */
    @Builder.Default
    private int stepCount = 0;

    /**
     * 最大步骤数
     */
    @Builder.Default
    private int maxSteps = 10;

    /**
     * Token 消耗量
     */
    @Builder.Default
    private int tokenConsumed = 0;

    /**
     * 最终结果
     */
    private Map<String, Object> finalResult;

    /**
     * 添加消息
     */
    public void addMessage(Message message) {
        this.messages.add(message);
    }

    /**
     * 增加步骤数
     */
    public void incrementStep() {
        this.stepCount++;
    }

    /**
     * 增加 Token 消耗
     */
    public void addTokens(int tokens) {
        this.tokenConsumed += tokens;
    }

    /**
     * 检查是否应该继续
     */
    public boolean shouldContinue() {
        return stepCount < maxSteps && finalResult == null;
    }

    /**
     * 消息类型
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        /**
         * 消息类型
         */
        private MessageType type;

        /**
         * 消息内容
         */
        private String content;

        /**
         * 工具调用（如果是 Action）
         */
        private ToolCall toolCall;

        /**
         * 工具结果（如果是 Observation）
         */
        private Object toolResult;

        /**
         * 时间戳
         */
        private long timestamp;

        public static Message thought(String content) {
            return Message.builder()
                    .type(MessageType.THOUGHT)
                    .content(content)
                    .timestamp(System.currentTimeMillis())
                    .build();
        }

        public static Message action(ToolCall toolCall) {
            return Message.builder()
                    .type(MessageType.ACTION)
                    .toolCall(toolCall)
                    .timestamp(System.currentTimeMillis())
                    .build();
        }

        public static Message observation(Object result) {
            return Message.builder()
                    .type(MessageType.OBSERVATION)
                    .toolResult(result)
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }

    /**
     * 消息类型枚举
     */
    public enum MessageType {
        THOUGHT,
        ACTION,
        OBSERVATION
    }

    /**
     * 工具调用
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolCall {
        /**
         * 工具名称
         */
        private String toolName;

        /**
         * 工具参数
         */
        private Map<String, Object> params;
    }
}
