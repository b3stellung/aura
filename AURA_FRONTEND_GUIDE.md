# Aura 前端重构指导文档

> 设计语言：Instagram暖白 + X(Twitter)极简 | 布局：MaaS风格 | 无emoji，全Lucide图标

---

## 1. 设计语言定义

### 1.1 色彩系统（IG + X混合）

```typescript
// tailwind.config.ts 扩展
colors: {
  // 背景层
  bg: {
    primary: '#FAFAFA',      // IG暖白 - 页面背景
    secondary: '#FFFFFF',    // 纯白 - 卡片/面板
    tertiary: '#F5F5F5',     // 浅灰 - 次级区域
    hover: '#EFEFEF',        // 悬停态
    dark: '#15202B',         // X深蓝 - 代码/终端区
  },
  
  // 文字层
  text: {
    primary: '#262626',      // IG近黑 - 主文字
    secondary: '#8E8E93',    // IG灰 - 次文字
    tertiary: '#C7C7CC',     // IG浅灰 - 占位符
    inverse: '#FFFFFF',      // 深色背景上的文字
    link: '#1D9BF0',         // X蓝 - 链接/交互
  },
  
  // 边框
  border: {
    DEFAULT: '#DBDBDB',      // IG标准边框
    light: '#EFEFEF',        // 轻边框
    focus: '#1D9BF0',        // 聚焦态 X蓝
  },
  
  // 品牌色
  brand: {
    primary: '#1D9BF0',      // X蓝 - 主CTA
    hover: '#1A8CD8',        // 悬停态
    gradient: 'linear-gradient(45deg, #F58529, #DD2A7B, #8134AF, #5B51D8)',  // IG渐变 - 特殊强调
  },
  
  // 语义色
  semantic: {
    success: '#34C759',
    error: '#ED4956',        // IG红
    warning: '#FF9F0A',
    info: '#1D9BF0',
  },
}
```

### 1.2 字体系统

```typescript
// 仅用400和600两个字重
fontFamily: {
  sans: ['-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'Roboto', 
         'Helvetica Neue', 'PingFang SC', 'Microsoft YaHei', 'sans-serif'],
  mono: ['ui-monospace', 'SFMono-Regular', 'Menlo', 'Monaco', 'Consolas', 'monospace'],
},

// 字号规范
fontSize: {
  'xs': '12px',      // 元数据、时间戳
  'sm': '13px',      // 次要文字
  'base': '14px',    // 正文（紧凑）
  'lg': '16px',      // 卡片标题
  'xl': '20px',      // 区域标题
  '2xl': '24px',     // 页面标题
}
```

### 1.3 间距与圆角

```typescript
// 4px基础单位
spacing: {
  '1': '4px',
  '2': '8px',
  '3': '12px',
  '4': '16px',
  '6': '24px',
  '8': '32px',
},

// 圆角规范
borderRadius: {
  'btn': '8px',       // 按钮
  'card': '12px',     // 卡片
  'input': '6px',     // 输入框
  'pill': '100px',    // 胶囊
}
```

---

## 2. 页面结构规划

### 2.1 整体布局（MaaS风格）

