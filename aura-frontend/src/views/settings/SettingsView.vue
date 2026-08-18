<script setup lang="ts">
import { ref } from 'vue'
import { 
  User, Bell, Moon, Globe, Shield, Save, LogOut, ChevronRight
} from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const router = useRouter()
const authStore = useAuthStore()

const settings = ref({
  notifications: true,
  darkMode: false,
  language: 'zh-CN',
})

const handleSave = () => {
  alert('设置已保存')
}

const handleLogout = () => {
  authStore.logout()
  router.push('/auth/login')
}

const settingGroups = [
  {
    title: '账号设置',
    items: [
      { icon: User, label: '个人信息', description: '管理你的账号信息' },
      { icon: Shield, label: '安全设置', description: '修改密码、绑定手机' },
    ]
  },
  {
    title: '偏好设置',
    items: [
      { icon: Bell, label: '通知设置', description: '管理推送和邮件通知' },
      { icon: Moon, label: '外观设置', description: '主题、字体大小' },
      { icon: Globe, label: '语言设置', description: '界面语言' },
    ]
  },
]
</script>

<template>
  <div class="min-h-screen bg-white">
    <div class="max-w-2xl mx-auto px-8 py-8">
      
      <!-- Header -->
      <header class="mb-10">
        <h1 class="text-4xl font-bold text-black mb-3 tracking-tight">设置</h1>
        <p class="text-lg text-gray-500">管理你的账号和偏好设置</p>
      </header>

      <!-- Profile Card -->
      <div class="rounded-2xl border border-gray-200 p-6 mb-8">
        <div class="flex items-center gap-4">
          <div class="w-16 h-16 rounded-full bg-gray-100 flex items-center justify-center">
            <User :size="28" class="text-gray-400" />
          </div>
          <div>
            <h2 class="text-lg font-bold text-black">
              {{ authStore.user?.nickname || 'User' }}
            </h2>
            <p class="text-sm text-gray-500">{{ authStore.user?.username || '' }}</p>
          </div>
        </div>
      </div>

      <!-- Settings Groups -->
      <div v-for="group in settingGroups" :key="group.title" class="mb-8">
        <h3 class="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-4">{{ group.title }}</h3>
        <div class="rounded-2xl border border-gray-200 overflow-hidden">
          <button 
            v-for="(item, index) in group.items"
            :key="item.label"
            class="w-full flex items-center justify-between px-6 py-4 hover:bg-gray-50 transition-colors"
            :class="{ 'border-t border-gray-100': index > 0 }"
          >
            <div class="flex items-center gap-4">
              <div class="w-10 h-10 rounded-xl bg-gray-50 flex items-center justify-center">
                <component :is="item.icon" :size="18" class="text-gray-500" />
              </div>
              <div class="text-left">
                <p class="text-sm font-medium text-black">{{ item.label }}</p>
                <p class="text-xs text-gray-500">{{ item.description }}</p>
              </div>
            </div>
            <ChevronRight :size="16" class="text-gray-300" />
          </button>
        </div>
      </div>

      <!-- Quick Settings -->
      <div class="rounded-2xl border border-gray-200 p-6 mb-8">
        <div class="flex items-center justify-between py-2">
          <div class="flex items-center gap-3">
            <Bell :size="16" class="text-gray-500" />
            <span class="text-sm text-black">通知提醒</span>
          </div>
          <label class="relative inline-flex items-center cursor-pointer">
            <input v-model="settings.notifications" type="checkbox" class="sr-only peer" />
            <div class="w-9 h-5 bg-gray-200 peer-focus:ring-2 peer-focus:ring-black/20 
                        rounded-full peer peer-checked:after:translate-x-full 
                        peer-checked:after:border-white after:content-[''] after:absolute 
                        after:top-[2px] after:left-[2px] after:bg-white after:rounded-full 
                        after:h-4 after:w-4 after:transition-all peer-checked:bg-black">
            </div>
          </label>
        </div>

        <div class="border-t border-gray-100 my-3"></div>

        <div class="flex items-center justify-between py-2">
          <div class="flex items-center gap-3">
            <Moon :size="16" class="text-gray-500" />
            <span class="text-sm text-black">深色模式</span>
          </div>
          <label class="relative inline-flex items-center cursor-pointer">
            <input v-model="settings.darkMode" type="checkbox" class="sr-only peer" />
            <div class="w-9 h-5 bg-gray-200 peer-focus:ring-2 peer-focus:ring-black/20 
                        rounded-full peer peer-checked:after:translate-x-full 
                        peer-checked:after:border-white after:content-[''] after:absolute 
                        after:top-[2px] after:left-[2px] after:bg-white after:rounded-full 
                        after:h-4 after:w-4 after:transition-all peer-checked:bg-black">
            </div>
          </label>
        </div>
      </div>

      <!-- Actions -->
      <div class="flex items-center gap-4">
        <button 
          @click="handleSave"
          class="flex-1 h-12 bg-black hover:bg-gray-800 text-white text-sm font-medium
                 rounded-xl transition-colors flex items-center justify-center gap-2"
        >
          <Save :size="16" />
          保存设置
        </button>
        <button 
          @click="handleLogout"
          class="h-12 px-6 bg-white hover:bg-gray-50 text-gray-600 text-sm font-medium
                 rounded-xl transition-colors flex items-center justify-center gap-2 
                 border border-gray-200"
        >
          <LogOut :size="16" />
          退出登录
        </button>
      </div>

      <!-- Footer -->
      <div class="mt-12 text-center">
        <p class="text-xs text-gray-400">Aura v1.0</p>
        <p class="text-xs text-gray-400 mt-1">Multi-Agent Orchestration System</p>
      </div>
    </div>
  </div>
</template>
