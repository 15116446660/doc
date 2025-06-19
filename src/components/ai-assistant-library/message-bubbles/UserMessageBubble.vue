<template>
  <!-- 编辑模式 -->
  <div v-if="message.status === 'editing'" class="edit-message-container">
    <el-input
      type="textarea"
      :model-value="editingContent"
      @update:model-value="updateContent"
      :rows="calculateRows(editingContent)"
      placeholder="编辑消息..."
      resize="none"
      autofocus
      :autosize="{ minRows: 1, maxRows: 10 }"
    />
    <div class="edit-actions">
      <el-button size="small" @click="$emit('cancel-edit', message)">取消</el-button>
      <el-button size="small" type="primary" @click="$emit('save-edit', message)">发送</el-button>
    </div>
  </div>
  
  <!-- 显示模式 -->
  <div v-else class="user-message">
    {{ message.content }}
    <span v-if="message.edited" class="edited-badge">(已编辑)</span>
  </div>
</template>

<script setup lang="ts">
import type { Message } from '@/types/chat'

// 定义组件属性
const props = defineProps<{
  message: Message
  editingContent: string
  calculateRows: (text: string) => number
}>()

// 定义事件
const emit = defineEmits<{
  (e: 'save-edit', message: Message): void
  (e: 'cancel-edit', message: Message): void
  (e: 'update:editingContent', value: string): void
}>()

// 更新编辑内容
const updateContent = (value: string) => {
  emit('update:editingContent', value)
}
</script>

<style scoped>
/* 用户消息样式 */
.user-message {
  white-space: pre-wrap;
  word-break: break-word;
}

/* 编辑消息样式 */
.edit-message-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.edit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.edited-badge {
  font-size: 12px;
  color: #909399;
  margin-left: 6px;
  font-style: italic;
}
</style> 