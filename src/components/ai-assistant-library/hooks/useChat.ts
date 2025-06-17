import { ref, reactive, computed, watch } from 'vue'
import { v4 as uuidv4 } from 'uuid'
import { ElMessage } from 'element-plus'
import { streamMessage, uploadAttachment } from '@/api/chat'
import type { Message, MessageRole, MessageStatus, Attachment, AIModel } from '@/types/chat'

/**
 * 聊天功能的核心逻辑封装
 */
export function useChat() {
  // 消息列表状态
  const messages = ref<Message[]>([])
  
  // 当前使用的AI模型
  const currentModelId = ref<string>('gpt-4')
  
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
  function addMessage(message: Message): void {
    messages.value.push(message)
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
    const userMessage: Message = {
      id: uuidv4(),
      role: 'user',
      content,
      timestamp: Date.now(),
      status: 'completed',
      attachments,
      commandId,
      commandName
    }
    
    // 添加用户消息到列表
    addMessage(userMessage)
    
    // 创建AI回复消息（初始为空）
    const aiMessage: Message = {
      id: uuidv4(),
      role: 'assistant',
      content: '',
      timestamp: Date.now(),
      status: 'thinking'
    }
    
    // 添加AI回复消息到列表
    addMessage(aiMessage)
    
    // 设置生成状态
    isGenerating.value = true
    
    // 创建中止控制器
    abortController.value = new AbortController()
    
    try {
      // 准备消息历史
      const messageHistory = messages.value
        .filter(msg => msg.id !== aiMessage.id)
        .map(msg => ({
          id: msg.id,
          role: msg.role,
          content: msg.content,
          timestamp: msg.timestamp
        }))
      
      // 更新AI消息状态为生成中
      updateMessageStatus(aiMessage.id, 'generating')
      
      // 发送请求获取AI回复
      const stream = await streamMessage(messageHistory, currentModelId.value, {
        deepThinking: isDeepThinkingMode.value,
        knowledgeBaseId: isRAGMode.value ? currentKnowledgeBaseId.value || undefined : undefined,
        fullTextReference: isFullTextReferenceMode.value,
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

    } catch (error: any) {
      // 检查是否是用户取消
      if (error.name === 'AbortError') {
        updateMessageStatus(aiMessage.id, 'stopped')
      } else {
        // 其他错误
        updateMessageStatus(aiMessage.id, 'error')
        aiMessage.error = error.message || '获取AI回复失败'
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
  function stopGenerating(): void {
    if (abortController.value) {
      abortController.value.abort()
      abortController.value = null
    }
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
    currentModelId.value = modelId
  }
  
  /**
   * 设置当前使用的知识库
   */
  function setCurrentKnowledgeBase(knowledgeBaseId: string | null): void {
    currentKnowledgeBaseId.value = knowledgeBaseId
    
    // 如果设置了知识库，自动开启RAG模式
    if (knowledgeBaseId) {
      isRAGMode.value = true
    }
  }
  
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
    stopGenerating,
    regenerateMessage,
    clearMessages,
    setMessageFeedback,
    uploadFile,
    toggleDeepThinkingMode,
    toggleRAGMode,
    toggleFullTextReferenceMode,
    setCurrentModel,
    setCurrentKnowledgeBase
  }
} 