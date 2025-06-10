import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios'

// 创建 axios 实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API || '/', // api 的 base_url
  timeout: 5000 // 请求超时时间
})

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 在发送请求之前做些什么
    // 比如添加token
    // const token = localStorage.getItem('token')
    // if (token) {
    //   config.headers.Authorization = `Bearer ${token}`
    // }
    return config
  },
  (error) => {
    // 对请求错误做些什么
    console.log(error) // for debug
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    // 对响应数据做点什么
    // 如果是二进制流，则直接返回
    if (response.request.responseType === 'blob' || response.request.responseType === 'arraybuffer') {
      return res
    }

    // 根据code判断
    if (res.code && res.code !== 200) {
      // ElMessage({
      //   message: res.message || 'Error',
      //   type: 'error',
      //   duration: 5 * 1000
      // })
      console.error('API Error:', res.message)
      return Promise.reject(new Error(res.message || 'Error'))
    } else {
      // 只返回data部分
      return res.data
    }
  },
  (error) => {
    console.log('err' + error) // for debug
    // ElMessage({
    //   message: error.message,
    //   type: 'error',
    //   duration: 5 * 1000
    // })
    return Promise.reject(error)
  }
)

export default service 