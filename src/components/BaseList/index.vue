<template>
  <div class="base-list">
    <!-- 过滤条件 -->
    <list-filter
      v-if="filterItems.length > 0"
      :filter-config="filterItems"
      :enable-advanced-filter="enableAdvancedFilter"
      :label-width="filterLabelWidth"
      :item-width="filterItemWidth"
      @filter-change="handleFilterChange"
    />

    <!-- 视图切换和操作按钮 -->
    <div v-if="hasHeaderContent" class="list-header">
      <div class="left-section">
        <slot name="header-left">
          <h2 v-if="title" class="list-title">{{ title }}</h2>
        </slot>
      </div>
      <div class="right-section">
        <slot name="header-right">
          <div v-if="enableViewSwitch" class="view-toggle">
            <el-button-group>
              <el-button
                :type="viewType === 'table' ? 'primary' : ''"
                @click="handleViewChange('table')"
              >
                <el-icon><List /></el-icon>
              </el-button>
              <el-button
                :type="viewType === 'cards' ? 'primary' : ''"
                @click="handleViewChange('cards')"
              >
                <el-icon><Grid /></el-icon>
              </el-button>
            </el-button-group>
          </div>
        </slot>
      </div>
    </div>

    <!-- 列表内容 -->
    <div class="list-content" v-loading="loading">
      <!-- 表格视图 -->
      <el-table
        v-if="viewType === 'table'"
        ref="tableRef"
        v-bind="tableProps"
        :data="list"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
      >
        <!-- 配置生成的列 -->
        <template v-if="columns && columns.length > 0">
          <el-table-column
            v-for="(column, index) in columns"
            :key="column.prop || column.slot || index"
            v-bind="getColumnProps(column)"
          >
            <!-- 自定义表头 -->
            <template v-if="column.headerSlot && $slots[column.headerSlot]" #header>
              <slot :name="column.headerSlot" />
            </template>
            
            <!-- 自定义单元格 -->
            <template v-if="column.slot && $slots[column.slot]" #default="scope">
              <slot :name="column.slot" v-bind="scope" />
            </template>
            
            <!-- 默认单元格 -->
            <template v-else-if="column.formatter" #default="scope">
              {{ column.formatter && column.formatter(scope.row, scope.column, scope.row[column.prop || ''], scope.$index) }}
            </template>
          </el-table-column>
        </template>
        
        <!-- 默认插槽 -->
        <slot />
      </el-table>

      <!-- 卡片视图 -->
      <div
        v-else
        class="card-view"
        v-bind="cardContainerProps"
      >
        <template v-if="$slots.card">
          <slot
            name="card"
            v-for="item in list"
            :key="getItemKey(item)"
            :item="item"
          />
        </template>
        <template v-else>
          <el-card
            v-for="item in list"
            :key="getItemKey(item)"
            class="card-item"
          >
            {{ item }}
          </el-card>
        </template>
      </div>

      <!-- 分页 -->
      <div v-if="enablePagination" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="currentPageSize"
          v-bind="paginationProps"
          :total="totalItems"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, useSlots } from 'vue'
import { List, Grid } from '@element-plus/icons-vue'
import type { TableInstance } from 'element-plus'
import ListFilter from './components/ListFilter.vue'
import type { 
  BaseListProps, 
  BaseListEmits,
  ViewType,
  FilterChangeEvent,
  TableColumn,
  ApiResponse,
  PaginationResponse,
  FilterFormItem,
  FilterConfig
} from './types'

const props = withDefaults(defineProps<BaseListProps>(), {
  filterConfig: undefined,
  enableAdvancedFilter: false,
  enableViewSwitch: true,
  defaultViewType: 'table',
  enablePagination: true,
  paginationConfig: () => ({
    pageSize: 10,
    pageSizes: [10, 20, 50, 100],
    layout: 'total, sizes, prev, pager, next, jumper',
    background: true,
    small: false
  }),
  requestApi: undefined,
  requestParams: () => ({}),
  responseHandler: undefined,
  tableProps: () => ({}),
  cardContainerProps: () => ({}),
  columns: () => [],
  title: undefined
})

