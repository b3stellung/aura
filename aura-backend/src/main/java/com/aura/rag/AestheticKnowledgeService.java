package com.aura.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 美学知识库服务
 * 
 * 负责：
 * 1. 知识入库（穿搭规则、色彩搭配、风格定义）
 * 2. 语义检索
 * 3. 知识管理
 */
@Slf4j
@Service
public class AestheticKnowledgeService {

    private final VectorStore vectorStore;

    public AestheticKnowledgeService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * 存储知识文档
     */
    public void storeKnowledge(String id, String content, Map<String, Object> metadata) {
        log.info("存储知识: id={}", id);
        
        try {
            Document document = new Document(id, content, metadata);
            vectorStore.write(List.of(document));
            log.info("知识存储成功: id={}", id);
        } catch (Exception e) {
            log.error("知识存储失败: id={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 批量存储知识（分批处理，每批最多10条）
     */
    public void storeKnowledgeBatch(List<KnowledgeItem> items) {
        log.info("批量存储知识: count={}", items.size());
        
        int batchSize = 10;  // DashScope Embedding API 限制
        for (int i = 0; i < items.size(); i += batchSize) {
            int end = Math.min(i + batchSize, items.size());
            List<KnowledgeItem> batch = items.subList(i, end);
            
            try {
                List<Document> documents = batch.stream()
                        .map(item -> new Document(item.id(), item.content(), item.metadata()))
                        .collect(Collectors.toList());
                
                vectorStore.write(documents);
                log.info("已存储 {}/{} 条知识", end, items.size());
            } catch (Exception e) {
                log.error("批量存储失败: batch {}/{}, error={}", i, items.size(), e.getMessage(), e);
                throw e;
            }
        }
        
        log.info("批量存储完成: count={}", items.size());
    }

    /**
     * 语义检索知识
     */
    public List<KnowledgeResult> searchKnowledge(String query, int topK) {
        log.info("检索知识: query={}, topK={}", query, topK);
        
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build();
        
        List<Document> results = vectorStore.similaritySearch(request);
        
        return results.stream()
                .map(doc -> new KnowledgeResult(
                        doc.getId(),
                        doc.getText(),
                        doc.getMetadata(),
                        doc.getScore() != null ? doc.getScore() : 0.0
                ))
                .collect(Collectors.toList());
    }

    /**
     * 按分类检索知识
     */
    public List<KnowledgeResult> searchByCategory(String query, String category, int topK) {
        log.info("按分类检索知识: query={}, category={}, topK={}", query, category, topK);
        
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build();
        
        List<Document> results = vectorStore.similaritySearch(request);
        
        // 过滤分类
        return results.stream()
                .filter(doc -> category.equals(doc.getMetadata().get("category")))
                .map(doc -> new KnowledgeResult(
                        doc.getId(),
                        doc.getText(),
                        doc.getMetadata(),
                        doc.getScore() != null ? doc.getScore() : 0.0
                ))
                .collect(Collectors.toList());
    }

    /**
     * 初始化美学知识库
     */
    public void initKnowledgeBase() {
        log.info("初始化美学知识库...");
        
        List<KnowledgeItem> knowledgeItems = buildDefaultKnowledge();
        storeKnowledgeBatch(knowledgeItems);
        
        log.info("美学知识库初始化完成: {} 条知识", knowledgeItems.size());
    }

    /**
     * 构建默认知识库
     */
    private List<KnowledgeItem> buildDefaultKnowledge() {
        List<KnowledgeItem> items = new ArrayList<>();
        
        // 色彩搭配规则
        items.add(new KnowledgeItem(
                "color-001",
                "同色系搭配：选择同一颜色的不同深浅，如浅蓝衬衫+深蓝西裤，营造层次感又不失统一。",
                Map.of("category", "色彩搭配", "type", "rule", "tags", "同色系,层次感")
        ));
        
        items.add(new KnowledgeItem(
                "color-002",
                "互补色搭配：色轮上相对的颜色组合，如蓝色+橙色、红色+绿色。适合打造视觉冲击力强的造型。",
                Map.of("category", "色彩搭配", "type", "rule", "tags", "互补色,撞色")
        ));
        
        items.add(new KnowledgeItem(
                "color-003",
                "中性色万能搭配：黑、白、灰、米、驼色是万能中性色，可以与任何颜色搭配，适合日常穿搭。",
                Map.of("category", "色彩搭配", "type", "rule", "tags", "中性色,百搭")
        ));
        
        // 场合穿搭
        items.add(new KnowledgeItem(
                "occasion-001",
                "约会穿搭原则：选择柔和色调、质感面料，避免过于正式或过于休闲。女生可选连衣裙或针织+半裙，男生可选衬衫+休闲西裤。",
                Map.of("category", "场合穿搭", "type", "guide", "tags", "约会,休闲约会")
        ));
        
        items.add(new KnowledgeItem(
                "occasion-002",
                "职场穿搭要点：保持专业感，选择合身剪裁。基础单品：白衬衫、黑色西裤、西装外套。配色以中性色为主。",
                Map.of("category", "场合穿搭", "type", "guide", "tags", "职场,通勤")
        ));
        
        items.add(new KnowledgeItem(
                "occasion-003",
                "派对穿搭技巧：可以大胆尝试亮片、丝绒等特殊材质。选择1-2个亮点单品，其余保持简洁。",
                Map.of("category", "场合穿搭", "type", "guide", "tags", "派对,晚宴")
        ));
        
        // 风格定义
        items.add(new KnowledgeItem(
                "style-001",
                "法式慵懒风：特点是不经意的精致感。常见单品：条纹衫、高腰牛仔裤、贝雷帽、丝巾。配色以黑白灰蓝为主。",
                Map.of("category", "风格定义", "type", "definition", "tags", "法式,慵懒")
        ));
        
        items.add(new KnowledgeItem(
                "style-002",
                "极简主义风格：Less is more。选择高质量基础款，配色控制在3色以内。注重剪裁和面料质感。",
                Map.of("category", "风格定义", "type", "definition", "tags", "极简,简约")
        ));
        
        items.add(new KnowledgeItem(
                "style-003",
                "复古风格：回溯70-90年代元素。常见单品：高腰阔腿裤、灯芯绒外套、印花衬衫、圆框眼镜。",
                Map.of("category", "风格定义", "type", "definition", "tags", "复古,vintage")
        ));
        
        // 材质知识
        items.add(new KnowledgeItem(
                "material-001",
                "真丝面料特点：光泽感强，触感顺滑，适合正式场合和约会。缺点是易皱、需要干洗。",
                Map.of("category", "材质知识", "type", "knowledge", "tags", "真丝,丝绸")
        ));
        
        items.add(new KnowledgeItem(
                "material-002",
                "亚麻面料特点：透气性好，适合夏季。自带褶皱感是其特色，营造休闲自然风格。",
                Map.of("category", "材质知识", "type", "knowledge", "tags", "亚麻,夏季")
        ));
        
        // 季节穿搭
        items.add(new KnowledgeItem(
                "season-001",
                "春季穿搭关键词：轻薄外套、浅色系、碎花元素、针织开衫。适合叠穿，早晚温差大时可增减层次。",
                Map.of("category", "季节穿搭", "type", "guide", "tags", "春季,春天")
        ));
        
        items.add(new KnowledgeItem(
                "season-002",
                "夏季穿搭关键词：透气面料、明亮色彩、简约剪裁。推荐棉麻材质，避免过于厚重。",
                Map.of("category", "季节穿搭", "type", "guide", "tags", "夏季,夏天")
        ));
        
        items.add(new KnowledgeItem(
                "season-003",
                "秋季穿搭关键词：大地色系、叠穿技巧、质感面料。驼色、焦糖色、墨绿是秋季经典色。",
                Map.of("category", "季节穿搭", "type", "guide", "tags", "秋季,秋天")
        ));
        
        items.add(new KnowledgeItem(
                "season-004",
                "冬季穿搭关键词：保暖与时尚兼顾。选择羊毛、羊绒等保暖材质。深色系为主，可用亮色配饰点缀。",
                Map.of("category", "季节穿搭", "type", "guide", "tags", "冬季,冬天")
        ));
        
        // 身材穿搭
        items.add(new KnowledgeItem(
                "body-001",
                "梨形身材穿搭：上半身选择亮色或有设计感的单品吸引视线，下半身选择A字裙或阔腿裤修饰臀部线条。",
                Map.of("category", "身材穿搭", "type", "guide", "tags", "梨形,下半身")
        ));
        
        items.add(new KnowledgeItem(
                "body-002",
                "苹果型身材穿搭：选择V领上衣拉长颈部线条，高腰裤提升腰线。避免过于紧身的上衣。",
                Map.of("category", "身材穿搭", "type", "guide", "tags", "苹果型,上半身")
        ));
        
        return items;
    }

    /**
     * 知识条目
     */
    public record KnowledgeItem(String id, String content, Map<String, Object> metadata) {}

    /**
     * 知识检索结果
     */
    public record KnowledgeResult(String id, String content, Map<String, Object> metadata, double score) {}
}
