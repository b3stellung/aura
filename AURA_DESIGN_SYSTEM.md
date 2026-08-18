# Aura 前端设计系统 v2.0

> 参考: Vercel极简 + Notion暖色 + Cursor精致 | 设计语言: IG暖白 + X简洁 | 无emoji，全Lucide

---

## 设计哲学

Aura的设计融合三大设计系统的精华：

| 来源 | 借鉴要素 | 应用场景 |
|------|----------|----------|
| **Vercel** | shadow-as-border技术、极简黑白、压缩字间距 | 卡片边框、整体克制感 |
| **Notion** | 暖色调灰度、温暖质感、4层阴影栈 | 背景色、阴影层次 |
| **Cursor** | oklab边框、暖色背景、精致细节 | 交互状态、品牌温度 |
| **Instagram** | 紧凑排版、系统字体、图片驱动 | 正文字号、内容密度 |
| **X (Twitter)** | 品牌蓝、简洁布局、功能优先 | CTA按钮、信息流 |

---

## 1. 色彩系统

### 1.1 背景层 (Notion暖色系)

```css
--bg-primary: #FAFAFA;      /* IG暖白 - 页面主背景 */
--bg-secondary: #FFFFFF;    /* 纯白 - 卡片/面板 */
--bg-tertiary: #F6F5F4;     /* Notion暖灰 - 次级区域、交替区块 */
--bg-hover: #F2F1ED;        /* Cursor暖色 - 悬停态 */
--bg-active: #EBEAE5;       /* Cursor激活态 */
--bg-dark: #15202B;         /* X深蓝 - 代码/终端区 */
```

### 1.2 文字层 (IG + Vercel混合)

```css
--text-primary: #262626;      /* IG近黑 - 主文字 (不用纯黑) */
--text-secondary: #615D59;    /* Notion暖灰500 - 次文字 */
--text-tertiary: #A39E98;     /* Notion暖灰300 - 占位符/禁用 */
--text-inverse: #FFFFFF;      /* 深色背景上的文字 */
--text-link: #1D9BF0;         /* X蓝 - 链接/交互 */
```

### 1.3 边框层 (Vercel shadow-as-border + Notion whisper)

```css
/* 标准边框 - Notion whisper风格 */
--border-default: 1px solid rgba(0, 0, 0, 0.1);

/* Vercel shadow-as-border (推荐用于卡片) */
--border-shadow: 0 0 0 1px rgba(0, 0, 0, 0.08);

/* 多层卡片阴影栈 (Vercel风格) */
--shadow-card: 
  0 0 0 1px rgba(0, 0, 0, 0.08),
  0 2px 2px rgba(0, 0, 0, 0.04),
  0 0 0 1px #fafafa;  /* 内发光 */

/* 提升态阴影 (Notion 4层) */
--shadow-elevated:
  0 4px 18px rgba(0, 0, 0, 0.04),
  0 2px 7px rgba(0, 0, 0, 0.027),
  0 0.8px 2.9px rgba(0, 0, 0, 0.02),
  0 0.2px 1px rgba(0, 0, 0, 0.01);

/* 聚焦态 (Vercel蓝色) */
--focus-ring: 0 0 0 2px hsla(212, 100%, 48%, 1);
```

### 1.4 品牌色 (X蓝为主)

```css
--brand-primary: #1D9BF0;     /* X蓝 - 主CTA */
--brand-hover: #1A8CD8;       /* 悬停态 */
--brand-active: #1680C9;      /* 按下态 */
--brand-light: #E8F5FE;       /* 浅蓝背景 (badge/tag) */
--brand-text: #0A66C2;        /* 蓝色文字 (badge) */

/* IG渐变 - 仅用于特殊强调 (如故事圈、logo) */
--gradient-ig: linear-gradient(45deg, #F58529, #DD2A7B, #8134AF, #5B51D8);
```

### 1.5 语义色

```css
--success: #34C759;    /* IG绿 */
--error: #ED4956;      /* IG红 */
--warning: #FF9F0A;    /* 橙 */
--info: #1D9BF0;       /* X蓝 */
```

---

## 2. 字体系统

