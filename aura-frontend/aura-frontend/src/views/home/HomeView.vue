<script setup lang="ts">
import { useRouter } from 'vue-router'
import { 
  Plus, Bot, Shirt, Code, GraduationCap, 
  ArrowRight, MessageSquare 
} from 'lucide-vue-next'
import { useAgentStore } from '@/stores/agent'
import { useConversationStore } from '@/stores/conversation'

const router = useRouter()
const agentStore = useAgentStore()
const conversationStore = useConversationStore()

const quickActions = [
  { icon: Plus, label: '新建对话', action: () => router.push('/sandbox') },
  { icon: Bot, label: '浏览Agent', action: () => router.push('/sandbox') },
  { icon: MessageSquare, label: '查看记录', action: () => router.push('/history') },
]

const getIcon = (iconName: string) => {
  const iconMap: Record<string, any> = {
    Bot, Shirt, Code, GraduationCap
  }
  return iconMap[iconName] || Bot
}
</script>

<template>
  <div class="p-6 max-w-4xl mx-auto">
    <!-- Welcome Section -->
    <div class="mb-8">
      <h1 class="text-display font-semibold text-text-primary mb-2">
        欢迎回来，{{ '畅哥' }}
      </h1>
      <p class="text-body text-text-secondary">
        准备好开始今天的AI之旅了吗？
      </p>
    </div>

    <!-- Quick Actions -->
    <div class="grid grid-cols-3 gap-4 mb-8">
      <button
        v-for="action in quickActions"
        :key="action.label"
        @click="action.action()"
        class="bg-bg-secondary rounded-card p-4 flex items-center gap-3 
               hover:bg-bg-hover transition-colors text-left"
        style="box-shadow: var(--shadow-card)"
      >
        <div class="w-10 h-10 rounded-btn bg-brand-light flex items-center justify-center">
          <component :is="action.icon" :size="20" :stroke-width="2" class="text-brand-primary" />
        </div>
        <span class="text-sm font-semibold text-text-primary">{{ action.label }}</span>
      </button>
    </div>

    <!-- Recent Conversations -->
    <div class="mb-8">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-h3 font-semibold text-text-primary">最近对话</h2>
        <router-link to="/history" 
                     class="text-sm text-brand-primary hover:underline flex items-center gap-1">
          查看全部
          <ArrowRight :size="14" :stroke-width="2" />
        </router-link>
      </div>

      <div v-if="conversationStore.conversations.length === 0" 
           class="bg-bg-secondary rounded-card p-8 text-center"
           style="box-shadow: var(--shadow-card)">
        <MessageSquare :size="48" :stroke-width="1.5" class="mx-auto text-text-tertiary mb-4" />
        <p class="text-body text-text-secondary mb-4">还没有对话记录</p>
        <button 
          @click="router.push('/sandbox')"
          class="px-4 py-2 bg-brand-primary hover:bg-brand-hover text-white font-semibold text-sm rounded-btn transition-colors"
        >
          开始新对话
        </button>
      </div>

      <div v-else class="space-y-2">
        <router-link
          v-for="conv in conversationStore.conversations.slice(0, 3)"
          :key="conv.id"
          :to="`/sandbox/${conv.id}`"
          class="bg-bg-secondary rounded-card p-4 flex items-center gap-3 
                 hover:bg-bg-hover transition-colors"
          style="box-shadow: var(--shadow-card)"
        >
          <div class="w-10 h-10 rounded-btn bg-brand-light flex items-center justify-center flex-shrink-0">
            <component :is="getIcon(conv.agentIcon)" :size="20" :stroke-width="2" class="text-brand-primary" />
          </div>
          <div class="flex-1 min-w-0">
            <h3 class="text-sm font-semibold text-text-primary truncate">{{ conv.title }}</h3>
            <p class="text-xs text-text-secondary truncate">{{ conv.lastMessage || '暂无消息' }}</p>
          </div>
          <span class="text-xs text-text-tertiary flex-shrink-0">{{ conv.messageCount }}条</span>
        </router-link>
      </div>
    </div>

    <!-- Recommended Agents -->
    <div>
      <h2 class="text-h3 font-semibold text-text-primary mb-4">推荐 Agent</h2>
      <div class="grid grid-cols-2 gap-4">
        <button
          v-for="agent in agentStore.agents.slice(0, 4)"
          :key="agent.id"
          @click="router.push(`/sandbox/${agent.id}`)"
          class="bg-bg-secondary rounded-card p-4 text-left hover:bg-bg-hover transition-colors"
          style="box-shadow: var(--shadow-card)"
        >
          <div class="flex items-center gap-3 mb-2">
            <div class="w-10 h-10 rounded-btn bg-brand-light flex items-center justify-center">
              <component :is="getIcon(agent.icon)" :size="20" :stroke-width="2" class="text-brand-primary" />
            </div>
            <h3 class="text-sm font-semibold text-text-primary">{{ agent.name }}</h3>
          </div>
          <p class="text-xs text-text-secondary line-clamp-2">{{ agent.description }}</p>
        </button>
      </div>
    </div>
  </div>
</template>
