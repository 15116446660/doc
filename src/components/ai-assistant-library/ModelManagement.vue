<template>
  <div class="model-management-wrapper" :class="{ 'show-form': isFormVisible }">
    <!-- 左侧模型列表 -->
    <div class="list-panel">
      <div class="section-header">
        <h3>已添加模型</h3>
        <el-button type="primary" size="small" @click="handleAdd">添加模型</el-button>
      </div>
      <div class="model-list">
        <div 
          v-for="model in models" 
          :key="model.id"
          class="model-item"
          :class="{ 'is-active': currentModelId === model.id }"
        >
          <div class="model-info">
            <img :src="model.logo" class="model-logo" alt="Model Logo" />
            <div class="model-details">
              <div class="model-name">{{ model.name }}</div>
              <div class="model-type">{{ getModelTypeName(model.type) }}</div>
            </div>
          </div>
          <div class="model-actions">
            <el-button 
              type="primary" 
              size="small" 
              plain 
              @click="selectModel(model.id)"
              v-if="currentModelId !== model.id"
            >
              使用
            </el-button>
            <el-button 
              type="info" 
              size="small" 
              plain 
              @click="handleEdit(model)"
            >
              编辑
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              plain 
              @click="confirmDelete(model)"
              v-if="models.length > 1"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧模型表单 -->
    <div v-if="isFormVisible" class="form-panel">
       <div class="form-header">
        <h3>{{ isEditing ? '编辑模型' : '添加模型' }}</h3>
        <el-button :icon="Close" circle text @click="closeForm"></el-button>
      </div>
       <el-form :model="modelForm" label-width="80px" class="model-form">
        <el-form-item label="模型类型">
          <el-select v-model="modelForm.type" placeholder="选择模型类型">
            <el-option
              v-for="option in modelTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="模型名称">
          <el-input v-model="modelForm.name" placeholder="输入模型名称" />
        </el-form-item>
        
        <el-form-item label="API密钥">
          <el-input v-model="modelForm.apiKey" placeholder="输入API密钥" show-password />
        </el-form-item>
        
        <el-form-item label="API URL" v-if="modelForm.type !== 'local'">
          <el-input v-model="modelForm.baseUrl" placeholder="输入API基础URL" />
        </el-form-item>
        
        <template v-if="currentProviderConfig.basicParams?.modelName && modelForm.type !== 'azure'">
          <el-form-item label="模型版本">
            <el-input v-model="modelForm.modelName" placeholder="输入模型名称，例如：gpt-4" />
          </el-form-item>
        </template>
        
        <template v-if="modelForm.type === 'azure'">
          <el-form-item label="部署名称">
            <el-input v-model="modelForm.deploymentName" placeholder="输入Azure部署名称" />
            <div class="field-hint">Azure OpenAI资源中的部署名称</div>
          </el-form-item>
          
          <el-form-item label="API版本">
            <el-input v-model="modelForm.apiVersion" placeholder="输入API版本，例如：2023-05-15" />
          </el-form-item>
        </template>
        
        <template v-if="modelForm.type === 'mistral' && currentProviderConfig.specialParams?.randomSeed">
          <el-form-item label="随机性种子">
            <el-input-number v-model="modelForm.randomSeed" :min="0" :max="9999" controls-position="right" />
            <div class="field-hint">用于可重复结果生成的随机性种子</div>
          </el-form-item>
        </template>
        
        <div class="advanced-options-section">
          <div class="advanced-options-header" @click="toggleAdvancedOptions">
            <span class="advanced-label">高级选项</span>
            <div class="advanced-toggle">
              <span>高级设置</span>
              <el-icon :class="{ 'is-active': showAdvancedOptions }">
                <arrow-down />
              </el-icon>
            </div>
          </div>
          
          <el-collapse-transition>
            <div class="advanced-options-content" v-show="showAdvancedOptions">
              <el-form-item label="温度">
                <div class="control-wrapper">
                  <div class="slider-with-value">
                    <div class="slider-container">
                      <el-slider 
                        v-model="modelForm.temperature" 
                        :min="currentProviderConfig.ranges?.temperature?.[0] || 0" 
                        :max="currentProviderConfig.ranges?.temperature?.[1] || 1" 
                        :step="0.1" 
                      />
                    </div>
                    <div class="slider-value">{{ modelForm.temperature.toFixed(1) }}</div>
                  </div>
                  <div class="field-hint">控制响应的随机性，较高的值会产生更多样化的回答</div>
                </div>
              </el-form-item>
              
              <el-form-item label="最大输出">
                <div class="control-wrapper">
                  <el-input-number 
                    v-model="modelForm.maxTokens" 
                    :min="currentProviderConfig.ranges?.maxTokens?.[0] || 1" 
                    :max="currentProviderConfig.ranges?.maxTokens?.[1] || 4096" 
                    controls-position="right" 
                  />
                  <div class="field-hint">模型最大生成的Token数</div>
                </div>
              </el-form-item>
              
              <el-form-item label="Top P" v-if="currentProviderConfig.advancedParams?.topP">
                <div class="control-wrapper">
                  <div class="slider-with-value">
                    <div class="slider-container">
                      <el-slider 
                        v-model="modelForm.topP" 
                        :min="currentProviderConfig.ranges?.topP?.[0] || 0" 
                        :max="currentProviderConfig.ranges?.topP?.[1] || 1" 
                        :step="0.05" 
                      />
                    </div>
                    <div class="slider-value">{{ modelForm.topP.toFixed(2) }}</div>
                  </div>
                  <div class="field-hint">控制生成多样性的核采样阈值</div>
                </div>
              </el-form-item>
              
              <el-form-item label="Top K" v-if="currentProviderConfig.advancedParams?.topK">
                <div class="control-wrapper">
                  <el-input-number 
                    v-model="modelForm.topK" 
                    :min="currentProviderConfig.ranges?.topK?.[0] || 1" 
                    :max="currentProviderConfig.ranges?.topK?.[1] || 100" 
                    controls-position="right" 
                  />
                  <div class="field-hint">每一步考虑的最高概率Token数量</div>
                </div>
              </el-form-item>
              
              <el-form-item label="频率惩罚" v-if="currentProviderConfig.advancedParams?.frequencyPenalty">
                <div class="control-wrapper">
                  <div class="slider-with-value">
                    <div class="slider-container">
                      <el-slider v-model="modelForm.frequencyPenalty" :min="-2" :max="2" :step="0.1" />
                    </div>
                    <div class="slider-value">{{ modelForm.frequencyPenalty.toFixed(1) }}</div>
                  </div>
                  <div class="field-hint">减少对重复出现Token的使用</div>
                </div>
              </el-form-item>
              
              <el-form-item label="存在惩罚" v-if="currentProviderConfig.advancedParams?.presencePenalty">
                <div class="control-wrapper">
                  <div class="slider-with-value">
                    <div class="slider-container">
                      <el-slider v-model="modelForm.presencePenalty" :min="-2" :max="2" :step="0.1" />
                    </div>
                    <div class="slider-value">{{ modelForm.presencePenalty.toFixed(1) }}</div>
                  </div>
                  <div class="field-hint">减少对已出现主题的重复</div>
                </div>
              </el-form-item>
              
              <el-form-item label="流式响应" v-if="currentProviderConfig.advancedParams?.stream">
                <div class="control-wrapper">
                  <el-switch v-model="modelForm.stream" />
                  <div class="field-hint">开启逐字生成响应</div>
                </div>
              </el-form-item>
              
              <el-form-item label="能力级别">
                <el-select v-model="modelForm.level" class="full-width-select">
                  <el-option label="基础" value="basic" />
                  <el-option label="高级" value="advanced" />
                  <el-option label="超级高级" value="super" />
                </el-select>
              </el-form-item>
            </div>
          </el-collapse-transition>
        </div>
      </el-form>
       <div class="form-footer">
          <el-button @click="closeForm">取消</el-button>
          <el-button type="primary" @click="saveModel">
            {{ isEditing ? '保存更新' : '确认添加' }}
          </el-button>
        </div>
    </div>

    <el-dialog v-model="showDeleteConfirm" title="确认删除" width="300px" append-to-body>
      <span>确定要删除模型 "{{ modelToDelete?.name }}" 吗？此操作不可恢复。</span>
      <template #footer>
        <el-button @click="showDeleteConfirm = false">取消</el-button>
        <el-button type="danger" @click="deleteModel">确认删除</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { ArrowDown, Close } from '@element-plus/icons-vue';
