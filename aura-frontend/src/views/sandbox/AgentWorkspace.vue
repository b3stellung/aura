<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  Send, MessageSquare, User, Copy, Check, Settings, Trash2, ArrowLeft
} from 'lucide-vue-next'
import { useAgentStore } from '@/stores/agent'
import { useConversationStore } from '@/stores/conversation'
import type { Message } from '@/types/conversation'
import { streamChat, type RunEvent } from '@/api/chat'
import ChainOfThought from '@/components/maas/ChainOfThought.vue'
import type { COTStep } from '@/components/maas/ChainOfThought.vue'

const route = useRoute()
const router = useRouter()
const agentStore = useAgentStore()
const conversationStore = useConversationStore()

const messageInput = ref('')
const messagesContainer = ref<HTMLElement | null>(null)
const isStreaming = ref(false)
const showConfig = ref(false)
const copiedId = ref<string | null>(null)
const cotSteps = ref<COTStep[]>([])
const isCOTStreaming = ref(false)

const conversationId = computed(() => route.params.id as string)
const conversation = computed(() => {
  return conversationStore.conversations.find(c => c.id === conversationId.value)
})

const currentAgent = computed(() => {
  if (conversation.value) {
    return agentStore.getAgent(conversation.value.agentId)
  }
  return agentStore.agents[0]
})

const configForm = ref({
  systemPrompt: currentAgent.value?.systemPrompt || '',
  temperature: currentAgent.value?.temperature || 0.7,
  maxTokens: currentAgent.value?.maxTokens || 4096,
})

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

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

  isStreaming.value = true
  cotSteps.value = []
  isCOTStreaming.value = true
  const assistantMessage: Message = {
    id: (Date.now() + 1).toString(),
    role: 'assistant',
    content: '',
    timestamp: new Date().toISOString(),
    isStreaming: true,
  }
  conversationStore.addMessage(conversation.value.id, assistantMessage)

  try {
    const abortController = new AbortController()
    
    await streamChat(
      { content: userMessage.content, sessionId: conversation.value.id },
      {
        onDelta: (delta: string) => {
          assistantMessage.content += delta
          scrollToBottom()
        },
        onThought: (thought: string) => {
          cotSteps.value.push({
            id: `thought-${Date.now()}`,
            type: 'thought',
            content: thought,
            timestamp: Date.now(),
            stepNumber: cotSteps.value.length + 1
          })
          scrollToBottom()
        },
        onTool: (tool) => {
          cotSteps.value.push({
            id: `tool-${Date.now()}`,
            type: 'tool',
            content: `调用工具: ${tool.name}`,
            timestamp: Date.now(),
            toolName: tool.name,
            toolParams: tool.arguments
          })
          scrollToBottom()
        },
        onObservation: (result: unknown) => {
          cotSteps.value.push({
            id: `observation-${Date.now()}`,
            type: 'observation',
            content: '工具返回结果',
            timestamp: Date.now(),
            toolResult: result
          })
          scrollToBottom()
        },
        onDone: () => {
          assistantMessage.isStreaming = false
          isCOTStreaming.value = false
          conversationStore.updateMessage(conversation.value.id, assistantMessage.id, assistantMessage.content)
          isStreaming.value = false
        },
        onError: (error: string) => {
          assistantMessage.content = `抱歉，发生了错误: ${error}`
          assistantMessage.isStreaming = false
          conversationStore.updateMessage(conversation.value.id, assistantMessage.id, assistantMessage.content)
          isStreaming.value = false
        },
      },
      abortController.signal
    )
  } catch (err) {
    const errorMsg = err instanceof Error ? err.message : '请求失败'
    assistantMessage.content = `抱歉，无法连接到后端服务: ${errorMsg}`
    assistantMessage.isStreaming = false
    conversationStore.updateMessage(conversation.value.id, assistantMessage.id, assistantMessage.content)
    isStreaming.value = false
  }
}

const handleCopy = (messageId: string, content: string) => {
  navigator.clipboard.writeText(content)
  copiedId.value = messageId
  setTimeout(() => { copiedId.value = null }, 2000)
}

