<template>
  <base-list
    :request-api="fetchProjects"
    :columns="columns"
    :filter-config="filterConfig"
    title="项目列表"
  >
    <!-- 项目名称列 -->
    <template #projectName="{ row }">
      <div class="project-name-cell">
        <div class="project-icon" :style="{ backgroundColor: getIconColor(row.name) }">
          {{ getInitial(row.name) }}
        </div>
        <span class="project-name">{{ row.name }}</span>
      </div>
    </template>

    <!-- 项目类型列 -->
    <template #projectType="{ row }">
      <div class="project-type">{{ row.type }}</div>
    </template>

    <!-- 项目状态列 -->
    <template #status="{ row }">
      <div class="status-tag" :class="getStatusClass(row.status)">
        {{ row.status }}
      </div>
    </template>

    <!-- 风险等级列 -->
    <template #risk="{ row }">
      <div class="status-tag" :class="getRiskClass(row.risk)">
        {{ row.risk }}
      </div>
    </template>

    <!-- 项目进度列 -->
    <template #progress="{ row }">
      <el-progress
        class="project-progress"
        :percentage="row.progress"
        :color="getProgressColor(row.progress)"
        :stroke-width="6"
      />
    </template>

    <!-- 负责人列 -->
    <template #owner="{ row }">
      <div class="project-owner">
        <div class="avatar" :style="{ backgroundColor: getAvatarColor(row.owner.name) }">
          {{ getInitial(row.owner.name) }}
        </div>
        <div>
          <div class="name">{{ row.owner.name }}</div>
          <div class="role">{{ row.owner.role }}</div>
        </div>
      </div>
    </template>

    <!-- 操作列 -->
    <template #operate="{ row }">
      <div class="operate-btns">
        <el-button
          class="edit-btn"
          link
          size="small"
          @click="handleEdit(row)"
        >
          编辑
        </el-button>
        <el-button
          class="delete-btn"
          link
          size="small"
          @click="handleDelete(row)"
        >
          删除
        </el-button>
      </div>
    </template>
  </base-list>
</template>

<script setup lang="ts">
import BaseList from '@/components/BaseList/index.vue'
import { getProjectList } from '@/api/project'
import type { TableColumn, FilterFormItem } from '@/components/BaseList/types'

// 表格列配置
const columns: TableColumn[] = [
  {
    type: 'selection',
    width: 50,
    align: 'center'
  },
  {
    prop: 'name',
    label: '项目名称',
    slot: 'projectName',
    minWidth: 180
  },
  {
    prop: 'type',
    label: '分类',
    slot: 'projectType',
    minWidth: 150
  },
  {
    prop: 'status',
    label: '状态',
    slot: 'status',
    width: 100,
    align: 'center'
  },
  {
    prop: 'risk',
    label: '风险',
    slot: 'risk',
    width: 100,
    align: 'center'
  },
  {
    prop: 'progress',
    label: '进度',
    slot: 'progress',
    minWidth: 150
  },
  {
    prop: 'owner',
    label: '负责人',
    slot: 'owner',
    minWidth: 150
  },
  {
    prop: 'startTime',
    label: '开始时间',
    minWidth: 120
  },
  {
    label: '操作',
    slot: 'operate',
    fixed: 'right',
    width: 120,
    align: 'center'
  }
]

// 过滤条件配置
const filterConfig: FilterFormItem[] = [
  {
    field: 'name',
    label: '项目名称',
    type: 'input'
  },
  {
    field: 'type',
    label: '项目类型',
    type: 'select',
    options: [
      { label: '产品设计', value: '产品设计' },
      { label: '产品规划', value: '产品规划' },
      { label: '市场营销', value: '市场营销' },
      { label: '活动策划', value: '活动策划' },
      { label: '品牌推广', value: '品牌推广' }
    ]
  },
  {
    field: 'status',
    label: '状态',
    type: 'select',
    options: [
      { label: '进行中', value: '进行中' },
      { label: '高风险', value: '高风险' }
    ]
  },
  {
    field: 'dateRange',
    label: '时间范围',
    type: 'daterange'
  }
]

// 获取项目列表数据
const fetchProjects = async (params: any) => {
  return await getProjectList(params)
}

// 获取首字母
const getInitial = (name: string): string => {
  return name ? name.charAt(0).toUpperCase() : ''
}

// 获取图标背景色
const getIconColor = (name: string): string => {
  const colors = [
    '#409EFF', '#67C23A', '#E6A23C', '#F56C6C', 
    '#909399', '#9254de', '#36cfc9', '#ff7875'
  ]
  const index = name ? name.charCodeAt(0) % colors.length : 0
  return colors[index]
}

// 获取头像背景色
const getAvatarColor = (name: string): string => {
  const colors = [
    '#1890ff', '#52c41a', '#faad14', '#f5222d', 
    '#722ed1', '#eb2f96', '#13c2c2', '#fa8c16'
  ]
  const index = name ? name.charCodeAt(0) % colors.length : 0
  return colors[index]
}

// 获取状态样式类
const getStatusClass = (status: string): string => {
  const map: Record<string, string> = {
    '进行中': 'in-progress',
    '高风险': 'high-risk'
  }
  return map[status] || ''
}

// 获取风险样式类
const getRiskClass = (risk: string): string => {
  const map: Record<string, string> = {
    '高': 'high',
    '低': 'low-risk'
  }
  return map[risk] || ''
}

// 获取进度条颜色
const getProgressColor = (progress: number): string => {
  if (progress < 30) return '#f56c6c'
  if (progress < 70) return '#e6a23c'
  return '#67c23a'
}

// 处理编辑
const handleEdit = (row: any) => {
  console.log('编辑项目', row)
}

// 处理删除
const handleDelete = (row: any) => {
  console.log('删除项目', row)
}
</script>

<style scoped>
/* 组件特定样式可以在这里添加 */
</style> 