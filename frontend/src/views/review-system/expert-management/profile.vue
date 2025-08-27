<template>
  <div>
    <h1>专家档案 (Expert Profile)</h1>
    <p>专家ID: {{ expertId }}</p>
    <ExpertProfile v-if="expertStore.currentExpert" :expert="expertStore.currentExpert" />
    <div v-else-if="expertStore.isLoading">Loading...</div>
    <div v-else>未找到专家信息</div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useExpertStore } from '@/stores/useExpertStore';
import ExpertProfile from './components/ExpertProfile.vue';

const route = useRoute();
const expertStore = useExpertStore();
const expertId = computed(() => Number(route.params.id));

// This is a mock implementation. A real one would fetch a single expert.
// We will simulate this by finding the expert in the list.
onMounted(async () => {
    await expertStore.fetchExperts();
    const expert = expertStore.experts.value.find(e => e.userId === expertId.value);
    // In a real store, you'd have a `setCurrentExpert` action.
    if (expert) {
        (expertStore.currentExpert as any) = expert;
    }
});
</script>

<style scoped>
/* Styles for expert profile page */
</style>
