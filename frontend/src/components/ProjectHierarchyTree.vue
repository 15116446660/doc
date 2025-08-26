<template>
  <div class="project-hierarchy-tree">
    <!-- 搜索栏 -->
    <div class="tree-search">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索部门、品类、子品类或项目..."
        prefix-icon="Search"
        clearable
        @input="handleSearch"
        @clear="handleSearchClear"
      />
      <el-select
        v-model="searchType"
        placeholder="搜索类型"
        style="width: 120px; margin-left: 10px"
        @change="handleSearch"
      >
        <el-option label="全部" value="all" />
        <el-option label="部门" value="department" />
        <el-option label="品类" value="category" />
        <el-option label="子品类" value="subcategory" />
        <el-option label="项目" value="project" />
      </el-select>
    </div>

    <!-- 工具栏 -->
    <div class="tree-toolbar">
      <el-button type="primary" size="small" @click="handleRefresh">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
      <el-button size="small" @click="handleExpandAll">
        <el-icon><FolderOpened /></el-icon>
        展开全部
      </el-button>
      <el-button size="small" @click="handleCollapseAll">
        <el-icon><Folder /></el-icon>
        收起全部
      </el-button>
      <el-button size="small" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        新建
      </el-button>
    </div>

    <!-- 树形结构 -->
    <div class="tree-container" v-loading="loading">
      <el-tree
        ref="treeRef"
        :data="treeData"
        :props="treeProps"
        :default-expand-all="false"
        :expand-on-click-node="false"
        :check-on-click-node="false"
        :highlight-current="true"
        :draggable="draggable"
        :allow-drop="allowDrop"
        :allow-drag="allowDrag"
        node-key="id"
        @node-click="handleNodeClick"
        @node-expand="handleNodeExpand"
        @node-collapse="handleNodeCollapse"
        @node-drop="handleNodeDrop"
        @node-contextmenu="handleNodeContextMenu"
      >
        <template #default="{ node, data }">
          <div class="tree-node" :class="getNodeClass(data)">
            <!-- 节点图标 -->
            <el-icon class="node-icon" :style="{ color: data.color }">
              <component :is="getNodeIcon(data)" />
            </el-icon>
            
            <!-- 节点内容 -->
            <div class="node-content">
              <span class="node-name">{{ data.name }}</span>
              <span v-if="data.code" class="node-code">({{ data.code }})</span>
              
              <!-- 节点状态标签 -->
              <div class="node-tags">
                <el-tag
                  v-if="data.nodeType === 'project' && data.projectStatus"
                  :type="getProjectStatusType(data.projectStatus)"
                  size="small"
                >
                  {{ getProjectStatusText(data.projectStatus) }}
                </el-tag>
                <el-tag
                  v-if="data.nodeType === 'project' && data.projectPriority"
                  :type="getProjectPriorityType(data.projectPriority)"
                  size="small"
                >
                  {{ getProjectPriorityText(data.projectPriority) }}
                </el-tag>
                <span v-if="data.childrenCount > 0" class="children-count">
                  ({{ data.childrenCount }})
                </span>
              </div>
            </div>

            <!-- 节点操作按钮 -->
            <div class="node-actions" v-if="showActions">
              <el-button
                type="text"
                size="small"
                @click.stop="handleAddChild(data)"
                v-if="canAddChild(data)"
              >
                <el-icon><Plus /></el-icon>
              </el-button>
              <el-button
                type="text"
                size="small"
                @click.stop="handleEdit(data)"
              >
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button
                type="text"
                size="small"
                @click.stop="handleDelete(data)"
                v-if="canDelete(data)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
        </template>
      </el-tree>
    </div>

    <!-- 右键菜单 -->
    <el-dropdown
      ref="contextMenuRef"
      trigger="manual"
      :style="contextMenuStyle"
      @command="handleContextMenuCommand"
    >
      <span></span>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="add" v-if="contextNode && canAddChild(contextNode)">
            <el-icon><Plus /></el-icon>
            新建子项
          </el-dropdown-item>
          <el-dropdown-item command="edit">
            <el-icon><Edit /></el-icon>
            编辑
          </el-dropdown-item>
          <el-dropdown-item command="copy">
            <el-icon><CopyDocument /></el-icon>
            复制
          </el-dropdown-item>
          <el-dropdown-item command="delete" v-if="contextNode && canDelete(contextNode)" divided>
            <el-icon><Delete /></el-icon>
            删除
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>

    <!-- 创建/编辑对话框 -->
    <project-hierarchy-form-dialog
      v-model="showCreateDialog"
      :node-type="formNodeType"
      :parent-node="formParentNode"
      :edit-data="formEditData"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox, ElTree } from 'element-plus'
