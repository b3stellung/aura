<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import MaaSWorkspace from '@/components/maas/MaaSWorkspace.vue'
import ChatTimeline from '@/components/maas/ChatTimeline.vue'
import ChatComposer from '@/components/maas/ChatComposer.vue'
import RunInspector from '@/components/maas/RunInspector.vue'
import { useChatStore } from '@/stores/chat'

const chat = useChatStore()
const { messages, isStreaming, error } = storeToRefs(chat)
const runEvents = computed(() => [])

onMounted(() => chat.scrollToBottom())

function send(text: string) {
  return chat.sendMessage(text)
}
</script>

<template>
  <MaaSWorkspace>
    <template #timeline>
      <ChatTimeline
        :messages="messages"
        :streaming="isStreaming"
        :error="error"
        @retry="chat.sendMessage($event.content)"
      />
    </template>
    <template #composer>
      <ChatComposer
        :busy="isStreaming"
        @send="send"
        @stop="chat.stopStreaming"
      />
    </template>
    <template #context>
      <RunInspector :events="runEvents" :running="isStreaming" />
    </template>
  </MaaSWorkspace>
</template>