import { v4 as uuidv4 } from 'uuid';
import type { AIModel } from '@/types/chat';
import { getProviderConfig } from '@/components/ai-assistant-library/config/providerConfigs';

// 为表单创建一个专用的、扁平化的状态类型
type ModelFormState = {
  id: string;
  name: string;
  type: string;
  logo: string;
  level: 'basic' | 'advanced' | 'super';
  apiKey: string;
  baseUrl: string;
  modelName: string;
  temperature: number;
  maxTokens: number;
  topP: number;
  topK: number;
  frequencyPenalty: number;
  presencePenalty: number;
  stream: boolean;
  deploymentName: string;
  apiVersion: string;
  randomSeed: number;
};

const props = defineProps<{
  currentModelId: string;
  models: AIModel[];
}>();

const emit = defineEmits<{
  (e: 'select-model', modelId: string): void;
  (e: 'models-updated', models: AIModel[]): void;
  (e: 'form-visibility-change', isVisible: boolean): void;
}>();

const localModels = ref<AIModel[]>([]);
const showAdvancedOptions = ref(false);
const isFormVisible = ref(false);
const isEditing = ref(false);
const modelForm = ref<ModelFormState>(createEmptyForm());
const showDeleteConfirm = ref(false);
const modelToDelete = ref<AIModel | null>(null);

