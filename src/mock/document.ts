import type { MockMethod } from 'vite-plugin-mock'

const mockProjects = [
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

const mockDocuments = [
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
    creatorAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-01 10:00:00',
    url: '/fake/path/to/document1.docx',
    templateId: 't1',
    templateName: '标准招标文件模板',
    directorId: '2',
    directorName: '李四',
    syncStatus: 'synced',
    reviewStatus: '已审核',
    reviewName: '李四'
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
    creatorAvatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    createTime: '2024-02-01 10:00:00',
    updateTime: '2024-02-01 10:00:00',
    url: '/fake/path/to/document2.pdf',
    templateId: 't3',
    templateName: '技术方案模板',
    directorId: '1',
    directorName: '张三',
    syncStatus: 'pending',
    reviewStatus: null,
    reviewName: null
  }
]

const mockVersions = [
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

const mockTemplates = [
  { id: 't1', name: '标准招标文件模板' },
  { id: 't2', name: '施工组织设计模板' },
  { id: 't3', name: '技术方案模板' }
]

const mockCollaborators = [
  { id: '1', name: '张三', headImg: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png' },
  { id: '2', name: '李四', headImg: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png' },
  { id: '3', name: '王五', headImg: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png' }
]

const documentMocks: MockMethod[] = [
  {
    url: '/api/project/list',
    method: 'get',
    response: ({ query }: { query: Record<string, any> }) => {
      console.log('mock request query:', query)
      return {
        code: 200,
        message: '操作成功',
        data: {
          records: mockProjects,
          total: mockProjects.length,
          size: parseInt(query.size) || 10,
          current: parseInt(query.current) || 1,
          pages: 1
        }
      }
    }
  },
  {
    url: '/api/project/type-options',
    method: 'get',
    response: {
      code: 200,
      message: '成功',
      data: [
        { label: '工程项目', value: 'engineering' },
        { label: '产品项目', value: 'product' },
        { label: '服务项目', value: 'service' }
      ]
    }
  },
  {
    url: '/api/project/status-options',
    method: 'get',
    response: {
      code: 200,
      message: '成功',
      data: [
        { label: '调研中', value: 'SURVEY' },
        { label: '进行中', value: 'ONGOING' },
        { label: '已完成', value: 'COMPLETED' },
        { label: '已暂停', value: 'PAUSED' },
        { label: '已取消', value: 'CANCELLED' }
      ]
    }
  },
  {
    url: '/api/project/category-options',
    method: 'get',
    response: {
      code: 200,
      message: '成功',
      data: [
        { label: '市政工程', value: 'municipal' },
        { label: '房建工程', value: 'building' },
        { label: '道路工程', value: 'road' }
      ]
    }
  },
  {
    url: '/api/project/department-options',
    method: 'get',
    response: {
      code: 200,
      message: '成功',
      data: [
        { label: '工程部', value: 'engineering' },
        { label: '研发部', value: 'rd' },
        { label: '市场部', value: 'marketing' }
      ]
    }
  },
  {
    url: '/api/project/director-options',
    method: 'get',
    response: {
      code: 200,
      message: '成功',
      data: [
        {
          id: '1',
          name: '张三',
          headImg: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
        },
        {
          id: '2',
          name: '李四',
          headImg: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
        },
        {
          id: '3',
          name: '王五',
          headImg: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
        }
      ]
    }
  },
  {
    url: '/api/document/list',
    method: 'get',
    response: ({ query }: { query: Record<string, any> }) => {
      const documents = mockDocuments.filter(doc => doc.projectId === query.projectId)
      return {
        code: 200,
        message: '成功',
        data: {
            records: documents,
            total: documents.length,
        }
      }
    }
  },
  {
    url: '/api/document/type-options',
    method: 'get',
    response: {
      code: 200,
      message: '成功',
      data: [
        { label: '招标文件', value: 'tender' },
        { label: '技术方案', value: 'technical' },
        { label: '合同文件', value: 'contract' },
        { label: '报价文件', value: 'quotation' }
      ]
    }
  },
  {
    url: '/api/document/status-options',
    method: 'get',
    response: {
      code: 200,
      message: '成功',
      data: [
        { label: '草稿', value: 'draft' },
        { label: '审核中', value: 'reviewing' },
        { label: '已发布', value: 'published' },
        { label: '已废弃', value: 'deprecated' }
      ]
    }
  },
  {
    url: '/api/document/tag-options',
    method: 'get',
    response: {
      code: 200,
      message: '成功',
      data: [
        { label: '招标', value: 'tender' },
        { label: '方案', value: 'plan' },
        { label: '合同', value: 'contract' },
        { label: '重要', value: 'important' },
        { label: '草稿', value: 'draft' }
      ]
    }
  },
  {
    url: '/api/document/versions',
    method: 'get',
    response: ({ query }: { query: Record<string, any> }) => {
        const versions = mockVersions.filter(version => version.documentId === query.documentId)
        return {
            code: 200,
            message: '成功',
            data: versions
        }
    }
  },
  {
    url: '/api/template/options',
    method: 'get',
    response: {
      code: 200,
      message: '成功',
      data: mockTemplates.map((t) => ({ label: t.name, value: t.id }))
    }
  },
  {
    url: '/api/document/collaborators',
    method: 'get',
    response: ({ query }) => {
      console.log(`[Mock] Getting collaborators for document ${query.documentId}`)
      return { code: 200, msg: 'Success', data: mockCollaborators }
    }
  },
  {
    url: '/api/document/collaborators',
    method: 'post',
    response: ({ query, body }) => {
      console.log(`[Mock] Updating collaborators for document ${query.documentId}`, body)
      return { code: 200, msg: '更新成功' }
    }
  }
]

export default documentMocks 