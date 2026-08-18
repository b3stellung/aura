<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Search, Bell, Settings } from 'lucide-vue-next'

const route = useRoute()

const pageTitle = computed(() => {
  return (route.meta?.title as string) || 'Aura'
})
</script>

<template>
  <header class="h-[52px] bg-bg-secondary flex items-center px-6"
          style="box-shadow: 0 1px 0 0 rgba(0,0,0,0.08)">
    <!-- Page Title -->
    <h2 class="text-sm font-semibold text-text-primary">{{ pageTitle }}</h2>

    <!-- Search Bar (centered) -->
    <div class="flex-1 max-w-md mx-8">
      <div class="relative">
        <Search :size="15" :stroke-width="2" 
                class="absolute left-3 top-1/2 -translate-y-1/2 text-text-tertiary" />
        <input 
          class="w-full h-8 pl-9 pr-12 bg-bg-tertiary rounded-btn text-sm text-text-primary
                 placeholder:text-text-tertiary 
                 focus:bg-bg-secondary focus:outline-none focus:ring-2 focus:ring-brand-primary"
          placeholder="搜索..." 
        />
        <kbd class="absolute right-3 top-1/2 -translate-y-1/2 text-[11px] text-text-tertiary 
                    px-1.5 py-0.5 bg-bg-hover rounded-[4px] font-mono">
          ⌘K
        </kbd>
      </div>
    </div>

    <!-- Actions -->
    <div class="flex items-center gap-1">
      <button class="p-2 rounded-btn hover:bg-bg-hover text-text-secondary transition-colors">
        <Bell :size="17" :stroke-width="2" />
      </button>
      <router-link to="/settings" 
                   class="p-2 rounded-btn hover:bg-bg-hover text-text-secondary transition-colors">
        <Settings :size="17" :stroke-width="2" />
      </router-link>
    </div>
  </header>
</template>
