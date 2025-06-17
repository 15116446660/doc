<template>
  <div class="chat-container" :class="{ 'dark-mode': isDarkMode }">
    <!-- 聊天气泡列表 -->
    <ChatBubbleList
      :messages="messages"
      :userAvatar="userAvatar"
      :assistantAvatar="assistantAvatar"
      :quickCommands="quickCommands"
      :autoScroll="true"
      @regenerate="regenerateMessage"
      @stop="stopGenerating"
      @feedback="handleMessageFeedback"
      @command="handleQuickCommand"
    />
    
    <!-- 消息输入发送区域 -->
    <ChatSender
      :isGenerating="isGenerating"
      :models="models"
      :currentModelId="currentModelId"
      :commands="commands"
      :isDeepThinkingMode="isDeepThinkingMode"
      :isRAGMode="isRAGMode"
      @send="handleSendMessage"
      @stop="stopGenerating"
      @modelChange="handleModelChange"
      @command="handleCommand"
      @toggleDeepThinking="toggleDeepThinkingMode"
      @toggleRAG="toggleRAGMode"
      @openModelConfig="openModelConfig"
      @executeCommand="handleExecuteCommand"
    />
    
    <!-- 历史会话对话框 -->
    <HistoryDialog
      v-if="showHistoryDialog"
      :conversations="conversations"
      :currentConversationId="activeConversationId"
      @select="handleSelectConversation"
      @delete="handleDeleteConversation"
      @rename="handleRenameConversation"
      @close="showHistoryDialog = false"
    />
    
    <!-- 模型配置对话框 -->
    <ModelConfigModal
      v-if="showModelConfigModal"
      :models="models"
      :currentModelId="currentModelId"
      @save="handleSaveModel"
      @close="showModelConfigModal = false"
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
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ChatBubbleList from './ChatBubbleList.vue'
import ChatSender from './ChatSender.vue'
import HistoryDialog from './HistoryDialog.vue'
import ModelConfigModal from './ModelConfigModal.vue'
import CommandManagementModal from './CommandManagementModal.vue'
import SettingsModal from './SettingsModal.vue'
import { useChat } from './hooks/useChat'
import { useConversations } from './hooks/useConversations'
import { useAIModels } from './hooks/useAIModels'
import { usePromptCommands } from './hooks/usePromptCommands'
import type { Attachment, Command } from '@/types/chat'

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
  sendUserMessage,
  stopGenerating,
  regenerateMessage,
  clearMessages,
  setMessageFeedback,
  currentModelId,
  setCurrentModel,
  toggleDeepThinkingMode,
  toggleRAGMode
} = useChat()

const {
  conversations,
  activeConversationId,
  createConversation,
  loadConversation,
  saveConversation,
  deleteConversation,
  renameConversation
} = useConversations()

const {
  models,
  loadModels,
  addCustomModel,
  updateModel
} = useAIModels()

const {
  commands,
  loadCommands,
  findCommand,
  addCommand,
  updateCommandById,
  removeCommand
} = usePromptCommands()

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
  await sendUserMessage(content, attachments)
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
const handleQuickCommand = (commandId: string) => {
  try {
    const command = findCommand(commandId)
    if (command && command.prompt) {
      sendUserMessage(command.prompt, undefined, command.id, command.name)
      saveCurrentConversation()
    } else if (command) {
      // 如果命令没有预设prompt，可以执行其他操作
      ElMessage.info(`执行了命令: ${command.name}`)
    }
  } catch (error) {
    ElMessage.error('执行命令失败')
  }
}

// 处理来自 ChatSender 的带上下文的命令执行
const handleExecuteCommand = async (command: Command, context: string) => {
  if (command.prompt) {
    const finalPrompt = command.prompt.replace('{selectedText}', context)
    await sendUserMessage(finalPrompt, [], command.id, command.name)
    saveCurrentConversation()
  } else {
    ElMessage.info(`执行了命令: ${command.name}`)
  }
}

// 处理其他命令菜单操作
const handleCommand = (command: string) => {
  switch (command) {
    case 'new':
      createNewConversation()
      break
    case 'history':
      showHistoryDialog.value = true
      break
    case 'commands':
      showCommandModal.value = true
      break
    case 'settings':
      showSettingsModal.value = true
      break
    case 'clear':
      ElMessageBox.confirm('确定要清空当前会话吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        clearMessages()
        saveCurrentConversation()
      }).catch(() => {})
      break
  }
}

// 打开模型配置
const openModelConfig = () => {
  showModelConfigModal.value = true
}

// 创建新会话
const createNewConversation = () => {
  createConversation(currentModelId.value)
  clearMessages()
}

