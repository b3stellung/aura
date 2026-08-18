import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // Auth pages (no app navigation)
    {
      path: '/auth',
      component: () => import('@/layouts/AuthLayout.vue'),
      children: [
        {
          path: 'login',
          name: 'Login',
          component: () => import('@/views/auth/LoginView.vue'),
        },
        {
          path: 'register',
          name: 'Register',
          component: () => import('@/views/auth/RegisterView.vue'),
        },
      ],
    },

    // Main app
    {
      path: '/',
      component: () => import('@/layouts/AppLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'Home',
          component: () => import('@/views/home/HomeView.vue'),
          meta: { title: '首页' },
        },
        {
          path: 'wardrobe',
          name: 'Wardrobe',
          component: () => import('@/views/WardrobeView.vue'),
          meta: { title: '衣橱' },
        },
        {
          path: 'ai',
          name: 'AiAssistant',
          component: () => import('@/views/AiAssistantView.vue'),
          meta: { title: '造型师' },
        },
        {
          path: 'history',
          name: 'History',
          component: () => import('@/views/history/HistoryView.vue'),
          meta: { title: '历史' },
        },
        {
          path: 'settings',
          name: 'Settings',
          component: () => import('@/views/settings/SettingsView.vue'),
          meta: { title: '设置' },
        },
        {
          path: 'sandbox',
          name: 'SandboxList',
          redirect: { name: 'AiAssistant' },
          meta: { title: 'Agent' },
        },
        {
          path: 'sandbox/:id',
          name: 'SandboxWorkspace',
          component: () => import('@/views/sandbox/AgentWorkspace.vue'),
          meta: { title: 'Agent Sandbox' },
        },
      ],
    },
  ],
})

// Navigation guard
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('aura-token')

  if (to.meta.requiresAuth && !token) {
    next({ name: 'Login' })
  } else if (to.path === '/auth/login' && token) {
    next({ name: 'Home' })
  } else {
    next()
  }
})

export default router
