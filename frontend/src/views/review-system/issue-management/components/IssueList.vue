<template>
  <div>
    <h3>问题列表</h3>
    <el-table :data="issues" style="width: 100%">
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="status" label="状态" />
      <el-table-column prop="severity" label="严重程度" />
    </el-table>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'

const issues = ref([
  { title: '问题1', status: '开放', severity: '高' },
  { title: '问题2', status: '已解决', severity: '中' },
]);
</script>
