<template>
  <div class="project-card" :class="{ 'is-completed': isCompleted }">
    <!-- 顶部状态条 -->
    <div class="project-card__status-bar" :style="{ backgroundColor: getStatusColor(project.status) }"></div>
    
    <!-- 卡片头部 -->
    <div class="project-card__header">
      <div class="project-card__title">
        <h3>{{ project.title || '未命名项目' }}</h3>
        <div class="project-card__meta">
          <span class="category">
            <el-icon><folder /></el-icon>
            {{ project.categoryName || '未分类' }}
          </span>
          <span class="status" :style="{ color: getStatusColor(project.status) }">
            <el-icon><circle-check /></el-icon>
            {{ project.status || '未设置' }}
          </span>
        </div>
      </div>
      <div class="project-card__actions">
        <div class="project-card__priority" v-if="project.priority">
          <el-tag :type="getPriorityType(project.priority)" size="small" effect="dark">
            {{ project.priority }}
          </el-tag>
        </div>
        <el-dropdown trigger="click" @command="handleCommand">
          <el-button class="more-button" link>
            <el-icon><more /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="view">查看详情</el-dropdown-item>
              <el-dropdown-item command="edit">编辑项目</el-dropdown-item>
              <el-dropdown-item command="delete" divided>删除项目</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    
    <!-- 卡片内容 -->
    <div class="project-card__body">
      <!-- 进度条 -->
      <div class="project-card__progress">
        <div class="progress-header">
          <span>项目进度</span>
          <span class="progress-value" :style="{ color: getProgressColor(project.progress) }">
            {{ project.progress || 0 }}%
          </span>
        </div>
        <el-progress 
          :percentage="project.progress || 0" 
          :stroke-width="8"
          :color="getProgressColor(project.progress)"
          :show-text="false"
          class="custom-progress"
        />
      </div>
      
      <!-- 项目关键信息 -->
      <div class="project-card__info-grid">
        <!-- 风险等级 -->
        <div class="info-item">
          <div class="info-label">风险等级</div>
          <div class="info-value">
            <el-tag :type="getRiskType(project.risk)" size="small">
              {{ project.risk || '未设置' }}
            </el-tag>
          </div>
        </div>
        
        <!-- 负责团队 -->
        <div class="info-item">
          <div class="info-label">负责团队</div>
          <div class="info-value team-info">
            <el-avatar :size="24" :src="project.leaderAvatar">
              {{ project?.team?.charAt(0) || 'U' }}
            </el-avatar>
            <span>{{ project.team || '未分配' }}</span>
          </div>
        </div>
        
        <!-- 开始时间 -->
        <div class="info-item">
          <div class="info-label">
            <el-icon><calendar /></el-icon>
            开始时间
          </div>
          <div class="info-value">{{ formatDate(project.date) }}</div>
        </div>
        
        <!-- 结束时间 -->
        <div class="info-item">
          <div class="info-label">
            <el-icon><timer /></el-icon>
            结束时间
          </div>
          <div class="info-value" :class="{ 'is-overdue': isOverdue(project.dueDate) }">
            {{ formatDate(project.dueDate) }}
          </div>
        </div>
      </div>
      
      <!-- 项目描述 -->
      <div v-if="project.description" class="project-card__desc">
        {{ truncateText(project.description, 100) }}
      </div>
      
      <!-- 标签 -->
      <div v-if="project.tags && project.tags.length > 0" class="project-card__tags">
        <el-tag 
          v-for="(tag, i) in project.tags.slice(0, 3)" 
          :key="i" 
          size="small"
          effect="plain"
          class="tag-item"
        >
          {{ tag }}
        </el-tag>
        <div v-if="project.tags.length > 3" class="tags-more">
          +{{ project.tags.length - 3 }}
        </div>
      </div>
    </div>
    
    <!-- 卡片底部 -->
    <div class="project-card__footer">
      <div class="footer-metrics">
        <div class="metric-item">
          <el-icon><document /></el-icon>
          <span>{{ project.documents || 0 }} 文档</span>
        </div>
        <div class="metric-item" :class="{ 'is-warning': isUrgent(project.dueDate) }">
          <el-icon><timer /></el-icon>
          <span>{{ getDaysLeft(project.dueDate) }}</span>
        </div>
      </div>
      <div class="footer-actions">
        <el-button link type="primary" size="small" @click="emit('view', project)">查看</el-button>
        <el-button link type="primary" size="small" @click="emit('edit', project)">编辑</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { 
  Document, Calendar, CircleCheck, Timer, Folder, More
} from '@element-plus/icons-vue'
import type { Project } from '@/api/project'

interface Props {
  project: Project
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'edit', project: Project): void
  (e: 'delete', project: Project): void
  (e: 'view', project: Project): void
}>()

// 是否已完成
const isCompleted = computed(() => props.project.status === '已完成')

// 获取状态颜色
function getStatusColor(status: string): string {
  const colorMap: Record<string, string> = {
    '进行中': '#3b82f6',
    '已完成': '#10b981',
    '待审核': '#f59e0b',
    '已暂停': '#6b7280',
    '高风险': '#ef4444'
  }
  return colorMap[status] || '#6b7280'
}

// 获取优先级类型
function getPriorityType(priority: string): string {
  const typeMap: Record<string, string> = {
    '高优先级': 'danger',
    '中优先级': 'warning',
    '低优先级': 'success'
  }
  return typeMap[priority] || 'info'
}

