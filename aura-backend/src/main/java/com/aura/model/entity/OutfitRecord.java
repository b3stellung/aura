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
 * 用户穿搭记录
 * 
 * 记录用户的穿搭组合，用于学习搭配偏好
 */
@Entity
@Table(name = "outfit_records", indexes = {
    @Index(name = "idx_outfit_user", columnList = "user_id"),
    @Index(name = "idx_outfit_occasion", columnList = "occasion")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutfitRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 穿搭场合
     * - date: 约会
     * - work: 职场
     * - casual: 休闲
     * - party: 派对
     * - formal: 正式
     */
    @Column(nullable = false, length = 50)
    private String occasion;

    /**
     * 季节
     * - spring: 春
     * - summer: 夏
     * - autumn: 秋
     * - winter: 冬
     */
    @Column(length = 20)
    private String season;

    /**
     * 风格心情
     * - elegant: 优雅
     * - casual: 休闲
     * - cool: 酷帅
     * - sweet: 甜美
     * - minimal: 极简
     */
    @Column(name = "mood", length = 50)
    private String mood;

    /**
     * 穿搭单品ID列表
     */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "item_ids", columnDefinition = "uuid[]")
    private List<UUID> itemIds;

    /**
     * 用户满意度评分（1-5）
     */
    @Column(name = "satisfaction")
    private Integer satisfaction;

    /**
     * 穿搭描述（可选，用户自述）
     */
    @Column(name = "description", columnDefinition = "text")
    private String description;

    /**
     * 穿搭图片URL（可选）
     */
    @Column(name = "image_url")
    private String imageUrl;

    /**
     * 是否为推荐穿搭（true=系统推荐，false=用户自搭）
     */
    @Column(name = "is_recommended")
    @Builder.Default
    private Boolean isRecommended = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
