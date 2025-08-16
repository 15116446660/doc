package com.biaoshu.documentreview.entity;

import com.biaoshu.documentreview.enums.ReviewStepStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 审核步骤实例实体
 * 
 * @author biaoshu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "review_step", indexes = {
    @Index(name = "idx_process_id", columnList = "process_id"),
    @Index(name = "idx_step_order", columnList = "process_id,step_order", unique = true),
    @Index(name = "idx_assignee_id", columnList = "assignee_id"),
    @Index(name = "idx_status", columnList = "status")
})
public class ReviewStep extends BaseEntity {

    /**
     * 审核流程ID
     */
    @NotNull(message = "审核流程ID不能为空")
    @Column(name = "process_id", nullable = false)
    private Long processId;

    /**
     * 审核流程关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id", insertable = false, updatable = false)
    private ReviewProcess reviewProcess;

    /**
     * 步骤名称
     */
    @NotBlank(message = "步骤名称不能为空")
    @Size(max = 100, message = "步骤名称长度不能超过100个字符")
    @Column(name = "step_name", nullable = false, length = 100)
    private String stepName;

    /**
     * 步骤描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 步骤顺序
     */
    @NotNull(message = "步骤顺序不能为空")
    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    /**
     * 步骤状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReviewStepStatus status = ReviewStepStatus.PENDING;

    /**
     * 分派人ID
     */
    @Column(name = "assignee_id")
    private Long assigneeId;

    /**
     * 分派人关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id", insertable = false, updatable = false)
    private User assignee;

    /**
     * 实际审核人ID
     */
    @Column(name = "reviewer_id")
    private Long reviewerId;

    /**
     * 实际审核人关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", insertable = false, updatable = false)
    private User reviewer;

    /**
     * 步骤开始时间
     */
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    /**
     * 步骤完成时间
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * 步骤截止时间
     */
    @Column(name = "deadline_at")
    private LocalDateTime deadlineAt;

    /**
     * 步骤超时时间（小时）
     */
    @Column(name = "timeout_hours")
    private Integer timeoutHours;

    /**
     * 审核结果（通过、驳回、修改后再审等）
     */
    @Column(name = "result", length = 50)
    private String result;

    /**
     * 审核意见
     */
    @Column(name = "comment", length = 2000)
    private String comment;

    /**
     * 审核附件（JSON格式存储附件信息）
     */
    @Column(name = "attachments", columnDefinition = "JSON")
    private String attachments;

    /**
     * 是否并行审核
     */
    @Column(name = "is_parallel")
    private Boolean isParallel = false;

    /**
     * 并行审核组ID
     */
    @Column(name = "parallel_group_id")
    private String parallelGroupId;

    /**
     * 工作流任务ID
     */
    @Column(name = "workflow_task_id", length = 100)
    private String workflowTaskId;

    /**
     * 步骤变量（JSON格式存储）
     */
    @Column(name = "step_variables", columnDefinition = "JSON")
    private String stepVariables;

    /**
     * 备注
     */
    @Column(name = "remark", length = 1000)
    private String remark;
}
