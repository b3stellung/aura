# Aura Warm Product Frontend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将现有 Aura 前端从紫色 SaaS/AI 控制台改造成温暖、舒适、以穿搭内容和逐层展示为主的产品式应用，同时保持现有核心业务逻辑可用。

**Architecture:** 以外层 `aura-frontend` 为唯一实现目录。先建立全局视觉 token 和轻量顶部导航，再把首页改成今日穿搭叙事页；随后将 AI 页面改成带当前搭配上下文的私人顾问界面；最后统一衣橱皮肤并收口认证、构建和开发兜底状态。保留现有 Pinia store、REST/SSE API 和业务动作，避免后端改动。

**Tech Stack:** Vue 3, TypeScript, Vue Router, Pinia, Vite, Tailwind CSS 4, lucide-vue-next, 现有 fetch/SSE API 层。

---

## 文件地图与边界

- Modify: `aura-frontend/src/style.css` — 全局 reset、暖色 token、基础动效和无障碍焦点样式。
- Modify: `aura-frontend/tailwind.config.js` — 暖色 Tailwind token、字体、圆角和阴影。
- Modify: `aura-frontend/src/layouts/AppLayout.vue` — 从固定侧边栏布局切换为顶部导航 + 内容区。
- Modify: `aura-frontend/src/components/layout/Header.vue` — 顶部 Aura 导航、用户入口和移动端行为。
- Modify: `aura-frontend/src/components/layout/Sidebar.vue` — 改为兼容旧路由/对话入口的轻量移动抽屉，桌面端不再显示厚重侧栏。
- Modify: `aura-frontend/src/router/index.ts` — 统一导航名称，补充 `/wardrobe`，保持 `/sandbox` 兼容重定向或入口。
- Modify: `aura-frontend/src/views/home/HomeView.vue` — 今日穿搭纵向叙事页。
- Modify: `aura-frontend/src/views/AiAssistantView.vue` — 私人顾问式 AI 页面，携带当前搭配上下文。
- Modify: `aura-frontend/src/components/maas/*.vue` — 仅在需要时重命名文案和调整视觉，不删除 SSE 数据接口能力。
- Modify: `aura-frontend/src/views/WardrobeView.vue` — 衣橱浏览层级、暖色皮肤和详情交互。
- Modify: `aura-frontend/src/views/sandbox/AgentWorkspace.vue` — 修复当前 TypeScript 错误；保留兼容入口。
- Modify: `aura-frontend/src/stores/auth.ts`, `aura-frontend/src/api/client.ts`, `aura-frontend/src/api/chat.ts`, `aura-frontend/src/api/wardrobe.ts`, `aura-frontend/src/router/index.ts` — 统一 token key 为 `aura-token`。
- Modify: `aura-frontend/src/views/auth/LoginView.vue`, `aura-frontend/src/views/auth/RegisterView.vue` — 移除伪造 token 成功路径，明确真实 API 失败反馈。
- Modify: `aura-frontend/src/stores/home.ts`, `aura-frontend/src/stores/wardrobe.ts`, `aura-frontend/src/stores/chat.ts` — 保留开发兜底但增加 `isFallback`/提示状态，避免伪装已同步。
- Test/Verify: `aura-frontend` — `npm run build`、路由检查、窄屏手工检查和 API/SSE 静态检查。

## Task 1: 建立暖色视觉基础

**Files:**
- Modify: `aura-frontend/src/style.css`
- Modify: `aura-frontend/tailwind.config.js`

- [ ] **Step 1: 记录当前构建基线**

Run from `C:\Users\Acer\Desktop\aura-project\aura-frontend`:

```powershell
npm run build
```

Expected: 当前基线会因 `AgentWorkspace.vue` 的 3 个 TypeScript 错误失败；记录错误，不在本 task 伪造通过结果。

- [ ] **Step 2: 替换全局 token**

