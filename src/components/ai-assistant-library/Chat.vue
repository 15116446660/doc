<template>
  <div class="chat-container" :class="{ 'dark-mode': isDarkMode }">
    <!-- 聊天气泡列表 -->
    <ChatBubbleList
      ref="chatBubbleListRef"
      :messages="messages"
      :userAvatar="userAvatar"
      :assistantAvatar="assistantAvatar"
      :quickCommands="quickCommands"
      :autoScroll="true"
      @regenerate="regenerateMessage"
      @stop="stopGenerating"
      @feedback="handleMessageFeedback"
      @command="handleQuickCommand"
      @startEditing="handleStartEditingMessage"
      @cancelEditing="handleCancelEditingMessage"
      @saveEditing="handleSaveEditingMessage"
    />
    
    <!-- 消息输入发送区域 -->
    <ChatSender
      :isGenerating="isGenerating"
      :models="models"
      :currentModelId="currentModelId"
      :commands="commands"
      :isDeepThinkingMode="isDeepThinkingMode"
      :isRAGMode="isRAGMode"
      :isFullTextReferenceMode="isFullTextReferenceMode"
      :currentKnowledgeBaseId="currentKnowledgeBaseId"
      @send="handleSendMessage"
      @stop="stopGenerating"
      @modelChange="handleModelChange"
      @command="handleExecuteCommand"
      @toggleDeepThinking="toggleDeepThinkingMode"
      @toggleRAG="toggleRAGMode"
      @toggleFullTextReference="toggleFullTextReferenceMode"
      @openModelConfig="openModelConfig"
      @selectKnowledgeBase="handleSelectKnowledgeBase"
      @clearSelectedKnowledgeBase="handleClearSelectedKnowledgeBase"
      @new-chat="handleNewConversation"
      @open-history="handleOpenHistory"
      @clear-chat="handleClearCurrentConversation"
      @open-command-management="handleOpenCommandManagement"
    />
    
    <!-- 历史会话对话框 -->
    <HistoryDialog
      v-if="showHistoryDialog"
      :conversations="conversations"
      :currentConversationId="activeConversationId || undefined"
      @select="handleSelectConversation"
      @delete="handleDeleteConversation"
      @rename="handleRenameConversation"
      @favorite="handleToggleFavorite"
      @clearNonFavorites="handleClearNonFavorites"
      @close="showHistoryDialog = false"
    />
    
    <!-- 模型配置对话框 -->
    <ModelManagementDialog
      v-if="showModelConfigModal"
      :show="showModelConfigModal"
      :models="models"
      :current-model-id="currentModelId"
      @update:show="showModelConfigModal = false"
      @select-model="handleModelChange"
      @add-model="handleAddModel"
      @update-model="handleUpdateModel"
      @delete-model="handleDeleteModel"
      @set-default-model="handleSetDefaultModel"
    />
    
    <!-- 命令管理对话框 -->
    <CommandManagementModal
      v-if="showCommandModal"
      :commands="commands"
      @save="handleSaveCommand"
      @delete="handleDeleteCommand"
      @close="showCommandModal = false"
    />
    
    <!-- 设置对话框 -->
    <SettingsModal
      v-if="showSettingsModal"
      v-model:visible="showSettingsModal"
      :theme="localTheme"
      :userAvatar="localUserAvatar"
      :assistantAvatar="localAssistantAvatar"
      @save="handleSaveSettings"
      @close="showSettingsModal = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import ChatBubbleList from './ChatBubbleList.vue'
import ChatSender from './ChatSender.vue'
import HistoryDialog from './HistoryDialog.vue'
import ModelManagementDialog from './ModelManagementDialog.vue'
import CommandManagementModal from './CommandManagementModal.vue'
import SettingsModal from './SettingsModal.vue'
import { useChat } from './hooks/useChat'
import { useConversations } from './hooks/useConversations'
import { useAIModels } from './hooks/useAIModels'
import { usePromptCommands } from './hooks/usePromptCommands'
import type { Attachment, Command, Message, AIModel } from '@/types/chat'

// 组件属性定义
const props = defineProps<{
  theme?: 'light' | 'dark' | 'auto'
  initialConversationId?: string
  userAvatar?: string
  assistantAvatar?: string
}>()

// 定义事件
const emit = defineEmits<{
  (e: 'themeChange', theme: 'light' | 'dark' | 'auto'): void
  (e: 'avatarChange', type: 'user' | 'assistant', url: string): void
}>()

// 对话框显示状态
const showHistoryDialog = ref(false)
const showModelConfigModal = ref(false)
const showCommandModal = ref(false)
const showSettingsModal = ref(false)

