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
import java.util.UUID;

/**
 * 插件注册实体
 */
@Entity
@Table(name = "plugin_registry")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginRegistry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "plugin_name", unique = true, nullable = false, length = 100)
    private String pluginName;

    /**
     * 插件类型
     * - tool: 工具插件
     * - memory: 记忆插件
     * - model: 模型插件
     */
    @Column(name = "plugin_type", nullable = false, length = 50)
    private String pluginType;

    @Column(length = 500)
    private String description;

    /**
     * Tool Schema（OpenAPI 格式）
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tool_schema", columnDefinition = "jsonb")
    private String toolSchema;

    /**
     * 远程插件端点 URL
     */
    @Column(name = "endpoint_url")
    private String endpointUrl;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
