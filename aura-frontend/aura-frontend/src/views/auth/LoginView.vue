<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Eye, EyeOff, LogIn } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const form = ref({
  username: '',
  password: '',
})

const showPassword = ref(false)
const loading = ref(false)
const error = ref('')

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    error.value = '请填写用户名和密码'
    return
  }

  loading.value = true
  error.value = ''

  try {
    // Mock login for now
    authStore.setAuth(
      'mock-token-' + Date.now(),
      'mock-refresh-token',
      {
        id: '1',
        username: form.value.username,
        nickname: '刘畅',
        studentId: '249970619',
      }
    )
    router.push('/')
  } catch (e) {
    error.value = '登录失败，请检查用户名和密码'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="bg-bg-secondary rounded-card p-8" style="box-shadow: var(--shadow-card)">
    <div class="text-center mb-8">
      <h1 class="text-xl font-semibold text-text-primary mb-2">登录 Aura</h1>
      <p class="text-sm text-text-secondary">个人美学操作系统</p>
    </div>

    <form @submit.prevent="handleLogin" class="space-y-4">
      <!-- Username -->
      <div>
        <label class="block text-sm font-semibold text-text-primary mb-1.5">用户名</label>
        <input
          v-model="form.username"
          type="text"
          class="w-full h-10 px-3 bg-bg-tertiary rounded-input text-sm text-text-primary
                 placeholder:text-text-tertiary
                 focus:bg-bg-secondary focus:outline-none focus:ring-2 focus:ring-brand-primary"
          placeholder="请输入用户名"
        />
      </div>

      <!-- Password -->
      <div>
        <label class="block text-sm font-semibold text-text-primary mb-1.5">密码</label>
        <div class="relative">
          <input
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
            class="w-full h-10 px-3 pr-10 bg-bg-tertiary rounded-input text-sm text-text-primary
                   placeholder:text-text-tertiary
                   focus:bg-bg-secondary focus:outline-none focus:ring-2 focus:ring-brand-primary"
            placeholder="请输入密码"
          />
          <button
            type="button"
            @click="showPassword = !showPassword"
            class="absolute right-3 top-1/2 -translate-y-1/2 text-text-tertiary hover:text-text-secondary"
          >
            <Eye v-if="!showPassword" :size="16" :stroke-width="2" />
            <EyeOff v-else :size="16" :stroke-width="2" />
          </button>
        </div>
      </div>

      <!-- Error Message -->
      <p v-if="error" class="text-sm text-error">{{ error }}</p>

      <!-- Submit Button -->
      <button
        type="submit"
        :disabled="loading"
        class="w-full h-10 bg-brand-primary hover:bg-brand-hover text-white font-semibold text-sm
               rounded-btn transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
      >
        <span v-if="loading">登录中...</span>
        <span v-else class="flex items-center justify-center gap-2">
          <LogIn :size="16" :stroke-width="2" />
          登录
        </span>
      </button>

      <!-- Register Link -->
      <p class="text-center text-sm text-text-secondary">
        还没有账号？
        <router-link to="/auth/register" class="text-brand-primary hover:underline font-semibold">
          立即注册
        </router-link>
      </p>
    </form>
  </div>
</template>
