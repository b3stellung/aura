<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const isLoaded = ref(false)

// Mouse spotlight effect
const mouseX = ref(0)
const mouseY = ref(0)

const handleMouseMove = (e: MouseEvent) => {
  mouseX.value = e.clientX
  mouseY.value = e.clientY
}

onMounted(() => {
  setTimeout(() => { isLoaded.value = true }, 100)
  window.addEventListener('mousemove', handleMouseMove)
})

onUnmounted(() => {
  window.removeEventListener('mousemove', handleMouseMove)
})

const form = ref({ username: '', password: '', confirmPassword: '', nickname: '' })
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
    authStore.setAuth(
      'mock-token-' + Date.now(),
      'mock-refresh-token',
      { id: '1', username: form.value.username, nickname: form.value.nickname }
    )
    router.push('/')
  } catch (e) {
    error.value = '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-white flex items-center justify-center p-8 relative overflow-hidden">
    
    <!-- Spotlight Effect -->
    <div 
      class="fixed w-[400px] h-[400px] rounded-full pointer-events-none z-50 mix-blend-difference"
      :style="{
        left: mouseX - 200 + 'px',
        top: mouseY - 200 + 'px',
        background: 'radial-gradient(circle, rgba(255,255,255,0.8) 0%, transparent 70%)',
        transition: 'left 0.1s ease-out, top 0.1s ease-out'
      }"
    />

    <!-- Background Pattern -->
    <div class="absolute inset-0 opacity-10">
      <div class="absolute inset-0" style="background-image: radial-gradient(circle, #e5e7eb 1px, transparent 1px); background-size: 40px 40px;" />
    </div>

    <div class="relative w-full max-w-sm" :class="{ 'animate-fade-in': isLoaded }">
      
      <!-- Logo -->
      <div class="text-center mb-14">
        <div class="w-24 h-24 mx-auto mb-8 rounded-full bg-black flex items-center justify-center
                    hover:scale-105 transition-transform duration-500 cursor-pointer">
          <span class="text-white text-4xl font-bold">A</span>
        </div>
        <h1 class="text-4xl font-bold text-black mb-3 tracking-tight">注册 Aura</h1>
        <p class="text-sm text-gray-500">创建你的Multi-Agent系统账号</p>
      </div>

      <!-- Form -->
      <div class="space-y-5">
        
        <!-- Nickname -->
        <div>
          <input
            v-model="form.nickname"
            type="text"
            class="w-full h-12 px-5 bg-gray-50 rounded-xl text-sm text-black
                   placeholder:text-gray-400 border border-gray-200
                   focus:border-black focus:bg-white transition-all duration-300"
            placeholder="昵称"
          />
        </div>

        <!-- Username -->
        <div>
          <input
            v-model="form.username"
            type="text"
            class="w-full h-12 px-5 bg-gray-50 rounded-xl text-sm text-black
                   placeholder:text-gray-400 border border-gray-200
                   focus:border-black focus:bg-white transition-all duration-300"
            placeholder="用户名"
          />
        </div>

        <!-- Password -->
        <div>
          <input
            v-model="form.password"
            type="password"
            class="w-full h-12 px-5 bg-gray-50 rounded-xl text-sm text-black
                   placeholder:text-gray-400 border border-gray-200
                   focus:border-black focus:bg-white transition-all duration-300"
            placeholder="密码"
          />
        </div>

        <!-- Confirm Password -->
        <div>
          <input
            v-model="form.confirmPassword"
            type="password"
            class="w-full h-12 px-5 bg-gray-50 rounded-xl text-sm text-black
                   placeholder:text-gray-400 border border-gray-200
                   focus:border-black focus:bg-white transition-all duration-300"
            placeholder="确认密码"
          />
        </div>

        <!-- Error -->
        <p v-if="error" class="text-sm text-red-500 text-center">{{ error }}</p>

        <!-- Submit -->
        <button
          @click="handleRegister"
          :disabled="loading"
          class="w-full h-12 bg-black hover:bg-gray-800 text-white text-sm font-medium
                 rounded-xl transition-all duration-300 disabled:opacity-50"
        >
          {{ loading ? '注册中...' : '注册' }}
        </button>

        <!-- Divider -->
        <div class="flex items-center gap-4 my-8">
          <div class="flex-1 h-px bg-gray-200" />
          <span class="text-xs text-gray-400">或</span>
          <div class="flex-1 h-px bg-gray-200" />
        </div>

        <!-- Login -->
        <p class="text-center text-sm text-gray-500">
          已有账号？
          <router-link to="/auth/login" class="text-black font-medium hover:underline transition-colors">
            立即登录
          </router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.animate-fade-in {
  animation: fade-in 0.6s ease-out;
}

@keyframes fade-in {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
