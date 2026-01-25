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
  background: #f3f4f6;
  padding: 24px 16px;
}

.login-box {
  width: 400px;
  padding: 24px 24px 20px;
  background: #ffffff;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.login-header {
  margin-bottom: 24px;
}

.login-header h1 {
  font-size: 20px;
  margin-bottom: 8px;
  color: #111827;
  font-weight: 600;
}

.login-header p {
  font-size: 13px;
  color: #6b7280;
}

:deep(.ant-form-item-label > label) {
  font-weight: 500;
  color: #374151;
}

:deep(.ant-input),
:deep(.ant-input-password) {
  border-radius: 4px;
  border: 1px solid #d1d5db;
}

:deep(.ant-input:hover),
:deep(.ant-input-password:hover) {
  border-color: #9ca3af;
}

:deep(.ant-input:focus),
:deep(.ant-input-password:focus) {
  border-color: #2563eb;
  box-shadow: none;
}

:deep(.ant-btn-primary) {
  height: 40px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
}

:deep(.ant-btn-primary:hover) {
  box-shadow: none;
}

.login-footer {
  text-align: center;
  margin-top: 16px;
  font-size: 13px;
  color: #6b7280;
}

.login-footer a {
  color: #2563eb;
  font-weight: 500;
  cursor: pointer;
}
</style>
