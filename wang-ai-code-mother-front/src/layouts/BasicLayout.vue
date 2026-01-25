<script setup lang="ts">
import GlobalHeader from './GlobalHeader.vue'
import GlobalFooter from './GlobalFooter.vue'
</script>

<template>
  <a-layout class="basic-layout">
    <!-- 头部导航 -->
    <a-layout-header class="layout-header">
      <GlobalHeader />
    </a-layout-header>

    <!-- 内容区域 -->
    <a-layout-content class="layout-content">
      <router-view />
    </a-layout-content>

    <!-- 底部版权 -->
    <a-layout-footer class="layout-footer">
      <GlobalFooter />
    </a-layout-footer>
  </a-layout>
</template>

<style scoped>
.basic-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f3f4f6;
}

.layout-header {
  background: #ffffff;
  padding: 0;
  height: 56px;
  line-height: 56px;
  border-bottom: 1px solid #e5e7eb;
}

.layout-content {
  flex: 1;
  padding: 24px;
  max-width: 1280px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.layout-footer {
  padding: 0;
  background: #f9fafb;
  border-top: 1px solid #e5e7eb;
  height: 56px;
  line-height: 56px;
}

@media (max-width: 768px) {
  .layout-content {
    padding: 16px;
  }
}
</style>
