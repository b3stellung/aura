#!/bin/bash

# Aura 快速启动脚本

echo "==========================================="
echo "   🚀 Aura 快速启动"
echo "==========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查 Docker
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker 未安装，请先安装 Docker${NC}"
    exit 1
fi

# 检查 Docker 服务
if ! docker info &> /dev/null; then
    echo -e "${RED}❌ Docker 服务未运行，请先启动 Docker${NC}"
    exit 1
fi

# 检查 docker-compose.yml
if [ ! -f "docker-compose.yml" ]; then
    echo -e "${RED}❌ docker-compose.yml 不存在${NC}"
    exit 1
fi

echo -e "${GREEN}✓${NC} 环境检查通过"
echo ""

# ==================== 启动基础服务 ====================
echo "📦 启动基础服务..."
echo "  - PostgreSQL"
echo "  - Redis"
echo "  - Milvus"
echo ""

docker compose up -d

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 10

# 检查服务状态
echo ""
echo "📊 服务状态："
docker compose ps

echo ""

# ==================== 检查服务健康 ====================
echo "🏥 检查服务健康状态..."

# 检查 PostgreSQL
if docker compose exec -T postgres pg_isready -U aura -d aura_db &> /dev/null; then
    echo -e "${GREEN}✓${NC} PostgreSQL 就绪"
else
    echo -e "${RED}✗${NC} PostgreSQL 未就绪"
fi

# 检查 Redis
if docker compose exec -T redis redis-cli -a aura123 ping &> /dev/null; then
    echo -e "${GREEN}✓${NC} Redis 就绪"
else
    echo -e "${RED}✗${NC} Redis 未就绪"
fi

# 检查 Milvus
if curl -s http://localhost:9091/healthz &> /dev/null; then
    echo -e "${GREEN}✓${NC} Milvus 就绪"
else
    echo -e "${YELLOW}⚠${NC} Milvus 可能还在启动中（首次启动需要 1-2 分钟）"
fi

echo ""

# ==================== 显示访问信息 ====================
echo "==========================================="
echo "   🎯 服务访问信息"
echo "==========================================="
echo ""
echo "📊 数据库连接："
echo "  - PostgreSQL: localhost:5432"
echo "    - 数据库: aura_db"
echo "    - 用户: aura"
echo "    - 密码: aura123"
echo ""
echo "  - Redis: localhost:6379"
echo "    - 密码: aura123"
echo ""
echo "  - Milvus: localhost:19530"
echo ""
echo "🔧 管理界面："
echo "  - Milvus 管理: http://localhost:8000"
echo ""
echo "==========================================="
echo ""
echo -e "${GREEN}✅ 基础服务启动完成！${NC}"
echo ""
echo "下一步："
echo "  1. 构建后端项目: cd aura-backend && mvn clean install"
echo "  2. 启动后端服务: mvn spring-boot:run"
echo "  3. 测试 API: curl http://localhost:8080/api/v1/health"
echo ""
