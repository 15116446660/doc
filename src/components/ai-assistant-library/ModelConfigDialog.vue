<template>
  <div v-if="show" class="modal-overlay" @click.self="onClose">
    <div class="modal-container">
      <div class="modal-header">
        <h2>添加新模型</h2>
        <button class="close-button" @click="onClose">&times;</button>
      </div>
      
      <div class="modal-tabs">
        <div class="tabs-scroll-container">
          <button 
            v-for="tab in tabs" 
            :key="tab.id"
            :class="['tab-button', { active: activeTab === tab.id }]"
            @click="activeTab = tab.id"
          >
            {{ tab.name }}
          </button>
        </div>
      </div>

      <div class="modal-content">
        <form @submit.prevent="handleSubmit" class="model-form">
          <div class="form-group">
            <label for="modelName">模型名称</label>
            <input 
              id="modelName"
              v-model="formData.name"
              type="text"
              required
              placeholder="输入模型名称"
            >
          </div>

          <div class="form-group">
            <label for="apiKey">API Key</label>
            <input 
              id="apiKey"
              v-model="formData.config.apiKey"
              type="password"
              required
              placeholder="输入 API Key"
            >
          </div>

          <div class="form-group">
            <label for="apiEndpoint">API 端点</label>
            <input 
              id="apiEndpoint"
              v-model="formData.config.apiEndpoint"
              type="text"
              required
              placeholder="输入 API 端点"
            >
          </div>

          <div class="form-group">
            <label for="modelVersion">模型版本</label>
            <input 
              id="modelVersion"
              v-model="formData.config.modelVersion"
              type="text"
              required
              placeholder="输入模型版本"
            >
          </div>

          <div class="form-group">
            <label for="temperature">Temperature</label>
            <input 
              id="temperature"
              v-model.number="formData.config.temperature"
              type="number"
              min="0"
              max="2"
              step="0.1"
              required
            >
          </div>

          <div class="form-group">
            <label for="maxTokens">最大 Token 数</label>
            <input 
              id="maxTokens"
              v-model.number="formData.config.maxTokens"
              type="number"
              min="1"
              required
            >
          </div>

          <div class="form-actions">
            <button type="button" class="cancel-button" @click="onClose">取消</button>
            <button type="submit" class="submit-button">确认</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from '@vue/runtime-core'
import type { AIModel } from '../../types'

interface Props {
  show: boolean
}

defineProps<Props>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'submit', model: AIModel): void
}>()

const tabs = [
  { id: 'openai', name: 'OpenAI' },
  { id: 'anthropic', name: 'Anthropic' },
  { id: 'gemini', name: 'Gemini' },
  { id: 'claude', name: 'Claude' },
  { id: 'custom', name: '自定义' }
]

const activeTab = ref('openai')

const formData = reactive<AIModel>({
  id: '',
  name: '',
  logo: '',
  config: {
    apiKey: '',
    apiEndpoint: '',
    temperature: 0.7,
    maxTokens: 2000,
    modelVersion: '',
    topP: 1,
    stream: true
  }
})

const onClose = () => {
  emit('close')
}

const handleSubmit = () => {
  emit('submit', { ...formData })
  onClose()
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-container {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.modal-header {
  padding: 16px 24px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h2 {
  margin: 0;
  font-size: 1.5rem;
}

.close-button {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  padding: 0;
  color: #666;
}

.modal-tabs {
  border-bottom: 1px solid #eee;
  overflow-x: auto;
}

.tabs-scroll-container {
  display: flex;
  padding: 0 16px;
  min-width: min-content;
}

.tab-button {
  padding: 12px 24px;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 1rem;
  color: #666;
  border-bottom: 2px solid transparent;
  white-space: nowrap;
}

.tab-button.active {
  color: #1a73e8;
  border-bottom-color: #1a73e8;
}

.modal-content {
  padding: 24px;
  overflow-y: auto;
}

.model-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: 500;
  color: #333;
}

.form-group input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.cancel-button,
.submit-button {
  padding: 8px 24px;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
}

.cancel-button {
  background: none;
  border: 1px solid #ddd;
  color: #666;
}

.submit-button {
  background: #1a73e8;
  border: none;
  color: white;
}

.submit-button:hover {
  background: #1557b0;
}
</style> 