<script setup lang="ts">
import { reactive, ref, onMounted, computed } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  EditOutlined,
  DeleteOutlined,
  StarOutlined,
  StarFilled,
  FilterOutlined,
  DownOutlined
} from '@ant-design/icons-vue'
import {
  listAppVoByPageByAdmin,
  deleteAppByAdmin,
  updateAppByAdmin,
  getAppVoByIdByAdmin
} from '@/api/appController'
import { useLoginUserStore } from '@/stores/LoginUser'

const loginUserStore = useLoginUserStore()

const loading = ref(false)
const tableData = ref<API.AppVO[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const selectedRowKeys = ref<Array<string | number>>([])
const selectedRows = ref<API.AppVO[]>([])

const searchForm = reactive({
  appName: '',
  codeGenType: '',
  userId: undefined as number | undefined,
  priority: undefined as number | undefined
})

const modalVisible = ref(false)
const modalTitle = ref('编辑应用')
const modalLoading = ref(false)

const appForm = reactive({
  id: undefined as number | undefined,
  appName: '',
  cover: '',
  priority: 0
})

const codeGenTypeOptions = [
  { label: 'HTML 单文件', value: 'HTML' },
  { label: '多文件项目', value: 'MULTI_FILE' }
]

const priorityOptions = [
  { label: '全部', value: undefined },
  { label: '精选', value: 99 },
  { label: '普通', value: 0 }
]

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80
  },
  {
    title: '应用名称',
    dataIndex: 'appName',
    key: 'appName'
  },
  {
    title: '应用封面',
    dataIndex: 'cover',
    key: 'cover',
    width: 100
  },
  {
    title: '初始提示词',
    dataIndex: 'initPrompt',
    key: 'initPrompt',
    ellipsis: true,
    width: 200
  },
  {
    title: '代码生成类型',
    dataIndex: 'codeGenType',
    key: 'codeGenType',
    width: 120
  },
  {
    title: '优先级',
    dataIndex: 'priority',
    key: 'priority',
    width: 80,
    filters: [
      { text: '精选', value: 99 },
      { text: '普通', value: 0 }
    ]
  },
  {
    title: '创建用户',
    dataIndex: ['user', 'userName'],
    key: 'userId',
    width: 100
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180,
    sorter: true
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right'
  }
]

const rowSelection = computed(() => {
  return {
    selectedRowKeys: selectedRowKeys.value,
    onChange: (selectedKeys: Array<string | number>, rows: API.AppVO[]) => {
      selectedRowKeys.value = selectedKeys
      selectedRows.value = rows
    }
  }
})

const hasSelection = computed(() => {
  return selectedRowKeys.value.length > 0
})

const fetchAppList = async () => {
  loading.value = true
  try {
    const res = await listAppVoByPageByAdmin({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      appName: searchForm.appName || undefined,
      codeGenType: searchForm.codeGenType || undefined,
      userId: searchForm.userId || undefined,
      priority: searchForm.priority || undefined
    })
    if (res.data.code === 0 && res.data.data) {
      tableData.value = res.data.data.records || []
      total.value = res.data.data.totalRow || 0
    } else {
      message.error(res.data.message || '获取应用列表失败')
    }
  } catch (error) {
    message.error('获取应用列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchAppList()
}

const handleReset = () => {
  searchForm.appName = ''
  searchForm.codeGenType = ''
  searchForm.userId = undefined
  searchForm.priority = undefined
  currentPage.value = 1
  fetchAppList()
}

const handlePageChange = (page: number, size: number) => {
  currentPage.value = page
  pageSize.value = size
  fetchAppList()
}

const handleTableChange = (pagination: any, filters: any, sorter: any) => {
  if (filters.priority) {
    searchForm.priority = filters.priority[0]
  } else {
    searchForm.priority = undefined
  }
  fetchAppList()
}

const openEditModal = async (record: API.AppVO) => {
  modalTitle.value = '编辑应用'
  modalLoading.value = true
  modalVisible.value = true

  try {
    const res = await getAppVoByIdByAdmin({ id: record.id })
    if (res.data.code === 0 && res.data.data) {
      const appData = res.data.data
      appForm.id = appData.id != null ? appData.id : undefined
      appForm.appName = appData.appName || ''
      appForm.cover = appData.cover || ''
      appForm.priority = appData.priority || 0
    } else {
      message.error(res.data.message || '获取应用信息失败')
      modalVisible.value = false
    }
  } catch (error) {
    message.error('获取应用信息失败，请稍后重试')
    modalVisible.value = false
  } finally {
    modalLoading.value = false
  }
}

const resetAppForm = () => {
  appForm.id = undefined
  appForm.appName = ''
  appForm.cover = ''
  appForm.priority = 0
}

const handleModalOk = async () => {
  if (!appForm.appName) {
    message.warning('请填写应用名称')
    return
  }

  modalLoading.value = true
  try {
    const res = await updateAppByAdmin({
      id: appForm.id,
      appName: appForm.appName,
      cover: appForm.cover,
      priority: appForm.priority
    })

    if (res.data.code === 0) {
      message.success('更新成功')
      modalVisible.value = false
      fetchAppList()
    } else {
      message.error(res.data.message || '更新失败')
    }
  } catch (error) {
    message.error('更新失败，请稍后重试')
  } finally {
    modalLoading.value = false
  }
}

const handleModalCancel = () => {
  modalVisible.value = false
  resetAppForm()
}

const handleDelete = (record: API.AppVO) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除应用 "${record.appName}" 吗？此操作不可恢复。`,
    okText: '确定',
    cancelText: '取消',
    okButtonProps: { danger: true },
    onOk: async () => {
      try {
        const res = await deleteAppByAdmin({ id: record.id })
        if (res.data.code === 0) {
          message.success('删除成功')
          if (tableData.value.length === 1 && currentPage.value > 1) {
            currentPage.value -= 1
          }
          fetchAppList()
        } else {
          message.error(res.data.message || '删除失败')
        }
      } catch (error) {
        message.error('删除失败，请稍后重试')
      }
    }
  })
}

const handleSetFeatured = async (record: API.AppVO) => {
  Modal.confirm({
    title: '确认设为精选',
    content: `确定要将应用 "${record.appName}" 设为精选吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await updateAppByAdmin({
          id: record.id,
          appName: record.appName,
          cover: record.cover,
          priority: 99
        })

        if (res.data.code === 0) {
          message.success('设置成功')
          fetchAppList()
        } else {
          message.error(res.data.message || '设置失败')
        }
      } catch (error) {
        message.error('设置失败，请稍后重试')
      }
    }
  })
}

