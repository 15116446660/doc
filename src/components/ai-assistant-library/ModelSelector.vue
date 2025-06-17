<template>
  <el-dropdown trigger="click" @command="handleModelChange" :disabled="disabled" popper-class="beautiful-popper">
    <div class="model-selector">
      <template v-if="currentModelLogo">
        <img 
          :src="currentModelLogo" 
          class="model-logo" 
          alt="logo" 
          @error="handleLogoError"
        />
      </template>
      <el-icon v-else class="model-logo-default"><Cpu /></el-icon>
      <span>{{ currentModelName }}</span>
      <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
    </div>
    <template #dropdown>
      <el-dropdown-menu placement="top">
        <div class="panel-title">选择模型</div>
        <div class="model-list-wrapper">
          <el-dropdown-item
            v-for="model in models"
            :key="model.id"
            :command="model.id"
            :class="{ 'is-active': model.id === currentModelId }"
            class="model-dropdown-item"
          >
            <template v-if="model.logo">
              <img 
                :src="model.logo" 
                class="model-logo" 
                alt="logo" 
                @error="handleLogoError"
              />
            </template>
            <el-icon v-else class="model-logo-default"><Cpu /></el-icon>
            <span>{{ model.name }}</span>
          </el-dropdown-item>
        </div>
        <div class="divider"></div>
        <div class="config-item" @click="openModelConfig">
          <el-icon><Setting /></el-icon>
          <span>模型配置</span>
        </div>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { AIModel } from '@/types/chat';
import { Cpu, ArrowDown, Setting } from '@element-plus/icons-vue';

const props = defineProps<{
  currentModelId: string;
  models: AIModel[];
  disabled?: boolean;
}>();

const emit = defineEmits<{
  (e: 'select-model', modelId: string): void;
  (e: 'open-config'): void;
}>();

// 处理logo加载错误
const handleLogoError = (event: Event) => {
  const imgElement = event.target as HTMLImageElement;
  imgElement.style.display = 'none';
  const parentElement = imgElement.parentElement;
  if (parentElement) {
    // 创建 Element Plus 图标组件
    const iconWrapper = document.createElement('div');
    iconWrapper.className = 'el-icon model-logo-default';
    iconWrapper.innerHTML = '<svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" style="width: 1em; height: 1em;"><path fill="currentColor" d="M544 805.888V168a32 32 0 0 0-32-32H384a32 32 0 0 0-32 32v637.888L246.656 608a32 32 0 0 0-45.248 45.248l288 288a32 32 0 0 0 45.248 0l288-288a32 32 0 0 0-45.248-45.248L544 805.824zM192 192h640v128H192V192z"/></svg>';
    parentElement.insertBefore(iconWrapper, imgElement.nextSibling);
  }
};

// 计算属性：当前选中的模型
const currentModel = computed(() => {
  return props.models.find(model => model.id === props.currentModelId) || null;
});

// 计算属性：当前模型的logo
const currentModelLogo = computed(() => {
  return currentModel.value?.logo || '';
});

// 计算属性：当前模型的名称
const currentModelName = computed(() => {
  return currentModel.value?.name || '选择模型';
});

// 处理模型切换
const handleModelChange = (modelId: string) => {
  emit('select-model', modelId);
};

// 打开模型配置
const openModelConfig = () => {
  emit('open-config');
};
</script>

<style lang="scss" scoped>
.model-selector {
  display: flex;
  align-items: center;
  padding: 5px 12px;
  border-radius: 8px;
  cursor: pointer;
  background-color: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  transition: all 0.3s ease;
  height: 32px;
  
  &:hover {
    border-color: var(--el-color-primary);
    background-color: var(--el-color-primary-light-9);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  }

  .model-logo, .model-logo-default {
    width: 18px;
    height: 18px;
    margin-right: 8px;
    border-radius: 4px;
    object-fit: contain;
  }

  .model-logo-default {
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: var(--el-fill-color-light);
    color: var(--el-text-color-secondary);
    font-size: 18px;
    
    :deep(svg) {
      width: 18px;
      height: 18px;
    }
  }

  span {
    font-size: 14px;
    color: var(--el-text-color-regular);
    font-weight: 500;
  }

  .dropdown-icon {
    margin-left: 8px;
    margin-right: 0;
    font-size: 12px;
    color: var(--el-text-color-secondary);
    transition: transform 0.3s ease;
  }

  &:hover .dropdown-icon {
    transform: rotate(180deg);
    color: var(--el-color-primary);
  }
}

.model-dropdown-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  
  .model-logo, .model-logo-default {
    width: 18px;
    height: 18px;
    margin-right: 8px;
    border-radius: 4px;
    object-fit: contain;
  }

  .model-logo-default {
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: var(--el-fill-color-light);
    color: var(--el-text-color-secondary);
    font-size: 18px;
    
    :deep(svg) {
      width: 18px;
      height: 18px;
    }
  }
}

.panel-title {
  padding: 8px 12px;
  font-size: 14px;
  color: var(--el-text-color-secondary);
  border-bottom: 1px solid var(--el-border-color-light);
}

.divider {
  height: 1px;
  background-color: var(--el-border-color-light);
  margin: 4px 0;
}

.config-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  cursor: pointer;
  color: var(--el-text-color-regular);
  
  &:hover {
    background-color: var(--el-fill-color-light);
  }
  
  .el-icon {
    margin-right: 8px;
  }
}
</style> 