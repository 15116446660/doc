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
          :class="{ 'is-active': currentModelId === model.id, 'is-default': model.isDefault }"
          @click="selectModel(model.id)"
        >
          <div class="model-info">
            <AIModelLogo 
              :model="model"
              size="small"
              class="model-logo"
            />
            <div class="model-details">
              <div class="model-name">
                {{ model.name }}
                <el-tag v-if="model.isDefault" type="success" size="small" class="default-tag">默认</el-tag>
              </div>
              <div class="model-type">{{ model.model }}</div>
            </div>
          </div>
          <div class="model-actions">
            <el-button 
              type="info" 
              size="small" 
              plain 
              @click.stop="handleEdit(model)"
            >
              编辑
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              plain 
              @click.stop="confirmDelete(model)"
              v-if="!model.isDefault"
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
        <el-form-item label="模型名称" required>
          <el-input v-model="modelForm.name" placeholder="例如：我的GPT-4" />
        </el-form-item>
        
        <el-form-item label="模型标识" required>
          <el-input v-model="modelForm.model" placeholder="例如：gpt-4-turbo" />
        </el-form-item>
        
        <el-form-item label="API密钥" required>
          <el-input v-model="modelForm.apiKey" placeholder="输入API密钥" show-password />
        </el-form-item>
        
        <el-form-item label="API URL" required>
          <el-input v-model="modelForm.apiUrl" placeholder="输入API基础URL" />
        </el-form-item>
        
        <el-form-item label="提示词" required>
          <el-input 
            type="textarea"
            :rows="4"
            v-model="modelForm.cueWord" 
            placeholder="配置该模型的默认提示词" 
          />
          </el-form-item>
          
        <el-form-item label="默认模型">
           <el-switch v-model="modelForm.isDefault" />
           <div class="field-hint">是否将此模型作为默认使用模型</div>
          </el-form-item>
        
        <div class="advanced-options-section">
          <div class="advanced-options-header" @click="showAdvancedOptions = !showAdvancedOptions">
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
                  <el-slider v-model="modelForm.temperature" :min="0" :max="2" :step="0.1" />
                  <div class="field-hint">控制响应的随机性，较高的值会产生更多样化的回答</div>
                </div>
              </el-form-item>
              
              <el-form-item label="最大输出">
                <div class="control-wrapper">
                  <el-input-number v-model="modelForm.maxTokens" :min="1" controls-position="right" />
                  <div class="field-hint">模型最大生成的Token数</div>
                </div>
              </el-form-item>
              
              <el-form-item label="Top P">
                <div class="control-wrapper">
                  <el-slider v-model="modelForm.topP" :min="0" :max="1" :step="0.05" />
                  <div class="field-hint">控制生成多样性的核采样阈值</div>
                </div>
              </el-form-item>
              
              <el-form-item label="频率惩罚">
                <div class="control-wrapper">
                      <el-slider v-model="modelForm.frequencyPenalty" :min="-2" :max="2" :step="0.1" />
                  <div class="field-hint">减少对重复出现Token的使用</div>
                </div>
              </el-form-item>
              
              <el-form-item label="存在惩罚">
                <div class="control-wrapper">
                      <el-slider v-model="modelForm.presencePenalty" :min="-2" :max="2" :step="0.1" />
                  <div class="field-hint">减少对已出现主题的重复</div>
                </div>
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
import { ref, watch, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { Close, ArrowDown } from '@element-plus/icons-vue';
import type { AIModel } from '@/types/chat';
import AIModelLogo from './AIModelLogo.vue';

defineProps<{
  currentModelId?: string;
  models: AIModel[];
}>();

const emit = defineEmits<{
  (e: 'select-model', modelId: string): void;
  (e: 'add-model', model: Omit<AIModel, 'id'>): void;
  (e: 'update-model', model: Partial<AIModel> & { id: string }): void;
  (e: 'delete-model', modelId: string): void;
  (e: 'set-default-model', modelId: string): void;
  (e: 'form-visibility-change', isVisible: boolean): void;
}>();

const isFormVisible = ref(false);
const isEditing = ref(false);
const showDeleteConfirm = ref(false);
const modelToDelete = ref<AIModel | null>(null);
const showAdvancedOptions = ref(false);

const initialFormState = (): Omit<AIModel, 'id'> => ({
  name: '',
  model: '',
  apiKey: '',
  apiUrl: '',
  cueWord: '',
  isDefault: false,
  temperature: 0.7,
  maxTokens: 4096,
  topP: 1.0,
  frequencyPenalty: 0,
  presencePenalty: 0,
});

const modelForm = reactive(initialFormState());

watch(isFormVisible, (newValue) => {
  emit('form-visibility-change', newValue);
});

function closeForm() {
  isFormVisible.value = false;
  isEditing.value = false;
  Object.assign(modelForm, initialFormState());
}

function handleAdd() {
  isEditing.value = false;
  Object.assign(modelForm, initialFormState());
  isFormVisible.value = true;
}

function handleEdit(model: AIModel) {
  isEditing.value = true;
  Object.assign(modelForm, JSON.parse(JSON.stringify(model)));
  isFormVisible.value = true;
}

function selectModel(modelId: string) {
  emit('select-model', modelId);
}

function saveModel() {
  if (!modelForm.name || !modelForm.model || !modelForm.apiKey || !modelForm.apiUrl || !modelForm.cueWord) {
    ElMessage.warning('请填写所有必填项');
    return;
  }

  if (isEditing.value) {
    // @ts-ignore
    emit('update-model', { ...modelForm });
  } else {
    emit('add-model', { ...modelForm });
  }
  closeForm();
}

function confirmDelete(model: AIModel) {
  modelToDelete.value = model;
  showDeleteConfirm.value = true;
}

function deleteModel() {
  if (modelToDelete.value) {
    emit('delete-model', modelToDelete.value.id);
  showDeleteConfirm.value = false;
    modelToDelete.value = null;
    if (isFormVisible.value && !isEditing.value) {
    closeForm();
  }
}
}
</script>

<style scoped>
.model-management-wrapper {
  display: flex;
  transition: all 0.3s ease-in-out;
}
.list-panel {
  width: 100%;
  padding-right: 20px;
  transition: width 0.3s ease-in-out;
}
.show-form .list-panel {
  width: 50%;
}
.form-panel {
  width: 50%;
  border-left: 1px solid #e0e0e0;
  padding-left: 20px;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
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
  padding: 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}
.model-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
.model-item.is-active {
  border-color: #409eff;
  background-color: #ecf5ff;
}
.model-info {
  display: flex;
  align-items: center;
  gap: 12px;
}
.model-logo {
  width: 24px;
  height: 24px;
  margin-right: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.model-name {
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
}
.model-type {
  font-size: 12px;
  color: #909399;
}
.default-tag {
  height: 20px;
  padding: 0 6px;
  line-height: 18px;
}
.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 22px;
}
.form-header h3 {
  margin: 0;
}
.model-form {
  padding-right: 10px;
}
.field-hint {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}
.form-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}
.advanced-options-section {
  border-top: 1px solid #e0e0e0;
  margin-top: 22px;
  padding-top: 18px;
}
.advanced-options-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  font-weight: 500;
  margin-bottom: 12px;
}
.advanced-toggle {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #909399;
  font-size: 14px;
}
.advanced-toggle .el-icon {
  transition: transform 0.3s;
}
.advanced-toggle .el-icon.is-active {
  transform: rotate(180deg);
}
.control-wrapper .el-slider {
  flex-grow: 1;
}
.control-wrapper {
  width: 100%;
}
</style> 