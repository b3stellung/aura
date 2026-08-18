<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { 
  Search, Filter, MessageSquare, Bot, Shirt, Code, GraduationCap,
  Trash2, Clock
} from 'lucide-vue-next'
import { useConversationStore } from '@/stores/conversation'

const router = useRouter()
const conversationStore = useConversationStore()

const searchQuery = ref('')
const filterType = ref('all') // all, today, week, month

const getIcon = (iconName: string) => {
  const iconMap: Record<string, any> = {
    Bot, Shirt, Code, GraduationCap
  }
  return iconMap[iconName] || Bot
}

const filteredConversations = computed(() => {
  let convs = conversationStore.conversations

  // Search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    convs = convs.filter(c => 
      c.title.toLowerCase().includes(query) ||
      c.agentName.toLowerCase().includes(query) ||
      c.lastMessage?.toLowerCase().includes(query)
    )
  }

  // Time filter
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const weekAgo = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000)
  const monthAgo = new Date(today.getTime() - 30 * 24 * 60 * 60 * 1000)

  if (filterType.value === 'today') {
    convs = convs.filter(c => new Date(c.updatedAt) >= today)
  } else if (filterType.value === 'week') {
    convs = convs.filter(c => new Date(c.updatedAt) >= weekAgo)
  } else if (filterType.value === 'month') {
    convs = convs.filter(c => new Date(c.updatedAt) >= monthAgo)
  }

  return convs.sort((a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime())
})

const formatTime = (dateStr: string) => {
  const date = new Date(dateStr)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000)

  if (date >= today) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else if (date >= yesterday) {
    return '昨天'
  } else {
    return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
  }
}

const handleDelete = (id: string, e: Event) => {
  e.stopPropagation()
  conversationStore.deleteConversation(id)
}

const filters = [
  { value: 'all', label: '全部' },
  { value: 'today', label: '今天' },
  { value: 'week', label: '本周' },
  { value: 'month', label: '本月' },
]
</script>

<template>
  <div class="p-6 max-w-4xl mx-auto">
    <!-- Header -->
    <div class="mb-6">
      <h1 class="text-display font-semibold text-text-primary mb-1">历史记录</h1>
      <p class="text-body text-text-secondary">查看和管理你的对话记录</p>
    </div>

    <!-- Search & Filter -->
    <div class="flex items-center gap-4 mb-6">
      <div class="flex-1 relative">
        <Search :size="16" :stroke-width="2" 
                class="absolute left-3 top-1/2 -translate-y-1/2 text-text-tertiary" />
        <input 
          v-model="searchQuery"
          class="w-full h-10 pl-10 pr-4 bg-bg-secondary rounded-input text-sm text-text-primary
                 placeholder:text-text-tertiary
                 focus:outline-none focus:ring-2 focus:ring-brand-primary"
          style="box-shadow: var(--shadow-card)"
          placeholder="搜索对话..." 
        />
      </div>
      <div class="flex items-center gap-1 bg-bg-secondary rounded-btn p-1"
           style="box-shadow: var(--shadow-card)">
        <button 
          v-for="filter in filters"
          :key="filter.value"
          @click="filterType = filter.value"
          class="px-3 py-1.5 text-xs font-semibold rounded-btn transition-colors"
          :class="filterType === filter.value 
            ? 'bg-brand-primary text-white' 
            : 'text-text-secondary hover:bg-bg-hover'"
        >
          {{ filter.label }}
        </button>
      </div>
    </div>

    <!-- Conversation List -->
    <div v-if="filteredConversations.length === 0" 
         class="bg-bg-secondary rounded-card p-12 text-center"
         style="box-shadow: var(--shadow-card)">
      <MessageSquare :size="48" :stroke-width="1.5" class="mx-auto text-text-tertiary mb-4" />
      <p class="text-body text-text-secondary mb-2">暂无对话记录</p>
      <p class="text-sm text-text-tertiary">
        {{ searchQuery ? '没有找到匹配的对话' : '开始你的第一次AI对话吧' }}
      </p>
    </div>

    <div v-else class="space-y-2">
      <div 
        v-for="conv in filteredConversations"
        :key="conv.id"
        @click="router.push(`/sandbox/${conv.id}`)"
        class="bg-bg-secondary rounded-card p-4 flex items-center gap-3 
               hover:bg-bg-hover transition-colors cursor-pointer"
        style="box-shadow: var(--shadow-card)"
      >
        <!-- Agent Icon -->
        <div class="w-10 h-10 rounded-btn bg-brand-light flex items-center justify-center flex-shrink-0">
          <component :is="getIcon(conv.agentIcon)" :size="20" :stroke-width="2" class="text-brand-primary" />
        </div>

        <!-- Content -->
        <div class="flex-1 min-w-0">
          <div class="flex items-center justify-between mb-1">
            <h3 class="text-sm font-semibold text-text-primary truncate">{{ conv.title }}</h3>
            <span class="text-xs text-text-tertiary flex-shrink-0 ml-2">
              {{ formatTime(conv.updatedAt) }}
            </span>
          </div>
          <p class="text-xs text-text-secondary truncate">{{ conv.lastMessage || '暂无消息' }}</p>
          <div class="flex items-center gap-2 mt-1">
            <span class="text-[10px] text-text-tertiary">{{ conv.agentName }}</span>
            <span class="text-[10px] text-text-tertiary">{{ conv.messageCount }}条消息</span>
          </div>
        </div>

        <!-- Delete Button -->
        <button 
          @click="handleDelete(conv.id, $event)"
          class="p-1.5 rounded-btn hover:bg-bg-hover text-text-tertiary hover:text-error opacity-0 group-hover:opacity-100 transition-opacity"
        >
          <Trash2 :size="14" :stroke-width="2" />
        </button>
      </div>
    </div>

    <!-- Stats -->
    <div class="mt-8 grid grid-cols-3 gap-4">
      <div class="bg-bg-secondary rounded-card p-4 text-center"
           style="box-shadow: var(--shadow-card)">
        <p class="text-2xl font-semibold text-text-primary">
          {{ conversationStore.conversations.length }}
        </p>
        <p class="text-xs text-text-secondary">总对话数</p>
      </div>
      <div class="bg-bg-secondary rounded-card p-4 text-center"
           style="box-shadow: var(--shadow-card)">
        <p class="text-2xl font-semibold text-text-primary">
          {{ conversationStore.conversations.reduce((sum, c) => sum + c.messageCount, 0) }}
        </p>
        <p class="text-xs text-text-secondary">总消息数</p>
      </div>
      <div class="bg-bg-secondary rounded-card p-4 text-center"
           style="box-shadow: var(--shadow-card)">
        <p class="text-2xl font-semibold text-text-primary">
          {{ conversationStore.groupedConversations.today.length }}
        </p>
        <p class="text-xs text-text-secondary">今日对话</p>
      </div>
    </div>
  </div>
</template>
