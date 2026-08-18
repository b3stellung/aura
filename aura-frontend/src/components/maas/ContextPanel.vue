<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { PanelRight, X, Sparkles, Layers3 } from 'lucide-vue-next'

const open = ref(false)
function close() { open.value = false }
function onKeydown(event: KeyboardEvent) { if (event.key === 'Escape') close() }
function lockScroll(value: boolean) { if (typeof document !== 'undefined') document.body.style.overflow = value ? 'hidden' : '' }
watch(open, lockScroll)
onMounted(() => document.addEventListener('keydown', onKeydown))
onBeforeUnmount(() => { document.removeEventListener('keydown', onKeydown); lockScroll(false) })
</script>

<template>
  <button class="context-trigger" :class="{ hidden: open }" type="button" aria-controls="context-drawer" :aria-expanded="open" aria-label="Open run context" @click="open = true"><PanelRight :size="15" /><span>Context</span></button>
  <div v-if="open" class="drawer-backdrop" aria-hidden="true" @click="close" />
  <aside id="context-drawer" class="context" :class="{ open }" aria-label="Run context">
    <button class="toggle" type="button" :aria-expanded="open" aria-controls="context-drawer" @click="open = !open">
      <X v-if="open" :size="15" /><PanelRight v-else :size="15" />
      <span>{{ open ? 'Close' : 'Context' }}</span>
    </button>
    <div class="context-body">
      <div class="context-header">
        <div class="context-icon"><Sparkles :size="16" /></div>
        <div><span class="eyebrow">Workspace</span><h3>Run context</h3></div>
        <span class="live-dot" title="Context is live" />
      </div>
      <p class="muted">Search results, tools and wardrobe context will appear here.</p>
      <div class="context-section">
        <div class="section-label"><Layers3 :size="13" /> Active context</div>
        <div class="slot-content"><slot><div class="empty"><span class="empty-icon"><PanelRight :size="17" /></span><strong>No context yet</strong><small>Relevant context will appear as you work.</small></div></slot></div>
      </div>
    </div>
  </aside>
</template>

<style scoped>
.context{--ink:#29233f;--muted:#8d87a5;--line:rgba(107,83,161,.16);box-sizing:border-box;position:relative;width:310px;min-width:260px;background:linear-gradient(180deg,#fdfbff,#f7f3ff);border-left:1px solid var(--line);color:var(--ink)}.context-body{height:100%;padding:22px 18px;overflow:auto}.context-header{display:flex;align-items:center;gap:10px;padding-bottom:14px;border-bottom:1px solid var(--line)}.context-icon{display:grid;place-items:center;width:31px;height:31px;border-radius:10px;color:#fff;background:linear-gradient(135deg,#9871df,#60429e);box-shadow:0 5px 14px rgba(101,69,166,.2)}.eyebrow{display:block;margin-bottom:2px;color:#987fc2;font-size:9px;font-weight:700;letter-spacing:.15em;text-transform:uppercase}.context h3{margin:0;font-size:16px;letter-spacing:-.02em}.live-dot{width:7px;height:7px;margin-left:auto;border-radius:50%;background:#62bf92;box-shadow:0 0 0 4px rgba(98,191,146,.13)}.muted{margin:14px 0 20px;color:var(--muted);font-size:12px;line-height:1.55}.context-section{padding:12px;border:1px solid var(--line);border-radius:13px;background:rgba(255,255,255,.62);box-shadow:0 4px 14px rgba(70,43,118,.04)}.section-label{display:flex;align-items:center;gap:6px;margin-bottom:8px;color:#78689a;font-size:10px;font-weight:700;letter-spacing:.1em;text-transform:uppercase}.slot-content{min-height:100px}.empty{display:flex;flex-direction:column;align-items:center;gap:7px;padding:22px 8px;color:var(--muted);font-size:12px;text-align:center}.empty-icon{display:grid;place-items:center;width:31px;height:31px;border-radius:9px;color:#987bc7;background:#f0eaff}.empty strong{color:#60567a;font-size:12px}.empty small{font-size:11px;line-height:1.45;color:#aaa4bd}.toggle{display:none}
@media(max-width:1120px){.context{position:fixed;z-index:30;inset:0 0 0 auto;width:min(86vw,330px);min-width:0;transform:translateX(100%);transition:transform .22s ease;box-shadow:-12px 0 30px rgba(53,32,92,.16)}.context.open{transform:translateX(0)}.drawer-backdrop{position:fixed;z-index:20;inset:0;background:rgba(35,24,56,.28)}.context-trigger{display:flex;position:fixed;z-index:31;right:12px;top:76px;align-items:center;gap:6px;padding:9px 11px;border:1px solid var(--line);border-radius:9px;color:#66568a;font-size:11px;font-weight:600;background:#fff;box-shadow:0 4px 12px rgba(53,32,92,.1);cursor:pointer}.context-trigger.hidden{display:none}.toggle{display:flex;position:absolute;left:-88px;top:18px;align-items:center;gap:6px;padding:9px 11px;border:1px solid var(--line);border-radius:9px;color:#66568a;font-size:11px;font-weight:600;background:#fff;box-shadow:0 4px 12px rgba(53,32,92,.1);cursor:pointer}.context.open .toggle{left:-46px;padding:9px}.context.open .toggle span{display:none}}
@media(min-width:1121px){.context-trigger,.drawer-backdrop{display:none}}
</style>
