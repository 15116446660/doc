<template>
  <el-card class="task-card">
    <template #header>
      <div class="card-header">
        <span>任务名称: {{ task.name }}</span>
        <el-tag :type="statusType">{{ task.status }}</el-tag>
      </div>
    </template>
    <p>优先级: {{ task.priority }}</p>
    <p>创建日期: {{ task.createDate }}</p>
  </el-card>
</template>

<script lang="ts" setup>
import { computed } from 'vue'

const props = defineProps<{
  task: {
    name: string;
    status: string;
    priority: string;
    createDate: string;
  }
}>();

const statusType = computed(() => {
    if (props.task.status === '进行中') return 'primary'
    if (props.task.status === '已完成') return 'success'
    return 'info'
})
</script>

<style scoped>
.task-card {
  margin-bottom: 10px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
