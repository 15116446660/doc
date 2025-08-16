// 文档类型枚举
export enum DocumentType {
  CONTRACT = 'CONTRACT',
  TECHNICAL = 'TECHNICAL',
  BUSINESS = 'BUSINESS',
  LEGAL = 'LEGAL',
  FINANCIAL = 'FINANCIAL',
  HR = 'HR',
  PROJECT = 'PROJECT',
  POLICY = 'POLICY',
  TRAINING = 'TRAINING',
  OTHER = 'OTHER'
}

// 文档状态枚举
export enum DocumentStatus {
  DRAFT = 'DRAFT',
  REVIEWING = 'REVIEWING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  PUBLISHED = 'PUBLISHED',
  WITHDRAWN = 'WITHDRAWN',
  ARCHIVED = 'ARCHIVED',
  EXPIRED = 'EXPIRED'
}

// 安全级别枚举
export enum SecurityLevel {
  PUBLIC = 'PUBLIC',
  INTERNAL = 'INTERNAL',
  CONFIDENTIAL = 'CONFIDENTIAL',
  SECRET = 'SECRET',
  RESTRICTED = 'RESTRICTED'
}

// 文档接口
export interface Document {
  id: number
  documentNumber: string
  title: string
  summary?: string
  documentType: DocumentType
  status: DocumentStatus
  securityLevel: SecurityLevel
  authorId: number
  authorName?: string
  departmentId?: number
  projectId?: number
  projectName?: string
  businessNumber?: string
  tags?: string
  filePath?: string
  fileSize?: number
  fileHash?: string
  currentVersion: number
  isLocked: boolean
  lockedBy?: number
  lockedByName?: string
  lockedAt?: string
  submittedAt?: string
  publishedAt?: string
  expiredAt?: string
  createdAt: string
  updatedAt: string
  createdBy?: number
  createdByName?: string
  updatedBy?: number
  updatedByName?: string
  remark?: string
  canDownload: boolean
  canEdit: boolean
  canDelete: boolean
}

// 文档查询参数
export interface DocumentQuery {
  keyword?: string
  documentType?: string
  status?: string
  securityLevel?: string
  authorId?: number
  projectId?: number
  departmentId?: number
  tags?: string
  createdAtStart?: string
  createdAtEnd?: string
  updatedAtStart?: string
  updatedAtEnd?: string
  dateRange?: string[]
  page?: number
  size?: number
}

// 文档创建参数
export interface DocumentCreate {
  title: string
  summary?: string
  documentType: DocumentType
  securityLevel?: SecurityLevel
  projectId?: number
  departmentId?: number
  businessNumber?: string
  tags?: string
  content?: string
  remark?: string
}

// 文档更新参数
export interface DocumentUpdate {
  title?: string
  summary?: string
  documentType?: DocumentType
  status?: DocumentStatus
  securityLevel?: SecurityLevel
  projectId?: number
  departmentId?: number
  businessNumber?: string
  tags?: string
  content?: string
  remark?: string
}

// 文档上传表单
export interface DocumentUploadForm {
  file: File | null
  title: string
  documentType: string
  summary: string
  securityLevel: string
  projectId?: number
  tags: string
}
