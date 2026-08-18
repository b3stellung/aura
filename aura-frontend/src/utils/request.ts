import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { useAuthStore } from '@/stores/auth'

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API || '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器 - 自动附加Token
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const authStore = useAuthStore()
    const token = authStore.token

    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  (error: unknown) => {
    console.error('请求拦截器错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器 - 统一错误处理
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data

    // 后端返回的统一响应格式: { code, message, data }
    if (res.code !== undefined && res.code !== 0 && res.code !== 200) {
      // Token过期或无效
      if (res.code === 401) {
        const authStore = useAuthStore()
        authStore.clearAuth()
        // 可以在这里跳转到登录页
        window.location.href = '/auth/login'
      }

      console.error('API错误:', res.message || '未知错误')
      return Promise.reject(new Error(res.message || '请求失败'))
    }

    return res
  },
  (error: unknown) => {
    console.error('响应错误:', error)

    if (axios.isAxiosError(error) && error.response) {
      const { status } = error.response

      switch (status) {
        case 401: {
          // Token过期或未授权
          const authStore = useAuthStore()
          authStore.clearAuth()
          window.location.href = '/auth/login'
          break
        }
        case 403:
          console.error('没有权限访问该资源')
          break
        case 404:
          console.error('请求的资源不存在')
          break
        case 500:
          console.error('服务器内部错误')
          break
        default:
          console.error(`请求失败: ${status}`)
      }
    } else if (error instanceof Error && error.message.includes('timeout')) {
      console.error('请求超时，请检查网络连接')
    } else if (error instanceof Error && error.message.includes('Network Error')) {
      console.error('网络错误，请检查网络连接')
    }

    return Promise.reject(error)
  }
)

export default service
