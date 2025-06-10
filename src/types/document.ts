export interface Project {
  id: string
  name: string
  projectNum: string
  priority: string
  description?: string
  status: string
  director?: string | null
  directorName?: string | null
  directorHeadImg?: string | null
  startTime: string
  endTime: string
  documentCount?: number
  userCount?: number
  createTime: string
  creator?: string | null
  createUid?: string | null
  updateTime?: string | null
  updater?: string | null
  updateUid?: string | null
  actualEndTime?: string | null
  tenantId?: string | null
  pid?: string | null
  pxh?: string | null
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
  description?: string
  tags?: string[]
  status: string
  reviewStatus?: string
  reviewName?: string
  version: string
  creator: string
  creatorAvatar?: string
  createTime: string
  updateTime: string
  url?: string
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