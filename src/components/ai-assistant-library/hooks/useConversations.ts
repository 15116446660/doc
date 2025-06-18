import { ref, computed, watch } from 'vue'
import { v4 as uuidv4 } from 'uuid'
import { ElMessage } from 'element-plus'
import type { Conversation, Message } from '@/types/chat'

/**
 * 会话管理的核心逻辑封装
 */
export function useConversations() {
  // 所有会话列表
  const conversations = ref<Conversation[]>([])
  
  // 当前活跃会话ID
  const activeConversationId = ref<string | null>(null)
  
  // 计算属性：当前活跃会话
  const activeConversation = computed<Conversation | undefined>(() => {
    if (!activeConversationId.value) return undefined
    return conversations.value.find(conv => conv.id === activeConversationId.value)
  })
  
  // 计算属性：当前会话的消息列表
  const activeMessages = computed<Message[]>(() => {
    return activeConversation.value?.messages || []
  })
  
  // 初始化：从IndexedDB加载会话数据
  async function init(): Promise<void> {
    try {
      // 这里应该实现从IndexedDB加载数据的逻辑
      // 暂时使用模拟数据
      const storedConversations = localStorage.getItem('conversations')
      if (storedConversations) {
        conversations.value = JSON.parse(storedConversations)
      }
      
      // 如果有会话，默认选择第一个
      if (conversations.value.length > 0) {
        activeConversationId.value = conversations.value[0].id
      }
    } catch (error) {
      console.error('加载会话数据失败:', error)
    }
  }
  
  // 保存会话数据到IndexedDB
  async function saveConversations(): Promise<void> {
    try {
      // 这里应该实现保存到IndexedDB的逻辑
      // 暂时使用localStorage
      localStorage.setItem('conversations', JSON.stringify(conversations.value))
    } catch (error) {
      console.error('保存会话数据失败:', error)
    }
  }
  
  // 监听会话变化，自动保存
  watch(conversations, () => {
    saveConversations()
  }, { deep: true })
  
  /**
   * 清空所有非收藏的历史会话
   */
  function clearNonFavoriteConversations(): void {
    // 过滤出所有已收藏的会话
    const favoritedConversations = conversations.value.filter(conv => conv.favorite === true);
    
    // 替换当前的会话列表
    conversations.value = favoritedConversations;
    
    // 如果删除后没有会话，则当前活跃会话ID设为null
    if (conversations.value.length === 0) {
      activeConversationId.value = null;
    } 
    // 如果当前活跃会话不在保留的列表中，则切换到第一个会话
    else if (!conversations.value.find(conv => conv.id === activeConversationId.value)) {
      activeConversationId.value = conversations.value[0].id;
    }
  }
  
  /**
   * 创建新会话
   */
  function createConversation(modelId: string = 'gpt-4'): string {
    // 检查最新会话是否为空，如果为空则不允许创建新会话
    if (conversations.value.length > 0) {
      const latestConversation = conversations.value[0]; // 假设列表是按时间倒序排列的
      if (latestConversation.messages.length === 0) {
        // 如果最新会话为空，直接返回该会话ID而不创建新会话
        activeConversationId.value = latestConversation.id;
        return latestConversation.id;
      }
    }

    const newConversation: Conversation = {
      id: uuidv4(),
      title: `新会话 ${new Date().toLocaleString()}`,
      messages: [],
      createdAt: Date.now(),
      updatedAt: Date.now(),
      modelId
    }
    
    conversations.value.unshift(newConversation)
    activeConversationId.value = newConversation.id
    return newConversation.id
  }
  
  /**
   * 切换到指定会话
   */
  function switchConversation(conversationId: string): void {
    const conversation = conversations.value.find(conv => conv.id === conversationId)
    if (!conversation) {
      ElMessage.error('会话不存在')
      return
    }
    
    activeConversationId.value = conversationId
  }
  
  /**
   * 更新会话标题
   */
  function updateConversationTitle(conversationId: string, title: string): void {
    const conversation = conversations.value.find(conv => conv.id === conversationId)
    if (conversation) {
      conversation.title = title
      conversation.updatedAt = Date.now()
    }
  }
  
  /**
   * 自动生成会话标题（基于第一条用户消息）
   */
  function generateConversationTitle(conversationId: string): void {
    const conversation = conversations.value.find(conv => conv.id === conversationId)
    if (!conversation || conversation.messages.length === 0) return
    
    // 查找第一条用户消息
    const firstUserMessage = conversation.messages.find(msg => msg.role === 'user')
    if (!firstUserMessage) return
    
    // 提取标题（最多20个字符）
    const content = firstUserMessage.content.trim()
    const title = content.length > 20 ? content.substring(0, 20) + '...' : content
    
    // 更新标题
    conversation.title = title
    conversation.updatedAt = Date.now()
  }
  
  /**
   * 删除会话
   */
  function deleteConversation(conversationId: string): void {
    const index = conversations.value.findIndex(conv => conv.id === conversationId)
    if (index === -1) return
    
    // 删除会话
    conversations.value.splice(index, 1)
    
    // 如果删除的是当前活跃会话，切换到其他会话
    if (activeConversationId.value === conversationId) {
      if (conversations.value.length > 0) {
        activeConversationId.value = conversations.value[0].id
      } else {
        activeConversationId.value = null
      }
    }
  }
  
  /**
 * 收藏/取消收藏会话
 */
function toggleFavorite(conversationId: string): void {
  const conversation = conversations.value.find(conv => conv.id === conversationId)
  if (conversation) {
    // 确保favorite字段存在，不存在则默认为false
    if (conversation.favorite === undefined) {
      conversation.favorite = false;
    }
    
    // 切换收藏状态
    conversation.favorite = !conversation.favorite;
    conversation.updatedAt = Date.now();
    
    // 立即保存更改到存储
    saveConversations();
    
    console.log(`会话 ${conversationId} 收藏状态已更新为: ${conversation.favorite}`);
  }
}
  
  /**
   * 更新会话中的消息列表
   */
  function updateConversationMessages(conversationId: string, messages: Message[]): void {
    const conversation = conversations.value.find(conv => conv.id === conversationId)
    if (conversation) {
      conversation.messages = [...messages]
      conversation.updatedAt = Date.now()
      
      // 如果是新会话且有消息，自动生成标题
      if (conversation.messages.length === 2 && conversation.title.startsWith('新会话')) {
        generateConversationTitle(conversationId)
      }
    }
  }
  
  /**
   * 搜索会话
   */
  function searchConversations(keyword: string): Conversation[] {
    if (!keyword.trim()) return conversations.value
    
    const lowerKeyword = keyword.toLowerCase()
    return conversations.value.filter(conv => {
      // 搜索标题
      if (conv.title.toLowerCase().includes(lowerKeyword)) return true
      
      // 搜索消息内容
      return conv.messages.some(msg => 
        msg.content.toLowerCase().includes(lowerKeyword)
      )
    })
  }
  
  /**
   * 导出会话数据
   */
  function exportConversation(conversationId: string): string {
    const conversation = conversations.value.find(conv => conv.id === conversationId)
    if (!conversation) return ''
    
    return JSON.stringify(conversation, null, 2)
  }
  
  /**
   * 导入会话数据
   */
  function importConversation(data: string): boolean {
    try {
      const conversation = JSON.parse(data) as Conversation
      
      // 验证数据格式
      if (!conversation.id || !conversation.title || !Array.isArray(conversation.messages)) {
        throw new Error('无效的会话数据格式')
      }
      
      // 检查是否已存在相同ID的会话
      const existingIndex = conversations.value.findIndex(conv => conv.id === conversation.id)
      if (existingIndex >= 0) {
        // 替换现有会话
        conversations.value[existingIndex] = conversation
      } else {
        // 添加新会话
        conversations.value.unshift(conversation)
      }
      
      return true
    } catch (error) {
      console.error('导入会话数据失败:', error)
      return false
    }
  }
  
  /**
   * 加载指定会话
   */
  function loadConversation(conversationId: string): void {
    const conversation = conversations.value.find(conv => conv.id === conversationId)
    if (!conversation) {
      ElMessage.error('会话不存在')
      return
    }
    activeConversationId.value = conversationId
  }
  
  /**
   * 保存会话
   */
  function saveConversation(conversationId: string, messages: Message[], modelId: string): void {
    const conversation = conversations.value.find(conv => conv.id === conversationId)
    if (conversation) {
      conversation.messages = [...messages]
      conversation.modelId = modelId
      conversation.updatedAt = Date.now()
    }
  }
  
  /**
   * 重命名会话
   */
  function renameConversation(conversationId: string, newTitle: string): void {
    const conversation = conversations.value.find(conv => conv.id === conversationId)
    if (conversation) {
      conversation.title = newTitle
      conversation.updatedAt = Date.now()
    }
  }
  
  // 初始化
  init()
  
  return {
    // 状态
    conversations,
    activeConversationId,
    activeConversation,
    activeMessages,
    
    // 方法
    init,
    createConversation,
    switchConversation,
    updateConversationTitle,
    generateConversationTitle,
    deleteConversation,
    toggleFavorite,
    updateConversationMessages,
    searchConversations,
    exportConversation,
    importConversation,
    loadConversation,
    saveConversation,
    renameConversation,
    clearNonFavoriteConversations
  }
} 