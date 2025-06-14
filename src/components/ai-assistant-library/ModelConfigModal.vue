<template>
  <el-dialog
    title="模型配置"
    v-model="dialogVisible"
    width="600px"
    @close="$emit('close')"
  >
    <div class="model-config-content">
      <!-- 模型选择器 -->
      <div class="model-selector">
        <el-select v-model="selectedModelId" placeholder="选择模型" style="width: 100%">
          <el-option
            v-for="model in props.models"
            :key="model.id"
            :label="model.name"
            :value="model.id"
          />
          <el-option value="new" label="+ 添加新模型" />
        </el-select>
      </div>
      
      <!-- 模型表单 -->
      <el-form :model="modelForm" label-position="top" class="model-form">
        <!-- 基本信息 -->
        <el-form-item label="模型名称" required>
          <el-input v-model="modelForm.name" placeholder="请输入模型名称" />
        </el-form-item>
        
        <el-form-item label="描述">
          <el-input v-model="modelForm.description" type="textarea" rows="2" placeholder="请输入模型描述" />
        </el-form-item>
        
        <el-form-item label="API端点">
          <el-input v-model="modelForm.apiEndpoint" placeholder="请输入API端点" />
        </el-form-item>
        
        <el-form-item label="系统提示词">
          <el-input v-model="modelForm.systemPrompt" type="textarea" rows="3" placeholder="请输入系统提示词" />
        </el-form-item>
        
        <!-- 参数设置 -->
        <el-divider>参数设置</el-divider>
        
        <el-form-item label="温度 (Temperature)">
          <el-slider
            v-model="modelForm.parameters!.temperature"
            :min="0"
            :max="2"
            :step="0.1"
            show-input
          />
          <div class="parameter-description">
            较低的值使输出更确定，较高的值使输出更随机和创造性
          </div>
        </el-form-item>
        
        <el-form-item label="最大令牌数 (Max Tokens)">
          <el-input-number
            v-model="modelForm.parameters!.maxTokens"
            :min="1"
            :max="32000"
            :step="100"
            style="width: 100%"
          />
          <div class="parameter-description">
            生成回复的最大长度
          </div>
        </el-form-item>
        
        <el-form-item label="Top P">
          <el-slider
            v-model="modelForm.parameters!.topP"
            :min="0"
            :max="1"
            :step="0.05"
            show-input
          />
          <div class="parameter-description">
            控制模型考虑的词汇范围，较低的值使输出更聚焦
          </div>
        </el-form-item>
        
        <el-form-item label="频率惩罚 (Frequency Penalty)">
          <el-slider
            v-model="modelForm.parameters!.frequencyPenalty"
            :min="0"
            :max="2"
            :step="0.1"
            show-input
          />
          <div class="parameter-description">
            减少模型重复使用相同词语的倾向
          </div>
        </el-form-item>
        
        <el-form-item label="存在惩罚 (Presence Penalty)">
          <el-slider
            v-model="modelForm.parameters!.presencePenalty"
            :min="0"
            :max="2"
            :step="0.1"
            show-input
          />
          <div class="parameter-description">
            减少模型重复讨论相同主题的倾向
          </div>
        </el-form-item>
        
        <el-form-item>
          <el-checkbox v-model="modelForm.isDefault">设为默认模型</el-checkbox>
        </el-form-item>
      </el-form>
    </div>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="$emit('close')">取消</el-button>
        <el-button type="primary" @click="saveModel">保存</el-button>
        <el-button 
          v-if="selectedModelId !== 'new' && !isDefaultModel"
          type="danger" 
          @click="confirmDelete"
        >
          删除
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessageBox } from 'element-plus'
import type { AIModel } from '@/types/chat'

// 定义组件属性
const props = defineProps<{
  models: AIModel[]
  currentModelId: string
}>()

// 定义事件
const emit = defineEmits<{
  (e: 'save', model: AIModel): void
  (e: 'delete', modelId: string): void
  (e: 'close'): void
}>()

// 状态
const dialogVisible = ref(true)
const selectedModelId = ref(props.currentModelId)

// 模型表单
const modelForm = reactive<AIModel>({
  id: '',
  name: '',
  description: '',
  apiEndpoint: '',
  systemPrompt: '',
  parameters: {
    temperature: 0.7,
    maxTokens: 2000,
    topP: 1,
    frequencyPenalty: 0,
    presencePenalty: 0
  },
  isDefault: false
})

// 计算属性：是否为默认模型
const isDefaultModel = computed(() => {
  return props.models.some(model => model.id === selectedModelId.value && model.isDefault)
})

// 监听选择的模型变化，更新表单
watch(selectedModelId, (newModelId) => {
  if (newModelId === 'new') {
    // 创建新模型，使用默认值
    Object.assign(modelForm, {
      id: '',
      name: '新模型',
      description: '',
      apiEndpoint: '',
      systemPrompt: '',
      parameters: {
        temperature: 0.7,
        maxTokens: 2000,
        topP: 1,
        frequencyPenalty: 0,
        presencePenalty: 0
      },
      isDefault: false
    })
  } else {
    // 编辑现有模型，复制值
    const selectedModel = props.models.find(model => model.id === newModelId)
    if (selectedModel) {
      // 深拷贝模型数据
      const modelCopy = JSON.parse(JSON.stringify(selectedModel))
      
      // 确保parameters对象存在
      if (!modelCopy.parameters) {
        modelCopy.parameters = {
          temperature: 0.7,
          maxTokens: 2000,
          topP: 1,
          frequencyPenalty: 0,
          presencePenalty: 0
        }
      }
      
      Object.assign(modelForm, modelCopy)
    }
  }
}, { immediate: true })

// 保存模型
function saveModel() {
  // 验证表单
  if (!modelForm.name.trim()) {
    ElMessageBox.alert('请输入模型名称', '提示')
    return
  }
  
  // 准备保存的模型数据
  const modelToSave: AIModel = {
    ...modelForm,
    id: selectedModelId.value === 'new' ? `model-${Date.now()}` : modelForm.id
  }
  
  // 触发保存事件
  emit('save', modelToSave)
}

// 确认删除模型
function confirmDelete() {
  ElMessageBox.confirm('确定要删除这个模型吗？此操作不可恢复。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    emit('delete', selectedModelId.value)
  }).catch(() => {})
}
</script>

<style scoped>
.model-config-content {
  max-height: 500px;
  overflow-y: auto;
  padding-right: 10px;
}

.model-selector {
  margin-bottom: 20px;
}

.model-form {
  margin-top: 16px;
}

.parameter-description {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 