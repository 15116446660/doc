import type { MockMethod } from 'vite-plugin-mock'
import mockjs from 'mockjs'
const { Random } = mockjs

// 模板分类数据
const templateCategories = [
  {
    id: 1,
    name: '标书模板',
    icon: 'Document',
    children: [
      {
        id: 11,
        name: '政府采购',
        children: [
          { id: 111, name: '工程类' },
          { id: 112, name: '服务类' },
          { id: 113, name: '货物类' }
        ]
      },
      {
        id: 12,
        name: '企业招标',
        children: [
          { id: 121, name: '技术标' },
          { id: 122, name: '商务标' },
          { id: 123, name: '资格预审' }
        ]
      }
    ]
  },
  {
    id: 2,
    name: '文档模板',
    icon: 'Files',
    children: [
      {
        id: 21,
        name: '方案文档',
        children: [
          { id: 211, name: '技术方案' },
          { id: 212, name: '实施方案' },
          { id: 213, name: '解决方案' }
        ]
      },
      {
        id: 22,
        name: '合同文档',
        children: [
          { id: 221, name: '合同正文' },
          { id: 222, name: '补充协议' }
        ]
      }
    ]
  },
  {
    id: 3,
    name: '内容模板',
    icon: 'Collection',
    children: [
      {
        id: 31,
        name: '公司介绍',
        children: []
      },
      {
        id: 32,
        name: '产品介绍',
        children: []
      },
      {
        id: 33,
        name: '案例介绍',
        children: []
      }
    ]
  }
]

// 模板分类数据
const templateTypes = [
  {
    id: 1,
    tenantId: 0,
    typeCode: "GYWD",
    typeName: "概要文档",
    children: [
      {
        id: 11,
        tenantId: 0,
        typeCode: "GYWD1",
        typeName: "概要文档1",
        parentId: 1,
        createTime: "2024-01-20 10:00:00",
        updateTime: "2024-01-20 10:00:00",
        createUid: 1,
        updateUid: 1
      }
    ],
    createTime: "2024-01-20 10:00:00",
    updateTime: "2024-01-20 10:00:00",
    createUid: 1,
    updateUid: 1
  },
  {
    id: 2,
    tenantId: 0,
    typeCode: "CPWD",
    typeName: "产品文档",
    children: [
      {
        id: 21,
        tenantId: 0,
        typeCode: "CPWD1",
        typeName: "产品文档1",
        parentId: 2,
        createTime: "2024-01-20 10:00:00",
        updateTime: "2024-01-20 10:00:00",
        createUid: 1,
        updateUid: 1
      }
    ],
    createTime: "2024-01-20 10:00:00",
    updateTime: "2024-01-20 10:00:00",
    createUid: 1,
    updateUid: 1
  },
  {
    id: 3,
    tenantId: 0,
    typeCode: "YJWD",
    typeName: "硬件文档",
    children: [
      {
        id: 31,
        tenantId: 0,
        typeCode: "YJWD1",
        typeName: "硬件文档1",
        parentId: 3,
        createTime: "2024-01-20 10:00:00",
        updateTime: "2024-01-20 10:00:00",
        createUid: 1,
        updateUid: 1
      }
    ],
    createTime: "2024-01-20 10:00:00",
    updateTime: "2024-01-20 10:00:00",
    createUid: 1,
    updateUid: 1
  }
]

