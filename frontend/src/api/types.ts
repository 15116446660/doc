// 通用API响应接口
export interface ApiResponse<T = any> {
  code: number;
  data: T;
  message?: string;
  timestamp?: number;
}

// 分页参数接口
export interface PaginationParams {
  current?: number;
  size?: number;
}

// 分页响应接口
export interface PaginationResponse<T> {
  data: T[];
  pageNum: number;
  pageSize: number;
  total: number;
  pages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

// 通用查询参数接口
export interface QueryParams extends PaginationParams {
  [key: string]: any;
}

// 通用排序参数
export interface SortParams {
  prop?: string;
  order?: 'ascending' | 'descending' | null;
} 