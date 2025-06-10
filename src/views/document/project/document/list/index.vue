<template>
  <div class="document-list-container">

    <base-list
      ref="listRef"
      :view-type="viewType"
      :card-config="cardConfig"
      title="文档列表"
      :filter-config="filterConfig"
      :columns="columns"
      :enable-advanced-filter="true"
      :request-api="getDocumentList"
      :request-params="{ projectId }"
      :table-props="tableProps"
      :pagination-config="paginationConfig"
      :show-filter-bar="false"
      @filter-change="handleFilterChange"
      @selection-change="handleSelectionChange"
      @view-change="handleViewChange"
    >
      <!-- 顶部工具栏插槽 -->
      <template #toolbar>
        <div class="toolbar-left">
          <el-button type="primary" @click="handleCreateDocument">
            <el-icon><plus /></el-icon>新建文档
          </el-button>
        </div>
      </template>

      <!-- 文档名称自定义插槽 -->
      <template #document-name="{ row }">
        <div class="document-name" @click="handleViewDocument(row)">
          <el-icon><document /></el-icon>
          <span class="document-title">{{ row.name }}</span>
        </div>
      </template>

      <!-- 状态自定义插槽 -->
      <template #status="{ row }">
        <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
      </template>

      <!-- 同步状态自定义插槽 -->
      <template #sync-status="{ row }">
        <el-tag :type="getSyncStatusType(row.syncStatus)">
          {{ getSyncStatusText(row.syncStatus) }}
        </el-tag>
      </template>

      <!-- 创建人自定义插槽 -->
      <template #creator="{ row }">
        <div class="user-info">
          <el-avatar :size="24" :src="row.creatorAvatar">
            {{ row.creator?.charAt(0) }}
          </el-avatar>
          <span>{{ row.creator }}</span>
        </div>
      </template>

      <!-- 版本号自定义插槽 -->
      <template #version="{ row }">
        <span 
          class="version-link"
          @click="handleVersionHistory(row)"
        >
          {{ row.version }}
        </span>
      </template>

      <!-- 操作自定义插槽 -->
      <template #actions="{ row }">
        <el-button type="primary" text @click="handleEdit(row)">编辑</el-button>
        <el-button type="primary" text @click="handleSync(row)">同步</el-button>
        <el-button type="success" text @click="handleDownload(row)">下载</el-button>
        <el-button type="danger" text @click="handleDelete(row)">删除</el-button>
      </template>

      <!-- 卡片视图插槽 -->
      <template #card="{ item }">
        <document-card
          :document="item"
          @view="handleViewDocument"
          @edit="handleEdit"
          @sync="handleSync"
          @download="handleDownload"
          @delete="handleDelete"
        />
      </template>
    </base-list>

    <!-- 文档创建对话框 -->
    <document-create-dialog
      v-model="createDialogVisible"
      :initial-data="createDialogData"
      :project-id="projectId"
      @submit="handleDialogSubmit"
      @error="handleDialogError"
    />

    <!-- 文档导入对话框 -->
    <!-- 
    <document-import-dialog
      v-model="importDialogVisible"
      :project-id="projectId"
      @success="handleImportSuccess"
    />
    -->

    <!-- 版本历史对话框 -->
    <version-history-dialog
      v-model="versionHistoryVisible"
      :document="currentDocument"
    />

    <!-- 文档内容查看抽屉 -->
    <document-content-viewer
      v-model="documentViewerVisible"
      :document-data="currentDocument"
      :is-version="false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus, Document } from '@element-plus/icons-vue'
import BaseList from '@/components/BaseList/index.vue'
import DocumentCard from './components/DocumentCard.vue'
import DocumentCreateDialog from './dialogs/DocumentCreateDialog.vue'
import VersionHistoryDialog from './dialogs/VersionHistoryDialog.vue'
import DocumentContentViewer from '@/components/DocumentContentViewer.vue'
import type { CardConfig, FilterFormItem, TableColumn, ViewType } from '@/components/BaseList/types'
import { getDocumentList, getDocumentStatusOptions, getDocumentTypeOptions } from '@/api/document'
import type { Document as DocumentModel, Project } from '@/types/document'

const route = useRoute()
const projectId = route.params.projectId as string

// 列表实例
const listRef = ref()
const viewType = ref<ViewType>('table')

// 项目信息
const projectInfo = ref<Project>()

