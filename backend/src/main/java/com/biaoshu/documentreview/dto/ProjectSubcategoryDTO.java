package com.biaoshu.documentreview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目子品类数据传输对象
 */
@Data
@Schema(description = "项目子品类信息")
public class ProjectSubcategoryDTO {

    @Schema(description = "子品类ID")
    private Long id;

    @Schema(description = "子品类名称")
    @NotBlank(message = "子品类名称不能为空")
    @Size(max = 100, message = "子品类名称长度不能超过100个字符")
    private String name;

    @Schema(description = "子品类编码")
    @Size(max = 50, message = "子品类编码长度不能超过50个字符")
    private String code;

    @Schema(description = "子品类描述")
    @Size(max = 500, message = "子品类描述长度不能超过500个字符")
    private String description;

    @Schema(description = "所属品类ID")
    @NotNull(message = "所属品类不能为空")
    private Long categoryId;

    @Schema(description = "所属品类名称")
    private String categoryName;

    @Schema(description = "所属部门ID")
    private Long departmentId;

    @Schema(description = "所属部门名称")
    private String departmentName;

    @Schema(description = "子品类负责人ID")
    private Long managerId;

    @Schema(description = "子品类负责人姓名")
    private String managerName;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "子品类图标")
    @Size(max = 100, message = "子品类图标长度不能超过100个字符")
    private String icon;

    @Schema(description = "子品类颜色")
    @Size(max = 20, message = "子品类颜色长度不能超过20个字符")
    private String color;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "项目列表")
    private List<ProjectDTO> projects;

    @Schema(description = "项目数量")
    private Long projectCount;
}