// 本地设置状态
const localTheme = ref<'light' | 'dark' | 'auto'>(props.theme || 'auto')
const localUserAvatar = ref<string>(props.userAvatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png')
const localAssistantAvatar = ref<string>(props.assistantAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png')

// 从各个hooks获取状态和方法
const {
  messages,
  isGenerating,
  isDeepThinkingMode,
  isRAGMode,
  isFullTextReferenceMode,
  sendUserMessage,
  stopGeneration,
  regenerateMessage,
  setMessageFeedback,
  currentModelId,
  setCurrentModel,
  toggleDeepThinkingMode,
  toggleRAGMode,
  toggleFullTextReferenceMode,
  setCurrentKnowledgeBase,
  currentKnowledgeBaseId,
  editUserMessage,
  startEditingMessage,
  cancelEditingMessage
} = useChat()

const {
  conversations,
  activeConversationId,
  activeConversation,
  createConversation,
  loadConversation,
  saveConversation,
  deleteConversation,
  renameConversation,
  toggleFavorite,
  clearNonFavoriteConversations,
  init: initConversations,
} = useConversations()

const {
  models,
  loadModels,
  addModel,
  updateModel,
  deleteModel,
  setDefaultModel
} = useAIModels()

const {
  commands,
  loadCommands,
  addCommand,
  updateCommandById,
  removeCommand
} = usePromptCommands()

// 引用聊天气泡列表组件
const chatBubbleListRef = ref<InstanceType<typeof ChatBubbleList> | null>(null)

// 计算属性：是否为暗色模式
const isDarkMode = computed(() => {
  if (props.theme === 'dark') return true
  if (props.theme === 'light') return false
  // 自动模式，根据系统偏好设置
  return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches
})

// 计算属性：快捷命令
const quickCommands = computed(() => {
  // 返回前5个命令作为快捷命令
  return commands.value.slice(0, 5)
})

// 监听消息变化，自动滚动到底部
watch(
  () => messages.value,
  () => {
    nextTick(() => {
      chatBubbleListRef.value?.scrollToBottom()
    })
  },
  { deep: true }
)

// 监听当前活跃会话变化，同步消息
watch(
  () => activeConversation.value,
  (newConversation) => {
    if (newConversation) {
      messages.value = newConversation.messages;
      setCurrentModel(newConversation.modelId);
    } else {
      messages.value = [];
    }
  },
  { immediate: true }
);

// 初始化
onMounted(async () => {
  await initConversations()
  await loadModels()
  await loadCommands()

  if (props.initialConversationId) {
    loadConversation(props.initialConversationId)
  } else if (conversations.value.length === 0) {
    createConversation(currentModelId.value)
  }

  const savedTheme = localStorage.getItem('chatTheme') as 'light' | 'dark' | 'auto' | null
  if (savedTheme) {
    localTheme.value = savedTheme
    emit('themeChange', savedTheme)
  }

  const savedUserAvatar = localStorage.getItem('chatUserAvatar')
  if (savedUserAvatar) {
    localUserAvatar.value = savedUserAvatar
    emit('avatarChange', 'user', savedUserAvatar)
  }

  const savedAssistantAvatar = localStorage.getItem('chatAssistantAvatar')
  if (savedAssistantAvatar) {
    localAssistantAvatar.value = savedAssistantAvatar
    emit('avatarChange', 'assistant', savedAssistantAvatar)
  }

  const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')

  const handleSystemThemeChange = () => {
    if (localTheme.value === 'auto') {
      emit('themeChange', 'auto')
    }
  }
  mediaQuery.addEventListener('change', handleSystemThemeChange)
  onBeforeUnmount(() => {
    mediaQuery.removeEventListener('change', handleSystemThemeChange)
  })
})

// 定义 stopGenerating 函数，关联到 stopGeneration
const stopGenerating = stopGeneration;

// 处理发送消息
const handleSendMessage = async (content: string, files: File[]) => {
  let attachments: Attachment[] = []
  if (files.length > 0) {
    try {
      attachments = files.map(file => ({
        id: Date.now().toString(),
        name: file.name,
        type: file.type,
        size: file.size,
        url: URL.createObjectURL(file)
      }))
    } catch (error) {
      ElMessage.error('附件处理失败')
      return
    }
  }
  
  // 如果启用了RAG模式，确保传递知识库ID
  if (isRAGMode.value) {
    await sendUserMessage(content, attachments, undefined, undefined)
  } else {
    await sendUserMessage(content, attachments)
  }
  
  saveCurrentConversation()
}

// 处理模型变更
const handleModelChange = (modelId: string) => {
  setCurrentModel(modelId)
  saveCurrentConversation()
}

// 处理消息反馈
const handleMessageFeedback = (messageId: string, feedback: 'like' | 'dislike') => {
  setMessageFeedback(messageId, feedback)
  saveCurrentConversation()
}

// 处理来自 ChatBubbleList 的快捷命令
const handleQuickCommand = (command: Command) => {
  handleExecuteCommand(command)
}

const handleExecuteCommand = (command: Command, input: string = '') => {
  sendUserMessage(
    command.prompt
      ? command.prompt.replace('{input}', input)
      : input,
    [], // attachments
    undefined, // knowledgeBaseId
    command.id // commandId
  );
  saveCurrentConversation();
}

const handleSelectKnowledgeBase = (knowledgeBaseId: string) => {
  setCurrentKnowledgeBase(knowledgeBaseId)
}

const handleClearSelectedKnowledgeBase = () => {
  setCurrentKnowledgeBase(null)
}

const handleStartEditingMessage = (messageId: string) => {
  startEditingMessage(messageId)
}

const handleCancelEditingMessage = (messageId: string) => {
  cancelEditingMessage(messageId)
}

const handleSaveEditingMessage = (messageId: string, newContent: string) => {
  editUserMessage(messageId, newContent)
  saveCurrentConversation()
}

// 新建会话
const handleNewConversation = () => {
  createConversation(currentModelId.value)
}

// 打开历史记录
const handleOpenHistory = () => {
  showHistoryDialog.value = true
}

// 清空当前会话
const handleClearCurrentConversation = () => {
  if (activeConversation.value) {
    messages.value = []
    saveCurrentConversation()
    ElMessage.success('当前会话已清空')
  }
}

// 保存当前会话
const saveCurrentConversation = () => {
  if (activeConversationId.value) {
    saveConversation(activeConversationId.value, messages.value, currentModelId.value)
  }
}

// 处理选择会话
const handleSelectConversation = (conversationId: string) => {
  loadConversation(conversationId)
  showHistoryDialog.value = false
  // 切换后滚动到底部
  nextTick(() => {
    chatBubbleListRef.value?.scrollToBottom()
  })
}

const handleDeleteConversation = (conversationId: string) => {
  deleteConversation(conversationId)
}

const handleRenameConversation = (conversationId: string, newTitle: string) => {
  renameConversation(conversationId, newTitle)
}

const handleToggleFavorite = (conversationId: string) => {
  toggleFavorite(conversationId)
}

const handleClearNonFavorites = () => {
  clearNonFavoriteConversations()
  ElMessage.success('非收藏会话已清空')
}

// 打开命令管理
const handleOpenCommandManagement = () => {
  showCommandModal.value = true
}

// 打开模型配置
const openModelConfig = () => {
  showModelConfigModal.value = true
}

// 处理命令保存
const handleSaveCommand = (command: Command) => {
  if (command.id) {
    updateCommandById(command.id, command)
  } else {
    addCommand(command)
  }
}

// 处理命令删除
const handleDeleteCommand = (commandId: string) => {
  removeCommand(commandId)
}

// 处理设置保存
const handleSaveSettings = (settings: { theme: 'light' | 'dark' | 'auto', userAvatar: string, assistantAvatar:string }) => {
  localTheme.value = settings.theme
  localUserAvatar.value = settings.userAvatar
  localAssistantAvatar.value = settings.assistantAvatar
  emit('themeChange', settings.theme)
  emit('avatarChange', 'user', settings.userAvatar)
  emit('avatarChange', 'assistant', settings.assistantAvatar)

  // 保存到本地存储
  localStorage.setItem('chatTheme', settings.theme)
  localStorage.setItem('chatUserAvatar', settings.userAvatar)
  localStorage.setItem('chatAssistantAvatar', settings.assistantAvatar)
}

// --- 模型管理 ---
async function handleAddModel(modelData: Omit<AIModel, 'id'>) {
  await addModel(modelData);
}

async function handleUpdateModel(modelData: Partial<AIModel> & { id: string }) {
  await updateModel(modelData);
}

async function handleDeleteModel(modelId: string) {
  await deleteModel(modelId);
}

async function handleSetDefaultModel(modelId: string) {
  await setDefaultModel(modelId);
}
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  background-color: var(--el-bg-color);
  color: var(--el-text-color-primary);
  transition: all 0.3s ease;
}

.chat-container.dark-mode {
  --el-bg-color: #1a1a1a;
  --el-text-color-primary: #f0f0f0;
  --el-border-color: #333;
}

:deep(.chat-bubble-list) {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

:deep(.chat-sender) {
  border-top: 1px solid var(--el-border-color);
  padding: 10px;
}
</style> 


