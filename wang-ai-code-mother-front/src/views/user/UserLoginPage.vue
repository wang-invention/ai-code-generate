<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { login } from '@/api/userController'
import { useLoginUserStore } from '@/stores/LoginUser'

const router = useRouter()
const route = useRoute()
const loginUserStore = useLoginUserStore()

const formState = reactive({
  userAccount: 'wang',
  userPassword: '12345678'
})

const loading = ref(false)

const handleLogin = async () => {
  if (!formState.userAccount || !formState.userPassword) {
    message.warning('请输入账号和密码')
    return
  }

  loading.value = true
  try {
    const res = await login(formState)
    if (res.data.code === 0 && res.data.data) {
      message.success('登录成功')

      const loginUserVO = res.data.data
      loginUserStore.setLoginUser(loginUserVO)

      if (loginUserVO.token) {
        localStorage.setItem('token', loginUserVO.token)
        console.log('Token saved:', loginUserVO.token)
      } else {
        console.error('No token in response:', loginUserVO)
      }

      const redirect = (route.query.redirect as string) || '/'
      router.push(redirect)
    } else {
      message.error(res.data.message || '登录失败')
    }
  } catch (error) {
    console.error('Login error:', error)
    message.error('登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const goToRegister = () => {
  router.push('/user/register')
}
</script>

<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>用户登录</h1>
        <p>欢迎来到AI零代码应用生成平台</p>
      </div>

      <a-form
        :model="formState"
        layout="vertical"
        @submit.prevent="handleLogin"
      >
        <a-form-item label="账号">
          <a-input
            v-model:value="formState.userAccount"
            placeholder="请输入账号"
            size="large"
          />
        </a-form-item>

        <a-form-item label="密码">
          <a-input-password
            v-model:value="formState.userPassword"
            placeholder="请输入密码"
            size="large"
          />
        </a-form-item>

        <a-form-item>
          <a-button
            type="primary"
            html-type="submit"
            size="large"
            block
            :loading="loading"
          >
            登录
          </a-button>
        </a-form-item>
      </a-form>

      <div class="login-footer">
        <span>还没有账号？</span>
        <a @click="goToRegister">立即注册</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: url('https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=2072&auto=format&fit=crop') no-repeat center center;
  background-size: cover;
  position: relative;
}

.login-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(5px);
}

.login-box {
  position: relative;
  width: 420px;
  padding: 48px 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideIn 0.6s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-header h1 {
  font-size: 32px;
  margin-bottom: 12px;
  background: #1890ff;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 700;
}

.login-header p {
  font-size: 14px;
  color: #666;
}

:deep(.ant-form-item-label > label) {
  font-weight: 500;
  color: #333;
}

:deep(.ant-input),
:deep(.ant-input-password) {
  border-radius: 8px;
  border: 2px solid #e8e8e8;
  transition: all 0.3s;
}

:deep(.ant-input:hover),
:deep(.ant-input-password:hover) {
  border-color: #667eea;
}

:deep(.ant-input:focus),
:deep(.ant-input-password:focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

:deep(.ant-btn-primary) {
  height: 48px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  background: #1890ff;
  border: none;
  transition: all 0.3s;
}

:deep(.ant-btn-primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(24, 144, 255, 0.4);
}

:deep(.ant-btn-primary:active) {
  transform: translateY(0);
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #666;
}

.login-footer a {
  color: #1890ff;
  font-weight: 500;
  cursor: pointer;
}

.login-footer a:hover {
  color: #764ba2;
  text-decoration: underline;
}
</style>
