<template>
  <div class="template-list-container">
    <div class="template-list-layout" :class="{ 'collapsed': sidebarCollapsed }">
      <!-- 左侧分类树 -->
      <div class="template-type-sidebar" :class="{ 'collapsed': sidebarCollapsed }">
        <div v-if="!sidebarCollapsed" class="sidebar-content">
          <template-type-tree ref="treeRef" @select="handleTypeSelect" />
        </div>
      </div>

      <!-- 右侧列表 -->
      <div class="template-list-main">
        <base-list
          ref="listRef"
          :filter-config="filterConfig"
          :columns="columns"
          :enable-advanced-filter="true"
          :enable-view-switch="true"
          :view-type="viewType"
          :request-api="getTemplateList"
          :table-props="tableProps"
          :pagination-config="paginationConfig"
          :card-layout="cardLayout"
          :card-config="cardConfig"
          :show-filter-bar="false"
          @filter-change="handleFilterChange"
          @view-change="handleViewChange"
          @selection-change="handleSelectionChange"
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
              <h2 class="list-title">模板列表</h2>
            </div>
          </template>
          <!-- 顶部工具栏插槽 -->
          <template #toolbar>
            <el-button type="primary" @click="handleCreateTemplate">
              <el-icon><plus /></el-icon>新建模板
            </el-button>
          </template>
          
          <!-- 模板名称自定义插槽 -->
          <template #template-name="{ row }">
            <div class="template-name" @click="handleViewTemplate(row)">
              <span class="template-title">{{ row.name || row.title || '未命名模板' }}</span>
            </div>
          </template>

          <!-- 状态自定义插槽 -->
          <template #status="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.status || '未设置' }}</el-tag>
          </template>
          
          <!-- 审核状态自定义插槽 -->
          <template #review-status="{ row }">
            <el-tag v-if="row.reviewStatus" type="warning">{{ row.reviewName || row.reviewStatus }}</el-tag>
            <span v-else>-</span>
          </template>

          <!-- 启用状态自定义插槽 -->
          <template #enable-status="{ row }">
            <el-tag :type="row.enableStatus ? 'success' : 'info'">
              {{ row.enableStatus ? '已启用' : '未启用' }}
            </el-tag>
          </template>

          <!-- 分类自定义插槽 -->
          <template #category="{ row }">
            <el-tag>{{ row.categoryName || '未分类' }}</el-tag>
          </template>

          <!-- 创建人自定义插槽 -->
          <template #creator="{ row }">
            <div class="user-info">
              <el-avatar :size="24" :src="row.ownerAvatar">
                {{ (row?.creator || row?.owner || 'U')?.charAt(0) }}
              </el-avatar>
              <span>{{ row.creator || row.owner || '未分配' }}</span>
            </div>
          </template>

          <!-- 版本号自定义插槽 -->
          <template #version="{ row }">
            <span 
              class="version-link"
              @click="handleVersionHistory(row)"
            >
              {{ row.version || '1.0.0' }}
            </span>
          </template>

          <!-- 操作自定义插槽 -->
          <template #actions="{ row }">
            <el-button type="primary" text @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" text @click="handleDownload(row)">下载</el-button>
            <el-button type="danger" text @click="handleDelete(row)">删除</el-button>
          </template>

          <!-- 卡片视图插槽 -->
          <template #card="{ item }">
            <template-card
              :template="item"
              :layout="cardLayout"
              @edit="handleEdit"
              @copy="handleDownload"
              @delete="handleDelete"
              @view="handleViewTemplate"
              @preview="handlePreviewTemplate"
              @download="handleDownload"
              @version-history="handleVersionHistory"
            />
          </template>
        </base-list>
      </div>
    </div>
    
    <!-- 模板创建对话框 -->
    <template-create-dialog
      v-model="createDialogVisible"
      :initial-data="createDialogData"
      @submit="handleDialogSubmit"
      @error="handleDialogError"
    />

    <!-- 版本历史对话框 -->
    <version-history-dialog
      v-model="versionHistoryVisible"
      :template="currentTemplate"
    />

    <!-- 模板内容查看抽屉 -->
    <template-content-viewer
      v-model="templateViewerVisible"
      :template-data="currentTemplate"
      :is-version="false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessageBox } from 'element-plus'
