<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import BasicLayout from './layouts/BasicLayout.vue'
import { useLoginUserStore } from '@/stores/LoginUser'

const route = useRoute()
const loginUserStore = useLoginUserStore()

const isAuthPage = computed(() => {
  return route.path === '/user/login' || route.path === '/user/register'
})

onMounted(() => {
  const token = localStorage.getItem('token')
  if (token) {
    loginUserStore.loadLoginUserFromStorage()
    if (!isAuthPage.value) {
      loginUserStore.fetchLoginUser()
    }
  }
})
</script>

<template>
  <BasicLayout v-if="!isAuthPage" />
  <router-view v-else />
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background: #f5f7fa;
  min-height: 100vh;
}

#app {
  min-height: 100vh;
}
</style>
