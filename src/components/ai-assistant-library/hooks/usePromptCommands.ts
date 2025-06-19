import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { v4 as uuidv4 } from 'uuid'
import { getCommands, getSharedCommands, createCommand, createSharedCommand, updateCommand, deleteCommand } from '@/api/chat'
import type { Command, CommandParameter } from '@/types/chat'

/**
 * 命令系统的核心逻辑封装
 */
export function usePromptCommands() {
  // 命令列表
  const commands = ref<Command[]>([])
  
  // 是否正在加载
  const loading = ref<boolean>(false)
  
  // 计算属性：按分类分组的命令
  const commandsByCategory = computed(() => {
    const result: Record<string, Command[]> = {}
    
    commands.value.forEach(cmd => {
      const category = cmd.category || '未分类'
      if (!result[category]) {
        result[category] = []
      }
      result[category].push(cmd)
    })
    
    return result
  })
  
  // 计算属性：所有分类列表
  const categories = computed(() => {
    return Object.keys(commandsByCategory.value)
  })
  
  // 计算属性：私有命令
  const privateCommands = computed(() => {
    return commands.value.filter(cmd => cmd.shareType === 'private' || !cmd.shareType)
  })
  
  // 计算属性：共享命令
  const sharedCommands = computed(() => {
    return commands.value.filter(cmd => cmd.shareType === 'shared')
  })
  
  /**
   * 加载命令列表
   */
  async function loadCommands(): Promise<void> {
    if (loading.value) return
    
    loading.value = true
    
    try {
      // 加载私有命令
      const privateResponse = await getCommands()
      
      // 加载共享命令
      const sharedResponse = await getSharedCommands()
      
      // 合并命令列表，确保系统命令在前面
      const systemCommands = privateResponse.filter(cmd => cmd.isSystem)
      const nonSystemPrivateCommands = privateResponse.filter(cmd => !cmd.isSystem)
      
      commands.value = [...systemCommands, ...nonSystemPrivateCommands, ...sharedResponse]
      
      // 合并本地自定义命令
      const localCommands = loadLocalCommands()
      if (localCommands.length > 0) {
        // 过滤掉已存在的命令
        const newCommands = localCommands.filter(localCmd => 
          !commands.value.some(cmd => cmd.id === localCmd.id)
        )
        
        commands.value = [...commands.value, ...newCommands]
      }
    } catch (error: any) {
      console.error('加载命令列表失败:', error)
      ElMessage.error('加载命令列表失败: ' + (error.message || '未知错误'))
      
      // 如果API加载失败，尝试从本地加载
      const localCommands = loadLocalCommands()
      if (localCommands.length > 0) {
        commands.value = localCommands
      }
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 从本地存储加载自定义命令
   */
  function loadLocalCommands(): Command[] {
    try {
      const savedCommands = localStorage.getItem('customCommands')
      if (savedCommands) {
        return JSON.parse(savedCommands)
      }
    } catch (error) {
      console.error('解析本地命令数据失败:', error)
    }
    
    return []
  }
  
  /**
   * 保存自定义命令到本地存储
   */
  function saveLocalCommands(): void {
    // 只保存非系统命令和私有命令
    const customCommands = commands.value.filter(cmd => !cmd.isSystem && cmd.shareType === 'private')
    localStorage.setItem('customCommands', JSON.stringify(customCommands))
  }
  
  /**
   * 添加新命令
   */
  async function addCommand(commandData: Omit<Command, 'id' | 'createdAt' | 'updatedAt'>): Promise<string> {
    try {
      // 根据共享类型选择不同的API
      let response: Command
      
      if (commandData.shareType === 'shared') {
        // 创建共享命令
        response = await createSharedCommand(commandData)
      } else {
        // 创建私有命令
        response = await createCommand(commandData)
      }
      
      // 添加到本地列表
      commands.value.push(response)
      
      // 如果是私有命令，保存到本地存储
      if (commandData.shareType === 'private') {
        saveLocalCommands()
      }
      
      return response.id
    } catch (error) {
      console.error('创建命令失败:', error)
      
      // 如果API失败且是私有命令，创建本地命令
      if (commandData.shareType === 'private') {
        const newCommand: Command = {
          ...commandData,
          id: `local-${uuidv4().substring(0, 8)}`,
          createdAt: Date.now(),
          updatedAt: Date.now(),
          isSystem: false,
          shareType: 'private'
        }
        
        commands.value.push(newCommand)
        saveLocalCommands()
        
        return newCommand.id
      } else {
        ElMessage.error('创建共享命令失败，请检查网络连接')
        throw error
      }
    }
  }
  
  /**
   * 更新命令
   */
  async function updateCommandById(commandId: string, updates: Partial<Omit<Command, 'id' | 'createdAt' | 'updatedAt'>>): Promise<boolean> {
    const index = commands.value.findIndex(cmd => cmd.id === commandId)
    if (index === -1) return false
    
    const command = commands.value[index]
    
    try {
      // 如果是系统命令或远程命令，尝试通过API更新
      if (command.isSystem || !commandId.startsWith('local-')) {
        await updateCommand(commandId, updates)
      }
      
      // 更新本地列表
      commands.value[index] = {
        ...command,
        ...updates,
        updatedAt: Date.now()
      }
      
      // 如果是私有命令，保存到本地存储
      if (commands.value[index].shareType === 'private') {
        saveLocalCommands()
      }
      
      return true
    } catch (error) {
      console.error('更新命令失败:', error)
      ElMessage.error('更新命令失败')
      return false
    }
  }
  
  /**
   * 删除命令
   */
  async function removeCommand(commandId: string): Promise<boolean> {
    const index = commands.value.findIndex(cmd => cmd.id === commandId)
    if (index === -1) return false
    
    const command = commands.value[index]
    
    // 不允许删除系统命令
    if (command.isSystem) {
      ElMessage.warning('系统命令不能删除')
      return false
    }
    
    try {
      // 如果是远程命令，尝试通过API删除
      if (!commandId.startsWith('local-')) {
        await deleteCommand(commandId)
      }
      
      // 从本地列表中删除
      commands.value.splice(index, 1)
      
      // 保存到本地存储
      saveLocalCommands()
      
      return true
    } catch (error) {
      console.error('删除命令失败:', error)
      ElMessage.error('删除命令失败')
      return false
    }
  }
  
  /**
   * 查找命令
   */
  function findCommand(commandId: string): Command | undefined {
    return commands.value.find(cmd => cmd.id === commandId)
  }
  
  /**
   * 搜索命令
   */
  function searchCommands(keyword: string): Command[] {
    if (!keyword.trim()) return commands.value
    
    const lowerKeyword = keyword.toLowerCase()
    return commands.value.filter(cmd => {
      return (
        cmd.name.toLowerCase().includes(lowerKeyword) ||
        cmd.description?.toLowerCase().includes(lowerKeyword) ||
        (cmd.prompt || '').toLowerCase().includes(lowerKeyword) ||
        cmd.category?.toLowerCase().includes(lowerKeyword)
      )
    })
  }
  
  /**
   * 执行命令
   */
  function executeCommand(commandId: string, input: string = ''): string {
    const command = findCommand(commandId)
    if (!command) {
      throw new Error('命令不存在')
    }
    
    // 替换命令模板中的{input}占位符
    let processedPrompt = (command.prompt || '').replace(/\{input\}/g, input)
    
    return processedPrompt
  }
  
  /**
   * 获取命令参数
   */
  function getCommandParameters(commandId: string): CommandParameter[] {
    const command = findCommand(commandId)
    return command?.parameters || []
  }
  
  // 监听命令列表变化，自动保存自定义命令
  watch(commands, () => {
    saveLocalCommands()
  }, { deep: true })
  
  // 组件挂载时加载数据
  onMounted(() => {
    loadCommands()
  })
  
  return {
    // 状态
    commands,
    loading,
    
    // 计算属性
    commandsByCategory,
    categories,
    privateCommands,
    sharedCommands,
    
    // 方法
    loadCommands,
    addCommand,
    updateCommandById,
    removeCommand,
    findCommand,
    searchCommands,
    executeCommand,
    getCommandParameters
  }
} 