package com.biaoshu.documentreview.dto.response;

import com.biaoshu.documentreview.entity.ReviewTask;
import com.biaoshu.documentreview.entity.User;
import com.biaoshu.documentreview.entity.Document;
import com.biaoshu.documentreview.entity.ReviewTaskAssignment;
import com.biaoshu.documentreview.entity.ReviewIssue;
import com.biaoshu.documentreview.entity.ReviewComment;
import com.biaoshu.documentreview.enums.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务详情响应DTO
 * 
 * @author biaoshu
 * @since 2024-01-01
 */
@Data
public class TaskDetailResponse {

    /**
     * 任务基本信息
     */
    private Long id;
    private String taskName;
    private String taskDescription;
    private BusinessType businessType;
    private ConfidentialityLevel confidentialityLevel;
    private Priority priority;
    private TaskStatus status;
    private Long reviewTemplateId;
    private Long projectId;
    private Long creatorId;
    private LocalDateTime deadline;
    private Boolean autoAssignmentEnabled;
    private Boolean convergenceGateEnabled;
    private LocalDateTime convergenceDeadline;
    private ReviewConclusion reviewConclusion;
    private String conclusionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 关联信息
     */
    private User creator;
    private String projectName;
    private String workflowName;
    private List<Document> documents;
    private List<ReviewTaskAssignment> assignments;
    private List<ReviewIssue> issues;
    private List<ReviewComment> comments;

    /**
     * 统计信息
     */
    private TaskStatistics statistics;

    /**
     * 进度信息
     */
    private TaskProgress progress;

    /**
     * 权限信息
     */
    private TaskPermissions permissions;

    /**
     * 任务统计信息
     */
    @Data
    public static class TaskStatistics {
        private Integer totalIssues;
        private Integer openIssues;
        private Integer resolvedIssues;
        private Integer criticalIssues;
        private Integer totalComments;
        private Integer assignedExperts;
        private Integer completedAssignments;
        private Double progressPercentage;
        private Long remainingHours;
        private Boolean isOverdue;
    }

    /**
     * 任务进度信息
     */
    @Data
    public static class TaskProgress {
        private String currentStage;
        private List<ProgressStep> steps;
        private Integer completedSteps;
        private Integer totalSteps;
        private Double completionRate;
        private LocalDateTime estimatedCompletion;
    }

    /**
     * 进度步骤
     */
    @Data
    public static class ProgressStep {
        private String stepName;
        private String stepDescription;
        private TaskStatus status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String assignee;
        private String notes;
    }

    /**
     * 任务权限信息
     */
    @Data
    public static class TaskPermissions {
        private Boolean canView;
        private Boolean canEdit;
        private Boolean canDelete;
        private Boolean canAssign;
        private Boolean canComment;
        private Boolean canResolveIssues;
        private Boolean canChangeStatus;
        private Boolean canExport;
    }
}
