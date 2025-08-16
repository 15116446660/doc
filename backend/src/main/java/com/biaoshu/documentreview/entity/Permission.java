package com.biaoshu.documentreview.entity;

import com.biaoshu.documentreview.enums.PermissionType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

/**
 * 权限实体
 * 
 * @author biaoshu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_permission", indexes = {
    @Index(name = "idx_permission_code", columnList = "permission_code", unique = true),
    @Index(name = "idx_parent_id", columnList = "parent_id")
})
public class Permission extends BaseEntity {

    /**
     * 权限编码
     */
    @NotBlank(message = "权限编码不能为空")
    @Size(max = 100, message = "权限编码长度不能超过100个字符")
    @Column(name = "permission_code", nullable = false, unique = true, length = 100)
    private String permissionCode;

    /**
     * 权限名称
     */
    @NotBlank(message = "权限名称不能为空")
    @Size(max = 100, message = "权限名称长度不能超过100个字符")
    @Column(name = "permission_name", nullable = false, length = 100)
    private String permissionName;

    /**
     * 权限类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "permission_type", nullable = false)
    private PermissionType permissionType;

    /**
     * 父权限ID
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 权限路径
     */
    @Column(name = "permission_path", length = 200)
    private String permissionPath;

    /**
     * 权限描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 是否启用
     */
    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;

    /**
     * 排序
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * 权限角色关联
     */
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<Role> roles;
}
