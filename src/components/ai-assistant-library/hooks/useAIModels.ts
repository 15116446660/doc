import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getModels } from '@/api/chat'
import type { AIModel } from '@/types/chat'

/**
 * AI模型管理的核心逻辑封装
 */
export function useAIModels() {
  // 模型列表
  const models = ref<AIModel[]>([])
  
  // 当前选择的模型ID
  const currentModelId = ref<string>('')
  
  // 是否正在加载
  const loading = ref<boolean>(false)
  
  // 计算属性：当前选择的模型
  const currentModel = computed<AIModel | undefined>(() => {
    return models.value.find(model => model.id === currentModelId.value)
  })
  
  // 计算属性：默认模型
  const defaultModel = computed<AIModel | undefined>(() => {
    return models.value.find(model => model.isDefault) || models.value[0]
  })
  
  /**
   * 加载模型列表
   */
  async function loadModels(): Promise<void> {
    if (loading.value) return
    
    loading.value = true
    
    try {
      const response = await getModels()
      models.value = response
      
      // 检查从 localStorage 加载的 ID 是否有效
      const savedModelId = localStorage.getItem('currentModelId');
      if (savedModelId && models.value.some(model => model.id === savedModelId)) {
        currentModelId.value = savedModelId;
      } else if (defaultModel.value) {
        // 否则，回退到默认模型
        currentModelId.value = defaultModel.value.id
        saveCurrentModelId(); // 如果回退到默认模型，也保存一下
      }
    } catch (error: any) {
      console.error('加载模型列表失败:', error)
      ElMessage.error('加载模型列表失败: ' + (error.message || '未知错误'))
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 选择模型
   */
  function selectModel(modelId: string): void {
    const model = models.value.find(m => m.id === modelId)
    if (!model) {
      ElMessage.error('模型不存在')
      return
    }
    
    currentModelId.value = modelId
    saveCurrentModelId()
  }
  
  /**
   * 保存当前选择的模型ID到本地存储
   */
  function saveCurrentModelId(): void {
    localStorage.setItem('currentModelId', currentModelId.value)
  }
  
  /**
   * 从本地存储加载当前选择的模型ID
   */
  function loadCurrentModelId(): void {
    const savedModelId = localStorage.getItem('currentModelId')
    if (savedModelId) {
      currentModelId.value = savedModelId
    }
  }
  
  /**
   * 添加自定义模型
   */
  function addCustomModel(model: Omit<AIModel, 'id'>): string {
    // 生成唯一ID
    const id = `custom-${Date.now()}`
    
    // 创建新模型
    const newModel: AIModel = {
      id,
      ...model
    }
    
    // 添加到模型列表
    models.value.push(newModel)
    
    // 保存到本地存储
    saveModels()
    
    return id
  }
  
  /**
   * 更新模型
   */
  function updateModel(modelId: string, updates: Partial<AIModel>): boolean {
    const index = models.value.findIndex(model => model.id === modelId)
    if (index === -1) return false
    
    // 更新模型
    models.value[index] = {
      ...models.value[index],
      ...updates
    }
    
    // 保存到本地存储
    saveModels()
    
    return true
  }
  
  /**
   * 删除模型
   */
  function deleteModel(modelId: string): boolean {
    // 不允许删除默认模型
    const model = models.value.find(m => m.id === modelId)
    if (!model || model.isDefault) return false
    
    // 从列表中删除
    models.value = models.value.filter(m => m.id !== modelId)
    
    // 如果删除的是当前选择的模型，则切换到默认模型
    if (currentModelId.value === modelId && defaultModel.value) {
      currentModelId.value = defaultModel.value.id
      saveCurrentModelId()
    }
    
    // 保存到本地存储
    saveModels()
    
    return true
  }
  
  /**
   * 设置默认模型
   */
  function setDefaultModel(modelId: string): boolean {
    // 取消所有模型的默认状态
    models.value.forEach(model => {
      model.isDefault = false
    })
    
    // 设置新的默认模型
    const model = models.value.find(m => m.id === modelId)
    if (!model) return false
    
    model.isDefault = true
    
    // 保存到本地存储
    saveModels()
    
    return true
  }
  
  /**
   * 保存模型列表到本地存储
   */
  function saveModels(): void {
    localStorage.setItem('aiModels', JSON.stringify(models.value))
  }
  
  /**
   * 更新整个模型列表
   */
  function updateModels(newModels: AIModel[]): void {
    models.value = newModels;
  }
  
  /**
   * 从本地存储加载模型列表
   */
  function loadLocalModels(): void {
    const savedModels = localStorage.getItem('aiModels')
    if (savedModels) {
      try {
        models.value = JSON.parse(savedModels)
      } catch (error) {
        console.error('解析本地模型数据失败:', error)
      }
    }
  }
  
  // 监听模型列表变化，自动保存
  watch(models, () => {
    saveModels()
  }, { deep: true })
  
  // 组件挂载时加载数据
  onMounted(() => {
    // 先尝试从本地加载
    loadLocalModels()
    loadCurrentModelId()
    
    // 然后从API加载最新数据
    loadModels()
  })
  
  return {
    // 状态
    models,
    currentModelId,
    loading,
    
    // 计算属性
    currentModel,
    defaultModel,
    
    // 方法
    loadModels,
    selectModel,
    addCustomModel,
    updateModel,
    updateModels,
    deleteModel,
    setDefaultModel
  }
} 