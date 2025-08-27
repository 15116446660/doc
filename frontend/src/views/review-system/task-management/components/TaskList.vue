<template>
  <div>
    <h3>任务列表</h3>
    <el-table :data="tasks" style="width: 100%">
      <el-table-column prop="name" label="任务名称" />
      <el-table-column prop="status" label="状态" />
      <el-table-column prop="priority" label="优先级" />
    </el-table>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'

const tasks = ref([
  { name: '任务1', status: '进行中', priority: '高' },
  { name: '任务2', status: '已完成', priority: '中' },
]);
</script>
