/**
 * AI助手相关类型定义
 */

// 消息角色类型
export type MessageRole = 'user' | 'assistant' | 'system';

// 消息状态类型
export type MessageStatus = 'sending' | 'thinking' | 'generating' | 'completed' | 'stopped' | 'error';

// 附件类型
export interface Attachment {
  id: string;
  name: string;
  type: string;
  size: number;
  url: string;
  thumbnail?: string;
}

// 消息类型
export interface Message {
  id: string;
  role: MessageRole;
  content: string;
  timestamp: number;
  status?: MessageStatus;
  attachments?: Attachment[];
  thinking?: string;
  error?: string;
  feedback?: 'like' | 'dislike';
  commandId?: string;
  commandName?: string;
}

// 会话类型
export interface Conversation {
  id: string;
  title: string;
  messages: Message[];
  createdAt: number;
  updatedAt: number;
  modelId: string;
  favorite?: boolean;
}

// AI模型类型
export interface AIModel {
  id: string;
  name: string;
  logo?: string;
  description?: string;
  icon?: string;
  apiEndpoint?: string;
  systemPrompt?: string;
  parameters?: {
    temperature?: number;
    maxTokens?: number;
    topP?: number;
    frequencyPenalty?: number;
    presencePenalty?: number;
    [key: string]: any;
  };
  isDefault?: boolean;
}

// 命令参数类型
export interface CommandParameter {
  name: string;
  description?: string;
  required?: boolean;
  defaultValue?: string;
  type?: 'string' | 'number' | 'boolean' | 'array';
}

// 命令类型
export interface Command {
  id: string;
  name: string;
  icon?: string;
  description?: string;
  prompt?: string;
  category?: string;
  createdAt: number;
  updatedAt: number;
  isSystem?: boolean;
  parameters?: CommandParameter[];
  shareType?: 'private' | 'shared'; // 命令共享类型
  creator?: string; // 创建者信息，仅共享命令有效
  hasSubCommands?: boolean; // 是否有子命令
  subCommandsEndpoint?: string; // 获取子命令的API端点
  subCommands?: SubCommand[];
}

/**
 * 子命令接口
 */
export interface SubCommand {
  id: string;
  name: string;
  description: string;
  icon: string;
  template: string; // 提示词模板
}

// 知识库类型
export interface KnowledgeBase {
  id: string;
  name: string;
  description: string;
  icon: string;
  apiEndpoint?: string;
  parameters?: {
    relevanceThreshold?: number;
    maxResults?: number;
    [key: string]: any;
  };
}

// 用户设置类型
export interface ChatSettings {
  theme: 'light' | 'dark' | 'auto';
  avatar?: string;
  messageDisplayDensity?: 'compact' | 'comfortable' | 'spacious';
  autoScroll?: boolean;
  notificationEnabled?: boolean;
  keyboardShortcuts?: Record<string, string>;
  language?: string;
}

export interface QuickCommand {
  id: string;
  name: string;
  prompt: string;
} 