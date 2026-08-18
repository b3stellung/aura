package com.aura.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Milvus 配置
 * 由 Spring AI 自动配置管理
 */
@Slf4j
@Configuration
public class VectorStoreConfig {
    // Spring AI 自动配置会创建 MilvusServiceClient 和 VectorStore
    // 无需手动配置
}
