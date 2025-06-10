<template>
  <div class="base-tree-select">
    <div class="tree-header">
      <h3 class="tree-title">{{ treeTitle }}</h3>
      <div class="header-actions">
        <el-button v-if="showAddRoot" type="primary" link @click="handleAddRoot">
          <el-icon><plus /></el-icon>
          {{ addButtonText }}
        </el-button>
        <el-button v-if="showRefreshButton" type="primary" link @click="refreshTreeData">
          <el-icon><refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>
    
    <div v-if="showSearch" class="tree-search">
      <el-input
        v-model="searchKeyword"
        :placeholder="searchPlaceholder"
        clearable
        :prefix-icon="Search"
        size="default"
      />
    </div>
    
    <div class="tree-content">
      <!-- 当前选中节点 -->
      <div v-if="showSelection" class="current-selection">
        <span class="selection-label">当前选择：</span>
        <el-tag 
          size="small" 
          :closable="!!currentNode" 
          @close="clearSelection"
          :type="currentNode ? 'primary' : 'info'"
          effect="light"
          class="selection-tag"
        >
          {{ currentNode ? currentNode[keyMap.label] : '全部' }}
        </el-tag>
      </div>
      <el-scrollbar>
        <el-tree
          ref="treeRef"
          :data="treeData"
          :props="treeProps"
          :filter-node-method="filterNode"
          :expand-on-click-node="false"
          node-key="id"
          highlight-current
          :default-expanded-keys="defaultExpandedKeys"
          @node-click="handleNodeClick"
          @node-contextmenu="handleContextMenu"
          @node-expand="handleNodeExpand"
          @node-collapse="handleNodeCollapse"
          :load="loadNode"
          :lazy="isLazy"
          :render-after-expand="false"
          class="custom-tree"
        >
          <template #default="{ data, node }">
            <slot name="node" :data="data" :node="node">
              <div class="custom-tree-node" :class="{ 'is-current': node.isCurrent }">
                <div class="node-content">
                  <slot name="icon" :data="data" :node="node" :is-expanded="expandedKeys.includes(data[keyMap.id])">
                    <el-icon class="node-icon" :class="{ 'is-expanded': expandedKeys.includes(data[keyMap.id]) }">
                      <folder-opened v-if="expandedKeys.includes(data[keyMap.id])" />
                      <folder v-else />
                    </el-icon>
                  </slot>
                  <div class="node-labels">
                    <span v-if="showNodeCode && keyMap.code && data[keyMap.code]" class="node-code">{{ data[keyMap.code] }}</span>
                    <span class="node-name">{{ data[keyMap.label] }}</span>
                  </div>
                </div>
              </div>
            </slot>
          </template>
        </el-tree>
      </el-scrollbar>
    </div>

    <!-- 右键菜单 -->
    <div 
      v-show="contextMenuVisible" 
      class="context-menu"
      :style="{ left: contextMenuPosition.x + 'px', top: contextMenuPosition.y + 'px' }"
    >
      <ul class="menu-list">
        <li v-if="enableAdd" class="menu-item" @click="handleContextMenuAction('add')">
          <el-icon><plus /></el-icon>
          <span>新增子节点</span>
        </li>
        <li v-if="enableEdit" class="menu-item" @click="handleContextMenuAction('edit')">
          <el-icon><edit-pen /></el-icon>
          <span>编辑当前节点</span>
        </li>
        <li v-if="enableDelete" class="menu-item danger" @click="handleContextMenuAction('delete')">
          <el-icon><delete /></el-icon>
          <span>删除当前节点</span>
        </li>
      </ul>
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? addDialogTitle : editDialogTitle"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="mergedFormRules"
        label-width="80px"
        @keyup.enter="handleSubmit"
      >
        <!-- 父节点信息 -->
        <div v-if="dialogType === 'add' && parentNode" class="parent-node-info">
          <div class="info-title">父节点</div>
          <div class="info-content">
            <div v-if="showNodeCode && keyMap.code && parentNode[keyMap.code]" class="info-item">
              <span class="label">节点编码:</span>
              <span class="value">{{ parentNode[keyMap.code] }}</span>
            </div>
            <div class="info-item">
              <span class="label">节点名称:</span>
              <span class="value">{{ parentNode[keyMap.label] }}</span>
            </div>
          </div>
        </div>
        
        <template v-for="(field, key) in formFields" :key="key">
          <el-form-item :label="field.label" :prop="key">
            <el-input
              v-model="formData[key]"
              :placeholder="field.placeholder"
              :maxlength="field.maxLength"
              :disabled="dialogType === 'edit' && isFieldDisabledOnEdit(key)"
              show-word-limit
            />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount, nextTick, computed } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus, EditPen, Delete, Search, Folder, FolderOpened, Refresh } from '@element-plus/icons-vue'
