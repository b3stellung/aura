<script setup lang="ts">
import { ref } from 'vue'
import { Menu, PanelRight, MoreHorizontal } from 'lucide-vue-next'
import ConversationSidebar from './ConversationSidebar.vue'
import ContextPanel from './ContextPanel.vue'

const emit = defineEmits<{
  sidebarOpen: [open: boolean]
  contextOpen: [open: boolean]
}>()

const sidebarOpen = ref(false)
const contextOpen = ref(false)

function setSidebarOpen(open: boolean) {
  sidebarOpen.value = open
  emit('sidebarOpen', open)
}

function setContextOpen(open: boolean) {
  contextOpen.value = open
  emit('contextOpen', open)
}

function toggleContextFromPanel(event: MouseEvent) {
  const target = event.target as HTMLElement
  if (target.closest('.toggle')) setContextOpen(!contextOpen.value)
}

function closeDrawers() {
  setSidebarOpen(false)
  setContextOpen(false)
}
</script>

<template>
  <div class="workspace" :class="{ 'sidebar-open': sidebarOpen, 'context-open': contextOpen }">
    <ConversationSidebar />

    <main class="main">
      <header class="topbar">
        <div class="brand-mark" aria-hidden="true">A</div>
        <div class="heading">
          <span class="eyebrow">AURA / MAAS</span>
          <h1>MaaS Workspace</h1>
        </div>
        <div class="topbar-spacer" />
        <span class="status"><i aria-hidden="true" /> Ready</span>
        <button class="drawer-action sidebar-action" type="button" aria-label="Open conversations" @click="setSidebarOpen(!sidebarOpen)"><Menu :size="16" /></button>
        <button class="drawer-action context-action" type="button" aria-label="Open context" @click="setContextOpen(!contextOpen)"><PanelRight :size="16" /></button>
        <button class="topbar-action" type="button" aria-label="Workspace settings"><MoreHorizontal :size="17" /></button>
      </header>

      <section class="timeline" aria-label="Conversation timeline">
        <div class="timeline-inner">
          <slot name="timeline">
            <div class="placeholder">
              <span class="placeholder-mark" aria-hidden="true">*</span>
              <strong>Start a conversation with Aura</strong>
              <span>Ask for a look, a plan, or a little inspiration.</span>
            </div>
          </slot>
        </div>
      </section>

      <footer class="composer">
        <div class="composer-inner">
          <slot name="composer"><slot /></slot>
        </div>
      </footer>
    </main>

    <ContextPanel @click="toggleContextFromPanel"><slot name="context" /></ContextPanel>
    <button v-if="sidebarOpen || contextOpen" class="workspace-backdrop" type="button" aria-label="Close open panels" @click="closeDrawers" />
  </div>
</template>

<style scoped>
.workspace {
  --ink: #29223e;
  --muted: #8b82a6;
  --line: rgba(89, 69, 132, 0.14);
  display: grid;
  grid-template-columns: minmax(216px, 248px) minmax(0, 1fr) minmax(264px, 304px);
  height: 100%;
  min-height: 0;
  overflow: hidden;
  color: var(--ink);
  background: #f7f4fc;
}

