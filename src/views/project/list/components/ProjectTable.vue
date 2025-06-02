<template>
  <div class="project-table-container">
    <el-table
      :data="projects"
      style="width: 100%"
      v-loading="loading"
      border
      stripe
      row-key="id"
      :max-height="'calc(100vh - 280px)'"
    >
      <el-table-column 
        prop="title" 
        label="项目名称" 
        min-width="200" 
        fixed="left"
      >
        <template #default="{ row }">
          <div class="project-name">
            <span class="name-text">{{ row.title }}</span>
            <el-tag v-if="row.documents > 0" size="small" type="info">{{ row.documents }}文档</el-tag>
          </div>
        </template>
      </el-table-column>
      
      <el-table-column prop="client" label="客户" min-width="150" />
      
      <el-table-column prop="dueDate" label="截止日期" min-width="120" />
      
      <el-table-column prop="progress" label="进度" min-width="180">
        <template #default="{ row }">
          <div class="progress-cell">
            <el-progress 
              :percentage="row.progress" 
              :color="row.progressColor"
              :stroke-width="6"
            />
          </div>
        </template>
      </el-table-column>
      
      <el-table-column prop="status" label="状态" min-width="120">
        <template #default="{ row }">
          <el-tag
            :type="getStatusType(row.status)"
            effect="light"
            size="small"
            class="status-tag"
          >
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column prop="risk" label="风险" min-width="100">
        <template #default="{ row }">
          <el-tag
            :type="getRiskType(row.risk)"
            effect="light"
            size="small"
            class="risk-tag"
          >
            {{ row.risk }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column prop="team" label="团队" min-width="120" />
      
      <el-table-column prop="categoryName" label="分类" min-width="180" />

      <el-table-column prop="budget" label="预算" min-width="120">
        <template #default="{ row }">
          {{ row.budget }}万
        </template>
      </el-table-column>
      
      <el-table-column label="操作" width="120" fixed="right">
        <template #default>
          <el-button link type="primary" size="small">查看</el-button>
          <el-button link type="primary" size="small">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import type { Project } from '@/api/project'

defineProps<{
  projects: Project[]
  loading: boolean
}>()

// 获取状态对应的类型
const getStatusType = (status: string) => {
  switch (status) {
    case '进行中':
      return 'primary'
    case '已完成':
      return 'success'
    case '待审核':
      return 'warning'
    case '已暂停':
      return 'info'
    case '高风险':
      return 'danger'
    default:
      return 'info'
  }
}

// 获取风险等级对应的类型
const getRiskType = (risk: string) => {
  switch (risk) {
    case '低':
      return 'success'
    case '中':
      return 'warning'
    case '高':
      return 'danger'
    case '严重':
      return 'danger'
    default:
      return 'info'
  }
}
</script>

<style scoped>
.project-table-container {
  width: 100%;
  height: 100%;
  overflow: hidden;
  position: relative;
}

.project-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name-text {
  font-weight: 500;
}

.progress-cell {
  padding: 8px 0;
}

.status-tag, .risk-tag {
  min-width: 60px;
  text-align: center;
}

:deep(.el-table) {
  height: 100%;
}

:deep(.el-table__header-wrapper) {
  position: sticky;
  top: 0;
  z-index: 2;
}

:deep(.el-table__fixed-header-wrapper) {
  z-index: 3;
}

:deep(.el-table__fixed-right) {
  height: 100% !important;
  bottom: 0;
  box-shadow: -6px 0 6px -4px rgba(0,0,0,.12);
}

:deep(.el-table__fixed) {
  height: 100% !important;
  bottom: 0;
  box-shadow: 6px 0 6px -4px rgba(0,0,0,.12);
}

:deep(.el-table__body-wrapper) {
  overflow-y: auto;
}

:deep(.el-table__fixed-body-wrapper) {
  overflow-y: hidden;
}
</style> 