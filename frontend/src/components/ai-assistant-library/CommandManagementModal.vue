<template>
  <el-dialog
    title="命令管理"
    v-model="dialogVisible"
    width="700px"
    @close="$emit('close')"
  >
    <div class="command-management-content">
      <!-- 命令列表和编辑区域 -->
      <div class="command-layout">
        <!-- 命令列表 -->
        <div class="command-list">
          <div class="command-list-header">
            <h3>命令列表</h3>
            <div class="command-actions">
              <el-button type="warning" size="small" @click="createTestLocalCommand">
                <el-icon><Warning /></el-icon>
                测试本地命令
              </el-button>
              <el-button type="info" size="small" @click="checkLocalStorage">
                <el-icon><Search /></el-icon>
                检查存储
              </el-button>
            <el-button type="primary" size="small" @click="createNewCommand">
              <el-icon><Plus /></el-icon>
              新建命令
            </el-button>
            </div>
          </div>
          
          <el-input
            v-model="searchQuery"
            placeholder="搜索命令..."
            prefix-icon="Search"
            clearable
            class="command-search"
          />
          
          <!-- 测试工具区域 -->
          <div class="test-tools" v-if="isDevelopment">
            <el-divider>测试工具</el-divider>
            <div class="test-buttons">
              <el-button size="small" type="info" @click="createTestLocalCommand">创建测试命令</el-button>
              <el-button size="small" type="warning" @click="checkLocalStorage">检查localStorage</el-button>
              <el-button size="small" type="danger" @click="confirmClearLocalStorage">清理localStorage</el-button>
            </div>
          </div>
          
          <div class="command-items">
            <div
              v-for="cmd in filteredCommands"
              :key="cmd.id"
              :class="['command-item', { active: cmd.id === selectedCommandId }]"
              @click="selectCommand(cmd.id)"
            >
              <div class="command-item-icon">
                <el-icon><component :is="cmd.icon || 'ChatLineRound'" /></el-icon>
              </div>
              <div class="command-item-info">
                <div class="command-item-name">{{ cmd.name }}</div>
                <div class="command-item-meta">
                  <span class="command-item-category" v-if="cmd.category">
                    {{ cmd.category }}
                  </span>
                  <span class="command-item-share-type" v-if="!cmd.isSystem">
                    <el-icon v-if="cmd.shareType === 'private'"><Lock /></el-icon>
                    <el-icon v-else-if="cmd.shareType === 'shared'"><Share /></el-icon>
                  </span>
                </div>
              </div>
              <div class="command-item-actions">
                <el-tooltip content="删除" placement="top" v-if="!cmd.isSystem">
                  <el-button
                    type="text"
                    @click.stop="confirmDeleteCommand(cmd.id)"
                  >
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </el-tooltip>
              </div>
            </div>
            
            <el-empty v-if="filteredCommands.length === 0" description="没有找到命令" />
          </div>
        </div>
        
        <!-- 命令编辑区域 -->
        <div class="command-edit" v-if="selectedCommand || isCreatingNew">
          <el-form :model="commandForm" label-position="top">
            <el-form-item label="命令名称" required>
              <el-input 
                v-model="commandForm.name" 
                placeholder="请输入命令名称"
                :disabled="selectedCommand?.isSystem"
              />
            </el-form-item>
            
            <el-form-item label="图标">
              <div class="icon-selector" :class="{ 'disabled': selectedCommand?.isSystem }">
                <div class="selected-icon-display" @click="!selectedCommand?.isSystem && toggleIconSelector()">
                  <el-icon v-if="commandForm.icon">
                    <component :is="commandForm.icon" />
                  </el-icon>
                  <span>{{ getIconLabel(commandForm.icon) }}</span>
                  <el-icon class="arrow-icon"><ArrowDown /></el-icon>
                </div>
                
                <div v-if="showIconSelector" class="icon-grid">
                  <div class="icon-grid-container">
                    <div 
                      v-for="icon in availableIcons" 
                      :key="icon.value"
                      :class="['icon-item', { active: commandForm.icon === icon.value }]"
                      @click="selectIcon(icon.value)"
                    >
                      <el-icon>
                        <component :is="icon.value" />
                      </el-icon>
                      <span class="icon-name">{{ icon.label }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </el-form-item>
            
            <el-form-item label="分类">
              <el-input 
                v-model="commandForm.category" 
                placeholder="请输入分类"
                :disabled="selectedCommand?.isSystem"
              />
            </el-form-item>
            
            <el-form-item label="共享类型" v-if="!selectedCommand?.isSystem">
              <el-radio-group v-model="commandForm.shareType">
                <el-radio label="private">
                  <div class="share-type-option">
                    <el-icon><Lock /></el-icon>
                    <span>本地私有</span>
                    <div class="share-type-desc">仅在本地设备可用</div>
                  </div>
                </el-radio>
                <el-radio label="shared">
                  <div class="share-type-option">
                    <el-icon><Share /></el-icon>
                    <span>服务端共享</span>
                    <div class="share-type-desc">所有用户可见和使用</div>
                  </div>
                </el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="描述">
              <el-input 
                v-model="commandForm.description" 
                type="textarea" 
                rows="2" 
                placeholder="请输入命令描述"
                :disabled="selectedCommand?.isSystem"
              />
            </el-form-item>
            
            <el-form-item label="提示词" required>
              <el-input 
                v-model="commandForm.prompt" 
                type="textarea" 
                rows="6" 
                placeholder="请输入命令提示词"
                :disabled="selectedCommand?.isSystem"
              />
              <div class="form-help-text">
                提示词将作为系统指令发送给AI，可以包含{参数}占位符
              </div>
            </el-form-item>
            
            <!-- 子命令支持 -->
            <div class="sub-commands-section" v-if="!selectedCommand?.isSystem">
              <el-divider content-position="left">子命令设置</el-divider>
              
              <el-form-item label="启用子命令">
                <el-switch 
                  v-model="commandForm.hasSubCommands" 
                  :disabled="selectedCommand?.isSystem"
                />
                <div class="form-help-text" v-if="commandForm.hasSubCommands">
                  启用后，此命令将作为父命令，可以添加多个子命令
                </div>
              </el-form-item>
              
              <el-form-item v-if="commandForm.hasSubCommands">
                <el-button 
                  type="primary" 
                  @click="openSubCommandManagement"
                  :disabled="!commandForm.id"
                >
                  <el-icon><Setting /></el-icon>
                  管理子命令
                </el-button>
                <div class="form-help-text" v-if="!commandForm.id">
                  请先保存命令后再管理子命令
                </div>
                <div v-if="subCommands.length > 0" class="sub-command-summary">
                  <div class="sub-command-count">
                    已添加 {{ subCommands.length }} 个子命令
                  </div>
                  <div class="sub-command-tags">
                    <el-tag 
                      v-for="subCmd in subCommands.slice(0, 3)" 
                      :key="subCmd.id"
                      size="small"
                      class="sub-command-tag"
                    >
                      {{ subCmd.name }}
                    </el-tag>
                    <el-tag v-if="subCommands.length > 3" size="small" type="info">
                      +{{ subCommands.length - 3 }} 个
                    </el-tag>
                  </div>
                  <div class="sub-command-actions">
                    <el-button 
                      type="primary" 
                      plain
                      size="small" 
                      @click="viewSubCommands"
                    >
                      <el-icon><View /></el-icon>
                      查看子命令
                    </el-button>
                  </div>
                </div>
              </el-form-item>
              
              <el-form-item label="子命令API端点" v-if="commandForm.hasSubCommands">
                <el-input 
                  v-model="commandForm.subCommandsEndpoint" 
                  placeholder="请输入获取子命令的API端点（可选）"
                  :disabled="selectedCommand?.isSystem"
                />
                <div class="form-help-text">
                  如果子命令需要从特定API获取，请填写对应端点，留空则使用默认端点
                </div>
              </el-form-item>
            </div>
            
            <!-- 参数列表 -->
            <div class="parameters-section" v-if="!selectedCommand?.isSystem">
              <div class="parameters-header">
                <h4>参数列表</h4>
                <el-button link @click="addParameter">
                  <el-icon><Plus /></el-icon>
                  添加参数
                </el-button>
              </div>
              
              <div 
                v-for="(param, index) in commandForm.parameters" 
                :key="index"
                class="parameter-item"
              >
                <el-row :gutter="10">
                  <el-col :span="8">
                    <el-input 
                      v-model="param.name" 
                      placeholder="参数名称"
                    />
                  </el-col>
                  <el-col :span="10">
                    <el-input 
                      v-model="param.description" 
                      placeholder="参数描述"
                    />
                  </el-col>
                  <el-col :span="4">
                    <el-checkbox v-model="param.required">必填</el-checkbox>
                  </el-col>
                  <el-col :span="2">
                    <el-button 
                      link 
                      @click="removeParameter(index)"
                    >
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </el-col>
                </el-row>
              </div>
            </div>
            
            <!-- 系统命令提示 -->
            <div v-if="selectedCommand?.isSystem" class="system-command-notice">
              <el-alert
                title="系统命令不可编辑"
                type="info"
                :closable="false"
                show-icon
              />
              
              <!-- 系统命令子命令查看入口 -->
              <div v-if="selectedCommand?.hasSubCommands" class="system-sub-commands">
                <div class="sub-command-summary mt-3">
                  <div class="system-sub-command-header">
                    <el-icon><Setting /></el-icon>
                    <span class="ml-1">子命令管理</span>
                  </div>
                  
                  <el-button 
                    type="primary" 
                    plain
                    size="small" 
                    class="mt-2 w-full"
                    @click="viewSubCommandsForSystem"
                  >
                    <el-icon><View /></el-icon>
                    查看预设子命令
                  </el-button>
                </div>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="form-actions" v-if="!selectedCommand?.isSystem">
              <el-button @click="handleClose">取消</el-button>
              <el-button type="primary" @click="saveCommand">保存</el-button>
            </div>
          </el-form>
        </div>
      </div>
    </div>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="$emit('close')">关闭</el-button>
      </span>
    </template>
  </el-dialog>
  
  <!-- 子命令管理对话框 -->
  <SubCommandManagementModal
    v-if="showSubCommandModal"
    :parent-command-id="commandForm.id || ''"
    :parent-command="selectedCommand"
    :sub-commands="subCommands"
    @save="handleSaveSubCommand"
    @delete="handleDeleteSubCommand"
    @refresh="refreshSubCommands"
    @close="showSubCommandModal = false"
  />
  
  <!-- 子命令查看对话框 -->
  <SubCommandViewerModal
    v-if="showSubCommandViewerModal"
    :parent-command-id="selectedCommand?.isSystem ? (selectedCommand.id || '') : (commandForm.id || '')"
    :parent-command="selectedCommand || undefined"
    @close="showSubCommandViewerModal = false"
  />
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onUnmounted, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
// 这些图标组件在模板中通过动态组件使用，ESLint可能无法正确识别
import {
  Plus,
  Delete,
  Lock,
  Share,
  ArrowDown,
  Setting,
  View,
  Warning,
  Search
} from '@element-plus/icons-vue'
import type { Command, SubCommand } from '@/types/chat'
import SubCommandManagementModal from './SubCommandManagementModal.vue'
import SubCommandViewerModal from './SubCommandViewerModal.vue'
import { usePromptCommands } from './hooks/usePromptCommands'

// 开发模式判断
const isDevelopment = ref(process.env.NODE_ENV === 'development' || import.meta.env?.DEV)

// 组件属性
const props = defineProps<{
  commands: Command[]
}>()

// 定义事件
const emit = defineEmits<{
  (e: 'save', command: Command): void
  (e: 'delete', commandId: string): void
  (e: 'close'): void
  (e: 'update:commands', commands: Command[]): void
}>()

// 状态
const dialogVisible = ref(true)
const searchQuery = ref('')
const selectedCommandId = ref<string | null>(null)
const showIconSelector = ref(false)

// 可用图标列表
const availableIcons = [
  { value: 'ChatLineRound', label: '聊天' },
  { value: 'Document', label: '文档' },
  { value: 'Edit', label: '编辑' },
  { value: 'Search', label: '搜索' },
  { value: 'QuestionFilled', label: '问题' },
  { value: 'Operation', label: '操作' },
  { value: 'Star', label: '星标' },
  { value: 'List', label: '列表' }
]

// 命令表单
const commandForm = reactive<Command>({
  id: '',
  name: '',
  icon: 'ChatLineRound',
  description: '',
  prompt: '',
  category: '',
  createdAt: 0,
  updatedAt: 0,
  parameters: [],
  shareType: 'private',
  hasSubCommands: false,
  subCommandsEndpoint: ''
})

// 添加新建状态
const isCreatingNew = ref(false)

// 子命令相关
const showSubCommandModal = ref(false)
const showSubCommandViewerModal = ref(false)
const subCommands = ref<SubCommand[]>([])
const { fetchSubCommands, addSubCommand, updateSubCommandById, removeSubCommand } = usePromptCommands()

// 计算属性：过滤后的命令列表
const filteredCommands = computed(() => {
  console.log('CommandManagementModal - 可用命令列表:', props.commands.length, '个')
  console.log('命令类型统计:', {
    系统命令: props.commands.filter(cmd => cmd.isSystem).length,
    本地命令: props.commands.filter(cmd => cmd.id.startsWith('local-')).length,
    私有命令: props.commands.filter(cmd => !cmd.isSystem && cmd.shareType === 'private' && !cmd.id.startsWith('local-')).length,
    共享命令: props.commands.filter(cmd => cmd.shareType === 'shared').length
  })
  
  if (!searchQuery.value) {
    const sorted = [...props.commands].sort((a, b) => {
      // 系统命令排在前面
      if (a.isSystem && !b.isSystem) return -1
      if (!a.isSystem && b.isSystem) return 1
      // 按名称排序
      return a.name.localeCompare(b.name)
    })
    console.log('排序后的命令列表:', sorted.length, '个')
    return sorted
  }
  
  const query = searchQuery.value.toLowerCase()
  const filtered = props.commands
    .filter(cmd => {
      return cmd.name.toLowerCase().includes(query) ||
             (cmd.description && cmd.description.toLowerCase().includes(query)) ||
             (cmd.category && cmd.category.toLowerCase().includes(query))
    })
    .sort((a, b) => {
      if (a.isSystem && !b.isSystem) return -1
      if (!a.isSystem && b.isSystem) return 1
      return a.name.localeCompare(b.name)
    })
  
  console.log('搜索过滤后的命令列表:', filtered.length, '个')
  return filtered
})

// 计算属性：当前选中的命令
const selectedCommand = computed(() => {
  if (!selectedCommandId.value) return null
  return props.commands.find(cmd => cmd.id === selectedCommandId.value) || null
})

// 选择命令
function selectCommand(commandId: string) {
  selectedCommandId.value = commandId
  isCreatingNew.value = false
  
  // 更新表单
  const command = props.commands.find(cmd => cmd.id === commandId)
  if (command) {
    Object.assign(commandForm, JSON.parse(JSON.stringify(command)))
  }
}

// 创建新命令
function createNewCommand() {
  console.log('创建新命令')
  // 重置表单
  Object.assign(commandForm, {
    id: '',
    name: '新命令',
    icon: 'ChatLineRound',
    description: '',
    prompt: '',
    category: '',
    createdAt: Date.now(),
    updatedAt: Date.now(),
    parameters: [],
    shareType: 'private',
    hasSubCommands: false,
    subCommandsEndpoint: ''
  })
  
  console.log('新命令表单已重置:', commandForm)
  
  // 清除选中状态，进入新建模式
  selectedCommandId.value = null
  isCreatingNew.value = true
  
  console.log('进入新建模式')
}

// 添加参数
function addParameter() {
  if (!commandForm.parameters) {
    commandForm.parameters = []
  }
  
  commandForm.parameters.push({
    name: '',
    description: '',
    required: false
  })
}

// 移除参数
function removeParameter(index: number) {
  if (commandForm.parameters) {
    commandForm.parameters.splice(index, 1)
  }
}

// 保存命令
function saveCommand() {
  // 验证表单
  if (!commandForm.name.trim()) {
    ElMessage.warning('请输入命令名称')
    return
  }
  
  if (!commandForm.prompt?.trim()) {
    ElMessage.warning('请输入命令提示词')
    return
  }
  
  // 准备保存的命令数据
  const commandToSave: Command = {
    ...commandForm,
    // 如果是新建命令且是私有类型，使用local-前缀的ID
    id: commandForm.id || (commandForm.shareType === 'private' ? `local-${Date.now()}` : `cmd-${Date.now()}`),
    updatedAt: Date.now(),
    shareType: commandForm.shareType || 'private'
  }
  
  console.log('准备保存命令:', commandToSave)
  console.log('命令ID:', commandToSave.id)
  console.log('命令类型:', commandToSave.shareType)
  
  // 触发保存事件
  emit('save', commandToSave)
  
  // 更新选中ID并退出新建模式
  selectedCommandId.value = commandToSave.id
  isCreatingNew.value = false
  
  ElMessage.success('命令已保存')
  
  // 验证保存结果
  setTimeout(() => {
    // 这里添加一个延时，确保命令已经保存到localStorage
    console.log('验证命令是否成功保存')
    const savedCommands = localStorage.getItem('customCommands')
    if (savedCommands) {
      const parsedCommands = JSON.parse(savedCommands)
      const foundCommand = parsedCommands.find((cmd: any) => cmd.id === commandToSave.id)
      console.log('在localStorage中找到命令:', foundCommand ? '是' : '否')
    } else {
      console.warn('localStorage中没有找到customCommands')
    }
  }, 500)
}

// 确认删除命令
function confirmDeleteCommand(commandId: string) {
  ElMessageBox.confirm(
    '确定要删除此命令吗？此操作无法撤销。',
    '确认删除',
    {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    emit('delete', commandId)
    ElMessage.success('命令已删除')
    if (commandId === selectedCommandId.value) {
      selectedCommandId.value = null
      isCreatingNew.value = false
    }
  }).catch(() => {
    // 用户取消则不执行任何操作
  })
}

// 添加关闭处理函数
function handleClose() {
  // 如果是新建模式，清除新建状态
  if (isCreatingNew.value) {
    isCreatingNew.value = false
    selectedCommandId.value = null
  }
  // 否则关闭对话框
  else {
    emit('close')
  }
}

/**
 * 获取图标标签
 */
function getIconLabel(iconValue: string | undefined): string {
  if (!iconValue) return '选择图标'
  const icon = availableIcons.find(icon => icon.value === iconValue)
  return icon ? icon.label : '选择图标'
}

/**
 * 选择图标
 */
function selectIcon(iconValue: string): void {
  commandForm.icon = iconValue
  showIconSelector.value = false
}

/**
 * 切换图标选择器
 */
function toggleIconSelector(): void {
  showIconSelector.value = !showIconSelector.value
  
  // 如果打开了选择器，添加点击外部关闭的事件
  if (showIconSelector.value) {
    setTimeout(() => {
      document.addEventListener('click', handleOutsideClick)
    }, 0)
  }
}

/**
 * 处理点击外部关闭选择器
 */
function handleOutsideClick(event: MouseEvent): void {
  const iconSelector = document.querySelector('.icon-selector')
  if (iconSelector && !iconSelector.contains(event.target as Node)) {
    showIconSelector.value = false
    document.removeEventListener('click', handleOutsideClick)
  }
}

// 组件卸载时移除事件监听
onUnmounted(() => {
  document.removeEventListener('click', handleOutsideClick)
})

// 组件挂载时调试
onMounted(() => {
  console.log('CommandManagementModal组件挂载')
  
  // 检查localStorage中的命令数据
  const savedCommands = localStorage.getItem('customCommands')
  console.log('localStorage中的命令数据:', savedCommands)
  
  if (savedCommands) {
    try {
      const parsedCommands = JSON.parse(savedCommands)
      console.log('解析后的命令数据:', parsedCommands)
      console.log('localStorage中的命令数量:', parsedCommands.length)
    } catch (error) {
      console.error('解析localStorage中的命令数据失败:', error)
    }
  } else {
    console.warn('localStorage中没有找到customCommands')
  }
  
  // 检查传入的命令列表
  console.log('传入的命令列表:', props.commands)
  console.log('传入的命令数量:', props.commands.length)
  
  // 检查本地命令
  const localCommands = props.commands.filter(cmd => cmd.id.startsWith('local-'))
  console.log('传入的本地命令:', localCommands)
  console.log('传入的本地命令数量:', localCommands.length)
})

// 初始化
watch(() => props.commands, (newCommands) => {
  console.log('CommandManagementModal - 命令列表变化，新命令数量:', newCommands.length)
  
  if (newCommands.length > 0) {
    console.log('命令列表详情:', newCommands.map(cmd => ({ id: cmd.id, name: cmd.name, shareType: cmd.shareType })))
    
    if (!selectedCommandId.value) {
    // 默认选择第一个命令
      console.log('没有选中的命令，默认选择第一个:', newCommands[0].id)
      selectCommand(newCommands[0].id)
    } else {
      // 检查选中的命令是否还存在
      const commandExists = newCommands.some(cmd => cmd.id === selectedCommandId.value)
      console.log('当前选中的命令:', selectedCommandId.value, '是否存在:', commandExists)
      
      if (!commandExists) {
        // 如果选中的命令不存在了，选择第一个命令
        console.log('选中的命令不存在，默认选择第一个:', newCommands[0].id)
    selectCommand(newCommands[0].id)
      }
    }
  } else {
    console.log('命令列表为空')
  }
}, { immediate: true })

// 打开子命令管理
async function openSubCommandManagement() {
  if (!commandForm.id) {
    ElMessage.warning('请先保存命令后再管理子命令')
    return
  }
  
  try {
    // 加载子命令
    subCommands.value = await fetchSubCommands(commandForm.id)
    showSubCommandModal.value = true
  } catch (error) {
    console.error('加载子命令失败:', error)
    ElMessage.error('加载子命令失败，请稍后再试')
  }
}

// 刷新子命令列表
async function refreshSubCommands() {
  if (commandForm.id) {
    try {
      subCommands.value = await fetchSubCommands(commandForm.id)
    } catch (error) {
      console.error('刷新子命令失败:', error)
    }
  }
}

// 处理子命令保存
async function handleSaveSubCommand(subCommand: SubCommand) {
  if (!commandForm.id) return
  
  try {
    if (subCommand.id) {
      // 更新子命令
      await updateSubCommandById(commandForm.id, subCommand.id, subCommand)
    } else {
      // 创建子命令
      await addSubCommand(commandForm.id, subCommand)
    }
    
    // 刷新子命令列表
    await refreshSubCommands()
  } catch (error) {
    console.error('保存子命令失败:', error)
    ElMessage.error('保存子命令失败，请稍后再试')
  }
}

// 处理子命令删除
async function handleDeleteSubCommand(subCommandId: string) {
  if (!commandForm.id) return
  
  try {
    await removeSubCommand(commandForm.id, subCommandId)
    
    // 刷新子命令列表
    await refreshSubCommands()
  } catch (error) {
    console.error('删除子命令失败:', error)
    ElMessage.error('删除子命令失败，请稍后再试')
  }
}

// 查看子命令（用于用户自定义命令）
function viewSubCommands() {
  if (!commandForm.id) {
    ElMessage.warning('请先保存命令后再查看子命令')
    return
  }
  
  // 打开子命令查看对话框
  showSubCommandViewerModal.value = true
}

// 查看系统命令的子命令
async function viewSubCommandsForSystem() {
  if (!selectedCommand.value || !selectedCommand.value.id) {
    ElMessage.warning('无法获取命令信息')
    return
  }
  
  try {
    // 对于系统命令，首先尝试获取其预设的子命令
    if (selectedCommand.value.subCommands) {
      subCommands.value = [...selectedCommand.value.subCommands]
    } else {
      // 如果没有预设子命令，则尝试从API获取
      subCommands.value = await fetchSubCommands(selectedCommand.value.id)
    }
    
    // 打开子命令查看对话框
    if (subCommands.value.length === 0) {
      ElMessage.info('此命令暂无子命令')
      return
    }
    
    showSubCommandViewerModal.value = true
  } catch (error) {
    console.error('加载预设子命令失败:', error)
    ElMessage.error('加载预设子命令失败，请稍后再试')
  }
}

// 创建测试本地命令
function createTestLocalCommand() {
  console.log('创建测试本地命令')
  
  // 生成唯一的测试命令名称
  const timestamp = Date.now()
  const randomPart = Math.floor(Math.random() * 10000).toString().padStart(4, '0')
  const testCommandId = `local-test-${timestamp}-${randomPart}`
  
  // 创建测试命令对象
  const testCommand: Command = {
    id: testCommandId,
    name: `测试命令-${timestamp}`,
    icon: 'Warning',
    description: '这是一个测试本地命令',
    prompt: '这是测试本地命令的提示词，用于测试本地命令是否正常工作。',
    category: '测试',
    createdAt: timestamp,
    updatedAt: timestamp,
    parameters: [],
    shareType: 'private',
    hasSubCommands: false,
    isSystem: false
  }
  
  console.log('创建的测试本地命令:', testCommand)
  
  // 直接保存测试命令
  emit('save', testCommand)
  
  // 选中新创建的命令
  setTimeout(() => {
    // 延迟选中，确保命令已经添加到列表中
    selectCommand(testCommandId)
    
    // 再次检查命令是否已添加到列表
    const commandExists = props.commands.some(cmd => cmd.id === testCommandId)
    console.log('测试命令是否已添加到列表:', commandExists ? '是' : '否')
    
    if (commandExists) {
      ElMessage.success('测试本地命令已创建')
    } else {
      ElMessage.warning('测试命令创建可能失败，请检查控制台')
      // 尝试手动添加
      const commands = [...props.commands]
      commands.push(testCommand)
      emit('update:commands', commands)
      console.log('已尝试手动添加测试命令到列表')
    }
    
    // 检查localStorage
    setTimeout(checkLocalStorage, 500)
  }, 500)
}

// 检查localStorage中的命令
function checkLocalStorage() {
  console.log('检查localStorage中的命令')
  
  // 获取localStorage中的命令数据
  const savedCommands = localStorage.getItem('customCommands')
  console.log('localStorage中的命令数据:', savedCommands)
  
  if (savedCommands) {
    try {
      const parsedCommands = JSON.parse(savedCommands)
      console.log('解析后的命令数据:', parsedCommands)
      console.log('localStorage中的命令数量:', parsedCommands.length)
    } catch (error) {
      console.error('解析localStorage中的命令数据失败:', error)
    }
  } else {
    console.warn('localStorage中没有找到customCommands')
  }
}

// 清理localStorage中的命令
function clearLocalStorage() {
  console.log('清理localStorage中的命令')
  localStorage.removeItem('customCommands')
  ElMessage.success('已清除本地命令缓存')
  
  // 重新加载页面以刷新命令列表
  setTimeout(() => {
    window.location.reload()
  }, 1000)
}

// 确认清理localStorage
function confirmClearLocalStorage() {
  ElMessageBox.confirm(
    '确定要清除所有本地命令缓存吗？这将删除所有自定义命令，且无法恢复。',
    '确认操作',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    clearLocalStorage()
  }).catch(() => {
    ElMessage.info('已取消操作')
  })
}
</script>

