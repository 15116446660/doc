package com.biaoshu.documentreview.dto;

import com.biaoshu.documentreview.entity.ReviewTask;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评审任务DTO
 */
public class ReviewTaskDTO {

    private Long id;
    private String taskName;
    private String taskDescription;
    private Long documentId;
    private String documentTitle;
    private Long projectId;
    private String projectName;
    private Long creatorId;
    private String creatorName;
    private ReviewTask.ReviewTaskStatus status;
    private ReviewTask.ReviewTaskPriority priority;
    private LocalDateTime deadline;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Boolean aiAnalysisEnabled;
    private ReviewTask.AIAnalysisStatus aiAnalysisStatus;
    private Integer aiAnalysisProgress;
    private String aiAnalysisResult;
    private String aiAnalysisError;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<ReviewTaskAssignmentDTO> assignments;
    private List<ReviewCommentDTO> comments;
    private List<AIAnalysisResultDTO> aiAnalysisResults;

    // 统计信息
    private Integer totalComments;
    private Integer unresolvedComments;
    private Integer totalAssignments;
    private Integer completedAssignments;

    /**
     * 评审任务分配DTO
     */
    public static class ReviewTaskAssignmentDTO {
        private Long id;
        private Long assigneeId;
        private String assigneeName;
        private String assigneeEmail;
        private Long assignerId;
        private String assignerName;
        private String role;
        private String status;
        private LocalDateTime assignedAt;
        private LocalDateTime acceptedAt;
        private LocalDateTime completedAt;
        private LocalDateTime deadline;
        private String notes;
        private String completionNotes;

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getAssigneeId() {
            return assigneeId;
        }

        public void setAssigneeId(Long assigneeId) {
            this.assigneeId = assigneeId;
        }

        public String getAssigneeName() {
            return assigneeName;
        }

        public void setAssigneeName(String assigneeName) {
            this.assigneeName = assigneeName;
        }

        public String getAssigneeEmail() {
            return assigneeEmail;
        }

        public void setAssigneeEmail(String assigneeEmail) {
            this.assigneeEmail = assigneeEmail;
        }

        public Long getAssignerId() {
            return assignerId;
        }

        public void setAssignerId(Long assignerId) {
            this.assignerId = assignerId;
        }

        public String getAssignerName() {
            return assignerName;
        }

        public void setAssignerName(String assignerName) {
            this.assignerName = assignerName;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDateTime getAssignedAt() {
            return assignedAt;
        }

        public void setAssignedAt(LocalDateTime assignedAt) {
            this.assignedAt = assignedAt;
        }

        public LocalDateTime getAcceptedAt() {
            return acceptedAt;
        }

        public void setAcceptedAt(LocalDateTime acceptedAt) {
            this.acceptedAt = acceptedAt;
        }

        public LocalDateTime getCompletedAt() {
            return completedAt;
        }

        public void setCompletedAt(LocalDateTime completedAt) {
            this.completedAt = completedAt;
        }

        public LocalDateTime getDeadline() {
            return deadline;
        }

        public void setDeadline(LocalDateTime deadline) {
            this.deadline = deadline;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public String getCompletionNotes() {
            return completionNotes;
        }

        public void setCompletionNotes(String completionNotes) {
            this.completionNotes = completionNotes;
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getDocumentTitle() {
        return documentTitle;
    }

    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public ReviewTask.ReviewTaskStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewTask.ReviewTaskStatus status) {
        this.status = status;
    }

    public ReviewTask.ReviewTaskPriority getPriority() {
        return priority;
    }

    public void setPriority(ReviewTask.ReviewTaskPriority priority) {
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

    public ReviewTask.AIAnalysisStatus getAiAnalysisStatus() {
        return aiAnalysisStatus;
    }

    public void setAiAnalysisStatus(ReviewTask.AIAnalysisStatus aiAnalysisStatus) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<ReviewTaskAssignmentDTO> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<ReviewTaskAssignmentDTO> assignments) {
        this.assignments = assignments;
    }

    public List<ReviewCommentDTO> getComments() {
        return comments;
    }

    public void setComments(List<ReviewCommentDTO> comments) {
        this.comments = comments;
    }

    public List<AIAnalysisResultDTO> getAiAnalysisResults() {
        return aiAnalysisResults;
    }

    public void setAiAnalysisResults(List<AIAnalysisResultDTO> aiAnalysisResults) {
        this.aiAnalysisResults = aiAnalysisResults;
    }

    public Integer getTotalComments() {
        return totalComments;
    }

    public void setTotalComments(Integer totalComments) {
        this.totalComments = totalComments;
    }

    public Integer getUnresolvedComments() {
        return unresolvedComments;
    }

    public void setUnresolvedComments(Integer unresolvedComments) {
        this.unresolvedComments = unresolvedComments;
    }

    public Integer getTotalAssignments() {
        return totalAssignments;
    }

    public void setTotalAssignments(Integer totalAssignments) {
        this.totalAssignments = totalAssignments;
    }

    public Integer getCompletedAssignments() {
        return completedAssignments;
    }

    public void setCompletedAssignments(Integer completedAssignments) {
        this.completedAssignments = completedAssignments;
    }
}
