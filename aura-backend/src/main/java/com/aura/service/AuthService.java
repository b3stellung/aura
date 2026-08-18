package com.aura.service;

import com.aura.model.dto.*;

import java.util.UUID;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户注册
     */
    AuthResponse register(RegisterRequest request);

    /**
     * 用户登录
     */
    AuthResponse login(LoginRequest request);

    /**
     * 刷新 Token
     */
    AuthResponse refreshToken(RefreshTokenRequest request);

    /**
     * 登出（使 Token 失效）
     */
    void logout(String accessToken);
}
