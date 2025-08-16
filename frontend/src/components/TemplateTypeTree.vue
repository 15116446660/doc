<template>
  <div class="template-type-tree">
    <div class="tree-header">
      <h3 class="tree-title">模板分类</h3>
      <el-button type="primary" link @click="handleAddRoot">
        <el-icon><plus /></el-icon>
        新增分类
      </el-button>
    </div>
    
    <div class="tree-search">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索分类..."
        clearable
        :prefix-icon="Search"
        size="default"
      />
    </div>
    
    <div class="tree-content">
      <!-- 当前选中分类 -->
      <div class="current-selection">
        <span class="selection-label">当前选择：</span>
        <el-tag 
          size="small" 
          :closable="!!currentNode" 
          @close="clearSelection"
          :type="currentNode ? 'primary' : 'info'"
          effect="light"
          class="selection-tag"
        >
          {{ currentNode ? currentNode.typeName : '全部' }}
        </el-tag>
      </div>
      <el-scrollbar>
        <el-tree
          ref="treeRef"
          :data="treeData"
          :props="defaultProps"
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
          lazy
          :render-after-expand="false"
          class="custom-tree"
        >
          <template #default="{ data, node }">
            <div class="custom-tree-node" :class="{ 'is-current': node.isCurrent }">
              <div class="node-content">
                <el-icon class="node-icon" :class="{ 'is-expanded': expandedKeys.includes(data.id) }">
                  <folder-opened v-if="expandedKeys.includes(data.id)" />
                  <folder v-else />
                </el-icon>
                <div class="node-labels">
                  <span class="node-code">{{ data.typeCode }}</span>
                  <span class="node-name">{{ data.typeName }}</span>
                </div>
              </div>
            </div>
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
        <li class="menu-item" @click="handleContextMenuAction('add')">
          <el-icon><plus /></el-icon>
          <span>新增子节点</span>
        </li>
        <li class="menu-item" @click="handleContextMenuAction('edit')">
          <el-icon><edit-pen /></el-icon>
          <span>编辑当前节点</span>
        </li>
        <li class="menu-item danger" @click="handleContextMenuAction('delete')">
          <el-icon><delete /></el-icon>
          <span>删除当前节点</span>
        </li>
      </ul>
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增分类' : '编辑分类'"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
        @keyup.enter="handleSubmit"
      >
        <!-- 父节点 -->
        <div v-if="dialogType === 'add' && parentNode" class="parent-node-info">
          <div class="info-title">父节点</div>
          <div class="info-content">
            <div class="info-item">
              <span class="label">节点编码:</span>
              <span class="value">{{ parentNode.typeCode }}</span>
            </div>
            <div class="info-item">
              <span class="label">节点名称:</span>
              <span class="value">{{ parentNode.typeName }}</span>
            </div>
          </div>
        </div>
        
        <el-form-item label="分类编码" prop="typeCode">
          <el-input
            v-model="formData.typeCode"
            placeholder="请输入分类编码"
            :maxlength="20"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="分类名称" prop="typeName">
          <el-input
            v-model="formData.typeName"
            placeholder="请输入分类名称"
            :maxlength="50"
            show-word-limit
          />
        </el-form-item>
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
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus, EditPen, Delete, Search, Folder, FolderOpened } from '@element-plus/icons-vue'
import type { ElTree } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import type { TemplateType } from '@/api/template'
import { getTemplateTypeTree, createTemplateType, updateTemplateType, deleteTemplateType } from '@/api/template'

const emit = defineEmits<{
  (e: 'select', type: TemplateType | null): void
}>()

// 树实例
const treeRef = ref<InstanceType<typeof ElTree>>()
// 搜索关键词
const searchKeyword = ref('')
// 树形数据
const treeData = ref<TemplateType[]>([])
// 树节点配置
const defaultProps = {
  children: 'children',
  label: 'typeName',
  isLeaf: (data: TemplateType) => !data.children || data.children.length === 0
}

// 默认展开的节点keys
const defaultExpandedKeys = ref<number[]>([])
// 展开的节点keys
const expandedKeys = ref<number[]>([])

// 对话框控制
const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const submitting = ref(false)
const parentNode = ref<TemplateType | null>(null)

