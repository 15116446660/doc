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

// ==================== 项目层级管理 API ====================

// 部门管理接口
export interface Department {
  id: number
  name: string
  code?: string
  description?: string
  managerId?: number
  managerName?: string
  parentId?: number
  sortOrder: number
  enabled: boolean
  level: number
  path?: string
  children?: Department[]
  createdAt?: string
  updatedAt?: string
}

// 项目品类接口
export interface ProjectCategoryEntity {
  id: number
  name: string
  code?: string
  description?: string
  departmentId: number
  managerId?: number
  managerName?: string
  sortOrder: number
  enabled: boolean
  icon?: string
  color?: string
  children?: ProjectSubcategoryEntity[]
  createdAt?: string
  updatedAt?: string
}

// 项目子品类接口
export interface ProjectSubcategoryEntity {
  id: number
  name: string
  code?: string
  description?: string
  categoryId: number
  managerId?: number
  managerName?: string
  sortOrder: number
  enabled: boolean
  icon?: string
  color?: string
  projects?: Project[]
  createdAt?: string
  updatedAt?: string
}

// 项目层级树节点接口
export interface ProjectHierarchyTreeNode {
  id: number
  name: string
  code?: string
  description?: string
  nodeType: 'department' | 'category' | 'subcategory' | 'project'
  parentId?: number
  sortOrder: number
  enabled: boolean
  icon?: string
  color?: string
  managerId?: number
  managerName?: string
  level: number
  path?: string
  children?: ProjectHierarchyTreeNode[]
  childrenCount?: number
  hasChildren?: boolean
  expanded?: boolean
  selected?: boolean
  // 项目特有属性
  projectStatus?: string
  projectPriority?: string
  projectProgress?: number
  projectBudget?: number
  tags?: string
  plannedStartTime?: string
  plannedEndTime?: string
  actualStartTime?: string
  actualEndTime?: string
  createdAt?: string
  updatedAt?: string
}

// ==================== 部门管理 API ====================

// 获取部门树形结构
export function getDepartmentTree() {
  return get<Department[]>('/api/departments/tree')
}

// 获取根部门列表
export function getRootDepartments() {
  return get<Department[]>('/api/departments/roots')
}

// 创建部门
export function createDepartment(data: Partial<Department>) {
  return post<Department>('/api/departments', data)
}

// 更新部门
export function updateDepartment(id: number, data: Partial<Department>) {
  return post<Department>(`/api/departments/${id}`, data)
}

// 删除部门
export function deleteDepartment(id: number) {
  return post(`/api/departments/${id}`)
}

// 移动部门
export function moveDepartment(id: number, newParentId?: number) {
  return post<Department>(`/api/departments/${id}/move`, { newParentId })
}

// 更新部门排序
export function updateDepartmentSort(id: number, sortOrder: number) {
  return post(`/api/departments/${id}/sort`, { sortOrder })
}

// ==================== 品类管理 API ====================

// 根据部门ID获取品类列表
export function getCategoriesByDepartment(departmentId: number) {
  return get<ProjectCategoryEntity[]>(`/api/project-categories/department/${departmentId}`)
}

// 创建品类
export function createProjectCategory(data: Partial<ProjectCategoryEntity>) {
  return post<ProjectCategoryEntity>('/api/project-categories', data)
}

// 更新品类
export function updateProjectCategory(id: number, data: Partial<ProjectCategoryEntity>) {
  return post<ProjectCategoryEntity>(`/api/project-categories/${id}`, data)
}

// 删除品类
export function deleteProjectCategory(id: number) {
  return post(`/api/project-categories/${id}`)
}

// 更新品类排序
export function updateCategorySort(id: number, sortOrder: number) {
  return post(`/api/project-categories/${id}/sort`, { sortOrder })
}

// ==================== 子品类管理 API ====================

// 根据品类ID获取子品类列表
export function getSubcategoriesByCategory(categoryId: number) {
  return get<ProjectSubcategoryEntity[]>(`/api/project-subcategories/category/${categoryId}`)
}

// 创建子品类
export function createProjectSubcategory(data: Partial<ProjectSubcategoryEntity>) {
  return post<ProjectSubcategoryEntity>('/api/project-subcategories', data)
}

// 更新子品类
export function updateProjectSubcategory(id: number, data: Partial<ProjectSubcategoryEntity>) {
  return post<ProjectSubcategoryEntity>(`/api/project-subcategories/${id}`, data)
}

// 删除子品类
export function deleteProjectSubcategory(id: number) {
  return post(`/api/project-subcategories/${id}`)
}

// 更新子品类排序
export function updateSubcategorySort(id: number, sortOrder: number) {
  return post(`/api/project-subcategories/${id}/sort`, { sortOrder })
}

// ==================== 项目层级管理 API ====================

// 获取完整的项目层级树
export function getProjectHierarchyTree() {
  return get<ProjectHierarchyTreeNode[]>('/api/project-hierarchy/tree')
}

// 获取指定部门的项目层级树
export function getDepartmentHierarchyTree(departmentId: number) {
  return get<ProjectHierarchyTreeNode>(`/api/project-hierarchy/tree/department/${departmentId}`)
}

// 获取指定品类的层级树
export function getCategoryHierarchyTree(categoryId: number) {
  return get<ProjectHierarchyTreeNode>(`/api/project-hierarchy/tree/category/${categoryId}`)
}

// 获取指定子品类的层级树
export function getSubcategoryHierarchyTree(subcategoryId: number) {
  return get<ProjectHierarchyTreeNode>(`/api/project-hierarchy/tree/subcategory/${subcategoryId}`)
}

// 搜索项目层级结构
export function searchProjectHierarchy(keyword: string, type: string = 'all') {
  return get<ProjectHierarchyTreeNode[]>('/api/project-hierarchy/search', { keyword, type })
}

// 移动层级节点
export function moveHierarchyNode(nodeType: string, nodeId: number, targetParentId?: number, newSortOrder?: number) {
  return post('/api/project-hierarchy/move', {
    nodeType,
    nodeId,
    targetParentId,
    newSortOrder
  })
}

// 批量更新排序
export function batchUpdateHierarchySort(nodeType: string, items: Array<{ id: number; sortOrder: number }>) {
  return post('/api/project-hierarchy/batch-sort', {
    nodeType,
    items
  })
}

// 获取层级统计信息
export function getHierarchyStatistics() {
  return get<Record<string, any>>('/api/project-hierarchy/statistics')
}

// 获取指定部门的统计信息
export function getDepartmentStatistics(departmentId: number) {
  return get<Record<string, any>>(`/api/project-hierarchy/statistics/department/${departmentId}`)
}

// 获取节点的面包屑导航
export function getHierarchyBreadcrumb(nodeType: string, nodeId: number) {
  return get<Array<Record<string, any>>>(`/api/project-hierarchy/breadcrumb/${nodeType}/${nodeId}`)
}

// 验证移动操作是否合法
export function validateHierarchyMove(nodeType: string, nodeId: number, targetParentId?: number) {
  return post<Record<string, any>>('/api/project-hierarchy/validate-move', {
    nodeType,
    nodeId,
    targetParentId
  })
}