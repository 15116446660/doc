package com.biaoshu.documentreview.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.biaoshu.documentreview.enums.IssueStatus;
import com.biaoshu.documentreview.enums.IssueSeverity;
import com.biaoshu.documentreview.enums.IssueType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评审问题实体
 * 
 * @author biaoshu
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("review_issues")
public class ReviewIssue extends BaseEntity {

    /**
     * 评审任务ID
     */
    @TableField("review_task_id")
    private Long reviewTaskId;

    /**
     * 文档ID
     */
    @TableField("document_id")
    private Long documentId;

    /**
     * 问题标题
     */
    @TableField("issue_title")
    private String issueTitle;

    /**
     * 问题描述
     */
    @TableField("issue_description")
    private String issueDescription;

    /**
     * 问题类型
     */
    @TableField("issue_type")
    private IssueType issueType;

    /**
     * 严重程度
     */
    @TableField("severity")
    private IssueSeverity severity;

    /**
     * 问题状态
     */
    @TableField("status")
    private IssueStatus status;

    /**
     * 位置信息(JSON格式)
     */
    @TableField("location_info")
    private String locationInfo;

    /**
     * 原始文本
     */
    @TableField("original_text")
    private String originalText;

    /**
     * 建议文本
     */
    @TableField("suggested_text")
    private String suggestedText;

    /**
     * 截止日期
     */
    @TableField("due_date")
    private LocalDateTime dueDate;

    /**
     * 报告人ID
     */
    @TableField("reporter_id")
    private Long reporterId;

    /**
     * 负责人ID
     */
    @TableField("assignee_id")
    private Long assigneeId;

    /**
     * 解决人ID
     */
    @TableField("resolver_id")
    private Long resolverId;

    /**
     * 报告时间
     */
    @TableField("reported_at")
    private LocalDateTime reportedAt;

    /**
     * 解决时间
     */
    @TableField("resolved_at")
    private LocalDateTime resolvedAt;

    /**
     * 验证时间
     */
    @TableField("verified_at")
    private LocalDateTime verifiedAt;

    /**
     * 解决说明
     */
    @TableField("resolution_notes")
    private String resolutionNotes;

    /**
     * 验证说明
     */
    @TableField("verification_notes")
    private String verificationNotes;

    /**
     * 标签(JSON数组)
     */
    @TableField("tags")
    private String tags;

    /**
     * 附件信息(JSON数组)
     */
    @TableField("attachments")
    private String attachments;

    // ========== 关联对象 ==========

    /**
     * 评审任务
     */
    @TableField(exist = false)
    private ReviewTask reviewTask;

    /**
     * 关联文档
     */
    @TableField(exist = false)
    private Document document;

    /**
     * 报告人
     */
    @TableField(exist = false)
    private User reporter;

    /**
     * 负责人
     */
    @TableField(exist = false)
    private User assignee;

    /**
     * 解决人
     */
    @TableField(exist = false)
    private User resolver;

    /**
     * 相关评论
     */
    @TableField(exist = false)
    private List<ReviewComment> comments;

    // ========== 计算属性 ==========

    /**
     * 是否为开放状态
     */
    public Boolean isOpen() {
        return status == IssueStatus.OPEN || status == IssueStatus.IN_PROGRESS;
    }

    /**
     * 是否已解决
     */
    public Boolean isResolved() {
        return status == IssueStatus.RESOLVED || status == IssueStatus.VERIFIED || status == IssueStatus.CLOSED;
    }

    /**
     * 是否为严重问题
     */
    public Boolean isCritical() {
        return severity == IssueSeverity.CRITICAL;
    }

    /**
     * 是否超时未解决
     */
    public Boolean isOverdue() {
        if (isResolved()) {
            return false;
        }
        
        // 这里需要根据问题的SLA时间来判断
        // 简化处理，假设严重问题24小时内解决，其他问题72小时内解决
        LocalDateTime deadline = reportedAt.plusHours(severity == IssueSeverity.CRITICAL ? 24 : 72);
        return LocalDateTime.now().isAfter(deadline);
    }

    /**
     * 获取解决耗时（小时）
     */
    public Long getResolutionTimeHours() {
        if (reportedAt == null || resolvedAt == null) {
            return null;
        }
        
        return java.time.Duration.between(reportedAt, resolvedAt).toHours();
    }

    /**
     * 获取当前处理时长（小时）
     */
    public Long getCurrentProcessingTimeHours() {
        if (reportedAt == null) {
            return null;
        }
        
        LocalDateTime endTime = resolvedAt != null ? resolvedAt : LocalDateTime.now();
        return java.time.Duration.between(reportedAt, endTime).toHours();
    }
}
