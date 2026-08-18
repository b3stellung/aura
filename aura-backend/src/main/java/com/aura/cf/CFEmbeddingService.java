package com.aura.cf;

import com.aura.repository.UserBehaviorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * CF 嵌入服务
 * 
 * 使用矩阵分解（ALS）训练用户-物品嵌入向量
 * 实现协同过滤推荐
 */
@Slf4j
@Service
public class CFEmbeddingService {

    private final UserBehaviorRepository behaviorRepository;
    
    /**
     * 用户嵌入向量: userId -> float[embeddingDim]
     */
    private final Map<UUID, float[]> userEmbeddings = new ConcurrentHashMap<>();
    
    /**
     * 物品嵌入向量: itemId -> float[embeddingDim]
     */
    private final Map<UUID, float[]> itemEmbeddings = new ConcurrentHashMap<>();
    
    /**
     * 嵌入维度
     */
    private static final int EMBEDDING_DIM = 64;
    
    /**
     * ALS 迭代次数
     */
    private static final int ALS_ITERATIONS = 10;
    
    /**
     * 正则化参数
     */
    private static final double LAMBDA = 0.01;
    
    /**
     * 学习率
     */
    private static final double LEARNING_RATE = 0.01;

    public CFEmbeddingService(UserBehaviorRepository behaviorRepository) {
        this.behaviorRepository = behaviorRepository;
    }

    /**
     * 获取用户嵌入向量
     */
    public float[] getUserEmbedding(UUID userId) {
        return userEmbeddings.getOrDefault(userId, generateDefaultEmbedding(userId));
    }

    /**
     * 获取物品嵌入向量
     */
    public float[] getItemEmbedding(UUID itemId) {
        return itemEmbeddings.getOrDefault(itemId, generateDefaultEmbedding(itemId));
    }

    /**
     * 计算用户-物品相似度
     */
    public double calculateSimilarity(UUID userId, UUID itemId) {
        float[] userVec = getUserEmbedding(userId);
        float[] itemVec = getItemEmbedding(itemId);
        return cosineSimilarity(userVec, itemVec);
    }

    /**
     * 获取用户最相似的物品
     */
    public List<UUID> getSimilarItemsForUser(UUID userId, int topK) {
        float[] userVec = getUserEmbedding(userId);
        
        return itemEmbeddings.entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(
                        entry.getKey(), 
                        cosineSimilarity(userVec, entry.getValue())))
                .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
                .limit(topK)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 获取物品最相似的物品
     */
    public List<UUID> getSimilarItemsForItem(UUID itemId, int topK) {
        float[] itemVec = getItemEmbedding(itemId);
        
        return itemEmbeddings.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(itemId))
                .map(entry -> new AbstractMap.SimpleEntry<>(
                        entry.getKey(), 
                        cosineSimilarity(itemVec, entry.getValue())))
                .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
                .limit(topK)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 定时训练CF模型（每天凌晨3点）
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void trainModel() {
        log.info("开始训练 CF 模型...");
        
        try {
            // 获取最近30天的交互数据
            LocalDateTime since = LocalDateTime.now().minusDays(30);
            List<Object[]> interactions = behaviorRepository.getUserItemInteractionMatrix(since);
            
            if (interactions.isEmpty()) {
                log.warn("无交互数据，跳过训练");
                return;
            }
            
            log.info("获取到 {} 条交互数据", interactions.size());
            
            // 构建用户-物品矩阵
            Map<UUID, Map<UUID, Double>> userItemMatrix = buildUserItemMatrix(interactions);
            
            // 执行 ALS 训练
            trainALS(userItemMatrix, interactions.size());
            
            log.info("CF 模型训练完成: 用户数={}, 物品数={}", 
                    userEmbeddings.size(), itemEmbeddings.size());
            
        } catch (Exception e) {
            log.error("CF 模型训练失败", e);
        }
    }

    /**
     * 手动触发训练
     */
    public void triggerTraining() {
        trainModel();
    }

    /**
     * 构建用户-物品矩阵
     */
    private Map<UUID, Map<UUID, Double>> buildUserItemMatrix(List<Object[]> interactions) {
        Map<UUID, Map<UUID, Double>> matrix = new HashMap<>();
        
        for (Object[] row : interactions) {
            UUID userId = (UUID) row[0];
            UUID itemId = (UUID) row[1];
            Long count = (Long) row[2];
            
            // 将交互次数转换为评分（1-5）
            double rating = Math.min(5.0, 1.0 + Math.log(count));
            
            matrix.computeIfAbsent(userId, k -> new HashMap<>())
                   .put(itemId, rating);
        }
        
        return matrix;
    }

