<template>
  <div class="project-list-container">
    <base-list
      ref="listRef"
      title="项目列表"
      :filter-config="filterConfig"
      :columns="columns"
      :enable-advanced-filter="true"
      :enable-view-switch="true"
      :request-api="getProjectList"
      :table-props="tableProps"
      :pagination-config="paginationConfig"
      @filter-change="handleFilterChange"
      @view-change="handleViewChange"
      @selection-change="handleSelectionChange"
    >
      <!-- 顶部工具栏插槽 -->
      <template #toolbar>
        <el-button type="primary" @click="handleCreateProject">
          <el-icon><plus /></el-icon>新建项目
        </el-button>
      </template>
      
      <!-- 项目名称自定义插槽 -->
      <template #project-name="{ row }">
        <div class="project-name" @click="handleViewProject(row)">
          <span class="project-title">{{ row.title || '未命名项目' }}</span>
        </div>
      </template>

      <!-- 状态自定义插槽 -->
      <template #status="{ row }">
        <el-tag :type="getStatusType(row.status)">{{ row.status || '未设置' }}</el-tag>
      </template>

      <!-- 风险自定义插槽 -->
      <template #risk="{ row }">
        <el-tag :type="getRiskType(row.risk)">{{ row.risk || '未设置' }}</el-tag>
      </template>

      <!-- 进度自定义插槽 -->
      <template #progress="{ row }">
        <el-progress :percentage="row.progress || 0" />
      </template>

      <!-- 负责人自定义插槽 -->
      <template #leader="{ row }">
        <div class="user-info">
          <el-avatar :size="24" :src="row.leaderAvatar">
            {{ row?.team?.charAt(0) || 'U' }}
          </el-avatar>
          <span>{{ row.team || '未分配' }}</span>
    </div>
      </template>

      <!-- 操作自定义插槽 -->
      <template #actions="{ row }">
        <el-button type="primary" text @click="handleEdit(row)">编辑</el-button>
        <el-button type="danger" text @click="handleDelete(row)">删除</el-button>
      </template>

      <!-- 卡片视图插槽 -->
      <template #card="{ item }">
        <project-card
          :project="item"
          @edit="handleEdit"
          @delete="handleDelete"
          @view="handleViewProject"
        />
      </template>
    </base-list>
    
    <!-- 项目创建对话框 -->
    <project-create-dialog ref="projectCreateDialogRef" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import BaseList from '@/components/BaseList/index.vue'
import ProjectCard from '@/components/ProjectCard.vue'
import ProjectCreateDialog from './dialogs/ProjectCreateDialog.vue'
import dialogInstance from '@/hooks/useDialog'
import type { FilterFormItem, OptionItem, TableColumn } from '@/components/BaseList/types'
import { getProjectList, getProjectStatusOptions, getProjectRiskOptions, getTeamMemberOptions } from '@/api/project'
import type { Project } from '@/api/project'
import { useRouter } from 'vue-router'

