<template>
  <template v-if="!item.meta || !item.meta.hidden">
    <!-- 如果没有子节点或者设置了 alwaysShow，显示为一级菜单 -->
    <el-menu-item v-if="showAsOneLevel" :index="resolvePath(item.children?.[0]?.path || item.path)">
      <el-icon v-if="item.meta && item.meta.icon">
        <component :is="item.meta.icon"></component>
      </el-icon>
      <template #title>{{ item.meta?.title }}</template>
    </el-menu-item>
    <!-- 否则显示为带子菜单的菜单项 -->
    <el-sub-menu v-else :index="resolvePath(item.path)">
      <template #title>
        <el-icon v-if="item.meta && item.meta.icon">
          <component :is="item.meta.icon"></component>
        </el-icon>
        <span>{{ item.meta?.title }}</span>
      </template>
      <sidebar-item
        v-for="child in item.children"
        :key="child.path"
        :item="child"
        :base-path="resolvePath(item.path)"
      ></sidebar-item>
    </el-sub-menu>
  </template>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { RouteRecordRaw } from 'vue-router'
import path from 'path-browserify'

const props = defineProps({
  item: {
    type: Object as () => RouteRecordRaw,
    required: true
  },
  basePath: {
    type: String,
    default: ''
  }
})

// 判断是否应该显示为一级菜单
const showAsOneLevel = computed(() => {
  // 如果没有子路由，或者 meta.alwaysShow 为 true，则显示为一级菜单

  // 如果路由没有子路由
  if (!props.item.children || props.item.children.length === 0) {
    return true;
  }

  // 如果设置了 alwaysShow，强制显示为一级菜单
  if (props.item.meta?.alwaysShow) {
    return true;
  }

  // 默认情况下，如果大于一个子路由，显示为 sub-menu
  return false;
});

// 判断是否有子路由需要显示
// const hasChildren = (item: RouteRecordRaw): boolean => {
//   return item.children && item.children.filter(child => !child.meta || !child.meta.hidden).length > 0;
// };

const resolvePath = (routePath: string): string => {
  if (routePath.startsWith('/')) {
    return routePath;
  }
  if (props.basePath.endsWith('/')) {
    return `${props.basePath}${routePath}`;
  }
  return path.resolve(props.basePath, routePath);
};

</script>

<style scoped>
/* 样式可以根据需要添加或修改 */
</style> 