import type { ElTree } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

interface TreeNode {
  [key: string]: any
  id: string | number
  children?: TreeNode[]
}

interface KeyMap {
  id: string
  label: string
  code?: string
  children?: string
  parentId?: string
}

interface FormField {
  label: string
  placeholder?: string
  maxLength?: number
  disabledOnEdit?: boolean
}

interface Props {
  treeTitle?: string
  addButtonText?: string
  searchPlaceholder?: string
  addDialogTitle?: string
  editDialogTitle?: string
  showSearch?: boolean
  showSelection?: boolean
  showNodeCode?: boolean
  showAddRoot?: boolean
  showRefreshButton?: boolean
  enableAdd?: boolean
  enableEdit?: boolean
  enableDelete?: boolean
  isLazy?: boolean
  keyMap: KeyMap
  formFields: Record<string, FormField>
  formRules?: FormRules
  disabledEditFields?: string[]
  // API 函数
  loadTreeData?: () => Promise<TreeNode[]>
  loadChildren?: (node: TreeNode) => Promise<TreeNode[]>
  createNode?: (data: any) => Promise<any>
  updateNode?: (id: string | number, data: any) => Promise<any>
  deleteNode?: (id: string | number) => Promise<any>
}

const props = withDefaults(defineProps<Props>(), {
  treeTitle: '树形选择',
  addButtonText: '新增节点',
  searchPlaceholder: '搜索节点...',
  addDialogTitle: '新增节点',
  editDialogTitle: '编辑节点',
  showSearch: true,
  showSelection: true,
  showNodeCode: true,
  showAddRoot: true,
  showRefreshButton: true,
  enableAdd: true,
  enableEdit: true,
  enableDelete: true,
  isLazy: false,
  keyMap: () => ({
    id: 'id',
    label: 'name',
    code: 'code',
    children: 'children',
    parentId: 'parentId'
  }),
  disabledEditFields: () => []
})

const emit = defineEmits<{
  (e: 'select', node: TreeNode | null): void
  (e: 'created', node: any): void
  (e: 'updated', node: any): void
  (e: 'deleted', node: TreeNode): void
  (e: 'refresh'): void
}>()

// 树实例
const treeRef = ref<InstanceType<typeof ElTree>>()
// 搜索关键词
const searchKeyword = ref('')
// 树形数据
const treeData = ref<TreeNode[]>([])
// 树节点配置
const treeProps = computed(() => ({
  children: props.keyMap.children || 'children',
  label: props.keyMap.label,
  isLeaf: (data: TreeNode) => {
    const childrenKey = props.keyMap.children || 'children'
    const children = (data as any)[childrenKey] as TreeNode[] | undefined
    return !children || children.length === 0
  }
}))

// 默认展开的节点keys
const defaultExpandedKeys = ref<(string | number)[]>([])
// 展开的节点keys
const expandedKeys = ref<(string | number)[]>([])

// 对话框控制
const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const submitting = ref(false)
const parentNode = ref<TreeNode | null>(null)

// 表单实例
const formRef = ref<FormInstance>()
// 表单数据
const formData = ref<Record<string, any>>({})

// 合并表单校验规则
const mergedFormRules = computed(() => ({
  ...props.formRules
}))

// 右键菜单控制
const contextMenuVisible = ref(false)
const contextMenuPosition = ref({ x: 0, y: 0 })
const currentContextNode = ref<TreeNode | null>(null)

// 当前选中节点
const currentNode = ref<TreeNode | null>(null)

// 判断字段是否在编辑时禁用
const isFieldDisabledOnEdit = (fieldName: string): boolean => {
  if (props.disabledEditFields.includes(fieldName)) {
    return true
  }
  
  const field = props.formFields[fieldName]
  return !!field.disabledOnEdit
}

// 监听搜索关键词变化
watch(searchKeyword, (val) => {
  if (val) {
    nextTick(() => {
      const allNodes = getAllTreeNodes()
      allNodes.forEach(node => {
        if (!expandedKeys.value.includes(node[props.keyMap.id])) {
          expandedKeys.value.push(node[props.keyMap.id])
        }
      })
    })
  }
  treeRef.value?.filter(val)
})

// 获取所有树节点
const getAllTreeNodes = (nodes = treeData.value, result: TreeNode[] = []): TreeNode[] => {
  nodes.forEach(node => {
    result.push(node)
    const childrenKey = props.keyMap.children || 'children'
    const children = (node as any)[childrenKey] as TreeNode[] | undefined
    if (children && children.length > 0) {
      getAllTreeNodes(children, result)
    }
  })
  return result
}

