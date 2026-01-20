<script setup lang="ts">
import { computed, ref, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { menuItems } from '../router'
import { useLoginUserStore } from '@/stores/LoginUser'
import { UserOutlined, SettingOutlined, LogoutOutlined } from '@ant-design/icons-vue'
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
    icon: () => h(UserOutlined),
  },
  {
    key: 'settings',
    label: '设置',
    icon: () => h(SettingOutlined),
  },
  {
    type: 'divider',
  },
  {
    key: 'logout',
    label: '退出登录',
    icon: () => h(LogoutOutlined),
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
          <a-button type="primary" class="login-button">登录</a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.header {
  background-color: #fff;
  border-bottom: 1px solid #e8e8e8;
  height: 64px;
  line-height: 64px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 40px;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  margin-right: 48px;
  cursor: pointer;
}

.logo-img {
  width: 32px;
  height: 32px;
  margin-right: 10px;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.header-menu {
  border-bottom: none;
  background: transparent;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.user-section {
  display: flex;
  align-items: center;
}

.user-avatar {
  cursor: pointer;
  transition: all 0.2s;
  border: 2px solid #e8e8e8;
  background: #f5f5f5;
  color: #666;
  font-weight: 500;
  font-size: 16px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.user-avatar:hover {
  border-color: #1890ff;
  background: #1890ff;
  color: #fff;
  transform: scale(1.05);
}

.login-button {
  height: 40px;
  padding: 0 24px;
  border-radius: 8px;
  font-weight: 500;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

:deep(.ant-dropdown-menu) {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  padding: 4px 0;
  min-width: 160px;
  border: 1px solid #e8e8e8;
}

:deep(.ant-dropdown-menu-item) {
  padding: 10px 16px;
  margin: 2px 8px;
  border-radius: 6px;
  transition: all 0.2s;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
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

:deep(.ant-menu-horizontal) {
  border-bottom: none;
}

:deep(.ant-menu-item) {
  border-radius: 6px;
  margin: 0 4px;
}

:deep(.ant-menu-item-selected) {
  background-color: #e6f4ff;
  color: #1890ff;
}

:deep(.ant-menu-item:hover) {
  background-color: #f5f5f5;
}
</style>
