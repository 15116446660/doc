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
      const pageList = filteredList.slice(start, end)
      
      return {
        code: 200,
        data: {
          list: pageList,
          total: filteredList.length,
          page: Number(page),
          limit: Number(limit)
        }
      }
    }
  }
]

export default mockData 