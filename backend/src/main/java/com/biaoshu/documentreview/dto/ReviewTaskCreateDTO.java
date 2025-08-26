package com.biaoshu.documentreview.dto;

import com.biaoshu.documentreview.entity.ReviewTask;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评审任务创建DTO
 */
public class ReviewTaskCreateDTO {

    @NotBlank(message = "任务名称不能为空")
    @Size(max = 200, message = "任务名称长度不能超过200字符")
    private String taskName;

    @Size(max = 2000, message = "任务描述长度不能超过2000字符")
    private String taskDescription;

    @NotNull(message = "文档ID不能为空")
    private Long documentId;

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    private ReviewTask.ReviewTaskPriority priority = ReviewTask.ReviewTaskPriority.MEDIUM;

    private LocalDateTime deadline;

    private Boolean aiAnalysisEnabled = true;

    private List<ReviewTaskAssignmentDTO> assignments;

    /**
     * 评审任务分配DTO
     */
    public static class ReviewTaskAssignmentDTO {
        @NotNull(message = "被分配人ID不能为空")
        private Long assigneeId;

        @NotNull(message = "评审角色不能为空")
        private String role;

        private LocalDateTime deadline;

        private String notes;

        // Getters and Setters
        public Long getAssigneeId() {
            return assigneeId;
        }

        public void setAssigneeId(Long assigneeId) {
            this.assigneeId = assigneeId;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
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

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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

    public Boolean getAiAnalysisEnabled() {
        return aiAnalysisEnabled;
    }

    public void setAiAnalysisEnabled(Boolean aiAnalysisEnabled) {
        this.aiAnalysisEnabled = aiAnalysisEnabled;
    }

    public List<ReviewTaskAssignmentDTO> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<ReviewTaskAssignmentDTO> assignments) {
        this.assignments = assignments;
    }
}
