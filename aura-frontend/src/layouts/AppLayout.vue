<script setup lang="ts">
import { ref } from 'vue'
import Sidebar from '@/components/layout/Sidebar.vue'
import Header from '@/components/layout/Header.vue'

const sidebarOpen = ref(false)
</script>

<template>
  <div class="h-screen flex flex-col bg-[#fffaf5] overflow-hidden">
    <Header @toggle-sidebar="sidebarOpen = true" />
    <Sidebar :open="sidebarOpen" @close="sidebarOpen = false" />
    <main class="flex-1 overflow-y-auto">
      <router-view />
    </main>
  </div>
</template>
