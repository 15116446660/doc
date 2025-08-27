<template>
  <el-card class="box-card">
    <template #header>
      <div class="card-header">
        <span>实时监控看板</span>
      </div>
    </template>
    <div v-if="reviewStore.statistics.value">
      <p>在审任务统计: {{ reviewStore.statistics.value.inProgress }}</p>
      <p>专家工作负载: {{ expertStore.workloadStats.value?.averageWorkload || 0 }}%</p>
      <p>SLA预警: 3个任务超时 (mock)</p>
      <p>问题解决率趋势: 92% (mock, 上升)</p>
    </div>
    <div v-else>
        Loading...
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useReviewStore } from '@/stores/useReviewStore';
import { useExpertStore } from '@/stores/useExpertStore';

const reviewStore = useReviewStore();
const expertStore = useExpertStore();

onMounted(() => {
    reviewStore.fetchStatistics();
    expertStore.fetchWorkloadStats();
});
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
