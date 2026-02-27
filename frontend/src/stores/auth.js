import { defineStore } from 'pinia'
import { login, logout, getInfo } from '@/api/auth'
import { ElMessage } from 'element-plus'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    name: '',
    roles: []
  }),
  actions: {
    // Login
    async login(userInfo) {
      const { username, password } = userInfo
      try {
        const data = await login({ username: username.trim(), password: password })
        console.log('Login response data:', data)
        const tokenHead = data.tokenType || 'Bearer'
        const tokenStr = data.token
        this.token = `${tokenHead} ${tokenStr}`

        localStorage.setItem('token', this.token)
        return true
      } catch (error) {
        return Promise.reject(error)
      }
    },

    // Get user info
    async getInfo() {
      try {
        const data = await getInfo()
        // AuthController: Result.success(UserContext.get());
        // UserPrincipal: id, username, nickname, roles
        if (!data) {
          throw new Error('Verification failed, please Login again.')
        }
        this.name = data.nickname
        this.roles = data.roles // This might need parsing if it comes as string or list
        // AuthController puts roles list into LoginResponse but /me returns UserPrincipal.
        // UserPrincipal usually has authorities.
        return data
      } catch (error) {
        return Promise.reject(error)
      }
    },

    // Logout
    async logout() {
      try {
        await logout()
        this.token = ''
        this.roles = []
        localStorage.removeItem('token')
      } catch (error) {
        return Promise.reject(error)
      }
    },

    // Remove token
    async resetToken() {
      this.token = ''
      this.roles = []
      localStorage.removeItem('token')
    }
  }
})
