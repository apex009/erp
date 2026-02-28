import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// Request interceptor
service.interceptors.request.use(
  config => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers['Authorization'] = authStore.token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 防止 401 多次触发跳转
let isRedirecting = false

// Response interceptor
service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      // 401: token 过期/无效
      if (res.code === 401) {
        if (!isRedirecting) {
          isRedirecting = true
          const authStore = useAuthStore()
          authStore.resetToken()
          ElMessage.error('登录状态已过期，请重新登录')
          router.replace('/login?redirect=' + router.currentRoute.value.fullPath)
          setTimeout(() => { isRedirecting = false }, 2000)
        }
        return Promise.reject(new Error(res.msg || '未授权'))
      }

      // 403: 无权限
      if (res.code === 403) {
        ElMessage.warning('您没有操作权限')
        return Promise.reject(new Error('无权限'))
      }

      // 其他错误
      ElMessage({
        message: res.msg || '请求失败',
        type: 'error',
        duration: 3000
      })
      return Promise.reject(new Error(res.msg || 'Error'))
    } else {
      return res.data
    }
  },
  error => {
    // HTTP 层错误（网络断开、超时等）
    const status = error.response?.status
    if (status === 401) {
      if (!isRedirecting) {
        isRedirecting = true
        const authStore = useAuthStore()
        authStore.resetToken()
        ElMessage.error('登录状态已过期，请重新登录')
        router.replace('/login?redirect=' + router.currentRoute.value.fullPath)
        setTimeout(() => { isRedirecting = false }, 2000)
      }
    } else if (status === 403) {
      ElMessage.warning('您没有操作权限')
    } else {
      ElMessage.error(error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default service
