<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { register } from '@/api/userController'

const router = useRouter()

const formState = reactive({
  userAccount: '',
  userPassword: '',
  checkPassword: ''
})

const loading = ref(false)

const handleRegister = async () => {
  if (!formState.userAccount || !formState.userPassword || !formState.checkPassword) {
    message.warning('请填写完整信息')
    return
  }

  if (formState.userPassword !== formState.checkPassword) {
    message.warning('两次输入的密码不一致')
    return
  }

  if (formState.userPassword.length < 8) {
    message.warning('密码长度不能少于8位')
    return
  }

  loading.value = true
  try {
    const res = await register({
      userAccount: formState.userAccount,
      userPassword: formState.userPassword,
      checkPassword: formState.checkPassword
    })
    if (res.data.code === 0) {
      message.success('注册成功，请登录')
      setTimeout(() => {
        router.push('/user/login')
      }, 1500)
    } else {
      message.error(res.data.message || '注册失败')
    }
  } catch (error) {
    message.error('注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/user/login')
}
</script>

<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h1>用户注册</h1>
        <p>欢迎加入AI零代码应用生成平台</p>
      </div>

      <a-form
        :model="formState"
        layout="vertical"
        @submit.prevent="handleRegister"
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
            placeholder="请输入密码（至少8位）"
            size="large"
          />
        </a-form-item>

        <a-form-item label="确认密码">
          <a-input-password
            v-model:value="formState.checkPassword"
            placeholder="请再次输入密码"
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
            注册
          </a-button>
        </a-form-item>
      </a-form>

      <div class="register-footer">
        <span>已有账号？</span>
        <a @click="goToLogin">立即登录</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f3f4f6;
  padding: 24px 16px;
}

.register-box {
  width: 400px;
  padding: 24px 24px 20px;
  background: #ffffff;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.register-header {
  margin-bottom: 24px;
}

.register-header h1 {
  font-size: 20px;
  margin-bottom: 8px;
  color: #111827;
  font-weight: 600;
}

.register-header p {
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

.register-footer {
  text-align: center;
  margin-top: 16px;
  font-size: 13px;
  color: #6b7280;
}

.register-footer a {
  color: #2563eb;
  font-weight: 500;
  cursor: pointer;
}
</style>
