<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  Send, Bot, User, Copy, Check, ChevronRight, Wrench,
  Settings, Save, Trash2, ArrowLeft, Loader2
} from 'lucide-vue-next'
import { useAgentStore } from '@/stores/agent'
import { useConversationStore } from '@/stores/conversation'
import type { Message } from '@/types/conversation'

const route = useRoute()
const router = useRouter()
const agentStore = useAgentStore()
const conversationStore = useConversationStore()

const messageInput = ref('')
const messagesContainer = ref<HTMLElement | null>(null)
const isStreaming = ref(false)
const showConfig = ref(false)
const copiedId = ref<string | null>(null)

// Get conversation or create new one
const conversationId = computed(() => route.params.id as string)
const conversation = computed(() => {
  return conversationStore.conversations.find(c => c.id === conversationId.value)
})

// Mock agent for demo
const currentAgent = computed(() => {
  if (conversation.value) {
    return agentStore.getAgent(conversation.value.agentId)
  }
  return agentStore.agents[0]
})

// Config form
const configForm = ref({
  systemPrompt: currentAgent.value?.systemPrompt || '',
  temperature: currentAgent.value?.temperature || 0.7,
  maxTokens: currentAgent.value?.maxTokens || 4096,
})

// Scroll to bottom
const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// Send message
const handleSend = async () => {
  if (!messageInput.value.trim() || isStreaming.value || !conversation.value) return

  const userMessage: Message = {
    id: Date.now().toString(),
    role: 'user',
    content: messageInput.value.trim(),
    timestamp: new Date().toISOString(),
  }

  conversationStore.addMessage(conversation.value.id, userMessage)
  messageInput.value = ''
  await scrollToBottom()

  // Mock streaming response
  isStreaming.value = true
  const assistantMessage: Message = {
    id: (Date.now() + 1).toString(),
    role: 'assistant',
    content: '',
    timestamp: new Date().toISOString(),
    isStreaming: true,
  }
  conversationStore.addMessage(conversation.value.id, assistantMessage)

  // Simulate streaming
  const mockResponse = `你好！我是${currentAgent.value?.name || 'Aura助手'}。

收到你的消息了："${userMessage.content}"

这是一个模拟回复，实际项目中会连接到后端AI服务。我会尽力帮助你解决问题！

有什么其他需要帮助的吗？`

  for (let i = 0; i < mockResponse.length; i++) {
    await new Promise(resolve => setTimeout(resolve, 20))
    assistantMessage.content += mockResponse[i]
    await scrollToBottom()
  }

  assistantMessage.isStreaming = false
  conversationStore.updateMessage(
    conversation.value.id, 
    assistantMessage.id, 
    assistantMessage.content
  )
  isStreaming.value = false
}

// Copy message
const handleCopy = (messageId: string, content: string) => {
  navigator.clipboard.writeText(content)
  copiedId.value = messageId
  setTimeout(() => {
    copiedId.value = null
  }, 2000)
}

// Delete conversation
const handleDelete = () => {
  if (conversation.value) {
    conversationStore.deleteConversation(conversation.value.id)
    router.push('/sandbox')
  }
}

// Handle enter key
const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

onMounted(() => {
  scrollToBottom()
})
</script>

