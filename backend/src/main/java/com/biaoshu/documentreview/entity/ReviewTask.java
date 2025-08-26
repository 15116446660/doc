package com.biaoshu.documentreview.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评审任务实体
 */
@Entity
@Table(name = "review_tasks")
public class ReviewTask extends BaseEntity {

    @Column(name = "task_name", nullable = false, length = 200)
    private String taskName;

    @Column(name = "task_description", columnDefinition = "TEXT")
    private String taskDescription;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", insertable = false, updatable = false)
    private Document document;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private Project project;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", insertable = false, updatable = false)
    private User creator;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReviewTaskStatus status = ReviewTaskStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private ReviewTaskPriority priority = ReviewTaskPriority.MEDIUM;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "ai_analysis_enabled", nullable = false)
    private Boolean aiAnalysisEnabled = true;

    @Column(name = "ai_analysis_status")
    @Enumerated(EnumType.STRING)
    private AIAnalysisStatus aiAnalysisStatus = AIAnalysisStatus.PENDING;

    @Column(name = "ai_analysis_progress")
    private Integer aiAnalysisProgress = 0;

    @Column(name = "ai_analysis_result", columnDefinition = "TEXT")
    private String aiAnalysisResult;

    @Column(name = "ai_analysis_error", columnDefinition = "TEXT")
    private String aiAnalysisError;

    @OneToMany(mappedBy = "reviewTask", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReviewTaskAssignment> assignments;

    @OneToMany(mappedBy = "reviewTask", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReviewComment> comments;

    @OneToMany(mappedBy = "reviewTask", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AIAnalysisResult> aiAnalysisResults;

    // 评审任务状态枚举
    public enum ReviewTaskStatus {
        PENDING("待开始"),
        IN_PROGRESS("进行中"),
        UNDER_REVIEW("评审中"),
        COMPLETED("已完成"),
        CANCELLED("已取消"),
        REJECTED("已拒绝");

        private final String description;

        ReviewTaskStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 评审任务优先级枚举
    public enum ReviewTaskPriority {
        LOW("低"),
        MEDIUM("中"),
        HIGH("高"),
        URGENT("紧急");

        private final String description;

        ReviewTaskPriority(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // AI分析状态枚举
    public enum AIAnalysisStatus {
        PENDING("待分析"),
        RUNNING("分析中"),
        COMPLETED("已完成"),
        FAILED("分析失败"),
        CANCELLED("已取消");

        private final String description;

        AIAnalysisStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Getters and Setters
    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public ReviewTaskStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewTaskStatus status) {
        this.status = status;
    }

    public ReviewTaskPriority getPriority() {
        return priority;
    }

    public void setPriority(ReviewTaskPriority priority) {
        this.priority = priority;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Boolean getAiAnalysisEnabled() {
        return aiAnalysisEnabled;
    }

    public void setAiAnalysisEnabled(Boolean aiAnalysisEnabled) {
        this.aiAnalysisEnabled = aiAnalysisEnabled;
    }

    public AIAnalysisStatus getAiAnalysisStatus() {
        return aiAnalysisStatus;
    }

    public void setAiAnalysisStatus(AIAnalysisStatus aiAnalysisStatus) {
        this.aiAnalysisStatus = aiAnalysisStatus;
    }

    public Integer getAiAnalysisProgress() {
        return aiAnalysisProgress;
    }

    public void setAiAnalysisProgress(Integer aiAnalysisProgress) {
        this.aiAnalysisProgress = aiAnalysisProgress;
    }

    public String getAiAnalysisResult() {
        return aiAnalysisResult;
    }

    public void setAiAnalysisResult(String aiAnalysisResult) {
        this.aiAnalysisResult = aiAnalysisResult;
    }

    public String getAiAnalysisError() {
        return aiAnalysisError;
    }

    public void setAiAnalysisError(String aiAnalysisError) {
        this.aiAnalysisError = aiAnalysisError;
    }

    public List<ReviewTaskAssignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<ReviewTaskAssignment> assignments) {
        this.assignments = assignments;
    }

    public List<ReviewComment> getComments() {
        return comments;
    }

    public void setComments(List<ReviewComment> comments) {
        this.comments = comments;
    }

    public List<AIAnalysisResult> getAiAnalysisResults() {
        return aiAnalysisResults;
    }

    public void setAiAnalysisResults(List<AIAnalysisResult> aiAnalysisResults) {
        this.aiAnalysisResults = aiAnalysisResults;
    }
}
