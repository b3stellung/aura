package com.aura.plugin.impl;

import com.aura.plugin.AuraPlugin;
import com.aura.rag.AestheticKnowledgeService;
import com.aura.rag.MilvusRetrievalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RAG 检索插件
 * 
 * 提供基于向量检索的穿搭知识查询能力
 */
@Slf4j
@Component
public class RAGSearchPlugin implements AuraPlugin {

    private final AestheticKnowledgeService knowledgeService;
    private final MilvusRetrievalService retrievalService;

    public RAGSearchPlugin(AestheticKnowledgeService knowledgeService, 
                           MilvusRetrievalService retrievalService) {
        this.knowledgeService = knowledgeService;
        this.retrievalService = retrievalService;
    }

    @Override
    public String getName() {
        return "RAGSearch";
    }

    @Override
    public String getDescription() {
        return "检索美学知识库，获取穿搭规则、色彩搭配、风格定义等专业知识";
    }

    @Override
    public String getToolSchema() {
        return """
                {
                    "name": "RAGSearch",
                    "description": "检索美学知识库，获取穿搭规则、色彩搭配、风格定义等专业知识",
                    "parameters": {
                        "type": "object",
                        "properties": {
                            "query": {
                                "type": "string",
                                "description": "检索关键词，如：约会穿搭、色彩搭配、极简风格"
                            },
                            "category": {
                                "type": "string",
                                "description": "知识分类（可选）：色彩搭配、场合穿搭、风格定义、材质知识、季节穿搭、身材穿搭"
                            },
                            "top_k": {
                                "type": "integer",
                                "description": "返回结果数量，默认3"
                            }
                        },
                        "required": ["query"]
                    }
                }
                """;
    }

    @Override
    public Object execute(Map<String, Object> params) {
        String query = (String) params.get("query");
        if (query == null || query.isBlank()) {
            return Map.of("query", "", "results_count", 0, "results", List.of(), "message", "查询内容为空");
        }
        String category = (String) params.get("category");
        Integer topK = params.get("top_k") != null ? 
                ((Number) params.get("top_k")).intValue() : 3;
        
        log.info("RAG 检索: query={}, category={}, topK={}", query, category, topK);
        
        try {
            List<AestheticKnowledgeService.KnowledgeResult> results;
            
            if (category != null && !category.isBlank()) {
                results = knowledgeService.searchByCategory(query, category, topK);
            } else {
                results = knowledgeService.searchKnowledge(query, topK);
            }
            
            // 格式化结果
            List<Map<String, Object>> formattedResults = results.stream()
                    .map(r -> Map.<String, Object>of(
                            "id", r.id(),
                            "content", r.content(),
                            "category", r.metadata().getOrDefault("category", "未知"),
                            "score", Math.round(r.score() * 100) / 100.0
                    ))
                    .collect(Collectors.toList());
            
            Map<String, Object> response = Map.of(
                    "query", query,
                    "results_count", formattedResults.size(),
                    "results", formattedResults
            );
            
            log.info("RAG 检索完成: 找到 {} 条结果", formattedResults.size());
            return response;
            
        } catch (Exception e) {
            log.error("RAG 检索失败", e);
            return Map.of(
                    "error", e.getMessage(),
                    "query", query,
                    "results", List.of()
            );
        }
    }
}
