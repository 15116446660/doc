<template>
  <el-card>
    <template #header>知识图谱</template>
    <div class="knowledge-graph-placeholder">
        <p>知识图谱将在此处渲染</p>
        <p>(Knowledge Graph will be rendered here)</p>
    </div>
  </el-card>
</template>

<script lang="ts" setup>
// Placeholder
</script>

<style scoped>
.knowledge-graph-placeholder {
  width: 100%;
  height: 300px;
  border: 1px dashed #ccc;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #999;
}
</style>
