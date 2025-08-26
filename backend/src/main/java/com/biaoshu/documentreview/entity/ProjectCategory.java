package com.biaoshu.documentreview.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 项目品类实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "project_category")
@Schema(description = "项目品类信息")
public class ProjectCategory extends BaseEntity {

    @Schema(description = "品类名称")
    @NotBlank(message = "品类名称不能为空")
    @Size(max = 100, message = "品类名称长度不能超过100个字符")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Schema(description = "品类编码")
    @Size(max = 50, message = "品类编码长度不能超过50个字符")
    @Column(name = "code", length = 50, unique = true)
    private String code;

    @Schema(description = "品类描述")
    @Size(max = 500, message = "品类描述长度不能超过500个字符")
    @Column(name = "description", length = 500)
    private String description;

    @Schema(description = "所属部门ID")
    @NotNull(message = "所属部门不能为空")
    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @Schema(description = "所属部门")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", insertable = false, updatable = false)
    private Department department;

    @Schema(description = "品类负责人ID")
    @Column(name = "manager_id")
    private Long managerId;

    // 注释掉User关联，避免循环依赖
    // @Schema(description = "品类负责人")
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "manager_id", insertable = false, updatable = false)
    // private User manager;

    @Schema(description = "排序号")
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Schema(description = "是否启用")
    @Column(name = "enabled")
    private Boolean enabled = true;

    @Schema(description = "品类图标")
    @Size(max = 100, message = "品类图标长度不能超过100个字符")
    @Column(name = "icon", length = 100)
    private String icon;

    @Schema(description = "品类颜色")
    @Size(max = 20, message = "品类颜色长度不能超过20个字符")
    @Column(name = "color", length = 20)
    private String color;

    @Schema(description = "子品类列表")
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProjectSubcategory> subcategories;
}
