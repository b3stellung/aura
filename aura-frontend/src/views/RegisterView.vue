<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Eye, EyeOff, Check, Loader2 } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { storeToRefs } from 'pinia'

const router = useRouter()
const authStore = useAuthStore()
const { isLoading, error } = storeToRefs(authStore)

const form = ref({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
})
const showPassword = ref(false)
const currentStep = ref(1) // 1: basic info, 2: profile info

const isStep1Valid = computed(() =>
  form.value.username.trim().length >= 3 && form.value.password.trim().length >= 6
)

const isStep2Valid = computed(() => form.value.nickname.trim().length >= 1)

const passwordStrength = computed(() => {
  const pwd = form.value.password
  if (!pwd) return { level: 0, label: '', color: '' }
  let score = 0
  if (pwd.length >= 6) score++
  if (pwd.length >= 8) score++
  if (/[A-Z]/.test(pwd)) score++
  if (/[0-9]/.test(pwd)) score++
  if (/[^A-Za-z0-9]/.test(pwd)) score++

  if (score <= 1) return { level: 1, label: '弱', color: 'bg-error' }
  if (score <= 2) return { level: 2, label: '一般', color: 'bg-warning' }
  if (score <= 3) return { level: 3, label: '强', color: 'bg-success' }
  return { level: 4, label: '非常强', color: 'bg-accent-via' }
})

function nextStep() {
  if (!isStep1Valid.value) return
  error.value = null
  currentStep.value = 2
}

function prevStep() {
  error.value = null
  currentStep.value = 1
}

async function handleRegister() {
  if (!isStep2Valid.value || !isStep1Valid.value) return
  try {
    await authStore.register({
      username: form.value.username.trim(),
      password: form.value.password.trim(),
      nickname: form.value.nickname.trim(),
    })
    router.push('/home')
  } catch {
    // error is in store
  }
}

function goBack() {
  if (currentStep.value === 2) {
    prevStep()
  } else {
    router.back()
  }
}

function goLogin() {
  router.push('/login')
}
</script>

