import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi, logout as logoutApi } from '@/api/auth'
import { getUserProfile, updateUserProfile } from '@/api/user'
import type { User, StyleProfile } from '@/types/auth'

const DEFAULT_USER: User = {
  id: '',
  username: '',
  nickname: 'User',
  avatar: '',
  bio: '',
  followers: 0,
  following: 0,
  outfits: 0,
}

const DEFAULT_STYLE_PROFILE: StyleProfile = {
  mainStyle: '简约主义',
  mainStyleScore: 78,
  preferredColors: ['#E8DDD3', '#D4E4ED', '#D3D8E8'],
  dimensions: { minimalist: 78, elegant: 64, casual: 56, trendy: 42 },
}

function loadStoredUser(): User {
  try {
    const stored = localStorage.getItem('aura-user')
    return stored ? { ...DEFAULT_USER, ...JSON.parse(stored) } : DEFAULT_USER
  } catch {
    return DEFAULT_USER
  }
}

function getResponseData(response: unknown): Record<string, unknown> {
  if (!response || typeof response !== 'object') return {}
  const record = response as Record<string, unknown>
  const data = record.data
  return data && typeof data === 'object' ? data as Record<string, unknown> : record
}

function normalizeUser(value: unknown): User {
  const source = value && typeof value === 'object' ? value as Record<string, unknown> : {}
  return {
    ...DEFAULT_USER,
    ...source,
    avatar: (source.avatar ?? source.avatarUrl ?? DEFAULT_USER.avatar) as string,
    styleProfile: (source.styleProfile as StyleProfile | undefined) ?? DEFAULT_STYLE_PROFILE,
  }
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('aura-token'))
  const refreshToken = ref<string | null>(localStorage.getItem('aura-refresh-token'))
  const user = ref<User>(loadStoredUser())
  const isLoading = ref(false)
  const error = ref<string | null>(null)
  const showLoginModal = ref(false)
  const showRegisterModal = ref(false)

  const isAuthenticated = computed(() => !!token.value)
  const isLoggedIn = computed(() => isAuthenticated.value)
  const styleProfile = computed(() => user.value.styleProfile ?? DEFAULT_STYLE_PROFILE)

  function setAuth(newToken: string, newRefreshToken: string, newUser: User) {
    token.value = newToken
    refreshToken.value = newRefreshToken
    user.value = normalizeUser(newUser)
    
    localStorage.setItem('aura-token', newToken)
    localStorage.setItem('aura-refresh-token', newRefreshToken)
    localStorage.setItem('aura-user', JSON.stringify(newUser))
  }

  function logout() {
    void logoutApi().catch(() => undefined)
    clearAuth()
  }

  function clearAuth() {
    token.value = null
    refreshToken.value = null
    user.value = DEFAULT_USER
    
    localStorage.removeItem('aura-token')
    localStorage.removeItem('aura-refresh-token')
    localStorage.removeItem('aura-user')
  }

  async function login(credentials: { username: string; password: string }) {
    isLoading.value = true
    error.value = null
    try {
      const response = await loginApi(credentials)
      const data = getResponseData(response)
      const accessToken = String(data.accessToken ?? data.token ?? '')
      const newRefreshToken = String(data.refreshToken ?? '')
      if (!accessToken) throw new Error('登录响应缺少 accessToken')
      setAuth(accessToken, newRefreshToken, normalizeUser(data.user))
      return user.value
    } catch (cause) {
      error.value = cause instanceof Error ? cause.message : '登录失败'
      throw cause
    } finally {
      isLoading.value = false
    }
  }

  async function register(credentials: { username: string; password: string; nickname: string; email?: string }) {
    isLoading.value = true
    error.value = null
    try {
      const response = await registerApi({ ...credentials, email: credentials.email ?? '' })
      const data = getResponseData(response)
      const accessToken = String(data.accessToken ?? data.token ?? '')
      const newRefreshToken = String(data.refreshToken ?? '')
      if (!accessToken) throw new Error('注册响应缺少 accessToken')
      setAuth(accessToken, newRefreshToken, normalizeUser(data.user ?? { username: credentials.username, nickname: credentials.nickname }))
      return user.value
    } catch (cause) {
      error.value = cause instanceof Error ? cause.message : '注册失败'
      throw cause
    } finally {
      isLoading.value = false
    }
  }

  async function fetchCurrentUser() {
    if (!token.value) return
    isLoading.value = true
    error.value = null
    try {
      const response = await getUserProfile()
      user.value = normalizeUser(response.data)
      localStorage.setItem('aura-user', JSON.stringify(user.value))
    } catch (cause) {
      error.value = cause instanceof Error ? cause.message : '获取用户资料失败'
    } finally {
      isLoading.value = false
    }
  }

  async function updateProfile(updates: { nickname?: string; avatar?: string; bio?: string }) {
    isLoading.value = true
    error.value = null
    try {
      const response = await updateUserProfile(updates)
      user.value = normalizeUser(response.data ?? { ...user.value, ...updates })
      localStorage.setItem('aura-user', JSON.stringify(user.value))
    } catch (cause) {
      error.value = cause instanceof Error ? cause.message : '更新资料失败'
      throw cause
    } finally {
      isLoading.value = false
    }
  }

  function openLogin() {
    showRegisterModal.value = false
    showLoginModal.value = true
  }

  function openRegister() {
    showLoginModal.value = false
    showRegisterModal.value = true
  }

  function closeModals() {
    showLoginModal.value = false
    showRegisterModal.value = false
  }

  return {
    token,
    refreshToken,
    user,
    isAuthenticated,
    isLoggedIn,
    isLoading,
    error,
    styleProfile,
    setAuth,
    logout,
    clearAuth,
    login,
    register,
    fetchCurrentUser,
    updateProfile,
    showLoginModal,
    showRegisterModal,
    openLogin,
    openRegister,
    closeModals,
  }
})
