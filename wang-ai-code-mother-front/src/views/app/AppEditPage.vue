<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { ArrowLeftOutlined } from '@ant-design/icons-vue'
import { getAppVoById, updateApp } from '@/api/appController'
import { useLoginUserStore } from '@/stores/LoginUser'
import type { API } from '@/api/typings.d'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const appId = ref<string>(String(route.params.id))
const loading = ref(false)
const saving = ref(false)

const appForm = reactive({
  id: undefined as string | undefined,
  appName: '',
  cover: ''
})

const isAdmin = ref(false)

const fetchAppInfo = async () => {
  loading.value = true
  try {
    const res = await getAppVoById({ id: appId.value })
    if (res.data.code === 0 && res.data.data) {
      const appData = res.data.data

      if (appData.userId !== loginUserStore.loginUser.id && loginUserStore.loginUser.userRole !== 'admin') {
        message.error('您没有权限编辑此应用')
        router.push('/')
        return
      }

      appForm.id = appData.id
      appForm.appName = appData.appName || ''
      appForm.cover = appData.cover || ''

      isAdmin.value = loginUserStore.loginUser.userRole === 'admin'
    } else {
      message.error(res.data.message || '获取应用信息失败')
    }
  } catch (error) {
    message.error('获取应用信息失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  if (!appForm.appName) {
    message.warning('请填写应用名称')
    return
  }

  saving.value = true
  try {
    const res = await updateApp({
      id: appForm.id,
      appName: appForm.appName
    })

    if (res.data.code === 0) {
      message.success('保存成功')
      router.push('/app/chat/' + appId.value)
    } else {
      message.error(res.data.message || '保存失败')
    }
  } catch (error) {
    message.error('保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

const goBack = () => {
  router.push('/app/chat/' + appId.value)
}

onMounted(() => {
  fetchAppInfo()
})
</script>

<template>
  <div class="app-edit-container">
    <a-spin :spinning="loading">
      <a-card :bordered="false" class="edit-card">
        <template #title>
          <div class="card-header">
            <a-button type="text" @click="goBack">
              <template #icon>
                <ArrowLeftOutlined />
              </template>
            </a-button>
            <h2 class="card-title">编辑应用信息</h2>
          </div>
        </template>

        <a-form :model="appForm" layout="vertical" class="edit-form">
          <a-form-item label="应用名称" required>
            <a-input
              v-model:value="appForm.appName"
              placeholder="请输入应用名称"
              size="large"
            />
          </a-form-item>

          <a-form-item label="应用封面">
            <a-input
              v-model:value="appForm.cover"
              placeholder="请输入应用封面URL"
              size="large"
            />
            <div style="margin-top: 8px; color: #999; font-size: 12px;">
              暂不支持修改封面，此功能仅作展示
            </div>
          </a-form-item>

          <a-form-item>
            <a-space>
              <a-button type="primary" size="large" :loading="saving" @click="handleSave">
                保存
              </a-button>
              <a-button size="large" @click="goBack">
                取消
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </a-card>
    </a-spin>
  </div>
</template>

<style scoped>
.app-edit-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
}

.edit-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.edit-form {
  padding: 24px 0;
}

:deep(.ant-form-item-label > label) {
  font-weight: 500;
  color: #333;
}

:deep(.ant-input) {
  border-radius: 4px;
}

:deep(.ant-btn) {
  border-radius: 4px;
  min-width: 100px;
}
</style>