<template>
  <div class="flex h-full">
    <!-- Main Chat Area -->
    <div class="flex-1 flex flex-col min-w-0">
      <!-- Chat Header -->
      <div class="h-[52px] flex items-center justify-between px-4 bg-bg-secondary"
           style="box-shadow: 0 1px 0 0 rgba(0,0,0,0.08)">
        <div class="flex items-center gap-3">
          <button 
            @click="router.push('/sandbox')"
            class="p-1.5 rounded-btn hover:bg-bg-hover text-text-secondary"
          >
            <ArrowLeft :size="16" :stroke-width="2" />
          </button>
          <div>
            <h3 class="text-sm font-semibold text-text-primary">
              {{ conversation?.title || '新对话' }}
            </h3>
            <p class="text-xs text-text-secondary">
              {{ currentAgent?.name || '通用助手' }}
            </p>
          </div>
        </div>
        <div class="flex items-center gap-1">
          <button 
            @click="showConfig = !showConfig"
            class="p-1.5 rounded-btn hover:bg-bg-hover text-text-secondary"
            :class="{ 'bg-bg-hover': showConfig }"
          >
            <Settings :size="16" :stroke-width="2" />
          </button>
          <button 
            @click="handleDelete"
            class="p-1.5 rounded-btn hover:bg-bg-hover text-text-tertiary hover:text-error"
          >
            <Trash2 :size="16" :stroke-width="2" />
          </button>
        </div>
      </div>

      <!-- Messages -->
      <div 
        ref="messagesContainer"
        class="flex-1 overflow-y-auto p-4 space-y-4"
      >
        <!-- Empty State -->
        <div v-if="!conversation || conversation.messages.length === 0" 
             class="flex flex-col items-center justify-center h-full text-center">
          <div class="w-16 h-16 rounded-card bg-brand-light flex items-center justify-center mb-4">
            <Bot :size="32" :stroke-width="1.5" class="text-brand-primary" />
          </div>
          <h2 class="text-h2 font-semibold text-text-primary mb-2">
            {{ currentAgent?.name || 'Aura助手' }}
          </h2>
          <p class="text-body text-text-secondary max-w-sm">
            {{ currentAgent?.description || '有什么可以帮你的？' }}
          </p>
        </div>

        <!-- Message List -->
        <template v-else>
          <div 
            v-for="msg in conversation?.messages" 
            :key="msg.id"
            class="flex gap-3"
            :class="{ 'justify-end': msg.role === 'user' }"
          >
            <!-- Assistant Avatar -->
            <div v-if="msg.role === 'assistant'" 
                 class="w-8 h-8 rounded-full bg-bg-tertiary flex items-center justify-center flex-shrink-0">
              <Bot :size="16" :stroke-width="2" class="text-text-secondary" />
            </div>

            <!-- Message Bubble -->
            <div 
              class="max-w-[70%] rounded-card px-4 py-2.5"
              :class="msg.role === 'user' 
                ? 'bg-brand-primary text-white' 
                : 'bg-bg-secondary text-text-primary'"
              :style="msg.role === 'assistant' ? 'box-shadow: 0 0 0 1px rgba(0,0,0,0.08)' : {}"
            >
              <p class="text-sm leading-relaxed whitespace-pre-wrap">{{ msg.content }}</p>
              
              <!-- Streaming indicator -->
              <div v-if="msg.isStreaming" class="flex items-center gap-1 mt-2">
                <Loader2 :size="12" :stroke-width="2" class="animate-spin text-text-tertiary" />
                <span class="text-xs text-text-tertiary">生成中...</span>
              </div>

              <!-- Actions -->
              <div v-if="msg.role === 'assistant' && !msg.isStreaming" 
                   class="flex items-center gap-2 mt-2 pt-2"
                   style="box-shadow: 0 -1px 0 0 rgba(0,0,0,0.05)">
                <button 
                  @click="handleCopy(msg.id, msg.content)"
                  class="flex items-center gap-1 text-xs text-text-tertiary hover:text-text-secondary"
                >
                  <Check v-if="copiedId === msg.id" :size="12" :stroke-width="2" />
                  <Copy v-else :size="12" :stroke-width="2" />
                  {{ copiedId === msg.id ? '已复制' : '复制' }}
                </button>
              </div>
            </div>

            <!-- User Avatar -->
            <div v-if="msg.role === 'user'" 
                 class="w-8 h-8 rounded-full bg-brand-light flex items-center justify-center flex-shrink-0">
              <User :size="16" :stroke-width="2" class="text-brand-primary" />
            </div>
          </div>
        </template>
      </div>

      <!-- Input Area -->
      <div class="p-4 bg-bg-secondary" style="box-shadow: 0 -1px 0 0 rgba(0,0,0,0.08)">
        <div class="max-w-3xl mx-auto">
          <div class="flex items-end gap-3 bg-bg-tertiary rounded-card p-3">
            <textarea 
              v-model="messageInput"
              @keydown="handleKeydown"
              class="flex-1 bg-transparent resize-none text-sm text-text-primary
                     placeholder:text-text-tertiary focus:outline-none min-h-[24px] max-h-[120px]"
              placeholder="输入消息..."
              rows="1"
              :disabled="isStreaming"
            />
            <button 
              @click="handleSend"
              :disabled="!messageInput.trim() || isStreaming"
              class="p-2 rounded-btn transition-colors flex-shrink-0"
              :class="messageInput.trim() && !isStreaming
                ? 'bg-brand-primary text-white hover:bg-brand-hover' 
                : 'bg-bg-hover text-text-tertiary cursor-not-allowed'"
            >
              <Send :size="16" :stroke-width="2" />
            </button>
          </div>
          <p class="text-[11px] text-text-tertiary mt-2 text-center">
            按 Enter 发送，Shift + Enter 换行
          </p>
        </div>
      </div>
    </div>

    <!-- Config Panel (Right Side) -->
    <div 
      v-if="showConfig"
      class="w-[300px] bg-bg-secondary overflow-y-auto flex-shrink-0"
      style="box-shadow: -1px 0 0 0 rgba(0,0,0,0.08)"
    >
      <div class="p-4">
        <h3 class="text-sm font-semibold text-text-primary mb-4">配置面板</h3>

        <!-- System Prompt -->
        <div class="mb-4">
          <label class="block text-xs font-semibold text-text-secondary mb-1.5">
            System Prompt
          </label>
          <textarea 
            v-model="configForm.systemPrompt"
            class="w-full h-32 p-2 bg-bg-tertiary rounded-input text-xs text-text-primary
                   placeholder:text-text-tertiary
                   focus:outline-none focus:ring-2 focus:ring-brand-primary resize-none"
            placeholder="设置系统提示词..."
          />
        </div>

        <!-- Temperature -->
        <div class="mb-4">
          <label class="block text-xs font-semibold text-text-secondary mb-1.5">
            Temperature: {{ configForm.temperature }}
          </label>
          <input 
            v-model.number="configForm.temperature"
            type="range" 
            min="0" 
            max="2" 
            step="0.1"
            class="w-full"
          />
          <div class="flex justify-between text-[10px] text-text-tertiary mt-1">
            <span>精确</span>
            <span>创意</span>
          </div>
        </div>

        <!-- Max Tokens -->
        <div class="mb-4">
          <label class="block text-xs font-semibold text-text-secondary mb-1.5">
            Max Tokens
          </label>
          <input 
            v-model.number="configForm.maxTokens"
            type="number" 
            class="w-full h-8 px-2 bg-bg-tertiary rounded-input text-xs text-text-primary
                   focus:outline-none focus:ring-2 focus:ring-brand-primary"
          />
        </div>

        <!-- Tools -->
        <div>
          <label class="block text-xs font-semibold text-text-secondary mb-1.5">
            Tools
          </label>
          <div class="space-y-2">
            <label 
              v-for="tool in ['web_search', 'terminal', 'file']"
              :key="tool"
              class="flex items-center gap-2 text-xs text-text-primary"
            >
              <input type="checkbox" checked class="rounded" />
              <span>{{ tool }}</span>
            </label>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
