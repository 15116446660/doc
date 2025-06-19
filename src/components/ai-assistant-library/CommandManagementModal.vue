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
            <el-button type="primary" size="small" @click="createNewCommand">
              <el-icon><Plus /></el-icon>
              新建命令
            </el-button>
          </div>
          
          <el-input
            v-model="searchQuery"
            placeholder="搜索命令..."
            prefix-icon="Search"
            clearable
            class="command-search"
          />
          
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
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onUnmounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
// 这些图标组件在模板中通过动态组件使用，ESLint可能无法正确识别
import {
  Plus,
  Delete,
  Lock,
  Share,
  ArrowDown
} from '@element-plus/icons-vue'
import type { Command } from '@/types/chat'

// 定义组件属性
const props = defineProps<{
  commands: Command[]
}>()

// 定义事件
const emit = defineEmits<{
  (e: 'save', command: Command): void
  (e: 'delete', commandId: string): void
  (e: 'close'): void
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
  shareType: 'private'
})

// 添加新建状态
const isCreatingNew = ref(false)

// 计算属性：过滤后的命令列表
const filteredCommands = computed(() => {
  if (!searchQuery.value) {
    return [...props.commands].sort((a, b) => {
      // 系统命令排在前面
      if (a.isSystem && !b.isSystem) return -1
      if (!a.isSystem && b.isSystem) return 1
      // 按名称排序
      return a.name.localeCompare(b.name)
    })
  }
  
  const query = searchQuery.value.toLowerCase()
  return props.commands
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
    shareType: 'private'
  })
  
  // 清除选中状态，进入新建模式
  selectedCommandId.value = null
  isCreatingNew.value = true
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
    id: commandForm.id || `cmd-${Date.now()}`,
    updatedAt: Date.now(),
    shareType: commandForm.shareType || 'private'
  }
  
  // 触发保存事件
  emit('save', commandToSave)
  
  // 更新选中ID并退出新建模式
  selectedCommandId.value = commandToSave.id
  isCreatingNew.value = false
  
  ElMessage.success('命令已保存')
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

// 初始化
watch(() => props.commands, (newCommands) => {
  if (newCommands.length > 0 && !selectedCommandId.value) {
    // 默认选择第一个命令
    selectCommand(newCommands[0].id)
  }
}, { immediate: true })
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
</style> 