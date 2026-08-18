<script setup lang="ts">
import { watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { History, Home, LogOut, MessageSquare, Plus, Shirt, Sparkles, User, X } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { useConversationStore } from '@/stores/conversation'

const props = defineProps<{ open: boolean }>()
const emit = defineEmits<{ close: [] }>()

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const conversationStore = useConversationStore()

const navItems = [
  { path: '/', icon: Home, label: '首页' },
  { path: '/wardrobe', icon: Shirt, label: '衣橱' },
  { path: '/ai', icon: Sparkles, label: '造型师' },
  { path: '/history', icon: History, label: '历史' },
]

const isActive = (path: string) => (path === '/' ? route.path === '/' : route.path.startsWith(path))
const close = () => emit('close')
const handleLogout = () => {
  authStore.logout()
  close()
  router.push('/auth/login')
}
const goHome = () => {
  router.push('/')
  close()
}
const startStylist = () => {
  router.push('/ai')
  close()
}

watch(() => route.fullPath, () => {
  if (props.open) close()
})
</script>

<template>
  <div v-if="open" class="fixed inset-0 z-40 bg-[#3d3028]/35 md:hidden" aria-hidden="true" @click="close" />
  <aside
    class="fixed inset-y-0 left-0 z-50 w-[min(82vw,300px)] flex flex-col bg-[#fff9f2] border-r border-[#f1dfcf] text-[#3d3028] shadow-2xl transition-transform duration-200 md:hidden"
    :class="open ? 'translate-x-0' : '-translate-x-full'"
    aria-label="Navigation"
  >
    <div class="h-16 flex items-center justify-between px-4 border-b border-[#f1dfcf]">
      <div class="flex items-center gap-2 cursor-pointer" @click="goHome">
        <div class="w-8 h-8 rounded-2xl bg-[#c97850] flex items-center justify-center shadow-sm">
          <span class="text-white text-xs font-bold">A</span>
        </div>
        <span class="text-sm font-semibold tracking-wide text-[#3d3028]">Aura</span>
      </div>
      <div class="flex items-center gap-1">
        <button
          @click="startStylist"
          class="p-1.5 rounded-xl hover:bg-[#f6eadf] text-[#9b7a61] hover:text-[#8f4d2f] transition-colors"
          title="New styling session"
        >
          <Plus :size="16" />
        </button>
        <button
          @click="close"
          class="p-1.5 rounded-xl hover:bg-[#f6eadf] text-[#9b7a61] hover:text-[#8f4d2f] transition-colors"
          type="button"
          aria-label="Close navigation"
        >
          <X :size="17" />
        </button>
      </div>
    </div>

    <nav class="flex-1 px-3 py-3 overflow-y-auto">
      <div class="space-y-0.5">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="flex items-center gap-2.5 h-10 px-3 rounded-xl text-sm transition-all duration-200"
          :class="isActive(item.path) ? 'bg-[#f3dfcf] text-[#8f4d2f] font-semibold shadow-sm' : 'text-[#7c6655] hover:bg-[#f8eee5] hover:text-[#4a3325]'"
        >
          <component :is="item.icon" :size="16" />
          <span>{{ item.label }}</span>
        </router-link>
      </div>

      <div class="mt-6">
        <p class="px-3 text-[11px] font-medium text-[#b29276] uppercase tracking-wider mb-2">Recent conversations</p>
        <div v-if="conversationStore.conversations.length === 0" class="px-3 py-4 text-center">
          <MessageSquare :size="16" class="mx-auto text-[#d8b89d] mb-1.5" />
          <p class="text-[11px] text-[#b29276]">No conversations yet</p>
        </div>
        <div v-else class="space-y-0.5">
          <router-link
            v-for="conv in conversationStore.conversations.slice(0, 5)"
            :key="conv.id"
            :to="`/sandbox/${conv.id}`"
            class="flex items-center gap-2 px-3 py-2 rounded-xl text-xs transition-all duration-200 truncate"
            :class="route.params.id === conv.id ? 'bg-[#f3dfcf] text-[#8f4d2f] font-medium' : 'text-[#7c6655] hover:bg-[#f8eee5] hover:text-[#4a3325]'"
          >
            <div class="w-1.5 h-1.5 rounded-full bg-[#d09a72] flex-shrink-0" />
            <span class="truncate">{{ conv.title }}</span>
          </router-link>
        </div>
      </div>
    </nav>

    <div class="p-3 border-t border-[#f1dfcf]">
      <div class="flex items-center gap-3 px-3 py-2 rounded-xl hover:bg-[#f8eee5] transition-colors">
        <div class="w-8 h-8 rounded-full bg-[#f3dfcf] flex items-center justify-center">
          <User :size="14" class="text-[#8f4d2f]" />
        </div>
        <div class="flex-1 min-w-0">
          <p class="text-xs font-medium text-[#3d3028] truncate">{{ authStore.user?.nickname || 'User' }}</p>
        </div>
        <button
          @click="handleLogout"
          class="p-1.5 rounded-xl hover:bg-[#f7e4dc] text-[#9b7a61] hover:text-[#c14f68] transition-colors"
          title="Log out"
        >
          <LogOut :size="14" />
        </button>
      </div>
    </div>
  </aside>
</template>
