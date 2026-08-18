package com.aura.security;

import com.aura.model.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

/**
 * 安全上下文工具类 - 获取当前登录用户
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * 获取当前认证的 User 实体
     */
    public static Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof User user) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    /**
     * 获取当前用户 ID
     */
    public static Optional<UUID> getCurrentUserId() {
        return getCurrentUser().map(User::getId);
    }

    /**
     * 获取当前用户名
     */
    public static Optional<String> getCurrentUsername() {
        return getCurrentUser().map(User::getUsername);
    }

    /**
     * 获取当前用户（未登录时抛异常）
     */
    public static User requireCurrentUser() {
        return getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("No authenticated user found"));
    }

    /**
     * 获取当前用户 ID（未登录时抛异常）
     */
    public static UUID requireCurrentUserId() {
        return getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("No authenticated user found"));
    }
}
