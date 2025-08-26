package com.biaoshu.documentreview.security;

import com.biaoshu.documentreview.enums.HierarchyPermission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 项目层级权限检查注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireHierarchyPermission {
    
    /**
     * 需要的权限
     */
    HierarchyPermission[] value();
    
    /**
     * 权限关系：AND（需要所有权限）或 OR（需要任一权限）
     */
    LogicalOperator logical() default LogicalOperator.OR;
    
    /**
     * 是否检查数据权限（是否只能操作自己管理的数据）
     */
    boolean checkDataPermission() default false;
    
    /**
     * 数据权限检查的参数名（用于获取要操作的数据ID）
     */
    String dataIdParam() default "id";
    
    /**
     * 节点类型（用于数据权限检查）
     */
    String nodeType() default "";
    
    enum LogicalOperator {
        AND, OR
    }
}
