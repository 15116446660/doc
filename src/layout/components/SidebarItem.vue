<template>
  <div v-if="!item.meta?.hidden">
    <!-- 没有子菜单的情况 -->
    <el-menu-item v-if="!hasChildren(item)" :index="resolvePath(basePath)" class="menu-item">
      <el-icon v-if="item.meta?.icon" class="menu-icon">
        <component :is="item.meta.icon" />
      </el-icon>
      <template #title>
        <span class="menu-title">{{ item.meta?.title }}</span>
        <span v-if="item.meta?.count" class="menu-count">{{ item.meta.count }}</span>
      </template>
    </el-menu-item>

    <!-- 有子菜单的情况 -->
    <el-sub-menu v-else :index="resolvePath(basePath)" class="menu-sub">
      <template #title>
        <el-icon v-if="item.meta?.icon" class="menu-icon">
          <component :is="item.meta.icon" />
        </el-icon>
        <span class="menu-title">{{ item.meta?.title }}</span>
        <span v-if="item.meta?.count" class="menu-count">{{ item.meta.count }}</span>
      </template>

      <sidebar-item
        v-for="child in item.children"
        :key="child.path"
        :item="child"
        :base-path="resolvePath(basePath)"
      />
    </el-sub-menu>
  </div>
</template>

<script setup lang="ts">
import { defineProps } from 'vue'
import type { RouteRecordRaw } from 'vue-router'
import path from 'path-browserify'

const props = defineProps<{
  item: RouteRecordRaw
  basePath: string
}>()

// 判断是否有子菜单
const hasChildren = (route: RouteRecordRaw) => {
  if (route.children) {
    return route.children.filter(child => !child.meta?.hidden).length > 0
  }
  return false
}

// 解析路由路径
const resolvePath = (routePath: string) => {
  if (props.item.path.startsWith('/')) {
    return props.item.path
  }
  return path.resolve(routePath, props.item.path)
}
</script>

<style scoped>
.menu-item {
  margin: 4px 8px;
  border-radius: 6px;
  min-height: 40px;
  padding: 0 12px;
}

.menu-sub {
  margin: 4px 8px;
}

:deep(.el-sub-menu__title) {
  border-radius: 6px;
  min-height: 40px;
  padding: 0 12px;
}

.menu-item.is-active {
  background-color: rgba(99, 102, 241, 0.1) !important;
  border: 1px solid rgb(191 219 254 / 0.5) !important;
  background-image: linear-gradient(to right, #eff6ff, #faf5ff);
}

.menu-icon {
  margin-right: 12px;
  width: 16px;
  height: 16px;
  color: inherit;
}

.menu-title {
  font-size: 14px;
  font-weight: 500;
}

.menu-count {
  display: inline-block;
  margin-left: 8px;
  background-color: #e5e7eb;
  color: #4b5563;
  font-size: 12px;
  padding: 0 8px;
  height: 20px;
  line-height: 20px;
  border-radius: 10px;
  font-weight: 500;
}

:deep(.el-sub-menu__title:hover) {
  background-color: #f3f4f6 !important;
}

:deep(.el-menu-item:hover) {
  background-color: #f3f4f6 !important;
}

:deep(.el-sub-menu .el-menu-item) {
  min-height: 40px;
  margin: 4px 8px;
  padding: 0 12px;
  border-radius: 6px;
}
</style> 