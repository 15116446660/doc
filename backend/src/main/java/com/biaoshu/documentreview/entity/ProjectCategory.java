package com.biaoshu.documentreview.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 项目分类实体类，用于表示部门、品类、子品类等层级结构
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "project_category")
@Schema(description = "项目分类，可表示部门、品类、子品类等")
public class ProjectCategory extends BaseEntity {

    @Schema(description = "分类名称")
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 100, message = "分类名称长度不能超过100个字符")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Schema(description = "分类类型：DEPARTMENT-部门, CATEGORY-品类, SUB_CATEGORY-子品类")
    @NotNull(message = "分类类型不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private CategoryType type;

    @Schema(description = "父分类ID")
    @Column(name = "parent_id")
    private Long parentId;

    @Schema(description = "父分类")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    @ToString.Exclude
    private ProjectCategory parent;

    @Schema(description = "子分类列表")
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<ProjectCategory> children;

    @Schema(description = "该分类下的项目列表")
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Project> projects;

    @Schema(description = "排序字段，值越小越靠前")
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * 分类类型枚举
     */
    public enum CategoryType {
        DEPARTMENT("部门"),
        CATEGORY("品类"),
        SUB_CATEGORY("子品类");

        private final String description;

        CategoryType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