const handleRemoveFeatured = async (record: API.AppVO) => {
  Modal.confirm({
    title: '确认取消精选',
    content: `确定要取消应用 "${record.appName}" 的精选状态吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await updateAppByAdmin({
          id: record.id,
          appName: record.appName,
          cover: record.cover,
          priority: 0
        })

        if (res.data.code === 0) {
          message.success('取消成功')
          fetchAppList()
        } else {
          message.error(res.data.message || '取消失败')
        }
      } catch (error) {
        message.error('取消失败，请稍后重试')
      }
    }
  })
}

const handleBatchDelete = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要删除的应用')
    return
  }

  Modal.confirm({
    title: '批量删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 个应用吗？此操作不可恢复。`,
    okText: '确定',
    cancelText: '取消',
    okButtonProps: { danger: true },
    onOk: async () => {
      try {
        const promises = selectedRows.value.map((row: API.AppVO) =>
          deleteAppByAdmin({ id: row.id })
        )
        await Promise.all(promises)
        message.success(`成功删除 ${selectedRowKeys.value.length} 个应用`)
        selectedRowKeys.value = []
        selectedRows.value = []
        fetchAppList()
      } catch (error) {
        message.error('批量删除失败，请稍后重试')
      }
    }
  })
}

const handleBatchSetFeatured = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要设为精选的应用')
    return
  }

  Modal.confirm({
    title: '批量设为精选',
    content: `确定要将选中的 ${selectedRowKeys.value.length} 个应用设为精选吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        const promises = selectedRows.value.map((row: API.AppVO) =>
          updateAppByAdmin({
            id: row.id,
            appName: row.appName,
            cover: row.cover,
            priority: 99
          })
        )
        await Promise.all(promises)
        message.success(`成功将 ${selectedRowKeys.value.length} 个应用设为精选`)
        selectedRowKeys.value = []
        selectedRows.value = []
        fetchAppList()
      } catch (error) {
        message.error('批量设置失败，请稍后重试')
      }
    }
  })
}

const handleBatchRemoveFeatured = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要取消精选的应用')
    return
  }

  Modal.confirm({
    title: '批量取消精选',
    content: `确定要取消选中的 ${selectedRowKeys.value.length} 个应用的精选状态吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        const promises = selectedRows.value.map((row: API.AppVO) =>
          updateAppByAdmin({
            id: row.id,
            appName: row.appName,
            cover: row.cover,
            priority: 0
          })
        )
        await Promise.all(promises)
        message.success(`成功取消 ${selectedRowKeys.value.length} 个应用的精选状态`)
        selectedRowKeys.value = []
        selectedRows.value = []
        fetchAppList()
      } catch (error) {
        message.error('批量取消失败，请稍后重试')
      }
    }
  })
}

onMounted(() => {
  fetchAppList()
})
</script>

