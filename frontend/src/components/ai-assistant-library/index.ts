// 导出所有组件
import Chat from './Chat.vue'
import ChatBubbleList from './ChatBubbleList.vue'
import ChatSender from './ChatSender.vue'
import MarkdownMessage from './MarkdownMessage.vue'
import HistoryDialog from './HistoryDialog.vue'
import ModelManagementDialog from './ModelManagementDialog.vue'
import CommandManagementModal from './CommandManagementModal.vue'
import SettingsModal from './SettingsModal.vue'

// 导出所有钩子函数
import { useChat } from './hooks/useChat'
import { useConversations } from './hooks/useConversations'
import { useAIModels } from './hooks/useAIModels'
import { usePromptCommands } from './hooks/usePromptCommands'

// 导出所有类型
import type { 
  Message, 
  MessageRole, 
  MessageStatus, 
  Attachment, 
  Conversation,
  AIModel,
  Command,
  CommandParameter,
  KnowledgeBase,
  ChatSettings
} from '@/types/chat'

// 导出组件
export {
  Chat,
  ChatBubbleList,
  ChatSender,
  MarkdownMessage,
  HistoryDialog,
  ModelManagementDialog,
  CommandManagementModal,
  SettingsModal
}

// 导出钩子函数
export {
  useChat,
  useConversations,
  useAIModels,
  usePromptCommands
}

// 导出类型
export type {
  Message, 
  MessageRole, 
  MessageStatus, 
  Attachment, 
  Conversation,
  AIModel,
  Command,
  CommandParameter,
  KnowledgeBase,
  ChatSettings
}

// 默认导出
export default {
  install: (app: any) => {
    app.component('Chat', Chat)
    app.component('ChatBubbleList', ChatBubbleList)
    app.component('ChatSender', ChatSender)
    app.component('MarkdownMessage', MarkdownMessage)
    app.component('HistoryDialog', HistoryDialog)
    app.component('ModelManagementDialog', ModelManagementDialog)
    app.component('CommandManagementModal', CommandManagementModal)
    app.component('SettingsModal', SettingsModal)
  }
} 