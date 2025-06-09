<template>
  <el-dialog
    v-model="visible"
    :title="`版本历史 [${template?.name || ''}]`"
    width="75%"
    destroy-on-close
  >
    <div class="version-history">
      <base-list
        :columns="columns"
        :request-api="loadVersionHistory"
        :enable-view-switch="false"
        :table-props="{
          border: true,
          stripe: true,
          'header-cell-style': {
            'text-align': 'center',
            'background-color': 'var(--el-fill-color-light)',
            color: 'var(--el-text-color-regular)'
          }
        }"
        :pagination-config="{
          pageSize: 10,
          pageSizes: [10, 20, 50, 100],
          layout: 'total, sizes, prev, pager, next, jumper'
        }"
      >
        <!-- 状态列自定义渲染 -->
        <template #status="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
        </template>
        
        <!-- 操作列自定义渲染 -->
        <template #action="{ row }">
          <el-button 
            type="primary" 
            link
            @click="handleView(row)"
          >
            查看
          </el-button>
        </template>
      </base-list>
    </div>
  </el-dialog>

  <!-- 使用公共模板内容查看器组件 -->
  <template-content-viewer
    v-model="drawerVisible"
    :template-data="currentVersion"
    :is-version="true"
  />
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { Template } from '@/api/template'
import { getTemplateVersionHistory } from '@/api/template'
import type { VersionHistoryItem } from '@/api/template'
import BaseList from '@/components/BaseList/index.vue'
import TemplateContentViewer from '@/components/TemplateContentViewer.vue'
import type { TableColumn } from '@/components/BaseList/types'

const props = defineProps<{
  modelValue: boolean
  template?: Template
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

// 控制对话框显示
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 抽屉控制
const drawerVisible = ref(false)
const currentVersion = ref<VersionHistoryItem | null>(null)

// 表格列配置
const columns = ref<TableColumn[]>([
  {
    type: 'index',
    label: '序号',
    width: 60,
    align: 'center',
    headerAlign: 'center'
  },
  {
    prop: 'name',
    label: '名称',
    minWidth: 150,
    headerAlign: 'center'
  },
  {
    prop: 'version',
    label: '版本号',
    width: 80,
    align: 'center',
    headerAlign: 'center'
  },
  {
    prop: 'content',
    label: '制作内容',
    minWidth: 300,
    showOverflowTooltip: true,
    headerAlign: 'center'
  },
  {
    prop: 'status',
    label: '审核状态',
    width: 100,
    align: 'center',
    slot: 'status',
    headerAlign: 'center'
  },
  {
    prop: 'reviewReason',
    label: '驳回原因',
    width: 120,
    align: 'center',
    showOverflowTooltip: true,
    headerAlign: 'center'
  },
  {
    prop: 'updater',
    label: '修改人',
    width: 100,
    align: 'center',
    headerAlign: 'center'
  },
  {
    prop: 'updateTime',
    label: '修改时间',
    width: 220,
    align: 'center',
    headerAlign: 'center'
  },
  {
    label: '操作',
    width: 80,
    align: 'center',
    fixed: 'right',
    slot: 'action',
    headerAlign: 'center'
  }
])

// 获取状态类型
const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    '审核中': 'warning',
    '通过': 'success',
    '驳回': 'danger',
    '草稿': 'info'
  }
  return typeMap[status] || 'info'
}

// 加载版本历史数据
const loadVersionHistory = async (params: any) => {
  if (!props.template?.id) {
    return {
      list: [],
      total: 0
    }
  }

  try {
    // 调用API获取版本历史
    const { pageNum, pageSize } = params
    const response = await getTemplateVersionHistory(props.template.id, { pageNum, pageSize })
    
    return {
      list: response.list,
      total: response.total
    }
  } catch (error) {
    console.error('加载版本历史失败:', error)
    ElMessage.error('加载版本历史失败')
    return {
      list: [],
      total: 0
    }
  }
}

// 查看版本
const handleView = (version: VersionHistoryItem) => {
  currentVersion.value = version
  drawerVisible.value = true
}
</script>

<style lang="scss" scoped>
.version-history {
  margin: -20px;
  
  :deep(.el-table) {
    width: 100%;
  }
}
</style> 