<template>
  <el-card class="box-card">
    <template #header>
      <div class="card-header">
        <span>专家工作负载</span>
      </div>
    </template>
    <div v-if="expertStore.workloadStats.value">
      <p>专家总数: {{ expertStore.workloadStats.value.totalExperts }}</p>
      <p>空闲: {{ expertStore.workloadStats.value.availableExperts }}</p>
      <p>忙碌: {{ expertStore.workloadStats.value.totalExperts - expertStore.workloadStats.value.availableExperts }}</p>
      <p>平均负载: {{ expertStore.workloadStats.value.averageWorkload.toFixed(1) }}%</p>
    </div>
    <div v-else>
        Loading...
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useExpertStore } from '@/stores/useExpertStore';

const expertStore = useExpertStore();

onMounted(() => {
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
