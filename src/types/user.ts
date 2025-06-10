export interface User {
  id: string
  name: string
  avatar?: string
  department?: string
  email?: string
  role?: string
  status?: 'active' | 'inactive'
  createTime?: string
  updateTime?: string
}

export interface UserOption {
  label: string
  value: string
  avatar?: string
} 