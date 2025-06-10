import type { MockMethod } from 'vite-plugin-mock'
import mockjs from 'mockjs'
const { Random } = mockjs

// 项目分类数据
const projectCategories = [
  {
    id: 1,
    name: '产品设计',
    icon: 'Document',
    children: [
      {
        id: 11,
        name: 'UI/UX设计',
        children: [
          { id: 111, name: '移动应用设计' },
          { id: 112, name: '网站设计' }
        ]
      },
      {
        id: 12,
        name: '产品规划',
        children: [
          { id: 121, name: '需求分析' },
          { id: 122, name: '原型设计' }
        ]
      }
    ]
  },
  {
    id: 2,
    name: '技术开发',
    icon: 'Setting',
    children: [
      {
        id: 21,
        name: '前端开发',
        children: [
          { id: 211, name: 'Web前端' },
          { id: 212, name: '移动前端' }
        ]
      },
      {
        id: 22,
        name: '后端开发',
        children: [
          { id: 221, name: 'API开发' },
          { id: 222, name: '数据库' }
        ]
      }
    ]
  },
  {
    id: 3,
    name: '市场营销',
    icon: 'Promotion',
    children: [
      {
        id: 31,
        name: '品牌推广',
        children: []
      },
      {
        id: 32,
        name: '活动策划',
        children: []
      }
    ]
  }
]

// 生成项目数据
const generateProjects = (count: number) => {
  const statusOptions = [
    { status: '进行中', statusClass: 'status-in-progress', color: '#60A5FA' },
    { status: '已完成', statusClass: 'status-completed', color: '#10B981' },
    { status: '待审核', statusClass: 'status-review', color: '#F59E0B' },
    { status: '已暂停', statusClass: 'status-paused', color: '#6B7280' },
    { status: '高风险', statusClass: 'status-risk', color: '#EF4444' }
  ]
  
  const priorityOptions = [
    { priority: '高优先级', priorityClass: 'priority-high', color: '#F43F5E' },
    { priority: '中优先级', priorityClass: 'priority-medium', color: '#F59E0B' },
    { priority: '低优先级', priorityClass: 'priority-low', color: '#10B981' }
  ]
  
  const riskLevels = ['低', '中', '高', '严重']
  
  const projects = []
  for (let i = 1; i <= count; i++) {
    const statusOption = statusOptions[Math.floor(Math.random() * statusOptions.length)]
    const priorityOption = priorityOptions[Math.floor(Math.random() * priorityOptions.length)]
    const categoryId = Math.floor(Math.random() * 3) + 1
    const subcategoryId = Math.floor(Math.random() * 2) + 1
    const categoryPath = `${categoryId}${subcategoryId}`
    
    projects.push({
      id: i,
      title: Random.ctitle(4, 8),
      description: Random.cparagraph(1, 3),
      status: statusOption.status,
      statusClass: statusOption.statusClass,
      statusColor: statusOption.color,
      priority: priorityOption.priority,
      priorityClass: priorityOption.priorityClass,
      priorityColor: priorityOption.color,
      progress: Math.floor(Math.random() * 100),
      progressColor: statusOption.color,
      date: Random.date('yyyy-MM-dd'),
      dueDate: Random.date('yyyy-MM-dd'),
      budget: Math.floor(Math.random() * 200) + 50,
      client: Random.cname() + Random.ctitle(2, 4),
      categoryId: categoryPath,
      categoryName: getCategoryName(categoryPath),
      risk: riskLevels[Math.floor(Math.random() * riskLevels.length)],
      team: generateTeamMembers(),
      members: [
        'https://placeholder.pics/svg/30/DEDEDE/555555/U',
        'https://placeholder.pics/svg/30/DEDEDE/555555/U'
      ],
      tags: generateTags(),
      documents: Math.floor(Math.random() * 30) + 5
    })
  }
  return projects
}

