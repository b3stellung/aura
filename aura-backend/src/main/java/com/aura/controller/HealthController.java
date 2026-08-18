package com.aura.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 */
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Aura Backend");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("version", "1.0.0-SNAPSHOT");
        return response;
    }
    
    @GetMapping("/test")
    public String test() {
        return "Aura Backend is running!";
    }
}
