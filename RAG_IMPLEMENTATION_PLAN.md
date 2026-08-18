# Aura RAG 系统实施计划

## 总体目标
实现基于 Milvus 的多路召回 RAG 系统，支持穿搭推荐的语义检索和知识增强。

---

## Phase 1：MilvusVectorStore 接入 + Embedding 模型
**预计耗时**：30分钟

### 任务清单
- [x] 1.1 检查 pom.xml 依赖（spring-ai-starter-vector-store-milvus 已有）
- [x] 1.2 配置 Embedding 模型（DashScope text-embedding-v3）
- [x] 1.3 配置 MilvusVectorStore 连接参数
- [x] 1.4 创建 VectorStoreConfig 配置类
- [ ] 1.5 验证 Embedding 调用和 Milvus 连通性

### 验收标准
- Embedding 模型能正确返回 1024 维向量
- Milvus collection 自动创建
- 能成功存储和检索一条测试向量

---

## Phase 2：美学知识库构建 + 向量入库
**预计耗时**：45分钟

### 任务清单
- [x] 2.1 设计知识库数据结构（穿搭规则、色彩搭配、风格定义）
- [x] 2.2 创建知识入库服务 AestheticKnowledgeService
- [x] 2.3 实现 Document 加载和分块逻辑
- [ ] 2.4 批量向量化并存入 Milvus
- [ ] 2.5 创建衣物向量入库服务 WardrobeVectorService

### 验收标准
- 知识库至少 50 条穿搭知识
- 衣物向量能正确存储和检索
- 支持按用户隔离查询

---

## Phase 3：多路召回 + RRF 融合
**预计耗时**：40分钟

### 任务清单
- [x] 3.1 实现文本向量召回（语义相似度）
- [x] 3.2 实现标量过滤召回（元数据匹配）
- [x] 3.3 实现 RRF 融合排序（已有代码，需集成）
- [x] 3.4 创建 MultiChannelRetriever 统一检索接口
- [x] 3.5 集成到 ReAct 引擎的插件系统

### 验收标准
- 多路召回结果正确融合
- Top-K 结果相关性 > 80%
- 检索延迟 < 200ms

---

## Phase 4：Reranker 精排
**预计耗时**：30分钟

### 任务清单
- [ ] 4.1 选择 Reranker 方案（DashScope 或本地 BGE）
- [ ] 4.2 实现 RerankerService
- [ ] 4.3 集成到检索管道
- [ ] 4.4 对比精排前后效果

### 验收标准
- Rerank 后 Top-5 准确率提升 > 15%
- 端到端延迟 < 500ms

---

## Phase 5：集成到 ReAct 引擎
**预计耗时**：30分钟

### 任务清单
- [x] 5.1 创建 RAGSearchPlugin 插件
- [x] 5.2 注册到 PluginManager
- [x] 5.3 优化 ReAct Prompt，引导使用 RAG 检索
- [ ] 5.4 端到端测试

### 验收标准
- ReAct 引擎能正确调用 RAG 检索
- 推荐结果包含知识库引用
- 整体响应时间 < 5s

---

## 技术栈
- **Embedding**: DashScope text-embedding-v3 (1024维)
- **VectorDB**: Milvus 2.4.0 (已部署)
- **Framework**: Spring AI 1.0.0 GA
- **Reranker**: DashScope 或 BGE-reranker-v2-m3

## 风险点
1. DashScope Embedding API 限流 → 批量请求 + 重试
2. Milvus 查询延迟 → 索引优化 (HNSW)
3. 知识库质量 → 人工审核 + 持续迭代
