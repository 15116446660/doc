<template>
  <div class="attachments-container">
    <div 
      v-for="attachment in attachments" 
      :key="attachment.id" 
      class="attachment-item"
      @click="$emit('attachment-click', attachment)"
    >
      <!-- 图片附件 -->
      <div v-if="isImageAttachment(attachment)" class="image-attachment">
        <img :src="attachment.thumbnail || attachment.url" :alt="attachment.name" />
      </div>
      
      <!-- 其他类型附件 -->
      <div v-else class="file-attachment">
        <el-icon><Document /></el-icon>
        <span>{{ attachment.name }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Document } from '@element-plus/icons-vue'
import type { Attachment } from '@/types/chat'

// 定义组件属性
const props = defineProps<{
  attachments: Attachment[]
}>()

// 定义事件
const emit = defineEmits<{
  (e: 'attachment-click', attachment: Attachment): void
}>()

// 判断附件是否为图片
const isImageAttachment = (attachment: Attachment): boolean => {
  return attachment.type.startsWith('image/') || 
    attachment.name.match(/\.(jpg|jpeg|png|gif|webp|svg)$/i) !== null
}
</script>

<style scoped>
/* 附件样式 */
.attachments-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.attachment-item {
  cursor: pointer;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid #ebeef5;
}

.image-attachment {
  width: 100px;
  height: 100px;
}

.image-attachment img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.file-attachment {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background-color: #f5f7fa;
}

.file-attachment .el-icon {
  margin-right: 8px;
  font-size: 18px;
  color: #909399;
}

.file-attachment span {
  font-size: 12px;
  color: #606266;
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .file-attachment {
    background-color: #2d2d2d;
  }
  
  .file-attachment span {
    color: #ddd;
  }
  
  .attachment-item {
    border-color: #444;
  }
}
</style> 