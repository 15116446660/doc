import { computed } from 'vue'
import { useAuthStore } from '@/store/auth'

// 权限枚举
export enum HierarchyPermission {
  // 部门权限
  DEPARTMENT_VIEW = 'department:view',
  DEPARTMENT_CREATE = 'department:create',
  DEPARTMENT_UPDATE = 'department:update',
  DEPARTMENT_DELETE = 'department:delete',
  DEPARTMENT_MANAGE = 'department:manage',
  
  // 品类权限
  CATEGORY_VIEW = 'category:view',
  CATEGORY_CREATE = 'category:create',
  CATEGORY_UPDATE = 'category:update',
  CATEGORY_DELETE = 'category:delete',
  CATEGORY_MANAGE = 'category:manage',
  
  // 子品类权限
  SUBCATEGORY_VIEW = 'subcategory:view',
  SUBCATEGORY_CREATE = 'subcategory:create',
  SUBCATEGORY_UPDATE = 'subcategory:update',
  SUBCATEGORY_DELETE = 'subcategory:delete',
  SUBCATEGORY_MANAGE = 'subcategory:manage',
  
  // 项目权限
  PROJECT_VIEW = 'project:view',
  PROJECT_CREATE = 'project:create',
  PROJECT_UPDATE = 'project:update',
  PROJECT_DELETE = 'project:delete',
  PROJECT_MANAGE = 'project:manage',
  
  // 层级管理权限
  HIERARCHY_VIEW = 'hierarchy:view',
  HIERARCHY_MANAGE = 'hierarchy:manage',
  HIERARCHY_MOVE = 'hierarchy:move',
  HIERARCHY_SORT = 'hierarchy:sort',
  
  // 统计权限
  STATISTICS_VIEW = 'statistics:view',
  STATISTICS_EXPORT = 'statistics:export',
  
  // 系统管理权限
  SYSTEM_ADMIN = 'system:admin'
}

/**
 * 权限管理组合式函数
 */
