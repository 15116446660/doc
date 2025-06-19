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
    
    console.log('开始加载命令列表')
    loading.value = true
    
    try {
      // 首先加载预设命令
      commands.value = [...presetCommands]
      console.log('已加载预设命令:', presetCommands.length, '个')
      
      // 加载本地自定义命令（优先加载，确保不会被API命令覆盖）
      const localCommands = loadLocalCommands()
      console.log('加载本地命令:', localCommands.length, '个')
      
      if (localCommands.length > 0) {
        // 过滤掉与预设命令ID重复的命令
        const filteredLocalCommands = localCommands.filter(localCmd => 
          !commands.value.some(cmd => cmd.id === localCmd.id)
        )
        console.log('过滤后的本地命令:', filteredLocalCommands.length, '个')
        
        if (filteredLocalCommands.length > 0) {
          commands.value = [...commands.value, ...filteredLocalCommands]
          console.log('添加本地命令后的命令列表:', commands.value.length, '个')
        }
      }
      
      try {
        // 尝试加载私有命令
        console.log('开始从API加载私有命令')
        const privateResponse = await getCommands()
        console.log('API返回的私有命令:', privateResponse.length, '个')
        
        // 尝试加载共享命令
        console.log('开始从API加载共享命令')
        const sharedResponse = await getSharedCommands()
        console.log('API返回的共享命令:', sharedResponse.length, '个')
        
        // 合并命令列表，避免与预设命令和本地命令重复
        const apiCommands = [...privateResponse, ...sharedResponse]
        console.log('API命令总数:', apiCommands.length, '个')
        
        // 过滤掉与已加载命令ID重复的命令
        const filteredApiCommands = apiCommands.filter(apiCmd => 
          !commands.value.some(cmd => cmd.id === apiCmd.id)
        )
        console.log('过滤后的API命令:', filteredApiCommands.length, '个')
        
        if (filteredApiCommands.length > 0) {
          commands.value = [...commands.value, ...filteredApiCommands]
          console.log('添加API命令后的命令列表:', commands.value.length, '个')
        }
      } catch (error) {
        console.warn('从API加载命令失败，使用本地预设命令和自定义命令', error)
      }
      
      // 预加载预设命令的子命令到缓存
      presetCommands.forEach(cmd => {
        if (cmd.hasSubCommands && cmd.subCommands) {
          subCommandsCache.value[cmd.id] = cmd.subCommands
        }
      })
      
      console.log('最终加载的命令列表:', commands.value.length, '个')
      console.log('命令列表详情:', commands.value.map(cmd => ({ id: cmd.id, name: cmd.name, shareType: cmd.shareType })))
    } catch (error: any) {
      console.error('加载命令列表失败:', error)
      ElMessage.error('加载命令列表失败: ' + (error.message || '未知错误'))
      
      // 如果加载失败，确保至少有预设命令可用
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
      console.log('从localStorage加载命令原始数据:', savedCommands ? savedCommands.substring(0, 100) + '...' : null)
      
      if (!savedCommands) {
        console.log('localStorage中没有找到自定义命令')
        return []
      }
      
      try {
        // 验证数据是否为空或无效
        if (savedCommands === '[]' || savedCommands === '{}' || savedCommands === 'null') {
          console.log('localStorage中的命令数据为空')
          return []
        }
        
        const parsedCommands = JSON.parse(savedCommands) as Command[]
        
        if (!Array.isArray(parsedCommands)) {
          console.error('解析后的数据不是数组:', typeof parsedCommands)
          return []
        }
        
        console.log('解析后的本地命令数量:', parsedCommands.length)
        console.log('本地命令ID列表:', parsedCommands.map(cmd => cmd.id))
        
        // 确保所有必要的字段都存在
        const validCommands = parsedCommands.filter(cmd => {
          if (!cmd.id || !cmd.name) {
            console.warn('发现无效的命令:', cmd)
            return false
          }
          
          // 确保命令有提示词，如果没有则设置默认值
          if (!cmd.prompt) {
            console.warn('命令缺少提示词，设置默认值:', cmd.id)
            cmd.prompt = `执行命令: ${cmd.name}`
          }
          
          // 确保其他必要字段
          cmd.shareType = cmd.shareType || 'private'
          cmd.parameters = cmd.parameters || []
          cmd.hasSubCommands = cmd.hasSubCommands || false
          cmd.isSystem = false
          
          return true
        })
        
        if (validCommands.length !== parsedCommands.length) {
          console.warn('过滤掉了', parsedCommands.length - validCommands.length, '个无效命令')
        }
        
        return validCommands
      } catch (parseError) {
        console.error('解析localStorage中的命令数据失败:', parseError)
        return []
      }
    } catch (error) {
      console.error('从localStorage加载命令失败:', error)
      return []
    }
  }
  
  /**
   * 保存自定义命令到本地存储
   */
  function saveLocalCommands(): void {
    // 保存所有本地命令（包括ID以local-开头的命令）和非系统的私有命令
    const customCommands = commands.value.filter(cmd => 
      (cmd.id.startsWith('local-') || (!cmd.isSystem && cmd.shareType === 'private'))
    )
    console.log('保存本地命令：', customCommands)
    console.log('当前commands列表总长度:', commands.value.length)
    console.log('当前commands列表所有ID:', commands.value.map(cmd => cmd.id))
    
    // 检查是否有命令需要保存
    if (customCommands.length === 0) {
      console.warn('没有本地命令需要保存')
      return
    }
    
    // 保存到localStorage
    try {
      // 为了避免循环引用或其他序列化问题，我们只保存必要的字段
      const simplifiedCommands = customCommands.map(cmd => ({
        id: cmd.id,
        name: cmd.name,
        icon: cmd.icon,
        description: cmd.description,
        prompt: cmd.prompt,
        category: cmd.category,
        createdAt: cmd.createdAt,
        updatedAt: cmd.updatedAt,
        parameters: cmd.parameters || [],
        shareType: cmd.shareType || 'private',
        hasSubCommands: cmd.hasSubCommands || false,
        isSystem: false // 本地命令永远不是系统命令
      }))
      
      const jsonString = JSON.stringify(simplifiedCommands)
      
      // 检查数据有效性
      if (!jsonString || jsonString === '[]' || jsonString === '{}' || jsonString === 'null') {
        console.error('序列化后的命令数据无效:', jsonString)
        return
      }
      
      // 清除之前的数据，确保没有残留
      localStorage.removeItem('customCommands')
      
      // 保存新数据
      localStorage.setItem('customCommands', jsonString)
      console.log('本地命令已保存到localStorage，命令数量:', simplifiedCommands.length, '数据长度:', jsonString.length)
      
      // 验证保存是否成功
      const savedData = localStorage.getItem('customCommands')
      if (savedData) {
        try {
          const parsedData = JSON.parse(savedData)
          console.log('保存后验证读取成功，命令数量:', parsedData.length)
          console.log('保存的命令ID列表:', parsedData.map((cmd: any) => cmd.id))
        } catch (parseError) {
          console.error('保存后验证解析失败:', parseError)
        }
      } else {
        console.warn('保存后验证读取失败，无法获取数据')
      }
    } catch (error) {
      console.error('保存本地命令到localStorage失败:', error)
    }
  }
  
  /**
   * 添加新命令
   */
  async function addCommand(commandData: Omit<Command, 'id' | 'createdAt' | 'updatedAt'>): Promise<string> {
    console.log('开始添加命令，数据:', commandData)
    
    try {
      // 根据共享类型选择不同的API
      let response: Command
      
      if (commandData.shareType === 'shared') {
        // 创建共享命令
        console.log('创建共享命令，调用API')
        response = await createSharedCommand(commandData)
      } else {
        // 创建私有命令
        console.log('创建私有命令，调用API')
        response = await createCommand(commandData)
      }
      
      // 添加到本地列表
      console.log('API创建成功，添加到本地列表:', response)
      commands.value.push(response)
      console.log('添加后的命令列表长度:', commands.value.length)
      
      // 如果是私有命令，保存到本地存储
      if (commandData.shareType === 'private') {
        console.log('是私有命令，保存到本地存储')
        saveLocalCommands()
      }
      
      return response.id
    } catch (error) {
      console.error('创建命令失败:', error)
      
      // 如果API失败且是私有命令，创建本地命令
      if (commandData.shareType === 'private') {
        console.log('API创建失败，创建本地命令')
        
        // 生成唯一ID
        const timestamp = Date.now()
        const randomPart = Math.floor(Math.random() * 10000).toString().padStart(4, '0')
        const localId = `local-${timestamp}-${randomPart}`
        console.log('生成的本地命令ID:', localId)
        
        // 确保ID不重复
        const isIdExists = () => commands.value.some(cmd => cmd.id === localId)
        if (isIdExists()) {
          console.warn('命令ID已存在，生成新ID')
          // 如果ID已存在，重新生成（极少发生）
          const newRandomPart = Math.floor(Math.random() * 10000).toString().padStart(4, '0')
          const newLocalId = `local-${timestamp + 1}-${newRandomPart}`
          console.log('重新生成的本地命令ID:', newLocalId)
          
          // 再次检查
          if (commands.value.some(cmd => cmd.id === newLocalId)) {
            console.error('无法生成唯一ID，使用UUID')
            const uuidLocalId = `local-${uuidv4()}`
            
            // 创建新的命令对象
            const newCommand: Command = {
              ...commandData,
              id: uuidLocalId,
              createdAt: timestamp,
              updatedAt: timestamp,
              isSystem: false,
              shareType: 'private'
            }
            
            // 添加到命令列表
            commands.value.push(newCommand)
            console.log('添加使用UUID的本地命令后的命令列表长度:', commands.value.length)
            saveLocalCommands()
            return uuidLocalId
          }
          
          // 创建新的命令对象
          const newCommand: Command = {
            ...commandData,
            id: newLocalId,
            createdAt: timestamp,
            updatedAt: timestamp,
            isSystem: false,
            shareType: 'private'
          }
          
          // 添加到命令列表
          commands.value.push(newCommand)
          console.log('添加重新生成ID的本地命令后的命令列表长度:', commands.value.length)
          saveLocalCommands()
          return newLocalId
        }
        
        const newCommand: Command = {
          ...commandData,
          id: localId,
          createdAt: timestamp,
          updatedAt: timestamp,
          isSystem: false,
          shareType: 'private'
        }
        
        console.log('创建的本地命令:', newCommand)
        
        // 检查ID是否已存在
        const exists = commands.value.some(cmd => cmd.id === localId)
        if (exists) {
          console.warn('命令ID已存在，这不应该发生:', localId)
        }
        
        // 添加到命令列表
        commands.value.push(newCommand)
        console.log('添加本地命令后的命令列表长度:', commands.value.length)
        
        // 检查命令是否成功添加
        const added = commands.value.some(cmd => cmd.id === localId)
        console.log('命令是否成功添加到列表:', added)
        
        // 保存到本地存储
        saveLocalCommands()
        
        // 再次检查命令是否存在于列表中
        setTimeout(() => {
          const stillExists = commands.value.some(cmd => cmd.id === localId)
          console.log('添加后延迟检查，命令是否存在于列表中:', stillExists)
        }, 100)
        
        return localId
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
  watch(commands, (newCommands) => {
    console.log('命令列表发生变化，当前命令数量:', newCommands.length)
    
    // 检查本地命令
    const localCommands = newCommands.filter(cmd => cmd.id.startsWith('local-'))
    console.log('本地命令数量:', localCommands.length)
    if (localCommands.length > 0) {
      console.log('本地命令列表:', localCommands.map(cmd => ({ id: cmd.id, name: cmd.name })))
    }
    
    // 检查私有命令
    const privateCommands = newCommands.filter(cmd => !cmd.isSystem && cmd.shareType === 'private' && !cmd.id.startsWith('local-'))
    console.log('私有命令数量:', privateCommands.length)
    
    // 检查系统命令
    const systemCommands = newCommands.filter(cmd => cmd.isSystem)
    console.log('系统命令数量:', systemCommands.length)
    
    // 保存本地和私有命令
    if (localCommands.length > 0 || privateCommands.length > 0) {
      console.log('有本地或私有命令，需要保存')
      
      // 使用setTimeout确保在当前事件循环结束后执行保存操作
      // 这样可以避免在批量更新命令时触发多次保存
      setTimeout(() => {
        console.log('延迟执行保存操作')
        saveLocalCommands()
      }, 100)
    } else {
      console.log('没有需要保存的本地或私有命令')
    }
  }, { deep: true })
  
  // 组件挂载时加载数据
  onMounted(() => {
    console.log('usePromptCommands组件挂载，开始加载命令')
    loadCommands().then(() => {
      console.log('命令加载完成，当前命令列表:', commands.value.length, '个')
      
      // 检查是否有本地命令
      const localCommands = commands.value.filter(cmd => cmd.id.startsWith('local-'))
      console.log('本地命令数量:', localCommands.length, '个')
      console.log('本地命令列表:', localCommands)
    })
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