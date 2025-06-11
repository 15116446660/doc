<template>
  <div class="project-list-container">
    <div class="project-list-layout" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
      <!-- 左侧分类树 -->
      <div class="project-type-sidebar" :class="{ 'collapsed': sidebarCollapsed }">
        <div v-if="!sidebarCollapsed" class="sidebar-content">
          <project-type-tree ref="treeRef" @select="handleTypeSelect" />
        </div>
      </div>

      <!-- 右侧列表 -->
      <div class="project-list-main">
        <base-list
          ref="listRef"
          card-layout="vertical"
          :filter-config="filterConfig"
          :columns="columns"
          :enable-advanced-filter="true"
          :request-api="getProjectList"
          :table-props="tableProps"
          :card-config="cardConfig"
          :view-type="viewType"
          :pagination-config="paginationConfig"
          :show-filter-bar="true"
          @filter-change="handleFilterChange"
          @selection-change="handleSelectionChange"
          @data-loaded="handleDataLoaded"
          @view-change="handleViewChange"
          @action-command="handleActionCommand"
        >
          <template #header-left>
            <div class="list-header-left">
              <el-button
                :icon="sidebarCollapsed ? ArrowRight : ArrowLeft"
                text
                circle
                @click="toggleSidebar"
                class="sidebar-toggle-btn"
              />
              <h2 class="list-title">项目列表</h2>
            </div>
          </template>
          <!-- 工具栏插槽 -->
          <template #toolbar>
            <div class="toolbar-left">
              <el-button type="primary" @click="handleCreateProject">
                <el-icon><Plus /></el-icon>创建项目
              </el-button>
            </div>
          </template>

          <!-- 自定义列插槽 -->
          <template #project-name="{ row }">
            <div class="project-name" @click="handleViewDocuments(row)">
              <span class="name">{{ row.name }}</span>
            </div>
          </template>

          <template #director="{ row }">
            <div class="user-info">
              <el-avatar :size="24" :src="row.directorHeadImg">
                {{ row.directorName?.charAt(0) }}
              </el-avatar>
              <span>{{ row.directorName }}</span>
            </div>
          </template>

          <template #status="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
          </template>

          <template #priority="{ row }">
            <el-tag :type="getPriorityType(row.priority)">{{ row.priority }}</el-tag>
          </template>

          <!-- 卡片视图插槽 -->
          <template #card="{ item }">
            <project-card 
              :project="item"
              @view-documents="handleViewDocuments"
              @edit="handleEdit"
              @delete="handleDelete"
              @members="handleManageMembers"
              @constants="handleManageConstants"
            />
          </template>
        </base-list>
      </div>
    </div>
    
    <!-- 项目创建/编辑对话框 -->
    <project-create-dialog
      v-model="createDialogVisible"
      :initial-data="createDialogData"
      @submit="handleDialogSubmit"
      @error="handleDialogError"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  Plus, 
  ArrowLeft, 
  ArrowRight,
  User,
  Tickets,
  EditPen,
  Delete,
  View,
} from '@element-plus/icons-vue'
import BaseList from '@/components/BaseList/index.vue'
import ProjectCreateDialog from './dialogs/ProjectCreateDialog.vue'
import ProjectTypeTree from './components/ProjectTypeTree.vue'
import ProjectCard from './components/ProjectCard.vue'
import type { FilterFormItem, TableColumn, CardConfig, ViewType, ActionItem } from '@/components/BaseList/types'
import { getProjectList, getProjectStatusOptions } from '@/api/document'
import type { Project, ProjectType } from '@/types/document'

const router = useRouter()

// 列表实例
const listRef = ref()
const treeRef = ref()

// 视图模式
const viewType = ref<ViewType>('table')

// 对话框控制
const createDialogVisible = ref(false)
const createDialogData = ref<Partial<Project>>()

// 侧边栏折叠状态
const sidebarCollapsed = ref(false)

// 表格数据
const tableData = ref<Project[]>([])

// 表格操作配置
const projectActions: ActionItem[] = [
  { label: '查看文档', command: 'view-documents', icon: View },
  { label: '成员', command: 'members', icon: User },
  { label: '项目常量', command: 'constants', icon: Tickets },
  { label: '编辑', command: 'edit', icon: EditPen },
  { label: '删除', command: 'delete', icon: Delete, divided: true }
]

// 处理查看文档
const handleViewDocuments = (project: Project) => {
  router.push({
    name: 'DocumentList',
    params: { projectId: project.id },
    query: { projectName: project.name }
  })
}

// 处理创建项目
const handleCreateProject = () => {
  createDialogData.value = {
    status: 'SURVEY',
    priority: 'P2',
    startTime: new Date().toISOString().split('T')[0],
    endTime: ''
  }
  createDialogVisible.value = true
}