import {
  Search,
  Refresh,
  FolderOpened,
  Folder,
  Plus,
  Edit,
  Delete,
  CopyDocument,
  OfficeBuilding,
  Collection,
  Files,
  Document
} from '@element-plus/icons-vue'
import type { ProjectHierarchyTreeNode } from '@/api/project'
import {
  getProjectHierarchyTree,
  searchProjectHierarchy,
  moveHierarchyNode,
  deleteDepartment,
  deleteProjectCategory,
  deleteProjectSubcategory
} from '@/api/project'
import ProjectHierarchyFormDialog from './ProjectHierarchyFormDialog.vue'

// Props
interface Props {
  draggable?: boolean
  showActions?: boolean
  expandAll?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  draggable: true,
  showActions: true,
  expandAll: false
})

// Emits
const emit = defineEmits<{
  nodeClick: [node: ProjectHierarchyTreeNode]
  nodeSelect: [node: ProjectHierarchyTreeNode]
  refresh: []
}>()

// Refs
const treeRef = ref<InstanceType<typeof ElTree>>()
const contextMenuRef = ref()

// Reactive data
const loading = ref(false)
const treeData = ref<ProjectHierarchyTreeNode[]>([])
const searchKeyword = ref('')
const searchType = ref('all')
const showCreateDialog = ref(false)
const formNodeType = ref('')
const formParentNode = ref<ProjectHierarchyTreeNode | null>(null)
const formEditData = ref<ProjectHierarchyTreeNode | null>(null)

// 右键菜单相关
const contextNode = ref<ProjectHierarchyTreeNode | null>(null)
const contextMenuStyle = ref({
  position: 'fixed',
  left: '0px',
  top: '0px',
  zIndex: 9999,
  display: 'none'
})

// 树形组件配置
const treeProps = {
  children: 'children',
  label: 'name',
  disabled: (data: ProjectHierarchyTreeNode) => !data.enabled
}

// 计算属性
const filteredTreeData = computed(() => {
  if (!searchKeyword.value) {
    return treeData.value
  }
  return filterTreeData(treeData.value, searchKeyword.value, searchType.value)
})

// 生命周期
onMounted(() => {
  loadTreeData()
})

