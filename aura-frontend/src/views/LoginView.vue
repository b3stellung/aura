<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Eye, EyeOff, Loader2, Sparkles } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { storeToRefs } from 'pinia'

const router = useRouter()
const authStore = useAuthStore()
const { isLoading, error } = storeToRefs(authStore)

const form = ref({ username: '', password: '' })
const showPassword = ref(false)

async function handleLogin() {
  if (!form.value.username.trim() || !form.value.password.trim()) return
  try {
    await authStore.login(form.value)
    router.push('/home')
  } catch {
    // error is in store
  }
}

function goRegister() {
  router.push('/register')
}
</script>

<template>
  <div class="min-h-screen bg-bg-primary flex items-center justify-center">
    <div class="w-full max-w-sm px-6">
      <!-- Logo -->
      <div class="text-center mb-8">
        <div class="w-12 h-12 rounded-xl gradient-instagram flex items-center justify-center mx-auto mb-4">
          <Sparkles :size="22" class="text-white" />
        </div>
        <h1 class="text-2xl font-bold text-text-primary tracking-tight">Aura</h1>
        <p class="text-sm text-text-secondary mt-1.5">探索属于你的穿搭风格</p>
      </div>

      <!-- Error Banner -->
      <Transition name="fade">
        <div
          v-if="error"
          class="mb-4 p-2.5 bg-error/10 rounded-card text-[12px] text-error text-center"
        >
          {{ error }}
        </div>
      </Transition>

      <!-- Login Form -->
      <div class="space-y-3">
        <div>
          <label class="text-[12px] text-text-secondary mb-1.5 block font-medium">用户名</label>
          <input
            v-model="form.username"
            type="text"
            placeholder="请输入用户名"
            autocomplete="username"
            class="w-full h-10 px-4 bg-bg-card border border-border rounded-card text-sm focus:outline-none focus:border-ig-blue focus:ring-1 focus:ring-ig-blue/20 transition-all"
            @keyup.enter="handleLogin"
          />
        </div>

        <div>
          <label class="text-[12px] text-text-secondary mb-1.5 block font-medium">密码</label>
          <div class="relative">
            <input
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入密码"
              autocomplete="current-password"
              class="w-full h-10 px-4 pr-12 bg-bg-card border border-border rounded-card text-sm focus:outline-none focus:border-ig-blue focus:ring-1 focus:ring-ig-blue/20 transition-all"
              @keyup.enter="handleLogin"
            />
            <button
              type="button"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-text-tertiary hover:text-text-secondary transition-colors"
              @click="showPassword = !showPassword"
            >
              <EyeOff v-if="showPassword" :size="16" />
              <Eye v-else :size="16" />
            </button>
          </div>
        </div>

        <div class="flex justify-end">
          <button class="text-[12px] text-ig-blue font-medium hover:underline">
            忘记密码？
          </button>
        </div>

        <button
          class="w-full h-10 rounded-card text-sm font-medium text-white transition-colors flex items-center justify-center gap-2"
          :class="form.username.trim() && form.password.trim()
            ? 'bg-ig-blue hover:bg-ig-blue-hover'
            : 'bg-bg-secondary text-text-tertiary cursor-not-allowed'"
          :disabled="isLoading || !form.username.trim() || !form.password.trim()"
          @click="handleLogin"
        >
          <Loader2 v-if="isLoading" :size="16" class="animate-spin" />
          {{ isLoading ? '登录中...' : '登录' }}
        </button>
      </div>

      <!-- Divider -->
      <div class="flex items-center gap-4 my-6">
        <div class="flex-1 h-px bg-border" />
        <span class="text-[11px] text-text-tertiary">或</span>
        <div class="flex-1 h-px bg-border" />
      </div>

      <!-- Social Login -->
      <button
        class="w-full h-10 rounded-card text-sm font-medium text-text-primary bg-bg-card border border-border hover:bg-bg-secondary transition-colors flex items-center justify-center gap-2"
      >
        <span class="text-base">📱</span>
        微信登录
      </button>

      <!-- Register Link -->
      <div class="mt-6 text-center">
        <span class="text-[13px] text-text-secondary">还没有账号？</span>
        <button
          class="text-[13px] text-ig-blue font-semibold ml-1 hover:underline"
          @click="goRegister"
        >
          注册
        </button>
      </div>

      <!-- Back to home -->
      <div class="mt-4 text-center">
        <button
          class="text-[12px] text-text-tertiary hover:text-text-secondary transition-colors"
          @click="router.push('/home')"
        >
          ← 返回首页
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
