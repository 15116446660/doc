<template>
  <el-dialog
    title="历史会话"
    v-model="dialogVisible"
    width="600px"
    @close="$emit('close')"
  >
    <div class="history-dialog-content">
      <!-- 搜索框 -->
      <div class="search-container">
        <el-input
          v-model="searchQuery"
          placeholder="搜索会话..."
          prefix-icon="Search"
          clearable
        />
      </div>
      
      <!-- 会话列表 -->
      <div class="conversations-list">
        <template v-if="filteredConversations.length > 0">
          <div
            v-for="conversation in filteredConversations"
            :key="conversation.id"
            :class="['conversation-item', { active: conversation.id === currentConversationId }]"
            @click="selectConversation(conversation.id)"
          >
            <div class="conversation-info">
              <div class="conversation-title">
                {{ conversation.title || formatDate(conversation.createdAt) }}
                <el-tag v-if="conversation.favorite" size="small" type="warning">收藏</el-tag>
              </div>
              <div class="conversation-date">{{ formatDate(conversation.updatedAt) }}</div>
            </div>
            
            <div class="conversation-actions">
              <el-tooltip content="重命名" placement="top">
                <el-button
                  link
                  @click.stop="openRenameDialog(conversation)"
                >
                  <el-icon><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              
              <el-tooltip content="收藏" placement="top">
                <el-button
                  link
                  @click.stop="toggleFavorite(conversation)"
                >
                  <el-icon><Star :class="{ 'is-favorite': conversation.favorite }" /></el-icon>
                </el-button>
              </el-tooltip>
              
              <el-tooltip content="删除" placement="top">
                <el-button
                  link
                  @click.stop="confirmDelete(conversation.id)"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </el-tooltip>
            </div>
          </div>
        </template>
        
        <el-empty v-else description="没有找到会话记录" />
      </div>
    </div>
  </el-dialog>
  
  <!-- 重命名对话框 -->
  <el-dialog
    v-model="renameDialogVisible"
    title="重命名会话"
    width="400px"
    append-to-body
  >
    <el-input v-model="newTitle" placeholder="请输入新标题" />
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="renameDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="renameConversation">确认</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessageBox } from 'element-plus'
import { Edit, Star, Delete } from '@element-plus/icons-vue'
import type { Conversation } from '@/types/chat'

// 定义组件属性
const props = defineProps<{
  conversations: Conversation[]
  currentConversationId?: string
}>()

// 定义事件
const emit = defineEmits<{
  (e: 'select', conversationId: string): void
  (e: 'delete', conversationId: string): void
  (e: 'rename', conversationId: string, newTitle: string): void
  (e: 'favorite', conversationId: string, favorite: boolean): void
  (e: 'close'): void
}>()

// 状态
const dialogVisible = ref(true)
const searchQuery = ref('')
const renameDialogVisible = ref(false)
const currentEditingConversation = ref<Conversation | null>(null)
const newTitle = ref('')

// 计算属性：过滤后的会话列表
const filteredConversations = computed(() => {
  if (!searchQuery.value) {
    return [...props.conversations].sort((a, b) => b.updatedAt - a.updatedAt)
  }
  
  const query = searchQuery.value.toLowerCase()
  return props.conversations
    .filter(conv => {
      // 搜索标题或内容
      const title = conv.title || formatDate(conv.createdAt)
      const hasMatchingTitle = title.toLowerCase().includes(query)
      
      // 搜索消息内容
      const hasMatchingContent = conv.messages.some(msg => 
        msg.content.toLowerCase().includes(query)
      )
      
      return hasMatchingTitle || hasMatchingContent
    })
    .sort((a, b) => b.updatedAt - a.updatedAt)
})

// 格式化日期
function formatDate(timestamp: number): string {
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 选择会话
function selectConversation(conversationId: string): void {
  emit('select', conversationId)
}

// 确认删除会话
function confirmDelete(conversationId: string): void {
  ElMessageBox.confirm('确定要删除这个会话吗？此操作不可恢复。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    emit('delete', conversationId)
  }).catch(() => {})
}

// 打开重命名对话框
function openRenameDialog(conversation: Conversation): void {
  currentEditingConversation.value = conversation
  newTitle.value = conversation.title || formatDate(conversation.createdAt)
  renameDialogVisible.value = true
}

// 重命名会话
function renameConversation(): void {
  if (currentEditingConversation.value && newTitle.value.trim()) {
    emit('rename', currentEditingConversation.value.id, newTitle.value.trim())
    renameDialogVisible.value = false
  }
}

// 切换收藏状态
function toggleFavorite(conversation: Conversation): void {
  emit('favorite', conversation.id, !conversation.favorite)
}

// 监听对话框关闭
watch(dialogVisible, (newValue) => {
  if (!newValue) {
    emit('close')
  }
})
</script>

<style scoped>
.history-dialog-content {
  height: 500px;
  display: flex;
  flex-direction: column;
}

.search-container {
  margin-bottom: 16px;
}

.conversations-list {
  flex: 1;
  overflow-y: auto;
  border: 1px solid var(--el-border-color-light);
  border-radius: 4px;
}

.conversation-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--el-border-color-light);
  cursor: pointer;
  transition: background-color 0.2s;
}

.conversation-item:hover {
  background-color: var(--el-fill-color-light);
}

.conversation-item.active {
  background-color: var(--el-color-primary-light-9);
}

.conversation-info {
  flex: 1;
  overflow: hidden;
}

.conversation-title {
  font-weight: 500;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conversation-date {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.conversation-actions {
  display: flex;
  align-items: center;
  opacity: 0.6;
  transition: opacity 0.2s;
}

.conversation-item:hover .conversation-actions {
  opacity: 1;
}

.is-favorite {
  color: var(--el-color-warning);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style> 