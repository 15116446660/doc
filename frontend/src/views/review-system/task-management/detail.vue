<template>
  <div>
    <h1>任务详情 (Task Detail)</h1>
    <p>任务ID: {{ $route.params.id }}</p>
    <!-- Task details will be displayed here -->
    <el-card>
        <template #header>任务基本信息</template>
        <p>任务名称: XXXXX</p>
        <p>任务描述: XXXXX</p>
    </el-card>
  </div>
</template>

<script setup lang="ts">
// Placeholder for task detail page logic
</script>

<style scoped>
/* Styles for task detail page */
</style>
