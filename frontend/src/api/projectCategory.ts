import request from '@/utils/request'
import type { ProjectCategory } from '@/types/document' // Assuming a type definition exists or will be created

// Let's define the types right here for now for clarity
export interface ProjectCategoryNode {
  id: number
  name: string
  type: 'DEPARTMENT' | 'CATEGORY' | 'SUB_CATEGORY'
  parentId: number | null
  children?: ProjectCategoryNode[]
}

export interface CreateCategoryPayload {
  name: string
  type: 'DEPARTMENT' | 'CATEGORY' | 'SUB_CATEGORY'
  parentId?: number | null
  sortOrder?: number
}

export interface UpdateCategoryPayload {
  name?: string
  parentId?: number | null
  sortOrder?: number
}

/**
 * 获取项目分类树形结构
 */
export function getCategoryTree(): Promise<ProjectCategoryNode[]> {
  return request({
    url: '/api/project-categories/tree',
    method: 'get',
  })
}

/**
 * 创建项目分类
 * @param data 创建分类的载荷
 */
export function createCategory(data: CreateCategoryPayload): Promise<ProjectCategory> {
  return request({
    url: '/api/project-categories',
    method: 'post',
    data,
  })
}

/**
 * 更新项目分类
 * @param id 分类ID
 * @param data 更新分类的载荷
 */
export function updateCategory(id: number, data: UpdateCategoryPayload): Promise<ProjectCategory> {
  return request({
    url: `/api/project-categories/${id}`,
    method: 'put',
    data,
  })
}

/**
 * 删除项目分类
 * @param id 分类ID
 */
export function deleteCategory(id: number): Promise<void> {
  return request({
    url: `/api/project-categories/${id}`,
    method: 'delete',
  })
}
