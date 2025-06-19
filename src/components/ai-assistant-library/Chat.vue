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
      @command="handleExecuteCommand"
      @subCommand="handleSelectSubCommand"
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
      :currentKnowledgeBaseId="currentKnowledgeBaseId"
      @send="handleSendMessage"
      @stop="stopGenerating"
      @modelChange="handleModelChange"
      @command="handleExecuteCommand"
      @toggleDeepThinking="toggleDeepThinkingMode"
      @toggleRAG="toggleRAGMode"
      @openModelConfig="openModelConfig"
      @selectKnowledgeBase="handleSelectKnowledgeBase"
      @clearSelectedKnowledgeBase="handleClearSelectedKnowledgeBase"
      @new-chat="handleNewConversation"
      @open-history="handleOpenHistory"
      @clear-chat="handleClearCurrentConversation"
      @open-command-management="handleOpenCommandManagement"
      @view-sub-commands="viewCommandSubCommands"
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
import type { Attachment, Command, Message, AIModel, SubCommand } from '@/types/chat'
import { v4 as uuidv4 } from 'uuid'

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
  removeCommand,
  fetchSubCommands
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
const handleQuickCommand = (commandId: string) => {
  const command = commands.find(cmd => cmd.id === commandId)
  if (command) {
    handleExecuteCommand(command)
  }
}

const handleExecuteCommand = (command: Command, input: string = '') => {
  if (command.hasSubCommands) {
    // If the command has sub-commands, load them from API
    handleCommandWithSubCommands(command)
  } else {
    // Handle regular command
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
}

// Add a new function to handle commands that have sub-commands
const handleCommandWithSubCommands = async (command: Command) => {
  try {
    // Show loading state in UI if needed
    isGenerating.value = true;
    
    // Create a user message indicating the parent command was selected
    const userMessage: Message = {
      id: uuidv4(),
      role: 'user',
      content: `已选择: ${command.name}`,
      timestamp: Date.now(),
      commandId: command.id,
      commandName: command.name
    };
    
    messages.value.push(userMessage);
    
    // Get context parameters if needed (like documentId, etc.)
    const context = {
      // Add context parameters needed for your application
      // Example: documentId: currentDocument.value?.id
    };
    
    // Fetch sub-commands from API
    const subCommands = await fetchSubCommands(command.id, context);
    
    if (subCommands.length === 0) {
      // No sub-commands available, show an error
      const errorMessage: Message = {
        id: uuidv4(),
        role: 'assistant',
        content: `抱歉，'${command.name}'命令没有可用的子命令。`,
        timestamp: Date.now(),
        status: 'completed'
      };
      
      messages.value.push(errorMessage);
    } else {
      // Create an assistant message to show available sub-commands
      const subCommandsMessage: Message = {
        id: uuidv4(),
        role: 'assistant',
        content: generateSubCommandsContent(command.name, subCommands),
        timestamp: Date.now(),
        status: 'completed',
        subCommands: subCommands // Store sub-commands in message for reference
      };
      
      messages.value.push(subCommandsMessage);
    }
    
    saveCurrentConversation();
  } catch (error: any) {
    console.error('加载子命令失败:', error);
    
    // Show error message
    const errorMessage: Message = {
      id: uuidv4(),
      role: 'assistant',
      content: `加载'${command.name}'的子命令失败: ${error.message || '未知错误'}`,
      timestamp: Date.now(),
      status: 'error',
      error: error.message
    };
    
    messages.value.push(errorMessage);
    saveCurrentConversation();
  } finally {
    isGenerating.value = false;
  }
}

// Helper function to generate content for sub-commands display
const generateSubCommandsContent = (commandName: string, subCommands: SubCommand[]): string => {
  let content = `### ${commandName} - 可用操作\n\n请选择以下操作之一:\n\n`;
  
  subCommands.forEach(subCmd => {
    content += `- **${subCmd.name}**: ${subCmd.description || ''}\n`;
  });
  
  content += '\n点击下方对应的操作按钮继续。';
  return content;
}

// Handle selecting a specific sub-command
const handleSelectSubCommand = (parentCommand: Command, subCommand: SubCommand, input: string = '') => {
  // Create a user message indicating which sub-command was selected
  const userMessage: Message = {
    id: uuidv4(),
    role: 'user',
    content: `已选择: ${subCommand.name}${input ? `\n\n${input}` : ''}`,
    timestamp: Date.now(),
    commandId: `${parentCommand.id}_${subCommand.id}`,
    commandName: subCommand.name
  };
  
  messages.value.push(userMessage);
  
  // Process the template to replace placeholders
  let template = subCommand.template || '';
  
  // Replace common placeholders
  template = template
    .replace(/{input}/g, input)
    .replace(/{selectedText}/g, ''); // Add your selected text logic here if needed
  
  // Send the processed template as a user message
  sendUserMessage(
    template,
    [], // attachments
    undefined, // knowledgeBaseId
    `${parentCommand.id}_${subCommand.id}` // commandId
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

/**
 * 查看命令的子命令列表
 * 
 * 将命令的子命令作为AI助手消息显示在对话中
 * 
 * @param commandId 命令ID
 */
const viewCommandSubCommands = async (commandId: string) => {
  try {
    isGenerating.value = true;
    
    // 查找命令
    const command = findCommand(commandId);
    if (!command) {
      throw new Error(`未找到命令: ${commandId}`);
    }
    
    // 获取子命令
    const subCommands = await fetchSubCommands(commandId);
    if (!subCommands || subCommands.length === 0) {
      throw new Error(`命令 '${command.name}' 没有子命令`);
    }
    
    // 生成子命令消息内容
    const content = generateSubCommandsContent(command.name, subCommands);
    
    // 创建消息
    const message: Message = {
      id: uuidv4(),
      role: 'assistant',
      content,
      timestamp: Date.now(),
      status: 'completed',
      commandId,
      commandName: command.name,
      subCommands // 添加子命令列表供UI展示
    };
    
    messages.value.push(message);
    saveCurrentConversation();
    
    // 滚动到底部
    nextTick(() => {
      chatBubbleListRef.value?.scrollToBottom();
    });
    
  } catch (error: any) {
    console.error('查看子命令失败:', error);
    
    // 显示错误消息
    const errorMessage: Message = {
      id: uuidv4(),
      role: 'assistant',
      content: `查看'${commandId}'的子命令失败: ${error.message || '未知错误'}`,
      timestamp: Date.now(),
      status: 'error',
      error: error.message
    };
    
    messages.value.push(errorMessage);
    saveCurrentConversation();
  } finally {
    isGenerating.value = false;
  }
}

/**
 * 查找命令对象
 * 
 * @param commandId 命令ID
 * @returns 找到的命令对象，如果未找到则返回undefined
 */
const findCommand = (commandId: string) => {
  if (!commandId) return undefined;
  
  // 从commands数组中查找命令
  return commands.value.find(cmd => cmd.id === commandId);
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