// 提取过滤条件配置
const filterItems = computed<FilterFormItem[]>(() => {
  if (!props.filterConfig) return []
  
  // 如果是FilterConfig类型
  if ('items' in props.filterConfig) {
    return props.filterConfig.items
  }
  
  // 如果是FilterFormItem[]类型
  return props.filterConfig as FilterFormItem[]
})

// 提取过滤表单标签宽度
const filterLabelWidth = computed(() => {
  if (!props.filterConfig) return '80px' // 默认宽度
  
  // 如果是FilterConfig类型且定义了labelWidth
  if ('items' in props.filterConfig && props.filterConfig.labelWidth !== undefined) {
    return props.filterConfig.labelWidth
  }
  
  return '80px' // 默认宽度
})

// 提取过滤表单项宽度
const filterItemWidth = computed(() => {
  if (!props.filterConfig) return '200px' // 默认宽度
  
  // 如果是FilterConfig类型且定义了itemWidth
  if ('items' in props.filterConfig && props.filterConfig.itemWidth !== undefined) {
    return props.filterConfig.itemWidth
  }
  
  return '200px' // 默认宽度
})

// 判断是否有头部内容
const hasHeaderContent = computed(() => {
  return props.title || props.enableViewSwitch || !!slots['header-left'] || !!slots['header-right']
})

const emit = defineEmits<BaseListEmits>()

// 视图类型
const viewType = ref<ViewType>(props.defaultViewType)
// 表格实例
const tableRef = ref<TableInstance>()
// 加载状态
const loading = ref(false)
// 列表数据
const list = ref<any[]>([])
// 总数
const total = ref(0)
// 当前页码
const currentPage = ref(1)
// 每页条数
const currentPageSize = ref(props.paginationConfig?.pageSize || 10)
// 过滤条件
const filterValues = ref<Record<string, any>>({})
// 排序条件
const sortInfo = ref<{ prop?: string, order?: string }>({})

// 合并请求参数
const requestParams = computed(() => {
  return {
    page: currentPage.value,
    limit: currentPageSize.value,
    ...filterValues.value,
    ...sortInfo.value,
    ...props.requestParams
  }
})

// 分页属性
const paginationProps = computed(() => {
  const { pageSize, ...rest } = props.paginationConfig || {}
  // 设置分页组件的中文文本
  return {
    ...rest,
    prevText: '上一页',
    nextText: '下一页',
    totalText: '共 {total} 条'
  }
})

// 获取slots
const slots = useSlots()

// 总条数（确保为数字）
const totalItems = computed(() => {
  return total.value || 0
})

// 获取列属性
const getColumnProps = (column: TableColumn) => {
  const { slot, headerSlot, formatter, ...rest } = column
  return rest
}