### 2.1 字体选择

```css
/* 主字体 - 系统字体栈 (IG风格，原生感) */
font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 
             'Helvetica Neue', 'PingFang SC', 'Microsoft YaHei', sans-serif;

/* 等宽字体 - 代码/技术标签 */
font-family: ui-monospace, SFFMono-Regular, Menlo, Monaco, Consolas, 
             'Liberation Mono', 'Courier New', monospace;
```

### 2.2 字号规范 (IG紧凑风格)

| 角色 | 字号 | 字重 | 行高 | 字间距 | 用途 |
|------|------|------|------|--------|------|
| Display | 28px | 600 | 1.2 | normal | 页面大标题 |
| H1 | 24px | 600 | 1.3 | normal | 区域标题 |
| H2 | 20px | 600 | 1.4 | normal | 卡片标题 |
| H3 | 16px | 600 | 1.4 | normal | 子标题 |
| Body | 14px | 400 | 1.5 | normal | 正文 (紧凑) |
| Caption | 12px | 400 | 1.4 | normal | 时间戳/元数据 |
| Small | 10px | 400 | 1.3 | normal | 徽章/标签 |

### 2.3 字重规则 (IG简洁)

```css
font-weight: 400;  /* 默认/正文 */
font-weight: 600;  /* 强调/标题/用户名 */
/* 不用 300, 500, 700 */
```

---

## 3. 间距与圆角

### 3.1 间距系统 (4px基础)

```css
--space-1: 4px;
--space-2: 8px;
--space-3: 12px;
--space-4: 16px;
--space-6: 24px;
--space-8: 32px;
--space-12: 48px;
--space-16: 64px;
```

### 3.2 圆角规范

```css
--radius-sm: 4px;      /* 小元素 */
--radius-md: 6px;      /* 输入框 */
--radius-lg: 8px;      /* 按钮 */
--radius-xl: 12px;     /* 卡片 */
--radius-pill: 9999px;  /* 胶囊/标签 */
--radius-full: 50%;    /* 圆形 */
```

---

## 4. 组件规范

### 4.1 按钮

#### Primary (X蓝)
```css
.btn-primary {
  background: var(--brand-primary);
  color: white;
  padding: 8px 16px;
  border-radius: var(--radius-lg);  /* 8px */
  font-size: 14px;
  font-weight: 600;
  transition: background 150ms ease;
}
.btn-primary:hover {
  background: var(--brand-hover);
}
```

#### Secondary (IG描边)
```css
.btn-secondary {
  background: var(--bg-secondary);
  color: var(--text-primary);
  padding: 7px 15px;
  border: 1px solid var(--border-default);  /* rgba(0,0,0,0.1) */
  border-radius: var(--radius-lg);
  font-size: 14px;
  font-weight: 600;
}
.btn-secondary:hover {
  background: var(--bg-hover);
}
```

#### Ghost (Vercel透明)
```css
.btn-ghost {
  background: transparent;
  color: var(--text-secondary);
  padding: 6px 12px;
  border-radius: var(--radius-lg);
  font-size: 14px;
}
.btn-ghost:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}
```

### 4.2 卡片 (Vercel shadow-as-border)

```css
.card {
  background: var(--bg-secondary);
  border-radius: var(--radius-xl);  /* 12px */
  box-shadow: var(--shadow-card);   /* Vercel多层阴影 */
  padding: 16px;
}

/* 提升态 (hover/focus) */
.card:hover {
  box-shadow: var(--shadow-elevated);
}
```

### 4.3 输入框 (IG风格)

```css
.input {
  background: var(--bg-tertiary);  /* #F6F5F4 */
  border: none;
  border-radius: var(--radius-md);  /* 6px */
  padding: 10px 12px;
  font-size: 14px;
  color: var(--text-primary);
}
.input:focus {
  background: var(--bg-secondary);
  box-shadow: 0 0 0 2px var(--brand-primary);
}
.input::placeholder {
  color: var(--text-tertiary);
}
```

### 4.4 搜索框 (IG风格)