import { Plus, ArrowLeft, ArrowRight, EditPen, Download, Delete, View, Timer } from '@element-plus/icons-vue'
import BaseList from '@/components/BaseList/index.vue'
import TemplateCard from './components/TemplateCard.vue'
import TemplateCreateDialog from './dialogs/TemplateCreateDialog.vue'
import TemplateTypeTree from './components/TemplateTypeTree.vue'
import VersionHistoryDialog from './dialogs/VersionHistoryDialog.vue'
import TemplateContentViewer from '@/components/TemplateContentViewer.vue'
import type { FilterFormItem, OptionItem, TableColumn, ActionItem, ViewType, CardConfig } from '@/components/BaseList/types'
import { getTemplateList, getTemplateStatusOptions } from '@/api/template'
import type { Template, TemplateType } from '@/api/template'

// 列表实例
const listRef = ref()
const treeRef = ref()

// 侧边栏折叠状态
const sidebarCollapsed = ref(false)

// 切换侧边栏折叠状态
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

// 对话框控制
const createDialogVisible = ref(false)
const createDialogData = ref<Partial<Template>>()
const versionHistoryVisible = ref(false)
const currentTemplate = ref<Template>()

// 模板内容查看控制
const templateViewerVisible = ref(false)

// 表格操作配置
const templateActions: ActionItem[] = [
  { label: '编辑', command: 'edit', icon: EditPen },
  { label: '预览', command: 'preview', icon: View },
  { label: '下载', command: 'download', icon: Download },
  { label: '版本历史', command: 'version-history', icon: Timer },
  { label: '删除', command: 'delete', icon: Delete, divided: true }
]

// 表格列配置
const columns = ref<TableColumn[]>([
  {
    type: 'index',
    label: '序号',
    width: 55,
    fixed: 'left',
    align: 'center',
    headerAlign: 'center'
  },
  {
    prop: 'name',
    label: '模板名称',
    minWidth: 180,
    align: 'center',
    headerAlign: 'center',
    fixed: 'left',
    slot: 'template-name',
    showOverflowTooltip: true
  },
  {
    prop: 'alias',
    label: '别名',
    width: 150,
    showOverflowTooltip: true
  },
  {
    prop: 'templateCode',
    label: '模板编号',
    align: 'center',
    headerAlign: 'center',
    width: 120
  },
  {
    prop: 'enableStatus',
    label: '启用状态',
    width: 100,
    align: 'center',
    headerAlign: 'center',
    slot: 'enable-status'
  },
  {
    prop: 'status',
    label: '审核状态',
    width: 100,
    align: 'center',
    headerAlign: 'center',
    slot: 'status'
  },
  {
    prop: 'scope',
    label: '适用范围',
    width: 120,
    align: 'center',
    headerAlign: 'center',
    showOverflowTooltip: true
  },
  {
    prop: 'type',
    label: '类型',
    width: 120,
    align: 'center',
    headerAlign: 'center',
    showOverflowTooltip: true
  },
  {
    prop: 'format',
    label: '格式',
    width: 80,
    align: 'center',
    headerAlign: 'center',
  },
  {
    prop: 'suffix',
    label: '文件尾缀',
    width: 100,
    align: 'center',
    headerAlign: 'center',
  },
  {
    prop: 'reviseContent',
    label: '修订内容',
    width: 150,
    align: 'center',
    headerAlign: 'center',
    showOverflowTooltip: true
  },
  {
    prop: 'creator',
    label: '创建人',
    width: 100,
    align: 'center',
    headerAlign: 'center',
    slot: 'creator'
  },
  {
    prop: 'createTime',
    label: '创建时间',
    width: 150,
    sortable: 'custom'
  },
  {
    prop: 'updateTime',
    label: '修改时间',
    width: 150,
    sortable: 'custom'
  },
  {
    prop: 'version',
    label: '版本',
    width: 80,
    align: 'center',
    headerAlign: 'center',
    fixed: 'right',
    slot: 'version'
  },
  {
    label: '操作',
    width: 160,
    align: 'center',
    fixed: 'right',
    actions: templateActions,
    maxVisibleActions: 1
  }
])

