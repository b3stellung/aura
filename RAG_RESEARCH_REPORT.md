# Aura RAG 深度调研报告

## 一、学术前沿（2024-2026）

### 1.1 关键论文

| 论文 | 来源 | 核心贡献 |
|------|------|----------|
| CFALR | ACM TOIS 2026 | CF嵌入+LLM语义空间融合，个性化穿搭推荐 |
| MLLM-Enhanced Outfit Rec | SIGIR eCom 2025 | 多阶段管线：MLLM推理+检索候选+学习打分 |
| LookBench | arxiv 2601.14706 | 真实电商穿搭检索基准，覆盖单件+套装 |
| FashionCLIP/Marqo-FashionSigLIP | Polytechnic of Milan | 时尚领域CLIP微调，比通用CLIP提升57% |

### 1.2 核心洞察

1. **LLM单独缺个性化信号** → 需要CF嵌入补充协同过滤
2. **领域微调CLIP >> 通用CLIP** → FashionCLIP/SigLIP效果显著
3. **Late Interaction检索（ColQwen2）** → 多向量表示，适合视觉丰富的时尚目录
4. **多模态RAG** → 图像+文本统一向量空间检索

---

## 二、工业界最佳实践

### 2.1 Embedding模型选型

| 模型 | 维度 | 特点 | 推荐度 |
|------|------|------|--------|
| text-embedding-v3 (DashScope) | 1024/768/512 | 可调维度，性价比高 | ⭐⭐⭐⭐ |
| text-embedding-v4 (DashScope) | 1024 | 最新，基于Qwen3 | ⭐⭐⭐⭐⭐ |
| BGE-M3 (BAAI) | 1024 | 多语言+多粒度 | ⭐⭐⭐⭐ |
| Marqo-FashionSigLIP | 768 | 时尚领域专用 | ⭐⭐⭐⭐⭐ |
| GME-Qwen2-VL (阿里) | 动态 | 多模态（图+文） | ⭐⭐⭐⭐⭐ |

### 2.2 Spring AI RAG架构

**方案A：QuestionAnswerAdvisor（简单RAG）**
```java
QuestionAnswerAdvisor qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
    .promptTemplate(new PromptTemplate("基于上下文回答：{context}\n\n问题：{question}"))
    .build();
```

**方案B：RetrievalAugmentationAdvisor（模块化RAG）**
```java
RetrievalAugmentationAdvisor advisor = RetrievalAugmentationAdvisor.builder()
    .documentRetriever(VectorStoreDocumentRetriever.builder()
        .vectorStore(vectorStore)
        .similarityThreshold(0.5)
        .topK(5)
        .build())
    .queryTransformers(new RewriteQueryTransformer(...))
    .documentJoiner(new ConcatenationDocumentJoiner())
    .build();
```

### 2.3 生产级RAG管线

```
用户查询
    ↓
[Pre-Retrieval] 查询改写/扩展/分解
    ↓
[Retrieval] 混合检索（向量+BM25+元数据过滤）
    ↓
[Post-Retrieval] 重排序 + 压缩
    ↓
[Generation] 结构化Prompt + 引用
```

---

## 三、Aura推荐方案

### 3.1 短期优化（当前可做）

1. **Embedding模型升级**
   - 从`text-embedding-v3`切换到`text-embedding-v4`（更新更强）
   - 支持可调维度（1024/768/512）

2. **Spring AI Advisor集成**
   - 使用`QuestionAnswerAdvisor`替代手动RAG
   - 自动处理检索+上下文注入

3. **混合检索**
   - 向量相似度 + 元数据过滤（季节/场合/风格）
   - RRF融合排序（已有代码）

4. **Re-ranking**
   - DashScope Rank API 或 BGE-reranker-v2-m3
   - Spring AI暂无内置，需自定义

### 3.2 中期演进

1. **多模态Embedding**
   - 集成GME-Qwen2-VL或Marqo-FashionSigLIP
   - 支持图片+文本联合检索

2. **Agentic RAG**
   - LLM根据查询类型选择检索策略
   - 视觉查询→图像搜索；风格问题→文本搜索

3. **个性化增强**
   - CF嵌入+LLM语义融合（CFALR方案）
   - 用户历史行为建模

### 3.3 长期愿景

1. **ColQwen2 Late Interaction**
   - 多向量表示，精细匹配
   - 适合时尚目录检索

2. **实时趋势融入**
   - 接入社交媒体/时尚资讯
   - 动态更新知识库

3. **对话式推荐**
   - 多轮对话RAG
   - 上下文理解+偏好学习

---

## 四、具体实施建议

### 4.1 立即可做（1-2天）

```yaml
# application.yml 升级
spring:
  ai:
    openai:
      embedding:
        options:
          model: text-embedding-v4  # 升级到v4
          dimensions: 1024
```

### 4.2 本周可做（3-5天）

1. 添加Spring AI RAG依赖
2. 实现QuestionAnswerAdvisor
3. 混合检索+RRF融合
4. 简单Re-ranking

### 4.3 下周规划

1. 多模态Embedding原型
2. 个性化信号接入
3. A/B测试框架

---

## 五、关键资源

### 学术论文
- CFALR: ACM TOIS 2026
- LookBench: arxiv 2601.14706
- FashionCLIP: github.com/patrickjohncyh/fashion-clip

### 工具库
- Spring AI RAG: spring-ai-rag, spring-ai-advisors-vector-store
- Marqo-FashionSigLIP: HuggingFace
- ColQwen2: colpali-engine (pip)
- RRF: 已实现（MilvusRetrievalService.rrfFusion）

### 数据集
- DeepFashion: 80万标注图像
- Polyvore Outfits: 套装搭配数据
- Fashion200k: 大规模时尚数据

---

**结论**：当前RAG系统已可用，建议按"短期优化→中期演进→长期愿景"路线迭代。重点是：
1. 升级Embedding到v4
2. 集成Spring AI Advisor
3. 逐步引入多模态能力
