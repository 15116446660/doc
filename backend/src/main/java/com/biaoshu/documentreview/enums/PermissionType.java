package com.biaoshu.documentreview.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 权限类型枚举
 * 
 * @author biaoshu
 */
@Getter
@AllArgsConstructor
public enum PermissionType {

    /**
     * 菜单权限
     */
    MENU("菜单权限"),

    /**
     * 按钮权限
     */
    BUTTON("按钮权限"),

    /**
     * 接口权限
     */
    API("接口权限"),

    /**
     * 数据权限
     */
    DATA("数据权限"),

    /**
     * 功能权限
     */
    FUNCTION("功能权限");

    private final String description;
}