// 过滤条件配置
const filterConfig = ref<FilterFormItem[]>([
  {
    type: 'input',
    field: 'keyword',
    label: '关键词',
    placeholder: '模板名称/负责人'
  },
  {
    type: 'input',
    field: 'alias',
    label: '别名',
    placeholder: '请输入别名'
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
      const res = await getTemplateStatusOptions()
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

const cardConfig: CardConfig = {
  gridFillMode: 'auto-fill',
  gap: '30px',
  minWidth: '270px',
  maxWidth: '320px',
  minHeight: '220px',
  maxHeight: '2600px'
}

// 当前视图类型
const viewType = ref<ViewType>('table')

// 根据视图类型获取分页配置
const paginationConfig = computed(() => {
  return viewType.value === 'cards' ? cardPaginationConfig : tablePaginationConfig
})

// 卡片布局方式
const cardLayout = ref<'vertical' | 'horizontal'>('horizontal')

// 获取状态类型
const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    '已发布': 'success',
    '草稿': 'info',
    '审核中': 'warning',
    '已废弃': 'danger'
  }
  return typeMap[status] || 'info'
}

// 处理过滤条件变化
const handleFilterChange = (filters: Record<string, any>) => {
  console.log('Filter changed:', filters)
}

// 处理视图切换
const handleViewChange = (type: ViewType) => {
  console.log('View changed:', type)
  viewType.value = type
}

// 处理选择变化
const handleSelectionChange = (selection: Template[]) => {
  console.log('Selection changed:', selection)
}

// 处理创建模板
const handleCreateTemplate = () => {
  createDialogData.value = {
    status: '草稿',
    version: '1.0.0',
    standardType: '行业标准模板'
  }
  createDialogVisible.value = true
}

// 处理编辑
const handleEdit = (row: Template) => {
  createDialogData.value = { ...row }
  createDialogVisible.value = true
}

// 处理下载
const handleDownload = async (row: Template) => {
  try {
    await ElMessageBox.confirm(
      `确定要下载模板"${row.title || row.name}"吗？`,
      '下载确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    console.log('Download:', row)
    // 这里实现下载逻辑
    // 可以使用浏览器的下载API或调用后端下载接口
    // 示例：如果有文件URL，可以直接创建下载链接
    if (row.url) {
      const link = document.createElement('a')
      link.href = row.url
      link.download = `${row.name || row.title || '模板'}.${row.suffix?.toLowerCase() || 'docx'}`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    } else {
      console.error('模板没有可下载的URL')
    }
  } catch {
    // 用户取消下载
  }
}

// 处理删除
const handleDelete = async (row: Template) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除模板"${row.title}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    console.log('Delete:', row)
  } catch {
    // 用户取消删除
  }
}

// 处理查看模板
const handleViewTemplate = (template: Template) => {
  // 设置当前模板
  currentTemplate.value = template
  
  // 显示抽屉
  templateViewerVisible.value = true
}

// 处理预览模板
const handlePreviewTemplate = (template: Template) => {
  handleViewTemplate(template)
}

// 处理版本历史
const handleVersionHistory = (template: Template) => {
  currentTemplate.value = template
  versionHistoryVisible.value = true
}

// 统一处理操作指令
const handleActionCommand = (event: { command: string, row: Template }) => {
  switch (event.command) {
    case 'preview':
      handlePreviewTemplate(event.row)
      break
    case 'download':
      handleDownload(event.row)
      break
    case 'edit':
      handleEdit(event.row)
      break
    case 'version-history':
      handleVersionHistory(event.row)
      break
    case 'delete':
      handleDelete(event.row)
      break
  }
}

// 处理对话框提交
const handleDialogSubmit = (formData: Template) => {
  console.log('模板创建/更新成功，表单数据:', formData)
  listRef.value?.refresh()
}

// 处理对话框错误
const handleDialogError = (error: Error) => {
  console.error('创建/更新模板失败:', error)
}

// 监听模板列表刷新事件
window.addEventListener('template-list-refresh', () => {
  listRef.value?.refresh()
})

// 处理分类选择
const handleTypeSelect = (type: TemplateType | null) => {
  console.log('Selected type:', type)
  // 这里可以根据选中的分类刷新列表
  if (type) {
    // 使用分类ID作为过滤条件
    listRef.value?.refresh({ categoryId: type.id })
  } else {
    // 清除分类过滤条件
    listRef.value?.refresh({ categoryId: undefined })
  }
}
</script>

<style lang="scss" scoped>
.template-list-container {
  height: 100%;
  padding: 20px;
  background-color: var(--el-bg-color);
  border-radius: 8px;
}

.template-list-layout {
  height: 100%;
  display: flex;
  gap: 20px;
  position: relative;
  transition: gap 0.3s ease-in-out;
  &.sidebar-collapsed {
    gap: 0;
  }
}

.template-type-sidebar {
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

.template-list-main {
  flex: 1;
  min-width: 0;
  background-color: var(--el-bg-color-overlay);
  border-radius: 8px;
}

.template-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.template-title {
  cursor: pointer;
  color: var(--el-color-primary);
  
  &:hover {
    text-decoration: underline;
  }
}

.version-link {
  color: var(--el-color-primary);
  cursor: pointer;
  transition: color 0.2s;
  
  &:hover {
    color: var(--el-color-primary-light-3);
    text-decoration: underline;
  }
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

.el-button+.el-button {
    margin-left: 0px;
}
</style> 