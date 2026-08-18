package com.aura.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 用户行为记录
 * 
 * 记录用户与衣物的交互行为，用于CF嵌入训练
 */
@Entity
@Table(name = "user_behavior", indexes = {
    @Index(name = "idx_behavior_user", columnList = "user_id"),
    @Index(name = "idx_behavior_item", columnList = "item_id"),
    @Index(name = "idx_behavior_type", columnList = "type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBehavior {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 衣物ID
     */
    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    /**
     * 行为类型
     * - VIEW: 浏览
     * - FAVORITE: 收藏
     * - WEAR: 穿搭记录
     * - RATE: 评分
     * - CLICK: 点击
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BehaviorType type;

    /**
     * 评分（1-5，仅RATE类型）
     */
    @Column(name = "rating")
    private Integer rating;

    /**
     * 场合（仅WEAR类型）
     */
    @Column(name = "occasion", length = 50)
    private String occasion;

    /**
     * 季节（仅WEAR类型）
     */
    @Column(name = "season", length = 20)
    private String season;

    /**
     * 停留时长（秒，仅VIEW类型）
     */
    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 行为类型枚举
     */
    public enum BehaviorType {
        VIEW,       // 浏览
        FAVORITE,   // 收藏
        WEAR,       // 穿搭记录
        RATE,       // 评分
        CLICK,      // 点击
        SHARE       // 分享
    }
}
