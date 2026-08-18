package com.aura.service.impl;

import com.aura.exception.BusinessException;
import com.aura.exception.ResourceNotFoundException;
import com.aura.model.dto.WardrobeItemRequest;
import com.aura.model.dto.WardrobeItemResponse;
import com.aura.model.entity.User;
import com.aura.model.entity.WardrobeItem;
import com.aura.repository.UserRepository;
import com.aura.repository.WardrobeItemRepository;
import com.aura.service.WardrobeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 衣橱服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WardrobeServiceImpl implements WardrobeService {

    private final WardrobeItemRepository wardrobeItemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public WardrobeItemResponse addItem(String userId, WardrobeItemRequest request) {
        User user = userRepository.findByUsername(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + userId));

        WardrobeItem item = WardrobeItem.builder()
                .user(user)
                .imageUrl(request.getImageUrl())
                .category(request.getCategory())
                .color(request.getColor())
                .material(request.getMaterial())
                .styleTags(request.getStyleTags())
                .seasonTags(request.getSeasonTags())
                .occasionTags(request.getOccasionTags())
                .build();

        item = wardrobeItemRepository.save(item);
        log.info("添加衣物到衣橱: userId={}, itemId={}", userId, item.getId());

        return mapToResponse(item);
    }

    @Override
    public List<WardrobeItemResponse> getUserWardrobe(String userId) {
        User user = userRepository.findByUsername(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + userId));

        List<WardrobeItem> items = wardrobeItemRepository.findByUserId(user.getId());
        return items.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public WardrobeItemResponse getItem(String userId, UUID itemId) {
        User user = userRepository.findByUsername(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + userId));

        WardrobeItem item = wardrobeItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("衣物不存在: " + itemId));

        // 验证衣物属于该用户
        if (!item.getUser().getId().equals(user.getId())) {
            throw new BusinessException("无权访问该衣物");
        }

        return mapToResponse(item);
    }

    @Override
    @Transactional
    public WardrobeItemResponse updateItem(String userId, UUID itemId, WardrobeItemRequest request) {
        User user = userRepository.findByUsername(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + userId));

        WardrobeItem item = wardrobeItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("衣物不存在: " + itemId));

        // 验证衣物属于该用户
        if (!item.getUser().getId().equals(user.getId())) {
            throw new BusinessException("无权修改该衣物");
        }

        // 更新字段
        item.setImageUrl(request.getImageUrl());
        item.setCategory(request.getCategory());
        item.setColor(request.getColor());
        item.setMaterial(request.getMaterial());
        item.setStyleTags(request.getStyleTags());
        item.setSeasonTags(request.getSeasonTags());
        item.setOccasionTags(request.getOccasionTags());

        item = wardrobeItemRepository.save(item);
        log.info("更新衣物信息: userId={}, itemId={}", userId, itemId);

        return mapToResponse(item);
    }

    @Override
    @Transactional
    public void deleteItem(String userId, UUID itemId) {
        User user = userRepository.findByUsername(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + userId));

        WardrobeItem item = wardrobeItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("衣物不存在: " + itemId));

        // 验证衣物属于该用户
        if (!item.getUser().getId().equals(user.getId())) {
            throw new BusinessException("无权删除该衣物");
        }

        wardrobeItemRepository.delete(item);
        log.info("删除衣物: userId={}, itemId={}", userId, itemId);
    }

    @Override
    public List<WardrobeItemResponse> getItemsByCategory(String userId, String category) {
        User user = userRepository.findByUsername(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + userId));

        List<WardrobeItem> items = wardrobeItemRepository.findByUserIdAndCategory(user.getId(), category);
        return items.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<WardrobeItemResponse> getItemsBySeason(String userId, String season) {
        User user = userRepository.findByUsername(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + userId));

        List<WardrobeItem> items = wardrobeItemRepository.findByUserIdAndSeason(user.getId(), season);
        return items.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<WardrobeItemResponse> getItemsByOccasion(String userId, String occasion) {
        User user = userRepository.findByUsername(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + userId));

        List<WardrobeItem> items = wardrobeItemRepository.findByUserIdAndOccasion(user.getId(), occasion);
        return items.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 实体转DTO
     */
    private WardrobeItemResponse mapToResponse(WardrobeItem item) {
        return WardrobeItemResponse.builder()
                .id(item.getId())
                .imageUrl(item.getImageUrl())
                .category(item.getCategory())
                .color(item.getColor())
                .material(item.getMaterial())
                .styleTags(item.getStyleTags())
                .seasonTags(item.getSeasonTags())
                .occasionTags(item.getOccasionTags())
                .wearCount(item.getWearCount())
                .lastWornAt(item.getLastWornAt())
                .attributes(item.getAttributes())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}