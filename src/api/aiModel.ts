/**
 * AI模型相关API
 */
import { get, post, del } from './request'
import type { AIModel } from '@/types/chat'

// API返回的模型原始数据结构
interface RawAIModel {
  id: string
  apiKey: string
  apiUrl: string
  model: string
  modelName: string
  isDefault: 'Y' | 'N'
  cueWord: string
  temperature?: number
  maxTokens?: number
  topP?: number
  frequencyPenalty?: number
  presencePenalty?: number
}

// 将API返回的原始数据转换为前端使用的AIModel格式
function transformRawToAIModel(raw: RawAIModel): AIModel {
  return {
    ...raw,
    name: raw.modelName,
    isDefault: raw.isDefault === 'Y'
  }
}

// 将前端的AIModel格式转换为API需要的原始数据格式
function transformAIModelToRaw(model: Partial<Omit<AIModel, 'id'>>): Omit<Partial<RawAIModel>, 'id'> {
  const { name, isDefault, ...rest } = model
  const raw: Omit<Partial<RawAIModel>, 'id'> & { modelName?: string } = { ...rest }
  
  if (name) {
    raw.modelName = name
  }

  if (isDefault !== undefined) {
    raw.isDefault = isDefault ? 'Y' : 'N'
  }
  
  return raw as Omit<Partial<RawAIModel>, 'id'>
}

/**
 * 获取AI模型列表（未分页）
 */
export async function getModelList(params?: { modelName?: string, model?: string }): Promise<AIModel[]> {
  const rawModels = await get<RawAIModel[]>('/aiModel/selectList', { params })
  return rawModels.map(transformRawToAIModel)
}

/**
 * 新增AI模型配置
 */
export function addModel(data: Omit<AIModel, 'id'>) {
  const rawData = transformAIModelToRaw(data)
  return post('/aiModel/addAiModel', rawData)
}

/**
 * 修改AI模型配置
 */
export function updateModel(data: Partial<AIModel> & { id: string }) {
  const { id, ...rest } = data
  const rawData = transformAIModelToRaw(rest)
  return post('/aiModel/updateAiModel', { ...rawData, id })
}

/**
 * 删除AI模型配置
 */
export function deleteModel(id: string) {
  return del(`/aiModel/delAiModel/${id}`)
}