将 `style.css` 的页面背景、正文、焦点环、滚动条和 selection 改为暖色 token，并保留 `prefers-reduced-motion`。在 `tailwind.config.js` 中把 `bg-*`、`text-*`、`border`、`ig-blue` 和 Instagram gradient 替换为 `aura-*` 暖色命名；同时保留旧 class 的兼容别名，避免一次性破坏现有视图。

必须包含这些值：`#F5F1EA`、`#FFFDFC`、`#2E2A26`、`#81786E`、`#68705A`、`#A98570`、`#E5DED4`。删除或停止使用全局 Instagram 渐变。

- [ ] **Step 3: 验证 token 生效**

```powershell
npm run build
```

Expected: 输出仍只包含既有 `AgentWorkspace.vue` 类型错误，不能新增 CSS/Tailwind/Vite 错误。

## Task 2: 将应用壳层改为轻量顶部导航

**Files:**
- Modify: `aura-frontend/src/layouts/AppLayout.vue`
- Modify: `aura-frontend/src/components/layout/Header.vue`
- Modify: `aura-frontend/src/components/layout/Sidebar.vue`
- Modify: `aura-frontend/src/router/index.ts`

- [ ] **Step 1: 定义导航目标**

导航项固定为：`/` 首页、`/wardrobe` 衣橱、`/ai` 造型师、`/history` 历史；`/sandbox` 作为旧入口保留并指向 `/ai` 或显示兼容工作区，不在主导航显示 Agent 名称。

- [ ] **Step 2: 实现桌面顶部导航**

`AppLayout.vue` 使用纵向 flex：顶部 `Header`，下方 `main`。桌面端不渲染固定 232px 侧栏；Header 包含 Aura 标识、导航链接、用户头像/设置入口和移动端菜单按钮。所有路由链接使用 `router-link`，当前路由通过 `route.path` 标记。

- [ ] **Step 3: 实现移动端菜单**

`Sidebar.vue` 仅作为移动端 drawer 使用，提供导航和关闭按钮；打开时显示 backdrop，关闭时恢复页面滚动。不得引入新的组件库或底部 tab bar。

- [ ] **Step 4: 补充衣橱路由并验证导航**

在 router 中添加 `/wardrobe` 指向 `views/WardrobeView.vue`，保留鉴权 meta。运行：

```powershell
npm run build
```

Expected: 无新的路由导入错误；TypeScript 剩余错误仍仅来自待处理的 `AgentWorkspace.vue`。

## Task 3: 重做首页为“今日穿搭”叙事页面

**Files:**
- Modify: `aura-frontend/src/views/home/HomeView.vue`
- Modify: `aura-frontend/src/stores/home.ts`（仅补充展示需要的状态，不重写请求逻辑）
- Modify: `aura-frontend/src/assets/images.json`（仅在缺少场景语义时补充已有本地资源映射，不下载新资源）

- [ ] **Step 1: 保留现有数据动作**

继续使用 `useHomeStore()` 的 `refresh`, `loadMore`, `toggleLike`, `toggleFavorite`, `setCategory`。不要在页面内复制请求逻辑；将推荐内容归一为 `heroRecommendation`、`outfitItems`、`scenes` 三个 computed 视图模型。

- [ ] **Step 2: 实现主视觉**

首屏包含宽幅主图、短标题、天气/场合/风格摘要，以及 `查看搭配` 和 `让 Aura 换一套` 两个动作。主图使用现有 `imageUrl`，无图时使用暖色占位，不出现统计数字卡片。

- [ ] **Step 3: 实现逐层区块**

按顺序实现：

1. 今日主搭配；
2. 为什么适合你（三个可展开说明）；
3. 搭配拆解（外套、上衣、下装、鞋履、配饰）；
4. 场景横向切换；
5. AI 调整邀请。

每个区块只保留一个主动作，使用 `IntersectionObserver` 或 CSS reveal class 做轻量淡入，不引入滚动引擎。

