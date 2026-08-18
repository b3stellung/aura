<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Eye, EyeOff, UserPlus } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const form = ref({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  studentId: '',
})

const showPassword = ref(false)
const loading = ref(false)
const error = ref('')

const handleRegister = async () => {
  if (!form.value.username || !form.value.password || !form.value.nickname) {
    error.value = '请填写必填字段'
    return
  }

  if (form.value.password !== form.value.confirmPassword) {
    error.value = '两次输入的密码不一致'
    return
  }

  loading.value = true
  error.value = ''

  try {
    // Mock register for now
    authStore.setAuth(
      'mock-token-' + Date.now(),
      'mock-refresh-token',
      {
        id: '1',
        username: form.value.username,
        nickname: form.value.nickname,
        studentId: form.value.studentId,
      }
    )
    router.push('/')
  } catch (e) {
    error.value = '注册失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="bg-bg-secondary rounded-card p-8" style="box-shadow: var(--shadow-card)">
    <div class="text-center mb-8">
      <h1 class="text-xl font-semibold text-text-primary mb-2">注册 Aura</h1>
      <p class="text-sm text-text-secondary">创建你的个人美学操作系统账号</p>
    </div>

    <form @submit.prevent="handleRegister" class="space-y-4">
      <!-- Nickname -->
      <div>
        <label class="block text-sm font-semibold text-text-primary mb-1.5">
          昵称 <span class="text-error">*</span>
        </label>
        <input
          v-model="form.nickname"
          type="text"
          class="w-full h-10 px-3 bg-bg-tertiary rounded-input text-sm text-text-primary
                 placeholder:text-text-tertiary
                 focus:bg-bg-secondary focus:outline-none focus:ring-2 focus:ring-brand-primary"
          placeholder="请输入昵称"
        />
      </div>

      <!-- Username -->
      <div>
        <label class="block text-sm font-semibold text-text-primary mb-1.5">
          用户名 <span class="text-error">*</span>
        </label>
        <input
          v-model="form.username"
          type="text"
          class="w-full h-10 px-3 bg-bg-tertiary rounded-input text-sm text-text-primary
                 placeholder:text-text-tertiary
                 focus:bg-bg-secondary focus:outline-none focus:ring-2 focus:ring-brand-primary"
          placeholder="请输入用户名"
        />
      </div>

      <!-- Student ID -->
      <div>
        <label class="block text-sm font-semibold text-text-primary mb-1.5">学号</label>
        <input
          v-model="form.studentId"
          type="text"
          class="w-full h-10 px-3 bg-bg-tertiary rounded-input text-sm text-text-primary
                 placeholder:text-text-tertiary
                 focus:bg-bg-secondary focus:outline-none focus:ring-2 focus:ring-brand-primary"
          placeholder="请输入学号（选填）"
        />
      </div>

      <!-- Password -->
      <div>
        <label class="block text-sm font-semibold text-text-primary mb-1.5">
          密码 <span class="text-error">*</span>
        </label>
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

      <!-- Confirm Password -->
      <div>
        <label class="block text-sm font-semibold text-text-primary mb-1.5">
          确认密码 <span class="text-error">*</span>
        </label>
        <input
          v-model="form.confirmPassword"
          :type="showPassword ? 'text' : 'password'"
          class="w-full h-10 px-3 bg-bg-tertiary rounded-input text-sm text-text-primary
                 placeholder:text-text-tertiary
                 focus:bg-bg-secondary focus:outline-none focus:ring-2 focus:ring-brand-primary"
          placeholder="请再次输入密码"
        />
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
        <span v-if="loading">注册中...</span>
        <span v-else class="flex items-center justify-center gap-2">
          <UserPlus :size="16" :stroke-width="2" />
          注册
        </span>
      </button>

      <!-- Login Link -->
      <p class="text-center text-sm text-text-secondary">
        已有账号？
        <router-link to="/auth/login" class="text-brand-primary hover:underline font-semibold">
          立即登录
        </router-link>
      </p>
    </form>
  </div>
</template>
