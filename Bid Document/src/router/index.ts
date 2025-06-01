import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/',
    redirect: '/dashboard',
    meta: { hidden: true }
  },
  {
    path: '/dashboard',
    component: () => import('../layout/index.vue'),
    redirect: '/dashboard/index',
    meta: { title: '仪表盘', icon: 'Odometer', alwaysShow: true },
    children: [
      {
        path: 'index',
        name: 'Dashboard',
        component: () => import('../views/dashboard/index.vue'),
        meta: { title: '仪表盘' }
      }
    ]
  },
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
        component: () => import('../views/project/overview.vue'),
        meta: { title: '项目概览' }
      },
      {
        path: 'create',
        name: 'ProjectCreate',
        component: () => import('../views/project/create.vue'),
        meta: { title: '项目创建' }
      },
      {
        path: 'list',
        name: 'ProjectList',
        component: () => import('../views/project/list.vue'),
        meta: { title: '项目列表' }
      },
      {
        path: 'board',
        name: 'ProjectBoard',
        component: () => import('../views/project/board.vue'),
        meta: { title: '项目看板' }
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
  const token = localStorage.getItem('token')
  if (to.path === '/login') {
    next()
  } else {
    if (!token) {
      next('/login')
    } else {
      next()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})

export default router 