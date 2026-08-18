package com.aura.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Agent 会话实体
 * 记录 ReAct 推理过程和结果
 */
@Entity
@Table(name = "agent_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 会话类型
     * - outfit_recommendation: 穿搭推荐
     * - makeup_tutorial: 妆容教程
     * - style_analysis: 风格分析
     */
    @Column(name = "session_type", length = 50)
    private String sessionType;

    @Column(name = "input_text")
    private String inputText;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "input_images", columnDefinition = "text[]")
    private List<String> inputImages;

    /**
     * ReAct 推理轨迹（JSON 格式）
     * 包含每个步骤的 Thought、Action、Observation
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "react_trace", columnDefinition = "jsonb")
    private String reactTrace;

    /**
     * 最终输出结果（JSON 格式）
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "final_output", columnDefinition = "jsonb")
    private String finalOutput;

    /**
     * 本次使用的插件列表
     */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "plugins_used", columnDefinition = "text[]")
    private List<String> pluginsUsed;

    /**
     * Token 消耗量
     */
    @Column(name = "token_consumed")
    @Builder.Default
    private Integer tokenConsumed = 0;

    /**
     * 总耗时（毫秒）
     */
    @Column(name = "latency_ms")
    private Integer latencyMs;

    /**
     * 会话状态
     * - running: 执行中
     * - success: 成功
     * - failed: 失败
     * - timeout: 超时
     */
    @Column(length = 20)
    @Builder.Default
    private String status = "running";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
