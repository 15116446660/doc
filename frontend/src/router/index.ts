import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { useAuthStore } from '@/store/auth'
import { reviewSystemRoutes } from './review-system'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    redirect: '/dashboard',
    meta: { hidden: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../layout/index.vue'),
    meta: { title: '仪表盘', icon: 'Odometer' },
    children: [
      {
        path: '',
        name: 'DashboardIndex',
        component: () => import('../views/dashboard/index.vue'),
        meta: { hidden: true }
      }
    ]
  },
  ...reviewSystemRoutes,
  {
    path: '/project',
    name: 'Project',
    component: () => import('../layout/index.vue'),
    redirect: '/project/overview',
    meta: { title: '项目管理', icon: 'Folder' },
    children: [
      {
        path: 'overview',
        name: 'ProjectOverview',
        component: () => import('../views/project/overview/index.vue'),
        meta: { title: '项目概览' }
      },
      {
        path: 'create',
        name: 'ProjectCreate',
        component: () => import('../views/project/create/index.vue'),
        meta: { title: '项目创建' }
      },
      {
        path: 'list',
        name: 'ProjectList',
        component: () => import('../views/project/list/index.vue'),
        meta: {
          title: '项目列表',
          icon: 'list'
        }
      },
      {
        path: 'board',
        name: 'ProjectBoard',
        component: () => import('../views/project/board/index.vue'),
        meta: { title: '项目看板' }
      },
      {
        path: 'hierarchy',
        name: 'ProjectHierarchy',
        component: () => import('../views/project/hierarchy/index.vue'),
        meta: {
          title: '层级管理',
          icon: 'Operation'
        }
      },
      {
        path: 'department',
        name: 'ProjectDepartment',
        component: () => import('../views/project/department/index.vue'),
        meta: {
          title: '部门管理',
          icon: 'OfficeBuilding'
        }
      },
      {
        path: 'category',
        name: 'ProjectCategory',
        component: () => import('../views/project/category/index.vue'),
        meta: {
          title: '品类管理',
          icon: 'Collection'
        }
      },
      {
        path: 'detail/:id',
        name: 'ProjectDetail',
        component: () => import('../views/project/detail/index.vue'),
        meta: {
          title: '项目详情',
          icon: 'document',
          hidden: true
        }
      }
    ]
  },
  {
    path: '/document',
    name: 'Document',
    component: () => import('../layout/index.vue'),
    meta: { title: '标书管理', icon: 'Document' },
    children: [
      {
        path: 'library',
        name: 'DocumentLibrary',
        component: () => import('@/views/document/library/index.vue'),
        meta: {
          title: '文档库',
          icon: 'Folder'
        }
      },
      {
        path: 'library/:projectId/documents',
        name: 'DocumentList',
        component: () => import('@/views/document/project/document/list/index.vue'),
        meta: {
          title: '文档列表',
          icon: 'Document',
          hidden: true,
          activeMenu: '/document/library'
        }
      },
      {
        path: 'edit',
        name: 'DocumentEdit',
        component: () => import('../views/document/edit/index.vue'),
        meta: { title: '标书编辑' }
      },
      {
        path: 'template',
        name: 'DocumentTemplate',
        component: () => import('../views/template/list/index.vue'),
        meta: { title: '模板中心' },
        redirect: '/document/template/list',
        children: [
          {
            path: 'list',
            name: 'TemplateList',
            component: () => import('../views/template/list/index.vue'),
            meta: { title: '模板列表' }
          },
          {
            path: 'detail/:id',
            name: 'TemplateDetail',
            component: () => import('../views/template/detail/index.vue'),
            meta: { title: '模板详情', hidden: true }
          }
        ]
      },
      {
        path: 'content',
        name: 'DocumentContent',
        component: () => import('../views/document/content/index.vue'),
        meta: { title: '内容库' }
      },
      {
        path: 'approval',
        name: 'DocumentApproval',
        component: () => import('../views/document/approval/index.vue'),
        meta: { title: '审批中心' }
      },
      {
        path: 'quality-check',
        name: 'QualityCheck',
        component: () => import('@/views/quality-check/index.vue'),
        meta: {
          title: '质量检查',
          icon: 'CircleCheck'
        }
      }
    ]
  },
  {
    path: '/tools',
    name: 'Tools',
    component: () => import('../layout/index.vue'),
    redirect: '/tools/analysis',
    meta: { title: '智能工具', icon: 'Tools' },
    children: [
      {
        path: 'analysis',
        name: 'ToolsAnalysis',
        component: () => import('../views/tools/analysis/index.vue'),
        meta: { title: '智能分析' }
      },
      {
        path: 'compliance',
        name: 'ToolsCompliance',
        component: () => import('../views/tools/compliance/index.vue'),
        meta: { title: '合规检查' }
      },
      {
        path: 'risk',
        name: 'ToolsRisk',
        component: () => import('../views/tools/risk/index.vue'),
        meta: { title: '风险预警' }
      },
      {
        path: 'failure',
        name: 'ToolsFailure',
        component: () => import('../views/tools/failure/index.vue'),
        meta: { title: '废标分析' }
      }
    ]
  },
  {
    path: '/companion',
    name: 'Companion',
    component: () => import('../layout/index.vue'),
    redirect: '/companion/space',
    meta: { title: '陪标管理', icon: 'Files' },
    children: [
      {
        path: 'space',
        name: 'CompanionSpace',
        component: () => import('../views/companion/space/index.vue'),
        meta: { title: '多标书空间' }
      },
      {
        path: 'duplicate',
        name: 'CompanionDuplicate',
        component: () => import('../views/companion/duplicate/index.vue'),
        meta: { title: '查重中心' }
      },
      {
        path: 'difference',
        name: 'CompanionDifference',
        component: () => import('../views/companion/difference/index.vue'),
        meta: { title: '差异化工具' }
      }
    ]
  },
  {
    path: '/knowledge',
    name: 'Knowledge',
    component: () => import('../layout/index.vue'),
    redirect: '/knowledge/regulation',
    meta: { title: '知识中心', icon: 'Reading' },
    children: [
      {
        path: 'regulation',
        name: 'KnowledgeRegulation',
        component: () => import('../views/knowledge/regulation/index.vue'),
        meta: { title: '法规政策' }
      },
      {
        path: 'cases',
        name: 'KnowledgeCases',
        component: () => import('../views/knowledge/cases/index.vue'),
        meta: { title: '案例库' }
      },
      {
        path: 'industry',
        name: 'KnowledgeIndustry',
        component: () => import('../views/knowledge/industry/index.vue'),
        meta: { title: '行业知识' }
      },
      {
        path: 'qa',
        name: 'KnowledgeQA',
        component: () => import('../views/knowledge/qa/index.vue'),
        meta: { title: '智能问答' }
      }
    ]
  },
  {
    path: '/analysis',
    name: 'Analysis',
    component: () => import('../layout/index.vue'),
    redirect: '/analysis/bid',
    meta: { title: '数据分析', icon: 'DataLine' },
    children: [
      {
        path: 'bid',
        name: 'AnalysisBid',
        component: () => import('../views/analysis/bid/index.vue'),
        meta: { title: '投标数据' }
      },
      {
        path: 'market',
        name: 'AnalysisMarket',
        component: () => import('../views/analysis/market/index.vue'),
        meta: { title: '市场分析' }
      },
      {
        path: 'report',
        name: 'AnalysisReport',
        component: () => import('../views/analysis/report/index.vue'),
        meta: { title: '自定义报表' }
      }
    ]
  },
  {
    path: '/system',
    name: 'System',
    component: () => import('../layout/index.vue'),
    redirect: '/system/user',
    meta: { title: '系统管理', icon: 'Setting' },
    children: [
      {
        path: 'user',
        name: 'SystemUser',
        component: () => import('../views/system/user/index.vue'),
        meta: { title: '用户权限' }
      },
      {
        path: 'organization',
        name: 'SystemOrganization',
        component: () => import('../views/system/organization/index.vue'),
        meta: { title: '组织架构' }
      },
      {
        path: 'workflow',
        name: 'SystemWorkflow',
        component: () => import('../views/system/workflow/index.vue'),
        meta: { title: '工作流配置' }
      },
      {
        path: 'integration',
        name: 'SystemIntegration',
        component: () => import('../views/system/integration/index.vue'),
        meta: { title: '系统集成' }
      },
      {
        path: 'ai',
        name: 'SystemAI',
        component: () => import('../views/system/ai/index.vue'),
        meta: { title: 'AI配置' }
      }
    ]
  },
  {
    path: '/ai-assistant',
    name: 'AIAssistant',
    component: () => import('../layout/index.vue'),
    meta: { title: 'AI助手', icon: 'ChatDotRound' },
    children: [
      {
        path: 'demo',
        name: 'AIAssistantDemo',
        component: () => import('../views/AIAssistantDemo.vue'),
        meta: { title: 'AI对话助手演示' }
      }
    ]
  },
  {
    path: '/ai-analysis',
    name: 'AIAnalysis',
    component: () => import('../layout/index.vue'),
    redirect: '/ai-analysis/dashboard',
    meta: { title: 'AI智能分析', icon: 'MagicStick' },
    children: [
      {
        path: 'dashboard',
        name: 'AIAnalysisDashboard',
        component: () => import('@/views/ai-analysis/dashboard/index.vue'),
        meta: { title: 'AI分析概览' }
      },
      {
        path: 'content-insertion',
        name: 'AIContentInsertion',
        component: () => import('@/views/ai-analysis/content-insertion/index.vue'),
        meta: { title: 'AI内容反向插入' }
      },
      {
        path: 'slice-tagging',
        name: 'AISliceTagging',
        component: () => import('@/views/ai-analysis/slice-tagging/index.vue'),
        meta: { title: '智能切片标记' }
      },
      {
        path: 'prompt-testing',
        name: 'AIPromptTesting',
        component: () => import('@/views/ai-analysis/prompt-testing/index.vue'),
        meta: { title: '提示词测试工具' }
      },
      {
        path: 'citation-marking',
        name: 'AICitationMarking',
        component: () => import('@/views/ai-analysis/citation-marking/index.vue'),
        meta: { title: '引用来源标记' }
      },
      {
        path: 'smart-formatting',
        name: 'AISmartFormatting',
        component: () => import('@/views/ai-analysis/smart-formatting/index.vue'),
        meta: { title: 'AI智能格式化' }
      },
      {
        path: 'long-text-interaction',
        name: 'AILongTextInteraction',
        component: () => import('@/views/ai-analysis/long-text-interaction/index.vue'),
        meta: { title: '长文本交互' }
      },
      {
        path: 'difference-comparison',
        name: 'AIDifferenceComparison',
        component: () => import('@/views/ai-analysis/difference-comparison/index.vue'),
        meta: { title: '文档差异对比' }
      },
      {
        path: 'review-task/:id',
        name: 'AIReviewTask',
        component: () => import('@/views/ai-analysis/review-task/index.vue'),
        meta: { title: 'AI评审任务', hidden: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guard
router.beforeEach((to, from, next) => {
  NProgress.start()
  const authStore = useAuthStore()
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)

  if (requiresAuth && !authStore.isAuthenticated) {
    next('/login')
  } else if (to.path === '/login' && authStore.isAuthenticated) {
    next('/')
  } else {
    next()
  }
})

router.afterEach(() => {
  NProgress.done()
})

export default router 