watch(() => props.models, (newModels) => {
  localModels.value = [...newModels];
}, { immediate: true, deep: true });

watch(isFormVisible, (newValue) => {
  emit('form-visibility-change', newValue);
});

const modelTypeOptions = [
  { label: 'OpenAI', value: 'openai' },
  { label: 'Azure OpenAI', value: 'azure' },
  { label: 'Anthropic (Claude)', value: 'anthropic' },
  { label: 'Google (Gemini)', value: 'google' },
  { label: 'Mistral AI', value: 'mistral' },
  { label: 'DeepSeek', value: 'deepseek' },
  { label: '其他', value: 'other' }
];

function getModelTypeName(type: string): string {
  return modelTypeOptions.find(opt => opt.value === type)?.label || type;
}

function createEmptyForm(): ModelFormState {
  const type = 'openai';
  return {
    id: '',
    name: '',
    type,
    apiKey: '',
    baseUrl: 'https://api.openai.com/v1',  // Default OpenAI URL
    modelName: '',
    temperature: 0.7,  // Default temperature
    maxTokens: 4096,   // Default max tokens
    level: 'basic',
    logo: '',
    topP: 1.0,
    topK: 50,
    frequencyPenalty: 0,
    presencePenalty: 0,
    stream: true,
    deploymentName: '',
    apiVersion: '2023-05-15',
    randomSeed: 42
  };
}

function closeForm() {
  isFormVisible.value = false;
  isEditing.value = false;
  modelForm.value = createEmptyForm();
}

function handleAdd() {
  isEditing.value = false;
  modelForm.value = createEmptyForm();
  isFormVisible.value = true;
}