const handleDelete = () => {
  if (conversation.value) {
    conversationStore.deleteConversation(conversation.value.id)
    router.push('/sandbox')
  }
}

const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

onMounted(() => { scrollToBottom() })
</script>

<template>
  <div class="h-full flex bg-white">
    
    <!-- Main Chat Area -->
    <div class="flex-1 flex flex-col min-w-0">
      
      <!-- Header -->
      <div class="h-14 flex items-center justify-between px-5 bg-white border-b border-gray-100">
        <div class="flex items-center gap-3">
          <button 
            @click="router.push('/sandbox')"
            class="p-1.5 rounded-lg hover:bg-gray-100 text-gray-400 hover:text-black transition-colors"
          >
            <ArrowLeft :size="16" />
          </button>
          <div class="w-px h-5 bg-gray-200" />
          <div>
            <h3 class="text-sm font-medium text-black">
              {{ conversation?.title || '新对话' }}
            </h3>
            <p class="text-[11px] text-gray-400">{{ currentAgent?.name || '通用助手' }}</p>
          </div>
        </div>
        <div class="flex items-center gap-1">
          <button 
            @click="showConfig = !showConfig"
            class="p-1.5 rounded-lg hover:bg-gray-100 text-gray-400 hover:text-black transition-colors"
            :class="{ 'bg-gray-100 text-black': showConfig }"
          >
            <Settings :size="16" />
          </button>
          <button 
            @click="handleDelete"
            class="p-1.5 rounded-lg hover:bg-gray-100 text-gray-400 hover:text-red-500 transition-colors"
          >
            <Trash2 :size="16" />
          </button>
        </div>
      </div>

      <!-- Messages -->
      <div ref="messagesContainer" class="flex-1 overflow-y-auto p-6">
        
        <!-- Empty State -->
        <div v-if="!conversation || conversation.messages.length === 0" 
             class="flex flex-col items-center justify-center h-full text-center">
          <div class="w-20 h-20 rounded-full bg-gray-100 flex items-center justify-center mb-6">
            <MessageSquare :size="32" class="text-gray-300" />
          </div>
          <h2 class="text-xl font-bold text-black mb-2">
            {{ currentAgent?.name || 'Aura助手' }}
          </h2>
          <p class="text-sm text-gray-500 max-w-sm mb-8">
            {{ currentAgent?.description || '有什么可以帮你的？' }}
          </p>
          <div class="flex flex-wrap justify-center gap-2">
            <button 
              v-for="s in ['帮我写代码', '解释概念', '创意写作']"
              :key="s"
              @click="messageInput = s"
              class="px-4 py-2 rounded-full bg-gray-100 text-sm text-gray-600 
                     hover:bg-gray-200 transition-colors"
            >
              {{ s }}
            </button>
          </div>
        </div>

        <!-- Message List -->
        <template v-else>
          <div class="space-y-6 max-w-3xl mx-auto">
            <div 
              v-for="msg in conversation?.messages" 
              :key="msg.id"
              class="flex gap-4"
              :class="{ 'flex-row-reverse': msg.role === 'user' }"
            >
              <!-- Avatar -->
              <div class="w-8 h-8 rounded-full flex-shrink-0 flex items-center justify-center"
                   :class="msg.role === 'user' ? 'bg-black' : 'bg-gray-100'">
                <User v-if="msg.role === 'user'" :size="14" class="text-white" />
                <MessageSquare v-else :size="14" class="text-gray-500" />
              </div>

              <!-- Message -->
              <div 
                class="max-w-[75%] rounded-2xl px-4 py-3"
                :class="msg.role === 'user' 
                  ? 'bg-black text-white' 
                  : 'bg-gray-50 text-black'"
              >
                <p class="text-sm leading-relaxed whitespace-pre-wrap">{{ msg.content }}</p>
                
                <!-- Streaming -->
                <div v-if="msg.isStreaming" class="flex items-center gap-2 mt-2">
                  <span class="w-1.5 h-1.5 rounded-full bg-gray-400 animate-pulse" />
                  <span class="text-[11px] text-gray-400">生成中...</span>
                </div>

                <!-- Actions -->
                <div v-if="msg.role === 'assistant' && !msg.isStreaming" 
                     class="flex items-center gap-2 mt-2 pt-2 border-t border-gray-200">
                  <button 
                    @click="handleCopy(msg.id, msg.content)"
                    class="flex items-center gap-1 text-[11px] text-gray-400 hover:text-black transition-colors"
                  >
                    <Check v-if="copiedId === msg.id" :size="10" class="text-green-500" />
                    <Copy v-else :size="10" />
                    {{ copiedId === msg.id ? '已复制' : '复制' }}
                  </button>
                </div>
                
                <!-- Chain of Thought -->
                <div v-if="msg.role === 'assistant' && cotSteps.length > 0 && !msg.isStreaming" 
                     class="mt-3 pt-3 border-t border-gray-200">
                  <ChainOfThought 
                    :steps="cotSteps" 
                    :is-streaming="isCOTStreaming"
                  />
                </div>
              </div>
            </div>
          </div>
        </template>
      </div>

      <!-- Input -->
      <div class="p-5 bg-white border-t border-gray-100">
        <div class="max-w-3xl mx-auto">
          <div class="flex items-end gap-3 bg-gray-50 rounded-2xl p-3 border border-gray-200 
                      focus-within:border-black transition-colors">
            <textarea 
              v-model="messageInput"
              @keydown="handleKeydown"
              class="flex-1 bg-transparent resize-none text-sm text-black
                     placeholder:text-gray-400 focus:outline-none min-h-[20px] max-h-[100px]"
              placeholder="输入消息..."
              rows="1"
              :disabled="isStreaming"
            />
            <button 
              @click="handleSend"
              :disabled="!messageInput.trim() || isStreaming"
              class="p-2.5 rounded-xl transition-all flex-shrink-0"
              :class="messageInput.trim() && !isStreaming
                ? 'bg-black text-white hover:bg-gray-800' 
                : 'bg-gray-200 text-gray-400 cursor-not-allowed'"
            >
              <Send :size="16" />
            </button>
          </div>
          <p class="text-[11px] text-gray-400 mt-2 text-center">
            Enter 发送 · Shift + Enter 换行
          </p>
        </div>
      </div>
    </div>

    <!-- Config Panel -->
    <div v-if="showConfig" 
         class="w-[280px] bg-white border-l border-gray-100 overflow-y-auto">
      <div class="p-5">
        <h3 class="text-sm font-bold text-black mb-5">配置</h3>

        <div class="space-y-5">
          <div>
            <label class="block text-xs font-medium text-gray-500 mb-2">System Prompt</label>
            <textarea 
              v-model="configForm.systemPrompt"
              class="w-full h-24 p-3 bg-gray-50 rounded-xl text-xs text-black
                     placeholder:text-gray-400 border border-gray-200
                     focus:border-black focus:outline-none resize-none transition-colors"
            />
          </div>

          <div>
            <label class="block text-xs font-medium text-gray-500 mb-2">
              Temperature: <span class="text-black">{{ configForm.temperature }}</span>
            </label>
            <input 
              v-model.number="configForm.temperature"
              type="range" min="0" max="2" step="0.1"
              class="w-full accent-black"
            />
          </div>

          <div>
            <label class="block text-xs font-medium text-gray-500 mb-2">Max Tokens</label>
            <input 
              v-model.number="configForm.maxTokens"
              type="number"
              class="w-full h-9 px-3 bg-gray-50 rounded-lg text-xs text-black
                     border border-gray-200 focus:border-black focus:outline-none transition-colors"
            />
          </div>

          <div>
            <label class="block text-xs font-medium text-gray-500 mb-2">Tools</label>
            <div class="space-y-2">
              <label v-for="t in ['web_search', 'terminal', 'file']" :key="t"
                     class="flex items-center gap-2 text-xs text-black cursor-pointer">
                <input type="checkbox" checked class="rounded border-gray-300 accent-black" />
                {{ t }}
              </label>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
