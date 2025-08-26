package com.biaoshu.documentreview.enums;

/**
 * 项目层级权限枚举
 */
public enum HierarchyPermission {
    
    // 部门权限
    DEPARTMENT_VIEW("department:view", "查看部门"),
    DEPARTMENT_CREATE("department:create", "创建部门"),
    DEPARTMENT_UPDATE("department:update", "更新部门"),
    DEPARTMENT_DELETE("department:delete", "删除部门"),
    DEPARTMENT_MANAGE("department:manage", "管理部门"),
    
    // 品类权限
    CATEGORY_VIEW("category:view", "查看品类"),
    CATEGORY_CREATE("category:create", "创建品类"),
    CATEGORY_UPDATE("category:update", "更新品类"),
    CATEGORY_DELETE("category:delete", "删除品类"),
    CATEGORY_MANAGE("category:manage", "管理品类"),
    
    // 子品类权限
    SUBCATEGORY_VIEW("subcategory:view", "查看子品类"),
    SUBCATEGORY_CREATE("subcategory:create", "创建子品类"),
    SUBCATEGORY_UPDATE("subcategory:update", "更新子品类"),
    SUBCATEGORY_DELETE("subcategory:delete", "删除子品类"),
    SUBCATEGORY_MANAGE("subcategory:manage", "管理子品类"),
    
    // 项目权限
    PROJECT_VIEW("project:view", "查看项目"),
    PROJECT_CREATE("project:create", "创建项目"),
    PROJECT_UPDATE("project:update", "更新项目"),
    PROJECT_DELETE("project:delete", "删除项目"),
    PROJECT_MANAGE("project:manage", "管理项目"),
    
    // 层级管理权限
    HIERARCHY_VIEW("hierarchy:view", "查看层级结构"),
    HIERARCHY_MANAGE("hierarchy:manage", "管理层级结构"),
    HIERARCHY_MOVE("hierarchy:move", "移动层级节点"),
    HIERARCHY_SORT("hierarchy:sort", "排序层级节点"),
    
    // 统计权限
    STATISTICS_VIEW("statistics:view", "查看统计信息"),
    STATISTICS_EXPORT("statistics:export", "导出统计数据"),
    
    // 系统管理权限
    SYSTEM_ADMIN("system:admin", "系统管理员");

    private final String code;
    private final String description;

    HierarchyPermission(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据权限代码获取权限枚举
     */
    public static HierarchyPermission fromCode(String code) {
        for (HierarchyPermission permission : values()) {
            if (permission.getCode().equals(code)) {
                return permission;
            }
        }
        throw new IllegalArgumentException("Unknown permission code: " + code);
    }

    /**
     * 检查是否为管理权限
     */
    public boolean isManagePermission() {
        return this.code.endsWith(":manage") || this == SYSTEM_ADMIN;
    }

    /**
     * 检查是否为查看权限
     */
    public boolean isViewPermission() {
        return this.code.endsWith(":view");
    }

    /**
     * 检查是否为创建权限
     */
    public boolean isCreatePermission() {
        return this.code.endsWith(":create");
    }

    /**
     * 检查是否为更新权限
     */
    public boolean isUpdatePermission() {
        return this.code.endsWith(":update");
    }

    /**
     * 检查是否为删除权限
     */
    public boolean isDeletePermission() {
        return this.code.endsWith(":delete");
    }

    /**
     * 获取节点类型相关的权限
     */
    public static HierarchyPermission[] getNodeTypePermissions(String nodeType) {
        switch (nodeType.toLowerCase()) {
            case "department":
                return new HierarchyPermission[]{
                    DEPARTMENT_VIEW, DEPARTMENT_CREATE, DEPARTMENT_UPDATE, 
                    DEPARTMENT_DELETE, DEPARTMENT_MANAGE
                };
            case "category":
                return new HierarchyPermission[]{
                    CATEGORY_VIEW, CATEGORY_CREATE, CATEGORY_UPDATE, 
                    CATEGORY_DELETE, CATEGORY_MANAGE
                };
            case "subcategory":
                return new HierarchyPermission[]{
                    SUBCATEGORY_VIEW, SUBCATEGORY_CREATE, SUBCATEGORY_UPDATE, 
                    SUBCATEGORY_DELETE, SUBCATEGORY_MANAGE
                };
            case "project":
                return new HierarchyPermission[]{
                    PROJECT_VIEW, PROJECT_CREATE, PROJECT_UPDATE, 
                    PROJECT_DELETE, PROJECT_MANAGE
                };
            default:
                return new HierarchyPermission[0];
        }
    }

    /**
     * 获取操作对应的权限
     */
    public static HierarchyPermission getOperationPermission(String nodeType, String operation) {
        String permissionCode = nodeType.toLowerCase() + ":" + operation.toLowerCase();
        try {
            return fromCode(permissionCode);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
