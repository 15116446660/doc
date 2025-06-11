<template>
  <el-card 
    ref="cardRef"
    class="template-card"
    :class="{ 'horizontal-layout': layout === 'horizontal' }"
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
        v-if="layout !== 'horizontal'"
      >
        <div class="more-actions" v-show="isHovered" @click.stop>
          <el-icon :size="16"><MoreFilled /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click.stop="handlePreview">
              <el-icon><View /></el-icon>
              <span>预览</span>
            </el-dropdown-item>
            <el-dropdown-item @click.stop="handleDownload">
              <el-icon><Download /></el-icon>
              <span>下载</span>
            </el-dropdown-item>
            <el-dropdown-item @click.stop="handleEdit">
              <el-icon><EditPen /></el-icon>
              <span>编辑</span>
            </el-dropdown-item>
            <el-dropdown-item @click.stop="handleVersionHistory">
              <el-icon><Timer /></el-icon>
              <span>版本历史</span>
            </el-dropdown-item>
            <el-dropdown-item divided @click.stop="handleDelete" class="danger-item">
              <el-icon><Delete /></el-icon>
              <span>删除</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <!-- 文档封面区域 -->
      <div class="document-cover">
        <!-- 文档图标 -->
        <div class="document-icon-container">
          <div class="document-icon">
            <el-icon :size="24"><Document /></el-icon>
          </div>
        </div>
        
        <!-- 中心图标 -->
        <div class="center-icon">
          <div class="starburst">
            <div v-for="n in 8" :key="n" class="ray"></div>
          </div>
          <el-icon :size="16"><Picture /></el-icon>
        </div>
        
        <!-- 下载量和收藏 -->
        <div class="header-actions" v-if="layout !== 'horizontal'">
          <div class="download-count">
            <el-icon :size="14"><Download /></el-icon>
            {{ template.downloads || 156 }}
          </div>
          <div class="favorite">
            <el-icon :size="20" :color="template.favorite ? '#F7BA2A' : '#909399'"><Star /></el-icon>
          </div>
        </div>
      </div>

      <!-- 文档信息区域 -->
      <div class="card-content">
        <div class="content-header" v-if="layout === 'horizontal'">
          <div class="header-left">
            <!-- 标题 -->
            <h3 class="title">{{ template.name || template.title || '无' }}</h3>
            <!-- 模板编号 -->
            <div class="template-code">
              <span class="label">编号:</span>
              <span class="value">{{ template.templateCode || '--' }}</span>
            </div>
          </div>
          
          <div class="header-right">
            <el-dropdown 
              trigger="click" 
              placement="bottom-end"
              @click.stop
            >
              <div class="more-actions" @click.stop>
                <el-icon :size="14"><MoreFilled /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click.stop="handlePreview">
                    <el-icon><View /></el-icon>
                    <span>预览</span>
                  </el-dropdown-item>
                  <el-dropdown-item @click.stop="handleDownload">
                    <el-icon><Download /></el-icon>
                    <span>下载</span>
                  </el-dropdown-item>
                  <el-dropdown-item @click.stop="handleEdit">
                    <el-icon><EditPen /></el-icon>
                    <span>编辑</span>
                  </el-dropdown-item>
                  <el-dropdown-item @click.stop="handleVersionHistory">
                    <el-icon><Timer /></el-icon>
                    <span>版本历史</span>
                  </el-dropdown-item>
                  <el-dropdown-item divided @click.stop="handleDelete" class="danger-item">
                    <el-icon><Delete /></el-icon>
                    <span>删除</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
        
        <template v-else>
          <!-- 模板编号 -->
          <div class="template-code">
            <span class="label">编号:</span>
            <span class="value">{{ template.templateCode || 'TMP-001' }}</span>
          </div>
          
          <!-- 标题 -->
          <h3 class="title">{{ template.name || template.title || '产品需求文档模版' }}</h3>
        </template>
        
        <!-- 状态标签 -->
        <div class="status-tags">
          <el-tag size="small" :type="template.enableStatus ? 'success' : 'info'">
            {{ template.enableStatus ? '已启用' : '未启用' }}
          </el-tag>
          <el-tag size="small" :type="getStatusType(template.status)">
            {{ template.status || '草稿' }}
          </el-tag>
          <!-- <el-tag v-if="template.reviewStatus" size="small" type="warning">
            {{ template.reviewName || template.reviewStatus }}
          </el-tag> -->
        </div>
        
        <!-- 信息列表 -->
        <div class="info-list">
          <div class="info-item">
            <span class="info-label">适用范围:</span>
            <span class="info-value">{{ template.scope || template.applicableScope || '全部' }}</span>
          </div>
          <div class="info-item" v-if="layout === 'vertical' || !layout">
            <span class="info-label">文件类型:</span>
            <span class="info-value">{{ template.typeName || template.type || 'XP1UDYNDS' }}</span>
          </div>
          <div class="info-item" v-if="layout === 'vertical' || !layout">
            <span class="info-label">文件格式:</span>
            <span class="info-value">{{ template.format || 'XP' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">标准类型:</span>
            <span class="info-value">{{ template.standardType || '行业标准' }}</span>
          </div>
        </div>

        <!-- 作者和版本 -->
        <div class="author-info">
          <div class="author">
            <el-avatar :size="layout === 'horizontal' ? 20 : 24" class="avatar">
              {{ template?.creator?.charAt(0) || template?.owner?.charAt(0) || '张' }}
            </el-avatar>
            <span class="author-name">{{ template.creator || template.owner || '张小明' }}</span>
          </div>
          <div class="version">v{{ template.version || '2.1' }}</div>
        </div>

        <!-- 更新时间 -->
        <div class="update-time" v-if="layout !== 'horizontal'">
          <el-icon :size="14"><Clock /></el-icon>
          {{ formatDate(template.updateTime) || '2024-01-20' }}
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { 
  Document, Star, Download, Clock, Picture, 
  MoreFilled, View, EditPen, Delete, Timer
} from '@element-plus/icons-vue'
import type { Template } from '@/api/template'

const props = defineProps<{
  template: Template
  layout?: 'vertical' | 'horizontal'
}>()

const emit = defineEmits<{
  (e: 'edit', template: Template): void
  (e: 'copy', template: Template): void
  (e: 'delete', template: Template): void
  (e: 'view', template: Template): void
  (e: 'preview', template: Template): void
  (e: 'download', template: Template): void
  (e: 'version-history', template: Template): void
}>()

// 鼠标悬浮状态
const isHovered = ref(false)
const cardRef = ref<HTMLElement | null>(null)

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).replace(/\//g, '-')
}

// 获取状态类型
const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    '已发布': 'success',
    '草稿': 'info',
    '审核中': 'warning',
    '已废弃': 'danger'
  }
  return typeMap[status] || 'info'
}

