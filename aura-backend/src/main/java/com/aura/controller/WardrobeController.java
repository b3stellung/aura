package com.aura.controller;

import com.aura.model.dto.WardrobeItemRequest;
import com.aura.model.dto.WardrobeItemResponse;
import com.aura.service.WardrobeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 衣橱管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/wardrobe")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WardrobeController {

    private final WardrobeService wardrobeService;

    /**
     * 添加衣物到衣橱
     */
    @PostMapping("/items")
    public ResponseEntity<WardrobeItemResponse> addItem(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody WardrobeItemRequest request) {
        
        log.info("添加衣物: userId={}", userId);
        WardrobeItemResponse response = wardrobeService.addItem(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 获取用户衣橱列表
     */
    @GetMapping("/items")
    public ResponseEntity<List<WardrobeItemResponse>> getUserWardrobe(
            @RequestHeader("X-User-Id") String userId) {
        
        log.info("获取衣橱列表: userId={}", userId);
        List<WardrobeItemResponse> items = wardrobeService.getUserWardrobe(userId);
        return ResponseEntity.ok(items);
    }

    /**
     * 获取单个衣物详情
     */
    @GetMapping("/items/{itemId}")
    public ResponseEntity<WardrobeItemResponse> getItem(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID itemId) {
        
        log.info("获取衣物详情: userId={}, itemId={}", userId, itemId);
        WardrobeItemResponse response = wardrobeService.getItem(userId, itemId);
        return ResponseEntity.ok(response);
    }

    /**
     * 更新衣物信息
     */
    @PutMapping("/items/{itemId}")
    public ResponseEntity<WardrobeItemResponse> updateItem(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID itemId,
            @Valid @RequestBody WardrobeItemRequest request) {
        
        log.info("更新衣物: userId={}, itemId={}", userId, itemId);
        WardrobeItemResponse response = wardrobeService.updateItem(userId, itemId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 删除衣物
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID itemId) {
        
        log.info("删除衣物: userId={}, itemId={}", userId, itemId);
        wardrobeService.deleteItem(userId, itemId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 按分类筛选衣物
     */
    @GetMapping("/items/category/{category}")
    public ResponseEntity<List<WardrobeItemResponse>> getItemsByCategory(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String category) {
        
        log.info("按分类筛选: userId={}, category={}", userId, category);
        List<WardrobeItemResponse> items = wardrobeService.getItemsByCategory(userId, category);
        return ResponseEntity.ok(items);
    }

    /**
     * 按季节筛选衣物
     */
    @GetMapping("/items/season/{season}")
    public ResponseEntity<List<WardrobeItemResponse>> getItemsBySeason(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String season) {
        
        log.info("按季节筛选: userId={}, season={}", userId, season);
        List<WardrobeItemResponse> items = wardrobeService.getItemsBySeason(userId, season);
        return ResponseEntity.ok(items);
    }

    /**
     * 按场合筛选衣物
     */
    @GetMapping("/items/occasion/{occasion}")
    public ResponseEntity<List<WardrobeItemResponse>> getItemsByOccasion(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String occasion) {
        
        log.info("按场合筛选: userId={}, occasion={}", userId, occasion);
        List<WardrobeItemResponse> items = wardrobeService.getItemsByOccasion(userId, occasion);
        return ResponseEntity.ok(items);
    }
}
