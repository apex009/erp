import { defineStore } from 'pinia'
import { login, logout, getInfo, loginBySms, getMenus } from '@/api/auth'
import { ElMessage } from 'element-plus'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    name: '',
    roles: [],
    menus: [],        // 后端返回的菜单列表 [{permCode, permName, path}]
    permissions: [],  // 后端返回的权限编码列表
    initialized: false // 标记是否已完成初始化（getInfo + fetchMenus）
  }),
  getters: {
    hasMenu: (state) => (path) => {
      if (state.roles.includes('ADMIN')) return true
      return state.menus.some(m => m.path === path)
    },
    hasPerm: (state) => (code) => {
      if (state.roles.includes('ADMIN')) return true
      return state.permissions.includes(code)
    }
  },
  actions: {
    // 密码登录
    async login(userInfo) {
      const { username, password } = userInfo
      try {
        const data = await login({ username: username.trim(), password: password })
        return this.handleLoginSuccess(data)
      } catch (error) {
        return Promise.reject(error)
      }
    },

    // 短信登录
    async loginBySms(userInfo) {
      try {
        const data = await loginBySms(userInfo)
        return this.handleLoginSuccess(data)
      } catch (error) {
        return Promise.reject(error)
      }
    },

    handleLoginSuccess(data) {
      console.log('Login response data:', data)
      const tokenHead = data.tokenType || 'Bearer'
      const tokenStr = data.token
      this.token = `${tokenHead} ${tokenStr}`
      localStorage.setItem('token', this.token)
      // 重置初始化标记，确保路由守卫重新拉取数据
      this.initialized = false
      return true
    },

    /**
     * 初始化用户信息 + 菜单权限（登录后首次调用）
     * 必须严格顺序：getInfo → fetchMenus
     * 任一失败则整体失败，防止半初始化状态
     */
    async initUserSession() {
      try {
        // 1. 拉取用户信息
        const userInfo = await getInfo()
        if (!userInfo) {
          throw new Error('获取用户信息失败')
        }
        this.name = userInfo.nickname
        this.roles = userInfo.roles || []

        // 2. 拉取菜单权限
        const menuData = await getMenus()
        this.menus = menuData.menus || []
        this.permissions = menuData.permissions || []

        // 3. 标记初始化完成
        this.initialized = true
        return true
      } catch (error) {
        console.error('initUserSession failed:', error)
        // 初始化失败，清理状态，防止半进入
        this.initialized = false
        this.name = ''
        this.roles = []
        this.menus = []
        this.permissions = []
        return Promise.reject(error)
      }
    },

    // 退出登录 —— 彻底清理所有状态
    async logout() {
      try {
        await logout()
      } catch (error) {
        // 即使后端 logout 失败也要清理前端状态
        console.warn('Backend logout failed:', error)
      } finally {
        this._clearAll()
      }
    },

    // 重置 token（被踢出/过期时调用）
    resetToken() {
      this._clearAll()
    },

    // 彻底清理所有前端状态
    _clearAll() {
      this.token = ''
      this.name = ''
      this.roles = []
      this.menus = []
      this.permissions = []
      this.initialized = false
      localStorage.removeItem('token')
    }
  }
})
