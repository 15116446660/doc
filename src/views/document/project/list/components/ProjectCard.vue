<template>
  <el-card 
    ref="cardRef"
    class="project-card"
    :body-style="{ padding: '0', height: '100%', display: 'flex', flexDirection: 'column' }" 
    @click="handleCardClick"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
  >
    <div class="card-inner">
      <!-- 更多操作按钮和下拉菜单 -->
      <el-dropdown 
        class="more-dropdown" 
        placement="bottom-end"
        @click.stop
      >
        <div class="more-actions" v-show="isHovered" @click.stop>
          <el-icon :size="16"><MoreFilled /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click.stop="handleViewDocuments">
              <el-icon><Document /></el-icon>
              <span>查看文档</span>
            </el-dropdown-item>
            <el-dropdown-item @click.stop="handleEdit">
              <el-icon><EditPen /></el-icon>
              <span>编辑项目</span>
            </el-dropdown-item>
            <el-dropdown-item divided @click.stop="handleDelete" class="danger-item">
              <el-icon><Delete /></el-icon>
              <span>删除项目</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <!-- 项目状态条 -->
      <div class="project-status-bar" :style="{ backgroundColor: getStatusColor(project.status) }"></div>

      <!-- 项目内容区域 -->
      <div class="card-content">
        <!-- 项目图号 -->
        <div class="project-code">
          <span class="label">图号:</span>
          <span class="value">{{ project.projectNum || '未设置' }}</span>
        </div>
        
        <!-- 项目名称 -->
        <h3 class="title">{{ project.name }}</h3>
        
        <!-- 状态和优先级 -->
        <div class="status-tags">
          <el-tag size="small" :type="getStatusType(project.status)">
            {{ getStatusLabel(project.status) }}
          </el-tag>
          <el-tag size="small" :type="getPriorityType(project.priority)" effect="dark">
            {{ project.priority }}
          </el-tag>
        </div>
        
        <!-- 项目信息 -->
        <div class="info-list">
          <div class="info-item">
            <span class="info-label">负责人:</span>
            <div class="user-info">
              <el-avatar :size="20" :src="project.directorHeadImg">
                {{ project.directorName?.charAt(0) }}
              </el-avatar>
              <span class="info-value">{{ project.directorName }}</span>
            </div>
          </div>
          <div class="info-item">
            <span class="info-label">开始时间:</span>
            <span class="info-value">{{ formatDate(project.startTime) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">结束时间:</span>
            <span class="info-value" :class="{ 'is-overdue': isOverdue(project.endTime) }">
              {{ formatDate(project.endTime) }}
            </span>
          </div>
          <div class="info-item">
            <span class="info-label">创建时间:</span>
            <span class="info-value">{{ formatDateTime(project.createTime) }}</span>
          </div>
        </div>

        <!-- 项目描述 -->
        <div v-if="project.description" class="project-desc">
          {{ project.description }}
        </div>
        <div v-else class="project-desc empty-desc">
          暂无项目描述
        </div>

        <!-- 底部统计信息 -->
        <div class="card-footer">
          <div class="footer-metrics">
            <div class="metric-item">
              <el-icon><Document /></el-icon>
              <span>{{ project.documentCount || 0 }} 文档</span>
            </div>
            <div class="metric-item">
              <el-icon><User /></el-icon>
              <span>{{ project.userCount || 0 }} 成员</span>
            </div>
            <div class="metric-item" :class="{ 'is-warning': isUrgent(project.endTime) }">
              <el-icon><Timer /></el-icon>
              <span>{{ getDaysLeft(project.endTime) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { 
  Document, User, Timer, MoreFilled, EditPen, Delete
} from '@element-plus/icons-vue'
import type { Project } from '@/types/document'

const props = defineProps<{
  project: Project
}>()

const router = useRouter()

// 鼠标悬浮状态
const isHovered = ref(false)
const cardRef = ref<HTMLElement | null>(null)

// 获取状态颜色
function getStatusColor(status: string): string {
  const colorMap: Record<string, string> = {
    'SURVEY': '#f59e0b',    // 调研中 - 橙色
    'ONGOING': '#3b82f6',   // 进行中 - 蓝色
    'COMPLETED': '#10b981', // 已完成 - 绿色
    'PAUSED': '#6b7280',    // 已暂停 - 灰色
    'CANCELLED': '#ef4444'  // 已取消 - 红色
  }
  return colorMap[status] || '#6b7280'
}

// 获取状态类型
function getStatusType(status: string): string {
  const typeMap: Record<string, string> = {
    'SURVEY': 'info',     // 调研中
    'ONGOING': 'success', // 进行中
    'COMPLETED': '',      // 已完成
    'PAUSED': 'warning',  // 已暂停
    'CANCELLED': 'danger' // 已取消
  }
  return typeMap[status] || 'info'
}

// 获取状态标签
function getStatusLabel(status: string): string {
  const labelMap: Record<string, string> = {
    'SURVEY': '调研中',
    'ONGOING': '进行中',
    'COMPLETED': '已完成',
    'PAUSED': '已暂停',
    'CANCELLED': '已取消'
  }
  return labelMap[status] || status
}

// 获取优先级类型
function getPriorityType(priority: string): string {
  const typeMap: Record<string, string> = {
    'P0': 'danger',  // 最高
    'P1': 'warning', // 高
    'P2': 'success', // 中
    'P3': 'info'     // 低
  }
  return typeMap[priority] || 'info'
}

// 格式化日期
function formatDate(dateStr?: string): string {
  if (!dateStr) return '未设置'
  
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 格式化日期时间
function formatDateTime(dateTimeStr?: string): string {
  if (!dateTimeStr) return '未设置'
  
  const date = new Date(dateTimeStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 检查是否逾期
function isOverdue(dueDate?: string): boolean {
  if (!dueDate) return false
  
  const today = new Date()
  const due = new Date(dueDate)
  return due < today
}

// 检查是否紧急（3天内）
function isUrgent(dueDate?: string): boolean {
  if (!dueDate) return false
  
  const today = new Date()
  const due = new Date(dueDate)
  const diffTime = due.getTime() - today.getTime()
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  
  return diffDays >= 0 && diffDays <= 3
}

// 获取剩余天数
function getDaysLeft(dueDate?: string): string {
  if (!dueDate) return '无截止日期'
  
  const today = new Date()
  const due = new Date(dueDate)
  const diffTime = due.getTime() - today.getTime()
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  
  if (diffDays < 0) return `逾期 ${Math.abs(diffDays)} 天`
  if (diffDays === 0) return '今日截止'
  return `剩余 ${diffDays} 天`
}

// 处理卡片点击
const handleCardClick = () => {
  handleViewDocuments()
}

// 处理查看文档
const handleViewDocuments = () => {
  router.push({
    name: 'DocumentList',
    params: { projectId: props.project.id },
    query: { projectName: props.project.name }
  })
}

// 处理编辑
const handleEdit = () => {
  // TODO: 处理编辑项目
}

// 处理删除
const handleDelete = () => {
  // TODO: 处理删除项目
}

// 鼠标事件处理
const handleMouseEnter = () => {
  isHovered.value = true
}

const handleMouseLeave = () => {
  isHovered.value = false
}
</script>

<style lang="scss" scoped>
.project-card {
  height: 100%;
  min-height: 320px;
  max-width: 280px;
  width: 100%;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: none;
  position: relative;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  }
}

.card-inner {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

.project-status-bar {
  height: 4px;
  width: 100%;
  flex-shrink: 0;
}

.more-actions {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 20;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.2s ease;
  
  &:hover {
    background-color: #fff;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
  }
  
  .el-icon {
    color: #606266;
  }
}

.more-dropdown {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 20;
}

:deep(.el-dropdown-menu) {
  padding: 6px 0;
  
  .el-dropdown-item {
    display: flex;
    align-items: center;
    padding: 8px 16px;
    
    .el-icon {
      margin-right: 8px;
      font-size: 16px;
    }
    
    &.danger-item {
      color: var(--el-color-danger);
      
      .el-icon {
        color: var(--el-color-danger);
      }
    }
  }
}

.card-content {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow-y: auto;
}

.project-code {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  
  .label {
    margin-right: 4px;
  }
  
  .value {
    color: var(--el-text-color-primary);
    font-weight: 500;
  }
}

.title {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  line-height: 1.4;
  color: var(--el-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.status-tags {
  display: flex;
  gap: 8px;
  margin-top: 4px;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item {
  font-size: 13px;
  display: flex;
  align-items: center;
  
  .info-label {
    color: var(--el-text-color-secondary);
    min-width: 65px;
  }
  
  .info-value {
    color: var(--el-text-color-primary);
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    
    &.is-overdue {
      color: var(--el-color-danger);
    }
  }
  
  .user-info {
    display: flex;
    align-items: center;
    gap: 8px;
    flex: 1;
    
    .el-avatar {
      flex-shrink: 0;
    }
  }
}

.project-desc {
  font-size: 13px;
  color: var(--el-text-color-regular);
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  
  &.empty-desc {
    color: var(--el-text-color-secondary);
    font-style: italic;
  }
}

.card-footer {
  margin-top: auto;
  padding-top: 12px;
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
}
</style>
