<template>
  <el-dialog
    :title="`管理子命令 - ${parentCommand?.name || ''}`"
    v-model="dialogVisible"
    width="700px"
    @close="$emit('close')"
  >
    <div class="sub-command-management-content">
      <!-- 子命令列表和编辑区域 -->
      <div class="sub-command-layout">
        <!-- 子命令列表 -->
        <div class="sub-command-list">
          <div class="sub-command-list-header">
            <h3>子命令列表</h3>
            <el-button type="primary" size="small" @click="createNewSubCommand">
              <el-icon><Plus /></el-icon>
              新建子命令
            </el-button>
          </div>
          
          <el-input
            v-model="searchQuery"
            placeholder="搜索子命令..."
            prefix-icon="Search"
            clearable
            class="sub-command-search"
          />
          
          <div class="sub-command-items">
            <div
              v-for="cmd in filteredSubCommands"
              :key="cmd.id"
              :class="['sub-command-item', { active: cmd.id === selectedSubCommandId }]"
              @click="selectSubCommand(cmd.id)"
            >
              <div class="sub-command-item-icon">
                <el-icon><component :is="cmd.icon || 'ChatLineRound'" /></el-icon>
              </div>
              <div class="sub-command-item-info">
                <div class="sub-command-item-name">{{ cmd.name }}</div>
                <div class="sub-command-item-description">{{ cmd.description }}</div>
              </div>
              <div class="sub-command-item-actions">
                <el-tooltip content="删除" placement="top">
                  <el-button
                    type="text"
                    @click.stop="confirmDeleteSubCommand(cmd.id)"
                  >
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </el-tooltip>
              </div>
            </div>
            
            <el-empty v-if="filteredSubCommands.length === 0" description="没有找到子命令" />
          </div>
        </div>
        
        <!-- 子命令编辑区域 -->
        <div class="sub-command-edit" v-if="selectedSubCommand || isCreatingNew">
          <el-form :model="subCommandForm" label-position="top">
            <el-form-item label="子命令名称" required>
              <el-input 
                v-model="subCommandForm.name" 
                placeholder="请输入子命令名称"
              />
            </el-form-item>
            
            <el-form-item label="图标">
              <div class="icon-selector">
                <div class="selected-icon-display" @click="toggleIconSelector">
                  <el-icon v-if="subCommandForm.icon">
                    <component :is="subCommandForm.icon" />
                  </el-icon>
                  <span>{{ getIconLabel(subCommandForm.icon) }}</span>
                  <el-icon class="arrow-icon"><ArrowDown /></el-icon>
                </div>
                
                <div v-if="showIconSelector" class="icon-grid">
                  <div class="icon-grid-container">
                    <div 
                      v-for="icon in availableIcons" 
                      :key="icon.value"
                      :class="['icon-item', { active: subCommandForm.icon === icon.value }]"
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
            
            <el-form-item label="描述">
              <el-input 
                v-model="subCommandForm.description" 
                type="textarea" 
                rows="2" 
                placeholder="请输入子命令描述"
              />
            </el-form-item>
            
            <el-form-item label="模板提示词" required>
              <el-input 
                v-model="subCommandForm.template" 
                type="textarea" 
                rows="6" 
                placeholder="请输入子命令模板提示词"
              />
              <div class="form-help-text">
                模板提示词将作为子命令执行时的提示词，可以包含{input}、{selectedText}等占位符
              </div>
            </el-form-item>

            <!-- 操作按钮 -->
            <div class="form-actions">
              <el-button @click="handleCancel">取消</el-button>
              <el-button type="primary" @click="saveSubCommand">保存</el-button>
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
import { ref, reactive, computed, watch, onMounted, onUnmounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  Plus,
  Delete,
  Search,
  ArrowDown
} from '@element-plus/icons-vue'
import type { SubCommand, Command } from '@/types/chat'

// 定义组件属性
const props = defineProps<{
  parentCommandId: string,
  parentCommand?: Command,
  subCommands: SubCommand[]
}>()

// 定义事件
const emit = defineEmits<{
  (e: 'save', subCommand: SubCommand): void
  (e: 'delete', subCommandId: string): void
  (e: 'close'): void
  (e: 'refresh'): void
}>()

// 基本状态
const dialogVisible = ref(true)
const searchQuery = ref('')
const selectedSubCommandId = ref<string | null>(null)
const isCreatingNew = ref(false)
const showIconSelector = ref(false)

// 编辑表单
const subCommandForm = reactive<{
  id?: string;
  name: string;
  description: string;
  icon: string;
  template: string;
}>({
  name: '',
  description: '',
  icon: 'Document',
  template: ''
})

// 可用图标列表
const availableIcons = [
  { label: '文档', value: 'Document' },
  { label: '聊天', value: 'ChatRound' },
  { label: '笔记本', value: 'Notebook' },
  { label: '代码', value: 'Terminal' },
  { label: '帮助', value: 'QuestionFilled' },
  { label: '翻译', value: 'Translate' },
  { label: '总结', value: 'Reading' },
  { label: '魔棒', value: 'MagicStick' },
  { label: '列表', value: 'List' },
  { label: '图表', value: 'DataLine' },
  { label: '搜索', value: 'Search' },
  { label: '链接', value: 'Link' },
  { label: '时钟', value: 'Timer' }
]

