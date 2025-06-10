import type { User } from '@/types/user'

// 搜索用户
export const searchUsers = async (query: string): Promise<User[]> => {
  // TODO: 实现真实的用户搜索API调用
  // 这里先返回模拟数据
  return [
    {
      id: '1',
      name: '张三',
      avatar: '',
      department: '技术部',
      email: 'zhangsan@example.com'
    },
    {
      id: '2',
      name: '李四',
      avatar: '',
      department: '市场部',
      email: 'lisi@example.com'
    },
    {
      id: '3',
      name: '王五',
      avatar: '',
      department: '销售部',
      email: 'wangwu@example.com'
    }
  ].filter(user => user.name.includes(query))
} 