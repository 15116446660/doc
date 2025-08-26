package com.biaoshu.documentreview.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 评审任务分配实体
 */
@Entity
@Table(name = "review_task_assignments")
public class ReviewTaskAssignment extends BaseEntity {

    @Column(name = "review_task_id", nullable = false)
    private Long reviewTaskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_task_id", insertable = false, updatable = false)
    private ReviewTask reviewTask;

    @Column(name = "assignee_id", nullable = false)
    private Long assigneeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id", insertable = false, updatable = false)
    private User assignee;

    @Column(name = "assigner_id", nullable = false)
    private Long assignerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigner_id", insertable = false, updatable = false)
    private User assigner;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private ReviewRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AssignmentStatus status = AssignmentStatus.PENDING;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "completion_notes", columnDefinition = "TEXT")
    private String completionNotes;

    // 评审角色枚举
    public enum ReviewRole {
        PRIMARY_REVIEWER("主评审员"),
        SECONDARY_REVIEWER("副评审员"),
        TECHNICAL_REVIEWER("技术评审员"),
        LEGAL_REVIEWER("法务评审员"),
        BUSINESS_REVIEWER("业务评审员"),
        OBSERVER("观察员");

        private final String description;

        ReviewRole(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 分配状态枚举
    public enum AssignmentStatus {
        PENDING("待接受"),
        ACCEPTED("已接受"),
        IN_PROGRESS("进行中"),
        COMPLETED("已完成"),
        REJECTED("已拒绝"),
        CANCELLED("已取消");

        private final String description;

        AssignmentStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Getters and Setters
    public Long getReviewTaskId() {
        return reviewTaskId;
    }

    public void setReviewTaskId(Long reviewTaskId) {
        this.reviewTaskId = reviewTaskId;
    }

    public ReviewTask getReviewTask() {
        return reviewTask;
    }

    public void setReviewTask(ReviewTask reviewTask) {
        this.reviewTask = reviewTask;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public User getAssignee() {
        return assignee;
    }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    public Long getAssignerId() {
        return assignerId;
    }

    public void setAssignerId(Long assignerId) {
        this.assignerId = assignerId;
    }

    public User getAssigner() {
        return assigner;
    }

    public void setAssigner(User assigner) {
        this.assigner = assigner;
    }

    public ReviewRole getRole() {
        return role;
    }

    public void setRole(ReviewRole role) {
        this.role = role;
    }

    public AssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssignmentStatus status) {
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
