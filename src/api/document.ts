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
  return get<PageResult<Project>>('/api/project/list', { params })
}

export const getProjectTypeOptions = () => {
  return get<OptionItem[]>('/api/project/type-options')
}

export const getProjectStatusOptions = () => {
  return get<OptionItem[]>('/api/project/status-options')
}

export const getProjectCategoryOptions = () => {
  return get<OptionItem[]>('/api/project/category-options')
}

export const getDepartmentOptions = () => {
  return get<OptionItem[]>('/api/project/department-options')
}

// Document APIs
export const getDocumentList = (params: any) => {
  return get<PageResult<Document>>('/api/document/list', params)
}

export const getDocumentTypeOptions = () => {
  return get<OptionItem[]>('/api/document/type-options')
}

export const getDocumentStatusOptions = () => {
  return get<OptionItem[]>('/api/document/status-options')
}

export const getDocumentTagOptions = () => {
  return get<OptionItem[]>('/api/document/tag-options')
}

export const getDocumentVersions = (documentId: string) => {
  return get<DocumentVersion[]>(`/api/document/versions`, { params: { documentId } })
}

export const getTemplateOptions = () => {
  return get<OptionItem[]>('/api/template/options')
}

/**
 * 获取负责人选项列表
 */
export const getDirectorOptions = () => {
  return get<any[]>('/api/project/director-options')
}

export function getDocumentCollaborators(documentId: string) {
  return get<DocumentCollaborator[]>(`/api/document/collaborators?documentId=${documentId}`)
}

export function updateDocumentCollaborators(documentId: string, collaborators: DocumentCollaborator[]) {
  return post(`/api/document/collaborators/update?documentId=${documentId}`, { collaborators })
}