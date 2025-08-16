import { post, get } from './request'

// 登录请求参数
export interface LoginRequest {
  usernameOrEmail: string
  password: string
  rememberMe?: boolean
}

// 用户信息
export interface UserInfo {
  id: number
  username: string
  realName: string
  email: string
  phone: string
  employeeId: string
  departmentId: number
  position: string
  avatarUrl: string
  roles: string[]
  permissions: string[]
}

// 登录响应
export interface LoginResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresAt: string
  userInfo: UserInfo
}

// 登录
export function login(data: LoginRequest): Promise<LoginResponse> {
  return post<LoginResponse>('/auth/login', data)
}

// 刷新Token
export function refreshToken(refreshToken: string): Promise<LoginResponse> {
  return post<LoginResponse>('/auth/refresh', null, {
    params: { refreshToken }
  })
}

// 登出
export function logout(): Promise<void> {
  return post<void>('/auth/logout')
}

// 验证Token
export function validateToken(): Promise<boolean> {
  return get<boolean>('/auth/validate')
}

// 获取当前用户信息
export function getCurrentUser(): Promise<UserInfo> {
  return get<UserInfo>('/test/current-user')
}