export function usePermission() {
  const authStore = useAuthStore()

  /**
   * 检查是否有指定权限
   */
  const hasPermission = (permission: HierarchyPermission | string): boolean => {
    const userPermissions = authStore.permissions || []
    
    // 系统管理员拥有所有权限
    if (userPermissions.includes(HierarchyPermission.SYSTEM_ADMIN)) {
      return true
    }
    
    return userPermissions.includes(permission)
  }

  /**
   * 检查是否有任一权限
   */
  const hasAnyPermission = (...permissions: (HierarchyPermission | string)[]): boolean => {
    return permissions.some(permission => hasPermission(permission))
  }

  /**
   * 检查是否有所有权限
   */
  const hasAllPermissions = (...permissions: (HierarchyPermission | string)[]): boolean => {
    return permissions.every(permission => hasPermission(permission))
  }

  /**
   * 检查是否为系统管理员
   */
  const isSystemAdmin = computed(() => {
    return hasPermission(HierarchyPermission.SYSTEM_ADMIN)
  })

  /**
   * 检查是否有部门管理权限
   */
  const canManageDepartment = computed(() => {
    return hasAnyPermission(
      HierarchyPermission.DEPARTMENT_MANAGE,
      HierarchyPermission.SYSTEM_ADMIN
    )
  })

  /**
   * 检查是否有品类管理权限
   */
  const canManageCategory = computed(() => {
    return hasAnyPermission(
      HierarchyPermission.CATEGORY_MANAGE,
      HierarchyPermission.SYSTEM_ADMIN
    )
  })

  /**
   * 检查是否有子品类管理权限
   */
  const canManageSubcategory = computed(() => {
    return hasAnyPermission(
      HierarchyPermission.SUBCATEGORY_MANAGE,
      HierarchyPermission.SYSTEM_ADMIN
    )
  })

  /**
   * 检查是否有项目管理权限
   */
  const canManageProject = computed(() => {
    return hasAnyPermission(
      HierarchyPermission.PROJECT_MANAGE,
      HierarchyPermission.SYSTEM_ADMIN
    )
  })

  /**
   * 检查是否有层级管理权限
   */
  const canManageHierarchy = computed(() => {
    return hasAnyPermission(
      HierarchyPermission.HIERARCHY_MANAGE,
      HierarchyPermission.SYSTEM_ADMIN
    )
  })

  /**
   * 检查节点操作权限
   */
  const canOperateNode = (nodeType: string, operation: string): boolean => {
    const permission = `${nodeType}:${operation}` as HierarchyPermission
    return hasPermission(permission)
  }

  /**
   * 检查是否可以创建节点
   */
  const canCreateNode = (nodeType: string): boolean => {
    return canOperateNode(nodeType, 'create')
  }

  /**
   * 检查是否可以编辑节点
   */
  const canEditNode = (nodeType: string): boolean => {
    return canOperateNode(nodeType, 'update')
  }

  /**
   * 检查是否可以删除节点
   */
  const canDeleteNode = (nodeType: string): boolean => {
    return canOperateNode(nodeType, 'delete')
  }

  /**
   * 检查是否可以查看节点
   */
  const canViewNode = (nodeType: string): boolean => {
    return canOperateNode(nodeType, 'view')
  }

  /**
   * 检查数据权限（是否可以操作指定的数据）
   */
  const hasDataPermission = (nodeType: string, nodeId: number): boolean => {
    // 系统管理员拥有所有数据权限
    if (isSystemAdmin.value) {
      return true
    }

    // 这里需要根据实际业务逻辑实现数据权限检查
    // 例如：检查用户是否为该节点的负责人或上级负责人
    const userManagedNodes = authStore.managedNodes || {}
    const managedNodeIds = userManagedNodes[nodeType] || []
    
    return managedNodeIds.includes(nodeId)
  }

  /**
   * 获取用户可操作的节点类型
   */
  const getOperableNodeTypes = (): string[] => {
    const nodeTypes: string[] = []
    
    if (canViewNode('department')) nodeTypes.push('department')
    if (canViewNode('category')) nodeTypes.push('category')
    if (canViewNode('subcategory')) nodeTypes.push('subcategory')
    if (canViewNode('project')) nodeTypes.push('project')
    
    return nodeTypes
  }

  /**
   * 获取节点的可用操作
   */
  const getNodeOperations = (nodeType: string) => {
    const operations: string[] = []
    
    if (canViewNode(nodeType)) operations.push('view')
    if (canCreateNode(nodeType)) operations.push('create')
    if (canEditNode(nodeType)) operations.push('edit')
    if (canDeleteNode(nodeType)) operations.push('delete')
    
    return operations
  }

  /**
   * 权限指令（用于v-permission指令）
   */
  const checkDirectivePermission = (value: string | string[]): boolean => {
    if (Array.isArray(value)) {
      return hasAnyPermission(...value)
    }
    return hasPermission(value)
  }

  return {
    // 基础权限检查
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    
    // 角色检查
    isSystemAdmin,
    
    // 模块权限检查
    canManageDepartment,
    canManageCategory,
    canManageSubcategory,
    canManageProject,
    canManageHierarchy,
    
    // 节点操作权限
    canOperateNode,
    canCreateNode,
    canEditNode,
    canDeleteNode,
    canViewNode,
    
    // 数据权限
    hasDataPermission,
    
    // 工具方法
    getOperableNodeTypes,
    getNodeOperations,
    checkDirectivePermission
  }
}

/**
 * 权限指令
 */
export const permissionDirective = {
  mounted(el: HTMLElement, binding: any) {
    const { checkDirectivePermission } = usePermission()
    
    if (!checkDirectivePermission(binding.value)) {
      el.style.display = 'none'
    }
  },
  
  updated(el: HTMLElement, binding: any) {
    const { checkDirectivePermission } = usePermission()
    
    if (!checkDirectivePermission(binding.value)) {
      el.style.display = 'none'
    } else {
      el.style.display = ''
    }
  }
}