// 默认的响应处理函数
const defaultResponseHandler = (response: any) => {
  // 移除调试语句
  // debugger
  
  // 处理 AxiosResponse 类型响应
  if (response && typeof response === 'object' && response.data !== undefined && response.status !== undefined) {
    // 这是一个 AxiosResponse，提取 data 部分继续处理
    return defaultResponseHandler(response.data);
  }

  // 处理标准API响应格式 (ApiResponse)
  if (response && typeof response === 'object' && response.code !== undefined) {
    const apiResponse = response as ApiResponse<any>;
    if (apiResponse.code === 200 && apiResponse.data !== undefined) {
      const data = apiResponse.data;
      
      // 处理分页数据格式 (PaginationResponse)
      if (data && typeof data === 'object' && 'list' in data && 'total' in data) {
        return {
          list: (data as PaginationResponse<any>).list || [],
          total: (data as PaginationResponse<any>).total || 0
        };
      }
      
      // 处理数组格式
      if (Array.isArray(data)) {
        return {
          list: data,
          total: data.length
        };
      }
      
      // 处理对象格式（包含列表数据）
      if (data && typeof data === 'object') {
        if (Array.isArray(data.data)) {
          return {
            list: data.data,
            total: data.total || data.data.length
          };
        }
        
        // 如果data是对象但不包含预期的数据结构，尝试将其作为单个项目
        return {
          list: [data],
          total: 1
        };
      }
    }
    
    // API 请求失败或数据格式不符合预期
    console.error('API request failed or invalid data format:', apiResponse.message);
    return { list: [], total: 0 };
  }
  
  // 直接处理分页响应格式 (PaginationResponse)
  if (response && typeof response === 'object' && 'list' in response && 'total' in response) {
    return {
      list: response.list || [],
      total: response.total || 0
    };
  }

  // 处理直接返回数组的情况
  if (Array.isArray(response)) {
    return {
      list: response,
      total: response.length
    };
  }

  // 处理包含data属性且data为数组的情况
  if (response && typeof response === 'object' && Array.isArray(response.data)) {
    return {
      list: response.data,
      total: response.total || response.count || response.data.length
    };
  }
  
  // 处理单个对象的情况
  if (response && typeof response === 'object' && !Array.isArray(response)) {
    // 如果不是null且是一个普通对象，将其作为单个项目
    if (Object.keys(response).length > 0) {
      return {
        list: [response],
        total: 1
      };
    }
  }

  // 默认返回空数据
  console.warn('Response format not recognized:', response);
  return {
    list: [],
    total: 0
  };
}

// 获取列表数据
const fetchData = async () => {
  if (!props.requestApi) return

  loading.value = true
  try {
    const response = await props.requestApi(requestParams.value)
    // 使用自定义或默认的响应处理函数
    const handler = props.responseHandler || defaultResponseHandler
    const { list: dataList, total: dataTotal } = handler(response)
    list.value = dataList
    total.value = dataTotal || 0
  } catch (error) {
    console.error('Failed to fetch data:', error)
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 获取数据项的key
const getItemKey = (item: any) => {
  if (item.id) return item.id
  if (item.key) return item.key
  return JSON.stringify(item)
}

// 处理视图切换
const handleViewChange = (type: ViewType) => {
  viewType.value = type
  emit('view-change', type)
}

// 处理过滤条件变化
const handleFilterChange = (event: FilterChangeEvent) => {
  filterValues.value = event.values
  currentPage.value = 1
  emit('filter-change', event)
  fetchData()
}

// 处理排序变化
const handleSortChange = (sort: { prop: string, order: string }) => {
  sortInfo.value = sort
  emit('sort-change', sort)
  fetchData()
}

// 处理选择变化
const handleSelectionChange = (selection: any[]) => {
  emit('selection-change', selection)
}

// 处理每页条数变化
const handleSizeChange = (size: number) => {
  currentPageSize.value = size
  emit('size-change', size)
  fetchData()
}

// 处理页码变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  emit('page-change', page)
  fetchData()
}

// 监听请求参数变化
watch(() => props.requestParams, () => {
  currentPage.value = 1
  fetchData()
}, { deep: true })

// 初始化
onMounted(() => {
  fetchData()
})

// 暴露方法
defineExpose({
  refresh: fetchData,
  getList: () => list.value,
  getTotal: () => total.value,
  getCurrentPage: () => currentPage.value,
  getPageSize: () => currentPageSize.value,
  getFilterValues: () => filterValues.value,
  getSortInfo: () => sortInfo.value,
  getTableRef: () => tableRef.value
})
</script>

<style scoped>
.base-list {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.list-title {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.right-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.list-content {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.card-view {
  flex: 1;
  overflow: auto;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
  padding: 1px;
}

.card-item {
  height: 100%;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-table) {
  flex: 1;
}

:deep(.el-loading-mask) {
  background-color: var(--el-mask-color);
}
</style> 