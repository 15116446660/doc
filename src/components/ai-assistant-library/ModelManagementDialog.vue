<template>
  <el-dialog
    v-model="dialogVisible"
    title="模型管理"
    :width="dialogWidth"
    :close-on-click-modal="false"
    @close="handleClose"
    class="model-management-dialog"
  >
    <ModelManagement 
      v-if="dialogVisible"
      :models="props.models"
      :current-model-id="props.currentModelId"
      @models-updated="handleModelsUpdated"
      @select-model="handleSelectModel"
      @form-visibility-change="handleFormVisibility"
    />
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import ModelManagement from './ModelManagement.vue';
import type { AIModel } from '@/types/chat';

const props = defineProps<{
  show: boolean;
  currentModelId: string;
  models: AIModel[];
}>();

const emit = defineEmits<{
  (e: 'update:show', value: boolean): void;
  (e: 'select-model', modelId: string): void;
  (e: 'models-updated', models: AIModel[]): void;
}>();

const isFormVisible = ref(false);

const dialogVisible = computed({
  get: () => props.show,
  set: (value) => emit('update:show', value)
});

const dialogWidth = computed(() => {
  return isFormVisible.value ? '80%' : '40%';
});

function handleFormVisibility(isVisible: boolean) {
  isFormVisible.value = isVisible;
}

function handleModelsUpdated(models: AIModel[]) {
  emit('models-updated', models);
}

function handleSelectModel(modelId: string) {
  emit('select-model', modelId);
}

function handleClose() {
  emit('update:show', false);
}
</script>

<style scoped>
.model-management-dialog :deep(.el-dialog__body) {
  padding: 10px 20px;
}
</style> 