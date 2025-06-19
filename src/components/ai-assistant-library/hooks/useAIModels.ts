import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getModelList, addModel, updateModel, deleteModel as apiDeleteModel } from '@/api/aiModel'
import type { AIModel } from '@/types/chat'

/**
 * AI模型管理的核心逻辑封装 - API版本
 */
export function useAIModels() {
  const models = ref<AIModel[]>([])
  const currentModelId = ref<string>('')
  const loading = ref<boolean>(false)

  const currentModel = computed<AIModel | undefined>(() => {
    return models.value.find(model => model.id === currentModelId.value)
  })

  const defaultModel = computed<AIModel | undefined>(() => {
    return models.value.find(model => model.isDefault) || models.value[0]
  })

  async function loadModels(): Promise<void> {
    if (loading.value) return
    loading.value = true
    try {
      const response = await getModelList()
      models.value = response
      
      const savedModelId = localStorage.getItem('currentModelId')
      if (savedModelId && models.value.some(model => model.id === savedModelId)) {
        currentModelId.value = savedModelId
      } else if (defaultModel.value) {
        currentModelId.value = defaultModel.value.id
        saveCurrentModelId()
      }
    } catch (error: any) {
      ElMessage.error('加载模型列表失败: ' + (error.message || '未知错误'))
    } finally {
      loading.value = false
    }
  }

  function selectModel(modelId: string): void {
    const model = models.value.find(m => m.id === modelId)
    if (model) {
      currentModelId.value = modelId
      saveCurrentModelId()
    }
  }

  function saveCurrentModelId(): void {
    localStorage.setItem('currentModelId', currentModelId.value)
  }

  async function addModelAndUpdate(modelData: Omit<AIModel, 'id' | 'isDefault'> & { isDefault?: boolean }): Promise<void> {
    try {
      const modelToAdd = {
        ...modelData,
        isDefault: modelData.isDefault || false,
      };
      await addModel(modelToAdd);
      ElMessage.success('模型添加成功');
      await loadModels(); // 重新加载列表
    } catch (error: any) {
      ElMessage.error('添加模型失败: ' + (error.message || '未知错误'));
    }
  }

  async function updateModelAndUpdate(modelData: Partial<AIModel> & { id: string }): Promise<void> {
    try {
      await updateModel(modelData);
      ElMessage.success('模型更新成功');
      await loadModels(); // 重新加载列表
    } catch (error: any) {
      ElMessage.error('更新模型失败: ' + (error.message || '未知错误'));
    }
  }

  async function deleteModel(modelId: string): Promise<void> {
    try {
      await apiDeleteModel(modelId);
      ElMessage.success('模型删除成功');
      await loadModels(); // 重新加载列表
    } catch (error: any) {
      ElMessage.error('删除模型失败: ' + (error.message || '未知错误'));
    }
  }
  
  async function setDefaultModel(modelId: string): Promise<void> {
    const model = models.value.find(m => m.id === modelId)
    if (!model) return;

    try {
        // 先将之前的默认模型（如果有）更新为非默认
        const currentDefault = models.value.find(m => m.isDefault && m.id !== modelId);
        if (currentDefault) {
            await updateModel({ id: currentDefault.id, isDefault: false });
        }
        
        // 设置新的默认模型
        await updateModel({ id: modelId, isDefault: true });
        ElMessage.success('默认模型设置成功');
        await loadModels();
    } catch (error: any) {
        ElMessage.error('设置默认模型失败: ' + (error.message || '未知错误'));
    }
  }
  
  function updateModels(newModels: AIModel[]): void {
    models.value = newModels;
  }

  onMounted(() => {
    loadModels()
  })
  
  return {
    models,
    currentModelId,
    loading,
    currentModel,
    defaultModel,
    loadModels,
    selectModel,
    addModel: addModelAndUpdate,
    updateModel: updateModelAndUpdate,
    deleteModel,
    setDefaultModel,
    updateModels
  }
} 