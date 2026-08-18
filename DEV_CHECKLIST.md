# Aura 开发任务清单

## 第一阶段：基础设施搭建 (Week 1-2)

### Day 1-2：环境准备 ✅

- [x] 创建项目目录结构
- [x] 编写 docker-compose.yml
- [x] 创建数据库初始化脚本
- [x] 配置 Spring Boot 项目
- [x] 创建环境检查脚本

### Day 3：启动基础服务

```bash
# 1. 启动 Docker 服务
cd /mnt/c/Users/Acer/Desktop/aura-project
bash start-services.sh

# 2. 验证服务状态
docker compose ps

# 3. 检查端口
ss -tlnp | grep -E ":(5432|6379|19530)"
```

**验收标准**：
- [ ] PostgreSQL 可以连接
- [ ] Redis 可以连接
- [ ] Milvus 可以连接

### Day 4：申请 API Key

1. 访问 [阿里云百炼控制台](https://bailian.console.aliyun.com)
2. 开通"百炼大模型服务"
3. 创建 API Key
4. 复制 `.env.example` 为 `.env` 并填入 API Key

```bash
cp .env.example .env
# 编辑 .env 文件，填入 QWEN_API_KEY
```

**验收标准**：
- [ ] 成功获取 API Key
- [ ] `.env` 文件配置正确

### Day 5：构建后端项目

```bash
# 1. 进入后端目录
cd aura-backend

# 2. 构建项目
mvn clean install

# 3. 启动服务
mvn spring-boot:run
```

**验收标准**：
- [ ] Maven 构建成功
- [ ] 服务正常启动
- [ ] 访问 http://localhost:8080/api/v1/health 返回成功

### Day 6-7：技术验证

```bash
# 测试 Qwen3.7-Plus 调用
curl -X POST http://localhost:8080/api/v1/test/vision \
  -H "Content-Type: application/json" \
  -d '{"image_url": "https://example.com/cloth.jpg"}'
```

**验收标准**：
- [ ] Qwen3.7-Plus 能正确识别衣物图片
- [ ] 返回的 JSON 格式正确
- [ ] 识别准确率 > 85%

---

## 第二阶段：ReAct 引擎实现 (Week 3-4)

### Week 3：核心引擎

- [ ] 实现 ReActState 状态类
- [ ] 实现 AuraReActEngine 引擎
- [ ] 实现 Tool Calling 机制
- [ ] 实现 ClothingAnalyzerPlugin

### Week 4：流式输出

- [ ] 实现 SSE 流式接口
- [ ] 实现多路召回（文本向量 + 图像向量 + 标量过滤）
- [ ] 实现 RRF 融合排序
- [ ] 实现 Reranker 精排

**验收标准**：
- [ ] ReAct 循环正常执行
- [ ] 能正确调用插件
- [ ] SSE 流式输出正常
- [ ] 返回 Top-5 精准推荐

---

## 第三阶段：RAG 系统 (Week 5-6)

### Week 5：向量检索

- [ ] 实现 MilvusRetrievalService
- [ ] 实现向量存储（衣物 + 知识库）
- [ ] 实现多路召回检索
- [ ] 实现 RRF 融合

### Week 6：知识增强

- [ ] 构建美学知识库
- [ ] 实现 RAG 检索插件
- [ ] 实现语义缓存
- [ ] （可选）集成 CNN 微服务

**验收标准**：
- [ ] 检索准确率 > 80%
- [ ] 检索召回率 > 70%
- [ ] RAG 结果相关性高

---

## 第四阶段：前端开发 (Week 7-8)

### Week 7：基础框架

- [ ] 初始化 Next.js 项目
- [ ] 配置 Tailwind + shadcn/ui
- [ ] 实现流式聊天组件
- [ ] 实现 SSE 接收

### Week 8：完整界面

- [ ] 实现 Lookbook 卡片
- [ ] 实现 ReAct 解析面板
- [ ] 实现图片上传
- [ ] 完善 UI/UX

**验收标准**：
- [ ] 前端正常启动
- [ ] 流式聊天功能正常
- [ ] Lookbook 卡片渲染正确
- [ ] ReAct 解析面板展开/收起正常

---

## 常用命令

### Docker 操作

```bash
# 启动所有服务
docker compose up -d

# 停止所有服务
docker compose down

# 查看日志
docker compose logs -f [service_name]

# 重启服务
docker compose restart [service_name]
```

### Maven 操作

```bash
# 构建项目
mvn clean install

# 启动服务
mvn spring-boot:run

# 运行测试
mvn test

# 跳过测试构建
mvn clean install -DskipTests
```

### 数据库操作

```bash
# 连接 PostgreSQL
docker compose exec postgres psql -U aura -d aura_db

# 连接 Redis
docker compose exec redis redis-cli -a aura123

# 备份数据库
docker compose exec postgres pg_dump -U aura aura_db > backup.sql
```

---

## 故障排查

### 问题 1：Milvus 启动失败

```bash
# 检查日志
docker compose logs milvus

# 可能原因：etcd 或 minio 未就绪
# 解决：等待 1-2 分钟，或重启
docker compose restart milvus
```

### 问题 2：PostgreSQL 连接失败

```bash
# 检查容器状态
docker compose ps postgres

# 检查日志
docker compose logs postgres

# 可能原因：初始化脚本错误
# 解决：检查 init-db/01-schema.sql
```

### 问题 3：Spring Boot 启动失败

```bash
# 检查日志
tail -f logs/application.log

# 可能原因：数据库连接失败
# 解决：确认 Docker 服务已启动，检查 application.yml 配置
```

### 问题 4：Qwen3.7-Plus 调用失败

```bash
# 检查 API Key 配置
echo $QWEN_API_KEY

# 检查网络连接
curl -v https://dashscope.aliyuncs.com

# 可能原因：API Key 无效或余额不足
# 解决：登录阿里云控制台检查
```

---

## 下一步行动

1. **立即可做**：
   - 运行 `bash start-services.sh` 启动基础服务
   - 申请阿里云百炼 API Key

2. **今天完成**：
   - 验证所有服务正常运行
   - 构建并启动后端项目
   - 测试健康检查接口

3. **本周完成**：
   - 实现衣物分析插件原型
   - 测试 Qwen3.7-Plus 识别能力
   - 完成技术验证

---

**文档版本**：v1.0  
**最后更新**：2026-07-30  
**负责人**：开发团队
