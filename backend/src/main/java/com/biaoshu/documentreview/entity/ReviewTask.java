package com.biaoshu.documentreview.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.biaoshu.documentreview.enums.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评审任务实体
 *
 * @author biaoshu
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("review_tasks")
public class ReviewTask extends BaseEntity {

    /**
     * 任务名称
     */
    @TableField("task_name")
    private String taskName;

    /**
     * 任务描述
     */
    @TableField("task_description")
    private String taskDescription;

    /**
     * 业务类型
     */
    @TableField("business_type")
    private BusinessType businessType;

    /**
     * 密级
     */
    @TableField("confidentiality_level")
    private ConfidentialityLevel confidentialityLevel;

    /**
     * 优先级
     */
    @TableField("priority")
    private Priority priority;

    /**
     * 任务状态
     */
    @TableField("status")
    private TaskStatus status;

    /**
     * 评审模板ID
     */
    @TableField("review_template_id")
    private Long reviewTemplateId;

    /**
     * 关联项目ID
     */
    @TableField("project_id")
    private Long projectId;

    /**
     * 创建人ID
     */
    @TableField("creator_id")
    private Long creatorId;

    /**
     * 截止时间
     */
    @TableField("deadline")
    private LocalDateTime deadline;

    /**
     * 是否启用自动分派
     */
    @TableField("auto_assignment_enabled")
    private Boolean autoAssignmentEnabled;

    /**
     * 是否启用AI分析
     */
    @TableField("ai_analysis_enabled")
    private Boolean aiAnalysisEnabled;

    /**
     * 专家要求 (JSON格式)
     */
    @TableField("expert_requirements")
    private String expertRequirements;

    /**
     * 是否启用收敛闸门
     */
    @TableField("convergence_gate_enabled")
    private Boolean convergenceGateEnabled;

    /**
     * 收敛截止时间
     */
    @TableField("convergence_deadline")
    private LocalDateTime convergenceDeadline;

    /**
     * 评审结论
     */
    @TableField("review_conclusion")
    private ReviewConclusion reviewConclusion;

    /**
     * 结论原因
     */
    @TableField("conclusion_reason")
    private String conclusionReason;

    /**
     * 进度数据(JSON格式)
     */
    @TableField("progress_data")
    private String progressData;

    // ========== 关联对象 ==========

    /**
     * 创建人信息
     */
    @TableField(exist = false)
    private User creator;

    /**
     * 关联文档列表
     */
    @TableField(exist = false)
    private List<Document> documents;

    /**
     * 分派的专家列表
     */
    @TableField(exist = false)
    private List<ReviewTaskAssignment> assignments;

    /**
     * 评审问题列表
     */
    @TableField(exist = false)
    private List<ReviewIssue> issues;

    /**
     * 评审评论列表
     */
    @TableField(exist = false)
    private List<ReviewComment> comments;

    /**
     * 评审指标
     */
    @TableField(exist = false)
    private ReviewMetrics metrics;

    /**
     * 工作流信息
     */
    @TableField(exist = false)
    private ReviewWorkflow workflow;

    // ========== 计算属性 ==========

    /**
     * 获取任务进度百分比
     */
    public Integer getProgressPercentage() {
        if (issues == null || issues.isEmpty()) {
            return 0;
        }

        long resolvedCount = issues.stream()
            .filter(issue -> issue.getStatus() == IssueStatus.RESOLVED
                          || issue.getStatus() == IssueStatus.VERIFIED
                          || issue.getStatus() == IssueStatus.CLOSED)
            .count();

        return (int) Math.round((double) resolvedCount / issues.size() * 100);
    }

    /**
     * 获取剩余时间(小时)
     */
    public Long getRemainingHours() {
        if (deadline == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(deadline)) {
            return 0L;
        }

        return java.time.Duration.between(now, deadline).toHours();
    }

    /**
     * 是否超时
     */
    public Boolean isOverdue() {
        if (deadline == null) {
            return false;
        }

        return LocalDateTime.now().isAfter(deadline) &&
               (status != TaskStatus.COMPLETED && status != TaskStatus.CANCELLED);
    }

    /**
     * 获取分派的专家数量
     */
    public Integer getAssignedExpertCount() {
        return assignments != null ? assignments.size() : 0;
    }

    /**
     * 获取问题总数
     */
    public Integer getTotalIssueCount() {
        return issues != null ? issues.size() : 0;
    }

    /**
     * 获取未解决问题数
     */
    public Integer getOpenIssueCount() {
        if (issues == null) {
            return 0;
        }

        return (int) issues.stream()
            .filter(issue -> issue.getStatus() == IssueStatus.OPEN
                          || issue.getStatus() == IssueStatus.IN_PROGRESS)
            .count();
    }

    /**
     * 是否可以开始评审
     */
    public Boolean canStartReview() {
        return status == TaskStatus.PENDING || status == TaskStatus.EXPERT_ASSIGNMENT;
    }

    /**
     * 是否可以完成评审
     */
    public Boolean canCompleteReview() {
        return status == TaskStatus.IN_REVIEW && getOpenIssueCount() == 0;
    }

}
