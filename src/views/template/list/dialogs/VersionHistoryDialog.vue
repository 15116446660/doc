<template>
  <el-dialog
    :title="`${template?.name || '模板'} - 版本历史`"
    v-model="dialogVisible"
    width="800px"
    :close-on-click-modal="false"
  >
    <div class="version-history">
      <el-timeline v-if="versionList.length > 0">
        <el-timeline-item
          v-for="version in versionList"
          :key="version.id"
          :timestamp="version.updateTime"
          :type="getVersionType(version)"
          :hollow="String(version.version) !== String(template?.version)"
        >
          <div class="version-item">
            <div class="version-header">
              <div class="version-info">
                <span class="version-number">v{{ version.version }}</span>
                <el-tag :type="getStatusType(version.status)" size="small" effect="light">{{ version.status }}</el-tag>
                <el-tag
                  v-if="String(version.version) === String(template?.version)"
                  type="success"
                  size="small"
                >当前版本</el-tag>
              </div>
              <div class="version-actions">
                <el-button
                  type="primary"
                  link
                  size="small"
                  @click="handlePreview(version)"
                >
                  预览
                </el-button>
                <el-button
                  type="primary"
                  link
                  size="small"
                  @click="handleDownload(version)"
                >
                  下载
                </el-button>
                <el-button
                  v-if="String(version.version) !== String(template?.version)"
                  type="warning"
                  link
                  size="small"
                  @click="handleRollback(version)"
                >
                  回滚到此版本
                </el-button>
              </div>
            </div>
            
            <div class="version-content">
              <div class="creator-info">
                <el-avatar :size="24" :src="version.updaterAvatar">
                  {{ version.updater?.charAt(0) }}
                </el-avatar>
                <span>{{ version.updater }}</span>
              </div>
              <div class="version-details">
                <el-collapse v-if="version.content" class="details-collapse">
                  <el-collapse-item name="1">
                    <template #title>
                      <span class="details-title">制修订内容</span>
                    </template>
                    <div class="details-content">
                      {{ version.content }}
                    </div>
                  </el-collapse-item>
                </el-collapse>
                <div v-if="version.reviewReason" class="rejection-reason">
                  <el-icon><WarningFilled /></el-icon>
                  <span class="details-title">驳回原因: </span>
                  <span class="details-content">{{ version.reviewReason }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无版本历史" />
    </div>

    <!-- 版本预览抽屉 -->
    <template-content-viewer
      v-model="previewVisible"
      :template-data="currentVersion"
      :is-version="true"
    />
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { WarningFilled } from '@element-plus/icons-vue'
import TemplateContentViewer from '@/components/TemplateContentViewer.vue'
import type { Template } from '@/api/template'
import { getTemplateVersionHistory } from '@/api/template'
import type { VersionHistoryItem as TemplateVersion } from '@/api/template'

const props = defineProps<{
  modelValue: boolean
  template?: Template
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const dialogVisible = ref(props.modelValue)

watch(() => props.modelValue, (val) => {
  dialogVisible.value = val
  if (val && props.template) {
    loadVersions()
  }
})

watch(() => dialogVisible.value, (val) => {
  emit('update:modelValue', val)
})

const versionList = ref<TemplateVersion[]>([])

const loadVersions = async () => {
  try {
    if (!props.template?.id) return
    const response = await getTemplateVersionHistory(props.template.id, { pageNum: 1, pageSize: 100 }) // Fetch all for now
    versionList.value = response.list
  } catch (error) {
    console.error('Failed to load versions:', error)
    ElMessage.error('加载版本历史失败')
  }
}

const getVersionType = (version: TemplateVersion) => {
  return String(version.version) === String(props.template?.version) ? 'primary' : ''
}

const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    '审核中': 'warning',
    '通过': 'success',
    '驳回': 'danger',
    '草稿': 'info'
  }
  return typeMap[status] || 'info'
}

const previewVisible = ref(false)
const currentVersion = ref<TemplateVersion>()

const handlePreview = (version: TemplateVersion) => {
  currentVersion.value = version
  previewVisible.value = true
}

const handleDownload = async (version: TemplateVersion) => {
  try {
    await ElMessageBox.confirm(
      `确定要下载 v${version.version} 版本的模板吗？`,
      '下载确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    // TODO: Implement actual download logic, maybe from a URL in the version object
    console.log('Download template version:', version)
    ElMessage.info('下载功能待实现')
  } catch {
    // User cancelled download
  }
}

const handleRollback = async (version: TemplateVersion) => {
  try {
    await ElMessageBox.confirm(
      `确定要回滚到 v${version.version} 版本吗？此操作将创建一个新的版本。`,
      '回滚确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // TODO: Call rollback API
    console.log('Rollback to version:', version)
    
    ElMessage.success('回滚成功')
    loadVersions()
  } catch {
    // User cancelled rollback
  }
}
</script>

<style lang="scss" scoped>
.version-history {
  max-height: 60vh;
  overflow-y: auto;
  padding: 4px;

  .el-timeline {
    padding-left: 0;
  }
}

.version-item {
  background-color: var(--el-fill-color-light);
  border-radius: 4px;
  padding: 12px;
  margin-bottom: 4px;

  .version-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .version-info {
      display: flex;
      align-items: center;
      gap: 8px;

      .version-number {
        font-size: 14px;
        font-weight: 500;
        color: var(--el-text-color-primary);
      }
    }

    .version-actions {
      display: flex;
      gap: 8px;
    }
  }

  .version-content {
    .creator-info {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;
      color: var(--el-text-color-regular);
      font-size: 14px;
    }

    .version-details {
      margin-top: 12px;
      
      .details-collapse {
        border: none;
        background-color: transparent;

        :deep(.el-collapse-item__header) {
          height: auto;
          line-height: 1;
          border: none;
          background-color: transparent;
        }

        :deep(.el-collapse-item__wrap) {
          border: none;
          background-color: transparent;
        }
        
        :deep(.el-collapse-item__content) {
          padding: 8px 0 0;
        }
      }

      .details-title {
        font-weight: 500;
        color: var(--el-text-color-regular);
      }

      .details-content {
        font-size: 14px;
        color: var(--el-text-color-secondary);
        white-space: pre-wrap;
        word-break: break-all;
      }

      .rejection-reason {
        margin-top: 8px;
        padding: 8px 12px;
        border-radius: 4px;
        background-color: var(--el-color-danger-light-9);
        color: var(--el-color-danger);
        display: flex;
        align-items: center;
        gap: 8px;

        .details-title {
          color: var(--el-color-danger);
        }
        .details-content {
          color: var(--el-color-danger-dark-2);
        }
      }
    }
  }
}
</style> 