- [ ] **Step 4: 处理本地兜底状态**

当 API 失败而使用本地图片/模拟推荐时，在页面底部或推荐区以低干扰文字显示“展示示例内容，连接衣橱后会替换为你的单品”，不要把 fallback 数据写成真实用户数据。

- [ ] **Step 5: 验证首页**

```powershell
npm run build
```

Expected: 首页模板、图片导入和 store 类型无新错误；视觉检查确认首屏没有 ECharts、Trending/AI Picks 看板或技术状态。

## Task 4: 将 AI Assistant 改为私人顾问空间

**Files:**
- Modify: `aura-frontend/src/views/AiAssistantView.vue`
- Modify: `aura-frontend/src/components/maas/MaaSWorkspace.vue`
- Modify: `aura-frontend/src/components/maas/ChatTimeline.vue`
- Modify: `aura-frontend/src/components/maas/ChatMessage.vue`
- Modify: `aura-frontend/src/components/maas/ChatComposer.vue`
- Modify: `aura-frontend/src/components/maas/ContextPanel.vue`
- Modify: `aura-frontend/src/components/maas/RunInspector.vue`

- [ ] **Step 1: 定义用户层状态文案**

把 `Ready`、`Running`、`Tool Call` 等普通用户文案转换为“可以开始”“正在调整搭配”“连接中断，可以重试”。保留内部 `RunEvent` 和 SSE 类型，不删除 `streamChat`、取消和重试能力。

- [ ] **Step 2: 增加当前搭配上下文**

`AiAssistantView.vue` 提供 `currentOutfit` 视图模型，包含标题、场景和单品列表；进入页面时显示上下文摘要和快捷需求：`更正式`、`更保暖`、`加入一点颜色`、`只用我的衣橱`。快捷需求复用 `chat.sendMessage()`。

- [ ] **Step 3: 重排聊天区域**

`MaaSWorkspace.vue` 保留三 slot 结构，但默认布局改为暖白背景、宽松时间线和可折叠当前搭配抽屉。`ChatMessage.vue` 以自然语言回复和推荐结果为主，不显示技术事件。`ChatComposer.vue` 保持输入、发送、停止和重试操作可见。

- [ ] **Step 4: 改造右侧上下文面板**

`ContextPanel.vue` 和 `RunInspector.vue` 由技术 Inspector 改为“当前搭配”内容：图片、场景、单品、收藏和保存入口；SSE 事件可作为低干扰的辅助状态，不以时间线暴露给普通用户。

- [ ] **Step 5: 验证真实 SSE 路径**

静态确认 `AiAssistantView.vue -> useChatStore.sendMessage -> api/chat.ts -> streamChat` 链路未断；运行：

```powershell
npm run build
```

Expected: AI 页面类型通过；接口不可用时显示可重试错误，而不是空白或假成功。

## Task 5: 统一衣橱为可浏览的个人空间

**Files:**
- Modify: `aura-frontend/src/views/WardrobeView.vue`
- Modify: `aura-frontend/src/stores/wardrobe.ts`

- [ ] **Step 1: 替换顶部统计卡片**

将五个同等权重统计卡片改为一个衣橱标题区和横向分类入口；分类入口仍调用 `wardrobeStore.setCategory()`，数量作为轻量辅助文字。

- [ ] **Step 2: 重做单品展示**

使用 3:4 图片比例、单品名称和少量标签作为主信息；品牌、季节、颜色等元数据放进详情抽屉或编辑弹窗。保留搜索、grid/list 切换、添加、编辑、删除。

- [ ] **Step 3: 标记 fallback 数据**

在 `wardrobe.ts` 增加明确的开发兜底状态，例如 `dataSource: 'api' | 'fallback'`；API 失败时保留现有可用演示内容，但页面显示轻量提示，不把 `mock_` ID 当作已同步数据。

- [ ] **Step 4: 验证衣橱操作**

```powershell
npm run build
```