// 获取风险类型
function getRiskType(risk: string): string {
  const typeMap: Record<string, string> = {
    '低': 'info',
    '中': 'warning',
    '高': 'danger',
    '严重': 'danger'
  }
  return typeMap[risk] || 'info'
}

// 获取进度条颜色
function getProgressColor(progress: number): string {
  if (progress >= 80) return '#10b981' // 绿色
  if (progress >= 50) return '#3b82f6' // 蓝色
  if (progress >= 30) return '#f59e0b' // 橙色
  return '#ef4444' // 红色
}

// 格式化日期
function formatDate(dateStr: string): string {
  if (!dateStr) return '未设置'
  
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 检查是否逾期
function isOverdue(dueDate: string): boolean {
  if (!dueDate) return false
  
  const today = new Date()
  const due = new Date(dueDate)
  return due < today
}

// 检查是否紧急（3天内）
function isUrgent(dueDate: string): boolean {
  if (!dueDate) return false
  
  const today = new Date()
  const due = new Date(dueDate)
  const diffTime = due.getTime() - today.getTime()
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  
  return diffDays >= 0 && diffDays <= 3
}

// 获取剩余天数
function getDaysLeft(dueDate: string): string {
  if (!dueDate) return '无截止日期'
  
  const today = new Date()
  const due = new Date(dueDate)
  const diffTime = due.getTime() - today.getTime()
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  
  if (diffDays < 0) return `逾期 ${Math.abs(diffDays)} 天`
  if (diffDays === 0) return '今日截止'
  return `剩余 ${diffDays} 天`
}

// 截断文本
function truncateText(text: string, maxLength: number): string {
  if (!text) return ''
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

// 处理下拉菜单命令
const handleCommand = (command: string) => {
  switch (command) {
    case 'view':
      emit('view', props.project)
      break
    case 'edit':
      emit('edit', props.project)
      break
    case 'delete':
      emit('delete', props.project)
      break
  }
}
</script>

<style lang="scss" scoped>
.project-card {
  position: relative;
  border-radius: var(--el-border-radius-base);
  background-color: var(--el-bg-color);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  height: 100%;
  min-height: 480px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  
  &:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    transform: translateY(-4px);
  }
  
  &__status-bar {
    height: 4px;
    width: 100%;
    flex-shrink: 0;
  }
  
  &__header {
    padding: 16px;
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    position: relative;
  }
  
  &__title {
    flex: 1;
    min-width: 0;
    
    h3 {
      margin: 0 0 8px;
      font-size: 16px;
      font-weight: 600;
      color: var(--el-text-color-primary);
      line-height: 1.4;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
  
  &__meta {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 13px;
    
    .category, .status {
      display: flex;
      align-items: center;
      gap: 4px;
      color: var(--el-text-color-secondary);
    }
    
    .status {
      font-weight: 500;
    }
  }
  
  &__actions {
    display: flex;
    align-items: center;
    gap: 8px;
    position: absolute;
    top: 12px;
    right: 12px;
  }
  
  &__priority {
    position: absolute;
    top: 16px;
    right: 16px;
  }
  
  &__body {
    padding: 16px;
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 16px;
    min-height: 0;
    overflow-y: auto;
  }
  
  &__progress {
    flex-shrink: 0;
    .progress-header {
      display: flex;
      justify-content: space-between;
      margin-bottom: 8px;
      font-size: 14px;
      color: var(--el-text-color-secondary);
      
      .progress-value {
        font-weight: 600;
      }
    }
    
    .custom-progress {
      :deep(.el-progress-bar__outer) {
        border-radius: 4px;
        background-color: var(--el-fill-color-light);
      }
      
      :deep(.el-progress-bar__inner) {
        border-radius: 4px;
        transition: width 0.6s ease;
      }
    }
  }
  
  &__info-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
    padding: 16px;
    background-color: var(--el-fill-color-light);
    border-radius: 8px;
    flex-shrink: 0;
    
    .info-item {
      .info-label {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 13px;
        color: var(--el-text-color-secondary);
        margin-bottom: 8px;
      }
      
      .info-value {
        font-size: 14px;
        color: var(--el-text-color-primary);
        
        &.is-overdue {
          color: var(--el-color-danger);
        }
      }
      
      .team-info {
        display: flex;
        align-items: center;
        gap: 8px;
        
        span {
          font-weight: 500;
        }
      }
    }
  }
  
  &__desc {
    font-size: 14px;
    color: var(--el-text-color-regular);
    line-height: 1.5;
    flex: 1;
    min-height: 0;
    overflow-y: auto;
  }
  
  &__tags {
    flex-shrink: 0;
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    align-items: center;
    
    .tag-item {
      margin: 0;
    }
    
    .tags-more {
      font-size: 12px;
      color: var(--el-text-color-secondary);
      background-color: var(--el-fill-color);
      padding: 0 8px;
      height: 24px;
      line-height: 24px;
      border-radius: 4px;
    }
  }
  
  &__footer {
    flex-shrink: 0;
    padding: 12px 16px;
    background-color: var(--el-fill-color-light);
    border-top: 1px solid var(--el-border-color-lighter);
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .footer-metrics {
      display: flex;
      gap: 16px;
      
      .metric-item {
        display: flex;
        align-items: center;
        gap: 4px;
        color: var(--el-text-color-secondary);
        font-size: 13px;
        
        &.is-warning {
          color: var(--el-color-warning);
          font-weight: 500;
        }
      }
    }
    
    .footer-actions {
      display: flex;
      gap: 8px;
    }
  }
}

.more-button {
  padding: 4px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.more-button:hover {
  background-color: rgba(0, 0, 0, 0.04);
}
</style> 