```
┌─────────────────────────────────────────────────────────────┐
│  Sidebar (240px)  │         Header (52px)                   │
│                   ├─────────────────────────────────────────┤
│  ┌─────────────┐  │                                         │
│  │   Logo      │  │         Main Content                    │
│  ├─────────────┤  │         (router-view)                   │
│  │   Nav Menu  │  │                                         │
│  │   - 首页    │  │         按路由切换：                      │
│  │   - Agent   │  │         - 首页仪表盘                     │
│  │   - 记录    │  │         - Agent Sandbox                 │
│  │   - 设置    │  │         - 历史记录                       │
│  ├─────────────┤  │         - 设置                          │
│  │   User Info │  │                                         │
│  └─────────────┘  │                                         │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 路由规划

```typescript
// router/index.ts
const routes = [
  // 认证页面（无侧边栏）
  {
    path: '/auth',
    component: () => import('@/layouts/AuthLayout.vue'),
    children: [
      { path: 'login', component: () => import('@/views/auth/LoginView.vue') },
      { path: 'register', component: () => import('@/views/auth/RegisterView.vue') },
    ]
  },
  
  // 主应用（带侧边栏）
  {
    path: '/',
    component: () => import('@/layouts/AppLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', component: () => import('@/views/home/HomeView.vue') },
      { 
        path: 'sandbox', 
        component: () => import('@/views/sandbox/SandboxView.vue'),
        children: [
          { path: '', component: () => import('@/views/sandbox/AgentListView.vue') },
          { path: ':id', component: () => import('@/views/sandbox/AgentWorkspace.vue') },
        ]
      },
      { path: 'history', component: () => import('@/views/history/HistoryView.vue') },
      { path: 'settings', component: () => import('@/views/settings/SettingsView.vue') },
    ]
  },
]
```

---

## 3. 核心页面详细设计

### 3.1 Agent Sandbox（核心功能）

这是整个应用的重点，类似ChatGPT/Claude的对话界面，但支持多Agent编排。

#### 3.1.1 Sandbox布局

```
┌──────────────────────────────────────────────────────────────────┐
│  Sandbox Header                                                  │
│  [Agent选择器] [模型选择] [参数设置] [保存] [清除]                 │
├────────────────────────────────┬─────────────────────────────────┤
│                                │                                 │
│   Agent工作区 (左侧70%)        │   配置面板 (右侧30%)             │
│                                │                                 │
│   ┌────────────────────────┐  │   ┌─────────────────────────┐   │
│   │ 对话记录                │  │   │ System Prompt           │   │
│   │                        │  │   │ [编辑区]                │   │
│   │ User: xxx              │  │   ├─────────────────────────┤   │
│   │                        │  │   │ Temperature             │   │
│   │ Assistant: xxx         │  │   │ [=====|====] 0.7        │   │
│   │                        │  │   ├─────────────────────────┤   │
│   │ [工具调用折叠]          │  │   │ Max Tokens              │   │
│   │                        │  │   │ [4096]                  │   │
│   └────────────────────────┘  │   ├─────────────────────────┤   │
│                                │   │ Tools                    │   │
│   ┌────────────────────────┐  │   │ [x] web_search          │   │
│   │ 输入框                  │  │   │ [x] terminal            │   │
│   │ [输入消息...]  [发送]   │  │   │ [ ] file                │   │
│   └────────────────────────┘  │   └─────────────────────────┘   │
│                                │                                 │
└────────────────────────────────┴─────────────────────────────────┘
```

#### 3.1.2 核心组件

```typescript
// types/sandbox.ts
interface Conversation {
  id: string
  title: string
  agentId: string
  messages: Message[]
  createdAt: string
  updatedAt: string
}

interface Message {
  id: string
  role: 'user' | 'assistant' | 'system' | 'tool'
  content: string
  toolCalls?: ToolCall[]
  toolResult?: ToolResult
  timestamp: string
}

interface AgentConfig {
  id: string
  name: string
  model: string
  systemPrompt: string
  temperature: number
  maxTokens: number
  tools: string[]
  icon: string  // Lucide图标名
}
```

#### 3.1.3 关键功能

1. **多Agent切换** - 侧边栏显示Agent列表，点击切换工作区
2. **对话流** - 支持Markdown渲染、代码高亮、工具调用展示
3. **实时流式响应** - SSE/WebSocket流式输出
4. **工具调用折叠** - 工具执行过程可展开/折叠
5. **配置面板** - 实时调整System Prompt、Temperature等参数
6. **保存/导出** - 对话可保存为记录，支持导出

### 3.2 历史记录页面

```
┌─────────────────────────────────────────────────────────────┐
│  搜索框 [搜索对话...]                    [筛选▼] [新建对话]  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  今天                                                       │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ [Agent图标] 穿搭推荐助手                              │   │
│  │ 帮我搭配一套适合面试的商务休闲装...                    │   │
│  │ 2小时前 · 12条消息                                    │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  昨天                                                       │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ [Agent图标] 代码助手                                  │   │
│  │ 帮我写一个Vue3的自定义hook...                         │   │
│  │ 1天前 · 8条消息                                      │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 3.3 首页仪表盘

```
┌─────────────────────────────────────────────────────────────┐
│  欢迎回来，刘畅                                              │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────┐  ┌─────────────────┐                  │
│  │ 快速开始         │  │ 最近对话         │                  │
│  │                 │  │                 │                  │
│  │ [新建对话]      │  │ · 穿搭推荐       │                  │
│  │ [浏览Agent]     │  │ · 代码助手       │                  │
│  │ [查看记录]      │  │ · 学习伙伴       │                  │
│  └─────────────────┘  └─────────────────┘                  │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ 推荐Agent                                            │   │
│  │                                                     │   │
│  │ [图标] 穿搭助手    [图标] 代码助手    [图标] 学习伙伴  │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 4. 组件规范

### 4.1 按钮

```vue
<!-- Primary Button (X蓝) -->
<button class="bg-brand-primary hover:bg-brand-hover text-white 
               font-semibold text-sm px-4 py-2 rounded-btn transition-colors">
  <component :is="icon" :size="16" class="mr-2" />
  <slot />
