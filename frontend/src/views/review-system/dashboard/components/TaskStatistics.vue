<template>
  <el-card class="box-card">
    <template #header>
      <div class="card-header">
        <span>任务统计</span>
      </div>
    </template>
    <div v-if="reviewStore.statistics.value">
      <p>任务总数: {{ reviewStore.statistics.value.total }}</p>
      <p>进行中: {{ reviewStore.statistics.value.inProgress }}</p>
      <p>已完成: {{ reviewStore.statistics.value.total - reviewStore.statistics.value.inProgress - reviewStore.statistics.value.pending }}</p>
      <p>待处理: {{ reviewStore.statistics.value.pending }}</p>
    </div>
     <div v-else>
        Loading...
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useReviewStore } from '@/stores/useReviewStore';

const reviewStore = useReviewStore();

onMounted(() => {
    reviewStore.fetchStatistics();
});
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
