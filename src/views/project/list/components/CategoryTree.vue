<template>
  <div class="category-tree-container">
    <div class="category-header">
      <h3>项目分类</h3>
    </div>
    
    <div class="search-box">
      <el-input
        v-model="searchText"
        placeholder="搜索分类..."
        prefix-icon="Search"
        clearable
      />
    </div>
    
    <div class="tree-wrapper" v-loading="loading">
      <el-tree
        ref="treeRef"
        :data="categories"
        :props="defaultProps"
        :highlight-current="true"
        :expand-on-click-node="false"
        node-key="id"
        :filter-node-method="filterNode"
        @node-click="handleNodeClick"
      >
        <template #default="{ node, data }">
          <div class="custom-tree-node">
            <span class="node-icon">
              <el-icon v-if="data.icon">
                <component :is="data.icon" />
              </el-icon>
              <el-icon v-else>
                <Folder />
              </el-icon>
            </span>
            <span class="node-label">{{ node.label }}</span>
            <span class="node-count" v-if="data.count">{{ data.count }}</span>
          </div>
        </template>
      </el-tree>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { Folder } from '@element-plus/icons-vue'
import type { ProjectCategory } from '@/api/project'
import { getProjectCategories } from '@/api/project'
import type { ElTree } from 'element-plus'

const emit = defineEmits(['select'])

const treeRef = ref<InstanceType<typeof ElTree>>()
const categories = ref<ProjectCategory[]>([])
const loading = ref(false)
const searchText = ref('')

const defaultProps = {
  children: 'children',
  label: 'name'
}

// 过滤节点方法
const filterNode = (value: string, data: ProjectCategory) => {
  if (!value) return true
  return data.name.includes(value)
}

// 处理节点点击事件
const handleNodeClick = (data: ProjectCategory) => {
  emit('select', data)
}

// 获取分类数据
const fetchCategories = async () => {
  loading.value = true
  debugger
  try {
    const res = await getProjectCategories()
    if (res.code === 200) {
      categories.value = res.data
    } else {
      console.error('获取项目分类失败:', res)
      categories.value = []
    }
  } catch (error) {
    console.error('获取项目分类失败:', error)
    categories.value = []
  } finally {
    loading.value = false
  }
}

// 监听搜索文本变化
watch(searchText, (val) => {
  treeRef.value?.filter(val)
})

onMounted(() => {
  debugger
  fetchCategories()
})
</script>

<style scoped>
.category-tree-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.category-header {
  padding: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.category-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.search-box {
  padding: 12px 16px;
  border-bottom: 1px solid #e5e7eb;
}

.tree-wrapper {
  padding: 12px 8px;
  flex: 1;
  overflow-y: auto;
}

.custom-tree-node {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 4px 0;
}

.node-icon {
  margin-right: 8px;
  display: flex;
  align-items: center;
  color: #6366F1;
}

.node-label {
  flex: 1;
  font-size: 14px;
}

.node-count {
  background-color: #e5e7eb;
  color: #4b5563;
  font-size: 12px;
  padding: 0 8px;
  height: 20px;
  line-height: 20px;
  border-radius: 10px;
  font-weight: 500;
}

:deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: rgba(99, 102, 241, 0.1);
  color: #6366F1;
}

:deep(.el-tree-node__content:hover) {
  background-color: #f3f4f6;
}

:deep(.el-tree-node__content) {
  border-radius: 4px;
  margin: 2px 0;
  height: 36px;
}
</style> 