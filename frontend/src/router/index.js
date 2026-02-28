import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login/index.vue')
    },
    {
      path: '/',
      name: 'root',
      redirect: '/dashboard',
      component: () => import('@/layout/index.vue'),
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/dashboard/index.vue')
        },
        {
          path: '/system/users',
          name: 'users',
          component: () => import('@/views/system/users/index.vue')
        },
        {
          path: '/system/roles',
          name: 'roles',
          component: () => import('@/views/system/roles/index.vue')
        },
        {
          path: '/system/depts',
          name: 'depts',
          component: () => import('@/views/system/depts/index.vue')
        },
        {
          path: '/system/permissions',
          name: 'permissions',
          component: () => import('@/views/system/permissions/index.vue')
        },
        // Base Data
        {
          path: '/base/products',
          name: 'products',
          component: () => import('@/views/base/products/index.vue')
        },
        {
          path: '/base/suppliers',
          name: 'suppliers',
          component: () => import('@/views/base/suppliers/index.vue')
        },
        {
          path: '/base/customers',
          name: 'customers',
          component: () => import('@/views/base/customers/index.vue')
        },
        {
          path: '/base/warehouses',
          name: 'warehouses',
          component: () => import('@/views/base/warehouses/index.vue')
        },
        // Purchase
        {
          path: '/purchase/requests',
          name: 'purchaseRequests',
          component: () => import('@/views/purchase/requests/index.vue')
        },
        {
          path: '/purchase/orders',
          name: 'purchaseOrders',
          component: () => import('@/views/purchase/orders/index.vue')
        },
        // Sales
        {
          path: '/sales/leads',
          name: 'salesLeads',
          component: () => import('@/views/sales/leads/index.vue')
        },
        {
          path: '/sales/orders',
          name: 'salesOrders',
          component: () => import('@/views/sales/orders/index.vue')
        },
        // Inventory
        {
          path: '/inventory/stocks',
          name: 'inventoryStocks',
          component: () => import('@/views/inventory/stocks/index.vue')
        },
        {
          path: '/inventory/records',
          name: 'inventoryRecords',
          component: () => import('@/views/inventory/records/index.vue')
        },
        // Finance
        {
          path: '/finance/receivables',
          name: 'financeReceivables',
          component: () => import('@/views/finance/receivables/index.vue')
        },
        {
          path: '/finance/payables',
          name: 'financePayables',
          component: () => import('@/views/finance/payables/index.vue')
        }
      ]
    }
  ]
})

// 路由守卫：登录后自动拉取用户信息和菜单权限
const whiteList = ['/login']

router.beforeEach(async (to, from, next) => {
  const { useAuthStore } = await import('@/stores/auth')
  const authStore = useAuthStore()

  if (authStore.token) {
    if (to.path === '/login') {
      // 已登录时访问 login 页直接跳转首页
      next({ path: '/' })
    } else if (authStore.initialized) {
      // 已完成初始化，直接放行
      next()
    } else {
      // 未初始化：严格顺序执行 getInfo → fetchMenus
      try {
        await authStore.initUserSession()
        // 初始化成功后重新导航（确保菜单/路由生效）
        next({ ...to, replace: true })
      } catch (error) {
        console.error('User session init failed:', error)
        // 初始化失败：清理状态，跳转登录，提示用户
        authStore.resetToken()
        const { ElMessage } = await import('element-plus')
        ElMessage.error('登录状态异常，请重新登录')
        next(`/login?redirect=${to.path}`)
      }
    }
  } else {
    // 未登录
    if (whiteList.includes(to.path)) {
      next()
    } else {
      next(`/login?redirect=${to.path}`)
    }
  }
})

export default router
