package com.biaoshu.documentreview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门数据传输对象
 */
@Data
@Schema(description = "部门信息")
public class DepartmentDTO {

    @Schema(description = "部门ID")
    private Long id;

    @Schema(description = "部门名称")
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 100, message = "部门名称长度不能超过100个字符")
    private String name;

    @Schema(description = "部门编码")
    @Size(max = 50, message = "部门编码长度不能超过50个字符")
    private String code;

    @Schema(description = "部门描述")
    @Size(max = 500, message = "部门描述长度不能超过500个字符")
    private String description;

    @Schema(description = "部门负责人ID")
    private Long managerId;

    @Schema(description = "部门负责人姓名")
    private String managerName;

    @Schema(description = "父部门ID")
    private Long parentId;

    @Schema(description = "父部门名称")
    private String parentName;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "部门层级")
    private Integer level;

    @Schema(description = "部门路径")
    private String path;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "子部门列表")
    private List<DepartmentDTO> children;

    @Schema(description = "品类列表")
    private List<ProjectCategoryDTO> categories;

    @Schema(description = "子部门数量")
    private Long childrenCount;

    @Schema(description = "品类数量")
    private Long categoryCount;
}
