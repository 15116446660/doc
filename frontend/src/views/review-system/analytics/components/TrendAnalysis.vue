<template>
  <el-card>
    <template #header>效率趋势分析</template>
    <LineChart />
  </el-card>
</template>

<script lang="ts" setup>
import LineChart from './ChartComponents/LineChart.vue';
</script>