// 列表实例
const listRef = ref()
const projectCreateDialogRef = ref()
const router = useRouter()

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
    prop: 'title',
    label: '项目名称',
    minWidth: 200,
    fixed: 'left',
    align: 'center',
    slot: 'project-name'
  },
  {
    prop: 'categoryName',
    label: '分类',
    width: 120
  },
  {
    prop: 'status',
    label: '状态',
    width: 100,
    slot: 'status'
  },
  {
    prop: 'risk',
    label: '风险',
    width: 100,
    slot: 'risk'
  },
  {
    prop: 'progress',
    label: '进度',
    width: 200,
    slot: 'progress'
  },
  {
    prop: 'team',
    label: '负责人',
    width: 120,
    slot: 'leader'
  },
  {
    prop: 'date',
    label: '开始时间',
    width: 120
  },
  {
    prop: 'dueDate',
    label: '结束时间',
    width: 120
  },
  {
    prop: 'createTime',
    label: '创建时间',
    width: 180
  },
  {
    label: '操作',
    width: 150,
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
    field: 'status',
    label: '状态',
    placeholder: '请选择状态',
    props: {
      remote: false,
      loading: false
    },
    options: async () => {
      const res = await getProjectStatusOptions()
      return res as unknown as OptionItem[]
    }
  },
  {
    type: 'select',
    field: 'risk',
    label: '风险',
    placeholder: '请选择风险',
    props: {
      filterable: true,
      remote: true,
      reserveKeyword: true,
      loading: false
    },
    options: async () => {
      const res = await getProjectRiskOptions()
      return res as unknown as OptionItem[]
    }
  },
  {
    type: 'daterange',
    field: 'dateRange',
    label: '创建时间',
    advanced: true
  },
  {
    type: 'select',
    field: 'leader',
    label: '负责人',
    advanced: true,
    placeholder: '请选择负责人',
    props: {
      filterable: true,
      remote: true,
      reserveKeyword: true,
      loading: false
    },
    options: async () => {
      const res = await getTeamMemberOptions()
      return res as unknown as OptionItem[]
    }
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
  layout: 'total, sizes, prev, pager, next, jumper'
}

// 获取状态类型
const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    '进行中': 'primary',
    '已完成': 'success',
    '待审核': 'warning',
    '已暂停': 'info'
  }
  return typeMap[status] || 'info'
}

// 获取风险类型
const getRiskType = (risk: string) => {
  const typeMap: Record<string, string> = {
    '低': 'info',
    '中': 'warning',
    '高': 'danger',
    '严重': 'danger'
  }
  return typeMap[risk] || 'info'
}

// 处理过滤条件变化
const handleFilterChange = (event: any) => {
  console.log('Filter changed:', event)
}

// 处理视图切换
const handleViewChange = (type: string) => {
  console.log('View changed:', type)
}

// 处理选择变化
const handleSelectionChange = (selection: any[]) => {
  console.log('Selection changed:', selection)
}

// 处理创建项目
const handleCreateProject = () => {
  dialogInstance.open('projectCreate', 
    // 属性
    { 
      initialData: {
        status: '进行中',
        risk: '低',
        progress: 0
      }
    }, 
    // 事件回调
    {
      submit: (formData) => {
        console.log('项目创建成功，表单数据:', formData)
        // 刷新列表
        listRef.value?.refresh()
      },
      error: (error) => {
        console.error('创建项目失败:', error)
      }
    }
  )
}

// 处理编辑
const handleEdit = (row: Project) => {
  dialogInstance.open('projectCreate', 
    // 属性
    { 
      initialData: row
    }, 
    // 事件回调
    {
      submit: (formData: Project) => {
        console.log('项目更新成功，表单数据:', formData)
        // 刷新列表
        listRef.value?.refresh()
      },
      error: (error: Error) => {
        console.error('更新项目失败:', error)
      }
    }
  )
}

// 处理删除
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除项目"${row.title}"吗？`,
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

// 处理查看项目
const handleViewProject = (project: Project) => {
  router.push(`/project/detail/${project.id}`)
}

// 监听项目列表刷新事件
window.addEventListener('project-list-refresh', () => {
  listRef.value?.refresh()
})
</script>

<style lang="scss" scoped>
.project-list-container {
  height: 100%;
  padding: 20px;
  background-color: var(--el-bg-color);
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

/* 卡片样式 */
.project-card {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.header-left {
  display: flex;
  gap: 12px;
}

.project-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.project-info h3 {
  margin: 0;
  font-size: 16px;
  line-height: 24px;
}

.project-info p {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.card-content {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.content-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.content-row .label {
  color: var(--el-text-color-secondary);
  width: 60px;
}

.card-footer {
  padding: 12px 16px;
  border-top: 1px solid var(--el-border-color-light);
  background-color: var(--el-fill-color-light);
}

.date-info {
  display: flex;
  justify-content: space-between;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.date-info p {
  margin: 0;
}

.project-title {
  cursor: pointer;
  color: var(--el-color-primary);
  
  &:hover {
    text-decoration: underline;
  }
}
</style>