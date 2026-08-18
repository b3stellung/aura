import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { TabType } from '@/types'

export const useAppStore = defineStore('app', () => {
  // Current active tab
  const activeTab = ref<TabType>('home')

  // Global loading overlay
  const isLoading = ref(false)

  // Global notification
  const notification = ref<{ message: string; type: 'success' | 'error' | 'info' } | null>(null)

  function setActiveTab(tab: TabType) {
    activeTab.value = tab
  }

  function showNotification(message: string, type: 'success' | 'error' | 'info' = 'info') {
    notification.value = { message, type }
    setTimeout(() => {
      notification.value = null
    }, 3000)
  }

  return {
    activeTab,
    isLoading,
    notification,
    setActiveTab,
    showNotification,
  }
})