</button>

<!-- Secondary Button (IG风格) -->
<button class="bg-white hover:bg-bg-hover text-text-primary 
               font-semibold text-sm px-4 py-2 rounded-btn 
               border border-border transition-colors">
  <slot />
</button>

<!-- Ghost Button -->
<button class="bg-transparent hover:bg-bg-hover text-text-secondary 
               text-sm px-3 py-1.5 rounded-btn transition-colors">
  <slot />
</button>
```

### 4.2 卡片

```vue
<div class="bg-white rounded-card border border-border p-4">
  <div class="flex items-center justify-between mb-3">
    <h3 class="font-semibold text-base text-text-primary">{{ title }}</h3>
    <button class="text-text-secondary hover:text-text-primary">
      <MoreHorizontal :size="16" />
    </button>
  </div>
  <slot />
</div>
```

### 4.3 输入框

```vue
<div class="relative">
  <Search :size="16" class="absolute left-3 top-1/2 -translate-y-1/2 text-text-tertiary" />
  <input 
    class="w-full bg-bg-tertiary border-none rounded-input py-2.5 pl-10 pr-4 
           text-sm text-text-primary placeholder:text-text-tertiary
           focus:bg-white focus:ring-2 focus:ring-brand-primary"
    placeholder="搜索..."
  />
</div>
```

### 4.4 消息气泡

```vue
<!-- User Message -->
<div class="flex justify-end mb-4">
  <div class="max-w-[70%] bg-brand-primary text-white rounded-card px-4 py-2.5">
    <p class="text-sm">{{ content }}</p>
  </div>
</div>

<!-- Assistant Message -->
<div class="flex gap-3 mb-4">
  <div class="w-8 h-8 rounded-full bg-bg-tertiary flex items-center justify-center flex-shrink-0">
    <Bot :size="16" class="text-text-secondary" />
  </div>
  <div class="max-w-[70%] bg-bg-secondary border border-border rounded-card px-4 py-2.5">
    <div class="text-sm text-text-primary prose-sm" v-html="renderMarkdown(content)" />
  </div>