// 节点过滤方法
const filterNode = (value: string, data: TreeNode) => {
  if (!value) return true
  const code = props.keyMap.code ? data[props.keyMap.code] : ''
  const label = data[props.keyMap.label] || ''
  return (code && code.includes(value)) || label.includes(value)
}

// 处理节点展开
const handleNodeExpand = (data: TreeNode) => {
  const id = data[props.keyMap.id]
  if (id !== undefined && !expandedKeys.value.includes(id)) {
    expandedKeys.value.push(id)
  }
}

// 处理节点收起
const handleNodeCollapse = (data: TreeNode) => {
  const id = data[props.keyMap.id]
  if (id !== undefined) {
    const index = expandedKeys.value.indexOf(id)
    if (index > -1) {
      expandedKeys.value.splice(index, 1)
    }
  }
}

// 懒加载节点
const loadNode = async (node: any, resolve: (data: TreeNode[]) => void) => {
  try {
    if (node.level === 0) {
      if (!props.loadTreeData) {
        resolve([])
        return
      }
      const data = await props.loadTreeData()
      defaultExpandedKeys.value = data.map(item => (item as any)[props.keyMap.id])
      expandedKeys.value = [...defaultExpandedKeys.value]
      resolve(data)
    } else {
      if (!props.loadChildren) {
        resolve([])
        return
      }
      const children = await props.loadChildren(node.data)
      resolve(children)
    }
  } catch (error) {
    console.error('加载节点失败:', error)
    ElMessage.error('加载节点失败')
    resolve([])
  }
}

// 新增根节点
const handleAddRoot = () => {
  dialogType.value = 'add'
  parentNode.value = null
  formData.value = {}
  dialogVisible.value = true
}

// 新增子节点
const handleAdd = (data: TreeNode) => {
  dialogType.value = 'add'
  parentNode.value = data
  const parentIdKey = props.keyMap.parentId || 'parentId'
  formData.value = {
    [parentIdKey]: data[props.keyMap.id]
  }
  dialogVisible.value = true
}

// 编辑节点
const handleEdit = (data: TreeNode) => {
  dialogType.value = 'edit'
  parentNode.value = null
  formData.value = { ...data }
  dialogVisible.value = true
}

// 删除节点
const handleDelete = async (data: TreeNode) => {
  if (!props.deleteNode) return
  
  try {
    await ElMessageBox.confirm(
      `确定要删除"${data[props.keyMap.label]}"吗？删除后无法恢复！`,
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    
    await props.deleteNode(data[props.keyMap.id])
    ElMessage.success('删除成功')
    emit('deleted', data)
    refreshTreeData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除节点失败:', error)
      ElMessage.error('删除节点失败')
    }
  }
}

// 刷新树数据
const refreshTreeData = () => {
  treeData.value = []
  nextTick(() => {
    loadNode({ level: 0 }, (data) => {
      treeData.value = data
    })
  })
  emit('refresh')
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    if (dialogType.value === 'add' && props.createNode) {
      await props.createNode(formData.value)
      ElMessage.success('新增成功')
      emit('created', formData.value)
    } else if (dialogType.value === 'edit' && props.updateNode) {
      await props.updateNode(formData.value[props.keyMap.id], formData.value)
      ElMessage.success('更新成功')
      emit('updated', formData.value)
    }
    
    dialogVisible.value = false
    refreshTreeData()
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    submitting.value = false
  }
}

// 节点点击事件
const handleNodeClick = (data: TreeNode) => {
  currentNode.value = data
  emit('select', data)
}

// 清除选中节点
const clearSelection = () => {
  treeRef.value?.setCurrentKey(undefined)
  currentNode.value = null
  emit('select', null)
}

// 处理右键菜单
const handleContextMenu = (event: MouseEvent, data: TreeNode) => {
  if (!props.enableAdd && !props.enableEdit && !props.enableDelete) return
  
  event.preventDefault()
  event.stopPropagation()
  
  contextMenuPosition.value = {
    x: event.clientX,
    y: event.clientY
  }
  
  currentContextNode.value = data
  contextMenuVisible.value = true
}

// 处理右键菜单操作
const handleContextMenuAction = (action: 'add' | 'edit' | 'delete') => {
  if (!currentContextNode.value) return
  
  switch (action) {
    case 'add':
      handleAdd(currentContextNode.value)
      break
    case 'edit':
      handleEdit(currentContextNode.value)
      break
    case 'delete':
      handleDelete(currentContextNode.value)
      break
  }
  
  contextMenuVisible.value = false
}

