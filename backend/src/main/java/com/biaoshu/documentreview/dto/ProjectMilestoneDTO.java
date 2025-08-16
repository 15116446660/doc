package com.biaoshu.documentreview.dto;

import com.biaoshu.documentreview.entity.ProjectMilestone;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目里程碑数据传输对象
 */
@Data
@Schema(description = "项目里程碑信息")
public class ProjectMilestoneDTO {

    @Schema(description = "里程碑ID")
    private Long id;

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "里程碑名称")
    private String name;

    @Schema(description = "里程碑描述")
    private String description;

    @Schema(description = "里程碑状态")
    private ProjectMilestone.MilestoneStatus status;

    @Schema(description = "里程碑类型")
    private ProjectMilestone.MilestoneType type;

    @Schema(description = "优先级")
    private ProjectMilestone.ProjectPriority priority;

    @Schema(description = "计划开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime plannedStartTime;

    @Schema(description = "计划结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime plannedEndTime;

    @Schema(description = "实际开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualStartTime;

    @Schema(description = "实际结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualEndTime;

    @Schema(description = "负责人ID")
    private Long assigneeId;

    @Schema(description = "负责人姓名")
    private String assigneeName;

    @Schema(description = "完成百分比")
    private Integer progress;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "是否关键路径")
    private Boolean isCritical;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 创建里程碑请求DTO
     */
    @Data
    @Schema(description = "创建里程碑请求")
    public static class CreateRequest {
        @Schema(description = "里程碑名称")
        @NotBlank(message = "里程碑名称不能为空")
        @Size(max = 100, message = "里程碑名称长度不能超过100个字符")
        private String name;

        @Schema(description = "里程碑描述")
        @Size(max = 1000, message = "里程碑描述长度不能超过1000个字符")
        private String description;

        @Schema(description = "里程碑类型")
        private ProjectMilestone.MilestoneType type = ProjectMilestone.MilestoneType.PHASE;

        @Schema(description = "优先级")
        private ProjectMilestone.ProjectPriority priority = ProjectMilestone.ProjectPriority.MEDIUM;

        @Schema(description = "计划开始时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime plannedStartTime;

        @Schema(description = "计划结束时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime plannedEndTime;

        @Schema(description = "负责人ID")
        private Long assigneeId;

        @Schema(description = "排序号")
        private Integer sortOrder;

        @Schema(description = "是否关键路径")
        private Boolean isCritical = false;

        @Schema(description = "备注")
        private String remark;
    }

    /**
     * 更新里程碑请求DTO
     */
    @Data
    @Schema(description = "更新里程碑请求")
    public static class UpdateRequest {
        @Schema(description = "里程碑名称")
        @Size(max = 100, message = "里程碑名称长度不能超过100个字符")
        private String name;

        @Schema(description = "里程碑描述")
        @Size(max = 1000, message = "里程碑描述长度不能超过1000个字符")
        private String description;

        @Schema(description = "里程碑状态")
        private ProjectMilestone.MilestoneStatus status;

        @Schema(description = "里程碑类型")
        private ProjectMilestone.MilestoneType type;

        @Schema(description = "优先级")
        private ProjectMilestone.ProjectPriority priority;

        @Schema(description = "计划开始时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime plannedStartTime;

        @Schema(description = "计划结束时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime plannedEndTime;

        @Schema(description = "实际开始时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime actualStartTime;

        @Schema(description = "实际结束时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime actualEndTime;

        @Schema(description = "负责人ID")
        private Long assigneeId;

        @Schema(description = "完成百分比")
        @Min(value = 0, message = "完成百分比不能小于0")
        @Max(value = 100, message = "完成百分比不能大于100")
        private Integer progress;

        @Schema(description = "排序号")
        private Integer sortOrder;

        @Schema(description = "是否关键路径")
        private Boolean isCritical;

        @Schema(description = "备注")
        private String remark;
    }

    /**
     * 里程碑查询请求DTO
     */
    @Data
    @Schema(description = "里程碑查询请求")
    public static class QueryRequest {
        @Schema(description = "项目ID")
        private Long projectId;

        @Schema(description = "里程碑名称（模糊查询）")
        private String name;

        @Schema(description = "里程碑状态")
        private ProjectMilestone.MilestoneStatus status;

        @Schema(description = "里程碑类型")
        private ProjectMilestone.MilestoneType type;

        @Schema(description = "负责人ID")
        private Long assigneeId;

        @Schema(description = "是否关键路径")
        private Boolean isCritical;

        @Schema(description = "计划开始时间（起始）")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime plannedStartTimeFrom;

        @Schema(description = "计划开始时间（结束）")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime plannedStartTimeTo;

        @Schema(description = "计划结束时间（起始）")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime plannedEndTimeFrom;

        @Schema(description = "计划结束时间（结束）")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime plannedEndTimeTo;

        @Schema(description = "页码", example = "1")
        private Integer page = 1;

        @Schema(description = "每页大小", example = "10")
        private Integer size = 10;

        @Schema(description = "排序字段", example = "sortOrder")
        private String sortBy = "sortOrder";

        @Schema(description = "排序方向", example = "asc")
        private String sortDir = "asc";
    }

    /**
     * 批量更新排序请求DTO
     */
    @Data
    @Schema(description = "批量更新排序请求")
    public static class BatchSortRequest {
        @Schema(description = "里程碑排序信息列表")
        @NotNull(message = "排序信息列表不能为空")
        private List<SortInfo> milestones;

        @Data
        @Schema(description = "排序信息")
        public static class SortInfo {
            @Schema(description = "里程碑ID")
            @NotNull(message = "里程碑ID不能为空")
            private Long id;

            @Schema(description = "排序号")
            @NotNull(message = "排序号不能为空")
            private Integer sortOrder;
        }
    }
}
