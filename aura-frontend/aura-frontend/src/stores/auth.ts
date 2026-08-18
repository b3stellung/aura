import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/types/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('aura-token'))
  const refreshToken = ref<string | null>(localStorage.getItem('aura-refresh-token'))
  const user = ref<User | null>(JSON.parse(localStorage.getItem('aura-user') || 'null'))

  const isAuthenticated = computed(() => !!token.value)

  function setAuth(newToken: string, newRefreshToken: string, newUser: User) {
    token.value = newToken
    refreshToken.value = newRefreshToken
    user.value = newUser
    
    localStorage.setItem('aura-token', newToken)
    localStorage.setItem('aura-refresh-token', newRefreshToken)
    localStorage.setItem('aura-user', JSON.stringify(newUser))
  }

  function logout() {
    token.value = null
    refreshToken.value = null
    user.value = null
    
    localStorage.removeItem('aura-token')
    localStorage.removeItem('aura-refresh-token')
    localStorage.removeItem('aura-user')
  }

  return {
    token,
    refreshToken,
    user,
    isAuthenticated,
    setAuth,
    logout,
  }
})
