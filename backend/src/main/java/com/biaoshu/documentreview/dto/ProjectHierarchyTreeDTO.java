package com.biaoshu.documentreview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目层级树DTO
 */
@Data
@Schema(description = "项目层级树数据传输对象")
public class ProjectHierarchyTreeDTO {

    @Schema(description = "节点ID")
    private Long id;

    @Schema(description = "节点名称")
    private String name;

    @Schema(description = "节点编码")
    private String code;

    @Schema(description = "节点描述")
    private String description;

    @Schema(description = "节点类型：department, category, subcategory, project")
    private String nodeType;

    @Schema(description = "父节点ID")
    private Long parentId;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "颜色")
    private String color;

    @Schema(description = "负责人ID")
    private Long managerId;

    @Schema(description = "负责人姓名")
    private String managerName;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "子节点列表")
    private List<ProjectHierarchyTreeDTO> children;

    @Schema(description = "子节点数量")
    private Integer childrenCount;

    @Schema(description = "是否有子节点")
    private Boolean hasChildren;

    @Schema(description = "节点层级")
    private Integer level;

    @Schema(description = "节点路径")
    private String path;

    @Schema(description = "是否展开")
    private Boolean expanded = false;

    @Schema(description = "是否选中")
    private Boolean selected = false;

    @Schema(description = "扩展属性")
    private Object extra;

    // 部门特有属性
    @Schema(description = "部门层级")
    private Integer departmentLevel;

    // 项目特有属性
    @Schema(description = "项目状态")
    private String projectStatus;

    @Schema(description = "项目优先级")
    private String projectPriority;

    @Schema(description = "项目进度")
    private Integer projectProgress;

    @Schema(description = "项目预算")
    private Double projectBudget;

    @Schema(description = "计划开始时间")
    private LocalDateTime plannedStartTime;

    @Schema(description = "计划结束时间")
    private LocalDateTime plannedEndTime;

    @Schema(description = "实际开始时间")
    private LocalDateTime actualStartTime;

    @Schema(description = "实际结束时间")
    private LocalDateTime actualEndTime;

    @Schema(description = "项目标签")
    private String tags;

    /**
     * 构造函数 - 部门
     */
    public static ProjectHierarchyTreeDTO fromDepartment(Long id, String name, String code, String description,
                                                        Long parentId, Integer sortOrder, Boolean enabled,
                                                        Long managerId, String managerName, Integer level, String path,
                                                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        ProjectHierarchyTreeDTO dto = new ProjectHierarchyTreeDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setCode(code);
        dto.setDescription(description);
        dto.setNodeType("department");
        dto.setParentId(parentId);
        dto.setSortOrder(sortOrder);
        dto.setEnabled(enabled);
        dto.setManagerId(managerId);
        dto.setManagerName(managerName);
        dto.setLevel(level);
        dto.setPath(path);
        dto.setDepartmentLevel(level);
        dto.setCreatedAt(createdAt);
        dto.setUpdatedAt(updatedAt);
        return dto;
    }

    /**
     * 构造函数 - 品类
     */
    public static ProjectHierarchyTreeDTO fromCategory(Long id, String name, String code, String description,
                                                      Long departmentId, Integer sortOrder, Boolean enabled,
                                                      String icon, String color, Long managerId, String managerName,
                                                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        ProjectHierarchyTreeDTO dto = new ProjectHierarchyTreeDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setCode(code);
        dto.setDescription(description);
        dto.setNodeType("category");
        dto.setParentId(departmentId);
        dto.setSortOrder(sortOrder);
        dto.setEnabled(enabled);
        dto.setIcon(icon);
        dto.setColor(color);
        dto.setManagerId(managerId);
        dto.setManagerName(managerName);
        dto.setLevel(2);
        dto.setCreatedAt(createdAt);
        dto.setUpdatedAt(updatedAt);
        return dto;
    }

    /**
     * 构造函数 - 子品类
     */
    public static ProjectHierarchyTreeDTO fromSubcategory(Long id, String name, String code, String description,
                                                         Long categoryId, Integer sortOrder, Boolean enabled,
                                                         String icon, String color, Long managerId, String managerName,
                                                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        ProjectHierarchyTreeDTO dto = new ProjectHierarchyTreeDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setCode(code);
        dto.setDescription(description);
        dto.setNodeType("subcategory");
        dto.setParentId(categoryId);
        dto.setSortOrder(sortOrder);
        dto.setEnabled(enabled);
        dto.setIcon(icon);
        dto.setColor(color);
        dto.setManagerId(managerId);
        dto.setManagerName(managerName);
        dto.setLevel(3);
        dto.setCreatedAt(createdAt);
        dto.setUpdatedAt(updatedAt);
        return dto;
    }

    /**
     * 构造函数 - 项目
     */
    public static ProjectHierarchyTreeDTO fromProject(Long id, String name, String code, String description,
                                                     Long subcategoryId, Integer sortOrder, String status, String priority,
                                                     Integer progress, Double budget, String tags, Long managerId, String managerName,
                                                     LocalDateTime plannedStartTime, LocalDateTime plannedEndTime,
                                                     LocalDateTime actualStartTime, LocalDateTime actualEndTime,
                                                     LocalDateTime createdAt, LocalDateTime updatedAt) {
        ProjectHierarchyTreeDTO dto = new ProjectHierarchyTreeDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setCode(code);
        dto.setDescription(description);
        dto.setNodeType("project");
        dto.setParentId(subcategoryId);
        dto.setSortOrder(sortOrder);
        dto.setEnabled(true);
        dto.setManagerId(managerId);
        dto.setManagerName(managerName);
        dto.setLevel(4);
        dto.setProjectStatus(status);
        dto.setProjectPriority(priority);
        dto.setProjectProgress(progress);
        dto.setProjectBudget(budget);
        dto.setTags(tags);
        dto.setPlannedStartTime(plannedStartTime);
        dto.setPlannedEndTime(plannedEndTime);
        dto.setActualStartTime(actualStartTime);
        dto.setActualEndTime(actualEndTime);
        dto.setCreatedAt(createdAt);
        dto.setUpdatedAt(updatedAt);
        return dto;
    }
}