```css
.search {
  background: #EFEFEF;  /* IG搜索背景 */
  border: none;
  border-radius: 8px;
  padding: 8px 16px;
  padding-left: 40px;  /* 图标空间 */
}
.search:focus {
  background: var(--bg-secondary);
  border: 1px solid var(--border-default);
}
```

### 4.5 Badge/Tag (胶囊)

```css
.badge {
  background: var(--brand-light);  /* #E8F5FE */
  color: var(--brand-text);        /* #0A66C2 */
  padding: 4px 8px;
  border-radius: var(--radius-pill);
  font-size: 12px;
  font-weight: 600;
}
```

---

## 5. 页面布局 (MaaS风格)

### 5.1 整体结构

```
┌─────────────────────────────────────────────────────────────┐
│  Sidebar (240px)  │         Header (52px)                   │
│                   ├─────────────────────────────────────────┤
│  ┌─────────────┐  │                                         │
│  │   Logo      │  │         Main Content                    │
│  ├─────────────┤  │         (router-view)                   │
│  │   Nav Menu  │  │                                         │
│  │   ───────   │  │         overflow-y-auto                 │
│  │   首页      │  │                                         │
│  │   Agent     │  │                                         │
│  │   记录      │  │                                         │
│  │   设置      │  │                                         │
│  ├─────────────┤  │                                         │
│  │   User      │  │                                         │
│  └─────────────┘  │                                         │
└─────────────────────────────────────────────────────────────┘
```

### 5.2 Sidebar.vue

```vue
<template>
  <aside class="w-[240px] h-screen bg-bg-secondary border-r border-border flex flex-col flex-shrink-0">
    <!-- Logo -->
    <div class="h-[52px] flex items-center px-5 border-b border-border">
      <h1 class="text-[17px] font-semibold text-text-primary">Aura</h1>
    </div>

    <!-- Navigation -->
    <nav class="flex-1 px-3 py-3">
      <button v-for="item in navItems" :key="item.path"
        class="w-full flex items-center gap-3 h-9 px-3 rounded-[8px] text-[13px]"
        :class="isActive(item.path) 
          ? 'bg-bg-hover text-text-primary' 
          : 'text-text-secondary hover:bg-bg-hover'">
        <component :is="item.icon" :size="17" :stroke-width="2" />
        <span>{{ item.label }}</span>
      </button>
    </nav>

    <!-- User Section -->
    <div class="border-t border-border p-3">
      <div class="flex items-center gap-3 px-3 py-2">
        <div class="w-8 h-8 rounded-full bg-brand-light flex items-center justify-center">
          <User :size="16" class="text-brand-primary" />
        </div>
        <div>
          <p class="text-[13px] font-semibold text-text-primary">刘畅</p>
          <p class="text-[11px] text-text-tertiary">249970619</p>
        </div>
      </div>
    </div>
  </aside>
</template>
```

### 5.3 Header.vue

```vue
<template>
  <header class="h-[52px] border-b border-border bg-bg-secondary flex items-center px-6">
    <!-- Page Title -->
    <h2 class="text-sm font-semibold text-text-primary">{{ pageTitle }}</h2>

    <!-- Search Bar (居中) -->
    <div class="flex-1 max-w-md mx-8">
      <div class="relative">
        <Search :size="15" class="absolute left-3 top-1/2 -translate-y-1/2 text-text-tertiary" />
        <input 
          class="w-full h-8 pl-9 pr-3 bg-bg-tertiary rounded-[8px] text-sm 
                 placeholder:text-text-tertiary focus:bg-bg-secondary focus:shadow-[0_0_0_2px_var(--brand-primary)]"
          placeholder="搜索..." 
        />
        <kbd class="absolute right-3 top-1/2 -translate-y-1/2 text-[11px] text-text-tertiary 
                    px-1.5 py-0.5 bg-bg-hover rounded-[4px]">⌘K</kbd>
      </div>
    </div>

    <!-- Actions -->
    <div class="flex items-center gap-1">
      <button class="p-2 rounded-[8px] hover:bg-bg-hover text-text-secondary">
        <Bell :size="17" :stroke-width="2" />
      </button>
      <button class="p-2 rounded-[8px] hover:bg-bg-hover text-text-secondary">
        <Settings :size="17" :stroke-width="2" />
      </button>
    </div>
  </header>
</template>
```

