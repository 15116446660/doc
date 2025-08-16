// 通用API响应接口
export interface ApiResponse<T = any> {
  code: number;
  data: T;
  msg?: string;
}

// 分页参数接口
export interface PaginationParams {
  current?: number;
  size?: number;
}

// 分页响应接口
export interface PaginationResponse<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
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