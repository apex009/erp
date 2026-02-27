<template>
  <div class="common-layout">
    <el-container>
      <el-aside width="240px" class="aside-menu">
        <div class="logo-container">
          <!-- <img src="@/assets/logo.svg" alt="Logo" /> -->
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
          <el-menu-item index="/dashboard">
            <el-icon><Odometer /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          <el-sub-menu index="base">
            <template #title>
              <el-icon><Files /></el-icon>
              <span>基础数据</span>
            </template>
            <el-menu-item index="/base/products">商品管理</el-menu-item>
            <el-menu-item index="/base/suppliers">供应商管理</el-menu-item>
            <el-menu-item index="/base/customers">客户管理</el-menu-item>
            <el-menu-item index="/base/warehouses">仓库管理</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="purchase">
            <template #title>
              <el-icon><ShoppingCart /></el-icon>
              <span>采购管理</span>
            </template>
            <el-menu-item index="/purchase/requests">采购申请</el-menu-item>
            <el-menu-item index="/purchase/orders">采购订单</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="sales">
            <template #title>
              <el-icon><ShoppingBag /></el-icon>
              <span>销售管理</span>
            </template>
            <el-menu-item index="/sales/leads">商机/线索</el-menu-item>
            <el-menu-item index="/sales/orders">销售订单</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="inventory">
            <template #title>
              <el-icon><Box /></el-icon>
              <span>库存管理</span>
            </template>
            <el-menu-item index="/inventory/stocks">库存查询</el-menu-item>
            <el-menu-item index="/inventory/records">库存流水</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="finance">
            <template #title>
              <el-icon><Wallet /></el-icon>
              <span>财务管理</span>
            </template>
            <el-menu-item index="/finance/receivables">应收管理</el-menu-item>
            <el-menu-item index="/finance/payables">应付管理</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="system">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/system/users">用户管理</el-menu-item>
            <el-menu-item index="/system/roles">角色管理</el-menu-item>
            <el-menu-item index="/system/depts">部门管理</el-menu-item>
            <el-menu-item index="/system/permissions">权限管理</el-menu-item>
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
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { Odometer, Setting, ArrowDown, Files, ShoppingCart, ShoppingBag, Box, Wallet } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

// Load user info on mount if not exists (optional, but good practice)
// authStore.getInfo() 

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


