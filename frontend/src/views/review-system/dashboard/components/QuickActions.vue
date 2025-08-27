<template>
  <el-card class="box-card">
    <template #header>
      <div class="card-header">
        <span>快速操作入口</span>
      </div>
    </template>
    <div class="quick-actions">
      <el-button type="primary">一键创建评审</el-button>
      <el-button type="danger">紧急任务处理</el-button>
      <el-button>待办事项</el-button>
      <el-button>最近访问</el-button>
    </div>
  </el-card>
</template>

<script setup lang="ts">
// Placeholder for quick actions logic
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.quick-actions .el-button {
  margin: 5px;
}
</style>
