package com.biaoshu.documentreview.entity;

import com.biaoshu.documentreview.enums.ReviewProcessStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 审核流程实例实体
 * 
 * @author biaoshu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "review_process", indexes = {
    @Index(name = "idx_process_number", columnList = "process_number", unique = true),
    @Index(name = "idx_document_id", columnList = "document_id"),
    @Index(name = "idx_template_id", columnList = "template_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_initiator_id", columnList = "initiator_id")
})
public class ReviewProcess extends BaseEntity {

    /**
     * 流程编号
     */
    @NotBlank(message = "流程编号不能为空")
    @Size(max = 100, message = "流程编号长度不能超过100个字符")
    @Column(name = "process_number", nullable = false, unique = true, length = 100)
    private String processNumber;

    /**
     * 文档ID
     */
    @NotNull(message = "文档ID不能为空")
    @Column(name = "document_id", nullable = false)
    private Long documentId;

    /**
     * 文档关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", insertable = false, updatable = false)
    private Document document;

    /**
     * 审核模板ID
     */
    @NotNull(message = "审核模板ID不能为空")
    @Column(name = "template_id", nullable = false)
    private Long templateId;

    /**
     * 审核模板关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", insertable = false, updatable = false)
    private ReviewTemplate reviewTemplate;

    /**
     * 流程状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReviewProcessStatus status = ReviewProcessStatus.PENDING;

    /**
     * 发起人ID
     */
    @NotNull(message = "发起人ID不能为空")
    @Column(name = "initiator_id", nullable = false)
    private Long initiatorId;

    /**
     * 发起人关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", insertable = false, updatable = false)
    private User initiator;

    /**
     * 当前步骤序号
     */
    @Column(name = "current_step_order")
    private Integer currentStepOrder = 1;

    /**
     * 流程开始时间
     */
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    /**
     * 流程结束时间
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * 流程截止时间
     */
    @Column(name = "deadline_at")
    private LocalDateTime deadlineAt;

    /**
     * 总超时时间（小时）
     */
    @Column(name = "total_timeout_hours")
    private Integer totalTimeoutHours;

    /**
     * 优先级（1-高，2-中，3-低）
     */
    @Column(name = "priority")
    private Integer priority = 2;

    /**
     * 紧急标识
     */
    @Column(name = "is_urgent")
    private Boolean isUrgent = false;

    /**
     * 流程结果（通过、驳回、撤回等）
     */
    @Column(name = "result", length = 50)
    private String result;

    /**
     * 流程结果说明
     */
    @Column(name = "result_reason", length = 1000)
    private String resultReason;

    /**
     * 工作流实例ID（Activiti流程实例ID）
     */
    @Column(name = "workflow_instance_id", length = 100)
    private String workflowInstanceId;

    /**
     * 流程变量（JSON格式存储）
     */
    @Column(name = "process_variables", columnDefinition = "JSON")
    private String processVariables;

    /**
     * 审核步骤实例列表
     */
    @OneToMany(mappedBy = "reviewProcess", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("stepOrder ASC")
    private List<ReviewStep> reviewSteps;

    /**
     * 备注
     */
    @Column(name = "remark", length = 1000)
    private String remark;
}
