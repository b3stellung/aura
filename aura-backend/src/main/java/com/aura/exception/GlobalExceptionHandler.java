package com.aura.exception;

import com.aura.model.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 
 * 确保所有接口返回友好的中文错误信息，而不是通用的"服务器内部错误"
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return ResponseEntity.status(e.getStatus())
                .body(ApiResponse.error(e.getMessage()));
    }

    /**
     * 参数校验异常（@Valid @NotBlank等注解触发）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("参数校验失败: {}", errors);
        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .success(false)
                .message("参数验证失败")
                .data(errors)
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 缺少必需的请求头（如X-User-Id）
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingHeader(MissingRequestHeaderException e) {
        String headerName = e.getHeaderName();
        String message;
        if ("X-User-Id".equals(headerName)) {
            message = "缺少必需的请求头 X-User-Id，请在请求头中提供用户标识";
        } else {
            message = "缺少必需的请求头: " + headerName;
        }
        log.warn("缺少请求头: {}", headerName);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(message));
    }

    /**
     * 缺少必需的请求参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(MissingServletRequestParameterException e) {
        String message = "缺少必需的请求参数: " + e.getParameterName();
        log.warn("缺少请求参数: {}", e.getParameterName());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(message));
    }

    /**
     * 请求体格式错误（JSON解析失败等）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("请求体格式错误，请检查JSON格式是否正确"));
    }

    /**
     * HTTP方法不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        String message = "不支持的请求方法: " + e.getMethod() + "，请使用 " + String.join(", ", e.getSupportedMethods());
        log.warn("不支持的HTTP方法: {}", e.getMethod());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error(message));
    }

    /**
     * 参数类型不匹配（如UUID格式错误）
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String message;
        Class<?> requiredType = e.getRequiredType();
        if (requiredType != null && requiredType.getSimpleName().equals("UUID")) {
            message = "参数 " + e.getName() + " 格式错误，请提供合法的UUID格式（如: 550e8400-e29b-41d4-a716-446655440000）";
        } else {
            message = "参数 " + e.getName() + " 类型错误，期望类型: " + (requiredType != null ? requiredType.getSimpleName() : "未知");
        }
        log.warn("参数类型不匹配: {} - {}", e.getName(), e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(message));
    }

    /**
     * IllegalArgumentException（包括UUID解析失败等）
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        String message = e.getMessage();
        // UUID格式错误的特殊处理
        if (message != null && message.contains("Invalid UUID string")) {
            message = "itemId格式错误，请提供合法的UUID格式（如: 550e8400-e29b-41d4-a716-446655440000）";
        }
        log.warn("非法参数: {}", message);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(message != null ? message : "参数格式不正确"));
    }

    /**
     * 404 资源未找到
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("请求的资源不存在"));
    }

    /**
     * 认证异常
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException e) {
        log.warn("认证失败: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("用户名或密码错误"));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUsernameNotFound(UsernameNotFoundException e) {
        log.warn("用户不存在: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("用户名或密码错误"));
    }

    /**
     * 通用异常兜底 - 返回更友好的错误信息
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception e) {
        // 记录完整异常堆栈用于调试
        log.error("未预期的异常: [{}] {}", e.getClass().getSimpleName(), e.getMessage(), e);
        
        // 根据异常类型提供更有用的提示
        String message;
        if (e instanceof NullPointerException) {
            message = "服务器处理请求时遇到空数据，请检查请求参数是否完整";
        } else if (e instanceof ClassCastException) {
            message = "服务器处理请求时遇到数据类型错误，请检查请求参数格式";
        } else {
            message = "服务器处理请求时发生错误: " + e.getClass().getSimpleName();
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
    }
}
