import router from './router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const whiteList = ['/login']

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  const hasToken = authStore.token

  if (hasToken) {
    if (to.path === '/login') {
      next({ path: '/' })
    } else {
      // Check if user info is loaded (optional step, strict mode)
      // if (!authStore.name) {
      //   try {
      //     await authStore.getInfo()
      //     next()
      //   } catch (error) {
      //     await authStore.resetToken()
      //     next(`/login?redirect=${to.path}`)
      //   }
      // } else {
      next()
      // }
    }
  } else {
    if (whiteList.indexOf(to.path) !== -1) {
      next()
    } else {
      next(`/login?redirect=${to.path}`)
    }
  }
})
