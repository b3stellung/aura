package com.aura.exception;

import org.springframework.http.HttpStatus;

/**
 * 认证失败异常
 */
public class AuthenticationException extends BusinessException {

    public AuthenticationException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
