<template>
  <el-dialog
    v-model="dialogVisible"
    title="模型管理"
    width="60%"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="model-management-container">
      <div class="model-list-section">
        <div class="section-header">
          <h3>已添加模型</h3>
          <el-button type="primary" size="small" @click="showAddModel = true">添加模型</el-button>
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
                @click="editModel(model)"
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
    </div>

    <!-- Add/Edit Model Form -->
    <el-dialog
      v-model="showAddModel"
      :title="isEditing ? '编辑模型' : '添加模型'"
      width="40%"
      append-to-body
    >
      <el-form :model="modelForm" label-width="80px">
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
        
        <!-- 不同提供商的特定字段 -->
        <template v-if="currentProviderConfig.basicParams.modelName && modelForm.type !== 'azure'">
          <el-form-item label="模型版本">
            <el-select 
              v-model="modelForm.modelName" 
              placeholder="选择模型版本" 
              class="full-width-select"
              filterable
              v-if="modelOptions.length > 0"
            >
              <el-option
                v-for="option in modelOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              >
                <div class="model-option-item">
                  <div class="model-option-name">{{ option.label }}</div>
                  <div class="model-option-desc" v-if="option.description">{{ option.description }}</div>
                </div>
              </el-option>
            </el-select>
            <el-input v-else v-model="modelForm.modelName" placeholder="输入模型名称，例如：gpt-4" />
          </el-form-item>
        </template>
        
        <!-- Azure特有字段 -->
        <template v-if="modelForm.type === 'azure'">
          <el-form-item label="部署名称">
            <el-input v-model="modelForm.deploymentName" placeholder="输入Azure部署名称" />
            <div class="field-hint">Azure OpenAI资源中的部署名称</div>
          </el-form-item>
          
          <el-form-item label="API版本">
            <el-input v-model="modelForm.apiVersion" placeholder="输入API版本，例如：2023-05-15" />
          </el-form-item>
        </template>
        
        <!-- Mistral特有字段 -->
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
          
          <div class="advanced-options-content" v-show="showAdvancedOptions">
            <!-- 通用高级选项：温度 -->
            <el-form-item label="温度">
              <div class="slider-with-value">
                <div class="slider-container">
                  <el-slider 
                    v-model="modelForm.temperature" 
                    :min="currentProviderConfig.ranges.temperature[0]" 
                    :max="currentProviderConfig.ranges.temperature[1]" 
                    :step="0.1" 
                  />
                </div>
                <div class="slider-value">{{ modelForm.temperature.toFixed(1) }}</div>
              </div>
              <div class="field-hint">控制响应的随机性，较高的值会产生更多样化的回答</div>
            </el-form-item>
            
            <!-- 通用高级选项：最大输出 -->
            <el-form-item label="最大输出">
              <el-input-number 
                v-model="modelForm.maxTokens" 
                :min="currentProviderConfig.ranges.maxTokens[0]" 
                :max="currentProviderConfig.ranges.maxTokens[1]" 
                controls-position="right" 
              />
              <div class="field-hint">模型最大生成的Token数</div>
            </el-form-item>
            
            <!-- Top P -->
            <el-form-item label="Top P" v-if="currentProviderConfig.advancedParams.topP">
              <div class="slider-with-value">
                <div class="slider-container">
                  <el-slider 
                    v-model="modelForm.topP" 
                    :min="currentProviderConfig.ranges.topP?.[0] || 0" 
                    :max="currentProviderConfig.ranges.topP?.[1] || 1" 
                    :step="0.05" 
                  />
                </div>
                <div class="slider-value">{{ modelForm.topP.toFixed(2) }}</div>
              </div>
              <div class="field-hint">控制生成多样性的核采样阈值</div>
            </el-form-item>
            
            <!-- Top K -->
            <el-form-item label="Top K" v-if="currentProviderConfig.advancedParams.topK">
              <el-input-number 
                v-model="modelForm.topK" 
                :min="currentProviderConfig.ranges.topK?.[0] || 1" 
                :max="currentProviderConfig.ranges.topK?.[1] || 100" 
                controls-position="right" 
              />
              <div class="field-hint">每一步考虑的最高概率Token数量</div>
            </el-form-item>
            
            <!-- 频率惩罚 -->
            <el-form-item label="频率惩罚" v-if="currentProviderConfig.advancedParams.frequencyPenalty">
              <div class="slider-with-value">
                <div class="slider-container">
                  <el-slider 
                    v-model="modelForm.frequencyPenalty" 
                    :min="-2" 
                    :max="2" 
                    :step="0.1" 
                  />
                </div>
                <div class="slider-value">{{ modelForm.frequencyPenalty.toFixed(1) }}</div>
              </div>
              <div class="field-hint">减少对重复出现Token的使用</div>
            </el-form-item>
            
            <!-- 存在惩罚 -->
            <el-form-item label="存在惩罚" v-if="currentProviderConfig.advancedParams.presencePenalty">
              <div class="slider-with-value">
                <div class="slider-container">
                  <el-slider 
                    v-model="modelForm.presencePenalty" 
                    :min="-2" 
                    :max="2" 
                    :step="0.1" 
                  />
                </div>
                <div class="slider-value">{{ modelForm.presencePenalty.toFixed(1) }}</div>
              </div>
              <div class="field-hint">减少对已出现主题的重复</div>
            </el-form-item>
            
            <!-- 流式响应 -->
            <el-form-item label="流式响应" v-if="currentProviderConfig.advancedParams.stream">
              <el-switch v-model="modelForm.stream" />
              <div class="field-hint">开启逐字生成响应</div>
            </el-form-item>
            
            <el-form-item label="能力级别">
              <el-select v-model="modelForm.level" class="full-width-select">
                <el-option label="基础" value="basic" />
                <el-option label="高级" value="advanced" />
                <el-option label="超级高级" value="super" />
              </el-select>
            </el-form-item>
          </div>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showAddModel = false">取消</el-button>
          <el-button type="primary" @click="saveModel">
            {{ isEditing ? '保存' : '添加' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- Delete Confirmation -->
    <el-dialog
      v-model="showDeleteConfirm"
      title="确认删除"
      width="30%"
      append-to-body
    >
      <p>确定要删除模型 "{{ modelToDelete?.name }}" 吗？此操作不可恢复。</p>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showDeleteConfirm = false">取消</el-button>
          <el-button type="danger" @click="deleteModel">确认删除</el-button>
        </div>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from '@vue/runtime-core';
import { ElMessage } from 'element-plus';
import { ArrowDown } from '@element-plus/icons-vue';
import { v4 as uuidv4 } from 'uuid';
import type { AIModel } from '@/types/chat';
import { getProviderConfig, getModelOptions, getDefaultBaseUrl } from '@/components/ai-assistant-library/config/providerConfigs';

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

const dialogVisible = computed({
  get: () => props.show,
  set: (value) => emit('update:show', value)
});

// Local copy of models
const models = ref<AIModel[]>([]);

// Advanced options toggle
const showAdvancedOptions = ref(false);

// Form data
const showAddModel = ref(false);
const isEditing = ref(false);
const modelForm = ref({
  id: '',
  name: '',
  type: 'openai',
  apiKey: '',
  baseUrl: '',
  modelName: '',
  temperature: 0.7,
  maxTokens: 2000,
  level: 'basic',
  logo: '',
  topP: 1.0,
  topK: 50,
  frequencyPenalty: 0,
  presencePenalty: 0,
  stream: true,
  // Azure 特有字段
  deploymentName: '',
  apiVersion: '2023-05-15',
  // Mistral 特有字段
  randomSeed: 42
});

// Delete confirmation
const showDeleteConfirm = ref(false);
const modelToDelete = ref<AIModel | null>(null);

// Initialize with props data
watch(() => props.models, (newModels: AIModel[]) => {
  models.value = [...newModels];
}, { immediate: true });

// Model type options
const modelTypeOptions = [
  { label: 'OpenAI', value: 'openai' },
  { label: 'Azure OpenAI', value: 'azure' },
  { label: 'Anthropic (Claude)', value: 'anthropic' },
  { label: 'Google (Gemini)', value: 'google' },
  { label: 'Mistral AI', value: 'mistral' },
  { label: 'DeepSeek', value: 'deepseek' },
  { label: '其他', value: 'other' }
];

// Get readable model type name
function getModelTypeName(type: string): string {
  const option = modelTypeOptions.find(opt => opt.value === type);
  return option ? option.label : type;
}

// Select a model for use
function selectModel(modelId: string) {
  emit('select-model', modelId);
  ElMessage.success('已切换模型');
}

// Open the edit dialog
function editModel(model: AIModel) {
  isEditing.value = true;
  modelForm.value = {
    id: model.id,
    name: model.name,
    type: model.type || 'openai',
    apiKey: model.apiKey || '',
    baseUrl: model.baseUrl || '',
    modelName: model.config?.modelVersion || '',
    temperature: model.temperature || 0.7,
    maxTokens: model.maxTokens || 2000,
    level: model.level || 'basic',
    logo: model.logo,
    topP: model.config?.topP || 1.0,
    topK: model.config?.topK || 50,
    frequencyPenalty: model.config?.frequencyPenalty || 0,
    presencePenalty: model.config?.presencePenalty || 0,
    stream: model.config?.stream !== undefined ? model.config.stream : true,
    deploymentName: '',
    apiVersion: '2023-05-15',
    randomSeed: 42
  };
  showAddModel.value = true;
}

// Open delete confirmation
function confirmDelete(model: AIModel) {
  modelToDelete.value = model;
  showDeleteConfirm.value = true;
}

// Delete a model
function deleteModel() {
  if (!modelToDelete.value) return;
  
  // If deleting current model, switch to another one
  if (modelToDelete.value.id === props.currentModelId) {
    const otherModel = models.value.find((m: AIModel) => m.id !== props.currentModelId);
    if (otherModel) {
      emit('select-model', otherModel.id);
    }
  }
  
  // Remove the model
  models.value = models.value.filter((m: AIModel) => m.id !== modelToDelete.value?.id);
  
  // Update parent
  emit('models-updated', models.value);
  
  showDeleteConfirm.value = false;
  ElMessage.success('模型已删除');
}

// Save model (add or update)
function saveModel() {
  if (!modelForm.value.name.trim()) {
    ElMessage.error('请输入模型名称');
    return;
  }
  
  if (!modelForm.value.apiKey.trim() && modelForm.value.type !== 'local') {
    ElMessage.error('请输入API密钥');
    return;
  }
  
  // 检查特殊字段
  const config = getProviderConfig(modelForm.value.type);
  
  // 检查是否需要填写模型名称
  if (config.basicParams.modelName && !modelForm.value.modelName && modelForm.value.type !== 'azure') {
    ElMessage.error('请输入模型名称');
    return;
  }
  
  // 检查Azure特有字段
  if (modelForm.value.type === 'azure') {
    if (!modelForm.value.deploymentName) {
      ElMessage.error('请输入Azure部署名称');
      return;
    }
    if (!modelForm.value.apiVersion) {
      ElMessage.error('请输入API版本');
      return;
    }
  }
  
  // Determine logo based on model type
  let logo = '/logos/svg/custom.svg';
  switch(modelForm.value.type) {
    case 'openai':
      logo = '/logos/svg/openai.svg';
      break;
    case 'anthropic':
      logo = '/logos/svg/anthropic.svg';
      break;
    case 'google':
      logo = '/logos/svg/google.svg';
      break;
    case 'deepseek':
      logo = '/logos/svg/deepseek.svg';
      break;
    case 'mistral':
      logo = '/logos/svg/mistral.svg';
      break;
    case 'azure':
      logo = '/logos/svg/azure.svg';
      break;
    case 'alibaba':
      logo = '/logos/svg/qwen.svg';
      break;
  }
  
  // 创建ModelConfig对象
  const modelConfig: any = {
    apiKey: modelForm.value.apiKey,
    apiEndpoint: modelForm.value.baseUrl,
    modelVersion: modelForm.value.type === 'azure' ? modelForm.value.deploymentName : modelForm.value.modelName,
    temperature: modelForm.value.temperature,
    maxTokens: modelForm.value.maxTokens,
    topP: modelForm.value.topP,
    stream: modelForm.value.stream
  };
  
  // 根据模型类型添加特定参数
  if (config.advancedParams.topK) {
    modelConfig.topK = modelForm.value.topK;
  }
  
  if (config.advancedParams.frequencyPenalty) {
    modelConfig.frequencyPenalty = modelForm.value.frequencyPenalty;
  }
  
  if (config.advancedParams.presencePenalty) {
    modelConfig.presencePenalty = modelForm.value.presencePenalty;
  }
  
  // 添加Azure特有参数
  if (modelForm.value.type === 'azure') {
    modelConfig.apiVersion = modelForm.value.apiVersion;
  }
  
  // 添加Mistral特有参数
  if (modelForm.value.type === 'mistral' && config.specialParams?.randomSeed) {
    modelConfig.randomSeed = modelForm.value.randomSeed;
  }
  
  const updatedModel: AIModel = {
    id: isEditing.value ? modelForm.value.id : uuidv4(),
    name: modelForm.value.name,
    type: modelForm.value.type,
    apiKey: modelForm.value.apiKey,
    baseUrl: modelForm.value.baseUrl,
    temperature: modelForm.value.temperature,
    maxTokens: modelForm.value.maxTokens,
    level: modelForm.value.level,
    logo: modelForm.value.logo || logo,
    config: modelConfig
  };
  
  if (isEditing.value) {
    // Update existing model
    const index = models.value.findIndex((m: AIModel) => m.id === updatedModel.id);
    if (index >= 0) {
      models.value[index] = updatedModel;
    }
    ElMessage.success('模型已更新');
  } else {
    // Add new model
    models.value.push(updatedModel);
    ElMessage.success('模型已添加');
  }
  
  // Update parent
  emit('models-updated', models.value);
  
  // Reset form
  resetForm();
}

// Reset the form
function resetForm() {
  modelForm.value = {
    id: '',
    name: '',
    type: 'openai',
    apiKey: '',
    baseUrl: getDefaultBaseUrl('openai'),
    modelName: '',
    temperature: 0.7,
    maxTokens: 2000,
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
  isEditing.value = false;
  showAddModel.value = false;
}

// Handle dialog close
function handleClose() {
  resetForm();
}

// Toggle advanced options visibility
function toggleAdvancedOptions() {
  showAdvancedOptions.value = !showAdvancedOptions.value;
}

// 当前选择的提供商配置
const currentProviderConfig = computed(() => {
  return getProviderConfig(modelForm.value.type);
});

// 模型选项列表
const modelOptions = computed(() => {
  return getModelOptions(modelForm.value.type);
});

// 监听model类型变化，更新基础URL和其他默认参数
watch(() => modelForm.value.type, (newType) => {
  const config = getProviderConfig(newType);
  // 更新baseUrl为该提供商的默认值
  if (!isEditing.value || !modelForm.value.baseUrl) {
    modelForm.value.baseUrl = config.defaults.baseUrl;
  }
  
  // 更新温度、最大Token数和其他默认参数
  modelForm.value.temperature = config.defaults.temperature;
  modelForm.value.maxTokens = config.defaults.maxTokens;
  
  // 如果有topP默认值，则更新
  if (config.defaults.topP !== undefined) {
    modelForm.value.topP = config.defaults.topP;
  }
  
  // 如果有topK默认值，则更新
  if (config.defaults.topK !== undefined) {
    modelForm.value.topK = config.defaults.topK;
  }
});
</script>

<style scoped>
.model-management-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
  width: 100%;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.model-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.model-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  transition: all 0.2s;
}

.model-item:hover {
  background-color: #f9fafb;
}

.model-item.is-active {
  border-color: #6366f1;
  background-color: rgba(99, 102, 241, 0.05);
}

.model-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.model-logo {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: contain;
  background-color: #fff;
  padding: 2px;
}

.model-details {
  display: flex;
  flex-direction: column;
}

.model-name {
  font-weight: 500;
  font-size: 15px;
}

.model-type {
  font-size: 13px;
  color: #6b7280;
}

.model-actions {
  display: flex;
  gap: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 12px;
}

.advanced-options-section {
  margin-top: 16px;
  border-top: 1px solid #eee;
  padding-top: 16px;
  width: 100%;
}

.advanced-options-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  cursor: pointer;
  transition: all 0.2s;
}

.advanced-options-header:hover {
  color: #409EFF;
}

.advanced-options-header .advanced-label {
  font-weight: 500;
  font-size: 15px;
  color: #606266;
}

.advanced-options-header .advanced-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-size: 14px;
}

