package com.aura;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Aura - 个人美学操作系统
 * 
 * @author Aura Team
 */
@SpringBootApplication
@EnableAsync
public class AuraApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuraApplication.class, args);
        System.out.println("===========================================");
        System.out.println("   🌟 Aura 后端服务启动成功！");
        System.out.println("   📚 API 文档: http://localhost:8080");
        System.out.println("   🎯 环境: development");
        System.out.println("===========================================");
    }
}
