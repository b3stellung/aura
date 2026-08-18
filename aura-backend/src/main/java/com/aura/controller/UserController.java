package com.aura.controller;

import com.aura.model.dto.*;
import com.aura.model.entity.User;
import com.aura.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 用户管理控制器 - 处理用户资料查询和修改
 * 所有接口都需要认证
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取当前用户资料
     * GET /api/v1/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser(
            @AuthenticationPrincipal User currentUser) {
        UserProfileResponse profile = userService.getUserProfile(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("获取成功", profile));
    }

    /**
     * 更新当前用户资料（用户名、头像）
     * PUT /api/v1/users/me
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse profile = userService.updateProfile(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("更新成功", profile));
    }

    /**
     * 修改密码
     * PUT /api/v1/users/me/password
     */
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("密码修改成功"));
    }

    /**
     * 注销账户
     * DELETE /api/v1/users/me
     */
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @AuthenticationPrincipal User currentUser,
            @RequestParam String password) {
        userService.deleteAccount(currentUser.getId(), password);
        return ResponseEntity.ok(ApiResponse.success("账户已注销"));
    }
}