// 对话框控制
const createDialogVisible = ref(false)
const createDialogData = ref<Partial<DocumentModel>>()
const versionHistoryVisible = ref(false)
const documentViewerVisible = ref(false)
const currentDocument = ref<DocumentModel>()

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
    prop: 'name',
    label: '文档名称',
    minWidth: 180,
    align: 'center',
    fixed: 'left',
    slot: 'document-name',
    showOverflowTooltip: true
  },
  {
    prop: 'documentCode',
    label: '文档编号',
    width: 120,
    align: 'center'
  },
  {
    prop: 'templateName',
    label: '模板',
    width: 180,
    align: 'center',
    showOverflowTooltip: true
  },
  {
    prop: 'status',
    label: '状态',
    width: 100,
    align: 'center',
    slot: 'status'
  },
  {
    prop: 'syncStatus',
    label: '同步状态',
    width: 120,
    align: 'center',
    slot: 'sync-status'
  },
  {
    prop: 'type',
    label: '文档类型',
    width: 120,
    align: 'center',
    showOverflowTooltip: true
  },
  {
    prop: 'format',
    label: '格式',
    width: 80,
    align: 'center'
  },
  {
    prop: 'creator',
    label: '负责人',
    width: 120,
    align: 'center',
    slot: 'creator'
  },
  {
    prop: 'createTime',
    label: '创建时间',
    width: 180,
    align: 'center'
  },
  {
    prop: 'updateTime',
    label: '上次更新时间',
    width: 180,
    align: 'center'
  },
  {
    prop: 'version',
    label: '版本',
    width: 80,
    align: 'center',
    fixed: 'right',
    slot: 'version'
  },
  {
    label: '操作',
    width: 200,
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
    placeholder: '文档名称/创建人'
  },
  {
    type: 'select',
    field: 'type',
    label: '文档类型',
    placeholder: '请选择文档类型',
    props: {
      remote: false,
      loading: false
    },
    options: []
  },
  {
    type: 'select',
    field: 'status',
    label: '状态',
    placeholder: '请选择状态',
    props: {
      filterable: true,
      remote: false,
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

// 分页配置
const paginationConfig = {
  pageSize: 10,
  pageSizes: [10, 20, 50, 100],
  layout: 'total, sizes, prev, pager, next, jumper',
  background: true
}


const cardConfig: CardConfig = {
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
    '已发布': 'success',
    '草稿': 'info',
    '审核中': 'warning',
    '已废弃': 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取同步状态类型
const getSyncStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    synced: 'success',
    pending: 'warning',
    failed: 'danger',
    none: 'info'
  }
  return typeMap[status] || 'info'
}

// 获取同步状态文本
const getSyncStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    synced: '已同步',
    pending: '待同步',
    failed: '同步失败',
    none: '未同步'
  }
  return textMap[status] || '未知'
}

// 处理过滤条件变化
const handleFilterChange = (filters: Record<string, any>) => {
  console.log('Filter changed:', filters)
}

// 处理选择变化
const handleSelectionChange = (selection: DocumentModel[]) => {
  console.log('Selection changed:', selection)
}

// 处理视图切换
const handleViewChange = (type: ViewType) => {
  console.log('View changed:', type)
  viewType.value = type
}

// 处理创建文档
const handleCreateDocument = () => {
  createDialogData.value = {
    status: '草稿',
    version: '1.0.0'
  }
  createDialogVisible.value = true
}

// 处理导入文档 - This function is no longer used
/*
const handleImportDocument = () => {
  importDialogVisible.value = true
}
*/

// 处理编辑
const handleEdit = (row: DocumentModel) => {
  createDialogData.value = { ...row }
  createDialogVisible.value = true
}

// 处理下载
const handleDownload = async (row: DocumentModel) => {
  try {
    await ElMessageBox.confirm(
      `确定要下载文档"${row.name}"吗？`,
      '下载确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    console.log('Download:', row)
    // 这里实现下载逻辑
    if (row.url) {
      const link = document.createElement('a')
      link.href = row.url
      link.download = `${row.name}.${row.format?.toLowerCase() || 'docx'}`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    }
  } catch {
    // 用户取消下载
  }
}

// 处理删除
const handleDelete = async (row: DocumentModel) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文档"${row.name}"吗？`,
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

// 处理查看文档
const handleViewDocument = (document: DocumentModel) => {
  currentDocument.value = document
  documentViewerVisible.value = true
}

// 处理版本历史
const handleVersionHistory = (document: DocumentModel) => {
  currentDocument.value = document
  versionHistoryVisible.value = true
}

// 处理对话框提交
const handleDialogSubmit = (formData: DocumentModel) => {
  console.log('文档创建/更新成功，表单数据:', formData)
  listRef.value?.refresh()
}

// 处理对话框错误
const handleDialogError = (error: Error) => {
  console.error('创建/更新文档失败:', error)
}

// 处理同步
const handleSync = (row: DocumentModel) => {
  ElMessageBox.confirm(`确定要将文档 "${row.name}" 同步到知识库吗？`, '同步确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    console.log('Syncing document:', row.id)
    ElMessage.success('已加入同步队列')
  }).catch(() => {})
}

// 初始化
onMounted(async () => {
  // TODO: 加载项目信息
  projectInfo.value = {
    id: projectId,
    name: route.query.projectName as string,
    type: '工程项目'
  }

  const typeFilter = filterConfig.value.find(item => item.field === 'type')
  if (typeFilter && typeFilter.props) {
    typeFilter.props.loading = true
    try {
      const options = await getDocumentTypeOptions()
      if(Array.isArray(options)) typeFilter.options = options
    } finally {
      typeFilter.props.loading = false
    }
  }

  const statusFilter = filterConfig.value.find(item => item.field === 'status')
  if (statusFilter && statusFilter.props) {
    statusFilter.props.loading = true
    try {
      const options = await getDocumentStatusOptions()
      if(Array.isArray(options)) statusFilter.options = options
    } finally {
      statusFilter.props.loading = false
    }
  }
})
</script>

<style lang="scss" scoped>
.document-list-container {
  height: 100%;
  padding: 20px;
  background-color: var(--el-bg-color);
  border-radius: 8px;
}

.document-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px;
  background-color: var(--el-bg-color-overlay);
  border-radius: 8px;
  box-shadow: var(--el-box-shadow-light);

  .header-info {
    display: flex;
    align-items: center;
    gap: 12px;

    .project-name {
      font-size: 18px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }
  }

  .header-actions {
    display: flex;
    gap: 12px;
  }
}

.document-name {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: var(--el-color-primary);

  &:hover {
    .document-title {
      text-decoration: underline;
    }
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
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

.el-button+.el-button {
  margin-left: 0px;
}

.toolbar-left {
  display: flex;
  gap: 12px;
}

.toolbar-right {
  display: flex;
  align-items: center;
}
</style> 