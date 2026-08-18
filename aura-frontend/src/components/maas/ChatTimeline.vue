<script setup lang="ts">
import type { ChatMessage } from '@/types'
import ChatMessageView from './ChatMessage.vue'
defineProps<{ messages: ChatMessage[]; streaming?: boolean; error?: string | null }>()
const emit = defineEmits<{ retry: [message: ChatMessage]; copy: [text: string]; run: [message: ChatMessage] }>()
</script>
<template>
  <section class="timeline" aria-live="polite">
    <div v-if="!messages.length" class="empty"><div class="empty-mark" aria-hidden="true">✦</div><h2>和 Aura 开始对话</h2><p>告诉我你正在思考什么，或从一个问题开始。</p></div>
    <ChatMessageView v-for="(message, index) in messages" :key="message.id || index" :message="message" :streaming="streaming && index === messages.length - 1" @retry="emit('retry',$event)" @copy="emit('copy',$event)" @run="emit('run',$event)" />
    <div v-if="error" class="error">{{ error }}</div>
  </section>
</template>
<style scoped>
.timeline{height:100%;padding:28px clamp(16px,4vw,48px) 120px;overflow:auto;scroll-behavior:smooth;background:radial-gradient(circle at 50% -10%,rgba(161,130,255,.08),transparent 42%)}.empty{display:grid;place-items:center;max-width:410px;margin:15vh auto 0;padding:34px 28px;text-align:center;border:1px solid rgba(132,108,196,.14);border-radius:22px;background:rgba(255,255,255,.52);box-shadow:0 16px 40px rgba(76,56,120,.06)}.empty-mark{display:grid;place-items:center;width:46px;height:46px;margin-bottom:15px;border-radius:15px;background:#eee8ff;color:#7658c4;font-size:22px}.empty h2{margin:0;color:#3b3158;font-size:20px}.empty p{margin:8px 0 0;color:#9088a4;font-size:13px}.error{margin:18px auto;max-width:760px;padding:11px 14px;border:1px solid #f3c7c5;border-radius:11px;color:#ad4847;background:#fff7f6;font-size:13px}@media (max-width:640px){.timeline{padding:18px 12px 110px}.empty{margin-top:10vh;padding:28px 20px}}
</style>
