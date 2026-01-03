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
}

.layout-header {
  background: #fff;
  padding: 0;
  height: 64px;
  line-height: 64px;
  border-bottom: 1px solid #f0f0f0;
}

.layout-content {
  flex: 1;
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
  margin-bottom: 80px; /* 为底部预留空间 */
}

.layout-footer {
  padding: 0;
  background: #fff;
  border-top: 1px solid #f0f0f0;
  height: 60px;
  line-height: 60px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .layout-content {
    padding: 16px;
  }
}
</style>