// 处理卡片点击
const handleCardClick = () => {
  emit('view', props.template)
}

// 处理预览
const handlePreview = () => {
  emit('preview', props.template)
}

// 处理下载
const handleDownload = () => {
  emit('download', props.template)
}

// 处理编辑
const handleEdit = () => {
  emit('edit', props.template)
}

// 处理版本历史
const handleVersionHistory = () => {
  emit('version-history', props.template)
}

// 处理删除
const handleDelete = () => {
  emit('delete', props.template)
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
.template-card {
  height: 100%;
  min-height: v-bind('layout === "horizontal" ? "210px" : "420px"');
  max-width: v-bind('layout === "horizontal" ? "360px" : "280px"');
  width: 100%;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: none;
  position: relative;
  // box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
  box-shadow: var(--el-box-shadow);

  :deep(.el-card__body) {
    padding: 0;
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--el-box-shadow-dark);

    .document-cover {
      .document-icon {
        transform: scale(1.05);
        box-shadow: 0 4px 8px rgba(64, 158, 255, 0.2);
      }
      
      .starburst {
        transform: rotate(45deg);
      }
    }
  }

  &.horizontal-layout {
    :deep(.el-card__body) {
      border-radius: 8px;
      overflow: hidden;
    }

    .card-inner {
      display: flex;
      flex-direction: row;
      height: 100%;
    }

    .document-cover {
      width: 160px;
      height: 100%;
      flex-shrink: 0;
      border-right: 1px solid var(--el-border-color-lighter);
    }

    .card-content {
      flex: 1;
      padding: 12px;
      gap: 8px;
      
      .title {
        font-size: 14px;
        margin-top: 0;
        -webkit-line-clamp: 1;
      }
      
      .info-item {
        font-size: 12px;
        
        .info-label {
          min-width: 60px;
        }
      }
      
      .status-tags {
        margin-top: 0;
      }
    }
  }
}

