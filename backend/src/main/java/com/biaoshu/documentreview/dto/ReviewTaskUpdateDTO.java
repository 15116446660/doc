package com.biaoshu.documentreview.dto;

import com.biaoshu.documentreview.entity.ReviewTask;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 评审任务更新DTO
 */
public class ReviewTaskUpdateDTO {

    @Size(max = 200, message = "任务名称长度不能超过200字符")
    private String taskName;

    @Size(max = 2000, message = "任务描述长度不能超过2000字符")
    private String taskDescription;

    private ReviewTask.ReviewTaskPriority priority;

    private LocalDateTime deadline;

    private Boolean aiAnalysisEnabled;

    private ReviewTask.ReviewTaskStatus status;

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

    public ReviewTask.ReviewTaskStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewTask.ReviewTaskStatus status) {
        this.status = status;
    }
}
