import type { FormItemRule } from 'element-plus'

// Axios 响应类型
export interface AxiosResponse<T = any> {
  data: T
  status: number
  statusText: string
  headers: Record<string, string>
  config: any
  request?: any
}

// 过滤条件表单项类型
export type FilterFormItemType = 
  | 'input' 
  | 'select' 
  | 'date' 
  | 'daterange' 
  | 'number' 
  | 'checkbox' 
  | 'radio'
  | 'cascader'

// 数据源选项接口
export interface OptionItem {
  label: string
  value: any
  children?: OptionItem[]
  [key: string]: any
}

// 过滤条件表单项配置
export interface FilterFormItem {
  type: FilterFormItemType
  field: string
  label: string
  defaultValue?: any
  placeholder?: string
  options?: OptionItem[] | (() => Promise<OptionItem[]>)
  rules?: FormItemRule[]
  props?: Record<string, any> // 透传给表单控件的属性
  advanced?: boolean // 是否为高级筛选项
  colSpan?: number // 栅格布局占用列数
  labelWidth?: string | number // 单个表单项的标签宽度
  itemWidth?: string | number // 单个表单项的整体宽度
  [key: string]: any
}

// 过滤表单配置
export interface FilterConfig {
  labelWidth?: string | number // 统一的表单项标签宽度
  itemWidth?: string | number // 统一的表单项整体宽度
  items: FilterFormItem[] // 过滤条件表单项
}

// 分页配置
export interface PaginationConfig {
  pageSize: number
  pageSizes?: number[]
  layout?: string
  total?: number
  [key: string]: any
}

// 视图类型
export type ViewType = 'table' | 'cards'

// 表格列类型
export type ColumnType = 'selection' | 'index' | 'expand' | 'default'

// 表格列配置
export interface TableColumn {
  type?: ColumnType
  prop?: string
  label?: string
  width?: string | number
  minWidth?: string | number
  fixed?: boolean | 'left' | 'right'
  sortable?: boolean
  slot?: string // 自定义单元格插槽名
  headerSlot?: string // 自定义表头插槽名
  formatter?: (row: any, column: any, cellValue: any, index: number) => any
  align?: 'left' | 'center' | 'right'
  showOverflowTooltip?: boolean
  [key: string]: any
}

// 列表组件属性
export interface BaseListProps {
  // 过滤条件配置
  filterConfig?: FilterFormItem[] | FilterConfig
  // 是否启用高级搜索
  enableAdvancedFilter?: boolean
  // 是否启用视图切换
  enableViewSwitch?: boolean
  // 默认视图类型
  defaultViewType?: ViewType
  // 是否启用分页
  enablePagination?: boolean
  // 分页配置
  paginationConfig?: PaginationConfig
  // 请求API函数
  requestApi?: (...args: any[]) => Promise<any>
  // 请求参数
  requestParams?: Record<string, any>
  // 响应数据处理函数
  responseHandler?: (response: any) => { list: any[], total?: number }
  // 表格属性
  tableProps?: Record<string, any>
  // 卡片容器属性
  cardContainerProps?: Record<string, any>
  // 表格列配置
  columns?: TableColumn[]
  // 列表标题
  title?: string
}

// 过滤条件变更事件
export interface FilterChangeEvent {
  values: Record<string, any>
  isAdvanced: boolean
}

// 列表组件事件
export interface BaseListEmits {
  (e: 'filter-change', event: FilterChangeEvent): void
  (e: 'view-change', type: ViewType): void
  (e: 'page-change', page: number): void
  (e: 'size-change', size: number): void
  (e: 'selection-change', selection: any[]): void
  (e: 'sort-change', sort: { prop: string, order: string }): void
}

// 通用API响应接口
export interface ApiResponse<T = any> {
  code: number
  data: T
  message?: string
}

// 分页响应接口
export interface PaginationResponse<T = any> {
  list: T[]
  total: number
  page?: number
  limit?: number
} 