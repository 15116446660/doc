<template>
  <div class="base-list">
    「『viewType]] {{ viewType }}
    <!-- 视图切换和操作按钮 -->
    <div v-if="hasHeaderContent" class="list-header">
      <div class="left-section">
        <slot name="header-left">
          <h2 v-if="title" class="list-title">{{ title }}</h2>
        </slot>
      </div>
      <div class="right-section">
        <!-- 工具栏 -->
        <div class="header-toolbar">
          <slot name="toolbar"></slot>
        </div>
        <slot name="header-right">
          <!-- 过滤按钮 -->
          <div v-if="!props.showFilterBar && filterItems.length > 0" class="filter-toggle">
            <el-tooltip content="显示/隐藏过滤条件" placement="bottom">
              <button 
                class="view-btn" 
                :class="{ 'active': showFilter }"
                @click="toggleFilter"
              >
                <el-icon><Filter /></el-icon>
              </button>
            </el-tooltip>
          </div>
          <div v-if="enableViewSwitch" class="view-toggle">
            <div class="view-toggle-buttons">
              <button 
                class="view-btn" 
                :class="{ 'active': viewType === 'cards' }"
                @click="handleViewChange('cards')"
              >
                <el-icon><Grid /></el-icon>
              </button>
              <button 
                class="view-btn" 
                :class="{ 'active': viewType === 'table' }"
                @click="handleViewChange('table')"
              >
                <el-icon><List /></el-icon>
              </button>
            </div>
          </div>
        </slot>
      </div>
    </div>

    <!-- 过滤条件 -->
    <transition name="filter-fade">
      <list-filter
        v-if="filterItems.length > 0 && ((props.showFilterBar) || (!props.showFilterBar && showFilter))"
        :filter-config="filterItems"
        :enable-advanced-filter="enableAdvancedFilter"
        :label-width="filterLabelWidth"
        :item-width="filterItemWidth"
        @filter-change="handleFilterChange"
        class="filter-section"
      />
    </transition>

    <!-- 列表内容 -->
    <div class="list-content" v-loading="loading">
      <!-- 表格视图 -->
      <el-table
        v-if="props.viewType === 'table'"
        ref="tableRef"
        v-bind="tableProps"
        :data="list"
        stripe
        border
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
      <div v-else class="card-view">
        <div
          v-for="item in list"
          :key="getItemKey(item)"
          class="card-item"
        >
          <slot name="card" :item="item" />
        </div>
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
import { List, Grid, Filter } from '@element-plus/icons-vue'
import type { TableInstance } from 'element-plus'
import ListFilter from './components/ListFilter.vue'
import type { 
  BaseListProps, 
  BaseListEmits,
  ViewType,
  FilterChangeEvent,
  TableColumn,
  FilterFormItem,
} from './types'

const props = withDefaults(defineProps<BaseListProps>(), {
  viewType: 'table',
  filterConfig: undefined,
  enableAdvancedFilter: false,
  enableViewSwitch: true,
  enablePagination: true,
  cardLayout: 'horizontal',
  cardConfig: () => ({ gutter: 16, column: { xs: 24, sm: 12, md: 8, lg: 6, xl: 4 } }),
  showFilterBar: false,
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
  title: undefined,
  defaultViewType: 'table'
})

// 控制过滤器显示
const showFilter = ref(!props.showFilterBar)

// 切换过滤器显示状态
const toggleFilter = () => {
  showFilter.value = !showFilter.value
}

// 提取过滤条件配置
const filterItems = computed<FilterFormItem[]>(() => {
  if (!props.filterConfig) return []
  
  // 如果是FilterConfig类型
  if ('items' in props.filterConfig) {
    return (props.filterConfig as any).items
  }
  
  // 如果是FilterFormItem[]类型
  return props.filterConfig as FilterFormItem[]
})

// 提取过滤表单标签宽度
const filterLabelWidth = computed(() => {
  if (!props.filterConfig) return '80px' // 默认宽度
  
  // 如果是FilterConfig类型且定义了labelWidth
  if ('items' in props.filterConfig && (props.filterConfig as any).labelWidth !== undefined) {
    return (props.filterConfig as any).labelWidth
  }
  
  return '80px' // 默认宽度
})

// 提取过滤表单项宽度
const filterItemWidth = computed(() => {
  if (!props.filterConfig) return '200px' // 默认宽度
  
  // 如果是FilterConfig类型且定义了itemWidth
  if ('items' in props.filterConfig && (props.filterConfig as any).itemWidth !== undefined) {
    return (props.filterConfig as any).itemWidth
  }
  
  return '200px' // 默认宽度
})

// 判断是否有头部内容
const hasHeaderContent = computed(() => {
  return props.title || props.enableViewSwitch || !!slots['header-left'] || !!slots['header-right'] || (!props.showFilterBar && filterItems.value.length > 0)
})

const emit = defineEmits<BaseListEmits>()

