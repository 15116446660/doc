export interface Project {
  id: string
  name: string
  projectCode: string
  type: string
  department: string
  owner: string
  ownerAvatar?: string
  startDate: string
  endDate: string
  description?: string
  status: string
  createTime: string
  updateTime: string
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