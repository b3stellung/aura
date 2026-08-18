<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Compass, Shirt, Sparkles, User } from 'lucide-vue-next'
import type { TabType } from '@/types'

const router = useRouter()
const route = useRoute()

interface TabItem {
  key: TabType
  label: string
  icon: typeof Compass
  path: string
}

const tabs: TabItem[] = [
  { key: 'home', label: 'Discover', icon: Compass, path: '/home' },
  { key: 'wardrobe', label: 'Wardrobe', icon: Shirt, path: '/wardrobe' },
  { key: 'ai', label: 'AI Stylist', icon: Sparkles, path: '/ai' },
  { key: 'profile', label: 'Profile', icon: User, path: '/profile' },
]

const currentTab = computed<TabType>(() => {
  return (route.meta.tab as TabType) || 'home'
})

function navigateTo(tab: TabItem) {
  if (route.path !== tab.path) {
    router.push(tab.path)
  }
}
</script>

<template>
  <nav
    class="fixed bottom-0 left-0 right-0 z-50 bg-bg-card border-t border-border"
    style="box-shadow: 0 -1px 3px rgba(0, 0, 0, 0.05)"
  >
    <div class="max-w-lg mx-auto flex items-center justify-around h-14">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        class="flex flex-col items-center justify-center gap-0.5 min-w-[64px] min-h-[44px] transition-colors duration-150"
        :class="currentTab === tab.key ? 'text-text-primary' : 'text-text-secondary'"
        @click="navigateTo(tab)"
      >
        <component
          :is="tab.icon"
          :size="22"
          :stroke-width="currentTab === tab.key ? 2.2 : 1.8"
        />
        <span class="text-[10px] leading-tight font-medium">{{ tab.label }}</span>
      </button>
    </div>
  </nav>
</template>
