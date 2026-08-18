package com.aura.repository;

import com.aura.model.entity.AgentSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Agent 会话 Repository
 */
@Repository
public interface AgentSessionRepository extends JpaRepository<AgentSession, UUID> {

    /**
     * 查找用户的会话列表（按时间倒序）
     */
    Page<AgentSession> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * 按状态查找会话
     */
    List<AgentSession> findByStatus(String status);

    /**
     * 查找用户的特定类型会话
     */
    List<AgentSession> findByUserIdAndSessionType(UUID userId, String sessionType);

    /**
     * 统计用户的会话数量
     */
    long countByUserId(UUID userId);

    /**
     * 统计用户的 Token 消耗
     */
    @Query("SELECT COALESCE(SUM(s.tokenConsumed), 0) FROM AgentSession s WHERE s.user.id = :userId")
    long sumTokenConsumedByUserId(@Param("userId") UUID userId);

    /**
     * 查找超时的会话
     */
    @Query("SELECT s FROM AgentSession s WHERE s.status = 'running' AND s.createdAt < :timeout")
    List<AgentSession> findTimeoutSessions(@Param("timeout") LocalDateTime timeout);

    /**
     * 统计每日会话数量
     */
    @Query("SELECT DATE(s.createdAt), COUNT(s) FROM AgentSession s WHERE s.createdAt >= :since GROUP BY DATE(s.createdAt)")
    List<Object[]> countDailySessions(@Param("since") LocalDateTime since);
}