// 计算属性：过滤后的子命令列表
const filteredSubCommands = computed(() => {
  if (!searchQuery.value.trim()) {
    return props.subCommands
  }
  
  const lowerQuery = searchQuery.value.toLowerCase()
  return props.subCommands.filter(cmd => 
    cmd.name.toLowerCase().includes(lowerQuery) || 
    cmd.description.toLowerCase().includes(lowerQuery)
  )
})

// 计算属性：当前选中的子命令
const selectedSubCommand = computed(() => {
  if (!selectedSubCommandId.value) return null
  return props.subCommands.find(cmd => cmd.id === selectedSubCommandId.value)
})

// 创建新的子命令
function createNewSubCommand() {
  selectedSubCommandId.value = null
  isCreatingNew.value = true
  
  // 重置表单
  Object.assign(subCommandForm, {
    name: '',
    description: '',
    icon: 'Document',
    template: ''
  })
}

// 选择子命令进行编辑
function selectSubCommand(subCommandId: string) {
  selectedSubCommandId.value = subCommandId
  isCreatingNew.value = false
  
  const subCommand = props.subCommands.find(cmd => cmd.id === subCommandId)
  if (subCommand) {
    // 填充表单
    Object.assign(subCommandForm, {
      id: subCommand.id,
      name: subCommand.name,
      description: subCommand.description,
      icon: subCommand.icon,
      template: subCommand.template
    })
  }
}

// 确认删除子命令
function confirmDeleteSubCommand(subCommandId: string) {
  ElMessageBox.confirm(
    '确定要删除此子命令吗？此操作无法撤销。',
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    emit('delete', subCommandId)
    
    if (selectedSubCommandId.value === subCommandId) {
      selectedSubCommandId.value = null
      isCreatingNew.value = false
    }
    
    ElMessage.success('子命令已删除')
  })
}

// 保存子命令
function saveSubCommand() {
  // 验证表单
  if (!subCommandForm.name) {
    ElMessage.warning('请输入子命令名称')
    return
  }
  
  if (!subCommandForm.template) {
    ElMessage.warning('请输入模板提示词')
    return
  }
  
  const subCommand: SubCommand = {
    id: subCommandForm.id || '',
    name: subCommandForm.name,
    description: subCommandForm.description,
    icon: subCommandForm.icon,
    template: subCommandForm.template
  }
  
  emit('save', subCommand)
  
  // 如果是创建新子命令，重置状态
  if (isCreatingNew.value) {
    isCreatingNew.value = false
  }
  
  ElMessage.success(isCreatingNew.value ? '子命令已创建' : '子命令已更新')
  
  // 刷新子命令列表
  emit('refresh')
}

// 取消编辑
function handleCancel() {
  if (isCreatingNew.value) {
    isCreatingNew.value = false
  } else {
    selectedSubCommandId.value = null
  }
}

// 切换图标选择器
function toggleIconSelector() {
  showIconSelector.value = !showIconSelector.value
}

// 选择图标
function selectIcon(iconName: string) {
  subCommandForm.icon = iconName
  showIconSelector.value = false
}

// 获取图标名称
function getIconLabel(iconName: string): string {
  const icon = availableIcons.find(i => i.value === iconName)
  return icon ? icon.label : '选择图标'
}

// 点击外部关闭图标选择器
const closeIconSelector = (e: MouseEvent) => {
  if (showIconSelector.value) {
    showIconSelector.value = false
  }
}

// 添加和移除全局点击事件
onMounted(() => {
  document.addEventListener('click', closeIconSelector)
})

// 组件卸载时移除全局事件监听
onUnmounted(() => {
  document.removeEventListener('click', closeIconSelector)
})
</script>

<style scoped>
.sub-command-management-content {
  display: flex;
  flex-direction: column;
  max-height: 70vh;
}

.sub-command-layout {
  display: flex;
  gap: 20px;
  height: 100%;
}

.sub-command-list {
  width: 260px;
  border-right: 1px solid var(--el-border-color-light);
  padding-right: 20px;
  display: flex;
  flex-direction: column;
}

.sub-command-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.sub-command-list-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.sub-command-search {
  margin-bottom: 16px;
}

.sub-command-items {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sub-command-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.sub-command-item:hover {
  background-color: var(--el-fill-color-light);
}

.sub-command-item.active {
  background-color: var(--el-color-primary-light-9);
}

.sub-command-item-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 6px;
  background-color: var(--el-color-primary-light-8);
  color: var(--el-color-primary);
  margin-right: 12px;
  flex-shrink: 0;
}

.sub-command-item-info {
  flex: 1;
  min-width: 0;
}

.sub-command-item-name {
  font-weight: 500;
  margin-bottom: 2px;
}

.sub-command-item-description {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sub-command-item-actions {
  opacity: 0;
  transition: opacity 0.2s;
}

.sub-command-item:hover .sub-command-item-actions {
  opacity: 1;
}

.sub-command-edit {
  flex: 1;
  min-width: 0;
}

.icon-selector {
  position: relative;
}

.selected-icon-display {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  cursor: pointer;
}

.selected-icon-display .el-icon {
  margin-right: 8px;
}

.arrow-icon {
  margin-left: auto;
}

.icon-grid {
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
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
  max-height: 200px;
  overflow-y: auto;
}

.icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.icon-item:hover {
  background-color: var(--el-fill-color-light);
}

.icon-item.active {
  background-color: var(--el-color-primary-light-9);
}

.icon-item .el-icon {
  font-size: 20px;
  margin-bottom: 4px;
}

.icon-name {
  font-size: 12px;
}

.form-help-text {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}
</style> 