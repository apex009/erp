<template>
  <div class="common-layout">
    <el-container>
      <el-aside width="240px" class="aside-menu">
        <div class="logo-container">
          <span>ERP 管理系统</span>
        </div>
        <el-menu
          active-text-color="#ffffff"
          background-color="#001529"
          class="el-menu-vertical-demo"
          :default-active="$route.path"
          text-color="#a6adb4"
          router
        >
          <!-- 经营看板 (所有角色都可见) -->
          <el-menu-item v-if="showMenu('/dashboard')" index="/dashboard">
            <el-icon><Odometer /></el-icon>
            <span>经营看板</span>
          </el-menu-item>

          <!-- 基础数据 -->
          <el-sub-menu v-if="showGroup('base')" index="base">
            <template #title>
              <el-icon><Files /></el-icon>
              <span>基础数据</span>
            </template>
            <el-menu-item v-if="showMenu('/base/products')" index="/base/products">商品管理</el-menu-item>
            <el-menu-item v-if="showMenu('/base/suppliers')" index="/base/suppliers">供应商管理</el-menu-item>
            <el-menu-item v-if="showMenu('/base/customers')" index="/base/customers">客户管理</el-menu-item>
            <el-menu-item v-if="showMenu('/base/warehouses')" index="/base/warehouses">仓库管理</el-menu-item>
          </el-sub-menu>

          <!-- 采购管理 -->
          <el-sub-menu v-if="showGroup('purchase')" index="purchase">
            <template #title>
              <el-icon><ShoppingCart /></el-icon>
              <span>采购管理</span>
            </template>
            <el-menu-item v-if="showMenu('/purchase/requests')" index="/purchase/requests">采购申请</el-menu-item>
            <el-menu-item v-if="showMenu('/purchase/orders')" index="/purchase/orders">采购订单</el-menu-item>
          </el-sub-menu>

          <!-- 销售管理 -->
          <el-sub-menu v-if="showGroup('sales')" index="sales">
            <template #title>
              <el-icon><ShoppingBag /></el-icon>
              <span>销售管理</span>
            </template>
            <el-menu-item v-if="showMenu('/sales/leads')" index="/sales/leads">商机/线索</el-menu-item>
            <el-menu-item v-if="showMenu('/sales/orders')" index="/sales/orders">销售订单</el-menu-item>
          </el-sub-menu>

          <!-- 库存管理 -->
          <el-sub-menu v-if="showGroup('inventory')" index="inventory">
            <template #title>
              <el-icon><Box /></el-icon>
              <span>库存管理</span>
            </template>
            <el-menu-item v-if="showMenu('/inventory/stocks')" index="/inventory/stocks">库存查询</el-menu-item>
            <el-menu-item v-if="showMenu('/inventory/records')" index="/inventory/records">库存流水</el-menu-item>
          </el-sub-menu>

          <!-- 财务管理 -->
          <el-sub-menu v-if="showGroup('finance')" index="finance">
            <template #title>
              <el-icon><Wallet /></el-icon>
              <span>财务管理</span>
            </template>
            <el-menu-item v-if="showMenu('/finance/receivables')" index="/finance/receivables">应收管理</el-menu-item>
            <el-menu-item v-if="showMenu('/finance/payables')" index="/finance/payables">应付管理</el-menu-item>
          </el-sub-menu>

          <!-- 系统管理 -->
          <el-sub-menu v-if="showGroup('system')" index="system">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item v-if="showMenu('/system/users')" index="/system/users">用户管理</el-menu-item>
            <el-menu-item v-if="showMenu('/system/roles')" index="/system/roles">角色管理</el-menu-item>
            <el-menu-item v-if="showMenu('/system/depts')" index="/system/depts">部门管理</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header class="header">
          <div class="header-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item>{{ $route.meta.title || $route.name }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <el-tag v-if="currentRoleLabel" :type="currentRoleType" size="small" class="role-tag">{{ currentRoleLabel }}</el-tag>
            <el-dropdown>
              <span class="el-dropdown-link">
                <el-avatar :size="32" class="user-avatar">{{ authStore.name ? authStore.name.charAt(0).toUpperCase() : 'U' }}</el-avatar>
                <span class="username">{{ authStore.name }}</span>
                <el-icon class="el-icon--right">
                  <arrow-down />
                </el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        <el-main>
          <router-view v-slot="{ Component }">
            <transition name="fade-transform" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { Odometer, Setting, ArrowDown, Files, ShoppingCart, ShoppingBag, Box, Wallet } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

