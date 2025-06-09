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
      ownerAvatar: `https://placeholder.pics/svg/30/${Random.hex()}/FFFFFF/${Random.character('upper')}`,
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

const templateList = generateTemplates(50)

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
      const list = templateList.slice(startIndex, endIndex)

      return {
        code: 200,
        message: 'success',
        data: {
          list,
          total: templateList.length,
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
      const newId = `template_${templateList.length + 1}`
      const now = new Date().toISOString().replace('T', ' ').substring(0, 19)
      
      const newTemplate = {
        id: newId,
        ...templateData,
        createTime: now,
        updateTime: now
      }
      
      // 将新模板添加到列表中
      templateList.unshift(newTemplate)
      
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
      const template = templateList.find(item => item.id === id)
      
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
      const index = templateList.findIndex(item => item.id === id)
      
      if (index === -1) {
        return {
          code: 404,
          message: '模板不存在'
        }
      }
      
      const now = new Date().toISOString().replace('T', ' ').substring(0, 19)
      const updatedTemplate = {
        ...templateList[index],
        ...templateData,
        updateTime: now
      }
      
      templateList[index] = updatedTemplate
      
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
      const index = templateList.findIndex(item => item.id === id)
      
      if (index === -1) {
        return {
          code: 404,
          message: '模板不存在'
        }
      }
      
      templateList.splice(index, 1)
      
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
      const template = templateList.find(item => item.id === id)
      
      if (!template) {
        return {
          code: 404,
          message: '模板不存在'
        }
      }
      
      const now = new Date().toISOString().replace('T', ' ').substring(0, 19)
      const newId = `template_${templateList.length + 1}`
      
      const newTemplate = {
        ...template,
        id: newId,
        title: `${template.title} - 副本`,
        status: '草稿',
        createTime: now,
        updateTime: now
      }
      
      templateList.unshift(newTemplate)
      
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
  }
]

export default mockData 