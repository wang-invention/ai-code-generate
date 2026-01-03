<script setup lang="ts">
import BasicLayout from './layouts/BasicLayout.vue'
import {test} from "@/api/testController.ts";
test().then(res=>{
  console.log(res)
})
</script>

<template>
  <BasicLayout />
</template>

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}
</style>
