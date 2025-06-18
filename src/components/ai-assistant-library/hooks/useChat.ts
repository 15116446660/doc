import { ref, reactive, computed, watch, effectScope, onUnmounted } from 'vue'
import { v4 as uuidv4 } from 'uuid'
import { ElMessage } from 'element-plus'
import { streamMessage, uploadAttachment } from '@/api/chat'
import type { Message, MessageRole, MessageStatus, Attachment, AIModel, ChatConfig, ChatContext, Reference, RAGChatRequest, NormalChatRequest, DocumentAggregation } from '@/types/chat'
import { useAIModels } from './useAIModels'
import {
  streamNormalChat,
  streamRAGChat,
  normalChat,
  ragChat
} from '@/api/chat'

/**
 * 聊天功能的核心逻辑封装
 */
export function useChat(initialConfig: ChatConfig = {
  modelId: '',
  deepthinking: false,
  rag: false
}) {
  const scope = effectScope()
  
  // 消息列表状态
  const messages = ref<Message[]>([])
  
  // 集成 AI 模型管理，确保在同一个作用域内
  const { currentModelId, selectModel: selectAIModel } = scope.run(() => useAIModels()) || { currentModelId: ref(''), selectModel: () => {} }
  
  // 深度思考模式
  const isDeepThinkingMode = ref<boolean>(false)
  
  // RAG模式（检索增强生成）
  const isRAGMode = ref<boolean>(false)
  
  // 全文引用模式
  const isFullTextReferenceMode = ref<boolean>(false)
  
  // 当前使用的知识库ID
  const currentKnowledgeBaseId = ref<string | null>(null)
  
  // 是否正在生成回复
  const isGenerating = ref<boolean>(false)
  
  // 中止控制器，用于取消请求
  const abortController = ref<AbortController | null>(null)
  
  // 计算属性：最后一条消息
  const lastMessage = computed<Message | undefined>(() => {
    if (messages.value.length === 0) return undefined
    return messages.value[messages.value.length - 1]
  })
  
  // 计算属性：是否有消息
  const hasMessages = computed<boolean>(() => messages.value.length > 0)
  
  const config = ref<ChatConfig>(initialConfig)
  const currentReferences = ref<Reference[]>([])
  
  // 计算属性：当前会话ID
  const sessionId = computed(() => {
    return uuidv4();
  });

  // 计算属性：当前聊天ID
  const chatId = computed(() => {
    return uuidv4();
  });
  
  /**
   * 创建新消息对象
   */
  function createMessage(role: MessageRole, content: string, attachments?: Attachment[]): Message {
    return {
      id: uuidv4(),
      role,
      content,
      timestamp: Date.now(),
      status: role === 'user' ? 'completed' : 'generating',
      attachments
    }
  }
  
  /**
   * 添加消息到列表
   */
  function addMessage(message: Partial<Message>): Message {
    const newMessage: Message = {
      id: message.id || uuidv4(),
      role: message.role || 'user',
      content: message.content || '',
      timestamp: message.timestamp || Date.now(),
      status: message.status || 'completed',
      thinking: message.thinking,
      commandId: message.commandId,
      reference: message.reference,
      docAggs: message.docAggs
    };
    messages.value.push(newMessage);
    return newMessage;
  }
  
  /**
   * 更新消息状态
   */
  function updateMessageStatus(messageId: string, status: MessageStatus): void {
    const message = messages.value.find(msg => msg.id === messageId)
    if (message) {
      message.status = status
    }
  }
  
  /**
   * 更新消息内容
   */
  function updateMessageContent(messageId: string, content: string): void {
    const message = messages.value.find(msg => msg.id === messageId)
    if (message) {
      message.content = content
    }
  }
  
  /**
   * 发送用户消息并获取AI回复
   */
  async function sendUserMessage(content: string, attachments?: Attachment[], commandId?: string, commandName?: string): Promise<void> {
    if (!content.trim() && (!attachments || attachments.length === 0)) {
      ElMessage.warning('消息不能为空')
      return
    }
    
    // 创建用户消息
    const userMessage = {
      id: uuidv4(),
      role: 'user' as const,
      content,
      timestamp: Date.now(),
      status: 'completed' as const,
      attachments,
      commandId,
      commandName
    } as Message;
    
    // 添加用户消息到列表
    addMessage(userMessage)
    
    // 创建AI回复消息（初始为空）
    const aiMessage = {
      id: uuidv4(),
      role: 'assistant' as const,
      content: '',
      timestamp: Date.now(),
      status: 'thinking' as MessageStatus
    } as unknown as Message;
    
    // 添加AI回复消息到列表
    addMessage(aiMessage)
    
    // 设置生成状态
    isGenerating.value = true
    
    // 创建中止控制器
    abortController.value = new AbortController()
    
    try {
      // 更新AI消息状态为生成中
      updateMessageStatus(aiMessage.id, 'generating')
      debugger
      if (isRAGMode.value && currentKnowledgeBaseId.value) {
        // 使用RAG模式
        const ragRequest: RAGChatRequest = {
          chatId: uuidv4(),
          sessionId: uuidv4(),
          question: content,
          deepthinking: isDeepThinkingMode.value,
          knowledgeBaseId: currentKnowledgeBaseId.value // 确保传递知识库ID
        };

        await streamRAGChat(
          ragRequest,
          (content: string, id?: string) => {
            aiMessage.content = content;
          },
          (error: Error) => {
            console.error('RAG Chat Error:', error);
            updateMessageStatus(aiMessage.id, 'error');
            (aiMessage as any).error = error.message || '获取AI回复失败';
            ElMessage.error('获取AI回复失败: ' + (error.message || '未知错误'));
          },
          () => {
            updateMessageStatus(aiMessage.id, 'completed');
            isGenerating.value = false;
            abortController.value = null;
          },
          abortController.value
        );
      } else {
        // 使用普通模式
        // 准备消息历史
        const messageHistory = messages.value
          .filter(msg => msg.id !== aiMessage.id)
          .map(msg => ({
            id: msg.id,
            role: msg.role,
            content: msg.content,
            timestamp: msg.timestamp
          }))
        
        // 发送请求获取AI回复
        const stream = await streamMessage(messageHistory, currentModelId.value, {
          deepThinking: isDeepThinkingMode.value,
          signal: abortController.value.signal
        })

        // 处理流式响应
        const reader = stream.getReader();
        const decoder = new TextDecoder();
        let thinkingContent = '';
        let isThinkingPhase = false;

        while (true) {
          const { done, value } = await reader.read();
          if (done) break;

          const chunk = decoder.decode(value, { stream: true });
          
          // 简单的协议解析：检查思考和回答的分隔符
          if (chunk.includes('<thinking>')) {
            isThinkingPhase = true;
          }
          if (chunk.includes('</thinking>')) {
            isThinkingPhase = false;
            continue; // 跳过分隔符本身
          }
          
          if (isThinkingPhase) {
            thinkingContent += chunk.replace('<thinking>', '');
            if(aiMessage.thinking !== thinkingContent) {
              aiMessage.thinking = thinkingContent;
            }
          } else {
            aiMessage.content += chunk;
          }
        }
        
        updateMessageStatus(aiMessage.id, 'completed')
      }
    } catch (error: any) {
      // 检查是否是用户取消
      if (error.name === 'AbortError') {
        updateMessageStatus(aiMessage.id, 'stopped')
      } else {
        // 其他错误
        updateMessageStatus(aiMessage.id, 'error')
        (aiMessage as any).error = error.message || '获取AI回复失败';
        ElMessage.error('获取AI回复失败: ' + (error.message || '未知错误'))
      }
    } finally {
      // 重置状态
      isGenerating.value = false
      abortController.value = null
    }
  }
  
  /**
   * 停止生成回复
   */
  function stopGeneration(): void {
    if (abortController.value) {
      abortController.value.abort()
      abortController.value = null
    }
    if (lastMessage.value?.status === 'pending') {
      updateMessageStatus(lastMessage.value.id, 'completed')
    }
    isGenerating.value = false
  }
  
  /**
   * 重新生成回复
   */
  async function regenerateMessage(aiMessageToRegenerate: Message): Promise<void> {
    if (isGenerating.value) return;

    const messageIndex = messages.value.findIndex(msg => msg.id === aiMessageToRegenerate.id);
    
    // 确保找到了AI消息，并且它不是第一条消息（前面必须有用户消息）
    if (messageIndex < 1) return;

    const userMessageToResend = messages.value[messageIndex - 1];

    // 确保前一条消息是用户消息
    if (userMessageToResend.role !== 'user') return;

    // 从当前消息列表中移除这对用户-AI消息以及之后的所有消息
    messages.value.splice(messageIndex - 1);

    // 重新发送原始用户消息
    await sendUserMessage(
      userMessageToResend.content,
      userMessageToResend.attachments,
      userMessageToResend.commandId,
      userMessageToResend.commandName
    );
  }
  
  /**
   * 清空消息列表
   */
  function clearMessages(): void {
    messages.value = []
    currentReferences.value = []
  }
  
  /**
   * 设置消息反馈（点赞/点踩）
   */
  function setMessageFeedback(messageId: string, feedback: 'like' | 'dislike'): void {
    const message = messages.value.find(msg => msg.id === messageId)
    if (message) {
      message.feedback = feedback
    }
  }
  
  /**
   * 上传附件
   */
  async function uploadFile(file: File): Promise<Attachment> {
    try {
      const response = await uploadAttachment(file)
      return {
        id: response.id,
        name: file.name,
        type: file.type,
        size: file.size,
        url: response.url,
        thumbnail: response.thumbnail
      }
    } catch (error: any) {
      ElMessage.error('上传附件失败: ' + (error.message || '未知错误'))
      throw error
    }
  }
  
  /**
   * 切换深度思考模式
   */
  function toggleDeepThinkingMode(): void {
    isDeepThinkingMode.value = !isDeepThinkingMode.value
  }
  
  /**
   * 切换RAG模式
   */
  function toggleRAGMode(): void {
    isRAGMode.value = !isRAGMode.value
    
    // 同步到配置
    config.value.rag = isRAGMode.value;
    
    // 如果关闭RAG模式，清空知识库ID
    if (!isRAGMode.value) {
      currentKnowledgeBaseId.value = null
    }
  }
  
  /**
   * 切换全文引用模式
   */
  function toggleFullTextReferenceMode(): void {
    isFullTextReferenceMode.value = !isFullTextReferenceMode.value
  }
  
  /**
   * 设置当前使用的AI模型
   */
  function setCurrentModel(modelId: string): void {
    selectAIModel(modelId)
  }
  
  /**
   * 设置当前使用的知识库
   */
  function setCurrentKnowledgeBase(knowledgeBaseId: string | null): void {
    currentKnowledgeBaseId.value = knowledgeBaseId
    
    // 如果设置了知识库，自动开启RAG模式
    if (knowledgeBaseId) {
      isRAGMode.value = true
      config.value.rag = true
    }
  }
  
  // 添加用户消息
  function addUserMessage(content: string): Message {
    const message: Message = {
      id: uuidv4(),
      role: 'user' as MessageRole,
      content,
      timestamp: Date.now()
    };
    messages.value.push(message);
    return message;
  }

  // 添加助手消息
  function addAssistantMessage(content: string, thinking?: string): Message {
    const message: Message = {
      id: uuidv4(),
      role: 'assistant' as MessageRole,
      content,
      timestamp: Date.now(),
      status: 'pending' as MessageStatus,
      thinking
    };
    messages.value.push(message);
    return message;
  }

  // 更新消息内容
  function updateMessage(id: string, content: string) {
    const message = messages.value.find(m => m.id === id);
    if (message) {
      message.content = content;
    }
  }

  // 发送消息
  async function sendMessage(content: string) {
    if (isGenerating.value) {
      return;
    }

    // 添加用户消息
    addUserMessage(content);

    // 创建中止控制器
    abortController.value = new AbortController();
    isGenerating.value = true;

    try {
      if (config.value.rag) {
        // 使用RAG模式
        const assistantMessage = addAssistantMessage('', config.value.deepthinking ? '正在思考...' : undefined);
        
        const ragRequest: RAGChatRequest = {
          chatId: config.value.chatId || uuidv4(),
          sessionId: config.value.sessionId || uuidv4(),
          question: content,
          deepthinking: config.value.deepthinking
        };

        await streamRAGChat(
          ragRequest,
          (content, id) => {
            updateMessage(assistantMessage.id, content);
          },
          (error) => {
            console.error('RAG Chat Error:', error);
            updateMessageStatus(assistantMessage.id, 'error');
            if (assistantMessage) {
              assistantMessage.error = error.message;
            }
          },
          () => {
            updateMessageStatus(assistantMessage.id, 'completed');
            isGenerating.value = false;
          },
          abortController.value
        );
      } else {
        // 使用普通对话模式
        const assistantMessage = addAssistantMessage('', config.value.deepthinking ? '正在思考...' : undefined);
        
        const normalRequest: NormalChatRequest = {
          prompt: content,
          modelId: config.value.modelId,
          deepthinking: config.value.deepthinking
        };

        await streamNormalChat(
          normalRequest,
          (content, id) => {
            updateMessage(assistantMessage.id, content);
          },
          (error) => {
            console.error('Normal Chat Error:', error);
            updateMessageStatus(assistantMessage.id, 'error');
            if (assistantMessage) {
              assistantMessage.error = error.message;
            }
          },
          () => {
            updateMessageStatus(assistantMessage.id, 'completed');
            isGenerating.value = false;
          },
          abortController.value
        );
      }
    } catch (error) {
      console.error('Chat Error:', error);
      if (lastMessage.value?.status === 'pending') {
        updateMessageStatus(lastMessage.value.id, 'error');
        if (lastMessage.value && error instanceof Error) {
          lastMessage.value.error = error.message;
        }
      }
      isGenerating.value = false;
    }
  }

  // 更新配置
  function updateConfig(newConfig: Partial<ChatConfig>) {
    config.value = {
      ...config.value,
      ...newConfig
    };
    
    // 同步RAG模式
    if (newConfig.rag !== undefined) {
      isRAGMode.value = newConfig.rag;
    }
    
    // 同步深度思考模式
    if (newConfig.deepthinking !== undefined) {
      isDeepThinkingMode.value = newConfig.deepthinking;
    }
  }
  
  // 清理副作用
  scope.run(() => {
    // 在这里注册需要自动清理的副作用
  });

  return {
    // 状态
    messages,
    currentModelId,
    isDeepThinkingMode,
    isRAGMode,
    isFullTextReferenceMode,
    currentKnowledgeBaseId,
    isGenerating,
    
    // 计算属性
    lastMessage,
    hasMessages,
    
    // 方法
    sendUserMessage,
    stopGeneration,
    regenerateMessage,
    clearMessages,
    setMessageFeedback,
    uploadFile,
    toggleDeepThinkingMode,
    toggleRAGMode,
    toggleFullTextReferenceMode,
    setCurrentModel,
    setCurrentKnowledgeBase,
    sendMessage,
    updateConfig
  }
} 