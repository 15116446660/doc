<template>
  <el-card>
    <template #header>批注面板</template>
    <div class="annotation-item">
      <p><strong>张专家:</strong> 这里的条款需要更明确。</p>
      <span>第3页，第2段</span>
    </div>
    <div class="annotation-item">
      <p><strong>李专家:</strong> 建议修改措辞。</p>
      <span>第5页，第1段</span>
    </div>
  </el-card>
</template>

<script lang="ts" setup>
// Placeholder
</script>

<style scoped>
.annotation-item {
  border-bottom: 1px solid #ebeef5;
  padding: 10px 0;
}
.annotation-item:last-child {
  border-bottom: none;
}
</style>
