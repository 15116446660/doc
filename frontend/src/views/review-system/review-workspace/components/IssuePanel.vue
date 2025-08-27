<template>
  <el-card>
    <template #header>问题面板</template>
    <div class="issue-item">
      <p><strong>[高]</strong> 合同条款不明确</p>
      <span>状态: 开放</span>
    </div>
    <div class="issue-item">
      <p><strong>[中]</strong> 格式错误</p>
      <span>状态: 已解决</span>
    </div>
  </el-card>
</template>

<script lang="ts" setup>
// Placeholder
</script>

<style scoped>
.issue-item {
  border-bottom: 1px solid #ebeef5;
  padding: 10px 0;
}
.issue-item:last-child {
  border-bottom: none;
}
</style>
