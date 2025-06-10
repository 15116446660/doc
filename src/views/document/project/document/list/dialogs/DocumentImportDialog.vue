<template>
  <el-dialog
    title="导入文档"
    v-model="dialogVisible"
    width="500px"
    :close-on-click-modal="false"
  >
    <div class="import-container">
      <el-upload
        class="document-upload"
        drag
        multiple
        :action="uploadAction"
        :headers="uploadHeaders"
        :data="uploadData"
        :before-upload="handleBeforeUpload"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :on-exceed="handleExceed"
        :limit="10"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持 docx, pdf, xlsx 等格式，单个文件不超过10MB，最多可同时上传10个文件
          </div>
        </template>
      </el-upload>

      <div v-if="uploadList.length > 0" class="upload-list">
        <div class="list-header">
          <div class="title">上传列表</div>
          <el-button type="primary" link @click="handleClearList">
            清空列表
          </el-button>
        </div>
        <el-scrollbar height="200px">
          <div
            v-for="item in uploadList"
            :key="item.uid"
            class="upload-item"
          >
            <div class="item-info">
              <el-icon><document /></el-icon>
              <span class="filename">{{ item.name }}</span>
            </div>
            <div class="item-status">
              <el-tag
                :type="item.status === 'success' ? 'success' : item.status === 'error' ? 'danger' : 'info'"
                size="small"
              >
                {{ getStatusText(item.status) }}
              </el-tag>
            </div>
          </div>
        </el-scrollbar>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button
          type="primary"
          @click="handleImport"
          :loading="importing"
          :disabled="!hasSuccessfulUploads"
        >
          导入文档
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { UploadRawFile } from 'element-plus'
import { UploadFilled, Document } from '@element-plus/icons-vue'

const props = defineProps<{
  modelValue: boolean
  projectId: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}>()

// 对话框可见性
const dialogVisible = ref(props.modelValue)

// 监听对话框可见性变化
watch(() => props.modelValue, (val) => {
  dialogVisible.value = val
})

// 监听内部对话框可见性变化
watch(() => dialogVisible.value, (val) => {
  emit('update:modelValue', val)
})

// 上传配置
const uploadAction = '/api/document/upload'
const uploadHeaders = {
  // TODO: 添加认证头
}
const uploadData = {
  projectId: props.projectId
}

// 上传列表
const uploadList = ref<{
  uid: string
  name: string
  status: 'uploading' | 'success' | 'error'
  url?: string
}[]>([])

// 是否有成功上传的文件
const hasSuccessfulUploads = computed(() => {
  return uploadList.value.some(item => item.status === 'success')
})

// 导入状态
const importing = ref(false)

// 获取状态文本
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    uploading: '上传中',
    success: '上传成功',
    error: '上传失败'
  }
  return statusMap[status] || status
}

// 上传前检查
const handleBeforeUpload = (file: UploadRawFile) => {
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('上传文件大小不能超过 10MB!')
    return false
  }

  // 添加到上传列表
  uploadList.value.push({
    uid: file.uid,
    name: file.name,
    status: 'uploading'
  })

  return true
}

// 上传成功
const handleUploadSuccess = (response: any, file: any) => {
  const index = uploadList.value.findIndex(item => item.uid === file.uid)
  if (index !== -1) {
    uploadList.value[index] = {
      ...uploadList.value[index],
      status: 'success',
      url: response.url
    }
  }
  ElMessage.success(`${file.name} 上传成功`)
}

// 上传失败
const handleUploadError = (error: Error, file: any) => {
  const index = uploadList.value.findIndex(item => item.uid === file.uid)
  if (index !== -1) {
    uploadList.value[index] = {
      ...uploadList.value[index],
      status: 'error'
    }
  }
  ElMessage.error(`${file.name} 上传失败`)
}

// 超出限制
const handleExceed = () => {
  ElMessage.warning('最多只能上传10个文件')
}

// 清空列表
const handleClearList = () => {
  uploadList.value = []
}

// 处理导入
const handleImport = async () => {
  try {
    importing.value = true
    
    // 获取成功上传的文件URL列表
    const urls = uploadList.value
      .filter(item => item.status === 'success')
      .map(item => item.url)
      .filter(Boolean)

    if (urls.length === 0) {
      ElMessage.warning('没有可导入的文件')
      return
    }

    // TODO: 调用导入API
    console.log('Import documents:', urls)
    
    emit('success')
    handleClose()
  } catch (error) {
    // @ts-ignore
    console.error('Failed to import documents:', error)
    ElMessage.error('导入失败')
  } finally {
    importing.value = false
  }
}

// 处理关闭
const handleClose = () => {
  dialogVisible.value = false
  uploadList.value = []
}
</script>

<style lang="scss" scoped>
.import-container {
  .document-upload {
    width: 100%;
  }
}

.upload-list {
  margin-top: 20px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;

  .list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px;
    border-bottom: 1px solid var(--el-border-color-lighter);

    .title {
      font-size: 14px;
      font-weight: 500;
      color: var(--el-text-color-primary);
    }
  }

  .upload-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px;
    border-bottom: 1px solid var(--el-border-color-lighter);

    &:last-child {
      border-bottom: none;
    }

    .item-info {
      display: flex;
      align-items: center;
      gap: 8px;
      flex: 1;
      min-width: 0;

      .filename {
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-upload) {
  width: 100%;
}

:deep(.el-upload-dragger) {
  width: 100%;
}
</style> 