package com.aura.service.impl;

import com.aura.exception.BusinessException;
import com.aura.model.dto.*;
import com.aura.model.entity.User;
import com.aura.repository.UserRepository;
import com.aura.security.JwtUtil;
import com.aura.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已被注册");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }

        // 创建用户
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .subscriptionTier("free")
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getUsername());

        // 生成 Token
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername(), user.getEmail());

        return buildAuthResponse(accessToken, refreshToken, user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // 使用 Spring Security 认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 从数据库获取完整用户信息
        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .orElseGet(() -> userRepository.findByEmail(request.getUsernameOrEmail())
                        .orElseThrow(() -> new BusinessException("用户不存在")));

        // 生成 Token
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername(), user.getEmail());

        log.info("User logged in successfully: {}", user.getUsername());
        return buildAuthResponse(accessToken, refreshToken, user);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // 验证 Refresh Token
        if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            throw new BusinessException("无效的 Refresh Token");
        }

        // 检查是否在黑名单中
        if (isTokenBlacklisted(refreshToken)) {
            throw new BusinessException("Refresh Token 已失效，请重新登录");
        }

        // 获取用户信息
        var userId = jwtUtil.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 将旧 Refresh Token 加入黑名单
        blacklistToken(refreshToken);

        // 生成新的 Token 对
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getEmail());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername(), user.getEmail());

        log.info("Token refreshed for user: {}", user.getUsername());
        return buildAuthResponse(newAccessToken, newRefreshToken, user);
    }

    @Override
    public void logout(String accessToken) {
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }

        if (accessToken != null && jwtUtil.validateToken(accessToken)) {
            // 将 Access Token 加入黑名单
            blacklistToken(accessToken);
            log.info("User logged out, token blacklisted");
        }

        SecurityContextHolder.clearContext();
    }

    /**
     * 将 Token 加入黑名单 (存入 Redis，设置过期时间为 Token 剩余有效期)
     */
    private void blacklistToken(String token) {
        try {
            var expiration = jwtUtil.getExpirationFromToken(token);
            long ttl = expiration.getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                redisTemplate.opsForValue().set(
                        TOKEN_BLACKLIST_PREFIX + token,
                        "blacklisted",
                        ttl,
                        TimeUnit.MILLISECONDS
                );
            }
        } catch (Exception e) {
            log.error("Failed to blacklist token: {}", e.getMessage());
        }
    }

    /**
     * 检查 Token 是否在黑名单中
     */
    private boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token));
    }

    private AuthResponse buildAuthResponse(String accessToken, String refreshToken, User user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationFromToken(accessToken).getTime() - System.currentTimeMillis())
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId().toString())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .avatarUrl(user.getAvatarUrl())
                        .subscriptionTier(user.getSubscriptionTier())
                        .build())
                .build();
    }
}
