package com.aura.controller;

import com.aura.exception.BusinessException;
import com.aura.model.entity.OutfitRecord;
import com.aura.model.entity.User;
import com.aura.model.entity.UserBehavior;
import com.aura.model.entity.UserBehavior.BehaviorType;
import com.aura.repository.OutfitRecordRepository;
import com.aura.repository.UserBehaviorRepository;
import com.aura.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 用户行为采集接口
 * 
 * 用于收集用户与衣物的交互行为，支持CF嵌入训练
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/behavior")
@CrossOrigin(origins = "*")
public class BehaviorController {

    private final UserBehaviorRepository behaviorRepository;
    private final OutfitRecordRepository outfitRecordRepository;
    private final UserRepository userRepository;

    public BehaviorController(UserBehaviorRepository behaviorRepository,
                              OutfitRecordRepository outfitRecordRepository,
                              UserRepository userRepository) {
        this.behaviorRepository = behaviorRepository;
        this.outfitRecordRepository = outfitRecordRepository;
        this.userRepository = userRepository;
    }

    // ==================== 参数验证辅助方法 ====================

    /**
     * 验证X-User-Id header
     */
    private void validateUserId(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new BusinessException("X-User-Id不能为空，请在请求头中提供用户标识");
        }
        if (userId.length() > 100) {
            throw new BusinessException("X-User-Id长度不能超过100个字符");
        }
    }

    /**
     * 验证并解析UUID
     */
    private UUID parseAndValidateUUID(String uuidStr, String fieldName) {
        if (!StringUtils.hasText(uuidStr)) {
            throw new BusinessException(fieldName + "不能为空");
        }
        try {
            return UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(fieldName + "格式错误: '" + uuidStr + "'，请提供合法的UUID格式（如: 550e8400-e29b-41d4-a716-446655440000）");
        }
    }

    /**
     * 从请求Map中安全获取String字段
     */
    private String getStringField(Map<String, Object> request, String fieldName, boolean required) {
        Object value = request.get(fieldName);
        if (value == null) {
            if (required) {
                throw new BusinessException(fieldName + "不能为空");
            }
            return null;
        }
        String strValue = value.toString().trim();
        if (required && !StringUtils.hasText(strValue)) {
            throw new BusinessException(fieldName + "不能为空");
        }
        return strValue;
    }

    /**
     * 从请求Map中安全获取Integer字段，带范围验证
     */
    private Integer getIntField(Map<String, Object> request, String fieldName, 
                                boolean required, Integer min, Integer max) {
        Object value = request.get(fieldName);
        if (value == null) {
            if (required) {
                throw new BusinessException(fieldName + "不能为空");
            }
            return null;
        }
        int intValue;
        try {
            intValue = ((Number) value).intValue();
        } catch (ClassCastException | NullPointerException e) {
            throw new BusinessException(fieldName + "必须是数字类型");
        }
        if (min != null && intValue < min) {
            throw new BusinessException(fieldName + "不能小于" + min);
        }
        if (max != null && intValue > max) {
            throw new BusinessException(fieldName + "不能大于" + max);
        }
        return intValue;
    }

    /**
     * 验证字符串长度
     */
    private void validateStringLength(String value, String fieldName, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw new BusinessException(fieldName + "长度不能超过" + maxLength + "个字符");
        }
    }

    // ==================== 接口实现 ====================

    /**
     * 记录浏览行为
     */
    @PostMapping("/view")
    public ResponseEntity<Map<String, Object>> recordView(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Map<String, Object> request) {
        
        validateUserId(userId);
        
        log.info("记录浏览行为: userId={}, itemId={}", userId, request.get("itemId"));
        
        try {
            User user = getOrCreateUser(userId);
            UUID itemId = parseAndValidateUUID(
                    getStringField(request, "itemId", true), "itemId");
            Integer duration = getIntField(request, "duration", false, 0, 86400);
            
            UserBehavior behavior = UserBehavior.builder()
                    .user(user)
                    .itemId(itemId)
                    .type(BehaviorType.VIEW)
                    .durationSeconds(duration)
                    .build();
            
            behaviorRepository.save(behavior);
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "浏览行为已记录"
            ));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("记录浏览行为失败", e);
            throw new BusinessException("记录浏览行为失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 记录收藏行为
     */
    @PostMapping("/favorite")
    public ResponseEntity<Map<String, Object>> recordFavorite(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Map<String, Object> request) {
        
        validateUserId(userId);
        
        log.info("记录收藏行为: userId={}, itemId={}", userId, request.get("itemId"));
        
        try {
            User user = getOrCreateUser(userId);
            UUID itemId = parseAndValidateUUID(
                    getStringField(request, "itemId", true), "itemId");
            
            UserBehavior behavior = UserBehavior.builder()
                    .user(user)
                    .itemId(itemId)
                    .type(BehaviorType.FAVORITE)
                    .build();
            
            behaviorRepository.save(behavior);
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "收藏行为已记录"
            ));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("记录收藏行为失败", e);
            throw new BusinessException("记录收藏行为失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 记录穿搭行为
     */
    @PostMapping("/wear")
    public ResponseEntity<Map<String, Object>> recordWear(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Map<String, Object> request) {
        
        validateUserId(userId);
        
        log.info("记录穿搭行为: userId={}, occasion={}", userId, request.get("occasion"));
        
        try {
            User user = getOrCreateUser(userId);
            UUID itemId = parseAndValidateUUID(
                    getStringField(request, "itemId", true), "itemId");
            String occasion = getStringField(request, "occasion", false);
            String season = getStringField(request, "season", false);
            
            validateStringLength(occasion, "occasion", 50);
            validateStringLength(season, "season", 20);
            
            UserBehavior behavior = UserBehavior.builder()
                    .user(user)
                    .itemId(itemId)
                    .type(BehaviorType.WEAR)
                    .occasion(occasion)
                    .season(season)
                    .build();
            
            behaviorRepository.save(behavior);
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "穿搭行为已记录"
            ));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("记录穿搭行为失败", e);
            throw new BusinessException("记录穿搭行为失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 记录评分行为
     */
    @PostMapping("/rate")
    public ResponseEntity<Map<String, Object>> recordRate(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Map<String, Object> request) {
        
        validateUserId(userId);
        
        log.info("记录评分行为: userId={}, itemId={}, rating={}", 
                userId, request.get("itemId"), request.get("rating"));
        
        try {
            User user = getOrCreateUser(userId);
            UUID itemId = parseAndValidateUUID(
                    getStringField(request, "itemId", true), "itemId");
            Integer rating = getIntField(request, "rating", true, 1, 5);
            
            UserBehavior behavior = UserBehavior.builder()
                    .user(user)
                    .itemId(itemId)
                    .type(BehaviorType.RATE)
                    .rating(rating)
                    .build();
            
            behaviorRepository.save(behavior);
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "评分行为已记录"
            ));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("记录评分行为失败", e);
            throw new BusinessException("记录评分行为失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 保存穿搭组合记录
     */
    @PostMapping("/outfit")
    public ResponseEntity<Map<String, Object>> saveOutfitRecord(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Map<String, Object> request) {
        
        validateUserId(userId);
        
        log.info("保存穿搭记录: userId={}, occasion={}", userId, request.get("occasion"));
        
        try {
            User user = getOrCreateUser(userId);
            
            // 验证itemIds
            Object itemIdsObj = request.get("itemIds");
            if (itemIdsObj == null) {
                throw new BusinessException("itemIds不能为空");
            }
            @SuppressWarnings("unchecked")
            List<String> itemIdStrings = (List<String>) itemIdsObj;
            if (itemIdStrings.isEmpty()) {
                throw new BusinessException("itemIds列表不能为空");
            }
            if (itemIdStrings.size() > 20) {
                throw new BusinessException("一个穿搭组合最多包含20件单品");
            }
            
            List<UUID> itemIds = itemIdStrings.stream()
                    .map(id -> parseAndValidateUUID(id, "itemId"))
                    .toList();
            
            // 验证并获取字段
            String occasion = getStringField(request, "occasion", true);
            validateStringLength(occasion, "occasion", 50);
            
            String season = getStringField(request, "season", false);
            validateStringLength(season, "season", 20);
            
            String mood = getStringField(request, "mood", false);
            validateStringLength(mood, "mood", 50);
            
            String description = getStringField(request, "description", false);
            validateStringLength(description, "description", 2000);
            
            String imageUrl = getStringField(request, "imageUrl", false);
            validateStringLength(imageUrl, "imageUrl", 500);
            
            Integer satisfaction = getIntField(request, "satisfaction", false, 1, 5);
            
            OutfitRecord record = OutfitRecord.builder()
                    .user(user)
                    .occasion(occasion)
                    .season(season)
                    .mood(mood)
                    .itemIds(itemIds)
                    .satisfaction(satisfaction)
                    .description(description)
                    .imageUrl(imageUrl)
                    .isRecommended(request.get("isRecommended") != null ? 
                            (Boolean) request.get("isRecommended") : false)
                    .build();
            
            outfitRecordRepository.save(record);
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "穿搭记录已保存",
                    "recordId", record.getId().toString()
            ));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("保存穿搭记录失败", e);
            throw new BusinessException("保存穿搭记录失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取用户行为历史
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getUserHistory(
            @RequestHeader("X-User-Id") String userId) {
        
        validateUserId(userId);
        
        log.info("获取用户行为历史: userId={}", userId);
        
        try {
            User user = getOrCreateUser(userId);
            
            List<UserBehavior> behaviors = behaviorRepository
                    .findByUserIdOrderByCreatedAtDesc(user.getId());
            
            List<Map<String, Object>> behaviorList = behaviors.stream()
                    .map(b -> Map.<String, Object>of(
                            "id", b.getId().toString(),
                            "itemId", b.getItemId().toString(),
                            "type", b.getType().name(),
                            "rating", b.getRating() != null ? b.getRating() : 0,
                            "occasion", b.getOccasion() != null ? b.getOccasion() : "",
                            "durationSeconds", b.getDurationSeconds() != null ? b.getDurationSeconds() : 0,
                            "createdAt", b.getCreatedAt() != null ? b.getCreatedAt().toString() : ""
                    ))
                    .toList();
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "userId", userId,
                    "behaviorCount", behaviorList.size(),
                    "behaviors", behaviorList
            ));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取用户行为历史失败", e);
            throw new BusinessException("获取用户行为历史失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取用户穿搭记录
     */
    @GetMapping("/outfits")
    public ResponseEntity<Map<String, Object>> getUserOutfits(
            @RequestHeader("X-User-Id") String userId) {
        
        validateUserId(userId);
        
        log.info("获取用户穿搭记录: userId={}", userId);
        
        try {
            User user = getOrCreateUser(userId);
            
            List<OutfitRecord> outfits = outfitRecordRepository
                    .findByUserIdOrderByCreatedAtDesc(user.getId());
            
            List<Map<String, Object>> outfitList = outfits.stream()
                    .map(o -> Map.<String, Object>of(
                            "id", o.getId().toString(),
                            "occasion", o.getOccasion() != null ? o.getOccasion() : "",
                            "season", o.getSeason() != null ? o.getSeason() : "",
                            "mood", o.getMood() != null ? o.getMood() : "",
                            "satisfaction", o.getSatisfaction() != null ? o.getSatisfaction() : 0,
                            "itemCount", o.getItemIds() != null ? o.getItemIds().size() : 0,
                            "description", o.getDescription() != null ? o.getDescription() : "",
                            "createdAt", o.getCreatedAt() != null ? o.getCreatedAt().toString() : ""
                    ))
                    .toList();
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "userId", userId,
                    "outfitCount", outfitList.size(),
                    "outfits", outfitList
            ));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取用户穿搭记录失败", e);
            throw new BusinessException("获取用户穿搭记录失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取或创建用户
     */
    private User getOrCreateUser(String userId) {
        return userRepository.findByUsername(userId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .username(userId.length() > 100 ? userId.substring(0, 100) : userId)
                            .email(userId + "@aura.temp")
                            .passwordHash("temp-hash-" + userId)
                            .build();
                    return userRepository.save(newUser);
                });
    }
}
