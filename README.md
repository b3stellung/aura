# Aura — 个人美学操作系统

AI 驱动的个人美学平台，聚焦衣橱管理与穿搭推荐。通过 RAG 知识检索、ReAct Agent 引擎和协同过滤推荐，为用户提供智能化的美学服务。

## 技术栈

**后端**
- Java 17 + Spring Boot 3.2 + Spring AI
- LLM: Qwen (通义千问) via DashScope API
- RAG: Milvus 向量数据库 + bge-large-zh 嵌入模型
- 数据库: PostgreSQL + Redis
- 认证: Spring Security + JWT

**前端**
- Vue 3 + TypeScript + Vite
- Tailwind CSS + Pinia + Lucide Icons
- SSE 流式对话

## 系统架构

```
┌──────────────────────────────────┐
│        Vue 3 前端 (SPA)          │
│   Tailwind + Pinia + SSE        │
└──────────────┬───────────────────┘
               │ REST / SSE
               ▼
┌──────────────────────────────────┐
│     Spring Boot 3.2 API 层       │
│  Chat  Wardrobe  Auth  RAG  ... │
└──────────────┬───────────────────┘
               │
               ▼
┌──────────────────────────────────┐
│       Spring AI Agent 层         │
│  ┌────────────┐ ┌──────────────┐│
│  │ ReAct 引擎  │ │ Tool Calling ││
│  │ 状态机编排   │ │ 插件化扩展   ││
│  └────────────┘ └──────────────┘│
│  ┌────────────────────────────┐ │
│  │  RAG 检索 (Milvus + Rerank) │ │
│  └────────────────────────────┘ │
└──────────────┬───────────────────┘
               │
               ▼
┌──────────────────────────────────┐
│            数据层                 │
│  PostgreSQL   Redis   Milvus    │
└──────────────────────────────────┘
```

## 核心功能

| 模块 | 功能 |
|------|------|
| AI 对话 | 多轮对话、SSE 流式输出、ReAct Agent 自主推理 |
| 衣橱管理 | 衣物录入/分类/标签、搭配推荐 |
| RAG 知识库 | 美学知识检索、向量相似度搜索、Rerank 重排序 |
| 个性化推荐 | 协同过滤 + 内容特征融合 (CFALR) |
| 用户系统 | JWT 认证、用户画像、行为追踪 |

## 项目结构

```
aura-project/
├── aura-backend/               # Spring Boot 后端
│   └── src/main/java/com/aura/
│       ├── controller/         # API 接口层
│       ├── service/            # 业务逻辑层
│       ├── model/              # 实体 & DTO
│       ├── repository/         # 数据访问层
│       ├── plugin/             # Agent 插件系统
│       ├── rag/                # RAG 检索服务
│       ├── react/              # ReAct 推理引擎
│       ├── cf/                 # 协同过滤 + 融合
│       ├── security/           # JWT & 认证
│       └── config/             # 配置类
├── aura-frontend/              # Vue 3 前端
│   └── src/
│       ├── api/                # API 请求封装
│       ├── components/         # 组件
│       ├── views/              # 页面
│       ├── stores/             # Pinia 状态管理
│       └── router/             # 路由
├── docker-compose.yml          # 一键启动基础设施
└── init-db/                    # 数据库初始化脚本
```

## 快速开始

### 1. 启动基础设施

```bash
docker-compose up -d
```

启动 PostgreSQL (5432)、Redis (6379)、Milvus (19530)。

### 2. 配置环境变量

复制 `.env.example` 为 `.env`，填入你的 DashScope API Key：

```bash
cp .env.example .env
```

### 3. 启动后端

```bash
cd aura-backend
mvn spring-boot:run
```

### 4. 启动前端

```bash
cd aura-frontend
npm install
npm run dev
```

访问 http://localhost:3000

## 环境要求

- JDK 17+
- Node.js 18+
- Docker & Docker Compose
- DashScope API Key (通义千问)

## License

MIT