.advanced-options-header .el-icon {
  transition: transform 0.2s;
  font-size: 16px;
  margin-left: 4px;
}

.advanced-options-header .el-icon.is-active {
  transform: rotate(180deg);
}

.advanced-options-content {
  padding: 16px 0;
  border-top: 1px solid #eee;
  animation: fadeIn 0.3s ease;
  width: 100%;
}

.advanced-options-content :deep(.el-form-item) {
  margin-bottom: 24px;
  width: 100%;
}

.advanced-options-content :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

.field-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  width: 100%;
}

.slider-with-value {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.slider-container {
  flex: 1;
  min-width: 200px;
  position: relative;
}

.slider-value {
  min-width: 45px;
  text-align: center;
  font-weight: 500;
  color: #409EFF;
  flex-shrink: 0;
  background-color: #EBF5FF;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 14px;
}

:deep(.el-slider) {
  margin: 0;
  width: 100%;
}

:deep(.el-form-item__content) {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  width: 100%;
}

:deep(.el-form-item__label) {
  padding-bottom: 8px;
}

:deep(.el-dialog) {
  max-width: 90vw;
}

:deep(.el-dialog__header) {
  padding-bottom: 16px;
}

:deep(.el-dialog__body) {
  padding: 20px 24px;
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-input-number .el-input__inner) {
  text-align: left;
}

:deep(.el-input), :deep(.el-select) {
  width: 100%;
}

.full-width-select {
  width: 100% !important;
}

.model-option-item {
  display: flex;
  flex-direction: column;
}

.model-option-name {
  font-weight: 500;
}

.model-option-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

/* 嵌套对话框样式优化 */
:deep(.el-dialog__wrapper + .el-dialog__wrapper) .el-dialog {
  max-width: 600px;
  margin: 0 auto !important;
}

/* 滑块样式优化 */
:deep(.el-slider__runway) {
  margin: 15px 0;
  height: 6px;
  background-color: #E4E7ED;
}

:deep(.el-slider__bar) {
  height: 6px;
  background-color: #409EFF;
}

:deep(.el-slider__button-wrapper) {
  height: 36px;
  width: 36px;
  top: -15px;
}

:deep(.el-slider__button) {
  width: 16px;
  height: 16px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}
</style> 