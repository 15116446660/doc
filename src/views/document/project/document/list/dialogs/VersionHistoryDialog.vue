<template>
  <el-dialog
    :title="`${document?.name || '文档'} - 版本历史`"
    v-model="dialogVisible"
    width="800px"
    :close-on-click-modal="false"
  >
    <div class="version-history">
      <el-timeline>
        <el-timeline-item
          v-for="version in versionList"
          :key="version.id"
          :timestamp="version.createTime"
          :type="getVersionType(version)"
          :hollow="version.version !== document?.version"
        >
          <div class="version-item">
            <div class="version-header">
              <div class="version-info">
                <span class="version-number">v{{ version.version }}</span>
                <el-tag
                  v-if="version.version === document?.version"
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
                  v-if="version.version !== document?.version"
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
                <el-avatar :size="24" :src="version.creatorAvatar">
                  {{ version.creator?.charAt(0) }}
                </el-avatar>
                <span>{{ version.creator }}</span>
              </div>
              <div class="change-description">
                {{ version.description || '无更新说明' }}
              </div>
            </div>
          </div>
        </el-timeline-item>
      </el-timeline>
    </div>

    <!-- 版本预览抽屉 -->
    <document-content-viewer
      v-model="previewVisible"
      :document-data="currentVersion"
      :is-version="true"
    />
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import DocumentContentViewer from '@/components/DocumentContentViewer.vue'
import type { Document, DocumentVersion } from '@/types/document'
import { getDocumentVersions } from '@/api/document'

const props = defineProps<{
  modelValue: boolean
  document?: Document
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

// 对话框可见性
const dialogVisible = ref(props.modelValue)

// 监听对话框可见性
watch(() => props.modelValue, (val) => {
  dialogVisible.value = val
  if (val && props.document) {
    loadVersions()
  }
})

// 监听内部对话框可见性
watch(() => dialogVisible.value, (val) => {
  emit('update:modelValue', val)
})

// 版本列表
const versionList = ref<DocumentVersion[]>([])

// 加载版本列表
const loadVersions = async () => {
  try {
    if (!props.document) return
    const versions = await getDocumentVersions(props.document.id)
    versionList.value = versions
  } catch (error) {
    console.error('Failed to load versions:', error)
    ElMessage.error('加载版本历史失败')
  }
}

// 获取版本类型
const getVersionType = (version: DocumentVersion) => {
  if (version.version === props.document?.version) {
    return 'primary'
  }
  return ''
}

// 预览控制
const previewVisible = ref(false)
const currentVersion = ref<DocumentVersion>()

// 处理预览
const handlePreview = (version: DocumentVersion) => {
  currentVersion.value = version
  previewVisible.value = true
}

// 处理下载
const handleDownload = async (version: DocumentVersion) => {
  try {
    await ElMessageBox.confirm(
      `确定要下载 v${version.version} 版本的文档吗？`,
      '下载确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    if (version.url) {
      const link = document.createElement('a')
      link.href = version.url
      link.download = `${props.document?.name}_v${version.version}.${props.document?.format?.toLowerCase() || 'docx'}`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    }
  } catch {
    // 用户取消下载
  }
}

// 处理回滚
const handleRollback = async (version: DocumentVersion) => {
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
    
    // TODO: 调用回滚API
    console.log('Rollback to version:', version)
    
    ElMessage.success('回滚成功')
    loadVersions()
  } catch {
    // 用户取消回滚
  }
}
</script>

<style lang="scss" scoped>
.version-history {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 16px;
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

    .change-description {
      color: var(--el-text-color-secondary);
      font-size: 14px;
      line-height: 1.5;
    }
  }
}

:deep(.el-timeline-item__node) {
  &.el-timeline-item__node--primary {
    background-color: var(--el-color-primary);
  }
}

:deep(.el-timeline-item__wrapper) {
  padding-bottom: 20px;
}
</style> 