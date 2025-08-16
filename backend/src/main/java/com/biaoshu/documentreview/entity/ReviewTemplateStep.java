package com.biaoshu.documentreview.entity;

import com.biaoshu.documentreview.enums.ReviewStepType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 审核模板步骤实体
 * 
 * @author biaoshu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "review_template_step", indexes = {
    @Index(name = "idx_template_id", columnList = "template_id"),
    @Index(name = "idx_step_order", columnList = "template_id,step_order", unique = true)
})
public class ReviewTemplateStep extends BaseEntity {

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
     * 步骤类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "step_type", nullable = false)
    private ReviewStepType stepType;

    /**
     * 审核人员配置（JSON格式存储）
     * 可以是用户ID列表、角色ID列表、部门ID列表等
     */
    @Column(name = "reviewers_config", columnDefinition = "JSON")
    private String reviewersConfig;

    /**
     * 是否必须步骤（不可跳过）
     */
    @Column(name = "is_required")
    private Boolean isRequired = true;

    /**
     * 是否并行审核
     */
    @Column(name = "is_parallel")
    private Boolean isParallel = false;

    /**
     * 并行审核通过条件（全部通过、多数通过等）
     */
    @Column(name = "parallel_pass_condition", length = 50)
    private String parallelPassCondition;

    /**
     * 步骤超时时间（小时）
     */
    @Column(name = "timeout_hours")
    private Integer timeoutHours = 24;

    /**
     * 自动分派规则（JSON格式存储）
     */
    @Column(name = "assignment_rules", columnDefinition = "JSON")
    private String assignmentRules;

    /**
     * 回避规则（JSON格式存储）
     */
    @Column(name = "avoidance_rules", columnDefinition = "JSON")
    private String avoidanceRules;

    /**
     * 替补规则（JSON格式存储）
     */
    @Column(name = "substitute_rules", columnDefinition = "JSON")
    private String substituteRules;

    /**
     * 步骤配置（JSON格式存储额外配置）
     */
    @Column(name = "step_config", columnDefinition = "JSON")
    private String stepConfig;

    /**
     * 是否启用AI辅助
     */
    @Column(name = "ai_enabled")
    private Boolean aiEnabled = false;

    /**
     * AI配置（JSON格式存储）
     */
    @Column(name = "ai_config", columnDefinition = "JSON")
    private String aiConfig;

    /**
     * 备注
     */
    @Column(name = "remark", length = 1000)
    private String remark;
}
