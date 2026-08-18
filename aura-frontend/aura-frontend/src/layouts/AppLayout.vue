<script setup lang="ts">
import Sidebar from '@/components/layout/Sidebar.vue'
import Header from '@/components/layout/Header.vue'
</script>

<template>
  <div class="h-screen flex bg-bg-primary overflow-hidden">
    <Sidebar />
    <div class="flex-1 flex flex-col min-w-0 h-screen">
      <Header />
      <main class="flex-1 overflow-y-auto">
        <router-view />
      </main>
    </div>
  </div>
</template>
