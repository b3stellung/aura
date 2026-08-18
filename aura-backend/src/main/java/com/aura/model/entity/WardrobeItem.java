package com.aura.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 用户衣橱物品实体
 * 
 * 注意：主要数据存在 Milvus，这里存储业务元数据
 */
@Entity
@Table(name = "wardrobe_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WardrobeItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(nullable = false, length = 50)
    private String category;  // 外套/内搭/下装/鞋包/配饰

    @Column(length = 50)
    private String color;  // 主色调

    @Column(length = 50)
    private String material;  // 材质

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "style_tags", columnDefinition = "text[]")
    private List<String> styleTags;  // 风格标签

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "season_tags", columnDefinition = "text[]")
    private List<String> seasonTags;  // 适用季节

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "occasion_tags", columnDefinition = "text[]")
    private List<String> occasionTags;  // 适用场合

    @Column(name = "wear_count")
    @Builder.Default
    private Integer wearCount = 0;

    @Column(name = "last_worn_at")
    private LocalDateTime lastWornAt;

    /**
     * VLM 提取的详细属性（JSON 格式）
     * 示例：{"vibe": "松弛感", "pattern": "纯色", "fit": "宽松"}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String attributes;

    /**
     * Milvus 中的向量 ID（用于关联）
     */
    @Column(name = "milvus_id", length = 36)
    private String milvusId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
