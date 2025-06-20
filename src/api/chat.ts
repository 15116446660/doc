import { get, post, put, del } from './request'
import type { Message, Conversation, AIModel, Command, KnowledgeBase, RAGChatRequest, RAGChatResponse, NormalChatRequest, NormalChatResponse, Reference, SubCommand } from '@/types/chat'

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
    .then(models => {
      // 如果服务端返回了模型列表，则使用服务端返回的模型列表
      if (models && models.length > 0) {
        return models;
      }
      
      // 服务端未返回模型列表，使用默认预设模型列表
      return getDefaultModels();
    })
    .catch(() => {
      // 请求出错时，使用默认预设模型列表
      return getDefaultModels();
    });
}

/**
 * 获取默认预设模型列表，在不加载服务端大模型的情况下使用
 */
function getDefaultModels(): AIModel[] {
  return [
    {
      id: 'gpt-4',
      name: 'GPT-4',
      description: '最强大的AI模型，适合复杂任务',
      provider: 'openai',
      logo: 'openai',
      level: 'super',
      isDefault: true,
      apiKey: '',
      maxTokens: 4096,
      temperature: 0.7,
      modelVersion: 'gpt-4'
    },
    {
      id: 'qwen-2.5',
      name: '通义千问 2.5',
      description: '阿里云最新一代基础大模型，性能全面升级',
      provider: 'alibaba',
      logo: 'alibaba',
      level: 'super',
      apiKey: '',
      maxTokens: 10240,
      temperature: 0.7,
      modelVersion: 'qwen-2.5',
      isDefault: false
    },
    {
      id: 'deepseek-v3',
      name: 'DeepSeek V3',
      description: '最新一代通用大模型，能力全面增强',
      provider: 'deepseek',
      logo: 'deepseek',
      level: 'super',
      apiKey: '',
      maxTokens: 16384,
      temperature: 0.7,
      modelVersion: 'deepseek-v3',
      isDefault: false
    },
    {
      id: 'gpt-3.5-turbo',
      name: 'GPT-3.5 Turbo',
      description: '快速高效的AI模型，适合一般任务',
      provider: 'openai',
      logo: 'openai',
      level: 'basic',
      apiKey: '',
      maxTokens: 4096,
      temperature: 0.7,
      modelVersion: 'gpt-3.5-turbo',
      isDefault: false
    }
  ];
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

// RAG 流式对话
export async function streamRAGChat(
  request: RAGChatRequest,
  onMessage: (content: string, id?: string) => void,
  onError: (error: Error) => void,
  onComplete: () => void,
  abortController?: AbortController
) {
  try {
    const Authorization = localStorage.getItem('token') || '';
    const response = await fetch('/api/document-ai/rag/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization
      },
      body: JSON.stringify(request),
      signal: abortController?.signal
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const reader = response.body?.getReader();
    if (!reader) {
      throw new Error('Response body is null');
    }

    const decoder = new TextDecoder();
    let buffer = '';
    let completeContent = '';

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      // 解码并添加到缓冲区
      buffer += decoder.decode(value, { stream: true });
      
      // 处理事件流格式
      const lines = buffer.split('\n');
      buffer = lines.pop() || ''; // 保留最后一个可能不完整的行
      
      for (const line of lines) {
        if (line.trim() === '') continue;
        
        if (line.startsWith('data:')) {
          const data = line.slice(5).trim();
          
          // 检查是否是结束标记
          if (data === '[\"DONE\"]') {
            console.log('Stream complete');
            break;
          }
          
          try {
            // 解析事件数据
            const parsedData = JSON.parse(data);
            if (parsedData) {
              completeContent += parsedData;
              onMessage(completeContent);
            }
          } catch (e) {
            console.warn('Failed to parse event data:', data, e);
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

// 普通流式对话
export async function streamNormalChat(
  request: NormalChatRequest,
  onMessage: (content: string, id?: string) => void,
  onError: (error: Error) => void,
  onComplete: () => void,
  abortController?: AbortController,
  onThinking?: (content: string) => void
) {
  try {
    // 创建FormData（普通对话使用form-data格式）
    const formData = new FormData();
    // 添加基本请求字段
    formData.append('prompt', request.prompt);
    if (request.modelId) formData.append('modelId', request.modelId);
    if (request.deepthinking !== undefined) formData.append('deepthinking', String(request.deepthinking));
    if (request.rag !== undefined) formData.append('rag', String(request.rag));
    
    // 处理附件（最多3个，限制类型为docx、pdf和txt）
    if (request.attachments && Array.isArray(request.attachments)) {
      const allowedTypes = ['application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'application/pdf', 'text/plain'];
      const validAttachments = request.attachments
        .filter(att => allowedTypes.includes(att.type))
        .slice(0, 3); // 最多3个附件
      
      validAttachments.forEach((att, index) => {
        if (att.file) {
          formData.append(`attachment${index + 1}`, att.file);
        }
      });
    }
    
    const Authorization = localStorage.getItem('token') || '';
    const response = await fetch('/api/document-ai/ai/poststreamPolish', {
      method: 'POST',
      headers: {
        Authorization
      },
      body: formData,
      signal: abortController?.signal
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const reader = response.body?.getReader();
    if (!reader) {
      throw new Error('Response body is null');
    }

    const decoder = new TextDecoder();
    let buffer = '';
    let completeContent = '';
    let thinkingContent = '';
    let isThinking = false;

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      // 解码并添加到缓冲区
      buffer += decoder.decode(value, { stream: true });
      
      // 处理事件流格式
      const lines = buffer.split('\n');
      buffer = lines.pop() || ''; // 保留最后一个可能不完整的行
      
      for (const line of lines) {
        if (line.trim() === '') continue;
        
        if (line.startsWith('data:')) {
          const data = line.slice(5).trim();
          
          // 检查是否是结束标记
          if (data === '[\"DONE\"]') {
            console.log('Stream complete');
            break;
          }
          
          try {
            let content = data;
            
            // 检查是否包含思考标记
            if (data.includes('<think>')) {
              isThinking = true;
              content = data.replace('<think>', '');
              thinkingContent += content;
              
              // 调用思考内容回调
              if (onThinking) {
                onThinking(thinkingContent);
              }
              continue;
            }
            
            if (data.includes('</think>')) {
              isThinking = false;
              continue;
            }
            
            if (isThinking) {
              thinkingContent += data;
              // 调用思考内容回调
              if (onThinking) {
                onThinking(thinkingContent);
              }
              continue;
            }
            
            // 非思考内容，添加到正常回复
            completeContent += content;
            onMessage(completeContent);
          } catch (e) {
            console.warn('Failed to parse event data:', data, e);
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

// RAG 非流式对话
export async function ragChat(request: RAGChatRequest): Promise<RAGChatResponse> {
  console.warn('非流式RAG对话已弃用，请使用流式对话函数streamRAGChat');
  return {
    code: 200,
    data: {
      answer: '该接口已弃用，请使用流式对话API(/api/document-ai/rag/chat)',
      reference: undefined,
      doc_aggs: undefined
    }
  };
}

// 普通非流式对话
export async function normalChat(request: NormalChatRequest): Promise<NormalChatResponse> {
  console.warn('非流式普通对话已弃用，请使用流式对话函数streamNormalChat');
  return {
    code: 200,
    data: {
      content: '该接口已弃用，请使用流式对话API(/api/document-ai/ai/poststreamPolish)'
    }
  };
}

/**
 * 获取命令的子命令列表
 * @param parentCommandId 父命令ID
 * @param context 上下文参数，如文档ID、项目ID等
 */
export function getSubCommands(parentCommandId: string, context?: Record<string, any>) {
  // 特殊处理document命令，使用硬编码的URL
  if (parentCommandId === 'document') {
    return post<SubCommand[]>(`/api/commands/document/sub-commands`, context || {})
  }
  
  // 其他命令使用通用URL模式
  return post<SubCommand[]>(`/api/commands/${parentCommandId}/sub-commands`, context || {})
}

/**
 * 创建子命令
 * @param parentCommandId 父命令ID
 * @param subCommand 子命令数据
 */
export function createSubCommand(parentCommandId: string, subCommand: Partial<SubCommand>) {
  return post<SubCommand>(`/api/commands/${parentCommandId}/sub-commands`, subCommand)
}

/**
 * 更新子命令
 * @param parentCommandId 父命令ID
 * @param subCommandId 子命令ID
 * @param updates 子命令更新数据
 */
export function updateSubCommand(parentCommandId: string, subCommandId: string, updates: Partial<SubCommand>) {
  return put<SubCommand>(`/api/commands/${parentCommandId}/sub-commands/${subCommandId}`, updates)
}

/**
 * 删除子命令
 * @param parentCommandId 父命令ID
 * @param subCommandId 子命令ID
 */
export function deleteSubCommand(parentCommandId: string, subCommandId: string) {
  return del<void>(`/api/commands/${parentCommandId}/sub-commands/${subCommandId}`)
} 