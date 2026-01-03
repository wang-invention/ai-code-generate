<script setup lang="ts"></script>

<template>
  <div class="footer">
    <div class="footer-content">
      <span>wang原创项目 by </span>
      <a href="https://www.codefather.cn" target="_blank" rel="noopener noreferrer">wang-invention</a>
    </div>
  </div>
</template>

<style scoped>
.footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #fff;
  border-top: 1px solid #f0f0f0;
  padding: 12px 0;
  text-align: center;
  z-index: 100;
}

.footer-content {
  font-size: 14px;
  color: #666;
}

.footer-content a {
  color: #1890ff;
  text-decoration: none;
}

.footer-content a:hover {
  text-decoration: underline;
}
</style>