// 点击其他区域关闭右键菜单
const handleClickOutside = () => {
  contextMenuVisible.value = false
}

// 挂载和卸载全局事件
onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  document.addEventListener('contextmenu', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
  document.removeEventListener('contextmenu', handleClickOutside)
})

// 暴露方法给父组件
defineExpose({
  clearSelection,
  refreshTreeData
})
</script>

<style lang="scss" scoped>
.base-tree-select {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: var(--el-bg-color);
  border-radius: 8px;
  padding: 16px;
  gap: 16px;
  
  .tree-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--el-border-color-light);
    
    .tree-title {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }
    
    .header-actions {
      display: flex;
      gap: 8px;
    }
  }
  
  .tree-search {
    padding: 0 0 8px;
    
    :deep(.el-input) {
      .el-input__wrapper {
        box-shadow: 0 0 0 1px var(--el-border-color) inset;
        
        &:hover {
          box-shadow: 0 0 0 1px var(--el-border-color-darker) inset;
        }
        
        &.is-focus {
          box-shadow: 0 0 0 1px var(--el-color-primary) inset;
        }
      }
    }
  }
  
  .tree-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 12px;
    min-height: 0;
    
    .current-selection {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 0;
      
      .selection-label {
        color: var(--el-text-color-secondary);
        font-size: 13px;
      }
      
      .selection-tag {
        margin: 0;
      }
    }
    
    :deep(.el-scrollbar) {
      flex: 1;
      min-height: 0;
      
      .el-scrollbar__wrap {
        padding-right: 8px;
      }
    }
    
    .custom-tree {
      background: transparent;
      
      :deep(.el-tree-node) {
        &.is-current > .el-tree-node__content {
          background-color: var(--el-color-primary-light-9);
          color: var(--el-color-primary);
        }
        
        .el-tree-node__content {
          height: 40px;
          border-radius: 4px;
          margin: 2px 0;
          
          &:hover {
            background-color: var(--el-fill-color-light);
          }
        }
      }
      
      .custom-tree-node {
        flex: 1;
        display: flex;
        align-items: center;
        padding-right: 8px;
        
        .node-content {
          display: flex;
          align-items: center;
          gap: 8px;
          
          .node-icon {
            font-size: 18px;
            color: var(--el-color-info);
            transition: transform 0.3s;
            
            &.is-expanded {
              transform: rotate(0deg);
              color: var(--el-color-primary);
            }
          }
          
          .node-labels {
            display: flex;
            align-items: center;
            gap: 8px;
            
            .node-code {
              color: var(--el-text-color-secondary);
              font-size: 12px;
              background-color: var(--el-fill-color);
              padding: 2px 6px;
              border-radius: 3px;
            }
            
            .node-name {
              color: var(--el-text-color-regular);
              font-size: 14px;
            }
          }
        }
        
        &.is-current {
          .node-icon {
            color: var(--el-color-primary);
          }
          
          .node-labels {
            .node-code {
              background-color: var(--el-color-primary-light-8);
              color: var(--el-color-primary);
            }
            
            .node-name {
              color: var(--el-color-primary);
              font-weight: 500;
            }
          }
        }
      }
    }
  }
}

// 右键菜单样式
.context-menu {
  position: fixed;
  z-index: 2000;
  background: var(--el-bg-color);
  border-radius: 4px;
  box-shadow: var(--el-box-shadow-light);
  padding: 4px 0;
  min-width: 160px;
  
  .menu-list {
    margin: 0;
    padding: 0;
    list-style: none;
    
    .menu-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 16px;
      cursor: pointer;
      color: var(--el-text-color-regular);
      font-size: 14px;
      transition: all 0.3s;
      
      .el-icon {
        font-size: 16px;
      }
      
      &:hover {
        background-color: var(--el-color-primary-light-9);
        color: var(--el-color-primary);
      }
      
      &.danger {
        color: var(--el-color-danger);
        
        &:hover {
          background-color: var(--el-color-danger-light-9);
        }
      }
    }
  }
}

// 对话框中的父节点信息样式
.parent-node-info {
  margin-bottom: 20px;
  padding: 12px;
  background-color: var(--el-fill-color-light);
  border-radius: 4px;
  
  .info-title {
    font-size: 14px;
    color: var(--el-text-color-secondary);
    margin-bottom: 8px;
  }
  
  .info-content {
    display: flex;
    flex-direction: column;
    gap: 8px;
    
    .info-item {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .label {
        color: var(--el-text-color-secondary);
        font-size: 13px;
        width: 70px;
      }
      
      .value {
        color: var(--el-text-color-primary);
        font-size: 14px;
        font-weight: 500;
      }
    }
  }
}
</style> 