<template>
  <div class="min-h-screen bg-bg-primary flex flex-col">
    <!-- Top Bar -->
    <header class="safe-area-top">
      <div class="flex items-center px-4 h-12">
        <button
          class="w-9 h-9 flex items-center justify-center rounded-full hover:bg-bg-secondary transition-colors"
          @click="goBack"
        >
          <ArrowLeft :size="20" class="text-text-primary" />
        </button>
      </div>
    </header>

    <!-- Progress Steps -->
    <div class="px-8 mb-8">
      <div class="flex items-center justify-center gap-3">
        <div class="flex items-center gap-2">
          <div
            class="w-7 h-7 rounded-full flex items-center justify-center text-xs font-medium transition-colors"
            :class="currentStep >= 1 ? 'gradient-instagram text-white' : 'bg-bg-secondary text-text-tertiary'"
          >
            <Check v-if="currentStep > 1" :size="14" />
            <span v-else>1</span>
          </div>
          <span class="text-xs font-medium" :class="currentStep >= 1 ? 'text-text-primary' : 'text-text-tertiary'">
            账号信息
          </span>
        </div>
        <div class="w-12 h-px bg-aura-border"></div>
        <div class="flex items-center gap-2">
          <div
            class="w-7 h-7 rounded-full flex items-center justify-center text-xs font-medium transition-colors"
            :class="currentStep >= 2 ? 'gradient-instagram text-white' : 'bg-bg-secondary text-text-tertiary'"
          >
            2
          </div>
          <span class="text-xs font-medium" :class="currentStep >= 2 ? 'text-text-primary' : 'text-text-tertiary'">
            个人信息
          </span>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="flex-1 flex flex-col items-center px-8 max-w-lg mx-auto w-full">
      <!-- Logo -->
      <div class="mb-8 text-center">
        <h1 class="text-2xl font-bold text-primary tracking-tight">创建账号</h1>
        <p class="text-sm text-text-secondary mt-1">加入 Aura，开始你的穿搭之旅</p>
      </div>

      <!-- Error Banner -->
      <Transition name="fade">
        <div
          v-if="error"
          class="w-full mb-4 p-3 bg-error/10 rounded-card text-sm text-error text-center"
        >
          {{ error }}
        </div>
      </Transition>

      <!-- Step 1: Account Info -->
      <Transition name="slide-fade" mode="out-in">
        <div v-if="currentStep === 1" key="step1" class="w-full space-y-4">
          <!-- Username -->
          <div>
            <label class="text-xs text-text-secondary mb-1.5 block font-medium">用户名</label>
            <input
              v-model="form.username"
              type="text"
              placeholder="至少 3 个字符"
              autocomplete="username"
              class="w-full h-12 px-4 bg-bg-card border border-aura-border rounded-card text-sm
                     focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/20
                     transition-all duration-200"
            />
            <p v-if="form.username && form.username.trim().length < 3" class="text-[11px] text-error mt-1">
              用户名至少需要 3 个字符
            </p>
          </div>

          <!-- Password -->
          <div>
            <label class="text-xs text-text-secondary mb-1.5 block font-medium">密码</label>
            <div class="relative">
              <input
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="至少 6 位"
                autocomplete="new-password"
                class="w-full h-12 px-4 pr-12 bg-bg-card border border-aura-border rounded-card text-sm
                       focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/20
                       transition-all duration-200"
              />
              <button
                type="button"
                class="absolute right-3 top-1/2 -translate-y-1/2 text-text-tertiary hover:text-text-secondary transition-colors"
                @click="showPassword = !showPassword"
              >
                <EyeOff v-if="showPassword" :size="18" />
                <Eye v-else :size="18" />
              </button>
            </div>

            <!-- Password Strength -->
            <div v-if="form.password" class="mt-2">
              <div class="flex gap-1.5">
                <div
                  v-for="i in 4"
                  :key="i"
                  class="flex-1 h-1 rounded-full transition-colors duration-300"
                  :class="i <= passwordStrength.level ? passwordStrength.color : 'bg-bg-secondary'"
                ></div>
              </div>
              <span class="text-[10px] text-text-tertiary mt-1 block">
                密码强度：{{ passwordStrength.label }}
              </span>
            </div>
          </div>

          <!-- Confirm Password -->
          <div>
            <label class="text-xs text-text-secondary mb-1.5 block font-medium">确认密码</label>
            <div class="relative">
              <input
                v-model="form.confirmPassword"
                :type="showPassword ? 'text' : 'password'"
                placeholder="再次输入密码"
                autocomplete="new-password"
                class="w-full h-12 px-4 bg-bg-card border border-aura-border rounded-card text-sm
                       focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/20
                       transition-all duration-200"
                @keyup.enter="nextStep"
              />
            </div>
            <p
              v-if="form.confirmPassword && form.password !== form.confirmPassword"
              class="text-[11px] text-error mt-1"
            >
              两次密码不一致
            </p>
          </div>

          <!-- Next Button -->
          <button
            class="w-full h-12 rounded-card text-sm font-semibold text-white transition-all duration-200 flex items-center justify-center gap-2 mt-6"
            :class="isStep1Valid && form.password === form.confirmPassword
              ? 'gradient-instagram shadow-md hover:shadow-lg active:scale-[0.98]'
              : 'bg-bg-secondary text-text-tertiary cursor-not-allowed'"
            :disabled="!isStep1Valid || form.password !== form.confirmPassword"
            @click="nextStep"
          >
            下一步
          </button>
        </div>

        <!-- Step 2: Profile Info -->
        <div v-else key="step2" class="w-full space-y-4">
          <!-- Nickname -->
          <div>
            <label class="text-xs text-text-secondary mb-1.5 block font-medium">昵称</label>
            <input
              v-model="form.nickname"
              type="text"
              placeholder="给自己取个好听的名字"
              class="w-full h-12 px-4 bg-bg-card border border-aura-border rounded-card text-sm
                     focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/20
                     transition-all duration-200"
              @keyup.enter="handleRegister"
            />
          </div>

          <!-- Tip -->
          <div class="bg-accent-start/5 rounded-card p-4">
            <p class="text-xs text-text-secondary leading-relaxed">
              🎨 注册后，AI 会根据你的穿搭偏好为你生成专属风格画像，帮助你发现更多穿搭灵感。
            </p>
          </div>

          <!-- Register Button -->
          <button
            class="w-full h-12 rounded-card text-sm font-semibold text-white transition-all duration-200 flex items-center justify-center gap-2 mt-6"
            :class="isStep2Valid
              ? 'gradient-instagram shadow-md hover:shadow-lg active:scale-[0.98]'
              : 'bg-bg-secondary text-text-tertiary cursor-not-allowed'"
            :disabled="isLoading || !isStep2Valid"
            @click="handleRegister"
          >
            <Loader2 v-if="isLoading" :size="16" class="animate-spin" />
            {{ isLoading ? '注册中...' : '完成注册' }}
          </button>
        </div>
      </Transition>
    </div>

    <!-- Login Link -->
    <div class="py-6 text-center safe-area-bottom">
      <span class="text-sm text-text-secondary">已有账号？</span>
      <button
        class="text-sm text-accent-via font-semibold ml-1 hover:underline"
        @click="goLogin"
      >
        登录
      </button>
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
.slide-fade-enter-active {
  transition: all 0.3s ease;
}
.slide-fade-leave-active {
  transition: all 0.2s ease;
}
.slide-fade-enter-from {
  opacity: 0;
  transform: translateX(20px);
}
.slide-fade-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}
</style>
