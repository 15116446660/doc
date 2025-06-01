<template>
  <div v-if="!item.meta?.hidden">
    <!-- 没有子菜单的情况 -->
    <el-menu-item v-if="!hasChildren(item)" :index="resolvePath(basePath)">
      <el-icon v-if="item.meta?.icon">
        <component :is="item.meta.icon" />
      </el-icon>
      <template #title>
        <span>{{ item.meta?.title }}</span>
      </template>
    </el-menu-item>

    <!-- 有子菜单的情况 -->
    <el-sub-menu v-else :index="resolvePath(basePath)">
      <template #title>
        <el-icon v-if="item.meta?.icon">
          <component :is="item.meta.icon" />
        </el-icon>
        <span>{{ item.meta?.title }}</span>
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
import { computed } from 'vue'
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
.el-menu-item, .el-sub-menu {
  text-align: left;
}

.el-menu-item .el-icon, .el-sub-menu .el-icon {
  margin-right: 12px;
  width: 16px;
  height: 16px;
}
</style> 