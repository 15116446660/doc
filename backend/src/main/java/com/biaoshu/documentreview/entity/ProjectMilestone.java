package com.biaoshu.documentreview.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 项目里程碑实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "project_milestone")
@Schema(description = "项目里程碑信息")
public class ProjectMilestone extends BaseEntity {

    @Schema(description = "项目ID")
    @NotNull(message = "项目ID不能为空")
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Schema(description = "项目")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private Project project;

    @Schema(description = "里程碑名称")
    @NotBlank(message = "里程碑名称不能为空")
    @Size(max = 100, message = "里程碑名称长度不能超过100个字符")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Schema(description = "里程碑描述")
    @Size(max = 1000, message = "里程碑描述长度不能超过1000个字符")
    @Column(name = "description", length = 1000)
    private String description;

    @Schema(description = "里程碑状态：PENDING-待开始，IN_PROGRESS-进行中，COMPLETED-已完成，CANCELLED-已取消")
    @NotNull(message = "里程碑状态不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MilestoneStatus status = MilestoneStatus.PENDING;

    @Schema(description = "里程碑类型：PHASE-阶段性，DELIVERABLE-交付物，REVIEW-审核节点，APPROVAL-审批节点")
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private MilestoneType type = MilestoneType.PHASE;

    @Schema(description = "优先级：LOW-低，MEDIUM-中，HIGH-高，URGENT-紧急")
    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private ProjectPriority priority = ProjectPriority.MEDIUM;

    @Schema(description = "计划开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "planned_start_time")
    private LocalDateTime plannedStartTime;

    @Schema(description = "计划结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "planned_end_time")
    private LocalDateTime plannedEndTime;

    @Schema(description = "实际开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "actual_start_time")
    private LocalDateTime actualStartTime;

    @Schema(description = "实际结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "actual_end_time")
    private LocalDateTime actualEndTime;

    @Schema(description = "负责人ID")
    @Column(name = "assignee_id")
    private Long assigneeId;

    @Schema(description = "负责人")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id", insertable = false, updatable = false)
    private User assignee;

    @Schema(description = "完成百分比（0-100）")
    @Column(name = "progress")
    private Integer progress = 0;

    @Schema(description = "排序号")
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Schema(description = "是否关键路径")
    @Column(name = "is_critical")
    private Boolean isCritical = false;

    @Schema(description = "备注")
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 里程碑状态枚举
     */
    public enum MilestoneStatus {
        PENDING("待开始"),
        IN_PROGRESS("进行中"),
        COMPLETED("已完成"),
        CANCELLED("已取消");

        private final String description;

        MilestoneStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 里程碑类型枚举
     */
    public enum MilestoneType {
        PHASE("阶段性"),
        DELIVERABLE("交付物"),
        REVIEW("审核节点"),
        APPROVAL("审批节点");

        private final String description;

        MilestoneType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 项目优先级枚举（复用Project中的枚举）
     */
    public enum ProjectPriority {
        LOW("低"),
        MEDIUM("中"),
        HIGH("高"),
        URGENT("紧急");

        private final String description;

        ProjectPriority(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
