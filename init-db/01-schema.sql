-- Aura 数据库初始化脚本

-- 启用 UUID 扩展
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==================== 用户表 ====================
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    avatar_url TEXT,
    subscription_tier VARCHAR(20) DEFAULT 'free' CHECK (subscription_tier IN ('free', 'pro', 'premium')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- ==================== 用户美学偏好表 ====================
CREATE TABLE IF NOT EXISTS user_aesthetic_profile (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    skin_tone VARCHAR(50),          -- 冷白皮/暖黄皮/小麦色/中性皮
    preferred_styles TEXT[],        -- 极简/复古/法式/新中式/街头
    avoid_elements TEXT[],          -- 讨厌的元素
    favorite_brands TEXT[],
    favorite_colors TEXT[],
    body_type VARCHAR(50),          -- 体型标签
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ==================== Agent 会话表 ====================
CREATE TABLE IF NOT EXISTS agent_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_type VARCHAR(50) CHECK (session_type IN ('outfit_recommendation', 'makeup_tutorial', 'style_analysis')),
    input_text TEXT,
    input_images TEXT[],
    react_trace JSONB,              -- 完整 ReAct 轨迹
    final_output JSONB,             -- 最终返回的结构化 JSON
    plugins_used TEXT[],            -- 本次使用的插件列表
    token_consumed INT DEFAULT 0,
    latency_ms INT,                 -- 总耗时
    status VARCHAR(20) DEFAULT 'running' CHECK (status IN ('running', 'success', 'failed', 'timeout')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_sessions_user ON agent_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_sessions_status ON agent_sessions(status);
CREATE INDEX IF NOT EXISTS idx_sessions_created ON agent_sessions(created_at);

-- ==================== 插件注册表 ====================
CREATE TABLE IF NOT EXISTS plugin_registry (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    plugin_name VARCHAR(100) UNIQUE NOT NULL,
    plugin_type VARCHAR(50) NOT NULL CHECK (plugin_type IN ('tool', 'memory', 'model')),
    description TEXT,
    tool_schema JSONB,              -- OpenAPI 格式的 Tool 定义
    endpoint_url TEXT,              -- 如果是远程插件
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ==================== 用户插件订阅表 ====================
CREATE TABLE IF NOT EXISTS user_plugin_subscriptions (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plugin_id UUID NOT NULL REFERENCES plugin_registry(id) ON DELETE CASCADE,
    is_enabled BOOLEAN DEFAULT true,
    subscribed_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    PRIMARY KEY (user_id, plugin_id)
);

-- ==================== 美妆产品表 ====================
CREATE TABLE IF NOT EXISTS beauty_products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL,
    brand VARCHAR(100),
    product_name VARCHAR(200),
    category VARCHAR(50) NOT NULL,  -- 口红/眼影/粉底/腮红/香水
    color_family VARCHAR(50),       -- 红棕色/大地色/裸粉色/正红色
    finish_type VARCHAR(50),        -- 哑光/水光/珠光/丝绒
    attributes JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_beauty_user ON beauty_products(user_id);
CREATE INDEX IF NOT EXISTS idx_beauty_category ON beauty_products(category);

-- ==================== 更新时间触发器 ====================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_aesthetic_profile_updated_at BEFORE UPDATE ON user_aesthetic_profile
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ==================== 插入默认插件 ====================
INSERT INTO plugin_registry (plugin_name, plugin_type, description, tool_schema) VALUES
('ClothingAnalyzer', 'tool', '分析衣物图片，提取颜色、材质、风格等属性', 
 '{"name": "ClothingAnalyzer", "description": "分析衣物图片", "parameters": {"image_url": "string"}}'::jsonb),
('RAGSearch', 'tool', '检索美学知识库和用户衣橱', 
 '{"name": "RAGSearch", "description": "检索相关穿搭信息", "parameters": {"query": "string", "kb_type": "string"}}'::jsonb),
('WeatherPlugin', 'tool', '查询天气信息，为穿搭推荐提供参考', 
 '{"name": "WeatherPlugin", "description": "查询天气", "parameters": {"location": "string"}}'::jsonb)
ON CONFLICT (plugin_name) DO NOTHING;

-- ==================== 完成提示 ====================
DO $$
BEGIN
    RAISE NOTICE '✅ Aura 数据库初始化完成！';
    RAISE NOTICE '📋 已创建表：users, user_aesthetic_profile, agent_sessions, plugin_registry, user_plugin_subscriptions, beauty_products';
    RAISE NOTICE '🔌 已插入默认插件：ClothingAnalyzer, RAGSearch, WeatherPlugin';
END $$;
