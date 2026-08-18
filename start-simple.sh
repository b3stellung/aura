#!/bin/bash

# 启动单个 Docker 服务

cd /mnt/c/Users/Acer/Desktop/aura-project

echo "启动 PostgreSQL..."
docker run -d \
  --name aura-postgres \
  -e POSTGRES_DB=aura_db \
  -e POSTGRES_USER=aura \
  -e POSTGRES_PASSWORD=aura123 \
  -p 5432:5432 \
  pgvector/pgvector:pg15

echo "启动 Redis..."
docker run -d \
  --name aura-redis \
  -p 6379:6379 \
  redis:7-alpine \
  redis-server --requirepass aura123

echo "等待服务启动..."
sleep 10

echo "检查服务状态..."
docker ps | grep aura-

echo "测试 PostgreSQL 连接..."
docker exec aura-postgres pg_isready -U aura -d aura_db

echo "测试 Redis 连接..."
docker exec aura-redis redis-cli -a aura123 ping

echo "✅ 基础服务启动完成！"