function handleEdit(model: AIModel) {
  isEditing.value = true;
  modelForm.value = {
    id: model.id,
    name: model.name,
    type: model.type,
    apiKey: model.apiKey,
    baseUrl: model.baseUrl || '',
    modelName: model.modelVersion,
    temperature: model.temperature,
    maxTokens: model.maxTokens,
    level: model.level || 'basic',
    logo: model.logo || '',
    topP: 1.0,
    topK: 50,
    frequencyPenalty: 0,
    presencePenalty: 0,
    stream: true,
    deploymentName: '',
    apiVersion: '2023-05-15',
    randomSeed: 42
  };
  isFormVisible.value = true;
}

function selectModel(modelId: string) {
  emit('select-model', modelId);
  ElMessage.success('已切换模型');
}

function confirmDelete(model: AIModel) {
  modelToDelete.value = model;
  showDeleteConfirm.value = true;
}

function deleteModel() {
  if (!modelToDelete.value) return;
  
  if (modelToDelete.value.id === props.currentModelId) {
    const otherModel = localModels.value.find(m => m.id !== props.currentModelId);
    if (otherModel) {
      emit('select-model', otherModel.id);
    }
  }
  
  const updatedModels = localModels.value.filter(m => m.id !== modelToDelete.value?.id);
  emit('models-updated', updatedModels);
  
  showDeleteConfirm.value = false;
  ElMessage.success('模型已删除');
  
  if(isEditing.value && modelToDelete.value.id === modelForm.value.id) {
    closeForm();
  }
}

function saveModel() {
  if (!modelForm.value.name.trim()) return ElMessage.error('请输入模型名称');
  if (!modelForm.value.apiKey.trim() && modelForm.value.type !== 'local') return ElMessage.error('请输入API密钥');
  
  const providerConfig = getProviderConfig(modelForm.value.type);
  if (providerConfig.basicParams?.modelName && !modelForm.value.modelName && modelForm.value.type !== 'azure') {
    return ElMessage.error('请输入模型版本');
  }
  if (modelForm.value.type === 'azure') {
    if (!modelForm.value.deploymentName) return ElMessage.error('请输入Azure部署名称');
    if (!modelForm.value.apiVersion) return ElMessage.error('请输入API版本');
  }
  
  const logo = modelForm.value.logo || '/logos/svg/custom.svg';
  
  const updatedModel: AIModel = {
    id: isEditing.value ? modelForm.value.id : uuidv4(),
    name: modelForm.value.name,
    type: modelForm.value.type,
    provider: modelForm.value.type,
    apiKey: modelForm.value.apiKey,
    baseUrl: modelForm.value.baseUrl,
    temperature: modelForm.value.temperature,
    maxTokens: modelForm.value.maxTokens,
    level: modelForm.value.level,
    logo,
    modelVersion: modelForm.value.type === 'azure' ? modelForm.value.deploymentName : modelForm.value.modelName
  };
  
  let updatedModels: AIModel[];
  if (isEditing.value) {
    updatedModels = localModels.value.map(m => m.id === updatedModel.id ? updatedModel : m);
    ElMessage.success('模型已更新');
  } else {
    updatedModels = [...localModels.value, updatedModel];
    ElMessage.success('模型已添加');
  }
  
  emit('models-updated', updatedModels);
  closeForm();
}

function toggleAdvancedOptions() {
  showAdvancedOptions.value = !showAdvancedOptions.value;
}

const currentProviderConfig = computed(() => getProviderConfig(modelForm.value.type));

