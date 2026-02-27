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

export default router
