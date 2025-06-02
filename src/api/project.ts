import { get } from './request'
import type { PaginationResponse, QueryParams } from './types'

// 项目分类接口
export interface ProjectCategory {
  id: number
  name: string
  icon?: string
  children?: ProjectCategory[]
}

// 项目列表请求参数
export interface ProjectListParams extends QueryParams {
  categoryId?: string
  status?: string
  risk?: string
  keyword?: string
  dateRange?: [Date, Date] | null
  leader?: string
}

// 项目列表响应数据
export type ProjectListResponse = PaginationResponse<Project>

// 项目数据接口
export interface Project {
  id: number
  title: string
  description: string
  status: string
  statusClass: string
  statusColor: string
  priority: string
  priorityClass: string
  priorityColor: string
  progress: number
  progressColor: string
  date: string
  dueDate: string
  budget: number
  client: string
  categoryId: string
  categoryName: string
  risk: string
  team: string
  members: string[]
  tags: string[]
  documents: number
}

// 获取项目分类
export function getProjectCategories() {
  return get<ProjectCategory[]>('/api/project/categories')
}

// 获取项目列表
export function getProjectList(params: ProjectListParams) {
  return get<ProjectListResponse>('/api/project/list', params)
} 