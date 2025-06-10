<template>
  <div class="project-card" :class="layout">
    <div class="project-card-header">
      <div class="project-info">
        <div class="project-title" @click="$emit('view', project)">
          {{ project.name || '未命名项目' }}
        </div>
        <div class="project-code">{{ project.projectCode }}</div>
      </div>
      <div class="project-status">
        <el-tag :type="getStatusType(project.status)">{{ project.status }}</el-tag>
      </div>
    </div>
    
    <div class="project-card-content">
      <div class="info-item">
        <span class="label">项目类型：</span>
        <span class="value">{{ project.type || '-' }}</span>
      </div>
      <div class="info-item">
        <span class="label">所属部门：</span>
        <span class="value">{{ project.department || '-' }}</span>
      </div>
      <div class="info-item">
        <span class="label">开始时间：</span>
        <span class="value">{{ project.startDate || '-' }}</span>
      </div>
      <div class="info-item">
        <span class="label">结束时间：</span>
        <span class="value">{{ project.endDate || '-' }}</span>
      </div>
    </div>

    <div class="project-card-footer">
      <div class="owner-info">
        <el-avatar :size="24" :src="project.ownerAvatar">
          {{ (project?.owner || 'U')?.charAt(0) }}
        </el-avatar>
        <span class="owner-name">{{ project.owner || '未分配' }}</span>
      </div>
      <div class="actions">
        <el-button type="primary" text @click="$emit('view', project)">查看文档</el-button>
        <el-button type="primary" text @click="$emit('edit', project)">编辑</el-button>
        <el-button type="danger" text @click="$emit('delete', project)">删除</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Project } from '@/types/document'

defineProps<{
  project: Project
  layout: 'vertical' | 'horizontal'
}>()

defineEmits<{
  (e: 'view', project: Project): void
  (e: 'edit', project: Project): void
  (e: 'delete', project: Project): void
}>()

const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    '进行中': 'success',
    '未开始': 'info',
    '已完成': 'primary',
    '已终止': 'danger'
  }
  return typeMap[status] || 'info'
}
</script>

<style lang="scss" scoped>
.project-card {
  background-color: var(--el-bg-color);
  border-radius: 8px;
  box-shadow: var(--el-box-shadow-light);
  padding: 16px;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--el-box-shadow);
  }

  &.vertical {
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  &.horizontal {
    display: flex;
    flex-direction: column;
    height: 100%;
  }
}

.project-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;

  .project-info {
    flex: 1;
    min-width: 0;
    margin-right: 16px;

    .project-title {
      font-size: 16px;
      font-weight: 600;
      color: var(--el-color-primary);
      margin-bottom: 4px;
      cursor: pointer;
      
      &:hover {
        text-decoration: underline;
      }
    }

    .project-code {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }
}

.project-card-content {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 16px;

  .info-item {
    display: flex;
    align-items: center;
    
    .label {
      color: var(--el-text-color-secondary);
      margin-right: 8px;
      white-space: nowrap;
    }
    
    .value {
      color: var(--el-text-color-primary);
      flex: 1;
      min-width: 0;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

.project-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px solid var(--el-border-color-lighter);

  .owner-info {
    display: flex;
    align-items: center;
    gap: 8px;

    .owner-name {
      color: var(--el-text-color-regular);
      font-size: 14px;
    }
  }

  .actions {
    display: flex;
    gap: 8px;
  }
}

.el-button+.el-button {
  margin-left: 0;
}
</style> 