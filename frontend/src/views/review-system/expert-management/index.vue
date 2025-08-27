<template>
  <div class="expert-management-container">
    <div class="header">
        <h1>专家资源管理</h1>
    </div>
    <ExpertList :experts="expertStore.experts.value" :loading="expertStore.isLoading.value" />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useExpertStore } from '@/stores/useExpertStore';
import ExpertList from './components/ExpertList.vue';

const expertStore = useExpertStore();

onMounted(() => {
    expertStore.fetchExperts();
});

</script>

<style scoped>
.expert-management-container {
  padding: 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
</style>
