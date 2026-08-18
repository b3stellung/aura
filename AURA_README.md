# Aura - 个人美学操作系统

## 项目概述

Aura 是一款面向 C 端用户的 AI 驱动个人美学操作系统，聚焦于衣橱管理与美妆搭配两大核心场景。

**核心理念**：美是随时随地随景随人，不拘一格，体现多元感受，并能直观展现人的故事。

---

## 技术架构

### 整体架构

```
┌─────────────────────────────────────────────────────┐
│                   前端 (Next.js 14)                   │
│       Tailwind CSS + shadcn/ui + Framer Motion       │
└─────────────────────────────────────────────────────┘
                          │ SSE / REST
                          ▼
┌─────────────────────────────────────────────────────┐
│              API 网关 (Spring Boot 3.2)               │
│            JDK 17 + Spring WebFlux                    │
└─────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────┐
│               Agent 运行时层                          │
│  ┌─────────────────────────────────────────────┐   │
│  │  Spring AI (核心框架)                         │   │
│  │  - ChatModel (Qwen3.7-Plus 多模态)            │   │
│  │  - Tool Calling (插件机制)                    │   │
│  │  - EmbeddingModel (bge-large-zh)             │   │
│  │  - VectorStore (Milvus)                      │   │
│  └─────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────┐   │
│  │  LangGraph4j (Phase 3 引入)                   │   │
│  │  - ReAct 状态机                               │   │
│  │  - 多 Agent 协同                              │   │
│  │  - 条件分支编排                               │   │
│  └─────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────┐
│                    数据层                             │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────┐ │
│  │   Milvus     │  │ PostgreSQL   │  │   Redis    │ │
│  │  (向量检索)   │  │ (业务数据)   │  │  (缓存)    │ │
│  └──────────────┘  └──────────────┘  └────────────┘ │
└─────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────┐
│              CNN 微服务 (Phase 3+ 引入)                │
│           FastAPI + PyTorch / ONNX Runtime            │
│     - ResNet 特征提取                                 │
│     - 体型/肤色识别                                   │
└─────────────────────────────────────────────────────┘
```

### 技术栈选型

| 层级 | 技术选型 | 版本 | 用途 |
|------|---------|------|------|
| **JDK** | OpenJDK | 17 (LTS) | 运行环境 |
| **后端框架** | Spring Boot | 3.2+ | 核心业务逻辑 |
| **AI 框架** | Spring AI | 1.0+ | LLM 接入、Tool Calling |
| **Agent 编排** | LangGraph4j | Latest | ReAct 状态机 (Phase 3) |
| **多模态模型** | Qwen3.7-Plus | Latest | 图片属性识别、穿搭推荐 |
| **文本 Embedding** | bge-large-zh-v1.5 | 1024 维 | 文本向量化 |
| **图像 Embedding** | CLIP ViT-B/32 | 768 维 | 图像特征提取 |
| **Reranker** | bge-reranker-v2-m3 | Latest | 检索结果重排序 |
| **向量数据库** | Milvus | 2.3+ | 向量存储与检索 |
| **关系数据库** | PostgreSQL | 15+ | 业务数据存储 |
| **缓存** | Redis | 7+ | 语义缓存、会话状态 |
| **前端框架** | Next.js | 14+ | Web/H5 端 |
| **UI 组件** | Tailwind + shadcn/ui | Latest | 界面设计 |
| **动效引擎** | Framer Motion | 10+ | 丝滑动画 |

### 核心设计决策

#### 1. 为什么用 Milvus 而不是 PgVector？

- **专业向量数据库**：Milvus 专门为向量检索设计，索引算法更丰富
- **多向量支持**：一个 Collection 可以存多个向量字段（text_vector + image_vector）
- **标量过滤能力强**：支持复杂的表达式过滤
- **扩展性好**：后期数据量上来可以水平扩展

#### 2. 为什么用 Spring AI 而不是直接调 API？

- **统一抽象**：一套 API 支持多种模型（Qwen3.7-Plus、GPT-4V 等）
- **Tool Calling 原生支持**：插件机制开箱即用
- **与 Spring 生态无缝集成**：依赖注入、配置管理、监控等
- **流式输出支持**：Flux\<T\> 天然支持 SSE

#### 3. 为什么后期引入 LangGraph4j？

- **Phase 1-2**：单 Agent ReAct 循环，手写 while 循环足够
- **Phase 3+**：CNN + VLM + RAG 多步骤流程变复杂，需要图编排
- **多 Agent 协同**：穿搭 Agent + 妆容 Agent 并行执行

---

## RAG 检索架构（精度优先）

### 多路召回策略

