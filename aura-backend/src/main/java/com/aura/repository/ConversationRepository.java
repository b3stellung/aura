package com.aura.repository;

import com.aura.model.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * 对话会话 Repository
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    /**
     * 查找用户的会话列表（按更新时间倒序）
     */
    Page<Conversation> findByUserIdOrderByUpdatedAtDesc(String userId, Pageable pageable);

    /**
     * 查找用户的活跃会话
     */
    List<Conversation> findByUserIdAndStatusOrderByUpdatedAtDesc(String userId, String status);

    /**
     * 统计用户的会话数量
     */
    long countByUserId(String userId);

    /**
     * 查找用户的最近会话
     */
    @Query("SELECT c FROM Conversation c WHERE c.userId = :userId ORDER BY c.updatedAt DESC")
    List<Conversation> findRecentByUserId(@Param("userId") String userId, Pageable pageable);

    /**
     * 检查会话是否属于指定用户
     */
    boolean existsByIdAndUserId(UUID id, String userId);
}
