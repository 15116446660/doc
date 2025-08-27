<template>
  <el-card>
    <template #header>重复问题检测</template>
    <p>检测到 1 个可能重复的问题:</p>
    <el-alert title="[高] 合同条款表述不清" type="warning" :closable="false">
        <p>相似度: 85%</p>
        <el-button type="primary" size="small">合并</el-button>
    </el-alert>
  </el-card>
</template>

<script lang="ts" setup>
// Placeholder
</script>