```
用户请求："今晚约会，我穿了这件燕麦色风衣"
    │
    ▼
┌─────────────────────────────────────────────────────┐
│              多路召回（Top-K = 100）                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐          │
│  │ 文本向量  │  │ 图像向量  │  │ 标量过滤  │          │
│  │ bge-large│  │ CLIP     │  │ 场合/季节 │          │
│  │ Top-50   │  │ Top-50   │  │ Top-50   │          │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘          │
│       └──────────────┼──────────────┘               │
│                      ▼                              │
│              RRF 融合排序                            │
│         (Reciprocal Rank Fusion)                    │
│                      │                              │
│                      ▼                              │
│              Top-30 候选集                           │
└─────────────────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────────────────┐
│              精排层（Reranker）                      │
│         bge-reranker-v2-m3                          │
│         输入：query + 30 候选                        │
│         输出：Top-10 精排结果                         │
└─────────────────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────────────────┐
│              LLM 生成层                              │
│         Qwen3.7-Plus + 用户图片 + Top-10 候选        │
│         生成 3-5 套搭配方案 + 故事化文案              │
└─────────────────────────────────────────────────────┘
```

### RRF 融合公式

```
RRF_score(d) = Σ 1 / (k + rank_i(d))

其中：
- k = 60 (常数)
- rank_i(d) = 文档 d 在第 i 路召回中的排名
- 权重：文本(1.0) > 图像(0.8) > 标量过滤(0.6)
```

---

## 数据库设计

### Milvus Collections

#### Collection 1: user_wardrobe (用户衣物库)

```json
{
  "fields": [
    {"name": "id", "type": "VarChar", "max_length": 36, "primary_key": true},
    {"name": "user_id", "type": "VarChar", "max_length": 36},
    {"name": "category", "type": "VarChar", "max_length": 50},
    {"name": "color", "type": "VarChar", "max_length": 50},
    {"name": "material", "type": "VarChar", "max_length": 50},
    {"name": "style_tags", "type": "Array", "element_type": "VarChar", "max_capacity": 10},
    {"name": "season_tags", "type": "Array", "element_type": "VarChar", "max_capacity": 4},
    {"name": "occasion_tags", "type": "Array", "element_type": "VarChar", "max_capacity": 10},
    {"name": "text_vector", "type": "FloatVector", "dimension": 1024},
    {"name": "image_vector", "type": "FloatVector", "dimension": 768},
    {"name": "metadata", "type": "VarChar", "max_length": 65535}
  ],
  "index": {
    "text_vector": {"type": "HNSW", "M": 16, "efConstruction": 200},
    "image_vector": {"type": "HNSW", "M": 16, "efConstruction": 200}
  }
}
```

#### Collection 2: aesthetic_knowledge (美学知识库)

```json
{
  "fields": [
    {"name": "id", "type": "VarChar", "max_length": 36, "primary_key": true},
    {"name": "category", "type": "VarChar", "max_length": 100},
    {"name": "content", "type": "VarChar", "max_length": 65535},
    {"name": "vector", "type": "FloatVector", "dimension": 1024}
  ],
  "index": {
    "vector": {"type": "HNSW", "M": 16, "efConstruction": 200}
  }
}
```

### PostgreSQL 表结构

```sql
-- 用户表
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    avatar_url TEXT,
    subscription_tier VARCHAR(20) DEFAULT 'free',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 用户美学偏好表
CREATE TABLE user_aesthetic_profile (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    skin_tone VARCHAR(50),
    preferred_styles TEXT[],
    avoid_elements TEXT[],
    favorite_brands TEXT[],
    favorite_colors TEXT[],
    body_type VARCHAR(50),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Agent 会话表
CREATE TABLE agent_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_type VARCHAR(50),
    input_text TEXT,
    input_images TEXT[],
    react_trace JSONB,
    final_output JSONB,
    plugins_used TEXT[],
    token_consumed INT DEFAULT 0,
    latency_ms INT,
    status VARCHAR(20) DEFAULT 'running',
    created_at TIMESTAMP DEFAULT NOW()
);

-- 插件注册表
CREATE TABLE plugin_registry (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plugin_name VARCHAR(100) UNIQUE NOT NULL,
    plugin_type VARCHAR(50) NOT NULL,
    description TEXT,
    tool_schema JSONB,
    endpoint_url TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW()
);

-- 用户插件订阅表
CREATE TABLE user_plugin_subscriptions (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plugin_id UUID NOT NULL REFERENCES plugin_registry(id) ON DELETE CASCADE,
    is_enabled BOOLEAN DEFAULT true,
    subscribed_at TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (user_id, plugin_id)
);
```

---

## 项目结构

