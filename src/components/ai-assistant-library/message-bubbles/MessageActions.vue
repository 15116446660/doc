<template>
  <!-- AI消息操作栏 -->
  <div class="message-actions" v-if="role === 'assistant' && status === 'completed'">
    <el-button 
      size="small" 
      link
      @click="emit('copy')"
      :title="'复制内容'"
    >
      <el-icon><DocumentCopy /></el-icon>
    </el-button>
    
    <el-button 
      size="small" 
      link
      @click="emit('regenerate')"
      v-if="isLatestMessage"
      :title="'重新生成'"
    >
      <el-icon><RefreshRight /></el-icon>
    </el-button>
    
    <div class="feedback-buttons">
      <el-button 
        size="small" 
        link
        @click="emit('feedback', 'like')"
        :class="{ active: props.feedback === 'like' }"
        :title="'有帮助'"
      >
        <el-icon><Star /></el-icon>
      </el-button>
      
      <el-button 
        size="small" 
        link
        @click="emit('feedback', 'dislike')"
        :class="{ active: props.feedback === 'dislike' }"
        :title="'没帮助'"
      >
        <el-icon><Close /></el-icon>
      </el-button>
    </div>
  </div>
  
  <!-- 用户消息操作栏 -->
  <div class="message-actions" v-else-if="role === 'user' && status !== 'editing'">
    <el-button 
      size="small" 
      link
      @click="emit('copy')"
      :title="'复制内容'"
    >
      <el-icon><DocumentCopy /></el-icon>
    </el-button>
    
    <el-button 
      size="small" 
      link
      @click="emit('edit')"
      :title="'编辑消息'"
    >
      <el-icon><Edit /></el-icon>
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { DocumentCopy, RefreshRight, Star, Close, Edit } from '@element-plus/icons-vue'

// 定义组件属性
const props = defineProps<{
  role: 'user' | 'assistant' | 'system'
  status?: string
  feedback?: 'like' | 'dislike'
  isLatestMessage?: boolean
}>()

// 定义事件
const emit = defineEmits<{
  (e: 'copy'): void
  (e: 'regenerate'): void
  (e: 'edit'): void
  (e: 'feedback', value: 'like' | 'dislike'): void
}>()
</script>

<style scoped>
.message-actions {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

.feedback-buttons {
  display: flex;
  gap: 4px;
}

.feedback-buttons .el-button.active {
  color: #409eff;
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .message-actions {
    color: #ddd;
  }
}
</style> 