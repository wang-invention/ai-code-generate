<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/LoginUser'
import { updateUser } from '@/api/userController'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const formRef = ref()
const loading = ref(false)
const avatarLoading = ref(false)
const previewVisible = ref(false)
const previewImage = ref('')

const formState = reactive({
  id: undefined as number | undefined,
  userName: '',
  userAvatar: '',
  userProfile: '',
  phone: '',
  email: ''
})

const fileList = ref<any[]>([])

const rules = {
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入正确的手机号码',
      trigger: 'blur'
    }
  ],
  email: [
    {
      pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
      message: '请输入正确的邮箱地址',
      trigger: 'blur'
    }
  ]
}

const initForm = () => {
  const loginUser = loginUserStore.loginUser
  formState.id = loginUser.id
  formState.userName = loginUser.userName || ''
  formState.userAvatar = loginUser.userAvatar || ''
  formState.phone = loginUser.phone || ''
  formState.email = loginUser.email || ''

  if (formState.userAvatar) {
    fileList.value = [
      {
        uid: '-1',
        name: 'avatar.png',
        status: 'done',
        url: formState.userAvatar
      }
    ]
  }
}

const handleAvatarChange = ({ fileList: newFileList }: any) => {
  fileList.value = newFileList
  if (newFileList.length > 0 && newFileList[0].status === 'done') {
    formState.userAvatar = newFileList[0].url || newFileList[0].response?.data
  }
}

const beforeUpload = (file: File) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isJpgOrPng) {
    message.error('只能上传 JPG/PNG 格式的图片!')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    message.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handlePreview = async (file: any) => {
  if (!file.url && !file.preview) {
    file.preview = await getBase64(file.originFileObj)
  }
  previewImage.value = file.url || file.preview
  previewVisible.value = true
}

const getBase64 = (file: File): Promise<string> => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = () => resolve(reader.result as string)
    reader.onerror = error => reject(error)
  })
}

const handleCancel = () => {
  router.push('/user/profile')
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    loading.value = true

    const res = await updateUser({
      id: formState.id,
      userName: formState.userName,
      userAvatar: formState.userAvatar,
      userProfile: formState.userProfile
    })

    if (res.data.code === 0) {
      message.success('保存成功')

      const updatedUser = {
        ...loginUserStore.loginUser,
        userName: formState.userName,
        userAvatar: formState.userAvatar,
        userProfile: formState.userProfile
      }
      loginUserStore.setLoginUser(updatedUser)

      setTimeout(() => {
        router.push('/user/profile')
      }, 1000)
    } else {
      message.error(res.data.message || '保存失败')
    }
  } catch (error: any) {
    if (error.errorFields) {
      message.warning('请检查表单填写是否正确')
    } else {
      message.error('保存失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  initForm()
})
</script>

<template>
  <div class="edit-profile-container">
    <a-page-header
      title="编辑资料"
      sub-title="修改您的个人信息"
      @back="handleCancel"
      class="page-header"
    />

    <div class="edit-profile-content">
      <a-card title="基本信息" :bordered="false" class="form-card">
        <a-form
          ref="formRef"
          :model="formState"
          :rules="rules"
          :label-col="{ xs: 24, sm: 6, md: 6, lg: 5, xl: 4 }"
          :wrapper-col="{ xs: 24, sm: 18, md: 18, lg: 19, xl: 20 }"
          layout="horizontal"
        >
          <a-form-item label="用户头像" name="userAvatar">
            <a-upload
              v-model:file-list="fileList"
              list-type="picture-card"
              :max-count="1"
              :before-upload="beforeUpload"
              @preview="handlePreview"
              @change="handleAvatarChange"
            >
              <div v-if="fileList.length === 0">
                <plus-outlined />
                <div style="margin-top: 8px">上传头像</div>
              </div>
            </a-upload>
            <div class="upload-tip">
              支持 JPG、PNG 格式，文件大小不超过 2MB
            </div>
          </a-form-item>

          <a-form-item label="用户名" name="userName">
            <a-input
              v-model:value="formState.userName"
              placeholder="请输入用户名"
              :max-length="20"
              show-count
            />
          </a-form-item>

          <a-form-item label="手机号" name="phone">
            <a-input
              v-model:value="formState.phone"
              placeholder="请输入手机号"
              :max-length="11"
            />
          </a-form-item>

          <a-form-item label="邮箱" name="email">
            <a-input
              v-model:value="formState.email"
              placeholder="请输入邮箱地址"
            />
          </a-form-item>

          <a-form-item :wrapper-col="{ offset: 0, span: 24 }">
            <a-space>
              <a-button type="primary" :loading="loading" @click="handleSubmit">
                保存
              </a-button>
              <a-button @click="handleCancel">
                取消
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </a-card>
    </div>

    <a-modal :visible="previewVisible" :footer="null" @cancel="previewVisible = false">
      <img alt="preview" style="width: 100%" :src="previewImage" />
    </a-modal>
  </div>
</template>

<style scoped>
.edit-profile-container {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 64px);
}

.page-header {
  background-color: #fff;
  padding: 16px 24px;
  margin-bottom: 24px;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.edit-profile-content {
  max-width: 900px;
  margin: 0 auto;
}

.form-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

:deep(.ant-form-item-label > label) {
  font-weight: 500;
  color: #262626;
}

:deep(.ant-input) {
  border-radius: 6px;
  transition: all 0.3s;
}

:deep(.ant-input:hover) {
  border-color: #4096ff;
}

:deep(.ant-input:focus) {
  border-color: #4096ff;
  box-shadow: 0 0 0 2px rgba(64, 150, 255, 0.1);
}

:deep(.ant-upload-select-picture-card) {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  transition: all 0.3s;
}

:deep(.ant-upload-select-picture-card:hover) {
  border-color: #4096ff;
}

:deep(.ant-upload-list-picture-card-container) {
  width: 120px;
  height: 120px;
  border-radius: 8px;
}

.upload-tip {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 8px;
}

:deep(.ant-btn-primary) {
  height: 40px;
  padding: 0 32px;
  font-size: 15px;
  border-radius: 6px;
}

:deep(.ant-btn) {
  height: 40px;
  padding: 0 24px;
  font-size: 15px;
  border-radius: 6px;
}

@media (max-width: 768px) {
  .edit-profile-container {
    padding: 16px;
  }

  :deep(.ant-form) {
    :deep(.ant-form-item) {
      :deep(.ant-col-6) {
        flex: 0 0 100%;
        max-width: 100%;
      }
      :deep(.ant-col-18) {
        flex: 0 0 100%;
        max-width: 100%;
      }
    }
  }
}
</style>
