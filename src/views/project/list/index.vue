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
      <!-- 项目名称自定义插槽 -->
      <template #project-name="{ row }">
        <div class="project-name">
          <el-avatar :size="32" :src="row.logo">
            {{ row?.title?.charAt(0) || 'P' }}
          </el-avatar>
          <span>{{ row.title || '未命名项目' }}</span>
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
        <el-card class="project-card" :body-style="{ padding: '0' }">
          <div class="card-header">
            <div class="header-left">
              <el-avatar :size="40" :src="item.logo">
                {{ item?.title?.charAt(0) || 'P' }}
              </el-avatar>
              <div class="project-info">
                <h3>{{ item.title || '未命名项目' }}</h3>
                <p>{{ item.categoryName || '未分类' }}</p>
              </div>
            </div>
            <div class="header-right">
              <el-dropdown trigger="click">
                <el-button type="primary" text>
                  <el-icon><More /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleEdit(item)">编辑</el-dropdown-item>
                    <el-dropdown-item @click="handleDelete(item)">删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
          <div class="card-content">
            <div class="content-row">
              <span class="label">状态：</span>
              <el-tag :type="getStatusType(item.status)">{{ item.status || '未设置' }}</el-tag>
            </div>
            <div class="content-row">
              <span class="label">风险：</span>
              <el-tag :type="getRiskType(item.risk)">{{ item.risk || '未设置' }}</el-tag>
            </div>
            <div class="content-row">
              <span class="label">进度：</span>
              <el-progress :percentage="item.progress || 0" />
            </div>
            <div class="content-row">
              <span class="label">负责人：</span>
              <div class="user-info">
                <el-avatar :size="24" :src="item.leaderAvatar">
                  {{ item?.team?.charAt(0) || 'U' }}
                </el-avatar>
                <span>{{ item.team || '未分配' }}</span>
              </div>
            </div>
          </div>
          <div class="card-footer">
            <div class="date-info">
              <p>开始：{{ item.date || '未设置' }}</p>
              <p>结束：{{ item.dueDate || '未设置' }}</p>
            </div>
          </div>
        </el-card>
      </template>
    </base-list>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { More } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import BaseList from '@/components/BaseList/index.vue'
import type { FilterFormItem, TableColumn } from '@/components/BaseList/types'
import { getProjectList } from '@/api/project'

// 列表实例
const listRef = ref()

// 表格列配置
const columns = ref<TableColumn[]>([
  {
    type: 'selection',
    width: 55,
    fixed: 'left'
  },
  {
    prop: 'title',
    label: '项目名称',
    minWidth: 200,
    fixed: 'left',
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
    options: [
      { label: '进行中', value: '进行中' },
      { label: '已完成', value: '已完成' },
      { label: '待审核', value: '待审核' },
      { label: '已暂停', value: '已暂停' }
    ]
  },
  {
    type: 'select',
    field: 'risk',
    label: '风险',
    options: [
      { label: '低', value: '低' },
      { label: '中', value: '中' },
      { label: '高', value: '高' },
      { label: '严重', value: '严重' }
    ]
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
    options: async () => {
      // 模拟异步获取负责人列表
      return [
        { label: '张三', value: '张三' },
        { label: '李四', value: '李四' },
        { label: '王五', value: '王五' }
      ]
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

// 获取项目列表
const fetchProjects = async (params: any) => {
  try {
    const response = await getProjectList({
      page: params.page || 1,
      limit: params.limit || 10,
      keyword: params.keyword,
      status: params.status,
      risk: params.risk,
      dateRange: params.dateRange,
      leader: params.leader
    })
    // 直接返回响应数据，不需要再处理
    return {
      data: response
    }
  } catch (error) {
    console.error('Failed to fetch projects:', error)
    return {
      data: {
        list: [],
        total: 0
      }
    }
  }
}

// 处理响应数据
const handleResponse = (response: any) => {
  console.log('API Response:', response)
  if (!response?.data) {
    return {
      list: [],
      total: 0
    }
  }

  // 直接返回数据
  return response.data
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

// 处理编辑
const handleEdit = (row: any) => {
  console.log('Edit:', row)
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
</script>

<style scoped>
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
</style>