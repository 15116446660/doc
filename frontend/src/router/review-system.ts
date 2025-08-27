// router/review-system.ts
export const reviewSystemRoutes = [
  {
    path: '/review-system',
    component: () => import('@/layout/index.vue'),
    meta: {
      title: '评审系统',
      icon: 'DocumentChecked',
      requiresAuth: true
    },
    children: [
      // 📊 评审总览仪表盘 (P0)
      {
        path: 'dashboard',
        name: 'ReviewDashboard',
        component: () => import('@/views/review-system/dashboard/index.vue'),
        meta: {
          title: '评审总览',
          priority: 'P0',
          permissions: ['review:dashboard:view']
        }
      },

      // 📝 评审任务管理 (P0)
      {
        path: 'tasks',
        name: 'TaskManagement',
        component: () => import('@/views/review-system/task-management/index.vue'),
        meta: {
          title: '任务管理',
          priority: 'P0',
          permissions: ['review:task:view']
        },
        children: [
          {
            path: 'create',
            name: 'TaskCreate',
            component: () => import('@/views/review-system/task-management/create.vue'),
            meta: {
              title: '创建任务',
              priority: 'P0',
              permissions: ['review:task:create']
            }
          },
          {
            path: ':id',
            name: 'TaskDetail',
            component: () => import('@/views/review-system/task-management/detail.vue'),
            meta: {
              title: '任务详情',
              priority: 'P0',
              permissions: ['review:task:view']
            }
          }
        ]
      },

      // 👥 专家资源管理 (P0/P1)
      {
        path: 'experts',
        name: 'ExpertManagement',
        component: () => import('@/views/review-system/expert-management/index.vue'),
        meta: {
          title: '专家管理',
          priority: 'P1',
          permissions: ['review:expert:view']
        },
        children: [
          {
            path: ':id/profile',
            name: 'ExpertProfile',
            component: () => import('@/views/review-system/expert-management/profile.vue'),
            meta: {
              title: '专家档案',
              priority: 'P1',
              permissions: ['review:expert:view']
            }
          }
        ]
      },

      // 🔄 流程配置管理 (P1)
      {
        path: 'workflows',
        name: 'WorkflowConfig',
        component: () => import('@/views/review-system/workflow-config/index.vue'),
        meta: {
          title: '流程配置',
          priority: 'P1',
          permissions: ['review:workflow:view']
        },
        children: [
          {
            path: 'designer/:id?',
            name: 'WorkflowDesigner',
            component: () => import('@/views/review-system/workflow-config/designer.vue'),
            meta: {
              title: '流程设计器',
              priority: 'P1',
              permissions: ['review:workflow:edit']
            }
          }
        ]
      },

      // 💻 评审工作台 (P0)
      {
        path: 'workspace/:taskId',
        name: 'ReviewWorkspace',
        component: () => import('@/views/review-system/review-workspace/index.vue'),
        meta: {
          title: '评审工作台',
          priority: 'P0',
          permissions: ['review:workspace:access']
        }
      },

      // 📋 问题清单管理 (P0)
      {
        path: 'issues',
        name: 'IssueManagement',
        component: () => import('@/views/review-system/issue-management/index.vue'),
        meta: {
          title: '问题管理',
          priority: 'P0',
          permissions: ['review:issue:view']
        },
        children: [
          {
            path: ':id',
            name: 'IssueDetail',
            component: () => import('@/views/review-system/issue-management/detail.vue'),
            meta: {
              title: '问题详情',
              priority: 'P0',
              permissions: ['review:issue:view']
            }
          }
        ]
      },

      // ⚙️ 系统配置管理 (P0)
      {
        path: 'settings',
        name: 'SystemSettings',
        component: () => import('@/views/review-system/settings/index.vue'),
        meta: {
          title: '系统设置',
          priority: 'P0',
          permissions: ['review:settings:view']
        },
        children: [
          {
            path: 'permissions',
            name: 'PermissionSettings',
            component: () => import('@/views/review-system/settings/permissions.vue'),
            meta: {
              title: '权限管理',
              priority: 'P0',
              permissions: ['review:settings:permissions']
            }
          },
          {
            path: 'notifications',
            name: 'NotificationSettings',
            component: () => import('@/views/review-system/settings/notifications.vue'),
            meta: {
              title: '通知配置',
              priority: 'P0',
              permissions: ['review:settings:notifications']
            }
          }
        ]
      }
    ]
  }
]
