import { get, post } from './request'
import type { PaginationResponse, QueryParams, ApiResponse } from './types'
import type { ProjectMember, Department, CompanyUser } from '@/types/document'

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
  logo?: string
  leaderAvatar?: string
}

// 获取项目分类
export function getProjectCategories(): Promise<ApiResponse<ProjectCategory[]>> {
  return get('/api/project/categories')
}

// 获取项目列表
export function getProjectList(params: ProjectListParams) {
  return get<ProjectListResponse>('/api/project/list', params)
}

// 获取项目状态选项
export const getProjectStatusOptions = () => {
  return get('/api/project/status-options')
}

// 获取项目风险选项
export const getProjectRiskOptions = () => {
  return get('/api/project/risk-options')
}

// 获取项目分类选项
export const getProjectCategoryOptions = () => {
  return get('/api/project/category-options')
}

// 获取团队成员选项
export const getTeamMemberOptions = () => {
  return get('/api/project/team-member-options')
}

// 获取技术团队选项
export const getTechnicalTeamOptions = () => {
  return get('/api/project/technical-team-options')
}

// 获取商务团队选项
export const getBusinessTeamOptions = () => {
  return get('/api/project/business-team-options')
}

// 获取法务团队选项
export const getLegalTeamOptions = () => {
  return get('/api/project/legal-team-options')
}

// 获取外部专家选项
export const getExternalExpertOptions = () => {
  return get('/api/project/external-expert-options')
}

// 获取资质要求选项
export const getQualificationOptions = () => {
  return get('/api/project/qualification-options')
}

// 获取项目标签选项
export const getProjectTagOptions = () => {
  return get('/api/project/tag-options')
}

// 创建项目（模拟post，实际开发请用axios.post）
export const createProject = (data: any) => {
  return get('/api/project/create', data)
}

// 更新项目（模拟put，实际开发请用axios.put）
export const updateProject = (id: number, data: any) => {
  return get(`/api/project/update?id=${id}`, data)
}

// 获取项目详情
export const getProjectDetail = (id: number) => {
  return get<Project>(`/api/project/detail?id=${id}`)
}

// 获取项目成员
export function getProjectMembers(projectId: string | number) {
  return get<ProjectMember[]>(`/api/project/members?projectId=${projectId}`)
}

// 获取所有部门
export function getDepartments() {
  return get<Department[]>('/api/departments')
}

// 获取公司所有用户（分页）
export interface UserQueryParams extends QueryParams {
  name?: string
  depId?: string
  position?: string
}
export function getUsers(params: UserQueryParams) {
  return get<PaginationResponse<CompanyUser>>('/api/users', params)
}

// 添加项目成员
export function addProjectMembers(projectId: string | number, userIds: number[]) {
  return post(`/api/project/members?projectId=${projectId}`, { userIds })
}

// 保存项目所有成员
export function saveProjectMembers(projectId: string | number, userIds: number[]) {
  return post(`/api/project/members/save?projectId=${projectId}`, { userIds })
}

// 删除项目成员
export function removeProjectMember(projectId: string | number, userId: number) {
  return post(`/api/project/members/delete?projectId=${projectId}&userId=${userId}`)
}

// 更新项目成员角色
export function updateProjectMember(projectId: string | number, userId: number, identity: string) {
  return post(`/api/project/members/update?projectId=${projectId}&userId=${userId}`, { identity })
} 