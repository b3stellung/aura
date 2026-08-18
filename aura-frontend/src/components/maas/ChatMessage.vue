<script setup lang="ts">
import type { ChatMessage as Message } from '@/types'

const props = defineProps<{ message: Message; streaming?: boolean }>()
const emit = defineEmits<{ copy: [text: string]; retry: [message: Message]; run: [message: Message] }>()
async function copy() { await navigator.clipboard?.writeText(props.message.content); emit('copy', props.message.content) }
</script>
<template>
  <article class="chat-message" :class="message.role">
    <div class="message-avatar" aria-hidden="true">{{ message.role === 'user' ? '你' : 'A' }}</div>
    <div class="message-main">
      <div class="message-meta"><span class="message-author">{{ message.role === 'user' ? '你' : 'Aura' }}</span><time v-if="message.timestamp">{{ message.timestamp }}</time></div>
      <div class="bubble">{{ message.content }}<span v-if="streaming" class="cursor" aria-label="正在生成">▋</span></div>
      <div v-if="message.role === 'assistant'" class="actions" aria-label="消息操作">
        <button type="button" @click="copy"><span aria-hidden="true">⧉</span> 复制</button><button type="button" @click="emit('retry', message)"><span aria-hidden="true">↻</span> 重试</button><button type="button" @click="emit('run', message)"><span aria-hidden="true">▶</span> 运行</button>
      </div>
    </div>
  </article>
</template>
<style scoped>
.chat-message{display:flex;align-items:flex-start;gap:11px;max-width:min(760px,92%);margin:20px 0}.chat-message.user{margin-left:auto;flex-direction:row-reverse}.message-avatar{display:grid;place-items:center;flex:0 0 32px;width:32px;height:32px;border:1px solid rgba(159,131,255,.28);border-radius:11px;background:linear-gradient(145deg,#9b76ff,#5f45a7);color:#fff;font-size:13px;font-weight:700;box-shadow:0 5px 14px rgba(81,52,153,.2)}.user .message-avatar{background:#f0eaff;color:#7454bd;border-color:#dfd3ff;box-shadow:none}.message-main{min-width:0}.message-meta{display:flex;align-items:center;gap:8px;margin:0 0 6px 2px;color:#9290a7;font-size:11px}.user .message-meta{justify-content:flex-end;margin-right:2px}.message-author{color:#5d5578;font-weight:700}.bubble{padding:13px 16px;border:1px solid rgba(132,108,196,.16);border-radius:6px 16px 16px 16px;background:rgba(255,255,255,.86);color:#2c2740;font-size:14px;line-height:1.7;white-space:pre-wrap;overflow-wrap:anywhere;box-shadow:0 8px 24px rgba(76,56,120,.07)}.user .bubble{border-color:transparent;border-radius:16px 6px 16px 16px;background:linear-gradient(135deg,#8061dd,#6045a9);color:#fff;box-shadow:0 10px 24px rgba(96,69,169,.2)}.actions{display:flex;gap:3px;margin-top:7px}.user .actions{justify-content:flex-end}.actions button{padding:3px 7px;border:0;border-radius:6px;background:transparent;color:#8a83a0;font-size:11px;cursor:pointer}.actions button:hover{background:#f0ebfb;color:#6145a7}.actions span{font-size:12px}.cursor{display:inline-block;margin-left:3px;color:#7657c9;animation:blink 1s step-end infinite}.user .cursor{color:#e7dcff}@keyframes blink{50%{opacity:0}}@media (max-width:640px){.chat-message{max-width:96%;gap:8px;margin:16px 0}.message-avatar{flex-basis:28px;width:28px;height:28px;border-radius:9px;font-size:11px}.bubble{padding:11px 13px;font-size:13px}}
</style>
