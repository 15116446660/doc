<template>
  <div class="ai-settings-page">
    <h2 class="page-title">AI 模型配置</h2>
    <div class="management-container">
      <ModelManagement 
        :models="models"
        :current-model-id="currentModelId"
        @models-updated="updateModels"
        @select-model="selectModel"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import ModelManagement from '@/components/ai-assistant-library/ModelManagement.vue';
import { useAIModels } from '@/components/ai-assistant-library/hooks/useAIModels';

const {
  models,
  currentModelId,
  loadModels,
  updateModels,
  selectModel,
} = useAIModels();

onMounted(() => {
  // 页面加载时获取模型列表
  loadModels();
});
</script>

<style scoped>
.ai-settings-page {
  padding: 24px;
  background-color: #f9fafb;
  height: 100%;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 24px;
}

.management-container {
  background-color: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
}
</style> 