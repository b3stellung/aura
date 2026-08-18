<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Bell, History, Home, Menu, Search, Settings, Shirt, Sparkles } from 'lucide-vue-next'

defineEmits<{ toggleSidebar: [] }>()

const route = useRoute()
const pageTitle = computed(() => (route.meta?.title as string) || 'Aura')

const navItems = [
  { path: '/', icon: Home, label: '首页' },
  { path: '/wardrobe', icon: Shirt, label: '衣橱' },
  { path: '/ai', icon: Sparkles, label: '造型师' },
  { path: '/history', icon: History, label: '历史' },
]

const isActive = (path: string) => (path === '/' ? route.path === '/' : route.path.startsWith(path))
</script>

<template>
  <header class="h-16 flex items-center gap-4 px-4 md:px-6 bg-[#fff9f2]/95 border-b border-[#f1dfcf] text-[#3d3028] backdrop-blur">
    <button
      class="md:hidden p-2 rounded-xl text-[#8d6547] hover:bg-[#f6eadf] transition-colors"
      type="button"
      aria-label="Open navigation"
      @click="$emit('toggleSidebar')"
    >
      <Menu :size="20" />
    </button>

    <router-link to="/" class="flex items-center gap-2.5 flex-shrink-0">
      <div class="w-8 h-8 rounded-2xl bg-[#c97850] flex items-center justify-center shadow-sm">
        <span class="text-white text-xs font-bold">A</span>
      </div>
      <div class="leading-tight">
        <p class="text-sm font-semibold tracking-wide text-[#3d3028]">Aura</p>
        <p class="hidden sm:block text-[11px] text-[#9b7a61]">{{ pageTitle }}</p>
      </div>
    </router-link>

    <nav class="hidden md:flex items-center gap-1 ml-4">
      <router-link
        v-for="item in navItems"
        :key="item.path"
        :to="item.path"
        class="flex items-center gap-2 h-9 px-3 rounded-full text-sm transition-all duration-200"
        :class="isActive(item.path) ? 'bg-[#f3dfcf] text-[#8f4d2f] font-semibold shadow-sm' : 'text-[#7c6655] hover:bg-[#f8eee5] hover:text-[#4a3325]'"
      >
        <component :is="item.icon" :size="16" />
        <span>{{ item.label }}</span>
      </router-link>
    </nav>

    <div class="hidden lg:block flex-1 max-w-xs mx-4">
      <div class="relative">
        <Search :size="14" class="absolute left-3 top-1/2 -translate-y-1/2 text-[#b29276]" />
        <input
          class="w-full h-9 pl-9 pr-4 bg-[#f8eee5] rounded-full text-xs text-[#3d3028] placeholder:text-[#b29276] border border-transparent focus:border-[#e2b493] focus:bg-white focus:outline-none transition-all"
          placeholder="Search..."
        />
      </div>
    </div>

    <div class="flex items-center gap-1 ml-auto">
      <button class="p-2 rounded-xl hover:bg-[#f8eee5] text-[#9b7a61] hover:text-[#8f4d2f] transition-colors relative" title="Notifications">
        <Bell :size="16" />
      </button>
      <router-link to="/settings" class="p-2 rounded-xl hover:bg-[#f8eee5] text-[#9b7a61] hover:text-[#8f4d2f] transition-colors" title="Settings">
        <Settings :size="16" />
      </router-link>
    </div>
  </header>
</template>