// 生成模板数据
const generateTemplates = (count: number) => {
  const statusOptions = [
    { status: '已发布', reviewStatus: '', reviewName: '', color: '#10B981' },
    { status: '草稿', reviewStatus: '', reviewName: '', color: '#6B7280' },
    { status: '审核中', reviewStatus: 'IN_REVIEW', reviewName: '审核中', color: '#F59E0B' },
    { status: '已废弃', reviewStatus: '', reviewName: '', color: '#EF4444' }
  ]
  
  const templates = []
  for (let i = 1; i <= count; i++) {
    const statusOption = statusOptions[Math.floor(Math.random() * statusOptions.length)]
    const categoryId = Math.floor(Math.random() * 3) + 1
    const subcategoryId = Math.floor(Math.random() * 2) + 1
    const categoryPath = `${categoryId}${subcategoryId}`
    
    const version = Math.floor(Math.random() * 3) + 1
    const createTime = Random.datetime('yyyy-MM-dd HH:mm:ss')
    const updateTime = Random.datetime('yyyy-MM-dd HH:mm:ss')
    const templateCode = `0${i % 9 + 1}`
    const title = Random.ctitle(4, 10)
    
    templates.push({
      id: i,
      name: title,
      title: title,
      categoryName: getCategoryName(categoryPath),
      status: statusOption.status,
      statusColor: statusOption.color,
      owner: Random.cname(),
      ownerAvatar: null,
      version,
      description: Random.cparagraph(1, 3),
      content: generateTemplateContent(),
      createTime,
      updateTime,
      downloads: Math.floor(Math.random() * 1000),
      favorite: Math.random() > 0.7,
      
      // 新增字段
      type: "XP1UDYNDS",
      typeName: null,
      format: "XP",
      templateCode,
      fileCode: templateCode,
      fileId: 100 + i,
      suffix: "docx",
      delFlag: 0,
      reviewStatus: statusOption.reviewStatus,
      reviewName: statusOption.reviewName,
      isChecklist: null,
      isExample: null,
      picture: null,
      projectId: null,
      rejectReason: null,
      reviseContent: null,
      scope: "全部",
      standardType: "优秀案例模版",
      applicableScope: "全部",
      suffixCode: "DOC",
      templateDocumentName: `${templateCode} ${title}`,
      tenantId: null,
      url: `/template/2025/05/17/${Random.guid()}.docx`,
      createUid: Math.floor(Math.random() * 10) + 1,
      creator: "Zoco",
      updateUid: null,
      updater: null,
      enableStatus: Math.random() > 0.3
    })
  }
  return templates
}

// 获取分类名称
function getCategoryName(categoryId: string) {
  if (categoryId.length < 2) return ''
  
  const mainCatId = parseInt(categoryId[0])
  const subCatId = parseInt(categoryId[1])
  
  const mainCategory = templateCategories.find(cat => cat.id === mainCatId)
  if (!mainCategory) return ''
  
  const subCategory = mainCategory.children.find(cat => cat.id === mainCatId * 10 + subCatId)
  if (!subCategory) return ''
  
  return `${mainCategory.name} / ${subCategory.name}`
}

// 生成模板内容
function generateTemplateContent() {
  const paragraphs = []
  const paragraphCount = Math.floor(Math.random() * 5) + 3
  
  for (let i = 0; i < paragraphCount; i++) {
    paragraphs.push(`<h3>${Random.ctitle(4, 8)}</h3>`)
    paragraphs.push(`<p>${Random.cparagraph(5, 15)}</p>`)
    
    // 随机添加列表
    if (Math.random() > 0.7) {
      const listItems = []
      const itemCount = Math.floor(Math.random() * 4) + 2
      for (let j = 0; j < itemCount; j++) {
        listItems.push(`<li>${Random.csentence(5, 15)}</li>`)
      }
      paragraphs.push(`<ul>${listItems.join('')}</ul>`)
    }
  }
  
  return paragraphs.join('')
}

const templates = generateTemplates(50)

// 模板版本历史数据
const templateVersionHistory = new Map()

// 生成模板版本历史数据
const generateTemplateVersionHistory = (templateId: number | string) => {
  if (templateVersionHistory.has(templateId)) {
    return templateVersionHistory.get(templateId)
  }

  const template = templates.find(t => t.id.toString() === templateId.toString())
  if (!template) return []

  const statusOptions = ['通过', '驳回', '审核中', '草稿']
  const versions = []
  
  // 生成1-5个版本记录
  const versionCount = Math.floor(Math.random() * 5) + 1
  
  for (let i = 0; i < versionCount; i++) {
    const versionNum = i === 0 ? 1 : 2
    const status = statusOptions[i % statusOptions.length]
    
    versions.push({
      id: `${templateId}-${i + 1}`,
      name: template.name,
      version: versionNum,
      content: i % 2 === 0 ? 
        '1. 章节中增加、增加报告三字，并将注意文档放在最后面；2. 章节2引用国标9.1中GB 5235与新方法GB 5235A、增加GB/Z 192、GB 10158两份标准。' : 
        '章节2引用国标GB/Z192、GB 10158两份标准；章节3.13增加GB/Z 192标准、增加GB 9433标准的名称。根据内部审核组件软件VerifyHDL编程安全要求的规范及名称。',
      status,
      reviewReason: status === '驳回' ? '格式不符合要求' : '',
      updater: i % 2 === 0 ? 'Zoco' : 'Tom',
      updateTime: Random.datetime('yyyy-MM-dd HH:mm:ss')
    })
  }
  
  templateVersionHistory.set(templateId, versions)
  return versions
}