// 处理行内编辑
const handleEdit = (project: Project) => {
  createDialogData.value = { ...project }
  createDialogVisible.value = true
}

// 处理行内删除
const handleDelete = (project: Project) => {
  console.log('Delete project:', project.id)
  // ElMessageBox.confirm(...)
}

// 处理成员管理
const handleManageMembers = (project: Project) => {
  console.log('Manage members for:', project.id)
}

// 处理项目常量
const handleManageConstants = (project: Project) => {
  console.log('Manage constants for:', project.id)
}

// 统一处理指令
const handleActionCommand = (event: { command: string, row: Project }) => {
  switch (event.command) {
    case 'view-documents':
      handleViewDocuments(event.row)
      break
    case 'edit':
      handleEdit(event.row)
      break
    case 'delete':
      handleDelete(event.row)
      break
    case 'members':
      handleManageMembers(event.row)
      break
    case 'constants':
      handleManageConstants(event.row)
      break
  }
}

// 处理对话框提交
const handleDialogSubmit = (formData: Project) => {
  console.log('项目创建/更新成功，表单数据:', formData)
  listRef.value?.refresh()
}

// 处理对话框错误
const handleDialogError = (error: Error) => {
  console.error('创建/更新项目失败:', error)
}

// 处理分类选择
const handleTypeSelect = (type: ProjectType | null) => {
  console.log('Selected type:', type)
  if (type) {
    listRef.value?.refresh({ categoryId: type.id })
  } else {
    listRef.value?.refresh({ categoryId: undefined })
  }
}

// 切换侧边栏折叠状态
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

// 处理视图切换
const handleViewChange = (type: ViewType) => {
  viewType.value = type
}

// 处理过滤条件变化
const handleFilterChange = (filters: Record<string, any>) => {
  console.log('Filter changed:', filters)
}

// 处理选择变化
const handleSelectionChange = (selection: Project[]) => {
  console.log('Selection changed:', selection)
}

// 处理数据加载完成
const handleDataLoaded = (data: any) => {
  // 处理API返回的数据结构，提取真正的记录列表
  if (data && data.records) {
    tableData.value = data.records
  } else if (Array.isArray(data)) {
    tableData.value = data
  } else {
    tableData.value = []
  }
}

// 表格列配置
const columns = ref<TableColumn[]>([
  {
    type: 'index',
    label: '序号',
    width: 55,
    fixed: 'left',
    align: 'center',
    headerAlign: 'center',
  },
  {
    prop: 'name',
    label: '项目名称',
    minWidth: 180,
    align: 'left',
    headerAlign: 'center',
    fixed: 'left',
    slot: 'project-name',
    showOverflowTooltip: true
  },
  {
    prop: 'projectNum',
    label: '项目图号',
    width: 120,
    align: 'center',
    headerAlign: 'center',
  },
  {
    prop: 'status',
    label: '状态',
    width: 100,
    slot: 'status',
    align: 'center',
    headerAlign: 'center',
  },
  {
    prop: 'priority',
    label: '优先级',
    width: 100,
    slot: 'priority',
    align: 'center',
    headerAlign: 'center',
    sortable: 'custom'
  },
  {
    prop: 'directorName',
    label: '负责人',
    width: 120,
    slot: 'director',
    align: 'center',
    headerAlign: 'center',
  },
  {
    prop: 'userCount',
    label: '成员数',
    width: 90,
    align: 'center',
    headerAlign: 'center',
  },
  {
    prop: 'documentCount',
    label: '文档数',
    width: 90,
    align: 'center',
    headerAlign: 'center',
  },
  {
    prop: 'startTime',
    label: '开始时间',
    width: 150,
    align: 'center',
    headerAlign: 'center',
    sortable: 'custom'
  },
  {
    prop: 'endTime',
    label: '结束时间',
    width: 150,
    align: 'center',
    headerAlign: 'center',
  },
  {
    prop: 'createTime',
    label: '创建时间',
    width: 180,
    align: 'center',
    headerAlign: 'center',
    sortable: 'custom'
  },
  {
    prop: 'creator',
    label: '创建人',
    width: 120,
    align: 'center',
    headerAlign: 'center',
  },
  {
    label: '操作',
    width: 200,
    align: 'center',
    headerAlign: 'center',
    fixed: 'right',
    actions: projectActions,
    maxVisibleActions: 1
  }
])

