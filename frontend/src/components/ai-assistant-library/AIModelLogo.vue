<template>
  <div class="ai-model-logo" :class="size">
    <AIModelIcon v-if="isSvgLogo || hasDetectedProvider" :provider="svgProvider" :size="size" />
    <img v-else-if="model.logo" :src="model.logo" :alt="model.name || model.modelName" class="img-logo" />
    <el-icon v-else class="model-logo-default"><Cpu /></el-icon>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { Cpu } from '@element-plus/icons-vue';
import AIModelIcon from './icons/AIModelIcon.vue';

const props = defineProps({
  model: {
    type: Object,
    required: true
  },
  size: {
    type: String,
    default: 'medium' // small, medium, large
  }
});

// 判断是否使用SVG图标
const isSvgLogo = computed(() => {
  return props.model.logo && typeof props.model.logo === 'string' && 
    !props.model.logo.startsWith('/') && !props.model.logo.startsWith('http');
});

// 检测模型类型
const detectProvider = computed(() => {
  if (!props.model.model) return null;
  
  const modelStr = props.model.model.toLowerCase();
  
  // 检测不同提供商的模型
  if (modelStr.includes('gpt') || modelStr.startsWith('text-davinci') || modelStr.includes('openai')) {
    return 'openai';
  } else if (modelStr.includes('claude')) {
    return 'anthropic';
  } else if (modelStr.includes('qwen') || modelStr.includes('tongyi')) {
    return 'alibaba';
  } else if (modelStr.includes('ernie') || modelStr.includes('wenxin')) {
    return 'baidu';
  } else if (modelStr.includes('gemini')) {
    return 'google';
  } else if (modelStr.includes('mistral')) {
    return 'mistral';
  } else if (modelStr.includes('deepseek')) {
    return 'deepseek';
  }
  
  return null;
});

// 是否有检测到的提供商
const hasDetectedProvider = computed(() => {
  return detectProvider.value !== null;
});

// 获取SVG图标所需的provider
const svgProvider = computed(() => {
  if (isSvgLogo.value) {
    return props.model.logo as string;
  }
  
  if (hasDetectedProvider.value) {
    return detectProvider.value as string;
  }
  
  return props.model.provider || 'unknown';
});
</script>

<style scoped>
.ai-model-logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 2rem;
  height: 2rem;
}

.ai-model-logo.small {
  width: 1.5rem;
  height: 1.5rem;
}

.ai-model-logo.large {
  width: 3rem;
  height: 3rem;
}

.img-logo {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 4px;
}

.model-logo-default {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--el-fill-color-light);
  color: var(--el-text-color-secondary);
  border-radius: 4px;
}
</style> 