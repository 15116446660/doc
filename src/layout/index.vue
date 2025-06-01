<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="sidebar">
      <el-menu
        :router="true"
        :default-active="activeMenu"
        class="el-menu-vertical"
        :collapse="isCollapse"
      >
        <sidebar-item
          v-for="route in routes"
          :key="route.path"
          :item="route"
          :base-path="route.path"
        />
      </el-menu>
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
      <el-main>
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
  return route.path
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
}

.sidebar {
  background-color: #304156;
  transition: width 0.3s;
}

.el-menu-vertical {
  border-right: none;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.toggle-sidebar {
  font-size: 20px;
  cursor: pointer;
  margin-right: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
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