// 过滤条件配置
const filterConfig = ref<FilterFormItem[]>([
  {
    type: 'input',
    field: 'keyword',
    label: '关键词',
    placeholder: '项目名称/负责人'
  },
  {
    type: 'select',
    field: 'priority',
    label: '优先级',
    placeholder: '请选择优先级',
    options: [
      { label: 'P0 - 最高', value: 'P0' },
      { label: 'P1 - 高', value: 'P1' },
      { label: 'P2 - 中', value: 'P2' },
      { label: 'P3 - 低', value: 'P3' }
    ]
  },
  {
    type: 'select',
    field: 'status',
    label: '状态',
    placeholder: '请选择状态',
    props: {
      filterable: true,
      remote: false,
      reserveKeyword: true,
      loading: false
    },
    options: []
  },
  {
    type: 'daterange',
    field: 'dateRange',
    label: '创建时间',
    advanced: true
  }
])

// 表格属性
const tableProps = {
  border: true,
  stripe: true,
  'row-key': 'id',
  'header-cell-style': {
    background: 'var(--el-fill-color-light)',
    color: 'var(--el-text-color-primary)'
  }
}

// 表格视图分页配置
const tablePaginationConfig = {
  pageSize: 10,
  pageSizes: [10, 20, 50, 100],
  layout: 'total, sizes, prev, pager, next, jumper',
  background: true
}

// 卡片视图分页配置
const cardPaginationConfig = {
  pageSize: 12,
  pageSizes: [12, 24, 36, 48],
  layout: 'total, sizes, prev, pager, next, jumper',
  background: true
}

// 根据视图类型获取分页配置
const paginationConfig = computed(() => {
  return viewType.value === 'cards' ? cardPaginationConfig : tablePaginationConfig
})

// 卡片配置
const cardConfig: CardConfig = {
  gridFillMode: 'auto-fill',
  gap: '30px',
  minWidth: '200px',
  maxWidth: '220px',
  minHeight: '120px',
  maxHeight: '120px'
}

// 获取状态类型
const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    'SURVEY': 'info',     // 调研中
    'ONGOING': 'success', // 进行中
    'COMPLETED': '',      // 已完成
    'PAUSED': 'warning',  // 已暂停
    'CANCELLED': 'danger' // 已取消
  }
  return typeMap[status] || 'info'
}

// 获取优先级类型
const getPriorityType = (priority: string) => {
  const typeMap: Record<string, string> = {
    'P0': 'danger',  // 最高
    'P1': 'warning', // 高
    'P2': 'success', // 中
    'P3': 'info'     // 低
  }
  return typeMap[priority] || 'info'
}

onMounted(async () => {
  // Find the status filter item in the config
  const statusFilter = filterConfig.value.find(item => item.field === 'status')
  if (statusFilter && statusFilter.props) {
    statusFilter.props.loading = true
    try {
      const options = await getProjectStatusOptions()
      // Make sure options is an array before assigning
      if (Array.isArray(options)) {
        statusFilter.options = options
      } else {
        console.error("getProjectStatusOptions did not return an array:", options)
        statusFilter.options = []
      }
    } catch (error) {
      console.error('Failed to load project status options:', error)
      statusFilter.options = [] // Ensure it's an array on error
    } finally {
      statusFilter.props.loading = false
    }
  }
})
</script>

<style lang="scss" scoped>
.project-list-container {
  height: 100%;
  padding: 20px;
  background-color: var(--el-bg-color);
  border-radius: 8px;
}

.project-list-layout {
  height: 100%;
  display: flex;
  gap: 20px;
  position: relative;
  transition: gap 0.3s ease-in-out;

  &.sidebar-collapsed {
    gap: 0;
  }
}

.project-type-sidebar {
  width: 280px;
  flex-shrink: 0;
  position: relative;
  transition: all 0.3s ease;
  background-color: var(--el-bg-color-overlay);
  border-radius: 8px;
  box-shadow: var(--el-box-shadow-light);
  overflow: hidden;

  &.collapsed {
    width: 0px;
    flex-shrink: 0;
  }

  .sidebar-content {
    height: 100%;
    overflow: hidden;
  }
}

.project-list-main {
  flex: 1;
  min-width: 0;
  background-color: var(--el-bg-color-overlay);
  border-radius: 8px;
  // box-shadow: var(--el-box-shadow-light);
}

.project-name {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: var(--el-color-primary);

  &:hover {
    .name {
      text-decoration: underline;
    }
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.project-title {
  cursor: pointer;
  color: var(--el-color-primary);
  
  &:hover {
    text-decoration: underline;
  }
}

.el-button+.el-button {
    margin-left: 0px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.danger-item {
  color: var(--el-color-danger);
}

.danger-item:hover {
  background-color: var(--el-color-danger-light-9);
  color: var(--el-color-danger);
}

.list-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.list-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.sidebar-toggle-btn {
  font-size: 18px;
  color: var(--el-text-color-regular);
  &:hover {
    color: var(--el-color-primary);
    background-color: var(--el-color-primary-light-9);
  }
}
</style>
