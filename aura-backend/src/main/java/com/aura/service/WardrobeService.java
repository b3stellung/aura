package com.aura.service;

import com.aura.model.dto.WardrobeItemRequest;
import com.aura.model.dto.WardrobeItemResponse;
import java.util.List;
import java.util.UUID;

/**
 * 衣橱服务接口
 */
public interface WardrobeService {
    
    /**
     * 添加衣物到衣橱
     */
    WardrobeItemResponse addItem(String userId, WardrobeItemRequest request);
    
    /**
     * 获取用户衣橱列表
     */
    List<WardrobeItemResponse> getUserWardrobe(String userId);
    
    /**
     * 获取单个衣物详情
     */
    WardrobeItemResponse getItem(String userId, UUID itemId);
    
    /**
     * 更新衣物信息
     */
    WardrobeItemResponse updateItem(String userId, UUID itemId, WardrobeItemRequest request);
    
    /**
     * 删除衣物
     */
    void deleteItem(String userId, UUID itemId);
    
    /**
     * 根据分类筛选衣物
     */
    List<WardrobeItemResponse> getItemsByCategory(String userId, String category);
    
    /**
     * 根据季节筛选衣物
     */
    List<WardrobeItemResponse> getItemsBySeason(String userId, String season);
    
    /**
     * 根据场合筛选衣物
     */
    List<WardrobeItemResponse> getItemsByOccasion(String userId, String occasion);
}