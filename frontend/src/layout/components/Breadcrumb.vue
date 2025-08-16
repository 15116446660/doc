<template>
  <el-breadcrumb separator="/">
    <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
    <el-breadcrumb-item 
      v-for="(item, index) in breadcrumbs" 
      :key="item.path"
      :to="index < breadcrumbs.length - 1 ? { path: item.path } : ''"
    >
      {{ item.meta?.title }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, type RouteLocationMatched } from 'vue-router'

const route = useRoute()

interface BreadcrumbItem extends RouteLocationMatched {
  path: string
}

const breadcrumbs = ref<BreadcrumbItem[]>([])

const getBreadcrumbs = () => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  breadcrumbs.value = matched.map(item => ({
    ...item,
    path: item.path
  }))
}

watch(
  () => route.path,
  () => getBreadcrumbs(),
  { immediate: true }
)
</script>

<style scoped>
.el-breadcrumb {
  line-height: 60px;
}
</style> 