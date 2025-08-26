package com.biaoshu.documentreview.service;

import com.biaoshu.documentreview.enums.HierarchyPermission;

import java.util.List;
import java.util.Set;

/**
 * 项目层级权限服务接口
 */
public interface HierarchyPermissionService {

    /**
     * 检查用户是否有指定权限
     */
    boolean hasPermission(Long userId, HierarchyPermission permission);

    /**
     * 检查用户是否有任一权限
     */
    boolean hasAnyPermission(Long userId, HierarchyPermission... permissions);

    /**
     * 检查用户是否有所有权限
     */
    boolean hasAllPermissions(Long userId, HierarchyPermission... permissions);

    /**
     * 获取用户的所有权限
     */
    Set<HierarchyPermission> getUserPermissions(Long userId);

    /**
     * 检查用户是否有数据权限（是否可以操作指定的数据）
     */
    boolean hasDataPermission(Long userId, String nodeType, Long nodeId);

    /**
     * 获取用户可以访问的部门ID列表
     */
    List<Long> getAccessibleDepartmentIds(Long userId);

    /**
     * 获取用户可以访问的品类ID列表
     */
    List<Long> getAccessibleCategoryIds(Long userId);

    /**
     * 获取用户可以访问的子品类ID列表
     */
    List<Long> getAccessibleSubcategoryIds(Long userId);

    /**
     * 获取用户可以访问的项目ID列表
     */
    List<Long> getAccessibleProjectIds(Long userId);

    /**
     * 检查用户是否为系统管理员
     */
    boolean isSystemAdmin(Long userId);

    /**
     * 检查用户是否为部门管理员
     */
    boolean isDepartmentManager(Long userId, Long departmentId);

    /**
     * 检查用户是否为品类管理员
     */
    boolean isCategoryManager(Long userId, Long categoryId);

    /**
     * 检查用户是否为子品类管理员
     */
    boolean isSubcategoryManager(Long userId, Long subcategoryId);

    /**
     * 检查用户是否为项目管理员
     */
    boolean isProjectManager(Long userId, Long projectId);

    /**
     * 为用户授予权限
     */
    void grantPermission(Long userId, HierarchyPermission permission);

    /**
     * 撤销用户权限
     */
    void revokePermission(Long userId, HierarchyPermission permission);

    /**
     * 为角色授予权限
     */
    void grantRolePermission(Long roleId, HierarchyPermission permission);

    /**
     * 撤销角色权限
     */
    void revokeRolePermission(Long roleId, HierarchyPermission permission);

    /**
     * 获取角色的所有权限
     */
    Set<HierarchyPermission> getRolePermissions(Long roleId);

    /**
     * 为用户分配角色
     */
    void assignRole(Long userId, Long roleId);

    /**
     * 取消用户角色
     */
    void unassignRole(Long userId, Long roleId);

    /**
     * 获取用户的所有角色ID
     */
    List<Long> getUserRoleIds(Long userId);
}
