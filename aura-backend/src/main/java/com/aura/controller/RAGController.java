package com.aura.controller;

import com.aura.rag.AestheticKnowledgeService;
import com.aura.rag.MilvusRetrievalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * RAG 系统管理接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/rag")
@CrossOrigin(origins = "*")
public class RAGController {

    private final AestheticKnowledgeService knowledgeService;
    private final MilvusRetrievalService retrievalService;
    private final VectorStore vectorStore;

    public RAGController(AestheticKnowledgeService knowledgeService, 
                         MilvusRetrievalService retrievalService,
                         VectorStore vectorStore) {
        this.knowledgeService = knowledgeService;
        this.retrievalService = retrievalService;
        this.vectorStore = vectorStore;
    }

    /**
     * 初始化知识库
     */
    @PostMapping("/init")
    public ResponseEntity<Map<String, Object>> initKnowledgeBase() {
        log.info("初始化知识库...");
        try {
            knowledgeService.initKnowledgeBase();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "知识库初始化完成"
            ));
        } catch (Exception e) {
            log.error("知识库初始化失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", e.getMessage() != null ? e.getMessage() : "Unknown error",
                    "type", e.getClass().getSimpleName()
            ));
        }
    }

    /**
     * 知识检索测试
     */
    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> searchKnowledge(
            @RequestBody Map<String, Object> request) {
        String query = (String) request.get("query");
        String category = (String) request.get("category");
        Integer topK = request.get("topK") != null ? 
                ((Number) request.get("topK")).intValue() : 5;
        
        log.info("知识检索: query={}, category={}, topK={}", query, category, topK);
        
        try {
            List<AestheticKnowledgeService.KnowledgeResult> results;
            
            if (category != null && !category.isBlank()) {
                results = knowledgeService.searchByCategory(query, category, topK);
            } else {
                results = knowledgeService.searchKnowledge(query, topK);
            }
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "query", query,
                    "results_count", results.size(),
                    "results", results.stream()
                            .map(r -> Map.of(
                                    "id", r.id(),
                                    "content", r.content(),
                                    "category", r.metadata().getOrDefault("category", "未知"),
                                    "score", Math.round(r.score() * 100) / 100.0
                            ))
                            .toList()
            ));
        } catch (Exception e) {
            log.error("知识检索失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", e.getMessage() != null ? e.getMessage() : "Unknown error",
                    "type", e.getClass().getSimpleName()
            ));
        }
    }

    /**
     * Embedding 测试
     */
    @PostMapping("/test/embedding")
    public ResponseEntity<Map<String, Object>> testEmbedding(
            @RequestBody Map<String, String> request) {
        String text = request.get("text");
        log.info("测试 Embedding: text={}", text);
        
        try {
            float[] vector = retrievalService.embedText(text);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "text", text,
                    "dimension", vector.length,
                    "vector_preview", java.util.Arrays.copyOf(vector, 5)
            ));
        } catch (Exception e) {
            log.error("Embedding 测试失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", e.getMessage() != null ? e.getMessage() : "Unknown error",
                    "type", e.getClass().getSimpleName()
            ));
        }
    }

    /**
     * 直接测试 VectorStore 插入
     */
    @PostMapping("/test/insert")
    public ResponseEntity<Map<String, Object>> testInsert() {
        log.info("测试 VectorStore 直接插入...");
        
        try {
            // 创建测试文档
            Document doc = new Document("test-" + System.currentTimeMillis(), 
                    "这是一条测试知识", 
                    Map.of("category", "test", "type", "test"));
            
            log.info("创建文档: id={}, content={}", doc.getId(), doc.getText());
            
            // 尝试插入
            vectorStore.write(List.of(doc));
            
            log.info("插入成功");
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "插入成功",
                    "doc_id", doc.getId()
            ));
        } catch (Exception e) {
            log.error("VectorStore 插入失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", e.getMessage() != null ? e.getMessage() : "Unknown error",
                    "type", e.getClass().getSimpleName(),
                    "stacktrace", java.util.Arrays.toString(e.getStackTrace())
            ));
        }
    }

    /**
     * Milvus 连通性测试
     */
    @GetMapping("/test/milvus")
    public ResponseEntity<Map<String, Object>> testMilvus() {
        log.info("测试 Milvus 连通性...");
        try {
            // 尝试 embedding + 存储 + 检索
            float[] vector = retrievalService.embedText("测试连接");
            boolean connected = vector != null && vector.length > 0;
            
            return ResponseEntity.ok(Map.of(
                    "status", connected ? "connected" : "failed",
                    "embedding_dimension", vector != null ? vector.length : 0
            ));
        } catch (Exception e) {
            log.error("Milvus 连通性测试失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", e.getMessage() != null ? e.getMessage() : "Unknown error",
                    "type", e.getClass().getSimpleName()
            ));
        }
    }
}
