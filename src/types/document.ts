export interface Project {
  id: string
  name: string
  projectNum?: string
  priority?: string
  description?: string
  status?: string
  director?: string
  directorName?: string
  directorHeadImg?: string
  startTime?: string
  endTime?: string
  documentCount?: number
  userCount?: number
  createTime?: string
  creator?: string
  createUid?: string | null
  tenantId?: string | null
  pid?: string | null
  pxh?: number | null
  type?: string
}

export interface ProjectType {
  id: string
  name: string
  parentId?: string
  children?: ProjectType[]
  sort?: number
}

export interface Document {
  id: string
  projectId: string
  name: string
  documentCode: string
  type: string
  format: string
  content: string
  description: string
  tags: string[]
  status: string
  version: string
  creator: string
  creatorAvatar?: string
  createTime: string
  updateTime: string
  url?: string
  templateId?: string
  templateName?: string
  directorId?: string
  directorName?: string
  syncStatus?: 'synced' | 'pending' | 'failed' | 'none'
  reviewStatus?: string
  reviewName?: string
  autoFill?: boolean
}

export interface DocumentVersion {
  id: string
  documentId: string
  version: string
  content: string
  description?: string
  creator: string
  creatorAvatar?: string
  createTime: string
  url?: string
}

export interface OptionItem {
  label: string
  value: string
} 