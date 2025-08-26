package com.biaoshu.documentreview.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 部门实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "department")
@Schema(description = "部门信息")
public class Department extends BaseEntity {

    @Schema(description = "部门名称")
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 100, message = "部门名称长度不能超过100个字符")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Schema(description = "部门编码")
    @Size(max = 50, message = "部门编码长度不能超过50个字符")
    @Column(name = "code", length = 50, unique = true)
    private String code;

    @Schema(description = "部门描述")
    @Size(max = 500, message = "部门描述长度不能超过500个字符")
    @Column(name = "description", length = 500)
    private String description;

    @Schema(description = "部门负责人ID")
    @Column(name = "manager_id")
    private Long managerId;

    // 注释掉User关联，避免循环依赖
    // @Schema(description = "部门负责人")
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "manager_id", insertable = false, updatable = false)
    // private User manager;

    @Schema(description = "父部门ID")
    @Column(name = "parent_id")
    private Long parentId;

    @Schema(description = "父部门")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private Department parent;

    @Schema(description = "子部门列表")
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Department> children;

    @Schema(description = "排序号")
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Schema(description = "是否启用")
    @Column(name = "enabled")
    private Boolean enabled = true;

    @Schema(description = "部门层级")
    @Column(name = "level")
    private Integer level = 1;

    @Schema(description = "部门路径，用于快速查询层级关系")
    @Size(max = 500, message = "部门路径长度不能超过500个字符")
    @Column(name = "path", length = 500)
    private String path;

    @Schema(description = "部门下的品类列表")
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProjectCategory> categories;
}
