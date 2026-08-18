package com.aura.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户资料响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private String id;
    private String username;
    private String email;
    private String avatarUrl;
    private String subscriptionTier;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
