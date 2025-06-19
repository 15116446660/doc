import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { v4 as uuidv4 } from 'uuid'
import { 
  getCommands, 
  getSharedCommands, 
  createCommand, 
  createSharedCommand, 
  updateCommand, 
  deleteCommand,
  getSubCommands,
  createSubCommand,
  updateSubCommand,
  deleteSubCommand
} from '@/api/chat'
import type { Command, CommandParameter, SubCommand } from '@/types/chat'
import { presetCommands, getPresetSubCommands } from '@/config/presetCommands'

/**
 * 命令系统的核心逻辑封装
 */
export function usePromptCommands() {
  // 命令列表
  const commands = ref<Command[]>([])
  
  // 子命令缓存
  const subCommandsCache = ref<Record<string, SubCommand[]>>({})
  
  // 是否正在加载
  const loading = ref<boolean>(false)
  const loadingSubCommands = ref<boolean>(false)
  
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
      // 首先加载预设命令
      commands.value = [...presetCommands]
      
      try {
        // 尝试加载私有命令
        const privateResponse = await getCommands()
        
        // 尝试加载共享命令
        const sharedResponse = await getSharedCommands()
        
        // 合并命令列表，避免与预设命令重复
        const apiCommands = [...privateResponse, ...sharedResponse]
        
        // 过滤掉与预设命令ID重复的命令
        const filteredApiCommands = apiCommands.filter(apiCmd => 
          !commands.value.some(cmd => cmd.id === apiCmd.id)
        )
        
        commands.value = [...commands.value, ...filteredApiCommands]
      } catch (error) {
        console.warn('从API加载命令失败，使用本地预设命令', error)
      }
      
      // 合并本地自定义命令
      const localCommands = loadLocalCommands()
      if (localCommands.length > 0) {
        // 过滤掉已存在的命令
        const newCommands = localCommands.filter(localCmd => 
          !commands.value.some(cmd => cmd.id === localCmd.id)
        )
        
        commands.value = [...commands.value, ...newCommands]
      }
      
      // 预加载预设命令的子命令到缓存
      presetCommands.forEach(cmd => {
        if (cmd.hasSubCommands && cmd.subCommands) {
          subCommandsCache.value[cmd.id] = cmd.subCommands
        }
      })
    } catch (error: any) {
      console.error('加载命令列表失败:', error)
      ElMessage.error('加载命令列表失败: ' + (error.message || '未知错误'))
      
      // 如果API加载失败，确保至少有预设命令可用
      commands.value = [...presetCommands]
      
      // 尝试从本地加载自定义命令
      const localCommands = loadLocalCommands()
      if (localCommands.length > 0) {
        const filteredLocalCommands = localCommands.filter(localCmd => 
          !commands.value.some(cmd => cmd.id === localCmd.id)
        )
        commands.value = [...commands.value, ...filteredLocalCommands]
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
    return commands.value.filter(command => {
      return (
        command.name.toLowerCase().includes(lowerKeyword) ||
        (command.description || '').toLowerCase().includes(lowerKeyword) ||
        (command.category || '').toLowerCase().includes(lowerKeyword)
      )
    })
  }
  
  /**
   * 执行命令
   */
  function executeCommand(commandId: string, input: string = ''): string {
    const command = findCommand(commandId)
    if (!command || !command.prompt) {
      return input
    }
    
    // 替换命令中的{input}占位符
    return command.prompt.replace(/{input}/g, input)
  }
  
  /**
   * 获取命令的参数列表
   */
  function getCommandParameters(commandId: string): CommandParameter[] {
    const command = findCommand(commandId)
    return command?.parameters || []
  }
  
  /**
   * 获取命令的子命令列表
   */
  async function fetchSubCommands(commandId: string, context?: Record<string, any>): Promise<SubCommand[]> {
    // 首先检查预设命令的子命令
    const presetSubCommands = getPresetSubCommands(commandId)
    if (presetSubCommands.length > 0) {
      // 更新缓存并返回预设子命令
      subCommandsCache.value[commandId] = presetSubCommands
      return presetSubCommands
    }
    
    // 检查缓存
    if (subCommandsCache.value[commandId]?.length > 0) {
      return subCommandsCache.value[commandId]
    }
    
    if (loadingSubCommands.value) {
      return []
    }
    
    loadingSubCommands.value = true
    try {
      const result = await getSubCommands(commandId, context)
      
      // 缓存子命令结果
      subCommandsCache.value[commandId] = result
      
      return result
    } catch (error: any) {
      console.error(`获取命令[${commandId}]的子命令失败:`, error)
      ElMessage.error(`获取子命令失败: ${error.message || '未知错误'}`)
      
      // 返回缓存的子命令，如果有的话
      return subCommandsCache.value[commandId] || []
    } finally {
      loadingSubCommands.value = false
    }
  }

  /**
   * 添加子命令
   */
  async function addSubCommand(parentCommandId: string, subCommandData: Partial<SubCommand>): Promise<SubCommand | null> {
    try {
      const result = await createSubCommand(parentCommandId, subCommandData)
      
      // 更新缓存
      if (!subCommandsCache.value[parentCommandId]) {
        subCommandsCache.value[parentCommandId] = []
      }
      subCommandsCache.value[parentCommandId].push(result)
      
      return result
    } catch (error) {
      console.error(`创建子命令失败:`, error)
      ElMessage.error('创建子命令失败')
      return null
    }
  }

  /**
   * 更新子命令
   */
  async function updateSubCommandById(parentCommandId: string, subCommandId: string, updates: Partial<SubCommand>): Promise<boolean> {
    try {
      await updateSubCommand(parentCommandId, subCommandId, updates)
      
      // 更新缓存
      if (subCommandsCache.value[parentCommandId]) {
        const index = subCommandsCache.value[parentCommandId].findIndex(cmd => cmd.id === subCommandId)
        if (index !== -1) {
          subCommandsCache.value[parentCommandId][index] = {
            ...subCommandsCache.value[parentCommandId][index],
            ...updates
          }
        }
      }
      
      return true
    } catch (error) {
      console.error(`更新子命令失败:`, error)
      ElMessage.error('更新子命令失败')
      return false
    }
  }

  /**
   * 删除子命令
   */
  async function removeSubCommand(parentCommandId: string, subCommandId: string): Promise<boolean> {
    try {
      await deleteSubCommand(parentCommandId, subCommandId)
      
      // 更新缓存
      if (subCommandsCache.value[parentCommandId]) {
        const index = subCommandsCache.value[parentCommandId].findIndex(cmd => cmd.id === subCommandId)
        if (index !== -1) {
          subCommandsCache.value[parentCommandId].splice(index, 1)
        }
      }
      
      return true
    } catch (error) {
      console.error(`删除子命令失败:`, error)
      ElMessage.error('删除子命令失败')
      return false
    }
  }

  /**
   * 获取已缓存的子命令
   */
  function getCachedSubCommands(parentCommandId: string): SubCommand[] {
    return subCommandsCache.value[parentCommandId] || []
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
    commands,
    loading,
    loadingSubCommands,
    commandsByCategory,
    categories,
    privateCommands,
    sharedCommands,
    loadCommands,
    findCommand,
    addCommand,
    updateCommandById,
    removeCommand,
    searchCommands,
    executeCommand,
    getCommandParameters,
    fetchSubCommands,
    addSubCommand,
    updateSubCommandById,
    removeSubCommand,
    getCachedSubCommands
  }
} 