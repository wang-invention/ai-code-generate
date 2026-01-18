<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'
import {
  listUserVoByPage,
  addUser,
  updateUser,
  deleteUser,
  getUserVoById
} from '@/api/userController'
import type { API } from '@/api/typings.d'

const loading = ref(false)
const tableData = ref<API.UserVO[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const searchForm = reactive({
  userName: '',
  userAccount: '',
  userRole: ''
})

const modalVisible = ref(false)
const modalTitle = ref('新增用户')
const modalLoading = ref(false)
const isEdit = ref(false)

const userForm = reactive({
  id: undefined as number | undefined,
  userName: '',
  userAccount: '',
  userAvatar: '',
  userProfile: '',
  userRole: 'user'
})

const userRoleOptions = [
  { label: '普通用户', value: 'user' },
  { label: '管理员', value: 'admin' }
]

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80
  },
  {
    title: '用户账号',
    dataIndex: 'userAccount',
    key: 'userAccount'
  },
  {
    title: '用户名',
    dataIndex: 'userName',
    key: 'userName'
  },
  {
    title: '用户头像',
    dataIndex: 'userAvatar',
    key: 'userAvatar',
    width: 100
  },
  {
    title: '用户简介',
    dataIndex: 'userProfile',
    key: 'userProfile',
    ellipsis: true
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
    key: 'userRole',
    width: 100
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    width: 180,
    fixed: 'right'
  }
]

