import type { MockMethod } from 'vite-plugin-mock'

const mockProjects = [
  {
    id: '101',
    name: '5G通信基站项目',
    projectNum: 'COM001',
    priority: 'P0',
    description: '面向新一代5G通信基站的研发与部署。',
    status: 'ONGOING',
    director: '201',
    directorName: '王通信',
    directorHeadImg: '/image/2025/05/20/comm-director.png',
    startTime: '2025-06-01',
    endTime: '2025-12-31',
    documentCount: 8,
    userCount: 12,
    createTime: '2025-06-01 09:00:00',
    creator: '系统管理员',
    createUid: null,
    tenantId: null,
    pid: null,
    pxh: null
  },
  {
    id: '102',
    name: '高性能计存一体化平台',
    projectNum: 'STO001',
    priority: 'P1',
    description: '打造高性能计算与存储一体化解决方案。',
    status: 'SURVEY',
    director: '202',
    directorName: '李计存',
    directorHeadImg: '/image/2025/05/20/storage-director.png',
    startTime: '2025-07-01',
    endTime: '2025-12-01',
    documentCount: 5,
    userCount: 8,
    createTime: '2025-07-01 10:00:00',
    creator: '超级管理员',
    createUid: null,
    tenantId: null,
    pid: null,
    pxh: null
  },
  {
    id: '103',
    name: 'AI推理GPU加速卡',
    projectNum: 'GPU001',
    priority: 'P2',
    description: '面向AI推理的高性能GPU加速卡研发。',
    status: 'COMPLETED',
    director: '203',
    directorName: '张显卡',
    directorHeadImg: '/image/2025/05/20/gpu-director.png',
    startTime: '2024-12-01',
    endTime: '2025-05-30',
    documentCount: 10,
    userCount: 15,
    createTime: '2024-12-01 08:30:00',
    creator: '项目管理员',
    createUid: null,
    tenantId: null,
    pid: null,
    pxh: null
  }
]

const mockDocuments = [
  // 通信
  {
    id: 'd101',
    projectId: '101',
    name: '5G基站总体方案设计',
    documentCode: 'COM-DOC-001',
    type: '技术方案',
    format: 'docx',
    content: '',
    description: '5G基站系统的总体技术方案设计文档。',
    tags: ['5G', '通信', '方案'],
    status: '已发布',
    version: '1.0.0',
    creator: '王通信',
    creatorAvatar: '/image/2025/05/20/comm-director.png',
    createTime: '2025-06-02 10:00:00',
    updateTime: '2025-06-10 10:00:00',
    url: '/fake/path/to/5g-design.docx',
    templateId: 't1',
    templateName: '通信方案模板',
    directorId: '201',
    directorName: '王通信',
    syncStatus: 'synced',
    reviewStatus: '已审核',
    reviewName: '王通信'
  },
  {
    id: 'd102',
    projectId: '101',
    name: '基站硬件选型报告',
    documentCode: 'COM-DOC-002',
    type: '选型报告',
    format: 'pdf',
    content: '',
    description: '5G基站硬件选型与评估报告。',
    tags: ['硬件', '评估'],
    status: '草稿',
    version: '0.9.0',
    creator: '李工程',
    creatorAvatar: '/image/default-avatar.png',
    createTime: '2025-06-05 09:00:00',
    updateTime: '2025-06-06 09:00:00',
    url: '/fake/path/to/hardware-report.pdf',
    templateId: 't2',
    templateName: '评估报告模板',
    directorId: '201',
    directorName: '王通信',
    syncStatus: 'pending',
    reviewStatus: null,
    reviewName: null
  },
  // 计存
  {
    id: 'd201',
    projectId: '102',
    name: '计存平台架构设计',
    documentCode: 'STO-DOC-001',
    type: '架构设计',
    format: 'docx',
    content: '',
    description: '高性能计存一体化平台的架构设计文档。',
    tags: ['架构', '计存'],
    status: '已发布',
    version: '1.0.0',
    creator: '李计存',
    creatorAvatar: '/image/2025/05/20/storage-director.png',
    createTime: '2025-07-02 11:00:00',
    updateTime: '2025-07-10 11:00:00',
    url: '/fake/path/to/storage-arch.docx',
    templateId: 't3',
    templateName: '架构设计模板',
    directorId: '202',
    directorName: '李计存',
    syncStatus: 'synced',
    reviewStatus: '已审核',
    reviewName: '李计存'
  },
  {
    id: 'd202',
    projectId: '102',
    name: '计存平台测试用例',
    documentCode: 'STO-DOC-002',
    type: '测试用例',
    format: 'xlsx',
    content: '',
    description: '计存一体化平台的功能与性能测试用例。',
    tags: ['测试', '计存'],
    status: '草稿',
    version: '0.8.0',
    creator: '王测试',
    creatorAvatar: '/image/default-avatar.png',
    createTime: '2025-07-05 14:00:00',
    updateTime: '2025-07-06 14:00:00',
    url: '/fake/path/to/storage-test.xlsx',
    templateId: 't4',
    templateName: '测试用例模板',
    directorId: '202',
    directorName: '李计存',
    syncStatus: 'pending',
    reviewStatus: null,
    reviewName: null
  },
  // GPU
  {
    id: 'd301',
    projectId: '103',
    name: 'GPU加速卡硬件设计',
    documentCode: 'GPU-DOC-001',
    type: '硬件设计',
    format: 'pdf',
    content: '',
    description: 'AI推理GPU加速卡的硬件设计文档。',
    tags: ['GPU', '硬件', 'AI'],
    status: '已发布',
    version: '1.0.0',
    creator: '张显卡',
    creatorAvatar: '/image/2025/05/20/gpu-director.png',
    createTime: '2025-01-10 13:00:00',
    updateTime: '2025-05-20 13:00:00',
    url: '/fake/path/to/gpu-hw.pdf',
    templateId: 't5',
    templateName: '硬件设计模板',
    directorId: '203',
    directorName: '张显卡',
    syncStatus: 'synced',
    reviewStatus: '已审核',
    reviewName: '张显卡'
  },
  {
    id: 'd302',
    projectId: '103',
    name: 'GPU加速卡驱动开发文档',
    documentCode: 'GPU-DOC-002',
    type: '驱动开发',
    format: 'docx',
    content: '',
    description: 'AI推理GPU加速卡的驱动开发与接口文档。',
    tags: ['GPU', '驱动', '开发'],
    status: '草稿',
    version: '0.7.0',
    creator: '李驱动',
    creatorAvatar: '/image/default-avatar.png',
    createTime: '2025-02-15 15:00:00',
    updateTime: '2025-03-01 15:00:00',
    url: '/fake/path/to/gpu-driver.docx',
    templateId: 't6',
    templateName: '驱动开发模板',
    directorId: '203',
    directorName: '张显卡',
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
      // 兼容扁平和嵌套 params 传参
      const projectId = query.projectId || (query.params && query.params.projectId)
      const documents = mockDocuments.filter(doc => doc.projectId === projectId)
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
    response: ({ query }: { query: Record<string, any> }) => {
      console.log(`[Mock] Getting collaborators for document ${query.documentId}`)
      return { code: 200, msg: 'Success', data: mockCollaborators }
    }
  },
  {
    url: '/api/document/collaborators',
    method: 'post',
    response: ({ query, body }: { query: Record<string, any>, body: any }) => {
      console.log(`[Mock] Updating collaborators for document ${query.documentId}`, body)
      return { code: 200, msg: '更新成功' }
    }
  }
]

export default documentMocks 