package com.aura.cf;

import com.aura.rag.MilvusRetrievalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * 融合服务
 * 
 * 将 CF 嵌入和语义嵌入融合，生成个性化推荐向量
 */
@Slf4j
@Service
public class FusionService {

    private final CFEmbeddingService cfService;
    private final MilvusRetrievalService semanticService;
    
    /**
     * CF 嵌入维度
     */
    private static final int CF_DIM = 64;
    
    /**
     * 语义嵌入维度
     */
    private static final int SEMANTIC_DIM = 1024;
    
    /**
     * 融合后维度
     */
    private static final int FUSED_DIM = 256;
    
    /**
     * CF 权重（可调）
     */
    private static final double CF_WEIGHT = 0.3;
    
    /**
     * 语义权重（可调）
     */
    private static final double SEMANTIC_WEIGHT = 0.7;

    public FusionService(CFEmbeddingService cfService, MilvusRetrievalService semanticService) {
        this.cfService = cfService;
        this.semanticService = semanticService;
    }

    /**
     * 融合用户CF嵌入和查询语义嵌入
     * 
     * @param userId 用户ID
     * @param queryText 查询文本
     * @return 融合向量 (256维)
     */
    public float[] fuseUserQuery(java.util.UUID userId, String queryText) {
        // 1. 获取用户CF嵌入 (64维)
        float[] cfVector = cfService.getUserEmbedding(userId);
        
        // 2. 获取查询语义嵌入 (1024维)
        float[] semanticVector = semanticService.embedText(queryText);
        
        // 3. 融合
        return fuse(cfVector, semanticVector);
    }

    /**
     * 融合物品CF嵌入和描述语义嵌入
     * 
     * @param itemId 物品ID
     * @param description 物品描述
     * @return 融合向量 (256维)
     */
    public float[] fuseItem(java.util.UUID itemId, String description) {
        // 1. 获取物品CF嵌入 (64维)
        float[] cfVector = cfService.getItemEmbedding(itemId);
        
        // 2. 获取描述语义嵌入 (1024维)
        float[] semanticVector = semanticService.embedText(description);
        
        // 3. 融合
        return fuse(cfVector, semanticVector);
    }

    /**
     * 融合CF嵌入和语义嵌入
     * 
     * 策略：加权拼接 + MLP降维
     * 
     * @param cfVector CF嵌入 (64维)
     * @param semanticVector 语义嵌入 (1024维)
     * @return 融合向量 (256维)
     */
    public float[] fuse(float[] cfVector, float[] semanticVector) {
        // 1. 维度对齐
        float[] cfAligned = alignDimension(cfVector, CF_DIM, FUSED_DIM / 2);
        float[] semanticAligned = alignDimension(semanticVector, SEMANTIC_DIM, FUSED_DIM / 2);
        
        // 2. 加权拼接
        float[] concatenated = new float[FUSED_DIM];
        
        // CF部分 (128维)
        for (int i = 0; i < FUSED_DIM / 2; i++) {
            concatenated[i] = (float) (cfAligned[i] * CF_WEIGHT);
        }
        
        // 语义部分 (128维)
        for (int i = 0; i < FUSED_DIM / 2; i++) {
            concatenated[FUSED_DIM / 2 + i] = (float) (semanticAligned[i] * SEMANTIC_WEIGHT);
        }
        
        // 3. MLP降维 (简化版：线性变换 + ReLU)
        float[] fused = mlpTransform(concatenated);
        
        // 4. L2归一化
        return l2Normalize(fused);
    }

    /**
     * 维度对齐（线性插值）
     */
    private float[] alignDimension(float[] vector, int fromDim, int toDim) {
        if (fromDim == toDim) return vector;
        
        float[] aligned = new float[toDim];
        double scale = (double) fromDim / toDim;
        
        for (int i = 0; i < toDim; i++) {
            double srcIdx = i * scale;
            int idx0 = (int) Math.floor(srcIdx);
            int idx1 = Math.min(idx0 + 1, fromDim - 1);
            double fraction = srcIdx - idx0;
            
            aligned[i] = (float) (vector[idx0] * (1 - fraction) + vector[idx1] * fraction);
        }
        
        return aligned;
    }

    /**
     * MLP变换（简化版）
     * 
     * 输入: 256维
     * 隐藏层: 128维 + ReLU
     * 输出: 128维
     */
    private float[] mlpTransform(float[] input) {
        // 简化版：直接截断到128维 + ReLU
        float[] output = new float[128];
        
        for (int i = 0; i < 128; i++) {
            // 加权求和（简化版，实际应该有学习到的权重）
            double sum = 0;
            for (int j = 0; j < input.length; j++) {
                sum += input[j] * getMLPWeight(i, j);
            }
            
            // ReLU激活
            output[i] = (float) Math.max(0, sum);
        }
        
        return output;
    }

    /**
     * 获取MLP权重（简化版，使用固定权重）
     * 
     * 实际应该从训练好的模型加载
     */
    private double getMLPWeight(int outputIdx, int inputIdx) {
        // 使用简单的哈希生成固定权重
        long seed = (long) outputIdx * 1000000 + inputIdx;
        return Math.sin(seed) * 0.1;
    }

    /**
     * L2归一化
     */
    private float[] l2Normalize(float[] vector) {
        double norm = 0;
        for (float v : vector) {
            norm += v * v;
        }
        norm = Math.sqrt(norm);
        
        if (norm == 0) return vector;
        
        float[] normalized = new float[vector.length];
        for (int i = 0; i < vector.length; i++) {
            normalized[i] = (float) (vector[i] / norm);
        }
        
        return normalized;
    }

    /**
     * 计算两个融合向量的相似度
     */
    public double calculateSimilarity(float[] fused1, float[] fused2) {
        return cosineSimilarity(fused1, fused2);
    }

    /**
     * 余弦相似度
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
     * 获取融合配置
     */
    public java.util.Map<String, Object> getFusionConfig() {
        return java.util.Map.of(
                "cfDim", CF_DIM,
                "semanticDim", SEMANTIC_DIM,
                "fusedDim", FUSED_DIM,
                "cfWeight", CF_WEIGHT,
                "semanticWeight", SEMANTIC_WEIGHT
        );
    }
}