const fetchUserList = async () => {
  loading.value = true
  try {
    const res = await listUserVoByPage({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      userName: searchForm.userName || undefined,
      userAccount: searchForm.userAccount || undefined,
      userRole: searchForm.userRole || undefined
    })
    if (res.data.code === 0 && res.data.data) {
      tableData.value = res.data.data.records || []
      total.value = res.data.data.totalRow || 0
    } else {
      message.error(res.data.message || '获取用户列表失败')
    }
  } catch (error) {
    message.error('获取用户列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchUserList()
}

const handleReset = () => {
  searchForm.userName = ''
  searchForm.userAccount = ''
  searchForm.userRole = ''
  currentPage.value = 1
  fetchUserList()
}

const handlePageChange = (page: number, size: number) => {
  currentPage.value = page
  pageSize.value = size
  fetchUserList()
}

const openAddModal = () => {
  modalTitle.value = '新增用户'
  isEdit.value = false
  resetUserForm()
  modalVisible.value = true
}

const openEditModal = async (record: API.UserVO) => {
  modalTitle.value = '编辑用户'
  isEdit.value = true
  modalLoading.value = true
  modalVisible.value = true

  try {
    const res = await getUserVoById({ id: record.id! })
    if (res.data.code === 0 && res.data.data) {
      const userData = res.data.data
      userForm.id = userData.id
      userForm.userName = userData.userName || ''
      userForm.userAccount = userData.userAccount || ''
      userForm.userAvatar = userData.userAvatar || ''
      userForm.userProfile = userData.userProfile || ''
      userForm.userRole = userData.userRole || 'user'
    } else {
      message.error(res.data.message || '获取用户信息失败')
      modalVisible.value = false
    }
  } catch (error) {
    message.error('获取用户信息失败，请稍后重试')
    modalVisible.value = false
  } finally {
    modalLoading.value = false
  }
}

const resetUserForm = () => {
  userForm.id = undefined
  userForm.userName = ''
  userForm.userAccount = ''
  userForm.userAvatar = ''
  userForm.userProfile = ''
  userForm.userRole = 'user'
}

const handleModalOk = async () => {
  if (!userForm.userName || !userForm.userAccount) {
    message.warning('请填写用户名和账号')
    return
  }

  modalLoading.value = true
  try {
    let res
    if (isEdit.value) {
      res = await updateUser({
        id: userForm.id,
        userName: userForm.userName,
        userAvatar: userForm.userAvatar,
        userProfile: userForm.userProfile,
        userRole: userForm.userRole
      })
    } else {
      res = await addUser({
        userName: userForm.userName,
        userAccount: userForm.userAccount,
        userAvatar: userForm.userAvatar,
        userProfile: userForm.userProfile,
        userRole: userForm.userRole
      })
    }

    if (res.data.code === 0) {
      message.success(isEdit.value ? '更新成功' : '添加成功')
      modalVisible.value = false
      fetchUserList()
    } else {
      message.error(res.data.message || (isEdit.value ? '更新失败' : '添加失败'))
    }
  } catch (error) {
    message.error(isEdit.value ? '更新失败，请稍后重试' : '添加失败，请稍后重试')
  } finally {
    modalLoading.value = false
  }
}

const handleModalCancel = () => {
  modalVisible.value = false
  resetUserForm()
}

const handleDelete = (record: API.UserVO) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除用户 "${record.userName}" 吗？此操作不可恢复。`,
    okText: '确定',
    cancelText: '取消',
    okButtonProps: { danger: true },
    onOk: async () => {
      try {
        const res = await deleteUser({ id: record.id })
        if (res.data.code === 0) {
          message.success('删除成功')
          if (tableData.value.length === 1 && currentPage.value > 1) {
            currentPage.value -= 1
          }
          fetchUserList()
        } else {
          message.error(res.data.message || '删除失败')
        }
      } catch (error) {
        message.error('删除失败，请稍后重试')
      }
    }
  })
}

onMounted(() => {
  fetchUserList()
})
</script>

<template>
  <div class="user-manage-container">
    <a-card :bordered="false">
      <template #title>
        <div class="card-title">
          <span>用户管理</span>
        </div>
      </template>

      <div class="search-form">
        <a-form layout="inline">
          <a-form-item label="用户名">
            <a-input
              v-model:value="searchForm.userName"
              placeholder="请输入用户名"
              allow-clear
              style="width: 200px"
            />
          </a-form-item>
          <a-form-item label="用户账号">
            <a-input
              v-model:value="searchForm.userAccount"
              placeholder="请输入用户账号"
              allow-clear
              style="width: 200px"
            />
          </a-form-item>
          <a-form-item label="用户角色">
            <a-select
              v-model:value="searchForm.userRole"
              placeholder="请选择用户角色"
              allow-clear
              style="width: 150px"
            >
              <a-select-option
                v-for="option in userRoleOptions"
                :key="option.value"
                :value="option.value"
              >
                {{ option.label }}
              </a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item>
            <a-space>
              <a-button type="primary" @click="handleSearch">
                <template #icon>
                  <SearchOutlined />
                </template>
                搜索
              </a-button>
              <a-button @click="handleReset">
                <template #icon>
                  <ReloadOutlined />
                </template>
                重置
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </div>

      <div class="table-toolbar">
        <a-button type="primary" @click="openAddModal">
          <template #icon>
            <PlusOutlined />
          </template>
          新增用户
        </a-button>
      </div>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="{
          current: currentPage,
          pageSize: pageSize,
          total: total,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total) => `共 ${total} 条`,
          onChange: handlePageChange
        }"
        :scroll="{ x: 1200 }"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'userAvatar'">
            <a-avatar
              v-if="record.userAvatar"
              :src="record.userAvatar"
              :size="40"
            />
            <a-avatar v-else :size="40">
              {{ record.userName?.charAt(0) || 'U' }}
            </a-avatar>
          </template>
          <template v-else-if="column.key === 'userRole'">
            <a-tag :color="record.userRole === 'admin' ? 'red' : 'blue'">
              {{ record.userRole === 'admin' ? '管理员' : '普通用户' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="openEditModal(record)">
                <template #icon>
                  <EditOutlined />
                </template>
                编辑
              </a-button>
              <a-button type="link" size="small" danger @click="handleDelete(record)">
                <template #icon>
                  <DeleteOutlined />
                </template>
                删除
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      :confirm-loading="modalLoading"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
      width="600px"
    >
      <a-form :model="userForm" layout="vertical">
        <a-form-item label="用户账号" required>
          <a-input
            v-model:value="userForm.userAccount"
            placeholder="请输入用户账号"
            :disabled="isEdit"
          />
        </a-form-item>
        <a-form-item label="用户名" required>
          <a-input v-model:value="userForm.userName" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item label="用户头像">
          <a-input v-model:value="userForm.userAvatar" placeholder="请输入头像URL" />
        </a-form-item>
        <a-form-item label="用户简介">
          <a-textarea
            v-model:value="userForm.userProfile"
            placeholder="请输入用户简介"
            :rows="4"
          />
        </a-form-item>
        <a-form-item label="用户角色">
          <a-select v-model:value="userForm.userRole" placeholder="请选择用户角色">
            <a-select-option
              v-for="option in userRoleOptions"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
.user-manage-container {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100vh;
}

.card-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.search-form {
  margin-bottom: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 4px;
}

.table-toolbar {
  margin-bottom: 16px;
}

:deep(.ant-card-head-title) {
  padding: 16px 0;
}

:deep(.ant-table) {
  font-size: 14px;
}

:deep(.ant-btn-link) {
  padding: 0 4px;
}
</style>