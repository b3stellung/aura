package com.aura.model.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新用户资料请求
 */
@Data
public class UpdateProfileRequest {

    @Size(max = 50, message = "用户名长度不能超过 50")
    private String username;

    @Size(max = 500, message = "头像 URL 长度不能超过 500")
    private String avatarUrl;
}
