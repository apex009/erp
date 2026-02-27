<template>
  <div class="login-container">
    <div class="login-content">
      <div class="login-left">
        <div class="login-info">
          <!-- Logo placeholder removed -->
          <h1 class="title">ERP 管理系统</h1>
          <p class="subtitle">企业资源计划</p>
        </div>
      </div>
      <div class="login-right">
        <div class="login-form-wrapper">
          <h2>欢迎回来</h2>
          <p class="welcome-text">请登录您的账户</p>
          
          <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" label-position="top" size="large">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="User" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input 
                v-model="loginForm.password" 
                type="password" 
                placeholder="请输入密码" 
                prefix-icon="Lock" 
                show-password 
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            
            <div class="form-options">
               <el-checkbox v-model="rememberMe">记住我</el-checkbox>
               <a href="#" class="forgot-password">忘记密码?</a>
            </div>

            <el-form-item>
              <el-button type="primary" :loading="loading" class="login-button" @click="handleLogin">
                登 录
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

const loginFormRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await authStore.login(loginForm)
        ElMessage.success('登录成功')
        router.push('/')
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.login-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow: hidden;
}

.login-content {
  display: flex;
  width: 900px;
  height: 550px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 15px 35px rgba(0,0,0,0.2);
  overflow: hidden;
  animation: fadeIn 0.6s ease-out;

  @media (max-width: 768px) {
    width: 90%;
    height: auto;
    flex-direction: column;
  }
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
  padding: 40px;
  text-align: center;
  position: relative;
  
  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: url('data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI4MCIgaGVpZ2h0PSI4MCIgdmlld0JveD0iMCAwIDgwIDgwIiBmaWxsPSJub25lIj48Y2lyY2xlIGN4PSI0MCIgY3k9IjQwIiByPSI0MCIgZmlsbD0id2hpdGUiIGZpbGwtb3BhY2l0eT0iMC4wNSIvPjwvc3ZnPg==');
    opacity: 0.3;
  }

  .title {
    font-size: 2.5rem;
    margin-bottom: 10px;
    font-weight: 700;
  }

  .subtitle {
    font-size: 1.1rem;
    opacity: 0.9;
  }
}

.login-right {
  flex: 1;
  padding: 60px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background-color: #ffffff;

  .login-form-wrapper {
    h2 {
      font-size: 1.8rem;
      color: #333;
      margin-bottom: 10px;
    }

    .welcome-text {
      color: #666;
      margin-bottom: 30px;
    }
    
    .form-options {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
        
        .forgot-password {
            color: #667eea;
            text-decoration: none;
            font-size: 14px;
            
            &:hover {
                text-decoration: underline;
            }
        }
    }

    .login-button {
      width: 100%;
      height: 45px;
      font-size: 16px;
      margin-top: 10px;
      background: linear-gradient(to right, #667eea, #764ba2);
      border: none;
      transition: transform 0.2s;
      
      &:hover {
        transform: translateY(-2px);
        opacity: 0.9;
      }
      
      &:active {
        transform: translateY(0);
      }
    }
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
