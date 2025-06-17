<template>
  <div class="ai-settings-container">
    <ModelManagement 
      :models="models"
      :current-model-id="currentModelId"
      @models-updated="handleModelsUpdated"
      @select-model="handleSelectModel"
    />
  </div>
</template>

<script setup lang="ts">
import ModelManagement from '@/components/ai-assistant-library/ModelManagement.vue';
import { useAIModels } from '@/components/ai-assistant-library/hooks/useAIModels';

const { models, currentModelId, updateModels, selectModel } = useAIModels();

const handleModelsUpdated = (updatedModels: any[]) => {
  updateModels(updatedModels);
};

const handleSelectModel = (modelId: string) => {
  selectModel(modelId);
};
</script>

<style scoped>
.ai-settings-container {
  padding: 16px;
  height: calc(100vh - 88px); /* Adjust based on your app's header/nav height */
  box-sizing: border-box;
}
</style>
