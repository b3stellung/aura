<script setup lang="ts">
import { useRouter } from 'vue-router'
import { 
  Bot, Shirt, Code, GraduationCap, Plus, Search, ArrowRight 
} from 'lucide-vue-next'
import { ref } from 'vue'
import { useAgentStore } from '@/stores/agent'
import { useConversationStore } from '@/stores/conversation'

const router = useRouter()
const agentStore = useAgentStore()
const conversationStore = useConversationStore()

const searchQuery = ref('')

const getIcon = (iconName: string) => {
  const iconMap: Record<string, any> = {
    Bot, Shirt, Code, GraduationCap
  }
  return iconMap[iconName] || Bot
}

const handleStartChat = (agentId: string) => {
  const agent = agentStore.getAgent(agentId)
  if (agent) {
    const conv = conversationStore.createConversation(agent.id, agent.name, agent.icon)
    router.push(`/sandbox/${conv.id}`)
  }
}
</script>

<template>
  <div class="p-6 max-w-4xl mx-auto">
    <!-- Header -->
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-display font-semibold text-text-primary mb-1">Agent</h1>
        <p class="text-body text-text-secondary">选择一个AI助手开始对话</p>
      </div>
    </div>

    <!-- Search -->
    <div class="relative mb-6">
      <Search :size="16" :stroke-width="2" 
              class="absolute left-3 top-1/2 -translate-y-1/2 text-text-tertiary" />
      <input 
        v-model="searchQuery"
        class="w-full h-10 pl-10 pr-4 bg-bg-secondary rounded-input text-sm text-text-primary
               placeholder:text-text-tertiary
               focus:outline-none focus:ring-2 focus:ring-brand-primary"
        style="box-shadow: var(--shadow-card)"
        placeholder="搜索Agent..." 
      />
    </div>

    <!-- Agent Grid -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div
        v-for="agent in agentStore.agents"
        :key="agent.id"
        class="bg-bg-secondary rounded-card p-5 hover:bg-bg-hover transition-colors cursor-pointer"
        style="box-shadow: var(--shadow-card)"
        @click="handleStartChat(agent.id)"
      >
        <div class="flex items-start gap-4">
          <div class="w-12 h-12 rounded-card bg-brand-light flex items-center justify-center flex-shrink-0">
            <component :is="getIcon(agent.icon)" :size="24" :stroke-width="2" class="text-brand-primary" />
          </div>
          <div class="flex-1 min-w-0">
            <div class="flex items-center justify-between mb-1">
              <h3 class="text-h3 font-semibold text-text-primary">{{ agent.name }}</h3>
              <ArrowRight :size="16" :stroke-width="2" class="text-text-tertiary" />
            </div>
            <p class="text-body text-text-secondary mb-3">{{ agent.description }}</p>
            <div class="flex flex-wrap gap-2">
              <span 
                v-for="tool in agent.tools"
                :key="tool"
                class="px-2 py-0.5 bg-bg-tertiary rounded-pill text-xs text-text-secondary"
              >
                {{ tool }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
