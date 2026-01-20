<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { menuItems } from '../router'
import { useLoginUserStore } from '@/stores/LoginUser'
import type { MenuProps } from 'ant-design-vue'

const loginUserStore = useLoginUserStore()

const router = useRouter()
const route = useRoute()
const isReady = ref(false)

router.isReady().then(() => {
  isReady.value = true
})

const filteredMenuItems = computed(() => {
  if (!menuItems) return []
  
  return menuItems.filter(item => {
    if (item.key === 'app-manage') {
      return loginUserStore.loginUser.userRole === 'admin'
    }
    return true
  })
})

const selectedKeys = computed(() => {
  if (!isReady.value) return []
  const menuItem = filteredMenuItems.value?.find(item => 'path' in item && item.path === route.path)
  return menuItem ? [menuItem.key as string] : []
})

const handleMenuClick = ({ key }: { key: string }) => {
  const menuItem = filteredMenuItems.value?.find(item => item?.key === key)
  if (menuItem && 'path' in menuItem && menuItem.path) {
    router.push(menuItem.path as string)
  }
}

const handleLogout = async () => {
  loginUserStore.clearLoginUser()
  await router.push('/user/login')
}

const userMenuItems: MenuProps['items'] = [
  {
    key: 'user-info',
    label: '个人信息',
    icon: '👤',
  },
  {
    key: 'settings',
    label: '设置',
    icon: '⚙️',
  },
  {
    type: 'divider',
  },
  {
    key: 'logout',
    label: '退出登录',
    icon: '🚪',
    danger: true,
  },
]

const handleUserMenuClick: MenuProps['onClick'] = ({ key }) => {
  if (key === 'logout') {
    handleLogout()
  } else if (key === 'user-info') {
    router.push('/user/profile')
  } else if (key === 'settings') {
    router.push('/user/settings')
  }
}
</script>

<template>
  <div class="header">
    <div class="header-content">
      <div class="header-left">
        <div class="logo">
          <img src="/favicon.ico" alt="Logo" class="logo-img" />
          <span class="title">AI零代码应用生成</span>
        </div>
        <a-menu
          v-if="isReady"
          mode="horizontal"
          :items="filteredMenuItems"
          :selected-keys="selectedKeys"
          class="header-menu"
          @select="handleMenuClick"
        />
      </div>
      <div class="header-right">
        <div v-if="loginUserStore.loginUser.id" class="user-section">
          <a-dropdown :trigger="['click']" placement="bottomRight">
            <a-avatar 
              :src="loginUserStore.loginUser?.userAvatar" 
              :size="40"
              class="user-avatar"
            >
              {{ loginUserStore.loginUser?.userName?.charAt(0) ?? 'U' }}
            </a-avatar>
            <template #overlay>
              <a-menu :items="userMenuItems" @click="handleUserMenuClick" />
            </template>
          </a-dropdown>
        </div>
        <div v-else>
          <a-button type="primary" @click="router.push('/user/login')">登录</a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.header {
  background-color: #fff;
  border-bottom: 1px solid #f0f0f0;
  height: 64px;
  line-height: 64px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  margin-right: 40px;
}

.logo-img {
  width: 32px;
  height: 32px;
  margin-right: 8px;
}

.title {
  font-size: 20px;
  font-weight: bold;
  background: #1890ff;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-menu {
  border-bottom: none;
  background: transparent;
}

.user-section {
  display: flex;
  align-items: center;
}

.user-avatar {
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
  background: #1890ff;
  color: #fff;
  font-weight: 600;
  font-size: 16px;
}

.user-avatar:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.user-avatar:active {
  transform: scale(0.95);
}

:deep(.ant-dropdown-menu) {
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  padding: 8px 0;
  min-width: 160px;
}

:deep(.ant-dropdown-menu-item) {
  padding: 10px 16px;
  margin: 4px 8px;
  border-radius: 8px;
  transition: all 0.2s;
}

:deep(.ant-dropdown-menu-item:hover) {
  background-color: #f5f5f5;
}

:deep(.ant-dropdown-menu-item-danger) {
  color: #ff4d4f;
}

:deep(.ant-dropdown-menu-item-danger:hover) {
  background-color: #fff1f0;
}
</style>