// 视图类型
const viewType = ref<ViewType>(props.viewType || props.defaultViewType)
watch(
  () => props.viewType,
  (newType) => {
    if (newType) {
      viewType.value = newType
    }
  }
)

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
// 根据视图类型获取页面大小
const getPageSizeByViewType = (type: ViewType) => {
  if (props.paginationConfig?.pageSize) {
    return props.paginationConfig.pageSize
  }
  return type === 'card' ? 12 : 10
}
// 当前页面大小
const currentPageSize = ref(getPageSizeByViewType(viewType.value))
// 过滤条件
const filterValues = ref<Record<string, any>>({})
// 排序条件
const sortInfo = ref<{ prop?: string, order?: string }>({})

// 合并请求参数
const requestParams = computed(() => {
  return {
    pageNum: currentPage.value,
    pageSize: currentPageSize.value,
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

// 默认响应处理函数
const defaultResponseHandler = (response: any) => {
  // 处理不同响应格式
  if (response) {
    // 如果是标准API响应格式
    if (response.records && typeof response.total !== 'undefined') {
      return {
        list: response.records,
        total: response.total
      }
    } else if (response.list && typeof response.total !== 'undefined') {
      // 如果是标准分页响应
      return {
        list: response.list,
        total: response.total
      }
    } else if (Array.isArray(response)) {
      // 如果直接返回数组
      return {
        list: response,
        total: response.length
      }
    }
  }
  
  console.warn('Unexpected response format', response)
  return {
    list: [],
    total: 0
  }
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
  // 切换视图时调整页面大小
  currentPageSize.value = getPageSizeByViewType(type)
  emit('view-change', type)
  fetchData()
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
  getTableRef: () => tableRef.value,
  toggleFilter,
  getShowFilter: () => showFilter.value
})

// 获取卡片网格列配置
const getCardGridColumns = () => {
  if (props.viewType === 'cards') {
    // 如果提供了自定义的网格模板列，直接使用
    if (props.cardConfig?.gridTemplateColumns) {
      return props.cardConfig.gridTemplateColumns
    }
    
    // 使用配置的填充模式或默认值
    const fillMode = props.cardConfig?.gridFillMode || 'auto-fill'
    
    if (props.cardLayout === 'horizontal') {
      // 使用配置的最小宽度或默认值
      const minWidth = props.cardConfig?.minWidth || '320px'
      return `repeat(${fillMode}, minmax(${minWidth}, 1fr))`
    } else {
      // 垂直布局使用更小的宽度
      const minWidth = props.cardConfig?.minWidth || '220px'
      return `repeat(${fillMode}, minmax(${minWidth}, 1fr))`
    }
  }
  return 'repeat(auto-fill, minmax(280px, 1fr))'
}

// 获取卡片间距
const getCardGap = () => {
  if (props.cardConfig?.gap !== undefined) {
    return props.cardConfig.gap
  }
  return props.viewType === 'cards' && props.cardLayout === 'horizontal' ? '16px' : '12px'
}

// 获取卡片高度样式
const getCardHeight = () => {
  const minHeight = props.cardConfig?.minHeight || (props.cardLayout === 'horizontal' ? '180px' : '220px')
  const maxHeight = props.cardConfig?.maxHeight || 'auto'
  
  return {
    height: 'auto',
    minHeight,
    maxHeight
  }
}

watch(viewType, (newType) => {
  currentPageSize.value = getPageSizeByViewType(newType)
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

.list-toolbar {
  margin: 16px 0;
  display: flex;
  justify-content: flex-start;
  align-items: center;
  gap: 12px;
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

.header-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
}

.list-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 8px;
  overflow: hidden;
}

.card-view {
  flex: 1;
  overflow: auto;
  display: grid;
  grid-template-columns: v-bind('getCardGridColumns()');
  gap: v-bind('getCardGap()');
  padding: 8px;
  justify-content: start;
  
  @media screen and (max-width: 1600px) {
    justify-content: start;
  }
  
  @media screen and (max-width: 640px) {
    grid-template-columns: 1fr;
    gap: 16px;
    
    > * {
      max-width: 100%;
    }
  }
  
  > * {
    height: v-bind('getCardHeight().height');
    min-height: v-bind('getCardHeight().minHeight');
    width: 100%;
  }
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

.view-toggle, .filter-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4px;
  background-color: var(--el-fill-color-light);
  border-radius: 4px;
}

.view-toggle-buttons {
  display: flex;
  background-color: var(--el-fill-color-light);
  border-radius: 4px;
  padding: 2px;
}

.view-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s ease;
  color: var(--el-text-color-secondary);
}

.view-btn.active {
  background-color: var(--el-color-primary);
  color: var(--el-color-white);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.view-btn:hover:not(.active) {
  background-color: var(--el-fill-color);
  color: var(--el-text-color-primary);
}

.filter-toggle {
  margin-right: 8px;
}

.filter-section {
  transition: all 0.3s ease-in-out;
  overflow: hidden;
  margin-bottom: 16px;
}

.filter-fade-enter-active,
.filter-fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
  max-height: 300px;
}

.filter-fade-enter-from,
.filter-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
  max-height: 0;
}
</style> 