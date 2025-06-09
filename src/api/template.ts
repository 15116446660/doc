import { get, post, put, del } from './request'
import type { OptionItem } from '@/components/BaseList/types'

export interface Template {
  id: string
  title: string
  categoryName: string
  status: string
  owner: string
  ownerAvatar?: string
  version: string
  description?: string
  content?: string
  createTime: string
  updateTime: string
  downloads?: number
  favorite?: boolean
  rating?: number
  templateFile?: string
  templateType?: string
  templateCode?: string
  applicableScope?: string
  suffixCode?: string
  standardType?: string
  revisionContent?: string
  alias?: string
}

export interface TemplateQueryParams {
  keyword?: string
  category?: string
  status?: string
  dateRange?: [string, string]
  pageNum: number
  pageSize: number
}

export interface TemplateListResponse {
  total: number
  list: Template[]
}

// 获取模板列表
export function getTemplateList(params: TemplateQueryParams) {
  return get<TemplateListResponse>('/api/template/list', params)
}

// 获取模板分类选项
export function getTemplateCategoryOptions() {
  return get<OptionItem[]>('/api/templates/categories')
}

// 获取模板状态选项
export function getTemplateStatusOptions() {
  return get<OptionItem[]>('/api/templates/statuses')
}

// 搜索用户
export function searchUsers(keyword: string) {
  return get<OptionItem[]>('/api/users/search', { keyword })
}

// 创建模板
export function createTemplate(data: Partial<Template>) {
  return post<Template>('/api/templates', data)
}

// 更新模板
export function updateTemplate(id: string, data: Partial<Template>) {
  return put<Template>(`/api/templates/${id}`, data)
}

// 删除模板
export function deleteTemplate(id: string) {
  return del<void>(`/api/templates/${id}`)
}

// 复制模板
export function copyTemplate(id: string) {
  return post<Template>(`/api/templates/${id}/copy`)
}

// 获取模板详情
export function getTemplate(id: string) {
  return get<Template>(`/api/templates/${id}`)
}

// 获取模板类型选项
export function getTemplateTypeOptions() {
  return get<OptionItem[]>('/api/templates/types')
}

// 获取负责人选项
export function getOwnerOptions() {
  return get<OptionItem[]>('/api/templates/owners')
} 