<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '240px'" class="sidebar">
      <div class="logo-container">
        <el-icon class="logo-icon" :size="28">
          <img src="/assets/svg/logo.svg" alt="logo" />
        </el-icon>
        <div class="logo-title-container">
          <h1 class="logo-title" v-show="!isCollapse">景智标书</h1>
          <span class="logo-subtitle" v-show="!isCollapse">人工智能标书管理平台</span>
        </div>
      </div>
      <el-menu
        :router="true"
        :default-active="activeMenu"
        class="el-menu-vertical"
        :collapse="isCollapse"
        background-color="#fff"
        text-color="#4B5563"
        active-text-color="#6366F1"
      >
        <sidebar-item
          v-for="route in routes"
          :key="route.path"
          :item="route"
          :base-path="route.path"
          :is-collapse="isCollapse"
        />
      </el-menu>
      <div class="version-info" v-show="!isCollapse">
        <span class="version-label">当前版本</span>
        <span class="version-number">v1.0.0 Beta</span>
      </div>
    </el-aside>
    <el-container>
      <el-header height="60px" class="header">
        <div class="header-left">
          <el-icon class="toggle-sidebar" @click="toggleSidebar">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <breadcrumb />
        </div>
        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              {{ userInfo.name }}
              <el-icon><CaretBottom /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import SidebarItem from './components/SidebarItem.vue'
import Breadcrumb from './components/Breadcrumb.vue'
import { Fold, Expand, CaretBottom } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 侧边栏折叠状态
const isCollapse = ref(false)
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

// 获取路由配置
const routes = computed(() => {
  return router.options.routes.filter(route => !route.meta?.hidden)
})

// 当前激活的菜单
const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) {
    return meta.activeMenu as string
  }
  return path
})

// 用户信息
const userInfo = computed(() => {
  return authStore.user || { name: '未登录' }
})

// 退出登录
const handleLogout = async () => {
  await authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  background-color: #f9fafb;
}

.sidebar {
  background-color: #fff;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.logo-container {
  height: 60px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #e5e7eb;
  overflow: hidden;
}

.logo-icon {
  min-width: 28px;
  margin-right: 12px;
}

.logo-icon img {
  width: 100%;
  height: 100%;
}

.logo-title-container {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
}

.logo-title {
  font-size: 18px;
  font-weight: 600;
  color: transparent;
  margin: 0;
  white-space: nowrap;
  background-image: linear-gradient(to right, #2563eb, #9333ea);
  -webkit-background-clip: text;
  background-clip: text;
}

.logo-subtitle {
  font-size: 12px;
  color: #6B7280;
  white-space: nowrap;
}

.el-menu-vertical {
  border-right: none;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

.version-info {
  padding: 16px;
  border-top: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  font-size: 12px;
}

.version-label {
  color: #6B7280;
}

.version-number {
  color: #111827;
  font-weight: 500;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
}

.toggle-sidebar {
  font-size: 20px;
  cursor: pointer;
  margin-right: 20px;
  color: #4B5563;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.new-doc-btn {
  background: linear-gradient(135deg, #6366F1 0%, #8B5CF6 100%);
  border: none;
  border-radius: 6px;
  padding: 0 16px;
  height: 36px;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.3s;
}

.new-doc-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: #4B5563;
  font-size: 14px;
}

.main-content {
  padding: 20px;
  background-color: #f9fafb;
  overflow-y: auto;
}

.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style> 