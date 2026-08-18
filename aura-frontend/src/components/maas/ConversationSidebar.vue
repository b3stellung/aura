<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { Search, Plus, Trash2, MessageSquare, Sparkles } from 'lucide-vue-next'
import { useConversationStore } from '@/stores/conversation'

const store = useConversationStore()
const query = ref('')
const open = ref(false)
const groups = computed(() => {
  const q = query.value.toLowerCase()
  const filter = (items: any[]) => items.filter((conversation) => conversation.title.toLowerCase().includes(q))
  return {
    today: filter(store.groupedConversations.today),
    yesterday: filter(store.groupedConversations.yesterday),
    older: filter(store.groupedConversations.older),
  }
})

function select(id: string) { store.setCurrentConversation(id) }
function close() { open.value = false }
function toggle() { open.value = !open.value }
function onKeydown(event: KeyboardEvent) { if (event.key === 'Escape') close() }
function lockScroll(value: boolean) { if (typeof document !== 'undefined') document.body.style.overflow = value ? 'hidden' : '' }
watch(open, lockScroll)
onMounted(() => document.addEventListener('keydown', onKeydown))
onBeforeUnmount(() => { document.removeEventListener('keydown', onKeydown); lockScroll(false) })
function create() { store.createConversation('stylist', 'Aura Stylist', '✨') }
</script>

<template>
  <button class="drawer-trigger" :class="{ hidden: open }" type="button" aria-controls="conversation-drawer" :aria-expanded="open" aria-label="Open conversations" @click="toggle"><MessageSquare :size="17" /><span>Chats</span></button>
  <div v-if="open" class="drawer-backdrop" aria-hidden="true" @click="close" />
  <aside id="conversation-drawer" class="maas-sidebar" :class="{ open }" aria-label="Conversations">
    <div class="sidebar-brand">
      <div class="brand-mark"><Sparkles :size="16" /></div>
      <div><span class="brand-kicker">Aura</span><strong>Conversations</strong></div>
      <button class="icon-btn" @click="create" aria-label="New conversation"><Plus :size="18" /></button>
    </div>

    <label class="search">
      <Search :size="15" aria-hidden="true" /><input v-model="query" placeholder="Search conversations" />
      <kbd>⌘ K</kbd>
    </label>
    <button class="new-btn" @click="create"><Plus :size="16" /> New conversation</button>

    <div v-for="(items, name) in groups" :key="name" class="group">
      <small>{{ name }}</small>
      <button v-for="conversation in items" :key="conversation.id" class="conversation"
        :class="{ active: store.currentConversation?.id === conversation.id }" @click="select(conversation.id)">
        <span class="conversation-icon"><MessageSquare :size="14" /></span>
        <span class="conversation-title">{{ conversation.title || 'New conversation' }}</span>
        <Trash2 :size="14" class="delete" aria-label="Delete conversation" @click.stop="store.deleteConversation(conversation.id)" />
      </button>
    </div>
    <div v-if="!store.conversations.length" class="empty"><MessageSquare :size="20" /><span>No conversations yet</span><small>Start a new conversation to begin.</small></div>
  </aside>
</template>