.main { display: flex; flex-direction: column; min-width: 0; min-height: 0; background: #fbfaff; }
.topbar { height: 68px; flex: 0 0 68px; display: flex; align-items: center; gap: 11px; padding: 0 28px; border-bottom: 1px solid var(--line); background: rgba(255, 254, 255, .88); }
.brand-mark { display: grid; place-items: center; width: 27px; height: 27px; border-radius: 9px; color: #fff; background: linear-gradient(145deg, #8b6ac9, #594080); font: 700 13px/1 Georgia, serif; box-shadow: 0 4px 12px rgba(106, 76, 160, .22); }
.heading { display: flex; flex-direction: column; gap: 2px; }
.eyebrow { color: #9b8bb9; font-size: 9px; font-weight: 700; letter-spacing: .16em; }
.topbar h1 { margin: 0; color: var(--ink); font-size: 15px; font-weight: 650; letter-spacing: -.01em; }
.topbar-spacer { flex: 1; }
.status { display: inline-flex; align-items: center; gap: 7px; padding: 6px 10px; border: 1px solid rgba(104, 160, 127, .2); border-radius: 999px; color: #4c8a68; background: #f2faf4; font-size: 11px; font-weight: 600; }
.status i { width: 6px; height: 6px; border-radius: 50%; background: #68b183; box-shadow: 0 0 0 3px rgba(104, 177, 131, .14); }
.topbar-action { width: 28px; height: 28px; margin-left: 3px; border: 0; border-radius: 8px; color: #9b91af; background: transparent; cursor: pointer; letter-spacing: 2px; }
.topbar-action:hover { color: #67548a; background: #f2eef9; }
.drawer-action { display: none; width: 30px; height: 30px; border: 1px solid var(--line); border-radius: 8px; color: #76649a; background: #fff; cursor: pointer; }
.timeline { flex: 1; min-height: 0; overflow-y: auto; padding: clamp(18px, 3vw, 30px) clamp(16px, 3vw, 30px) clamp(18px, 2.5vw, 24px); }
.timeline-inner { width: min(100%, 860px); min-height: 100%; margin: 0 auto; }
.placeholder { display: flex; flex-direction: column; align-items: center; gap: 8px; margin-top: min(24vh, 190px); color: var(--muted); text-align: center; font-size: 12px; }
.placeholder strong { color: #625575; font-size: 15px; font-weight: 600; }
.placeholder-mark { display: grid; place-items: center; width: 42px; height: 42px; margin-bottom: 5px; border: 1px solid #dfd4ef; border-radius: 14px; color: #8f73be; background: #f3eefb; font-size: 20px; }
.composer { padding: clamp(10px, 1.6vw, 14px) clamp(16px, 3vw, 30px) clamp(14px, 2.5vw, 22px); border-top: 1px solid var(--line); background: rgba(255, 254, 255, .9); }
.composer-inner { width: min(100%, clamp(620px, 72vw, 860px)); margin: 0 auto; }
.workspace-backdrop { display: none; }

/* Keep the existing panels visually in the same dusk palette. */
:deep(.maas-sidebar), :deep(.context) { border-color: var(--line); background: #f8f5fd; }
:deep(.sidebar-head strong), :deep(.context h3) { color: var(--ink); }
:deep(.new-btn) { background: #5d447f; }
:deep(.conversation.active), :deep(.conversation:hover) { background: #eee8f8; }

@media (max-width: 1120px) {
  .workspace { grid-template-columns: minmax(0, 1fr); }
  .drawer-action { display: inline-grid; place-items: center; }
  :deep(.maas-sidebar) { position: fixed; z-index: 30; inset: 0 auto 0 0; width: min(82vw, 300px); min-width: 0; transform: translateX(-105%); transition: transform .22s ease; box-shadow: 12px 0 30px rgba(53,32,92,.16); }
  .workspace.sidebar-open :deep(.maas-sidebar) { transform: translateX(0); }
  :deep(.context) { position: fixed; z-index: 30; inset: 0 0 0 auto; width: min(86vw, 330px); min-width: 0; transform: translateX(105%); transition: transform .22s ease; box-shadow: -12px 0 30px rgba(53,32,92,.16); }
  .workspace.context-open :deep(.context), :deep(.context.open) { transform: translateX(0); }
  .workspace-backdrop { display: block; position: fixed; z-index: 20; inset: 0; width: 100%; height: 100%; border: 0; background: rgba(42, 28, 69, .22); backdrop-filter: blur(2px); }
  :deep(.drawer-trigger), :deep(.context-trigger), :deep(.drawer-backdrop) { display: none !important; }
  .sidebar-action { margin-left: 4px; }
}
@media (max-width: 720px) {
  .topbar { height: 60px; flex-basis: 60px; padding: 0 17px; }
  .eyebrow { font-size: 8px; }
}
</style>
