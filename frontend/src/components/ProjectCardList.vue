<template>
  <div class="project-card-list">
    <el-row :gutter="16">
      <el-col 
        v-for="project in projects" 
        :key="project.id" 
        :xs="24" 
        :sm="12" 
        :md="8" 
        :lg="6" 
        :xl="6"
        class="card-col"
      >
        <project-card 
          :project="project" 
          @edit="handleEdit" 
          @delete="handleDelete"
          @view="handleView"
        />
      </el-col>
    </el-row>
    
    <div v-if="!projects || projects.length === 0" class="empty-state">
      <el-empty description="暂无项目数据" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { defineProps, defineEmits } from 'vue'
import ProjectCard from './ProjectCard.vue'
import type { Project } from '@/api/project'

interface Props {
  projects: Project[]
}

defineProps<Props>()

const emit = defineEmits<{
  (e: 'edit', project: Project): void
  (e: 'delete', project: Project): void
  (e: 'view', project: Project): void
}>()

// 处理编辑
const handleEdit = (project: Project) => {
  emit('edit', project)
}

// 处理删除
const handleDelete = (project: Project) => {
  emit('delete', project)
}

// 处理查看
const handleView = (project: Project) => {
  emit('view', project)
}
</script>

<style lang="scss" scoped>
.project-card-list {
  width: 100%;
  
  .el-row {
    margin: -8px;
  }
  
  .card-col {
    padding: 8px;
    height: 520px; // 设置固定高度
    
    :deep(.el-col) {
      height: 100%;
    }
  }
  
  .empty-state {
    padding: 40px 0;
  }
}
</style>