Expected: 衣橱页面模板和 `OutfitItem` 类型无新增错误；添加、编辑、删除方法的调用签名保持不变。

## Task 6: 收口认证 token 和开发兜底

**Files:**
- Modify: `aura-frontend/src/api/client.ts`
- Modify: `aura-frontend/src/api/chat.ts`
- Modify: `aura-frontend/src/api/wardrobe.ts`
- Modify: `aura-frontend/src/router/index.ts`
- Modify: `aura-frontend/src/views/auth/LoginView.vue`
- Modify: `aura-frontend/src/views/auth/RegisterView.vue`
- Modify: `aura-frontend/src/stores/auth.ts`
- Modify: `aura-frontend/src/stores/chat.ts`

- [ ] **Step 1: 统一 token key**

所有请求只读取 `localStorage.getItem('aura-token')`；登录、注册、退出只写入/清除 `aura-token` 和 `aura-refresh-token`。删除 `aura_token` 读取分支，避免路由守卫和 API 客户端判断不一致。

- [ ] **Step 2: 删除伪造登录成功路径**

登录和注册组件只调用 auth store 的真实 API action；API 失败时保留表单并展示错误。不得在成功分支写入 `mock-token-${Date.now()}`。如需离线演示，必须由显式开发环境开关控制，默认关闭。

- [ ] **Step 3: 保留可见但诚实的 fallback**

聊天、首页和衣橱可以继续使用 fallback 内容帮助 UI 开发，但必须有显式 `isFallback`/`dataSource` 状态，且不模拟“已保存到后端”的成功提示。

- [ ] **Step 4: 静态检查 token 使用**

```powershell
rg -n "aura_token|mock-token|mock-refresh-token" src
```

Expected: 不再出现生产代码中的 `aura_token` 或伪造 token；仅允许开发说明或明确受控的测试 fixture。

## Task 7: 修复构建错误并完成整体验证

**Files:**
- Modify: `aura-frontend/src/views/sandbox/AgentWorkspace.vue`

- [ ] **Step 1: 修复 `AgentWorkspace.vue` 类型错误**

删除未使用的 `RunEvent` import；对 `conversation.value` 在模板使用前增加空值安全处理，或在 setup 中提供非空的 computed view model。不得使用 `as any` 绕过错误。

- [ ] **Step 2: 运行类型和生产构建**

```powershell
npm run build
```

Expected: `vue-tsc -b` 和 `vite build` 均成功，生成 `dist`。

- [ ] **Step 3: 做静态一致性检查**

```powershell
rg -n "gradient-instagram|Trending|AI Picks|Temperature|Max Tokens|System Prompt|Tool Call|Run Status|aura_token|mock-token" src
```

Expected: 主用户页面不再引用这些内容；若兼容工作区保留内部字段，必须不被主导航和普通用户页面展示。

- [ ] **Step 4: 做响应式检查**

启动：

```powershell
npm run dev -- --host 127.0.0.1
```

手工检查 `/`、`/wardrobe`、`/ai`、`/history`、`/settings`：桌面宽度无横向溢出；移动宽度能打开/关闭菜单和当前搭配抽屉；AI 输入始终可见；图片没有破坏内容布局。

- [ ] **Step 5: 做核心交互检查**

手工验证：首页场景切换、喜欢/收藏、进入 AI、AI 快捷需求、停止/重试、衣橱分类/搜索/添加/编辑/删除、登录失败提示和退出登录。

## 完成定义

- `npm run build` 通过。
- 首页、衣橱、造型师使用同一套暖色 token 和导航系统。
- 首页首屏以穿搭/场景为主，不显示看板统计。
- AI 页面不向普通用户展示 Agent、模型参数、工具日志和运行状态字段。
- 现有 API、SSE、衣橱和认证核心逻辑未被破坏。
- 兜底数据被明确标记，认证 token key 全局统一。
- 桌面和移动布局均通过手工检查。
