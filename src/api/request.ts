import axios from 'axios'
import type { AxiosRequestConfig, AxiosResponse, AxiosError } from 'axios'
import { ElMessage, ElLoading } from 'element-plus'
import type { ApiResponse } from './types'

export interface RequestConfig extends AxiosRequestConfig {
  rawResponse?: boolean
}

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const config = response.config as RequestConfig
    const res = response.data

    if (res.code !== 200) {
      ElMessage.error(res.msg || 'Request failed')
      return Promise.reject(new Error(res.msg || 'Error'))
    }

    // If rawResponse is true, return the full axios response object
    if (config.rawResponse) {
      return response
    }

    // Default behavior: return only the 'data' part of the response body
    return res.data
  },
  (error: AxiosError<ApiResponse>) => {
    console.error('响应错误:', error)
    
    // 获取错误信息
    let message = '请求失败'
    if (error.response?.data?.msg) {
      message = error.response.data.msg
    } else if (error.message) {
      message = error.message
    }
    
    // 显示错误消息
    ElMessage({
      message,
      type: 'error',
      duration: 5 * 1000
    })
    
    // 处理特定状态码
    if (error.response?.status === 401) {
      // 未登录或token过期，可以在这里处理登出逻辑
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    
    return Promise.reject(error)
  }
)

// 封装GET请求
export function get<T>(url: string, params?: any, config?: RequestConfig): Promise<T> {
  return service.get(url, { params, ...config })
}

// 封装POST请求
export function post<T>(url: string, data?: any, config?: RequestConfig): Promise<T> {
  return service.post(url, data, config)
}

// 带加载状态的请求
export function requestWithLoading<T>(requestPromise: Promise<T>, loadingText = '加载中...'): Promise<T> {
  const loading = ElLoading.service({
    lock: true,
    text: loadingText,
    background: 'rgba(0, 0, 0, 0.7)'
  })
  
  return requestPromise
    .finally(() => {
      loading.close()
    })
}

// 文件上传配置接口
export interface UploadConfig extends AxiosRequestConfig {
  onProgress?: (progressEvent: any) => void;
  fileName?: string;
}

// 文件上传方法
export function upload(url: string, file: File | FormData, config: UploadConfig = {}) {
  const formData = file instanceof FormData ? file : new FormData();
  
  if (file instanceof File) {
    formData.append(config.fileName || 'file', file);
  }
  
  return service.post(url, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: config.onProgress,
    ...config
  });
}

// 批量文件上传方法
export function uploadMultiple(url: string, files: File[], config: UploadConfig = {}) {
  const formData = new FormData();
  files.forEach((file, index) => {
    formData.append(config.fileName || `file${index}`, file);
  });
  
  return service.post(url, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: config.onProgress,
    ...config
  });
}

// 文件下载方法（文件流）
export function download(url: string, fileName?: string, config: AxiosRequestConfig = {}) {
  return service.get(url, {
    responseType: 'blob',
    ...config
  }).then(response => {
    const blob = new Blob([response.data]);
    const downloadUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = fileName || getFileNameFromResponse(response) || 'download';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(downloadUrl);
    return response;
  });
}

// 从响应头中获取文件名
function getFileNameFromResponse(response: any): string {
  const contentDisposition = response?.headers?.['content-disposition'];
  if (contentDisposition) {
    const matches = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/.exec(contentDisposition);
    if (matches != null && matches[1]) {
      return matches[1].replace(/['"]/g, '');
    }
  }
  return '';
}

// 导出axios实例
export default service 