<style scoped>
.maas-sidebar{--ink:#29233f;--muted:#8d87a5;--line:rgba(107,83,161,.16);--lavender:#f6f2ff;--purple:#7553b5;box-sizing:border-box;width:280px;min-width:240px;height:100%;padding:20px 14px;display:flex;flex-direction:column;gap:14px;overflow:auto;background:linear-gradient(180deg,#fcfaff 0%,#f7f3ff 100%);border-right:1px solid var(--line);color:var(--ink)}
.sidebar-brand{display:flex;align-items:center;gap:10px;padding:2px 6px 12px}.sidebar-brand strong{display:block;font-size:15px;letter-spacing:-.01em}.brand-kicker{display:block;color:var(--purple);font-size:10px;font-weight:700;letter-spacing:.16em;text-transform:uppercase}.brand-mark{display:grid;place-items:center;width:30px;height:30px;border-radius:10px;color:#fff;background:linear-gradient(135deg,#9871df,#60419e);box-shadow:0 5px 14px rgba(101,69,166,.25)}.icon-btn{margin-left:auto;padding:7px;border:1px solid var(--line);border-radius:9px;color:var(--purple);background:#fff;cursor:pointer}.icon-btn:hover{background:var(--lavender)}
.search{display:flex;align-items:center;gap:8px;padding:9px 10px;border:1px solid var(--line);border-radius:11px;color:var(--muted);background:#fff;box-shadow:0 2px 8px rgba(68,43,117,.04)}.search input{min-width:0;width:100%;border:0;outline:0;color:var(--ink);font:inherit;font-size:12px;background:transparent}.search input::placeholder{color:#aaa4be}.search kbd{white-space:nowrap;padding:2px 5px;border:1px solid #e8e1f4;border-radius:5px;color:#aaa4be;font-size:9px;background:#faf8ff}
.new-btn{display:flex;align-items:center;justify-content:center;gap:8px;padding:10px;border:0;border-radius:11px;color:#fff;font-weight:600;font-size:12px;background:linear-gradient(135deg,#8060c6,#65459f);box-shadow:0 7px 16px rgba(103,70,164,.22);cursor:pointer}.new-btn:hover{filter:brightness(1.05);transform:translateY(-1px)}
.group{display:flex;flex-direction:column;gap:3px}.group small{margin:8px 7px 4px;color:var(--muted);font-size:10px;font-weight:700;letter-spacing:.1em;text-transform:uppercase}.conversation{display:flex;align-items:center;gap:9px;width:100%;padding:9px 8px;border:1px solid transparent;border-radius:10px;text-align:left;color:#5b5473;background:transparent;cursor:pointer;transition:.15s ease}.conversation:hover{border-color:var(--line);background:rgba(255,255,255,.7)}.conversation.active{border-color:rgba(117,83,181,.2);color:#4b347e;background:#eee8ff;box-shadow:inset 3px 0 #7957ba}.conversation-icon{display:grid;place-items:center;width:25px;height:25px;flex:0 0 25px;border-radius:8px;color:#8467b9;background:#f0eaff}.conversation.active .conversation-icon{color:#fff;background:#8060c4}.conversation-title{flex:1;min-width:0;overflow:hidden;white-space:nowrap;text-overflow:ellipsis;font-size:12px}.delete{flex:0 0 auto;opacity:0;color:#aa8daf;cursor:pointer}.conversation:hover .delete,.conversation.active .delete{opacity:.72}.delete:hover{color:#b14979}.empty{display:flex;flex-direction:column;align-items:center;gap:7px;padding:34px 12px;color:var(--muted);font-size:13px;text-align:center}.empty svg{color:#a58bd1}.empty small{font-size:11px;color:#aaa4bd}
@media(max-width:1120px){.maas-sidebar{position:fixed;z-index:30;inset:0 auto 0 0;width:min(82vw,300px);min-width:0;padding:16px 12px;transform:translateX(-100%);transition:transform .22s ease;box-shadow:12px 0 30px rgba(53,32,92,.16)}.maas-sidebar.open{transform:translateX(0)}.drawer-backdrop{position:fixed;z-index:20;inset:0;background:rgba(35,24,56,.28)}.drawer-trigger{display:flex;position:fixed;z-index:31;left:12px;top:76px;align-items:center;gap:6px;padding:9px 11px;border:1px solid var(--line);border-radius:9px;color:#66568a;font-size:11px;font-weight:600;background:#fff;box-shadow:0 4px 12px rgba(53,32,92,.1);cursor:pointer}.drawer-trigger.hidden{display:none}}
@media(min-width:1121px){.drawer-trigger,.drawer-backdrop{display:none}}
</style>
