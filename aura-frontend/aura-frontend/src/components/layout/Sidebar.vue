<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  Home, Bot, History, Settings, User, LogOut,
  Plus
} from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { useConversationStore } from '@/stores/conversation'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const conversationStore = useConversationStore()

const navItems = [
  { path: '/', icon: Home, label: '首页' },
  { path: '/sandbox', icon: Bot, label: 'Agent' },
  { path: '/history', icon: History, label: '历史记录' },
  { path: '/settings', icon: Settings, label: '设置' },
]

const isActive = (path: string) => {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}

const handleNewChat = () => {
  router.push('/sandbox')
}

const handleLogout = () => {
  authStore.logout()
  router.push('/auth/login')
}
</script>

<template>
  <aside class="w-[240px] h-screen bg-bg-secondary flex flex-col flex-shrink-0"
         style="box-shadow: 1px 0 0 0 rgba(0,0,0,0.08)">
    <!-- Logo -->
    <div class="h-[52px] flex items-center justify-between px-5"
         style="box-shadow: 0 1px 0 0 rgba(0,0,0,0.08)">
      <h1 class="text-[17px] font-semibold text-text-primary">Aura</h1>
      <button 
        @click="handleNewChat"
        class="p-1.5 rounded-btn hover:bg-bg-hover text-text-secondary transition-colors"
        title="新对话"
      >
        <Plus :size="16" :stroke-width="2" />
      </button>
    </div>

    <!-- Navigation -->
    <nav class="flex-1 px-3 py-3 overflow-y-auto">
      <router-link
        v-for="item in navItems"
        :key="item.path"
        :to="item.path"
        class="w-full flex items-center gap-3 h-9 px-3 rounded-btn text-[13px] transition-colors"
        :class="isActive(item.path) 
          ? 'bg-bg-hover text-text-primary' 
          : 'text-text-secondary hover:bg-bg-hover'"
      >
        <component :is="item.icon" :size="17" :stroke-width="2" />
        <span>{{ item.label }}</span>
      </router-link>

      <!-- Recent Conversations -->
      <div class="mt-6">
        <p class="px-3 text-[11px] font-semibold text-text-tertiary uppercase tracking-wider mb-2">
          最近对话
        </p>
        <div v-if="conversationStore.conversations.length === 0" 
             class="px-3 text-[12px] text-text-tertiary">
          暂无对话记录
        </div>
        <router-link
          v-for="conv in conversationStore.conversations.slice(0, 5)"
          :key="conv.id"
          :to="`/sandbox/${conv.id}`"
          class="flex items-center gap-2 px-3 py-2 rounded-btn text-[12px] text-text-secondary hover:bg-bg-hover truncate"
        >
          <span class="truncate">{{ conv.title }}</span>
        </router-link>
      </div>
    </nav>

    <!-- User Section -->
    <div class="p-3" style="box-shadow: 0 -1px 0 0 rgba(0,0,0,0.08)">
      <div class="flex items-center justify-between px-3 py-2">
        <div class="flex items-center gap-3 min-w-0">
          <div class="w-8 h-8 rounded-full bg-brand-light flex items-center justify-center flex-shrink-0">
            <User :size="16" :stroke-width="2" class="text-brand-primary" />
          </div>
          <div class="min-w-0">
            <p class="text-[13px] font-semibold text-text-primary truncate">
              {{ authStore.user?.nickname || '未登录' }}
            </p>
            <p class="text-[11px] text-text-tertiary truncate">
              {{ authStore.user?.studentId || '' }}
            </p>
          </div>
        </div>
        <button 
          @click="handleLogout"
          class="p-1.5 rounded-btn hover:bg-bg-hover text-text-tertiary transition-colors"
          title="退出登录"
        >
          <LogOut :size="14" :stroke-width="2" />
        </button>
      </div>
    </div>
  </aside>
</template>