```
aura-backend/
├── src/main/java/com/aura/
│   ├── AuraApplication.java                 # 启动类
│   ├── config/                              # 配置类
│   │   ├── AiConfig.java                    # Spring AI 配置
│   │   ├── MilvusConfig.java                # Milvus 客户端配置
│   │   ├── RedisConfig.java                 # Redis 配置
│   │   └── SecurityConfig.java              # 安全配置
│   ├── controller/                          # API 控制器
│   │   ├── AuthController.java              # 认证接口
│   │   ├── WardrobeController.java          # 衣橱管理接口
│   │   └── AuraController.java              # AI 推荐接口
│   ├── service/                             # 业务逻辑
│   │   ├── AuthService.java
│   │   ├── WardrobeService.java
│   │   └── AuraService.java                 # 核心推荐服务
│   ├── repository/                          # 数据访问
│   │   ├── UserRepository.java
│   │   └── SessionRepository.java
│   ├── model/                               # 数据模型
│   │   ├── entity/                          # 实体类
│   │   ├── dto/                             # 数据传输对象
│   │   └── vo/                              # 视图对象
│   ├── plugin/                              # 插件系统
│   │   ├── AuraPlugin.java                  # 插件接口
│   │   ├── PluginManager.java               # 插件管理器
│   │   └── impl/                            # 插件实现
│   │       ├── ClothingAnalyzerPlugin.java  # 衣物分析
│   │       ├── RAGSearchPlugin.java         # RAG 检索
│   │       └── WeatherPlugin.java           # 天气查询
│   ├── rag/                                 # RAG 模块
│   │   ├── MilvusRetrievalService.java      # 向量检索
│   │   ├── RerankerService.java             # 精排服务
│   │   └── SemanticCacheService.java        # 语义缓存
│   ├── react/                               # ReAct 引擎
│   │   ├── ReActState.java                  # 状态定义
│   │   ├── AuraReActEngine.java             # 引擎实现
│   │   └── ReActTimeoutException.java       # 超时异常
│   └── security/                            # 安全模块
│       ├── JwtService.java
│       └── PrivacyService.java              # 数据脱敏
├── src/main/resources/
│   ├── application.yml                      # 主配置
│   ├── application-dev.yml                  # 开发环境配置
│   ├── prompts/                             # Prompt 模板
│   │   ├── react-thought.txt
│   │   ├── clothing-analyze.txt
│   │   └── outfit-recommend.txt
│   └── db/
│       └── schema.sql                       # 数据库初始化脚本
├── pom.xml                                  # Maven 配置
└── docker-compose.yml                       # 本地开发环境
```

---

## 开发计划

### Phase 1: 基础设施搭建 (Week 1-2)

**目标**：环境就绪，技术验证通过

| 任务 | 描述 | 验收标准 |
|------|------|----------|
| 1.1 | 创建 Spring Boot 项目骨架 | 项目能正常启动 |
| 1.2 | 配置 Milvus + PostgreSQL + Redis | 服务正常运行 |
| 1.3 | 创建数据库表结构 | 表创建成功 |
| 1.4 | 集成 Spring AI + Qwen3.7-Plus | 能调用模型 |
| 1.5 | 实现衣物分析插件原型 | 识别准确率 > 85% |

### Phase 2: ReAct 引擎实现 (Week 3-4)

**目标**：单 Agent 闭环，SSE 流式输出

| 任务 | 描述 | 验收标准 |
|------|------|----------|
| 2.1 | 实现 ReAct 状态机 | 循环正常执行 |
| 2.2 | 实现 Tool Calling 机制 | 插件调用成功 |
| 2.3 | 实现 SSE 流式输出 | 前端实时展示 |
| 2.4 | 实现多路召回 + RRF 融合 | 返回 Top-30 候选 |
| 2.5 | 实现 Reranker 精排 | 返回 Top-10 结果 |

### Phase 3: RAG + CNN 集成 (Week 5-6)

**目标**：三层 RAG 就绪，CNN 特征增强

| 任务 | 描述 | 验收标准 |
|------|------|----------|
| 3.1 | 实现 Milvus 向量检索 | 检索准确率 > 80% |
| 3.2 | 实现美学知识库 RAG | 检索结果相关 |
| 3.3 | 集成 CNN 微服务（可选） | 特征提取成功 |
| 3.4 | 引入 LangGraph4j（可选） | 图编排正常 |

### Phase 4: 前端开发 (Week 7-8)

**目标**：完整的 Web Demo

| 任务 | 描述 | 验收标准 |
|------|------|----------|
| 4.1 | 初始化 Next.js 项目 | 项目正常启动 |
| 4.2 | 实现流式聊天组件 | SSE 接收正常 |
| 4.3 | 实现 Lookbook 卡片 | 渲染正确 |
| 4.4 | 实现 ReAct 解析面板 | 展开/收起正常 |

---

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.9+
- Docker & Docker Compose
- Node.js 20+ (前端开发)

### 启动后端

```bash
# 1. 启动基础服务
docker compose up -d

# 2. 构建并启动后端
cd aura-backend
mvn spring-boot:run

# 3. 访问 API
curl http://localhost:8080/api/v1/health
```

### 启动前端

```bash
cd aura-frontend
npm install
npm run dev

# 访问 http://localhost:3000
```

---

## 环境变量配置

```bash
# .env 文件
QWEN_API_KEY=your_qwen_api_key        # 阿里云百炼 API Key
DB_PASSWORD=aura123                     # PostgreSQL 密码
JWT_SECRET=your_jwt_secret              # JWT 签名密钥
```

---

## 文档版本

- **版本**：v2.0
- **最后更新**：2026-07-30
- **更新内容**：基于选型讨论，更新为 Milvus + Spring AI 架构
