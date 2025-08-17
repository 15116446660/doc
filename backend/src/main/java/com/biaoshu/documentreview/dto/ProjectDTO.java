package com.biaoshu.documentreview.dto;

import com.biaoshu.documentreview.entity.Project;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目数据传输对象
 */
@Data
@Schema(description = "项目信息")
public class ProjectDTO {

    @Schema(description = "项目ID")
    private Long id;

    @Schema(description = "项目名称")
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100, message = "项目名称长度不能超过100个字符")
    private String name;

    @Schema(description = "项目编号")
    @Size(max = 50, message = "项目编号长度不能超过50个字符")
    private String code;

    @Schema(description = "项目描述")
    @Size(max = 1000, message = "项目描述长度不能超过1000个字符")
    private String description;

    @Schema(description = "项目状态")
    @NotNull(message = "项目状态不能为空")
    private Project.ProjectStatus status;

    @Schema(description = "项目优先级")
    private Project.ProjectPriority priority;

    @Schema(description = "项目负责人ID")
    private Long managerId;

    @Schema(description = "项目负责人姓名")
    private String managerName;

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

    @Schema(description = "项目预算")
    private Double budget;

    @Schema(description = "项目标签")
    private String tags;

    @Schema(description = "项目进度百分比")
    private Double progress;

    @Schema(description = "项目成员数量")
    private Integer memberCount;

    @Schema(description = "里程碑数量")
    private Integer milestoneCount;

    @Schema(description = "文档数量")
    private Integer documentCount;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @Schema(description = "创建人")
    private String createdBy;

    @Schema(description = "更新人")
    private String updatedBy;

    @Schema(description = "项目成员列表")
    private List<ProjectMemberDTO> members;

    @Schema(description = "项目里程碑列表")
    private List<ProjectMilestoneDTO> milestones;

    /**
     * 项目创建请求DTO
     */
    @Data
    @Schema(description = "项目创建请求")
    public static class CreateRequest {
        @Schema(description = "项目名称")
        @NotBlank(message = "项目名称不能为空")
        @Size(max = 100, message = "项目名称长度不能超过100个字符")
        private String name;

        @Schema(description = "项目编号")
        @Size(max = 50, message = "项目编号长度不能超过50个字符")
        private String code;

        @Schema(description = "项目描述")
        @Size(max = 1000, message = "项目描述长度不能超过1000个字符")
        private String description;

        @Schema(description = "项目优先级")
        private Project.ProjectPriority priority = Project.ProjectPriority.MEDIUM;

        @Schema(description = "项目负责人ID")
        private Long managerId;

        @Schema(description = "计划开始时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime plannedStartTime;

        @Schema(description = "计划结束时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime plannedEndTime;

        @Schema(description = "项目预算")
        private Double budget;

        @Schema(description = "项目标签")
        private String tags;

        @Schema(description = "项目成员ID列表")
        private List<Long> memberIds;
    }

    /**
     * 项目更新请求DTO
     */
    @Data
    @Schema(description = "项目更新请求")
    public static class UpdateRequest {
        @Schema(description = "项目名称")
        @Size(max = 100, message = "项目名称长度不能超过100个字符")
        private String name;

        @Schema(description = "项目编号")
        @Size(max = 50, message = "项目编号长度不能超过50个字符")
        private String code;

        @Schema(description = "项目描述")
        @Size(max = 1000, message = "项目描述长度不能超过1000个字符")
        private String description;

        @Schema(description = "项目状态")
        private Project.ProjectStatus status;

        @Schema(description = "项目优先级")
        private Project.ProjectPriority priority;

        @Schema(description = "项目负责人ID")
        private Long managerId;

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

        @Schema(description = "项目预算")
        private Double budget;

        @Schema(description = "项目标签")
        private String tags;
    }

    /**
     * 项目查询请求DTO
     */
    @Data
    @Schema(description = "项目查询请求")
    public static class QueryRequest {
        @Schema(description = "项目名称（模糊查询）")
        private String name;

        @Schema(description = "项目状态")
        private Project.ProjectStatus status;

        @Schema(description = "项目负责人ID")
        private Long managerId;

        @Schema(description = "项目优先级")
        private Project.ProjectPriority priority;

        @Schema(description = "分类ID")
        private Long categoryId;

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

        @Schema(description = "排序字段", example = "createdAt")
        private String sortBy = "createdAt";

        @Schema(description = "排序方向", example = "desc")
        private String sortDir = "desc";
    }
}
