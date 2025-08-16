<template>
  <div class="date-range-example">
    <h3>DateRangePicker 使用示例</h3>
    
    <BaseList
      :filter-config="filterConfig"
      :columns="columns"
      :request-api="fetchData"
      :enable-pagination="true"
      :show-filter-bar="true"
      @filter-change="handleFilterChange"
    />
  </div>
</template>

<script setup lang="ts">
import BaseList from './index.vue'
import type { FilterFormItem, TableColumn } from './types'

// 过滤配置
const filterConfig: FilterFormItem[] = [
  {
    type: 'daterange',
    field: 'createTime',
    label: '创建时间',
    placeholder: '请选择创建时间范围',
    // 默认值配置方式1：数组格式
    defaultValue: ['2024-01-01', '2024-12-31'],
    // 或者配置方式2：对象格式
    // defaultValue: { start: '2024-01-01', end: '2024-12-31' },
    props: {
      // 自定义占位符
      startPlaceholder: '开始创建时间',
      endPlaceholder: '结束创建时间',
      // 自定义日期格式
      valueFormat: 'YYYY-MM-DD',
      format: 'YYYY-MM-DD',
      // 自定义快捷选项
      shortcuts: [
        {
          text: '今天',
          value: () => {
            const today = new Date()
            return [today, today]
          }
        },
        {
          text: '昨天',
          value: () => {
            const yesterday = new Date()
            yesterday.setTime(yesterday.getTime() - 3600 * 1000 * 24)
            return [yesterday, yesterday]
          }
        },
        {
          text: '最近7天',
          value: () => {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
            return [start, end]
          }
        },
        {
          text: '最近30天',
          value: () => {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
            return [start, end]
          }
        }
      ]
    }
  },
  {
    type: 'input',
    field: 'keyword',
    label: '关键词',
    placeholder: '请输入关键词'
  },
  {
    type: 'select',
    field: 'status',
    label: '状态',
    placeholder: '请选择状态',
    options: [
      { label: '全部', value: '' },
      { label: '启用', value: 'active' },
      { label: '禁用', value: 'inactive' }
    ]
  }
]

// 表格列配置
const columns: TableColumn[] = [
  {
    prop: 'id',
    label: 'ID',
    width: 80
  },
  {
    prop: 'name',
    label: '名称',
    width: 200
  },
  {
    prop: 'createTime',
    label: '创建时间',
    width: 180,
    formatter: (row: any) => {
      return row.createTime ? new Date(row.createTime).toLocaleDateString() : '-'
    }
  },
  {
    prop: 'status',
    label: '状态',
    width: 100,
    formatter: (row: any) => {
      return row.status === 'active' ? '启用' : '禁用'
    }
  }
]

// 模拟数据获取
const fetchData = async (params: any) => {
  console.log('请求参数:', params)
  
  // 模拟API请求
  await new Promise(resolve => setTimeout(resolve, 1000))
  
  // 模拟数据
  const mockData = [
    { id: 1, name: '测试数据1', createTime: '2024-01-15', status: 'active' },
    { id: 2, name: '测试数据2', createTime: '2024-02-20', status: 'inactive' },
    { id: 3, name: '测试数据3', createTime: '2024-03-10', status: 'active' }
  ]
  
  return {
    list: mockData,
    total: mockData.length
  }
}

// 处理过滤条件变化
const handleFilterChange = (event: any) => {
  console.log('过滤条件变化:', event)
  
  // event.values 包含处理后的数据：
  // {
  //   createTime: ['2024-01-01', '2024-12-31'],  // 原始数组值
  //   createTimeStart: '2024-01-01',              // 开始时间
  //   createTimeEnd: '2024-12-31',                // 结束时间
  //   keyword: '搜索关键词',
  //   status: 'active'
  // }
  
  // 可以根据需要处理这些数据
  const { createTimeStart, createTimeEnd, keyword, status } = event.values
  
  // 构建API请求参数
  const apiParams = {
    startDate: createTimeStart,
    endDate: createTimeEnd,
    keyword,
    status,
    page: 1,
    pageSize: 10
  }
  
  console.log('API请求参数:', apiParams)
}
</script>

<style scoped>
.date-range-example {
  padding: 20px;
}

h3 {
  margin-bottom: 20px;
  color: #333;
}
</style> 