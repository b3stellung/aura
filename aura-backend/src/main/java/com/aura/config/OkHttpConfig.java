package com.aura.config;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 配置 OkHttp 超时时间
 * Spring AI 默认使用 OkHttp 作为 HTTP 客户端
 */
@Configuration
public class OkHttpConfig {

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)   // 连接超时
                .readTimeout(120, TimeUnit.SECONDS)      // 读取超时
                .writeTimeout(30, TimeUnit.SECONDS)      // 写入超时
                .build();
    }
}
