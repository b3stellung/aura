# Aura MaaS 三栏工作台设计

## 目标

将现有 AI Assistant / Agent Workspace 前端收口为主流 MaaS 工作台，优先展示已有 ReAct、RAG 与插件能力，同时保持与 Hermes 后端接口边界清晰。第一阶段只改前端，不新增后端接口假设；未知字段通过适配层归一化。

## 页面结构

页面采用响应式三栏布局：

- 左栏（会话）：新建会话、会话搜索、历史列表、重命名和删除。
- 中栏（运行区）：Agent/模型/会话顶部栏，消息流、Markdown 渲染、引用卡片、输入框、停止和重试。
- 右栏（上下文）：可折叠的 ReAct 时间线、RAG 检索结果、插件调用状态、衣橱上下文。

窄屏时右栏折叠为抽屉，左栏可隐藏；中栏始终保持可用。

## 数据流与接口适配

新增统一的 API 适配层，隔离后端响应差异：

1. REST client 负责会话列表、会话详情、Agent 配置和历史消息。
2. SSE client 负责发送消息并解析标准事件：`delta`、`thought`、`tool`、`retrieval`、`done`、`error`。
3. chat store 保存当前会话、消息、运行事件、连接状态和 token/耗时统计。
4. UI 只消费归一化后的前端类型，不直接依赖后端 DTO。

SSE 连接中断时保留已接收内容并显示重连/重试操作；收到 `done` 后关闭连接；收到 `error` 后保留错误上下文并允许重试。401 统一交给认证层处理。

## 组件边界

- `MaaSWorkspace.vue`：布局与栏位显隐。
- `ConversationSidebar.vue`：会话操作。
- `ChatTimeline.vue` / `ChatMessage.vue`：消息渲染。
- `RunInspector.vue`：ReAct、工具、RAG 事件时间线。
- `ContextPanel.vue`：检索结果、插件与衣橱上下文。
- `ChatComposer.vue`：输入、附件和运行控制。
- `api/client.ts`、`api/chat.ts`：HTTP/SSE 传输。
- `stores/chat.ts`、`stores/conversation.ts`：状态与动作。

现有页面和 store 优先复用；仅在职责不清或无法支持流式事件时拆分组件。

## 状态与交互

运行状态包括 `idle`、`connecting`、`streaming`、`completed`、`error`、`cancelled`。所有状态提供明确的视觉反馈和可访问文本。空会话显示快捷提示；加载显示骨架；无检索结果显示说明而非空白区域。

消息支持 Markdown、代码块、引用来源和复制；工具调用默认折叠，最新事件自动滚动但用户上滑后暂停自动滚动。

## 验证标准

- `npm run build` 通过。
- 无新增 TypeScript 错误。
- SSE 事件能驱动消息增量、思考、工具、检索和结束状态。
- 401、断线、错误、空数据和窄屏布局均有可见反馈。
- 不改动 Hermes 负责的后端代码与接口语义。
