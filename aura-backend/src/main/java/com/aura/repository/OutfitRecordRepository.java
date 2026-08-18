package com.aura.repository;

import com.aura.model.entity.OutfitRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * 穿搭记录 Repository
 */
@Repository
public interface OutfitRecordRepository extends JpaRepository<OutfitRecord, UUID> {

    /**
     * 查找用户的所有穿搭记录
     */
    List<OutfitRecord> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * 按场合查找穿搭记录
     */
    List<OutfitRecord> findByUserIdAndOccasion(UUID userId, String occasion);

    /**
     * 按季节查找穿搭记录
     */
    List<OutfitRecord> findByUserIdAndSeason(UUID userId, String season);

    /**
     * 按风格查找穿搭记录
     */
    List<OutfitRecord> findByUserIdAndMood(UUID userId, String mood);

    /**
     * 查找用户满意穿搭（评分>=4）
     */
    @Query("SELECT o FROM OutfitRecord o WHERE o.user.id = :userId AND o.satisfaction >= 4 ORDER BY o.satisfaction DESC, o.createdAt DESC")
    List<OutfitRecord> findUserSatisfiedOutfits(@Param("userId") UUID userId);

    /**
     * 查找系统推荐穿搭
     */
    @Query("SELECT o FROM OutfitRecord o WHERE o.user.id = :userId AND o.isRecommended = true ORDER BY o.createdAt DESC")
    List<OutfitRecord> findRecommendedOutfits(@Param("userId") UUID userId);

    /**
     * 统计用户穿搭数量
     */
    long countByUserId(UUID userId);

    /**
     * 按场合统计穿搭数量
     */
    @Query("SELECT o.occasion, COUNT(o) FROM OutfitRecord o WHERE o.user.id = :userId GROUP BY o.occasion")
    List<Object[]> countByOccasion(@Param("userId") UUID userId);

    /**
     * 查找相似穿搭（相同场合+季节+风格）
     */
    @Query("SELECT o FROM OutfitRecord o WHERE o.user.id = :userId AND o.occasion = :occasion AND o.season = :season AND o.mood = :mood ORDER BY o.satisfaction DESC")
    List<OutfitRecord> findSimilarOutfits(@Param("userId") UUID userId, 
                                          @Param("occasion") String occasion,
                                          @Param("season") String season,
                                          @Param("mood") String mood);
}
