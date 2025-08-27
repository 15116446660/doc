<template>
  <el-card class="expert-card">
    <template #header>
      <div class="card-header">
        <span>{{ expert.name }}</span>
        <el-tag :type="expert.status === '空闲' ? 'success' : 'warning'">{{ expert.status }}</el-tag>
      </div>
    </template>
    <p>领域: {{ expert.field }}</p>
    <p>评审中任务: {{ expert.activeTasks }}</p>
  </el-card>
</template>

<script lang="ts" setup>
defineProps<{
  expert: {
    name: string;
    status: string;
    field: string;
    activeTasks: number;
  }
}>();
</script>

<style scoped>
.expert-card {
  margin-bottom: 10px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
