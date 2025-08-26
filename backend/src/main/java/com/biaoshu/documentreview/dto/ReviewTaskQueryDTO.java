package com.biaoshu.documentreview.dto;

import com.biaoshu.documentreview.entity.ReviewTask;

import java.time.LocalDateTime;

/**
 * 评审任务查询DTO
 */
public class ReviewTaskQueryDTO {

    private String taskName;
    private Long projectId;
    private Long documentId;
    private Long creatorId;
    private Long assigneeId;
    private ReviewTask.ReviewTaskStatus status;
    private ReviewTask.ReviewTaskPriority priority;
    private ReviewTask.AIAnalysisStatus aiAnalysisStatus;
    private LocalDateTime deadlineStart;
    private LocalDateTime deadlineEnd;
    private LocalDateTime createdAtStart;
    private LocalDateTime createdAtEnd;
    private Boolean aiAnalysisEnabled;
    private String keyword;

    // Getters and Setters
    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
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

    public ReviewTask.AIAnalysisStatus getAiAnalysisStatus() {
        return aiAnalysisStatus;
    }

    public void setAiAnalysisStatus(ReviewTask.AIAnalysisStatus aiAnalysisStatus) {
        this.aiAnalysisStatus = aiAnalysisStatus;
    }

    public LocalDateTime getDeadlineStart() {
        return deadlineStart;
    }

    public void setDeadlineStart(LocalDateTime deadlineStart) {
        this.deadlineStart = deadlineStart;
    }

    public LocalDateTime getDeadlineEnd() {
        return deadlineEnd;
    }

    public void setDeadlineEnd(LocalDateTime deadlineEnd) {
        this.deadlineEnd = deadlineEnd;
    }

    public LocalDateTime getCreatedAtStart() {
        return createdAtStart;
    }

    public void setCreatedAtStart(LocalDateTime createdAtStart) {
        this.createdAtStart = createdAtStart;
    }

    public LocalDateTime getCreatedAtEnd() {
        return createdAtEnd;
    }

    public void setCreatedAtEnd(LocalDateTime createdAtEnd) {
        this.createdAtEnd = createdAtEnd;
    }

    public Boolean getAiAnalysisEnabled() {
        return aiAnalysisEnabled;
    }

    public void setAiAnalysisEnabled(Boolean aiAnalysisEnabled) {
        this.aiAnalysisEnabled = aiAnalysisEnabled;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
