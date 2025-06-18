import { get, post, put, del } from './request'
import type { Message, Conversation, AIModel, Command, KnowledgeBase, RAGChatRequest, RAGChatResponse, NormalChatRequest, NormalChatResponse, Reference } from '@/types/chat'

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
export async function streamMessage(messages: Message[], modelId: string, options?: {
  systemPrompt?: string;
  temperature?: number;
  maxTokens?: number;
  deepThinking?: boolean;
  knowledgeBaseId?: string;
  signal?: AbortSignal;
}): Promise<ReadableStream<Uint8Array>> {
  const response = await fetch('/api/chat/completions/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      messages,
      modelId,
      ...options,
    }),
    signal: options?.signal,
  });

  if (!response.ok) {
    const errorData = await response.json();
    throw new Error(errorData.msg || '流式请求失败');
  }

  if (!response.body) {
    throw new Error('响应体为空');
  }

  return response.body;
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

const BASE_URL = '/api/chat';

// 创建统一的请求头
const createHeaders = (signal?: AbortSignal) => {
  const headers = new Headers({
    'Content-Type': 'application/json',
  });
  const token = localStorage.getItem('token');
  if (token) {
    headers.append('Authorization', token);
  }
  return headers;
};

// 流式对话基础函数
async function streamChat<T>(
  url: string,
  data: T,
  onMessage: (content: string, id?: string) => void,
  onError: (error: Error) => void,
  onComplete: () => void,
  abortController?: AbortController
) {
  try {
    const Authorization = localStorage.getItem('token') || '';
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization
      },
      body: JSON.stringify(data),
      signal: abortController?.signal
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const reader = response.body?.getReader();
    if (!reader) {
      throw new Error('Response body is null');
    }

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      const text = new TextDecoder().decode(value);
      const lines = text.split('\n');
      
      for (const line of lines) {
        if (line.trim()) {
          try {
            const data = JSON.parse(line);
            onMessage(data.content, data.id);
          } catch (e) {
            console.warn('Failed to parse line:', line);
          }
        }
      }
    }

    onComplete();
  } catch (error: unknown) {
    if (error instanceof Error && error.name === 'AbortError') {
      console.log('Fetch aborted');
      return;
    }
    onError(error instanceof Error ? error : new Error('Unknown error occurred'));
  }
}

// RAG 流式对话
export async function streamRAGChat(
  request: RAGChatRequest,
  onMessage: (content: string, id?: string) => void,
  onError: (error: Error) => void,
  onComplete: () => void,
  abortController?: AbortController
) {
  return streamChat(
    '/api/document-ai/ai/rag/streamChat',
    request,
    onMessage,
    onError,
    onComplete,
    abortController
  );
}

// 普通流式对话
export async function streamNormalChat(
  request: NormalChatRequest,
  onMessage: (content: string, id?: string) => void,
  onError: (error: Error) => void,
  onComplete: () => void,
  abortController?: AbortController
) {
  return streamChat(
    '/api/document-ai/ai/poststreamPolish',
    request,
    onMessage,
    onError,
    onComplete,
    abortController
  );
}

// RAG 非流式对话
export async function ragChat(request: RAGChatRequest): Promise<RAGChatResponse> {
  const Authorization = localStorage.getItem('token') || '';
  const response = await fetch('/api/document-ai/ai/rag/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization
    },
    body: JSON.stringify(request)
  });

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }

  return response.json();
}

// 普通非流式对话
export async function normalChat(request: NormalChatRequest): Promise<NormalChatResponse> {
  const Authorization = localStorage.getItem('token') || '';
  const response = await fetch('/api/document-ai/ai/postPolish', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization
    },
    body: JSON.stringify(request)
  });

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }

  return response.json();
} 