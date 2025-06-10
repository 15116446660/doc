<template>
  <el-card class="document-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <div class="card-title" @click="handleViewDocument">
          <el-icon class="title-icon"><Document /></el-icon>
          <span class="title-text">{{ document.name }}</span>
        </div>
        <el-dropdown trigger="click" @command="handleCommand">
          <el-icon class="more-icon"><MoreFilled /></el-icon>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="edit"><el-icon><EditPen /></el-icon>编辑</el-dropdown-item>
              <el-dropdown-item command="sync"><el-icon><Refresh /></el-icon>同步</el-dropdown-item>
              <el-dropdown-item command="download"><el-icon><Download /></el-icon>下载</el-dropdown-item>
              <el-dropdown-item command="delete" divided class="danger"><el-icon><Delete /></el-icon>删除</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </template>

    <div class="card-body">
      <div class="info-grid">
        <div class="info-item">
          <el-icon><User /></el-icon>
          <span>{{ document.creator || '未指定' }}</span>
        </div>
        <div class="info-item">
          <el-icon><PriceTag /></el-icon>
          <span>{{ document.type || '无类型' }}</span>
        </div>
        <div class="info-item full-width">
          <el-icon><Files /></el-icon>
          <span :title="document.templateName">{{ document.templateName || '无模板' }}</span>
        </div>
      </div>
      
      <div class="status-tags">
        <el-tag :type="getStatusType(document.status || '')" size="small" effect="light" round>{{ document.status || '未知' }}</el-tag>
        <el-tag :type="getSyncStatusType(document.syncStatus || '')" size="small" effect="light" round>
          <el-icon v-if="document.syncStatus === 'pending'" class="is-loading"><Refresh /></el-icon>
          {{ getSyncStatusText(document.syncStatus || '') }}
        </el-tag>
      </div>
    </div>

    <div class="card-footer">
      <div class="update-time">
        <el-icon><Clock /></el-icon>
        <span>{{ document.updateTime }}</span>
      </div>
      <div class="version">
        V{{ document.version }}
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { 
  Document, 
  MoreFilled, 
  Clock,
  User,
  PriceTag,
  Files,
  EditPen,
  Refresh,
  Download,
  Delete
} from '@element-plus/icons-vue'
import type { Document as DocumentModel } from '@/types/document'

const props = defineProps<{
  document: DocumentModel
}>()

const emit = defineEmits<{
  (e: 'view', doc: DocumentModel): void
  (e: 'edit', doc: DocumentModel): void
  (e: 'sync', doc: DocumentModel): void
  (e: 'download', doc: DocumentModel): void
  (e: 'delete', doc: DocumentModel): void
}>()

const handleViewDocument = () => emit('view', props.document)
const handleEdit = () => emit('edit', props.document)
const handleSync = () => emit('sync', props.document)
const handleDownload = () => emit('download', props.document)
const handleDelete = () => emit('delete', props.document)

const handleCommand = (command: string) => {
  const actions: Record<string, () => void> = {
    edit: handleEdit,
    sync: handleSync,
    download: handleDownload,
    delete: handleDelete,
  }
  actions[command]?.()
}

const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    '已发布': 'success',
    '草稿': 'info',
    '审核中': 'warning',
    '已废弃': 'danger'
  }
  return typeMap[status] || 'info'
}

const getSyncStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    synced: 'success',
    pending: 'warning',
    failed: 'danger',
    none: 'info'
  }
  return typeMap[status] || 'info'
}

const getSyncStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    synced: '已同步',
    pending: '待同步',
    failed: '同步失败',
    none: '未同步'
  }
  return textMap[status] || '未知'
}
</script>

<style lang="scss" scoped>
.document-card {
  --card-padding: 16px;
  background-color: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
  transition: all 0.2s ease-in-out;
  display: flex;
  flex-direction: column;
  height: 220px;
  box-shadow: var(--el-box-shadow-light);

  &:hover {
    transform: translateY(-2px);
    scale: 1.01;
    box-shadow: var(--el-box-shadow);
  }

  :deep(.el-card__header) {
    padding: var(--card-padding) var(--card-padding) 0;
    border-bottom: none;
  }

  :deep(.el-card__body) {
    padding: var(--card-padding);
    flex-grow: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;

  .card-title {
    display: flex;
    align-items: center;
    gap: 10px;
    font-weight: 600;
    font-size: 16px;
    color: var(--el-text-color-primary);
    cursor: pointer;
    margin-bottom: 12px;

    .title-icon {
      color: var(--el-color-primary);
      font-size: 20px;
      flex-shrink: 0;
    }

    .title-text {
      line-height: 1.3;
    }

    &:hover {
      .title-text {
        color: var(--el-color-primary);
      }
    }
  }

  .more-icon {
    cursor: pointer;
    color: var(--el-text-color-secondary);
    font-size: 20px;
    &:hover {
      color: var(--el-color-primary);
    }
  }
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 10px;
  font-size: 13px;
  color: var(--el-text-color-regular);

  .info-item {
    display: flex;
    align-items: center;
    gap: 8px;
    overflow: hidden;

    .el-icon {
      color: var(--el-text-color-secondary);
      font-size: 16px;
    }

    span {
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  .full-width {
    grid-column: 1 / -1;
  }
}

.status-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  
  .el-tag {
    .el-icon {
      margin-right: 4px;
    }
    .is-loading {
      animation: rotating 2s linear infinite;
    }
  }
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  margin-top: 12px;
  border-top: 1px solid var(--el-border-color-lighter);
  font-size: 12px;
  color: var(--el-text-color-secondary);

  .update-time, .version {
    display: flex;
    align-items: center;
    gap: 6px;
  }

  .version {
    font-weight: 600;
    padding: 2px 6px;
    background-color: var(--el-fill-color-light);
    border-radius: 4px;
    color: var(--el-text-color-regular)
  }
}

.el-dropdown-menu__item {
  display: flex;
  align-items: center;
  .el-icon {
    margin-right: 8px;
  }

  &.danger {
    color: var(--el-color-danger);
    &:hover {
      background-color: var(--el-color-danger-light-9);
      color: var(--el-color-danger);
    }
  }
}
</style> 