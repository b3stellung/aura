package com.aura.repository;

import com.aura.model.entity.UserBehavior;
import com.aura.model.entity.UserBehavior.BehaviorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 用户行为 Repository
 */
@Repository
public interface UserBehaviorRepository extends JpaRepository<UserBehavior, UUID> {

    /**
     * 查找用户的所有行为
     */
    List<UserBehavior> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * 查找用户对特定物品的行为
     */
    List<UserBehavior> findByUserIdAndItemId(UUID userId, UUID itemId);

    /**
     * 查找用户的特定类型行为
     */
    List<UserBehavior> findByUserIdAndType(UUID userId, BehaviorType type);

    /**
     * 查找用户收藏的物品
     */
    @Query("SELECT b FROM UserBehavior b WHERE b.user.id = :userId AND b.type = 'FAVORITE' ORDER BY b.createdAt DESC")
    List<UserBehavior> findUserFavorites(@Param("userId") UUID userId);

    /**
     * 查找用户穿搭记录
     */
    @Query("SELECT b FROM UserBehavior b WHERE b.user.id = :userId AND b.type = 'WEAR' ORDER BY b.createdAt DESC")
    List<UserBehavior> findUserWearRecords(@Param("userId") UUID userId);

    /**
     * 统计用户行为数量
     */
    long countByUserId(UUID userId);

    /**
     * 统计物品被交互次数
     */
    long countByItemId(UUID itemId);

    /**
     * 查找最近活跃用户
     */
    @Query("SELECT DISTINCT b.user.id FROM UserBehavior b WHERE b.createdAt >= :since")
    List<UUID> findActiveUserIdsSince(@Param("since") LocalDateTime since);

    /**
     * 查找物品的平均评分
     */
    @Query("SELECT AVG(b.rating) FROM UserBehavior b WHERE b.itemId = :itemId AND b.type = 'RATE' AND b.rating IS NOT NULL")
    Double getAverageRating(@Param("itemId") UUID itemId);

    /**
     * 查找用户-物品交互矩阵数据
     */
    @Query("SELECT b.user.id, b.itemId, COUNT(b) as interactionCount " +
           "FROM UserBehavior b " +
           "WHERE b.createdAt >= :since " +
           "GROUP BY b.user.id, b.itemId")
    List<Object[]> getUserItemInteractionMatrix(@Param("since") LocalDateTime since);
}
