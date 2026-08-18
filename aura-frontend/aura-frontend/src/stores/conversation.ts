import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Conversation, Message, ConversationList } from '@/types/conversation'

export const useConversationStore = defineStore('conversation', () => {
  const conversations = ref<Conversation[]>([])
  const currentConversation = ref<Conversation | null>(null)

  // Group conversations by date
  const groupedConversations = computed<ConversationList>(() => {
    const now = new Date()
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
    const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000)

    const sorted = [...conversations.value].sort(
      (a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
    )

    return {
      today: sorted.filter(c => new Date(c.updatedAt) >= today),
      yesterday: sorted.filter(c => {
        const date = new Date(c.updatedAt)
        return date >= yesterday && date < today
      }),
      older: sorted.filter(c => new Date(c.updatedAt) < yesterday),
    }
  })

  function loadConversations() {
    const saved = localStorage.getItem('aura-conversations')
    if (saved) {
      conversations.value = JSON.parse(saved)
    }
  }

  function saveConversations() {
    localStorage.setItem('aura-conversations', JSON.stringify(conversations.value))
  }

  function createConversation(agentId: string, agentName: string, agentIcon: string): Conversation {
    const conversation: Conversation = {
      id: generateId(),
      title: '新对话',
      agentId,
      agentName,
      agentIcon,
      messages: [],
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      messageCount: 0,
    }
    conversations.value.unshift(conversation)
    currentConversation.value = conversation
    saveConversations()
    return conversation
  }

  function setCurrentConversation(id: string) {
    const conversation = conversations.value.find(c => c.id === id)
    if (conversation) {
      currentConversation.value = conversation
    }
  }

  function addMessage(conversationId: string, message: Message) {
    const conversation = conversations.value.find(c => c.id === conversationId)
    if (conversation) {
      conversation.messages.push(message)
      conversation.messageCount = conversation.messages.length
      conversation.updatedAt = new Date().toISOString()
      conversation.lastMessage = message.content.slice(0, 100)
      
      // Update title from first user message
      if (message.role === 'user' && conversation.messages.length === 1) {
        conversation.title = message.content.slice(0, 50) || '新对话'
      }
      
      saveConversations()
    }
  }

  function updateMessage(conversationId: string, messageId: string, content: string) {
    const conversation = conversations.value.find(c => c.id === conversationId)
    if (conversation) {
      const message = conversation.messages.find(m => m.id === messageId)
      if (message) {
        message.content = content
        saveConversations()
      }
    }
  }

  function deleteConversation(id: string) {
    conversations.value = conversations.value.filter(c => c.id !== id)
    if (currentConversation.value?.id === id) {
      currentConversation.value = null
    }
    saveConversations()
  }

  // Initialize
  loadConversations()

  return {
    conversations,
    currentConversation,
    groupedConversations,
    loadConversations,
    createConversation,
    setCurrentConversation,
    addMessage,
    updateMessage,
    deleteConversation,
  }
})

function generateId(): string {
  return Date.now().toString(36) + Math.random().toString(36).slice(2)
}
