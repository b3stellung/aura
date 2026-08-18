# MaaS Responsive Dusk Layout Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 MaaS 工作台改为中央优先、两侧抽屉化的暮色紫响应式布局，改善缩放端编排和主题一致性。

**Architecture:** 外层 AppLayout 保留全局导航；MaaSWorkspace 只管理中央工作区和两个可开关的侧栏。桌面端展示会话栏与上下文栏，中屏收起上下文，小屏改为带遮罩的抽屉。所有区域共用 CSS tokens 和状态类，不改变 stores、API 或消息协议。

**Tech Stack:** Vue 3、TypeScript、CSS Grid、CSS variables、lucide-vue-next、Tailwind utilities。

---

### Task 1: 中央优先工作区

**Files:** `aura-frontend/src/components/maas/MaaSWorkspace.vue`

- [ ] 将固定三栏改为中央优先 grid，桌面端侧栏可见、中屏仅中央+可呼出上下文、小屏中央全宽。
- [ ] 增加 `sidebarOpen` / `contextOpen` 事件桥接和遮罩层，不改现有 named slots。
- [ ] 使用 `clamp()` 设置内容 padding、composer 宽度和顶栏间距，避免缩放时挤压。

### Task 2: 会话与上下文抽屉

**Files:** `ConversationSidebar.vue`, `ContextPanel.vue`

- [ ] 桌面端保持静态栏，中小屏使用 translateX 抽屉和 backdrop。
- [ ] 提供可访问的打开/关闭按钮，Escape 关闭，打开时锁定页面滚动。
- [ ] 保持现有会话 store、删除、新建和 context slot 行为。

### Task 3: 全局暮色紫状态与动效

**Files:** `aura-frontend/src/assets/styles/main.css`, `aura-frontend/src/style.css`

- [ ] 统一页面背景、表面、边框、强调色、阴影和焦点环变量。
- [ ] 添加 fade/slide/glow 动效及 reduced-motion 覆盖。
- [ ] 确保全局导航、MaaS 侧栏、消息和输入区使用同一组变量。

### Task 4: 视觉验证

**Files:** no new files

- [ ] 在 1440px、1100px、768px、390px 宽度检查中央区宽度、抽屉、遮罩和滚动。
- [ ] 运行 `node node_modules/vue-tsc/bin/vue-tsc.js -b`，区分本次改动错误与已有错误。
- [ ] 运行 Vite build；若 native optional binding 仍缺失，记录环境阻塞而不改 lockfile。