const routePermMap = {
  '/dashboard': null,
  '/base/products': 'api:products:read',
  '/base/suppliers': 'api:suppliers:read',
  '/base/customers': 'api:customers:read',
  '/base/warehouses': 'api:warehouses:read',
  '/purchase/requests': 'api:purchase:read',
  '/purchase/orders': 'api:purchase:read',
  '/sales/leads': 'api:leads:read',
  '/sales/orders': 'api:sales:read',
  '/inventory/stocks': 'api:inventory:read',
  '/inventory/records': 'api:inventory:read',
  '/finance/receivables': 'api:receivables:read',
  '/finance/payables': 'api:payables:read',
  '/system/users': 'api:system:read',
  '/system/roles': 'api:system:read',
  '/system/depts': 'api:system:read'
}

/**
 * 判断是否显示某个菜单项（基于 RBAC 菜单 + 额外 API 权限拦截）
 * ADMIN 角色看到所有菜单
 */
const showMenu = (path) => {
  if (authStore.roles.includes('ADMIN')) return true
  const inMenu = authStore.menus.some(m => m.path === path)
  if (!inMenu) return false
  
  // 防止错配：无读取数据权限则强行隐藏入口
  const reqPerm = routePermMap[path]
  if (reqPerm) {
    return authStore.hasPerm(reqPerm)
  }
  return true
}

/**
 * 判断子菜单组是否有可见项
 */
const showGroup = (prefix) => {
  if (authStore.roles.includes('ADMIN')) return true
  return authStore.menus.some(m => m.path && m.path.startsWith('/' + prefix + '/'))
}

/**
 * 当前角色标签
 */
const currentRoleLabel = computed(() => {
  if (authStore.roles.includes('ADMIN')) return '超级管理员'
  if (authStore.roles.includes('SALES')) return '销售'
  if (authStore.roles.includes('FIN')) return '财务'
  return ''
})

const currentRoleType = computed(() => {
  if (authStore.roles.includes('ADMIN')) return 'danger'
  if (authStore.roles.includes('SALES')) return 'success'
  if (authStore.roles.includes('FIN')) return 'warning'
  return 'info'
})

const handleLogout = async () => {
    await authStore.logout()
    router.push('/login')
}
</script>

<style scoped lang="scss">
.common-layout {
  height: 100vh;
  
  .el-container {
    height: 100%;
  }
}

.aside-menu {
  background-color: #001529;
  box-shadow: 2px 0 6px rgba(0,21,41,.35);
  z-index: 10;
  display: flex;
  flex-direction: column;
  
  .logo-container {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #002140;
    color: white;
    font-size: 20px;
    font-weight: bold;
    
    img {
      width: 32px;
      height: 32px;
      margin-right: 10px;
    }
  }

  .el-menu {
    border-right: none;
    flex: 1;
  }
}

.header {
  height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  padding: 0 20px;
  z-index: 9;
  
  .header-right {
    display: flex;
    align-items: center;
    gap: 12px;

    .role-tag {
      font-weight: 500;
    }

    .el-dropdown-link {
      cursor: pointer;
      display: flex;
      align-items: center;
      
      .user-avatar {
          margin-right: 8px;
          background: #667eea;
      }
      
      .username {
          font-weight: 500;
      }
    }
  }
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}

/* Page Transition */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.5s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