<template>
  <div class="app-manage-container">
    <a-card :bordered="false" class="manage-card">
      <template #title>
        <div class="card-title">
          <span>应用管理</span>
          <a-tag color="blue">{{ total }}</a-tag>
        </div>
      </template>

      <div class="search-form">
        <a-form layout="inline">
          <a-form-item label="应用名称">
            <a-input
              v-model:value="searchForm.appName"
              placeholder="请输入应用名称"
              allow-clear
              style="width: 200px"
            >
              <template #prefix>
                <SearchOutlined />
              </template>
            </a-input>
          </a-form-item>
          <a-form-item label="代码类型">
            <a-select
              v-model:value="searchForm.codeGenType"
              placeholder="请选择代码类型"
              allow-clear
              style="width: 150px"
            >
              <a-select-option
                v-for="option in codeGenTypeOptions"
                :key="option.value"
                :value="option.value"
              >
                {{ option.label }}
              </a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="优先级">
            <a-select
              v-model:value="searchForm.priority"
              placeholder="请选择优先级"
              style="width: 120px"
            >
              <a-select-option
                v-for="option in priorityOptions"
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

      <div v-if="hasSelection" class="batch-actions">
        <a-space>
          <a-button type="primary" @click="handleBatchSetFeatured">
            <template #icon>
              <StarOutlined />
            </template>
            批量设为精选
          </a-button>
          <a-button @click="handleBatchRemoveFeatured">
            <template #icon>
              <StarFilled />
            </template>
            批量取消精选
          </a-button>
          <a-button danger @click="handleBatchDelete">
            <template #icon>
              <DeleteOutlined />
            </template>
            批量删除
          </a-button>
        </a-space>
      </div>

      <a-table
        :columns="columns"
        :data-source="loading ? [] : tableData"
        :loading="loading"
        :pagination="{
          current: currentPage,
          pageSize: pageSize,
          total: total,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total: number) => `共 ${total} 条`,
          onChange: handlePageChange
        }"
        :row-selection="loading ? null : rowSelection"
        :scroll="{ x: 1400 }"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'cover'">
            <a-avatar
              v-if="record.cover"
              :src="record.cover"
              :size="40"
              shape="square"
            />
            <a-avatar v-else :size="40" shape="square">
              {{ record.appName?.charAt(0) || 'A' }}
            </a-avatar>
          </template>
          <template v-else-if="column.key === 'priority'">
            <a-tag v-if="record.priority === 99" color="gold">
              <StarFilled />
              精选
            </a-tag>
            <a-tag v-else color="default">
              {{ record.priority }}
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
              <a-button
                v-if="record.priority !== 99"
                type="link"
                size="small"
                @click="handleSetFeatured(record)"
              >
                <template #icon>
                  <StarOutlined />
                </template>
                精选
              </a-button>
              <a-button
                v-else
                type="link"
                size="small"
                @click="handleRemoveFeatured(record)"
              >
                <template #icon>
                  <StarFilled />
                </template>
                取消精选
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
        <template #emptyText>
          <a-empty description="暂无应用数据">
            <a-button type="primary" @click="handleReset">重置筛选</a-button>
          </a-empty>
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
      <a-form :model="appForm" layout="vertical">
        <a-form-item label="应用名称" required>
          <a-input v-model:value="appForm.appName" placeholder="请输入应用名称" />
        </a-form-item>
        <a-form-item label="应用封面">
          <a-input v-model:value="appForm.cover" placeholder="请输入封面URL" />
        </a-form-item>
        <a-form-item label="优先级">
          <a-input-number
            v-model:value="appForm.priority"
            :min="0"
            :max="99"
            style="width: 100%"
            placeholder="请输入优先级（0-99，99为精选）"
          />
          <div style="margin-top: 8px; color: #999; font-size: 12px;">
            优先级为 99 的应用将显示在精选列表中
          </div>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
.app-manage-container {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100vh;
}

.manage-card {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border-radius: 16px;
  animation: fadeIn 0.6s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.card-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.search-form {
  margin-bottom: 16px;
  padding: 20px;
  background: #fafafa;
  border-radius: 12px;
}

.batch-actions {
  margin-bottom: 16px;
  padding: 12px 20px;
  background: #1890ff;
  border-radius: 12px;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

:deep(.ant-card-head-title) {
  padding: 20px 0;
}

:deep(.ant-table) {
  font-size: 14px;
  border-radius: 8px;
  overflow: hidden;
}

:deep(.ant-table-thead > tr > th) {
  background: #fafafa;
  font-weight: 600;
  color: #333;
}

:deep(.ant-table-tbody > tr:hover > td) {
  background: #f5f5f5;
}

:deep(.ant-table-row-selected) {
  background: #e6f7ff !important;
}

:deep(.ant-btn-link) {
  padding: 0 4px;
}

:deep(.ant-input),
:deep(.ant-select-selector) {
  border-radius: 8px;
}

:deep(.ant-btn) {
  border-radius: 8px;
  transition: all 0.3s;
}

:deep(.ant-btn:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

:deep(.ant-tag) {
  border-radius: 12px;
  font-weight: 500;
}
</style>
