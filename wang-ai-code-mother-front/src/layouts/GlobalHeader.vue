<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { menuItems } from '../router'

const router = useRouter()
const route = useRoute()
const isReady = ref(false)

router.isReady().then(() => {
  isReady.value = true
})

const selectedKeys = computed(() => {
  if (!isReady.value) return []
  const menuItem = menuItems?.find(item => 'path' in item && item.path === route.path)
  return menuItem ? [menuItem.key as string] : []
})

const handleMenuClick = ({ key }: { key: string }) => {
  const menuItem = menuItems?.find(item => item?.key === key)
  if (menuItem && 'path' in menuItem && menuItem.path) {
    router.push(menuItem.path as string)
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
          :items="menuItems"
          :selected-keys="selectedKeys"
          class="header-menu"
          @select="handleMenuClick"
        />
      </div>
      <div class="header-right">
        <a-button type="primary">登录</a-button>
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
  color: #1890ff;
}

.header-menu {
  border-bottom: none;
  background: transparent;
}
</style>