// 处理选择会话
const handleSelectConversation = (conversationId: string) => {
  loadConversation(conversationId)
  showHistoryDialog.value = false
}

// 处理删除会话
const handleDeleteConversation = (conversationId: string) => {
  deleteConversation(conversationId)
}

// 处理重命名会话
const handleRenameConversation = (conversationId: string, newTitle: string) => {
  renameConversation(conversationId, newTitle)
}

// 处理保存模型
const handleSaveModel = (model: any) => {
  if (model.id && models.value.some(m => m.id === model.id)) {
    // 更新现有模型
    updateModel(model.id, model)
  } else {
    // 添加新模型
    addCustomModel(model)
  }
  showModelConfigModal.value = false
}

// 处理保存命令
const handleSaveCommand = async (command: Omit<Command, 'id' | 'createdAt' | 'updatedAt'> & { id?: string }) => {
  if (command.id && commands.value.some(cmd => cmd.id === command.id)) {
    // 更新现有命令
    await updateCommandById(command.id, command)
  } else {
    // 添加新命令
    await addCommand(command)
  }
  showCommandModal.value = false
}

// 处理删除命令
const handleDeleteCommand = (commandId: string) => {
  removeCommand(commandId)
}

// 保存当前会话
const saveCurrentConversation = () => {
  if (activeConversationId.value) {
    saveConversation(activeConversationId.value, messages.value, currentModelId.value)
  }
}

// 处理保存设置
const handleSaveSettings = (settings: { theme: 'light' | 'dark' | 'auto', userAvatar: string, assistantAvatar: string }) => {
  // 更新本地设置
  localTheme.value = settings.theme
  localUserAvatar.value = settings.userAvatar
  localAssistantAvatar.value = settings.assistantAvatar
  
  // 触发事件
  emit('themeChange', settings.theme)
  
  if (settings.userAvatar !== props.userAvatar) {
    emit('avatarChange', 'user', settings.userAvatar)
  }
  
  if (settings.assistantAvatar !== props.assistantAvatar) {
    emit('avatarChange', 'assistant', settings.assistantAvatar)
  }
  
  // 保存到本地存储
  localStorage.setItem('chatTheme', settings.theme)
  localStorage.setItem('userAvatar', settings.userAvatar)
  localStorage.setItem('assistantAvatar', settings.assistantAvatar)
  
  // 应用主题
  applyTheme(settings.theme)
}

// 应用主题
const applyTheme = (theme: 'light' | 'dark' | 'auto') => {
  if (theme === 'dark') {
    document.documentElement.classList.add('dark-mode')
  } else if (theme === 'light') {
    document.documentElement.classList.remove('dark-mode')
  } else {
    // 自动模式，根据系统偏好设置
    const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches
    document.documentElement.classList.toggle('dark-mode', prefersDark)
  }
}

// 计算属性：用户头像
const userAvatar = computed(() => {
  return localUserAvatar.value
})

// 计算属性：助手头像
const assistantAvatar = computed(() => {
  return localAssistantAvatar.value
})

// 初始化
onMounted(async () => {
  // 加载本地设置
  const savedTheme = localStorage.getItem('chatTheme') as 'light' | 'dark' | 'auto' | null
  const savedUserAvatar = localStorage.getItem('userAvatar')
  const savedAssistantAvatar = localStorage.getItem('assistantAvatar')
  
  if (savedTheme) {
    localTheme.value = savedTheme
    applyTheme(savedTheme)
  } else {
    applyTheme(localTheme.value)
  }
  
  if (savedUserAvatar) {
    localUserAvatar.value = savedUserAvatar
  }
  
  if (savedAssistantAvatar) {
    localAssistantAvatar.value = savedAssistantAvatar
  }

  // 加载模型
  await loadModels()
  
  // 加载命令
  await loadCommands()
  
  // 如果有初始会话ID，加载该会话
  if (props.initialConversationId) {
    loadConversation(props.initialConversationId)
  } else {
    // 否则创建新会话
    createConversation(currentModelId.value)
  }
  
  // 监听系统暗色模式变化
  const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
  const handleChange = () => {
    // 触发重新渲染
    if (localTheme.value === 'auto') {
      document.documentElement.classList.toggle('dark-mode', mediaQuery.matches)
    }
  }
  
  mediaQuery.addEventListener('change', handleChange)
  
  // 清理函数
  onBeforeUnmount(() => {
    mediaQuery.removeEventListener('change', handleChange)
  })
})

// 监听消息变化，保存会话
watch(messages, () => {
  saveCurrentConversation()
}, { deep: true })

// 监听当前模型变化，保存会话
watch(currentModelId, () => {
  saveCurrentConversation()
})
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