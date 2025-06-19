/**
 * AI助手相关类型定义
 */

// 消息角色类型
export type MessageRole = 'user' | 'assistant' | 'system';

// 消息状态类型
export type MessageStatus = 'sending' | 'thinking' | 'generating' | 'completed' | 'stopped' | 'error' | 'editing';

// 附件类型
export interface Attachment {
  id: string;
  name: string;
  type: string;
  size: number;
  url: string;
  thumbnail?: string;
}

// 基础消息类型
export interface Message {
  id: string;
  role: 'user' | 'assistant' | 'system';
  content: string;
  timestamp: number;
  status?: MessageStatus;
  thinking?: string;
  commandId?: string;
  commandName?: string;
  error?: string;
  reference?: Reference;
  docAggs?: DocumentAggregation[];
  attachments?: Attachment[];
  feedback?: 'like' | 'dislike';
  edited?: boolean;
  originalContent?: string;
  subCommands?: SubCommand[]; // 用于展示子命令选项
}

// 知识库引用
export interface Reference {
  total: number;
  chunks: ReferenceChunk[];
}

export interface ReferenceChunk {
  id: string;
  content: string;
  document_id: string;
  document_name: string;
  dataset_id: string;
  image_id?: string;
  positions: number[];
}

// 文档聚合信息
export interface DocumentAggregation {
  doc_name: string;
  doc_id: string;
  count: number;
}

// RAG 对话请求
export interface RAGChatRequest {
  chatId: string;
  sessionId: string;
  question: string;
  deepthinking?: boolean;
  knowledgeBaseId?: string;
}

// RAG 对话响应
export interface RAGChatResponse {
  code: number;
  data: {
    answer: string;
    reference?: Reference;
    doc_aggs?: DocumentAggregation[];
    prompt?: string;
  };
}

// 普通对话请求
export interface NormalChatRequest {
  prompt: string;
  modelId?: string;
  deepthinking?: boolean;
  rag?: boolean;
  attachments?: any;
}

// 普通对话响应
export interface NormalChatResponse {
  code: number;
  data: {
    content: string;
    thinking?: string;
  };
}

// 聊天配置
export interface ChatConfig {
  modelId: string;
  deepthinking: boolean;
  rag: boolean;
  chatId?: string;
  sessionId?: string;
  knowledgeBaseId?: string | null;
  fullTextReference?: boolean;
}

// 聊天上下文
export interface ChatContext {
  config: ChatConfig;
  messages: Message[];
  currentMessage?: Message;
  abortController?: AbortController;
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

// AI模型配置（详细参数）
export interface AIModelConfig {
  modelVersion: string;
  apiKey: string;
  apiEndpoint?: string;
  temperature?: number;
  maxTokens?: number;
  topP?: number;
  topK?: number;
  frequencyPenalty?: number;
  presencePenalty?: number;
  stream?: boolean;
  // Azure特有
  apiVersion?: string;
  // Mistral特有
  randomSeed?: number;
}

// AI模型选项
export interface ModelOption {
  id: string;
  name: string;
  description?: string;
  maxTokens?: number;
  contextWindow?: number;
}

// AI服务提供商配置
export interface Provider {
  name: string;
  logo: string;
  description: string;
  configFields: string[];
  
  // 参数范围
  ranges?: {
    temperature: [number, number];
    maxTokens: [number, number];
    topP?: [number, number];
    topK?: [number, number];
  };
  
  // 基础参数配置
  basicParams?: {
    modelName: boolean;
  };
  
  // 高级参数配置
  advancedParams?: {
    topP: boolean;
    topK: boolean;
    frequencyPenalty: boolean;
    presencePenalty: boolean;
    stream: boolean;
  };
  
  // 特殊参数配置
  specialParams?: {
    randomSeed?: boolean;
  };
  
  // 模型选项列表
  modelOptions?: ModelOption[];
  
  // 默认配置
  defaults?: {
    baseUrl: string;
    [key: string]: any;
  };
}

// 用于模型定义的接口
export interface AIModelDefinition {
  id: string;
  label: string;
  provider: string;
  type: string;
  logo: string;
  description: string;
  level: string;
  defaults: {
    maxTokens: number;
    temperature: number;
    modelVersion: string;
  }
}

// AI模型定义 - 根据新API重构
export interface AIModel {
  id: string;
  name: string; // 对应 modelName
  model?: string; // 模型标识
  apiKey: string;
  apiUrl?: string;
  isDefault: boolean;
  cueWord?: string; // 提示词
  description?: string;
  logo?: string;
  // 新增字段
  provider?: string;
  level?: string;
  modelVersion?: string;
  // 高级设置
  temperature?: number;
  maxTokens?: number;
  topP?: number;
  frequencyPenalty?: number;
  presencePenalty?: number;
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