<style scoped>
.command-management-content {
  height: 550px;
}

.command-layout {
  display: flex;
  height: 100%;
  gap: 20px;
}

.command-list {
  width: 250px;
  border-right: 1px solid var(--el-border-color-light);
  padding-right: 16px;
  display: flex;
  flex-direction: column;
}

.command-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.command-list-header h3 {
  margin: 0;
}

.command-actions {
  display: flex;
  gap: 8px;
}

.command-search {
  margin-bottom: 16px;
}

.command-items {
  flex: 1;
  overflow-y: auto;
}

.command-item {
  display: flex;
  align-items: center;
  padding: 10px;
  border-radius: 4px;
  cursor: pointer;
  margin-bottom: 8px;
  transition: background-color 0.2s;
}

.command-item:hover {
  background-color: var(--el-fill-color-light);
}

.command-item.active {
  background-color: var(--el-color-primary-light-9);
}

.command-item-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 4px;
  background-color: var(--el-color-primary-light-8);
  margin-right: 12px;
  color: var(--el-color-primary);
}

.command-item-info {
  flex: 1;
  overflow: hidden;
}

.command-item-name {
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.command-item-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.command-item-category {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.command-item-actions {
  opacity: 0;
  transition: opacity 0.2s;
}

.command-item:hover .command-item-actions {
  opacity: 1;
}

.command-edit {
  flex: 1;
  overflow-y: auto;
  padding-right: 10px;
}

.icon-selector {
  position: relative;
  width: 100%;
}

.icon-selector.disabled .selected-icon-display {
  cursor: not-allowed;
  background-color: var(--el-fill-color);
  color: var(--el-text-color-disabled);
}

.selected-icon-display {
  display: flex;
  align-items: center;
  padding: 0px 12px;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.selected-icon-display:hover:not(.disabled) {
  border-color: var(--el-color-primary);
}

.selected-icon-display .el-icon {
  margin-right: 8px;
  font-size: 16px;
}

.selected-icon-display .arrow-icon {
  margin-left: auto;
  transition: transform 0.3s;
}

.icon-selector:has(.icon-grid) .arrow-icon {
  transform: rotate(180deg);
}

.icon-grid {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background-color: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  padding: 12px;
  margin-top: 4px;
  z-index: 10;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.icon-grid-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12px 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  text-align: center;
}

.icon-item:hover {
  background-color: var(--el-fill-color-light);
}

.icon-item.active {
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

.icon-item .el-icon {
  font-size: 20px;
  margin-bottom: 4px;
}

.icon-name {
  font-size: 12px;
  margin-top: 4px;
}

.form-help-text {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}

.parameters-section {
  margin-top: 16px;
}

.parameters-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.parameters-header h4 {
  margin: 0;
}

.parameter-item {
  margin-bottom: 12px;
  padding: 12px;
  background-color: var(--el-fill-color-light);
  border-radius: 4px;
}

.system-command-notice {
  margin-top: 16px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.form-actions {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.share-type-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.share-type-desc {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.command-item-share-type {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.command-item-share-type .el-icon {
  font-size: 14px;
  margin-right: 2px;
}

.sub-commands-section {
  margin-bottom: 20px;
}

.sub-command-summary {
  margin-top: 8px;
  padding: 8px;
  background-color: var(--el-fill-color-light);
  border-radius: 4px;
}

.sub-command-count {
  font-size: 13px;
  margin-bottom: 6px;
  color: var(--el-text-color-secondary);
}

.sub-command-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.sub-command-tag {
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sub-command-actions {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.system-sub-commands {
  margin-top: 16px;
}

.system-sub-command-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  margin-bottom: 8px;
  color: var(--el-text-color-primary);
}

.mt-1 {
  margin-top: 4px;
}

.mt-2 {
  margin-top: 8px;
}

.mt-3 {
  margin-top: 12px;
}

.w-full {
  width: 100%;
}

.ml-1 {
  margin-left: 4px;
}
</style> 