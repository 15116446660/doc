import { get, post, put, del } from './request'
import type { Message, Conversation, AIModel, Command, KnowledgeBase } from '@/types/chat'

/**
 * 发送消息并获取AI回复
 * @param messages 消息历史
 * @param modelId 模型ID
 * @param options 其他选项
 */
export function sendMessage(messages: Message[], modelId: string, options?: {
  systemPrompt?: string;
  temperature?: number;
  maxTokens?: number;
  stream?: boolean;
  deepThinking?: boolean;
  knowledgeBaseId?: string;
}) {
  return post<Message>('/api/chat/completions', {
    messages,
    modelId,
    ...options
  })
}

/**
 * 获取流式消息回复
 * @param messages 消息历史
 * @param modelId 模型ID
 * @param options 其他选项
 */
export function streamMessage(messages: Message[], modelId: string, options?: {
  systemPrompt?: string;
  temperature?: number;
  maxTokens?: number;
  deepThinking?: boolean;
  knowledgeBaseId?: string;
  signal?: AbortSignal;
}) {
  return post<ReadableStream>('/api/chat/completions/stream', {
    messages,
    modelId,
    ...options
  }, {
    responseType: 'stream',
    signal: options?.signal
  })
}

/**
 * 获取可用的AI模型列表
 */
export function getModels() {
  return get<AIModel[]>('/api/chat/models')
}

/**
 * 获取命令列表
 */
export function getCommands() {
  return get<Command[]>('/api/commands')
}

/**
 * 获取共享命令列表
 */
export function getSharedCommands() {
  return get<Command[]>('/api/commands/shared')
}

/**
 * 创建命令
 */
export function createCommand(command: Partial<Command>) {
  return post<Command>('/api/commands', command)
}

/**
 * 创建共享命令
 */
export function createSharedCommand(command: Partial<Command>) {
  return post<Command>('/api/commands/shared', command)
}

/**
 * 更新命令
 */
export function updateCommand(commandId: string, updates: Partial<Command>) {
  // 根据命令ID判断是否为共享命令
  if (commandId.startsWith('shared-')) {
    return put<Command>(`/api/commands/shared/${commandId}`, updates)
  }
  return put<Command>(`/api/commands/${commandId}`, updates)
}

/**
 * 删除命令
 */
export function deleteCommand(commandId: string) {
  // 根据命令ID判断是否为共享命令
  if (commandId.startsWith('shared-')) {
    return del<void>(`/api/commands/shared/${commandId}`)
  }
  return del<void>(`/api/commands/${commandId}`)
}

/**
 * 获取知识库列表
 */
export function getKnowledgeBases() {
  return get<KnowledgeBase[]>('/api/chat/knowledge-bases')
}

/**
 * 上传文件附件
 */
export function uploadAttachment(file: File, onProgress?: (percent: number) => void) {
  const formData = new FormData()
  formData.append('file', file)
  
  return post<{ id: string; url: string; thumbnail?: string }>('/api/chat/attachments', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: (progressEvent: any) => {
      if (progressEvent.total && onProgress) {
        const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(percent)
      }
    }
  })
} 