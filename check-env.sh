#!/bin/bash

# Aura 环境检查脚本

echo "==========================================="
echo "   🔍 Aura 环境检查"
echo "==========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查结果
PASS=0
FAIL=0

# 检查函数
check_command() {
    local cmd=$1
    local name=$2
    local version_cmd=$3
    
    if command -v $cmd &> /dev/null; then
        echo -e "${GREEN}✓${NC} $name 已安装"
        if [ ! -z "$version_cmd" ]; then
            version=$($version_cmd 2>&1 | head -1)
            echo "  版本: $version"
        fi
        ((PASS++))
    else
        echo -e "${RED}✗${NC} $name 未安装"
        ((FAIL++))
    fi
}

# 检查端口
check_port() {
    local port=$1
    local name=$2
    
    if ss -tlnp | grep -q ":$port "; then
        echo -e "${GREEN}✓${NC} $name 端口 $port 已占用（服务可能运行中）"
        ((PASS++))
    else
        echo -e "${YELLOW}⚠${NC} $name 端口 $port 未占用（服务未启动）"
    fi
}

# ==================== 检查基础工具 ====================
echo "📦 基础工具检查："
check_command "java" "Java" "java -version 2>&1"
check_command "mvn" "Maven" "mvn -version 2>&1 | head -1"
check_command "docker" "Docker" "docker --version"
check_command "docker-compose" "Docker Compose" "docker-compose --version"
echo ""

# ==================== 检查服务端口 ====================
echo "🔌 服务端口检查："
check_port 5432 "PostgreSQL"
check_port 6379 "Redis"
check_port 19530 "Milvus"
check_port 8080 "Aura Backend"
echo ""

# ==================== 检查 Docker 服务 ====================
echo "🐳 Docker 服务检查："
if command -v docker &> /dev/null; then
    if docker info &> /dev/null; then
        echo -e "${GREEN}✓${NC} Docker 服务运行中"
        ((PASS++))
        
        # 检查容器状态
        echo ""
        echo "📦 Docker 容器状态："
        docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "(aura-|NAMES)" || echo "  无 Aura 相关容器运行"
    else
        echo -e "${RED}✗${NC} Docker 服务未运行"
        ((FAIL++))
    fi
else
    echo -e "${RED}✗${NC} Docker 未安装"
    ((FAIL++))
fi
echo ""

# ==================== 检查环境变量 ====================
echo "🔧 环境变量检查："
if [ ! -z "$QWEN_API_KEY" ]; then
    echo -e "${GREEN}✓${NC} QWEN_API_KEY 已设置"
    ((PASS++))
else
    echo -e "${YELLOW}⚠${NC} QWEN_API_KEY 未设置（可选）"
fi
echo ""

# ==================== 检查项目文件 ====================
echo "📁 项目文件检查："
if [ -f "docker-compose.yml" ]; then
    echo -e "${GREEN}✓${NC} docker-compose.yml 存在"
    ((PASS++))
else
    echo -e "${RED}✗${NC} docker-compose.yml 不存在"
    ((FAIL++))
fi

if [ -f "aura-backend/pom.xml" ]; then
    echo -e "${GREEN}✓${NC} pom.xml 存在"
    ((PASS++))
else
    echo -e "${RED}✗${NC} pom.xml 不存在"
    ((FAIL++))
fi
echo ""

# ==================== 总结 ====================
echo "==========================================="
echo "   📊 检查结果"
echo "==========================================="
echo -e "${GREEN}通过: $PASS${NC}"
echo -e "${RED}失败: $FAIL${NC}"
echo ""

if [ $FAIL -eq 0 ]; then
    echo -e "${GREEN}✅ 环境检查通过！可以开始开发。${NC}"
    echo ""
    echo "下一步："
    echo "  1. 启动基础服务: docker compose up -d"
    echo "  2. 构建后端项目: cd aura-backend && mvn clean install"
    echo "  3. 启动后端服务: mvn spring-boot:run"
else
    echo -e "${RED}❌ 环境检查失败，请先安装缺失的组件。${NC}"
    echo ""
    echo "安装指南："
    echo "  - Java 17: https://adoptium.net/"
    echo "  - Maven 3.9: https://maven.apache.org/download.cgi"
    echo "  - Docker: https://docs.docker.com/get-docker/"
fi

echo ""
