package com.aura.service;

import com.aura.model.dto.*;

import java.util.UUID;

/**
 * 用户管理服务接口
 */
public interface UserService {

    /**
     * 获取用户资料
     */
    UserProfileResponse getUserProfile(UUID userId);

    /**
     * 更新用户资料
     */
    UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request);

    /**
     * 修改密码
     */
    void changePassword(UUID userId, ChangePasswordRequest request);

    /**
     * 注销账户
     */
    void deleteAccount(UUID userId, String password);
}
