<template>
  <el-drawer
    :title="title"
    v-model="drawerVisible"
    size="80%"
    :close-on-click-modal="false"
    :close-on-press-escape="true"
    direction="rtl"
  >
    <div class="document-viewer">
      <!-- 文档信息 -->
      <div class="document-info">
        <div class="info-header">
          <div class="info-title">
            <span class="label">文档名称：</span>
            <span class="value">{{ documentData?.name }}</span>
          </div>
          <div class="info-actions">
            <el-button type="primary" @click="handleDownload">
              <el-icon><download /></el-icon>下载
            </el-button>
          </div>
        </div>
        <div class="info-meta">
          <div class="meta-item">
            <span class="label">文档编号：</span>
            <span class="value">{{ documentData?.documentCode }}</span>
          </div>
          <div class="meta-item">
            <span class="label">文档类型：</span>
            <span class="value">{{ documentData?.type }}</span>
          </div>
          <div class="meta-item">
            <span class="label">版本：</span>
            <span class="value">v{{ documentData?.version }}</span>
          </div>
          <div class="meta-item">
            <span class="label">创建人：</span>
            <span class="value">{{ documentData?.creator }}</span>
          </div>
          <div class="meta-item">
            <span class="label">创建时间：</span>
            <span class="value">{{ documentData?.createTime }}</span>
          </div>
          <div class="meta-item">
            <span class="label">更新时间：</span>
            <span class="value">{{ documentData?.updateTime }}</span>
          </div>
        </div>
      </div>

      <!-- 文档预览 -->
      <div class="document-preview">
        <div v-if="loading" class="preview-loading">
          <el-skeleton :rows="10" animated />
        </div>
        <div v-else-if="previewError" class="preview-error">
          <el-empty
            :description="previewError"
            :image-size="120"
          >
            <template #image>
              <el-icon :size="48"><warning /></el-icon>
            </template>
          </el-empty>
        </div>
        <div v-else class="preview-content">
          <!-- 根据文档格式显示不同的预览组件 -->
          <template v-if="isImage">
            <el-image
              :src="documentData?.url"
              :preview-src-list="[documentData?.url as string]"
              fit="contain"
            />
          </template>
          <template v-else>
            <!-- 这里可以集成第三方文档预览组件 -->
            <iframe
              v-if="documentData?.url"
              :src="getPreviewUrl(documentData.url)"
              frameborder="0"
              class="preview-iframe"
            />
          </template>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, Warning } from '@element-plus/icons-vue'
import type { Document, DocumentVersion } from '@/types/document'

const props = defineProps<{
  modelValue: boolean
  documentData?: Document | DocumentVersion
  isVersion?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

// 抽屉可见性
const drawerVisible = ref(props.modelValue)

// 监听抽屉可见性
watch(() => props.modelValue, (val) => {
  drawerVisible.value = val
})

// 监听内部抽屉可见性
watch(() => drawerVisible.value, (val) => {
  emit('update:modelValue', val)
})

// 标题
const title = computed(() => {
  if (props.isVersion) {
    return `文档预览 - v${props.documentData?.version}`
  }
  return '文档预览'
})

// 加载状态
const loading = ref(true)
const previewError = ref<string>('')

// 是否为图片
const isImage = computed(() => {
  return props.documentData?.format === 'image'
})

// 获取预览URL
const getPreviewUrl = (url: string) => {
  // TODO: 根据实际情况处理预览URL
  // 可以使用在线文档预览服务，如 Office Online、永中等
  return url
}

// 处理下载
const handleDownload = () => {
  if (!props.documentData?.url) {
    ElMessage.warning('文档URL不存在')
    return
  }

  const link = document.createElement('a')
  link.href = props.documentData.url
  link.download = `${props.documentData.name || '文档'}.${props.documentData.format?.toLowerCase() || 'docx'}`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 监听文档数据变化
watch(() => props.documentData, () => {
  if (props.documentData) {
    loading.value = true
    previewError.value = ''

    // 模拟加载过程
    setTimeout(() => {
      if (!props.documentData?.url) {
        previewError.value = '文档内容不存在或无法访问'
      }
      loading.value = false
    }, 1000)
  }
}, { immediate: true })
</script>

<style lang="scss" scoped>
.document-viewer {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: var(--el-bg-color);
}

.document-info {
  padding: 16px;
  border-bottom: 1px solid var(--el-border-color-light);

  .info-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    .info-title {
      font-size: 16px;
      font-weight: 500;

      .label {
        color: var(--el-text-color-regular);
      }

      .value {
        color: var(--el-text-color-primary);
      }
    }
  }

  .info-meta {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 12px;

    .meta-item {
      .label {
        color: var(--el-text-color-regular);
        margin-right: 8px;
      }

      .value {
        color: var(--el-text-color-primary);
      }
    }
  }
}

.document-preview {
  flex: 1;
  min-height: 0;
  padding: 16px;
  background-color: var(--el-fill-color-lighter);

  .preview-loading {
    padding: 20px;
    background-color: var(--el-bg-color);
    border-radius: 4px;
  }

  .preview-error {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;

    :deep(.el-empty__icon) {
      color: var(--el-color-danger);
    }
  }

  .preview-content {
    height: 100%;

    .el-image {
      width: 100%;
      height: 100%;
    }

    .preview-iframe {
      width: 100%;
      height: 100%;
      background-color: white;
      border-radius: 4px;
    }
  }
}
</style> 