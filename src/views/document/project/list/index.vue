<template>
  <div class="project-list-container">
    <div class="project-list-layout">
      <!-- 左侧分类树 -->
      <div class="project-type-sidebar" :class="{ 'collapsed': sidebarCollapsed }">
        <div v-if="!sidebarCollapsed" class="sidebar-content">
          <project-type-tree ref="treeRef" @select="handleTypeSelect" />
        </div>
        <div class="sidebar-toggle" @click="toggleSidebar">
          <el-icon :size="20">
            <arrow-left v-if="!sidebarCollapsed" />
            <arrow-right v-else />
          </el-icon>
        </div>
      </div>

      <!-- 右侧列表 -->
      <div class="project-list-main">
        <base-list
          ref="listRef"
          title="项目列表"
          card-layout="vertical"
          :filter-config="filterConfig"
          :columns="columns"
          :enable-advanced-filter="true"
          :request-api="getProjectList"
          :table-props="tableProps"
          :pagination-config="paginationConfig"
          :show-filter-bar="true"
          :card-config="cardConfig"
          @filter-change="handleFilterChange"
          @selection-change="handleSelectionChange"
          @data-loaded="handleDataLoaded"
        >
          <!-- 工具栏插槽 -->
          <template #toolbar>
            <div class="toolbar-left">
              <el-button type="primary" @click="handleCreateProject">
                <el-icon><Plus /></el-icon>创建项目
              </el-button>
              <div class="view-toggle">
                <el-radio-group v-model="viewType" @change="handleViewChange" size="small">
                  <el-radio-button label="table">表格</el-radio-button>
                  <el-radio-button label="cards">卡片</el-radio-button>
                </el-radio-group>
              </div>
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

          <template #actions="{ row }">
            <el-button
              type="primary"
              link
              @click="handleViewDocuments(row)"
            >
              查看文档
            </el-button>
          </template>

          <!-- 卡片视图插槽 -->
          <template #card="{ item }">
            <project-card :project="item" />
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
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import BaseList from '@/components/BaseList/index.vue'
import ProjectCreateDialog from './dialogs/ProjectCreateDialog.vue'
import ProjectTypeTree from './components/ProjectTypeTree.vue'
import ProjectCard from './components/ProjectCard.vue'
import type { FilterFormItem, OptionItem, TableColumn, CardContainerConfig } from '@/components/BaseList/types'
import { getProjectList, getProjectStatusOptions } from '@/api/document'
import type { Project, ProjectType } from '@/types/document'

const router = useRouter()

// 列表实例
const listRef = ref()
const treeRef = ref()

// 视图模式
const viewType = ref('table')

// 对话框控制
const createDialogVisible = ref(false)
const createDialogData = ref<Partial<Project>>()

// 侧边栏折叠状态
const sidebarCollapsed = ref(false)

// 表格数据
const tableData = ref<Project[]>([])

// 表格列配置
const columns = ref<TableColumn[]>([
  {
    type: 'index',
    label: '序号',
    width: 55,
    fixed: 'left',
    align: 'center'
  },
  {
    prop: 'projectNum',
    label: '项目图号',
    width: 120,
    fixed: 'left',
    align: 'center'
  },
  {
    prop: 'name',
    label: '项目名称',
    minWidth: 180,
    align: 'left',
    slot: 'project-name',
    showOverflowTooltip: true
  },
  {
    prop: 'status',
    label: '状态',
    width: 100,
    slot: 'status'
  },
  {
    prop: 'priority',
    label: '优先级',
    width: 100,
    slot: 'priority'
  },
  {
    prop: 'directorName',
    label: '负责人',
    width: 120,
    slot: 'director'
  },
  {
    prop: 'userCount',
    label: '成员数',
    width: 90,
    align: 'center'
  },
  {
    prop: 'documentCount',
    label: '文档数',
    width: 90,
    align: 'center'
  },
  {
    prop: 'startTime',
    label: '开始时间',
    width: 150
  },
  {
    prop: 'endTime',
    label: '结束时间',
    width: 150
  },
  {
    prop: 'createTime',
    label: '创建时间',
    width: 180
  },
  {
    prop: 'creator',
    label: '创建人',
    width: 120
  },
  {
    label: '操作',
    width: 250,
    align: 'center',
    fixed: 'right',
    slot: 'actions'
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
      remote: true,
      reserveKeyword: true,
      loading: false
    },
    options: async () => {
      const res = await getProjectStatusOptions()
      return res as unknown as OptionItem[]
    }
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
const cardConfig: CardContainerConfig = {
  minWidth: '200px',
  gridFillMode: 'auto-fill',
  gap: '10px',
  minHeight: '120px',
  maxHeight: '120px',
  maxWidth: '220px'
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
const handleViewChange = (type: string) => {
  viewType.value = type
}
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
    width: 20px;
    flex-shrink: 0;
  }

  .sidebar-content {
    height: 100%;
    overflow: hidden;
  }

  .sidebar-toggle {
    position: absolute;
    top: 50%;
    right: 0;
    width: 20px;
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: var(--el-color-primary-light-8);
    color: var(--el-color-primary);
    cursor: pointer;
    border-radius: 4px 0 0 4px;
    transform: translateY(-50%);
    transition: all 0.3s;
    z-index: 10;
    box-shadow: -2px 0 8px rgba(0, 0, 0, 0.05);

    &:hover {
      background-color: var(--el-color-primary-light-5);
      color: white;
      width: 24px;
    }
  }
}

.project-list-main {
  flex: 1;
  min-width: 0;
  margin-left: 20px;
  background-color: var(--el-bg-color-overlay);
  border-radius: 8px;
  box-shadow: var(--el-box-shadow-light);
}

.project-name {
  display: flex;
  align-items: center;
  gap: 8px;
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
</style>