// 表单实例
const formRef = ref<FormInstance>()
// 表单数据
const formData = ref<Partial<TemplateType>>({
  typeCode: '',
  typeName: ''
})
// 表单校验规则
const formRules: FormRules = {
  typeCode: [
    { required: true, message: '请输入分类编码', trigger: 'blur' },
    { pattern: /^[A-Z0-9]+$/, message: '分类编码只能包含大写字母和数字', trigger: 'blur' }
  ],
  typeName: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

// 右键菜单控制
const contextMenuVisible = ref(false)
const contextMenuPosition = ref({ x: 0, y: 0 })
const currentContextNode = ref<TemplateType | null>(null)

// 当前选中节点
const currentNode = ref<TemplateType | null>(null)

// 监听搜索关键词变化
watch(searchKeyword, (val) => {
  if (val) {
    // 搜索时展开所有节点以显示匹配结果
    nextTick(() => {
      // 手动展开所有节点
      const allNodes = getAllTreeNodes()
      allNodes.forEach(node => {
        if (!expandedKeys.value.includes(node.id)) {
          expandedKeys.value.push(node.id)
        }
      })
    })
  }
  treeRef.value?.filter(val)
})

// 获取所有树节点
const getAllTreeNodes = (nodes = treeData.value, result: TemplateType[] = []): TemplateType[] => {
  nodes.forEach(node => {
    result.push(node)
    if (node.children && node.children.length > 0) {
      getAllTreeNodes(node.children, result)
    }
  })
  return result
}

// 节点过滤方法
const filterNode = (value: string, data: TemplateType) => {
  if (!value) return true
  return data.typeCode.includes(value) || data.typeName.includes(value)
}

// 处理节点展开
const handleNodeExpand = (data: TemplateType) => {
  if (!expandedKeys.value.includes(data.id)) {
    expandedKeys.value.push(data.id)
  }
}

// 处理节点收起
const handleNodeCollapse = (data: TemplateType) => {
  const index = expandedKeys.value.indexOf(data.id)
  if (index > -1) {
    expandedKeys.value.splice(index, 1)
  }
}

// 懒加载节点
const loadNode = (node: any, resolve: (data: TemplateType[]) => void) => {
  // 根节点
  if (node.level === 0) {
    // 加载一级节点
    getTemplateTypeTree().then(data => {
      // 保存顶级节点ID作为默认展开
      defaultExpandedKeys.value = data.map(item => item.id)
      expandedKeys.value = [...defaultExpandedKeys.value]
      resolve(data)
    }).catch(() => {
      ElMessage.error('加载模板分类失败')
      resolve([])
    })
  } else {
    // 如果节点已经有children属性且不为空，直接使用
    if (node.data.children && node.data.children.length > 0) {
      resolve(node.data.children)
      return
    }
    
    // 模拟异步加载子节点
    setTimeout(() => {
      // 这里应该调用真实API获取子节点，这里仅作演示
      const children = node.data.children || []
      resolve(children)
    }, 100)
  }
}

// 新增根节点
const handleAddRoot = () => {
  dialogType.value = 'add'
  parentNode.value = null
  formData.value = {
    typeCode: '',
    typeName: ''
  }
  dialogVisible.value = true
}

// 新增子节点
const handleAdd = (data: TemplateType) => {
  dialogType.value = 'add'
  parentNode.value = data
  formData.value = {
    typeCode: '',
    typeName: '',
    parentId: data.id
  }
  dialogVisible.value = true
}

// 编辑节点
const handleEdit = (data: TemplateType) => {
  dialogType.value = 'edit'
  parentNode.value = null
  formData.value = {
    ...data
  }
  dialogVisible.value = true
}

// 删除节点
const handleDelete = async (data: TemplateType) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除分类"${data.typeName}"吗？删除后无法恢复！`,
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    
    await deleteTemplateType(data.id)
    ElMessage.success('删除成功')
    // 重新加载数据
    refreshTreeData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除分类失败:', error)
      ElMessage.error('删除分类失败')
    }
  }
}

// 刷新树数据
const refreshTreeData = () => {
  // 重置树数据，触发懒加载重新获取数据
  treeData.value = []
  nextTick(() => {
    loadNode({ level: 0 }, (data) => {
      treeData.value = data
    })
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    if (dialogType.value === 'add') {
      await createTemplateType(formData.value)
      ElMessage.success('新增成功')
    } else {
      await updateTemplateType(formData.value.id!, formData.value)
      ElMessage.success('更新成功')
    }
    
    dialogVisible.value = false
    // 重新加载数据
    refreshTreeData()
  } catch (error) {
    console.error('保存分类失败:', error)
    ElMessage.error('保存失败')
  } finally {
    submitting.value = false
  }
}

// 节点点击事件
const handleNodeClick = (data: TemplateType) => {
  currentNode.value = data
  emit('select', data)
}

// 清除选中节点
const clearSelection = () => {
  // 使用 undefined 代替 null 来取消当前选择
  treeRef.value?.setCurrentKey(undefined)
  currentNode.value = null
  emit('select', null)
}

// 暴露方法给父组件
defineExpose({
  clearSelection
})

// 处理右键菜单
const handleContextMenu = (event: MouseEvent, data: TemplateType) => {
  event.preventDefault()
  event.stopPropagation()
  
  // 设置右键菜单位置
  contextMenuPosition.value = {
    x: event.clientX,
    y: event.clientY
  }
  
  // 设置当前节点
  currentContextNode.value = data
  
  // 显示右键菜单
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
  
  // 隐藏右键菜单
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
</script>

<style lang="scss" scoped>
.template-type-tree {
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
      padding: 8px 12px;
      background-color: var(--el-fill-color-light);
      border-radius: 4px;
      
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