const mockData: MockMethod[] = [
  {
    url: '/api/templates/categories',
    method: 'get',
    response: () => {
      return {
        code: 200,
        data: [
          { label: '标书模板/政府采购/工程类', value: '标书模板/政府采购/工程类' },
          { label: '标书模板/政府采购/服务类', value: '标书模板/政府采购/服务类' },
          { label: '标书模板/政府采购/货物类', value: '标书模板/政府采购/货物类' },
          { label: '标书模板/企业招标/技术标', value: '标书模板/企业招标/技术标' },
          { label: '标书模板/企业招标/商务标', value: '标书模板/企业招标/商务标' },
          { label: '文档模板/方案文档/技术方案', value: '文档模板/方案文档/技术方案' },
          { label: '文档模板/方案文档/实施方案', value: '文档模板/方案文档/实施方案' },
          { label: '文档模板/合同文档/合同正文', value: '文档模板/合同文档/合同正文' },
          { label: '内容模板/公司介绍', value: '内容模板/公司介绍' },
          { label: '内容模板/产品介绍', value: '内容模板/产品介绍' },
          { label: '内容模板/案例介绍', value: '内容模板/案例介绍' }
        ]
      }
    }
  },
  {
    url: '/api/templates/statuses',
    method: 'get',
    response: () => {
      return {
        code: 200,
        data: [
          { label: '已发布', value: '已发布' },
          { label: '草稿', value: '草稿' },
          { label: '审核中', value: '审核中' },
          { label: '已废弃', value: '已废弃' }
        ]
      }
    }
  },
  {
    url: '/api/templates/types',
    method: 'get',
    response: () => {
      return {
        code: 200,
        data: [
          { label: '招标文件', value: '招标文件' },
          { label: '投标文件', value: '投标文件' },
          { label: '合同文本', value: '合同文本' },
          { label: '技术方案', value: '技术方案' },
          { label: '商务方案', value: '商务方案' },
          { label: '项目管理', value: '项目管理' }
        ]
      }
    }
  },
  {
    url: '/api/templates/owners',
    method: 'get',
    response: () => {
      return {
        code: 200,
        data: [
          { label: '张三', value: '张三' },
          { label: '李四', value: '李四' },
          { label: '王五', value: '王五' },
          { label: '赵六', value: '赵六' },
          { label: '钱七', value: '钱七' },
          { label: '孙八', value: '孙八' }
        ]
      }
    }
  },
  {
    url: '/api/template/list',
    method: 'get',
    response: ({ query }: { query: Record<string, any> }) => {
      const { pageNum = 1, pageSize = 12 } = query
      const startIndex = (pageNum - 1) * pageSize
      const endIndex = startIndex + pageSize
      const records = templates.slice(startIndex, endIndex)

      return {
        code: 200,
        message: 'success',
        data: {
          records,
          total: templates.length,
          pageNum: Number(pageNum),
          pageSize: Number(pageSize)
        }
      }
    }
  },
  {
    url: '/api/templates',
    method: 'post',
    response: (req: any) => {
      const templateData = req.body
      // 生成新的模板ID
      const newId = `template_${templates.length + 1}`
      const now = new Date().toISOString().replace('T', ' ').substring(0, 19)
      
      const newTemplate = {
        id: newId,
        ...templateData,
        createTime: now,
        updateTime: now
      }
      
      // 将新模板添加到列表中
      templates.unshift(newTemplate)
      
      return {
        code: 200,
        data: newTemplate,
        message: '模板创建成功'
      }
    }
  },
  {
    url: '/api/templates/:id',
    method: 'get',
    response: (req: any) => {
      const { id } = req.params
      const template = templates.find(item => item.id === id)
      
      if (!template) {
        return {
          code: 404,
          message: '模板不存在'
        }
      }
      
      return {
        code: 200,
        data: template
      }
    }
  },
  {
    url: '/api/templates/:id',
    method: 'put',
    response: (req: any) => {
      const { id } = req.params
      const templateData = req.body
      const index = templates.findIndex(item => item.id === id)
      
      if (index === -1) {
        return {
          code: 404,
          message: '模板不存在'
        }
      }
      
      const now = new Date().toISOString().replace('T', ' ').substring(0, 19)
      const updatedTemplate = {
        ...templates[index],
        ...templateData,
        updateTime: now
      }
      
      templates[index] = updatedTemplate
      
      return {
        code: 200,
        data: updatedTemplate,
        message: '模板更新成功'
      }
    }
  },
  {
    url: '/api/templates/:id',
    method: 'delete',
    response: (req: any) => {
      const { id } = req.params
      const index = templates.findIndex(item => item.id === id)
      
      if (index === -1) {
        return {
          code: 404,
          message: '模板不存在'
        }
      }
      
      templates.splice(index, 1)
      
      return {
        code: 200,
        message: '模板删除成功'
      }
    }
  },
  {
    url: '/api/templates/:id/copy',
    method: 'post',
    response: (req: any) => {
      const { id } = req.params
      const template = templates.find(item => item.id === id)
      
      if (!template) {
        return {
          code: 404,
          message: '模板不存在'
        }
      }
      
      const now = new Date().toISOString().replace('T', ' ').substring(0, 19)
      const newId = `template_${templates.length + 1}`
      
      const newTemplate = {
        ...template,
        id: newId,
        title: `${template.title} - 副本`,
        status: '草稿',
        createTime: now,
        updateTime: now
      }
      
      templates.unshift(newTemplate)
      
      return {
        code: 200,
        data: newTemplate,
        message: '模板复制成功'
      }
    }
  },
  {
    url: '/api/users/search',
    method: 'get',
    response: (req: any) => {
      const { keyword } = req.query
      const users = [
        { label: '张三', value: '张三' },
        { label: '李四', value: '李四' },
        { label: '王五', value: '王五' },
        { label: '赵六', value: '赵六' },
        { label: '钱七', value: '钱七' },
        { label: '孙八', value: '孙八' },
        { label: '周九', value: '周九' },
        { label: '吴十', value: '吴十' }
      ]
      
      if (!keyword) {
        return {
          code: 200,
          data: users.slice(0, 5)
        }
      }
      
      const filteredUsers = users.filter(user => 
        user.label.includes(keyword)
      )
      
      return {
        code: 200,
        data: filteredUsers
      }
    }
  },
  {
    url: '/api/template/types/tree',
    method: 'get',
    response: () => {
      return {
        code: 200,
        data: templateTypes
      }
    }
  },
  {
    url: '/api/template/types',
    method: 'post',
    response: (req: any) => {
      const data = req.body
      const now = new Date().toISOString().replace('T', ' ').substring(0, 19)
      const newId = Math.max(...templateTypes.flat().map(t => t.id)) + 1
      
      const newType = {
        id: newId,
        tenantId: 0,
        ...data,
        createTime: now,
        updateTime: now,
        createUid: 1,
        updateUid: 1
      }
      
      if (data.parentId) {
        const parent = templateTypes.find(t => t.id === data.parentId)
        if (parent) {
          parent.children = parent.children || []
          parent.children.push(newType)
        }
      } else {
        templateTypes.push(newType)
      }
      
      return {
        code: 200,
        data: newType,
        message: '新增成功'
      }
    }
  },
  {
    url: '/api/template/types/:id',
    method: 'put',
    response: (req: any) => {
      const { id } = req.params
      const data = req.body
      const now = new Date().toISOString().replace('T', ' ').substring(0, 19)
      
      const updateNode = (nodes: any[]) => {
        for (const node of nodes) {
          if (node.id === parseInt(id)) {
            Object.assign(node, data, {
              updateTime: now,
              updateUid: 1
            })
            return true
          }
          if (node.children && updateNode(node.children)) {
            return true
          }
        }
        return false
      }
      
      if (updateNode(templateTypes)) {
        return {
          code: 200,
          message: '更新成功'
        }
      }
      
      return {
        code: 404,
        message: '分类不存在'
      }
    }
  },
  {
    url: '/api/template/types/:id',
    method: 'delete',
    response: (req: any) => {
      const { id } = req.params
      
      const deleteNode = (nodes: any[]) => {
        const index = nodes.findIndex(node => node.id === parseInt(id))
        if (index > -1) {
          nodes.splice(index, 1)
          return true
        }
        for (const node of nodes) {
          if (node.children && deleteNode(node.children)) {
            return true
          }
        }
        return false
      }
      
      if (deleteNode(templateTypes)) {
        return {
          code: 200,
          message: '删除成功'
        }
      }
      
      return {
        code: 404,
        message: '分类不存在'
      }
    }
  },
  {
    url: '/api/template/version/history',
    method: 'get',
    response: (req: any) => {
      const { templateId, pageNum = 1, pageSize = 10 } = req.query
      
      if (!templateId) {
        return {
          code: 400,
          message: '缺少模板ID参数',
          data: null
        }
      }
      
      const versions = generateTemplateVersionHistory(templateId)
      
      // 分页处理
      const start = (pageNum - 1) * pageSize
      const end = start + pageSize
      const pagedVersions = versions.slice(start, end)
      
      return {
        code: 200,
        message: 'success',
        data: {
          list: pagedVersions,
          total: versions.length
        }
      }
    }
  }
]

export default mockData 