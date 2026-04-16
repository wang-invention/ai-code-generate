<script setup lang="ts">
import GlobalHeader from './GlobalHeader.vue'
import GlobalFooter from './GlobalFooter.vue'
</script>

<template>
  <a-layout class="basic-layout">
    <!-- 头部导航 -->
    <a-layout-header class="layout-header" fixed>
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

/* 头部固定 */
.layout-header {
  position: fixed;
  width: 100%;
  top: 0;
  z-index: 999;
  display: flex;
  align-items: center;
  background: transparent;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  //box-shadow: 0 0 12px rgba(0, 0, 0, 0.04);
  padding: 0;
}

/* 内容区域：自动撑开，不写死 100vh */
.layout-content {
  background: linear-gradient(180deg,
  #ffffff 0%,    /* 最顶部：纯白 */ #7adac1 50%,   /* 正中间：蓝色 */
  #ffffff 100%   /* 最底部：纯白 */
  );  background-size: cover;
  background-repeat: no-repeat;
  background-position: center center;

  /* 👇 关键：让内容自动占满，把 footer 挤到最下面 */
  flex: 1;

  /* 👇 关键：避开固定的头部，不被盖住 */
  padding: 80px 24px 40px;
}

/* 底部：正常文档流，跟着内容走 */
.layout-footer {
  padding: 0;
  background: #f9fafb;
  line-height: 56px;
  display: block !important;
  min-height: auto !important;
  height: auto !important;
  position: static !important;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .layout-content {
    padding: 80px 16px 40px;
  }
}
</style>