---

## 6. Agent Sandbox 设计

### 6.1 Sandbox布局 (Claude/ChatGPT风格)

```
┌──────────────────────────────────────────────────────────────────┐
│  Sandbox Header                                                  │
│  [Agent名称] [模型: qwen-plus] [Temperature: 0.7] [保存] [清除]  │
├────────────────────────────────┬─────────────────────────────────┤
│                                │                                 │
│   对话区 (左侧 70%)            │   配置面板 (右侧 30%)            │
│                                │                                 │
│   ┌────────────────────────┐  │   ┌─────────────────────────┐   │
│   │                        │  │   │ System Prompt           │   │
│   │   [User] 消息          │  │   │ ┌─────────────────────┐ │   │
│   │                        │  │   │ │                     │ │   │
│   │   [Assistant] 回复     │  │   │ │  编辑区域...        │ │   │
│   │                        │  │   │ │                     │ │   │
│   │   [工具调用] 可折叠    │  │   │ └─────────────────────┘ │   │
│   │                        │  │   ├─────────────────────────┤   │
│   └────────────────────────┘  │   │ Parameters              │   │
│                                │   │ Temperature: [===|==]   │   │
│   ┌────────────────────────┐  │   │ Max Tokens: [4096]      │   │
│   │ [输入消息...] [发送]   │  │   ├─────────────────────────┤   │
│   └────────────────────────┘  │   │ Tools                   │   │
│                                │   │ [x] web_search          │   │
│                                │   │ [x] terminal            │   │
│                                │   │ [ ] file                │   │
│                                │   └─────────────────────────┘   │
└────────────────────────────────┴─────────────────────────────────┘
```

### 6.2 消息气泡

#### User消息 (右对齐，X蓝)
```vue
<div class="flex justify-end mb-4">
  <div class="max-w-[70%] bg-brand-primary text-white rounded-[12px] px-4 py-2.5">
    <p class="text-[14px] leading-[1.5]">{{ content }}</p>
  </div>
</div>
```

#### Assistant消息 (左对齐，白底边框)
```vue
<div class="flex gap-3 mb-4">
  <div class="w-8 h-8 rounded-full bg-bg-tertiary flex items-center justify-center flex-shrink-0">
    <Bot :size="16" :stroke-width="2" class="text-text-secondary" />
  </div>
  <div class="max-w-[70%] bg-bg-secondary rounded-[12px] px-4 py-2.5" 
       style="box-shadow: 0 0 0 1px rgba(0,0,0,0.08)">
    <div class="text-[14px] text-text-primary leading-[1.5] prose-sm" 
         v-html="renderMarkdown(content)" />
  </div>
</div>
```

#### 工具调用 (可折叠)
```vue
<div class="mb-3 ml-11">
  <button @click="expanded = !expanded" 
          class="flex items-center gap-2 text-[12px] text-text-secondary hover:text-text-primary">
    <ChevronRight :size="14" :class="{ 'rotate-90': expanded }" />
    <Wrench :size="14" />
    <span>调用工具: {{ toolName }}</span>
  </button>
  <div v-if="expanded" class="mt-2 p-3 bg-bg-dark rounded-[8px] text-[12px] font-mono text-text-inverse overflow-x-auto">
    <pre>{{ toolResult }}</pre>
  </div>
</div>
```

### 6.3 输入框 (ChatGPT风格)

```vue
<div class="border-t border-border bg-bg-secondary p-4">
  <div class="max-w-3xl mx-auto">
    <div class="flex items-end gap-3 bg-bg-tertiary rounded-[12px] p-3">
      <textarea 
        v-model="message"
        class="flex-1 bg-transparent resize-none text-[14px] text-text-primary 
               placeholder:text-text-tertiary focus:outline-none"
        placeholder="输入消息..."
        rows="1"
        @keydown.enter.exact="send"
      />
      <button 
        @click="send"
        :disabled="!message.trim()"
        class="p-2 rounded-[8px] transition-colors"
        :class="message.trim() 
          ? 'bg-brand-primary text-white hover:bg-brand-hover' 
          : 'bg-bg-hover text-text-tertiary'">
        <Send :size="16" :stroke-width="2" />
      </button>
    </div>
    <p class="text-[11px] text-text-tertiary mt-2 text-center">
      按 Enter 发送，Shift + Enter 换行
    </p>
  </div>
</div>
```