// 获取分类名称
function getCategoryName(categoryId: string) {
  if (categoryId.length < 2) return ''
  
  const mainCatId = parseInt(categoryId[0])
  const subCatId = parseInt(categoryId[1])
  
  const mainCategory = projectCategories.find(cat => cat.id === mainCatId)
  if (!mainCategory) return ''
  
  const subCategory = mainCategory.children.find(cat => cat.id === mainCatId * 10 + subCatId)
  if (!subCategory) return ''
  
  return `${mainCategory.name} / ${subCategory.name}`
}

// 生成团队成员
function generateTeamMembers() {
  const members = []
  const count = Math.floor(Math.random() * 3) + 1
  
  for (let i = 0; i < count; i++) {
    members.push(Random.cname().substr(0, 2))
  }
  
  return members.join(' ')
}

// 生成标签
function generateTags(): string[] {
  const allTags = ['UI设计', '移动端', '用户体验', '后台', '管理系统', 'API', '数据分析', '营销', '品牌', '活动']
  const tags: string[] = []
  const count = Math.floor(Math.random() * 3) + 1
  
  for (let i = 0; i < count; i++) {
    const randomIndex = Math.floor(Math.random() * allTags.length)
    if (!tags.includes(allTags[randomIndex])) {
      tags.push(allTags[randomIndex])
    }
  }
  
  return tags
}

const projectList = generateProjects(50)

// 项目表单选项数据
const formOptions = {
  projectCategories: ['政府项目', '企业项目', '事业单位项目', '国际项目'],
  statusOptions: ['进行中', '待审核', '已暂停', '已完成'],
  riskLevels: ['低', '中', '高', '严重'],
  managers: ['张三', '李四', '王五', '赵六'],
  technicalTeam: {
    leads: ['赵六', '钱七', '孙八'],
    members: ['赵六', '钱七', '孙八', '周九', '吴十']
  },
  businessTeam: {
    leads: ['李一', '王二', '张三'],
    members: ['李一', '王二', '张三', '赵四', '钱五']
  },
  legalTeam: {
    leads: ['陈一', '林二'],
    members: ['陈一', '林二', '黄三']
  },
  externalExperts: ['刘教授', '张工程师', '王顾问', '李专家', '赵博士'],
  qualifications: [
    '营业执照',
    '资质证书',
    '纳税证明',
    '社保缴纳证明',
    '银行资信证明',
    '无重大违法记录证明'
  ],
  projectTags: [
    '政府采购',
    '信息化建设',
    '软件开发',
    '系统集成',
    '硬件采购',
    '咨询服务',
    '工程建设',
    '运维服务'
  ]
}

// 生成选项数据
const generateOptions = (count: number, prefix: string) => {
  return Array.from({ length: count }, (_, index) => ({
    label: `${prefix}${index + 1}`,
    value: `${prefix}${index + 1}`
  }))
}