    /**
     * ALS 矩阵分解训练
     */
    private void trainALS(Map<UUID, Map<UUID, Double>> userItemMatrix, int interactionCount) {
        // 收集所有用户和物品
        Set<UUID> allUsers = userItemMatrix.keySet();
        Set<UUID> allItems = userItemMatrix.values().stream()
                .flatMap(m -> m.keySet().stream())
                .collect(Collectors.toSet());
        
        log.info("训练数据: 用户数={}, 物品数={}", allUsers.size(), allItems.size());
        
        // 初始化嵌入向量
        for (UUID userId : allUsers) {
            if (!userEmbeddings.containsKey(userId)) {
                userEmbeddings.put(userId, randomEmbedding());
            }
        }
        
        for (UUID itemId : allItems) {
            if (!itemEmbeddings.containsKey(itemId)) {
                itemEmbeddings.put(itemId, randomEmbedding());
            }
        }
        
        // ALS 迭代
        for (int iter = 0; iter < ALS_ITERATIONS; iter++) {
            double totalError = 0;
            
            // 固定物品向量，更新用户向量
            for (UUID userId : allUsers) {
                float[] userVec = userEmbeddings.get(userId);
                Map<UUID, Double> userRatings = userItemMatrix.get(userId);
                
                for (Map.Entry<UUID, Double> entry : userRatings.entrySet()) {
                    UUID itemId = entry.getKey();
                    double rating = entry.getValue();
                    float[] itemVec = itemEmbeddings.get(itemId);
                    
                    // 计算预测误差
                    double predicted = dotProduct(userVec, itemVec);
                    double error = rating - predicted;
                    totalError += error * error;
                    
                    // 更新用户向量
                    for (int d = 0; d < EMBEDDING_DIM; d++) {
                        userVec[d] += LEARNING_RATE * (error * itemVec[d] - LAMBDA * userVec[d]);
                    }
                }
            }
            
            // 固定用户向量，更新物品向量
            for (UUID itemId : allItems) {
                float[] itemVec = itemEmbeddings.get(itemId);
                
                for (Map.Entry<UUID, Map<UUID, Double>> userEntry : userItemMatrix.entrySet()) {
                    UUID userId = userEntry.getKey();
                    Map<UUID, Double> userRatings = userEntry.getValue();
                    
                    if (userRatings.containsKey(itemId)) {
                        double rating = userRatings.get(itemId);
                        float[] userVec = userEmbeddings.get(userId);
                        
                        double predicted = dotProduct(userVec, itemVec);
                        double error = rating - predicted;
                        
                        for (int d = 0; d < EMBEDDING_DIM; d++) {
                            itemVec[d] += LEARNING_RATE * (error * userVec[d] - LAMBDA * itemVec[d]);
                        }
                    }
                }
            }
            
            if (iter % 2 == 0) {
                log.info("ALS 迭代 {}/{}: MSE={}", iter + 1, ALS_ITERATIONS, totalError / interactionCount);
            }
        }
    }

    /**
     * 生成随机嵌入向量
     */
    private float[] randomEmbedding() {
        float[] vec = new float[EMBEDDING_DIM];
        Random random = new Random();
        for (int i = 0; i < EMBEDDING_DIM; i++) {
            vec[i] = (float) (random.nextGaussian() * 0.1);
        }
        return vec;
    }

    /**
     * 生成默认嵌入向量（基于ID哈希）
     */
    private float[] generateDefaultEmbedding(UUID id) {
        float[] vec = new float[EMBEDDING_DIM];
        Random random = new Random(id.hashCode());
        for (int i = 0; i < EMBEDDING_DIM; i++) {
            vec[i] = (float) (random.nextGaussian() * 0.1);
        }
        return vec;
    }

    /**
     * 计算余弦相似度
     */
    private double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) return 0;
        
        double dotProduct = 0;
        double normA = 0;
        double normB = 0;
        
        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        
        if (normA == 0 || normB == 0) return 0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * 计算点积
     */
    private double dotProduct(float[] a, float[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    /**
     * 获取模型状态
     */
    public Map<String, Object> getModelStatus() {
        return Map.of(
                "userCount", userEmbeddings.size(),
                "itemCount", itemEmbeddings.size(),
                "embeddingDim", EMBEDDING_DIM,
                "isTrained", !userEmbeddings.isEmpty()
        );
    }
}
