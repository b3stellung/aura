# Aura MaaS Workspace Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 将现有 Vue 前端升级为可展示 ReAct、RAG 和插件运行过程的三栏 MaaS 工作台。

**Architecture:** 以现有 `api`、Pinia stores 和页面为基础，增加统一的 SSE 事件适配层，并将消息、运行轨迹和上下文分别交给聚焦组件渲染。后端接口语义不变，未知 DTO 由前端 normalizer 兼容。

**Tech Stack:** Vue 3、TypeScript、Pinia、Axios、原生 EventSource/fetch streaming、lucide-vue-next、Vite。

---

### Task 1: 建立前端协议类型与 SSE 解析器

**Files:**
- Create: `aura-frontend/src/types/maas.ts`
- Modify: `aura-frontend/src/api/chat.ts`
- Modify: `aura-frontend/src/api/index.ts`

- [ ] 定义 `RunStatus`、`ChatMessage`、`RunEvent`、`Citation`、`ToolCall` 与 `StreamCallbacks` 类型。
- [ ] 实现 `parseSseEvent`，将 `event/data` 文本归一化为 `delta/thought/tool/retrieval/done/error`。
- [ ] 暴露 `streamChat`，使用带 Authorization 的 fetch 流读取器，支持 AbortSignal，并在 `done/error` 后关闭。
- [ ] 为非 JSON data、空行和未知事件提供安全降级，不抛出未捕获异常。

### Task 2: 扩展 chat/conversation stores

**Files:**
- Modify: `aura-frontend/src/stores/chat.ts`
- Modify: `aura-frontend/src/stores/conversation.ts`

- [ ] 保存当前消息、运行事件、`runStatus`、AbortController、token/latency 统计和当前会话 ID。
- [ ] 实现 `sendMessage`、`stopGeneration`、`retryLastMessage`、`clearConversation`，把 Task 1 的 SSE 回调映射到状态。
- [ ] 保证重复发送被拒绝，停止后保留已生成内容，错误状态可重试。
- [ ] 保持现有调用方兼容，新增动作均返回 Promise。

### Task 3: 实现 MaaS 工作台基础布局

**Files:**
- Create: `aura-frontend/src/components/maas/MaaSWorkspace.vue`
- Create: `aura-frontend/src/components/maas/ConversationSidebar.vue`
- Create: `aura-frontend/src/components/maas/ContextPanel.vue`
- Modify: `aura-frontend/src/views/AiAssistantView.vue`

- [ ] 使用 CSS grid 实现左/中/右栏，窄屏隐藏左栏并将右栏改为抽屉。
- [ ] 接入 conversation store，完成新建、切换、搜索、重命名和删除操作的 UI 状态。
- [ ] 在上下文栏展示检索结果、插件状态、衣橱上下文；无数据时显示解释性空状态。
- [ ] 在中央栏预留 `ChatTimeline`、`ChatComposer` 和 `RunInspector` 插槽式区域。

### Task 4: 实现消息流与运行检查器

**Files:**
- Create: `aura-frontend/src/components/maas/ChatTimeline.vue`
- Create: `aura-frontend/src/components/maas/ChatMessage.vue`
- Create: `aura-frontend/src/components/maas/RunInspector.vue`
- Create: `aura-frontend/src/components/maas/ChatComposer.vue`

- [ ] 支持用户/助手消息、Markdown 文本、代码块、复制、引用来源和错误提示。
- [ ] 按时间线渲染 Thought、Tool、Retrieval、Done 事件，工具事件默认折叠。
- [ ] 输入区支持发送、停止、重试、快捷提示和图片选择预览；无内容时禁用发送。
- [ ] 实现自动滚动策略：用户上滑后暂停，回到底部后恢复。

### Task 5: 对齐路由、主题和可访问性

**Files:**
- Modify: `aura-frontend/src/router/index.ts`
- Modify: `aura-frontend/src/style.css`
- Modify: `aura-frontend/src/assets/styles/main.css`

- [ ] 将 `/assistant` 指向 MaaS 工作台并保留旧入口兼容跳转。
- [ ] 使用设计系统已有颜色、圆角、阴影和字体变量，不引入第二套主题。
- [ ] 为按钮、抽屉、状态徽标、消息区域增加 aria-label、键盘焦点和 reduced-motion 支持。

### Task 6: 验证与对接说明

**Files:**
- Modify: `aura-frontend/README.md`
- Create: `aura-frontend/src/types/maas.test.ts` (若当前测试基础设施可用)

- [ ] 运行 `npm install` 后执行 `npm run build`，确认 `vue-tsc` 和 Vite 均通过。
- [ ] 至少验证 SSE parser 的六类事件、未知事件和 malformed data。
- [ ] 手动验证空会话、流式输出、停止、错误重试、401、窄屏布局。
- [ ] 在 README 记录后端需要提供的 SSE event 名称和示例 payload，不改变后端实现。
