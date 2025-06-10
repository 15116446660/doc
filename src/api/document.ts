import type { Project, ProjectType, Document, DocumentVersion, OptionItem } from '@/types/document'

// Mock data
const mockProjects: Project[] = [
  {
    id: '1',
    name: '示例项目1',
    projectNum: 'PRJ001',
    priority: 'P0',
    description: '这是一个示例项目',
    status: 'SURVEY',
    director: '45',
    directorName: '李立彪',
    directorHeadImg: '/image/2025/05/20/9e3a9a325303440fab5be669c6bb9e25a1.png',
    startTime: '2025-05-30',
    endTime: '2025-06-27',
    documentCount: 3,
    userCount: 4,
    createTime: '2025-05-30 15:06:56',
    creator: '超级管理员',
    createUid: null,
    tenantId: null,
    pid: null,
    pxh: null
  },
  {
    id: '2',
    name: '测试项目',
    projectNum: 'mz',
    priority: 'P1',
    description: '',
    status: 'ONGOING',
    director: '46',
    directorName: '李四',
    directorHeadImg: '/image/default-avatar.png',
    startTime: '2025-06-01',
    endTime: '2025-07-30',
    documentCount: 5,
    userCount: 3,
    createTime: '2025-06-01 09:30:00',
    creator: '管理员',
    createUid: null,
    tenantId: null,
    pid: null,
    pxh: null
  }
]

const mockDocuments: Document[] = [
  {
    id: '1',
    projectId: '1',
    name: '示例文档1',
    documentCode: 'DOC001',
    type: '招标文件',
    format: 'docx',
    content: '',
    description: '这是一个示例文档',
    tags: ['招标', '重要'],
    status: '已发布',
    version: '1.0.0',
    creator: '张三',
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-01 10:00:00'
  },
  {
    id: '2',
    projectId: '1',
    name: '示例文档2',
    documentCode: 'DOC002',
    type: '技术方案',
    format: 'pdf',
    content: '',
    description: '这是另一个示例文档',
    tags: ['方案', '草稿'],
    status: '草稿',
    version: '1.0.0',
    creator: '李四',
    createTime: '2024-02-01 10:00:00',
    updateTime: '2024-02-01 10:00:00'
  }
]

const mockVersions: DocumentVersion[] = [
  {
    id: '1',
    documentId: '1',
    version: '1.0.0',
    content: '',
    description: '初始版本',
    creator: '张三',
    createTime: '2024-01-01 10:00:00'
  },
  {
    id: '2',
    documentId: '1',
    version: '1.1.0',
    content: '',
    description: '更新了部分内容',
    creator: '李四',
    createTime: '2024-01-02 10:00:00'
  }
]

// Project APIs
export const getProjectList = async (params: any) => {
  // 模拟API响应，实际项目中应替换为真实API调用
  console.log('请求参数:', params)
  
  // 在实际环境中，这里应该调用后端API
  return {
    code: 200,
    message: '操作成功',
    data: {
      records: mockProjects,
      total: mockProjects.length,
      size: 10,
      current: 1,
      pages: 1
    }
  }
}

export const getProjectTypeOptions = async () => {
  // Mock API response
  return [
    { label: '工程项目', value: 'engineering' },
    { label: '产品项目', value: 'product' },
    { label: '服务项目', value: 'service' }
  ]
}

export const getProjectStatusOptions = async () => {
  // Mock API response
  return [
    { label: '调研中', value: 'SURVEY' },
    { label: '进行中', value: 'ONGOING' },
    { label: '已完成', value: 'COMPLETED' },
    { label: '已暂停', value: 'PAUSED' },
    { label: '已取消', value: 'CANCELLED' }
  ]
}

export const getProjectCategoryOptions = async () => {
  // Mock API response
  return [
    { label: '市政工程', value: 'municipal' },
    { label: '房建工程', value: 'building' },
    { label: '道路工程', value: 'road' }
  ]
}

export const getDepartmentOptions = async () => {
  // Mock API response
  return [
    { label: '工程部', value: 'engineering' },
    { label: '研发部', value: 'rd' },
    { label: '市场部', value: 'marketing' }
  ]
}

// Document APIs
export const getDocumentList = async (params: any) => {
  // Mock API response
  const documents = mockDocuments.filter(doc => doc.projectId === params.projectId)
  return {
    data: documents,
    total: documents.length
  }
}

export const getDocumentTypeOptions = async () => {
  // Mock API response
  return [
    { label: '招标文件', value: 'tender' },
    { label: '技术方案', value: 'technical' },
    { label: '合同文件', value: 'contract' },
    { label: '报价文件', value: 'quotation' }
  ]
}

export const getDocumentStatusOptions = async () => {
  // Mock API response
  return [
    { label: '草稿', value: 'draft' },
    { label: '审核中', value: 'reviewing' },
    { label: '已发布', value: 'published' },
    { label: '已废弃', value: 'deprecated' }
  ]
}

export const getDocumentTagOptions = async () => {
  // Mock API response
  return [
    { label: '招标', value: 'tender' },
    { label: '方案', value: 'plan' },
    { label: '合同', value: 'contract' },
    { label: '重要', value: 'important' },
    { label: '草稿', value: 'draft' }
  ]
}

export const getDocumentVersions = async (documentId: string) => {
  // Mock API response
  return mockVersions.filter(version => version.documentId === documentId)
} 