</div>
```

---

## 5. 目录结构

```
aura-frontend/
├── public/
├── src/
│   ├── api/                    # API请求封装
│   │   ├── auth.ts
│   │   ├── agent.ts
│   │   ├── conversation.ts
│   │   └── user.ts
│   │
│   ├── assets/                 # 静态资源
│   │   └── styles/
│   │       └── global.css
│   │
│   ├── components/             # 通用组件
│   │   ├── ui/                 # 基础UI组件
│   │   │   ├── Button.vue
│   │   │   ├── Input.vue
│   │   │   ├── Card.vue
│   │   │   ├── Avatar.vue
│   │   │   └── Badge.vue
│   │   │
│   │   ├── layout/             # 布局组件
│   │   │   ├── Sidebar.vue
│   │   │   ├── Header.vue
│   │   │   ├── AppLayout.vue
│   │   │   └── AuthLayout.vue
│   │   │
│   │   └── sandbox/            # Sandbox专用组件
│   │       ├── ChatMessage.vue
│   │       ├── ChatInput.vue
│   │       ├── ToolCall.vue
│   │       ├── ConfigPanel.vue
│   │       └── AgentSelector.vue
│   │
│   ├── composables/            # 组合式函数
│   │   ├── useAuth.ts
│   │   ├── useChat.ts
│   │   └── useAgent.ts
│   │
│   ├── router/                 # 路由配置
│   │   └── index.ts
│   │
│   ├── stores/                 # Pinia状态管理
│   │   ├── auth.ts
│   │   ├── agent.ts
│   │   ├── conversation.ts
│   │   └── ui.ts
│   │
│   ├── types/                  # TypeScript类型
│   │   ├── auth.ts
│   │   ├── agent.ts
│   │   └── conversation.ts
│   │
│   ├── utils/                  # 工具函数
│   │   ├── markdown.ts
│   │   ├── token.ts
│   │   └── format.ts
│   │
│   └── views/                  # 页面视图
│       ├── auth/
│       │   ├── LoginView.vue
│       │   └── RegisterView.vue
│       ├── home/
│       │   └── HomeView.vue
│       ├── sandbox/
│       │   ├── SandboxView.vue
│       │   ├── AgentListView.vue
│       │   └── AgentWorkspace.vue
│       ├── history/
│       │   └── HistoryView.vue
│       └── settings/
│           └── SettingsView.vue
│
├── index.html
├── package.json
├── tailwind.config.ts
├── tsconfig.json
└── vite.config.ts
```

---

## 6. 开发步骤

### Phase 1: 基础搭建（Day 1）
- [x] 项目初始化（Vite + Vue3 + TS + Tailwind）
- [ ] 配置设计系统（colors, fonts, spacing）
- [ ] 实现AppLayout（Sidebar + Header）
- [ ] 路由配置

### Phase 2: 核心Sandbox（Day 2-3）
- [ ] AgentWorkspace组件
- [ ] ChatMessage消息组件
- [ ] ChatInput输入组件
- [ ] 流式响应（SSE）
- [ ] 工具调用展示

### Phase 3: 数据层（Day 4）
- [ ] Pinia stores（agent, conversation, auth）
- [ ] API封装（登录/注册/对话/Agent）
- [ ] 本地持久化（localStorage）

### Phase 4: 完善功能（Day 5）
- [ ] 历史记录页面
- [ ] 首页仪表盘
- [ ] 设置页面
- [ ] 响应式适配

---

## 7. 关键实现细节

### 7.1 流式响应处理

```typescript
// composables/useChat.ts
export function useChat() {
  const messages = ref<Message[]>([])
  const isStreaming = ref(false)
  
  async function sendMessage(content: string) {
    // 添加用户消息
    messages.value.push({
      id: generateId(),
      role: 'user',
      content,
      timestamp: new Date().toISOString(),
    })
    
    // 创建助手消息占位
    const assistantMessage: Message = {
      id: generateId(),
      role: 'assistant',
      content: '',
      timestamp: new Date().toISOString(),
    }
    messages.value.push(assistantMessage)
    
    // 开始流式请求
    isStreaming.value = true
    const eventSource = new EventSource(`/api/chat/stream?message=${encodeURIComponent(content)}`)
    
    eventSource.onmessage = (event) => {
      const data = JSON.parse(event.data)
      if (data.type === 'content') {
        assistantMessage.content += data.content
      } else if (data.type === 'tool_call') {
        assistantMessage.toolCalls = assistantMessage.toolCalls || []
        assistantMessage.toolCalls.push(data.toolCall)
      }
    }
    
    eventSource.onerror = () => {
      isStreaming.value = false
      eventSource.close()
    }
  }
  
  return { messages, sendMessage, isStreaming }
}
```

### 7.2 Agent配置持久化

```typescript
// stores/agent.ts
export const useAgentStore = defineStore('agent', () => {
  const agents = ref<AgentConfig[]>([])
  const currentAgent = ref<AgentConfig | null>(null)
  
  // 从localStorage加载
  function loadAgents() {
    const saved = localStorage.getItem('aura-agents')
    if (saved) {
      agents.value = JSON.parse(saved)
    } else {
      // 默认Agent
      agents.value = [
        {
          id: 'general',
          name: '通用助手',
          model: 'qwen-plus',
          systemPrompt: '你是一个有用的AI助手...',
          temperature: 0.7,
          maxTokens: 4096,
          tools: ['web_search', 'terminal'],
          icon: 'Bot',
        },
      ]
    }
  }
  
  // 保存到localStorage
  function saveAgents() {
    localStorage.setItem('aura-agents', JSON.stringify(agents.value))
  }
  
  return { agents, currentAgent, loadAgents, saveAgents }
})
```

---

## 8. 设计禁忌

1. **不要emoji** - 所有图标用Lucide
2. **不要底部TabBar** - 桌面端用侧边栏
3. **不要过多样式** - 保持IG/X的克制感
4. **不要纯黑文字** - 用#262626
5. **不要重阴影** - 用边框定义卡片
6. **不要装饰字体** - 系统字体栈
7. **不要过多颜色** - 黑白灰 + 一个品牌蓝

---

## 9. Lucide图标使用规范

```vue
<script setup>
import { 
  Bot,        // Agent
  MessageSquare,  // 对话
  History,    // 历史
  Settings,   // 设置
  Search,     // 搜索
  Send,       // 发送
  Copy,       // 复制
  Check,      // 完成
  X,          // 关闭
  ChevronDown, // 下拉
  MoreHorizontal, // 更多
  User,       // 用户
  LogOut,     // 退出
  Sun,        // 亮色模式
  Moon,       // 暗色模式
} from 'lucide-vue-next'
</script>

<template>
  <!-- 使用示例 -->
  <Bot :size="16" :stroke-width="2" />
</template>
```

---

*文档版本: v1.0 | 更新时间: 2026-08-05*