// 方法
const loadTreeData = async () => {
  try {
    loading.value = true
    const response = await getProjectHierarchyTree()
    treeData.value = response.data || []
    
    if (props.expandAll) {
      nextTick(() => {
        expandAllNodes()
      })
    }
  } catch (error) {
    console.error('加载项目层级树失败:', error)
    ElMessage.error('加载项目层级树失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    await loadTreeData()
    return
  }

  try {
    loading.value = true
    const response = await searchProjectHierarchy(searchKeyword.value, searchType.value)
    treeData.value = response.data || []
    
    // 展开搜索结果
    nextTick(() => {
      expandAllNodes()
    })
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

const handleSearchClear = () => {
  searchKeyword.value = ''
  loadTreeData()
}

const handleRefresh = () => {
  loadTreeData()
  emit('refresh')
}

const handleExpandAll = () => {
  expandAllNodes()
}

const handleCollapseAll = () => {
  collapseAllNodes()
}

const expandAllNodes = () => {
  const expandKeys: string[] = []
  const collectKeys = (nodes: ProjectHierarchyTreeNode[]) => {
    nodes.forEach(node => {
      expandKeys.push(String(node.id))
      if (node.children && node.children.length > 0) {
        collectKeys(node.children)
      }
    })
  }
  collectKeys(treeData.value)
  
  expandKeys.forEach(key => {
    treeRef.value?.setExpanded(key, true)
  })
}

const collapseAllNodes = () => {
  const collapseKeys: string[] = []
  const collectKeys = (nodes: ProjectHierarchyTreeNode[]) => {
    nodes.forEach(node => {
      collapseKeys.push(String(node.id))
      if (node.children && node.children.length > 0) {
        collectKeys(node.children)
      }
    })
  }
  collectKeys(treeData.value)
  
  collapseKeys.forEach(key => {
    treeRef.value?.setExpanded(key, false)
  })
}

const handleNodeClick = (data: ProjectHierarchyTreeNode) => {
  emit('nodeClick', data)
  emit('nodeSelect', data)
}

const handleNodeExpand = (data: ProjectHierarchyTreeNode) => {
  data.expanded = true
}

const handleNodeCollapse = (data: ProjectHierarchyTreeNode) => {
  data.expanded = false
}

const handleNodeDrop = async (draggingNode: any, dropNode: any, dropType: string) => {
  if (!props.draggable) return

  try {
    const dragData = draggingNode.data as ProjectHierarchyTreeNode
    const dropData = dropNode.data as ProjectHierarchyTreeNode
    
    let targetParentId: number | undefined
    let newSortOrder: number | undefined

    if (dropType === 'inner') {
      targetParentId = dropData.id
    } else {
      targetParentId = dropData.parentId
      // 计算新的排序位置
      const siblings = getSiblings(dropData, treeData.value)
      const dropIndex = siblings.findIndex(item => item.id === dropData.id)
      newSortOrder = dropType === 'before' ? dropIndex : dropIndex + 1
    }

    await moveHierarchyNode(dragData.nodeType, dragData.id, targetParentId, newSortOrder)
    ElMessage.success('移动成功')
    await loadTreeData()
  } catch (error) {
    console.error('移动失败:', error)
    ElMessage.error('移动失败')
    await loadTreeData() // 重新加载以恢复原状态
  }
}

const handleNodeContextMenu = (event: MouseEvent, data: ProjectHierarchyTreeNode) => {
  event.preventDefault()
  contextNode.value = data
  
  contextMenuStyle.value = {
    position: 'fixed',
    left: event.clientX + 'px',
    top: event.clientY + 'px',
    zIndex: 9999,
    display: 'block'
  }
  
  nextTick(() => {
    contextMenuRef.value?.handleOpen()
  })
}

const handleContextMenuCommand = (command: string) => {
  if (!contextNode.value) return

  switch (command) {
    case 'add':
      handleAddChild(contextNode.value)
      break
    case 'edit':
      handleEdit(contextNode.value)
      break
    case 'copy':
      handleCopy(contextNode.value)
      break
    case 'delete':
      handleDelete(contextNode.value)
      break
  }
  
  contextMenuStyle.value.display = 'none'
}

const handleAddChild = (node: ProjectHierarchyTreeNode) => {
  formParentNode.value = node
  formEditData.value = null
  
  // 根据父节点类型确定子节点类型
  switch (node.nodeType) {
    case 'department':
      formNodeType.value = 'category'
      break
    case 'category':
      formNodeType.value = 'subcategory'
      break
    case 'subcategory':
      formNodeType.value = 'project'
      break
    default:
      ElMessage.warning('该节点不支持添加子项')
      return
  }
  
  showCreateDialog.value = true
}

const handleEdit = (node: ProjectHierarchyTreeNode) => {
  formParentNode.value = null
  formEditData.value = node
  formNodeType.value = node.nodeType
  showCreateDialog.value = true
}

const handleCopy = (node: ProjectHierarchyTreeNode) => {
  // 复制节点信息到剪贴板
  const copyData = {
    name: node.name,
    code: node.code,
    description: node.description,
    nodeType: node.nodeType
  }
  
  navigator.clipboard.writeText(JSON.stringify(copyData, null, 2))
    .then(() => {
      ElMessage.success('节点信息已复制到剪贴板')
    })
    .catch(() => {
      ElMessage.error('复制失败')
    })
}

const handleDelete = async (node: ProjectHierarchyTreeNode) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除${getNodeTypeText(node.nodeType)}"${node.name}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 根据节点类型调用相应的删除API
    switch (node.nodeType) {
      case 'department':
        await deleteDepartment(node.id)
        break
      case 'category':
        await deleteProjectCategory(node.id)
        break
      case 'subcategory':
        await deleteProjectSubcategory(node.id)
        break
      case 'project':
        // 调用项目删除API
        break
    }

    ElMessage.success('删除成功')
    await loadTreeData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleFormSuccess = () => {
  showCreateDialog.value = false
  loadTreeData()
}

// 工具方法
const getNodeClass = (data: ProjectHierarchyTreeNode) => {
  return [
    `node-${data.nodeType}`,
    { 'node-disabled': !data.enabled },
    { 'node-selected': data.selected }
  ]
}

const getNodeIcon = (data: ProjectHierarchyTreeNode) => {
  switch (data.nodeType) {
    case 'department':
      return OfficeBuilding
    case 'category':
      return Collection
    case 'subcategory':
      return Files
    case 'project':
      return Document
    default:
      return Document
  }
}

const getNodeTypeText = (nodeType: string) => {
  switch (nodeType) {
    case 'department':
      return '部门'
    case 'category':
      return '品类'
    case 'subcategory':
      return '子品类'
    case 'project':
      return '项目'
    default:
      return '节点'
  }
}

const getProjectStatusType = (status: string) => {
  switch (status) {
    case 'DRAFT':
      return 'info'
    case 'IN_PROGRESS':
      return 'warning'
    case 'COMPLETED':
      return 'success'
    case 'ARCHIVED':
      return 'danger'
    default:
      return 'info'
  }
}

const getProjectStatusText = (status: string) => {
  switch (status) {
    case 'DRAFT':
      return '草稿'
    case 'IN_PROGRESS':
      return '进行中'
    case 'COMPLETED':
      return '已完成'
    case 'ARCHIVED':
      return '已归档'
    default:
      return status
  }
}

const getProjectPriorityType = (priority: string) => {
  switch (priority) {
    case 'LOW':
      return 'info'
    case 'MEDIUM':
      return 'warning'
    case 'HIGH':
      return 'danger'
    case 'URGENT':
      return 'danger'
    default:
      return 'info'
  }
}

const getProjectPriorityText = (priority: string) => {
  switch (priority) {
    case 'LOW':
      return '低'
    case 'MEDIUM':
      return '中'
    case 'HIGH':
      return '高'
    case 'URGENT':
      return '紧急'
    default:
      return priority
  }
}

const canAddChild = (data: ProjectHierarchyTreeNode) => {
  return data.nodeType !== 'project'
}

const canDelete = (data: ProjectHierarchyTreeNode) => {
  return data.childrenCount === 0 || !data.childrenCount
}

const allowDrop = (draggingNode: any, dropNode: any, type: string) => {
  if (!props.draggable) return false
  
  const dragData = draggingNode.data as ProjectHierarchyTreeNode
  const dropData = dropNode.data as ProjectHierarchyTreeNode
  
  // 不能拖拽到自己的子节点
  if (isDescendant(dragData, dropData)) {
    return false
  }
  
  // 根据节点类型限制拖拽
  if (type === 'inner') {
    return canAddChild(dropData)
  }
  
  return true
}

const allowDrag = (draggingNode: any) => {
  return props.draggable
}

const isDescendant = (ancestor: ProjectHierarchyTreeNode, node: ProjectHierarchyTreeNode): boolean => {
  if (!ancestor.children) return false
  
  return ancestor.children.some(child => 
    child.id === node.id || isDescendant(child, node)
  )
}

const getSiblings = (node: ProjectHierarchyTreeNode, tree: ProjectHierarchyTreeNode[]): ProjectHierarchyTreeNode[] => {
  if (!node.parentId) {
    return tree
  }
  
  const findParent = (nodes: ProjectHierarchyTreeNode[]): ProjectHierarchyTreeNode | null => {
    for (const n of nodes) {
      if (n.id === node.parentId) {
        return n
      }
      if (n.children) {
        const found = findParent(n.children)
        if (found) return found
      }
    }
    return null
  }
  
  const parent = findParent(tree)
  return parent?.children || []
}

const filterTreeData = (
  nodes: ProjectHierarchyTreeNode[],
  keyword: string,
  type: string
): ProjectHierarchyTreeNode[] => {
  const filtered: ProjectHierarchyTreeNode[] = []
  
  for (const node of nodes) {
    const matchesKeyword = node.name.toLowerCase().includes(keyword.toLowerCase()) ||
                          (node.code && node.code.toLowerCase().includes(keyword.toLowerCase()))
    const matchesType = type === 'all' || node.nodeType === type
    
    if (matchesKeyword && matchesType) {
      filtered.push({ ...node })
    } else if (node.children && node.children.length > 0) {
      const filteredChildren = filterTreeData(node.children, keyword, type)
      if (filteredChildren.length > 0) {
        filtered.push({
          ...node,
          children: filteredChildren
        })
      }
    }
  }
  
  return filtered
}

// 暴露方法给父组件
defineExpose({
  refresh: loadTreeData,
  expandAll: expandAllNodes,
  collapseAll: collapseAllNodes,
  getSelectedNode: () => treeRef.value?.getCurrentNode(),
  setSelectedNode: (nodeId: string) => treeRef.value?.setCurrentKey(nodeId)
})
</script>

<style scoped>
.project-hierarchy-tree {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.tree-search {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.tree-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.tree-container {
  flex: 1;
  overflow: auto;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  padding: 8px;
}

.tree-node {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.tree-node:hover {
  background-color: var(--el-fill-color-light);
}

.tree-node:hover .node-actions {
  opacity: 1;
}

.node-icon {
  margin-right: 8px;
  font-size: 16px;
}

.node-content {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
}

.node-name {
  font-weight: 500;
}

.node-code {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.node-tags {
  display: flex;
  align-items: center;
  gap: 4px;
}

.children-count {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.node-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.node-actions .el-button {
  padding: 4px;
  min-height: auto;
}

.node-disabled {
  opacity: 0.6;
}

.node-selected {
  background-color: var(--el-color-primary-light-9);
}

.node-department .node-icon {
  color: var(--el-color-primary);
}

.node-category .node-icon {
  color: var(--el-color-success);
}

.node-subcategory .node-icon {
  color: var(--el-color-warning);
}

.node-project .node-icon {
  color: var(--el-color-info);
}
</style>
