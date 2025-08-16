<template>
  <div class="ai-model-logo-demo">
    <h2>AI模型Logo展示</h2>
    <div class="logo-grid">
      <div v-for="provider in providers" :key="provider.id" class="logo-item">
        <div class="logo-container">
          <AIModelIcon :provider="provider.id" size="large" />
        </div>
        <div class="logo-name">{{ provider.name }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import AIModelIcon from './icons/AIModelIcon.vue';

const providers = ref([
  { id: 'openai', name: 'OpenAI' },
  { id: 'alibaba', name: '通义千问' },
  { id: 'anthropic', name: 'Claude' },
  { id: 'deepseek', name: 'DeepSeek' },
  { id: 'baidu', name: '文心一言' },
  { id: 'google', name: 'Gemini' },
  { id: 'mistral', name: 'Mistral AI' },
  { id: 'unknown', name: '默认' }
]);
</script>

<style scoped>
.ai-model-logo-demo {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

h2 {
  margin-bottom: 20px;
  color: #333;
  text-align: center;
}

.logo-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 20px;
}

.logo-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.logo-container {
  width: 60px;
  height: 60px;
  margin-bottom: 10px;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.logo-name {
  font-size: 14px;
  text-align: center;
}

/* 主题颜色 */
:deep(.provider-openai) {
  color: #10a37f;
}

:deep(.provider-alibaba) {
  color: #ff6a00;
}

:deep(.provider-anthropic) {
  color: #b300ff;
}

:deep(.provider-deepseek) {
  color: #0078d4;
}

:deep(.provider-baidu) {
  color: #2932e1;
}

:deep(.provider-google) {
  color: #4285f4;
}

:deep(.provider-mistral) {
  color: #7c3aed;
}
</style> 