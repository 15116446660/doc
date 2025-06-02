import axios from 'axios'

// 项目分类接口
export interface ProjectCategory {
  id: number
  name: string
  icon?: string
  children?: ProjectCategory[]
}

// 项目列表请求参数
export interface ProjectListParams {
  page?: number
  limit?: number
  categoryId?: string
  status?: string
  risk?: string
  keyword?: string
  dateRange?: [Date, Date] | null
  leader?: string
}

// 项目列表响应数据
export interface ProjectListResponse {
  list: Project[]
  total: number
  page: number
  limit: number
}

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
  return axios.get<{code: number, data: ProjectCategory[]}>('/api/project/categories')
}

// 获取项目列表
export function getProjectList(params: ProjectListParams) {
  return axios.get<{code: number, data: ProjectListResponse}>('/api/project/list', { params })
} 