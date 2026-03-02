<template>
  <div class="error-page">
    <div class="error-code">403</div>
    <div class="error-desc">抱歉，您没有操作权限访问该页面或功能。</div>
    <el-button type="primary" @click="goHome">返回首页</el-button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.error-page {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}
.error-code {
  font-size: 120px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 20px;
}
.error-desc {
  font-size: 20px;
  color: #606266;
  margin-bottom: 30px;
}
</style>
