import request from '@/utils/request'
import type {
  Project,
  Document,
  DocumentVersion,
  OptionItem,
  DocumentCollaborator
} from '@/types/document'
import type { PageResult } from '@/types/global'
import { get, post } from './request'

// Project APIs
export const getProjectList = (params: any) => {
  return request<PageResult<Project>>({
    url: '/api/project/list',
    method: 'get',
    params
  })
}

export const getProjectTypeOptions = () => {
  return request<OptionItem[]>({
    url: '/api/project/type-options',
    method: 'get'
  })
}

export const getProjectStatusOptions = () => {
  return request<OptionItem[]>({
    url: '/api/project/status-options',
    method: 'get'
  })
}

export const getProjectCategoryOptions = () => {
  return request<OptionItem[]>({
    url: '/api/project/category-options',
    method: 'get'
  })
}

export const getDepartmentOptions = () => {
  return request<OptionItem[]>({
    url: '/api/project/department-options',
    method: 'get'
  })
}

// Document APIs
export const getDocumentList = (params: any) => {
  return request<PageResult<Document>>({
    url: '/api/document/list',
    method: 'get',
    params
  })
}

export const getDocumentTypeOptions = () => {
  return request<OptionItem[]>({
    url: '/api/document/type-options',
    method: 'get'
  })
}

export const getDocumentStatusOptions = () => {
  return request<OptionItem[]>({
    url: '/api/document/status-options',
    method: 'get'
  })
}

export const getDocumentTagOptions = () => {
  return request<OptionItem[]>({
    url: '/api/document/tag-options',
    method: 'get'
  })
}

export const getDocumentVersions = (documentId: string) => {
  return request<DocumentVersion[]>({
    url: '/api/document/versions',
    method: 'get',
    params: { documentId }
  })
}

export const getTemplateOptions = () => {
  return request<OptionItem[]>({
    url: '/api/template/options',
    method: 'get'
  })
}

/**
 * 获取负责人选项列表
 */
export const getDirectorOptions = () => {
  return request<any[]>({
    url: '/api/project/director-options',
    method: 'get'
  })
}

export function getDocumentCollaborators(documentId: string) {
  return get<DocumentCollaborator[]>(`/api/document/collaborators?documentId=${documentId}`)
}

export function updateDocumentCollaborators(documentId: string, collaborators: DocumentCollaborator[]) {
  return post(`/api/document/collaborators/update?documentId=${documentId}`, { collaborators })
}