const mockData: MockMethod[] = [
  {
    url: '/api/project/categories',
    method: 'get',
    response: () => {
      return {
        code: 200,
        data: projectCategories
      }
    }
  },
  {
    url: '/api/project/list',
    method: 'get',
    response: (req: any) => {
      const { page = 1, limit = 10, categoryId, status, risk, keyword } = req.query
      
      let filteredList = [...projectList]
      
      // 按分类筛选
      if (categoryId) {
        filteredList = filteredList.filter(item => item.categoryId.startsWith(categoryId))
      }
      
      // 按状态筛选
      if (status) {
        filteredList = filteredList.filter(item => item.status === status)
      }
      
      // 按风险等级筛选
      if (risk) {
        filteredList = filteredList.filter(item => item.risk === risk)
      }
      
      // 关键词搜索
      if (keyword) {
        const key = keyword.toLowerCase()
        filteredList = filteredList.filter(item => 
          item.title.toLowerCase().includes(key) || 
          item.description.toLowerCase().includes(key) ||
          item.client.toLowerCase().includes(key)
        )
      }
      
      // 分页
      const start = (page - 1) * limit
      const end = start + limit
      const records = filteredList.slice(start, end)
      return {
        code: 200,
        data: {
          records: records,
          total: filteredList.length,
          page: Number(page),
          limit: Number(limit)
        }
      }
    }
  },
  {
    url: '/api/project/form-options',
    method: 'get',
    response: () => {
      return {
        code: 200,
        data: formOptions
      }
    }
  },
  {
    url: '/api/project/create',
    method: 'post',
    response: (req: any) => {
      const projectData = req.body
      // 生成新的项目ID
      const newId = projectList.length + 1
      const newProject = {
        id: newId,
        ...projectData,
        // 添加一些默认值
        statusClass: 'status-in-progress',
        statusColor: '#60A5FA',
        progressColor: '#60A5FA',
        members: [
          'https://placeholder.pics/svg/30/DEDEDE/555555/U',
          'https://placeholder.pics/svg/30/DEDEDE/555555/U'
        ],
        documents: 0
      }
      
      // 将新项目添加到列表中
      projectList.unshift(newProject)
      
      return {
        code: 200,
        data: newProject,
        message: '项目创建成功'
      }
    }
  },
  // 获取项目状态选项
  {
    url: '/api/project/status-options',
    method: 'get',
    response: () => {
      return {
        code: 200,
        data: [
          { label: '进行中', value: '进行中' },
          { label: '已完成', value: '已完成' },
          { label: '待审核', value: '待审核' },
          { label: '已暂停', value: '已暂停' }
        ]
      }
    }
  },

  // 获取项目风险选项
  {
    url: '/api/project/risk-options',
    method: 'get',
    response: () => {
      return {
        code: 200,
        data: [
          { label: '低', value: '低' },
          { label: '中', value: '中' },
          { label: '高', value: '高' },
          { label: '严重', value: '严重' }
        ]
      }
    }
  },

  // 获取项目分类选项
  {
    url: '/api/project/category-options',
    method: 'get',
    response: () => {
      return {
        code: 200,
        data: generateOptions(5, '项目分类')
      }
    }
  },

  // 获取团队成员选项
  {
    url: '/api/project/team-member-options',
    method: 'get',
    response: () => {
      return {
        code: 200,
        data: generateOptions(10, '成员')
      }
    }
  },

  // 获取技术团队选项
  {
    url: '/api/project/technical-team-options',
    method: 'get',
    response: () => {
      return {
        code: 200,
        data: {
          leads: generateOptions(3, '技术负责人'),
          members: generateOptions(8, '技术成员')
        }
      }
    }
  },

  // 获取商务团队选项
  {
    url: '/api/project/business-team-options',
    method: 'get',
    response: () => {
      return {
        code: 200,
        data: {
          leads: generateOptions(2, '商务负责人'),
          members: generateOptions(5, '商务成员')
        }
      }
    }
  },

  // 获取法务团队选项
  {
    url: '/api/project/legal-team-options',
    method: 'get',
    response: () => {
      return {
        code: 200,
        data: {
          leads: generateOptions(2, '法务负责人'),
          members: generateOptions(4, '法务成员')
        }
      }
    }
  },

  // 获取外部专家选项
  {
    url: '/api/project/external-expert-options',
    method: 'get',
    response: () => {
      return {
        code: 200,
        data: generateOptions(6, '专家')
      }
    }
  },

  // 获取资质要求选项
  {
    url: '/api/project/qualification-options',
    method: 'get',
    response: () => {
      return {
        code: 200,
        data: [
          { label: 'ISO9001认证', value: 'ISO9001认证' },
          { label: 'CMMI5认证', value: 'CMMI5认证' },
          { label: '高新技术企业', value: '高新技术企业' },
          { label: '软件企业认定', value: '软件企业认定' },
          { label: '信息系统集成资质', value: '信息系统集成资质' }
        ]
      }
    }
  },

  // 获取项目标签选项
  {
    url: '/api/project/tag-options',
    method: 'get',
    response: () => {
      return {
        code: 200,
        data: [
          { label: 'Web开发', value: 'Web开发' },
          { label: '移动应用', value: '移动应用' },
          { label: '人工智能', value: '人工智能' },
          { label: '大数据', value: '大数据' },
          { label: '云计算', value: '云计算' },
          { label: '物联网', value: '物联网' }
        ]
      }
    }
  }
]

export default mockData 