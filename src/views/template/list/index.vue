<template>
  <div class="template-list-container">
    <div class="template-list-layout">
      <!-- 左侧分类树 -->
      <div class="template-type-sidebar" :class="{ 'collapsed': sidebarCollapsed }">
        <div v-if="!sidebarCollapsed" class="sidebar-content">
          <template-type-tree ref="treeRef" @select="handleTypeSelect" />
        </div>
        <div class="sidebar-toggle" @click="toggleSidebar">
          <el-icon :size="20">
            <arrow-left v-if="!sidebarCollapsed" />
            <arrow-right v-else />
          </el-icon>
        </div>
      </div>

      <!-- 右侧列表 -->
      <div class="template-list-main">
        <base-list
          ref="listRef"
          title="模板列表"
          :filter-config="filterConfig"
          :columns="columns"
          :enable-advanced-filter="true"
          :enable-view-switch="true"
          :request-api="getTemplateList"
          :table-props="tableProps"
          :pagination-config="paginationConfig"
          :card-layout="cardLayout"
          @filter-change="handleFilterChange"
          @view-change="handleViewChange"
          @selection-change="handleSelectionChange"
        >
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
    <el-drawer
      v-model="templateViewerVisible"
      :title="currentTemplate?.name ? `${currentTemplate.name} (v${currentTemplate.version})` : '模板内容'"
      direction="btt"
      size="100%"
      :destroy-on-close="true"
    >
      <div class="template-content-viewer">
        <div v-if="contentLoading" class="loading-container">
          <el-skeleton :rows="10" animated />
        </div>
        <div v-else-if="templateContent" class="template-content">
          <div class="content-header">
            <div class="meta-info">
              <div class="info-item">
                <span class="label">版本号：</span>
                <span class="value">{{ currentTemplate?.version || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="label">创建人：</span>
                <span class="value">{{ currentTemplate?.creator || currentTemplate?.owner || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="label">创建时间：</span>
                <span class="value">{{ currentTemplate?.createTime || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="label">状态：</span>
                <el-tag size="small" :type="getStatusType(currentTemplate?.status || '')">
                  {{ currentTemplate?.status || '-' }}
                </el-tag>
              </div>
            </div>
            <div class="actions">
              <el-button type="primary" size="small" @click="handleDownloadInDrawer">
                <el-icon><download /></el-icon>
                下载文档
              </el-button>
            </div>
          </div>
          
          <el-divider />
          
          <div class="content-body">
            <!-- 这里可以使用iframe或其他方式展示Word内容 -->
            <div v-if="previewUrl" class="document-preview">
              <iframe :src="previewUrl" frameborder="0"></iframe>
            </div>
            <div v-else class="document-content">
              <!-- 模拟Word文档内容 -->
              <div class="document-header">
                <h1>{{ currentTemplate?.name }}</h1>
              </div>
              <div class="document-section">
                <h2>模板描述</h2>
                <p>{{ currentTemplate?.description || '暂无描述' }}</p>
              </div>
              <div class="document-section">
                <h2>模板内容示例</h2>
                <div v-html="templateContent"></div>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="no-content">
          <el-empty description="暂无内容可预览" />
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus, ArrowLeft, ArrowRight, Download } from '@element-plus/icons-vue'
import BaseList from '@/components/BaseList/index.vue'
import TemplateCard from './components/TemplateCard.vue'
import TemplateCreateDialog from './dialogs/TemplateCreateDialog.vue'
import TemplateTypeTree from '@/components/TemplateTypeTree.vue'
import VersionHistoryDialog from './dialogs/VersionHistoryDialog.vue'
import type { FilterFormItem, OptionItem, TableColumn } from '@/components/BaseList/types'
import { getTemplateList, getTemplateStatusOptions, getTemplateCategoryOptions } from '@/api/template'
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
const contentLoading = ref(false)
const templateContent = ref<string | null>(null)
const previewUrl = ref<string | null>(null)

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
    prop: 'templateCode',
    label: '模板编号',
    width: 120,
    fixed: 'left',
    align: 'center'
  },
  {
    prop: 'name',
    label: '模板名称',
    minWidth: 180,
    align: 'center',
    slot: 'template-name',
    showOverflowTooltip: true
  },
  {
    prop: 'enableStatus',
    label: '启用状态',
    width: 100,
    slot: 'enable-status'
  },
  {
    prop: 'status',
    label: '审核状态',
    width: 100,
    slot: 'status'
  },
  {
    prop: 'scope',
    label: '适用范围',
    width: 120,
    showOverflowTooltip: true
  },
  {
    prop: 'type',
    label: '类型',
    width: 120,
    showOverflowTooltip: true
  },
  {
    prop: 'format',
    label: '格式',
    width: 80,
    align: 'center'
  },
  {
    prop: 'suffix',
    label: '后缀',
    width: 80,
    align: 'center'
  },
  {
    prop: 'standardType',
    label: '标准类型',
    width: 120,
    showOverflowTooltip: true
  },
  {
    prop: 'reviseContent',
    label: '修订内容',
    width: 150,
    showOverflowTooltip: true
  },
  {
    prop: 'creator',
    label: '创建人',
    width: 100,
    slot: 'creator'
  },
  {
    prop: 'createTime',
    label: '创建时间',
    width: 150
  },
  {
    prop: 'updateTime',
    label: '修改时间',
    width: 150
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
    placeholder: '模板名称/负责人'
  },
  {
    type: 'select',
    field: 'category',
    label: '分类',
    placeholder: '请选择分类',
    props: {
      remote: false,
      loading: false
    },
    options: async () => {
      const res = await getTemplateCategoryOptions()
      return res as unknown as OptionItem[]
    }
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

// 当前视图类型
const currentViewType = ref('table')

// 根据视图类型获取分页配置
const paginationConfig = computed(() => {
  return currentViewType.value === 'cards' ? cardPaginationConfig : tablePaginationConfig
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
const handleViewChange = (type: string) => {
  console.log('View changed:', type)
  currentViewType.value = type
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
const handleViewTemplate = async (template: Template) => {
  // 设置当前模板
  currentTemplate.value = template
  
  // 显示抽屉
  templateViewerVisible.value = true
  contentLoading.value = true
  
  try {
    // 模拟加载内容
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟模板内容
    templateContent.value = `
      <p>这是模板 "${template.name}" 的内容预览。在实际应用中，您可以通过API获取真实的模板内容或使用文档预览服务。</p>
      <p>模板类型: ${template.typeName || template.type || '未指定'}</p>
      <p>适用范围: ${template.scope || template.applicableScope || '全部'}</p>
      <p>标准类型: ${template.standardType || '未指定'}</p>
      <div style="margin-top: 20px; padding: 15px; background-color: #f5f7fa; border-radius: 4px;">
        <h3 style="margin-top: 0;">模板内容示例</h3>
        <p>这里是模板的具体内容，可以包含格式化的文本、表格、图片等。</p>
        <p>在实际应用中，您可以集成文档预览组件来展示真实的Word文档内容。</p>
      </div>
    `
    
    // 如果有预览URL，可以设置
    if (template.url) {
      // 如果是Office文档，可以使用Office Online预览
      // previewUrl.value = `https://view.officeapps.live.com/op/view.aspx?src=${encodeURIComponent(template.url)}`
      
      // 这里只是模拟，实际项目中可以使用真实的预览服务
      previewUrl.value = null
    }
    
    contentLoading.value = false
  } catch (error) {
    console.error('加载模板内容失败:', error)
    ElMessage.error('加载模板内容失败')
    contentLoading.value = false
    templateContent.value = null
  }
}

// 在抽屉中下载文档
const handleDownloadInDrawer = () => {
  if (!currentTemplate.value) return
  
  handleDownload(currentTemplate.value)
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
    width: 20px;
    flex-shrink: 0;
  }

  .sidebar-content {
    height: 100%;
    overflow: auto;
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

    &:hover {
      background-color: var(--el-color-primary-light-5);
      color: white;
    }
  }
}

.template-list-main {
  flex: 1;
  min-width: 0;
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

.el-button+.el-button {
    margin-left: 0px;
}

.template-content-viewer {
  height: 100%;
  padding: 20px;
  
  .loading-container {
    padding: 40px;
  }
  
  .content-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .meta-info {
      display: flex;
      flex-wrap: wrap;
      gap: 20px;
      
      .info-item {
        display: flex;
        align-items: center;
        
        .label {
          color: var(--el-text-color-secondary);
          margin-right: 8px;
        }
        
        .value {
          font-weight: 500;
        }
      }
    }
  }
  
  .content-body {
    height: calc(100% - 100px);
    overflow: auto;
    
    .document-preview {
      height: 100%;
      
      iframe {
        width: 100%;
        height: 100%;
        border: 1px solid var(--el-border-color-light);
        border-radius: 4px;
      }
    }
    
    .document-content {
      padding: 20px;
      background-color: white;
      border: 1px solid var(--el-border-color-light);
      border-radius: 4px;
      
      .document-header {
        text-align: center;
        margin-bottom: 30px;
        
        h1 {
          font-size: 24px;
          font-weight: bold;
        }
      }
      
      .document-section {
        margin-bottom: 20px;
        
        h2 {
          font-size: 18px;
          font-weight: bold;
          margin-bottom: 10px;
          color: var(--el-color-primary);
        }
        
        p {
          line-height: 1.6;
          margin-bottom: 10px;
        }
      }
    }
  }
  
  .no-content {
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
  }
}
</style> 