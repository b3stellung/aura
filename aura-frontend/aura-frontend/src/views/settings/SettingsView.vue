<script setup lang="ts">
import { ref } from 'vue'
import { 
  User, Bell, Moon, Sun, Globe, Shield, 
  Save, LogOut, ChevronRight
} from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const router = useRouter()
const authStore = useAuthStore()

const settings = ref({
  nickname: authStore.user?.nickname || '',
  studentId: authStore.user?.studentId || '',
  notifications: true,
  darkMode: false,
  language: 'zh-CN',
})

const handleSave = () => {
  // Mock save
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
      { icon: User, label: '个人信息', description: '管理你的账号信息', action: () => {} },
      { icon: Shield, label: '安全设置', description: '修改密码、绑定手机', action: () => {} },
    ]
  },
  {
    title: '偏好设置',
    items: [
      { icon: Bell, label: '通知设置', description: '管理推送和邮件通知', action: () => {} },
      { icon: Moon, label: '外观设置', description: '主题、字体大小', action: () => {} },
      { icon: Globe, label: '语言设置', description: '界面语言', action: () => {} },
    ]
  },
]
</script>

<template>
  <div class="p-6 max-w-2xl mx-auto">
    <!-- Header -->
    <div class="mb-6">
      <h1 class="text-display font-semibold text-text-primary mb-1">设置</h1>
      <p class="text-body text-text-secondary">管理你的账号和偏好设置</p>
    </div>

    <!-- Profile Card -->
    <div class="bg-bg-secondary rounded-card p-6 mb-6"
         style="box-shadow: var(--shadow-card)">
      <div class="flex items-center gap-4">
        <div class="w-16 h-16 rounded-full bg-brand-light flex items-center justify-center">
          <User :size="32" :stroke-width="1.5" class="text-brand-primary" />
        </div>
        <div>
          <h2 class="text-h3 font-semibold text-text-primary">
            {{ authStore.user?.nickname || '未设置昵称' }}
          </h2>
          <p class="text-sm text-text-secondary">
            {{ authStore.user?.username || '' }}
          </p>
          <p class="text-xs text-text-tertiary">
            学号: {{ authStore.user?.studentId || '未绑定' }}
          </p>
        </div>
      </div>
    </div>

    <!-- Settings Groups -->
    <div v-for="group in settingGroups" :key="group.title" class="mb-6">
      <h3 class="text-sm font-semibold text-text-secondary mb-3">{{ group.title }}</h3>
      <div class="bg-bg-secondary rounded-card overflow-hidden"
           style="box-shadow: var(--shadow-card)">
        <button 
          v-for="(item, index) in group.items"
          :key="item.label"
          @click="item.action"
          class="w-full flex items-center justify-between p-4 hover:bg-bg-hover transition-colors"
          :class="{ 'border-b border-border': index < group.items.length - 1 }"
        >
          <div class="flex items-center gap-3">
            <div class="w-8 h-8 rounded-btn bg-bg-tertiary flex items-center justify-center">
              <component :is="item.icon" :size="16" :stroke-width="2" class="text-text-secondary" />
            </div>
            <div class="text-left">
              <p class="text-sm font-semibold text-text-primary">{{ item.label }}</p>
              <p class="text-xs text-text-secondary">{{ item.description }}</p>
            </div>
          </div>
          <ChevronRight :size="16" :stroke-width="2" class="text-text-tertiary" />
        </button>
      </div>
    </div>

    <!-- Quick Settings -->
    <div class="bg-bg-secondary rounded-card p-4 mb-6"
         style="box-shadow: var(--shadow-card)">
      <div class="flex items-center justify-between py-2">
        <div class="flex items-center gap-3">
          <Bell :size="16" :stroke-width="2" class="text-text-secondary" />
          <span class="text-sm text-text-primary">通知提醒</span>
        </div>
        <label class="relative inline-flex items-center cursor-pointer">
          <input 
            v-model="settings.notifications"
            type="checkbox" 
            class="sr-only peer"
          />
          <div class="w-9 h-5 bg-bg-tertiary peer-focus:ring-2 peer-focus:ring-brand-primary 
                      rounded-full peer peer-checked:after:translate-x-full 
                      peer-checked:after:border-white after:content-[''] after:absolute 
                      after:top-[2px] after:left-[2px] after:bg-white after:rounded-full 
                      after:h-4 after:w-4 after:transition-all peer-checked:bg-brand-primary">
          </div>
        </label>
      </div>

      <div class="border-t border-border my-2"></div>

      <div class="flex items-center justify-between py-2">
        <div class="flex items-center gap-3">
          <Moon :size="16" :stroke-width="2" class="text-text-secondary" />
          <span class="text-sm text-text-primary">深色模式</span>
        </div>
        <label class="relative inline-flex items-center cursor-pointer">
          <input 
            v-model="settings.darkMode"
            type="checkbox" 
            class="sr-only peer"
          />
          <div class="w-9 h-5 bg-bg-tertiary peer-focus:ring-2 peer-focus:ring-brand-primary 
                      rounded-full peer peer-checked:after:translate-x-full 
                      peer-checked:after:border-white after:content-[''] after:absolute 
                      after:top-[2px] after:left-[2px] after:bg-white after:rounded-full 
                      after:h-4 after:w-4 after:transition-all peer-checked:bg-brand-primary">
          </div>
        </label>
      </div>
    </div>

    <!-- Actions -->
    <div class="flex items-center gap-4">
      <button 
        @click="handleSave"
        class="flex-1 h-10 bg-brand-primary hover:bg-brand-hover text-white font-semibold text-sm
               rounded-btn transition-colors flex items-center justify-center gap-2"
      >
        <Save :size="16" :stroke-width="2" />
        保存设置
      </button>
      <button 
        @click="handleLogout"
        class="h-10 px-4 bg-bg-secondary hover:bg-bg-hover text-text-secondary font-semibold text-sm
               rounded-btn transition-colors flex items-center justify-center gap-2"
        style="box-shadow: var(--shadow-card)"
      >
        <LogOut :size="16" :stroke-width="2" />
        退出登录
      </button>
    </div>

    <!-- Footer -->
    <div class="mt-8 text-center">
      <p class="text-xs text-text-tertiary">Aura v1.0.0</p>
      <p class="text-xs text-text-tertiary">个人美学操作系统</p>
    </div>
  </div>
</template>