.card-inner {
  height: 100%;
  display: flex;
  flex-direction: row;
  overflow: hidden;
  position: relative;
}

.document-cover {
  height: 140px;
  background: #F5F7FA;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  position: relative;
  overflow: hidden;
  flex-shrink: 0;
}

.document-icon-container {
  position: absolute;
  top: 16px;
  left: 16px;
  z-index: 2;
}

.document-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #EBF3FF;
  border-radius: 12px;
  transition: all 0.3s ease;

  :deep(.el-icon) {
    color: #409EFF;
    transition: all 0.3s ease;
  }
}

.center-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: white;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.05);
  z-index: 1;
  
  .el-icon {
    color: #C0C4CC;
  }
  
  .starburst {
    position: absolute;
    width: 100%;
    height: 100%;
    transition: transform 0.5s ease;
    
    .ray {
      position: absolute;
      top: 50%;
      left: 50%;
      width: 60px;
      height: 1px;
      background: linear-gradient(to right, rgba(0,0,0,0.03), transparent);
      transform-origin: 0 0;
      
      @for $i from 0 through 7 {
        &:nth-child(#{$i + 1}) {
          transform: rotate(#{$i * 45}deg) translateX(-50%);
        }
      }
    }
  }
}

.header-actions {
  position: absolute;
  top: 16px;
  right: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  z-index: 2;
}

.download-count {
  height: 28px;
  display: flex;
  align-items: center;
  gap: 4px;
  background-color: rgba(0, 0, 0, 0.2);
  color: white;
  padding: 6px 12px;
  border-radius: 14px;
  font-size: 13px;
  transition: all 0.3s ease;

  &:hover {
    background-color: rgba(0, 0, 0, 0.25);
  }

  .el-icon {
    transition: transform 0.3s ease;
  }

  &:hover .el-icon {
    transform: translateY(-1px);
  }
}

.more-actions {
  position: absolute;
  top: 16px;
  right: 16px;
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
  top: 16px;
  right: 16px;
  z-index: 20;
  
  .more-dropdown-link {
    display: block;
    width: 32px;
    height: 32px;
    cursor: pointer;
  }
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
  gap: 10px;
  overflow-y: auto;
}

.template-code {
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
  gap: 6px;
  margin-top: 2px;
}

.info-item {
  font-size: 12px;
  display: flex;
  align-items: flex-start;
  
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
  }
}

.author-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
  padding-top: 6px;
}

.author {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .avatar {
    background-color: var(--el-color-primary-light-5);
    color: var(--el-color-primary);
  }
  
  .author-name {
    font-size: 14px;
    color: var(--el-text-color-regular);
  }
}

.version {
  font-size: 13px;
  color: var(--el-color-info);
  background-color: var(--el-fill-color-light);
  padding: 2px 8px;
  border-radius: 10px;
}

.update-time {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  
  .header-left {
    flex: 1;
    min-width: 0;
  }
  
  .header-right {
    flex-shrink: 0;
    margin-left: 8px;
    
    .more-actions {
      width: 28px;
      height: 28px;
      position: static;
      box-shadow: none;
      background-color: rgba(0, 0, 0, 0.1);
      
      &:hover {
        background-color: rgba(0, 0, 0, 0.15);
        box-shadow: none;
      }
      
      .el-icon {
        font-size: 14px;
        color: var(--el-text-color-regular);
      }
    }
  }
}
</style> 