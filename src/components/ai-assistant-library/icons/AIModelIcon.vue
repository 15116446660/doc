<template>
  <div class="ai-model-icon" :class="`provider-${provider}`">
    <component :is="getIconComponent" />
  </div>
</template>

<script setup lang="ts">
import { computed, defineProps } from 'vue';
import OpenAIIcon from './OpenAIIcon.vue';
import AlibabaIcon from './AlibabaIcon.vue';
import AnthropicIcon from './AnthropicIcon.vue';
import DeepSeekIcon from './DeepSeekIcon.vue';
import BaiduIcon from './BaiduIcon.vue';
import GoogleIcon from './GoogleIcon.vue';
import MistralIcon from './MistralIcon.vue';
import DefaultIcon from './DefaultIcon.vue';

const props = defineProps({
  provider: {
    type: String,
    required: true
  },
  size: {
    type: String,
    default: 'medium' // small, medium, large
  }
});

const getIconComponent = computed(() => {
  switch(props.provider.toLowerCase()) {
    case 'openai':
      return OpenAIIcon;
    case 'alibaba':
      return AlibabaIcon;
    case 'anthropic':
      return AnthropicIcon;
    case 'deepseek':
      return DeepSeekIcon;
    case 'baidu':
      return BaiduIcon;
    case 'google':
      return GoogleIcon;
    case 'mistral':
      return MistralIcon;
    default:
      return DefaultIcon;
  }
});
</script>

<style scoped>
.ai-model-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 2rem;
  height: 2rem;
  border-radius: 4px;
  overflow: hidden;
}

.ai-model-icon.small {
  width: 1.5rem;
  height: 1.5rem;
}

.ai-model-icon.large {
  width: 3rem;
  height: 3rem;
}
</style> 