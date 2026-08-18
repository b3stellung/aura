import { defineStore } from 'pinia'
import { ref, computed, nextTick } from 'vue'
import type { ChatMessage } from '@/types'
import * as chatApi from '@/api/chat'
import type { ChatSession } from '@/api/chat'

// Mock messages for development
const MOCK_MESSAGES: ChatMessage[] = [
  {
    id: '1',
    role: 'assistant',
    content: 'Hello! I am your AI Stylist. Tell me what you need today and I will help you put together an outfit!',
    timestamp: '10:00',
  },
]

export const useChatStore = defineStore('chat', () => {
  // State
  const messages = ref<ChatMessage[]>([...MOCK_MESSAGES])
  const sessions = ref<ChatSession[]>([])
  const currentSessionId = ref<string | null>(null)
  const isStreaming = ref(false)
  const streamingText = ref('')
  const error = ref<string | null>(null)
  const showHistory = ref(false)

  // Abort controller for cancelling stream
  let abortController: AbortController | null = null

  // Getters
  const messageCount = computed(() => messages.value.length)

  const hasRecommendationMessages = computed(() =>
    messages.value.some((m) => m.recommendation)
  )

  // Actions
  async function fetchHistory() {
    try {
      sessions.value = await chatApi.getChatHistory()
    } catch {
      sessions.value = []
    }
  }

  async function loadSession(sessionId: string) {
    try {
      messages.value = await chatApi.getSessionMessages(sessionId)
      currentSessionId.value = sessionId
    } catch {
      // Keep current messages
    }
    showHistory.value = false
  }

  function startNewSession() {
    messages.value = [...MOCK_MESSAGES]
    currentSessionId.value = null
    showHistory.value = false
  }

  async function sendMessage(content: string) {
    if (!content.trim() || isStreaming.value) return

    // Add user message
    const userMsg: ChatMessage = {
      id: Date.now().toString(),
      role: 'user',
      content: content.trim(),
      timestamp: new Date().toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit',
      }),
    }
    messages.value.push(userMsg)

    // Scroll to bottom
    await scrollToBottom()

    isStreaming.value = true
    streamingText.value = ''
    error.value = null

    // Create placeholder for AI response
    const aiMsgId = (Date.now() + 1).toString()
    const aiMsg: ChatMessage = {
      id: aiMsgId,
      role: 'assistant',
      content: '',
      timestamp: new Date().toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit',
      }),
    }
    messages.value.push(aiMsg)

    try {
      abortController = new AbortController()

      // Try streaming first
      await chatApi.sendMessageStream(
        { content: content.trim(), sessionId: currentSessionId.value || undefined },
        (chunk) => {
          streamingText.value += chunk
          // Update the message in-place
          const msg = messages.value.find((m) => m.id === aiMsgId)
          if (msg) msg.content = streamingText.value
          scrollToBottom()
        },
        (rec) => {
          const msg = messages.value.find((m) => m.id === aiMsgId)
          if (msg) msg.recommendation = rec
        },
        abortController.signal
      )
    } catch (err) {
      const msg = messages.value.find((item) => item.id === aiMsgId)
      if (msg) msg.content = ''
      if ((err as DOMException)?.name !== 'AbortError') {
        error.value = err instanceof Error ? err.message : 'Unable to reach Aura right now.'
      }
    } finally {
      isStreaming.value = false
      streamingText.value = ''
      abortController = null
      await scrollToBottom()
    }
  }

  // Simulate AI response for development without backend
  async function simulateAiResponse(msgId: string, _userInput: string) {
    const responses = [
      '好的，让我为你想想... 根据你的衣橱和风格偏好，我觉得可以试试简约大方的搭配方案。',
      '了解你的需求！我来为你推荐一套适合的穿搭。',
      '根据你的风格画像，我有几个搭配建议给你。',
    ]
    const fullText = responses[Math.floor(Math.random() * responses.length)]

    // Typewriter effect
    for (let i = 0; i < fullText.length; i++) {
      const msg = messages.value.find((m) => m.id === msgId)
      if (!msg) break
      msg.content = fullText.slice(0, i + 1)
      await new Promise((r) => setTimeout(r, 30))
      await scrollToBottom()
    }

    // Sometimes add a recommendation card
    if (Math.random() > 0.4) {
      await new Promise((r) => setTimeout(r, 500))
      const msg = messages.value.find((m) => m.id === msgId)
      if (msg) {
        msg.recommendation = {
          title: '今日推荐搭配',
          items: [],
          matchScore: Math.floor(Math.random() * 20) + 80,
          imageUrl: undefined,
        }
      }
    }
  }

  void simulateAiResponse

  async function stopStreaming() {
    abortController?.abort()
    isStreaming.value = false
    streamingText.value = ''
  }

  function clearMessages() {
    messages.value = [...MOCK_MESSAGES]
    currentSessionId.value = null
  }

  function toggleHistory() {
    showHistory.value = !showHistory.value
  }

  async function scrollToBottom() {
    await nextTick()
    const container = document.getElementById('chat-container')
    if (container) {
      container.scrollTop = container.scrollHeight
    }
  }

  return {
    messages,
    sessions,
    currentSessionId,
    isStreaming,
    streamingText,
    error,
    showHistory,
    messageCount,
    hasRecommendationMessages,
    fetchHistory,
    loadSession,
    startNewSession,
    sendMessage,
    stopStreaming,
    clearMessages,
    toggleHistory,
    scrollToBottom,
  }
})