watch(() => modelForm.value.type, (newType) => {
  // 根据不同的提供商设置默认值
  switch (newType) {
    case 'openai':
      modelForm.value.baseUrl = 'https://api.openai.com/v1';
      modelForm.value.maxTokens = 4096;
      break;
    case 'anthropic':
      modelForm.value.baseUrl = 'https://api.anthropic.com/v1';
      modelForm.value.maxTokens = 200000;
      break;
    case 'google':
      modelForm.value.baseUrl = 'https://generativelanguage.googleapis.com/v1beta';
      modelForm.value.maxTokens = 32768;
      break;
    case 'deepseek':
      modelForm.value.baseUrl = 'https://api.deepseek.com/v1';
      modelForm.value.maxTokens = 8192;
      break;
    default:
      if (!isEditing.value) {
        modelForm.value.baseUrl = '';
        modelForm.value.maxTokens = 4096;
      }
  }
  
  // 保持通用默认值
  if (!isEditing.value) {
    modelForm.value.temperature = 0.7;
    modelForm.value.topP = 1.0;
    modelForm.value.topK = 50;
  }
});
</script>

<style scoped>
.model-management-wrapper {
  display: flex;
  height: 100%;
  width: 100%;
  overflow: hidden;
  gap: 16px;
  background-color: var(--el-bg-color-page);
  padding: 16px;
  border-radius: 8px;
  box-sizing: border-box;
}

.list-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 300px;
  transition: all 0.3s ease;
}

.form-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  transition: all 0.3s ease;
  min-width: 350px;
}

.model-management-wrapper.show-form .list-panel {
  flex-basis: 50%;
}

.model-management-wrapper.show-form .form-panel {
  flex-basis: 50%;
}

/* 响应式布局 */
@media (max-width: 992px) {
  .model-management-wrapper {
    flex-direction: column;
    height: auto;
  }

  .list-panel,
  .form-panel {
    flex: 1;
    min-height: 400px; /* 保证在小屏幕上有最小高度 */
  }

  .model-management-wrapper.show-form .list-panel,
  .model-management-wrapper.show-form .form-panel {
     flex-basis: auto; /* 重置 basis 以适应垂直布局 */
  }
}

.section-header, .form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 8px 12px;
  flex-shrink: 0;
}

.form-header {
  padding: 8px 8px 8px 24px;
  border-bottom: 1px solid var(--el-border-color);
}

h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.model-list {
  min-width: 200px;
  flex-grow: 1;
  overflow-y: auto;
  padding-right: 8px; /* For scrollbar */
}

.model-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  margin-bottom: 12px;
  background-color: var(--el-bg-color);
  transition: all 0.2s ease;
}

.model-item.is-active {
  border-color: var(--el-color-primary);
  box-shadow: 0 0 10px rgba(var(--el-color-primary-rgb), 0.1);
}

.model-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.model-logo {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: contain;
  background: #fff;
  border: 1px solid var(--el-border-color-lighter);
}

.model-details {
  display: flex;
  flex-direction: column;
}

.model-name {
  font-weight: 500;
  font-size: 14px;
}

.model-type {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.model-actions {
  display: flex;
  gap: 8px;
}

.model-form {
  flex-grow: 1;
  overflow-y: auto;
  padding: 24px;
}

.full-width-select {
  width: 100%;
}

.field-hint {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
  margin-top: 4px;
}

.advanced-options-section {
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  margin-top: 24px;
  overflow: hidden;
}

.advanced-options-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  background-color: var(--el-bg-color-page);
}

.advanced-label {
  font-weight: 500;
}

.advanced-toggle {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.advanced-toggle .el-icon {
  transition: transform 0.3s;
}

.advanced-toggle .el-icon.is-active {
  transform: rotate(180deg);
}

.advanced-options-content {
  padding: 16px;
}

.slider-with-value {
  display: flex;
  align-items: center;
  gap: 16px;
}

.slider-container {
  flex-grow: 1;
}

.slider-value {
  width: 40px;
  text-align: right;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.model-option-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}
.model-option-desc {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.form-footer {
  padding: 16px 24px;
  border-top: 1px solid var(--el-border-color);
  background-color: var(--el-bg-color);
  text-align: right;
  flex-shrink: 0;
}

.control-wrapper {
  width: 100%;
}
</style> 