---

## 7. 历史记录页面

### 7.1 列表项设计

```vue
<div class="flex items-center gap-3 p-4 hover:bg-bg-hover cursor-pointer border-b border-border">
  <!-- Agent图标 -->
  <div class="w-10 h-10 rounded-[12px] bg-brand-light flex items-center justify-center flex-shrink-0">
    <component :is="agentIcon" :size="20" :stroke-width="2" class="text-brand-primary" />
  </div>
  
  <!-- 内容 -->
  <div class="flex-1 min-w-0">
    <h3 class="text-[14px] font-semibold text-text-primary truncate">{{ title }}</h3>
    <p class="text-[12px] text-text-secondary truncate">{{ preview }}</p>
  </div>
  
  <!-- 元数据 -->
  <div class="text-right flex-shrink-0">
    <p class="text-[11px] text-text-tertiary">{{ timeAgo }}</p>
    <p class="text-[11px] text-text-tertiary">{{ messageCount }}条消息</p>
  </div>
</div>
```

---

## 8. Lucide图标使用规范

### 8.1 导入方式

```typescript
import { 
  Bot, MessageSquare, History, Settings, Search, 
  Send, Copy, Check, X, ChevronDown, ChevronRight,
  MoreHorizontal, User, LogOut, Bell, Wrench,
  Sun, Moon, Plus, Trash2, Edit3, Save
} from 'lucide-vue-next'
```

### 8.2 使用规范

```vue
<!-- 标准用法 -->
<IconName :size="16" :stroke-width="2" />

<!-- 尺寸规范 -->
<!-- Sidebar导航: 17px -->
<!-- Header图标: 17px -->
<!-- 按钮图标: 16px -->
<!-- 内联图标: 14px -->
<!-- 小图标: 12px -->
```

---

## 9. 设计禁忌

1. **不要emoji** - 所有图标用Lucide
2. **不要底部TabBar** - 桌面端用侧边栏
3. **不要纯黑文字** - 用#262626
4. **不要纯白背景** - 用#FAFAFA (页面) 或 #F6F5F4 (区域)
5. **不要重阴影** - 用Vercel shadow-as-border
6. **不要装饰字体** - 系统字体栈
7. **不要过多颜色** - 黑白灰 + 一个品牌蓝
8. **不要字重500** - 只用400和600

---

## 10. Tailwind配置

```typescript
// tailwind.config.ts
export default {
  theme: {
    extend: {
      colors: {
        bg: {
          primary: '#FAFAFA',
          secondary: '#FFFFFF',
          tertiary: '#F6F5F4',
          hover: '#F2F1ED',
          active: '#EBEAE5',
          dark: '#15202B',
        },
        text: {
          primary: '#262626',
          secondary: '#615D59',
          tertiary: '#A39E98',
          inverse: '#FFFFFF',
          link: '#1D9BF0',
        },
        brand: {
          primary: '#1D9BF0',
          hover: '#1A8CD8',
          active: '#1680C9',
          light: '#E8F5FE',
          text: '#0A66C2',
        },
        border: {
          DEFAULT: 'rgba(0, 0, 0, 0.1)',
        },
      },
      borderRadius: {
        'btn': '8px',
        'card': '12px',
        'input': '6px',
        'pill': '9999px',
      },
      boxShadow: {
        'card': '0 0 0 1px rgba(0,0,0,0.08), 0 2px 2px rgba(0,0,0,0.04)',
        'elevated': '0 4px 18px rgba(0,0,0,0.04), 0 2px 7px rgba(0,0,0,0.027)',
        'focus': '0 0 0 2px hsla(212, 100%, 48%, 1)',
      },
    },
  },
}
```

---

*文档版本: v2.0 | 参考: Vercel + Notion + Cursor + IG + X | 更新: 2026-08-05*
