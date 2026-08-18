package com.aura.repository;

import com.aura.model.entity.WardrobeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * 衣橱物品 Repository
 */
@Repository
public interface WardrobeItemRepository extends JpaRepository<WardrobeItem, UUID> {

    /**
     * 查找用户的所有衣物
     */
    List<WardrobeItem> findByUserId(UUID userId);

    /**
     * 按分类查找用户衣物
     */
    List<WardrobeItem> findByUserIdAndCategory(UUID userId, String category);

    /**
     * 按场合查找用户衣物
     */
    @Query(value = "SELECT w.* FROM wardrobe_items w WHERE w.user_id = :userId AND :occasion = ANY(w.occasion_tags)", nativeQuery = true)
    List<WardrobeItem> findByUserIdAndOccasion(@Param("userId") UUID userId, @Param("occasion") String occasion);

    /**
     * 按季节查找用户衣物
     */
    @Query(value = "SELECT w.* FROM wardrobe_items w WHERE w.user_id = :userId AND :season = ANY(w.season_tags)", nativeQuery = true)
    List<WardrobeItem> findByUserIdAndSeason(@Param("userId") UUID userId, @Param("season") String season);

    /**
     * 统计用户衣物数量
     */
    long countByUserId(UUID userId);

    /**
     * 按分类统计
     */
    @Query("SELECT w.category, COUNT(w) FROM WardrobeItem w WHERE w.user.id = :userId GROUP BY w.category")
    List<Object[]> countByCategory(@Param("userId") UUID userId);
}
