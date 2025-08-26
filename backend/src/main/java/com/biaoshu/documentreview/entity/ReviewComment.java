package com.biaoshu.documentreview.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评审评论实体
 */
@Entity
@Table(name = "review_comments")
public class ReviewComment extends BaseEntity {

    @Column(name = "review_task_id", nullable = false)
    private Long reviewTaskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_task_id", insertable = false, updatable = false)
    private ReviewTask reviewTask;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", insertable = false, updatable = false)
    private User author;

    @Column(name = "parent_comment_id", insertable = false, updatable = false)
    private Long parentCommentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private ReviewComment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReviewComment> replies;

    @Enumerated(EnumType.STRING)
    @Column(name = "comment_type", nullable = false)
    private CommentType commentType;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "position_info", columnDefinition = "TEXT")
    private String positionInfo;

    @Column(name = "quoted_text", columnDefinition = "TEXT")
    private String quotedText;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity")
    private CommentSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CommentStatus status = CommentStatus.OPEN;

    @Column(name = "is_resolved", nullable = false)
    private Boolean isResolved = false;

    @Column(name = "resolved_by_id")
    private Long resolvedById;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by_id", insertable = false, updatable = false)
    private User resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "tags", length = 500)
    private String tags;

    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachments;

    @Column(name = "mentioned_users", columnDefinition = "TEXT")
    private String mentionedUsers;

    // 评论类型枚举
    public enum CommentType {
        GENERAL("一般评论"),
        SUGGESTION("建议"),
        QUESTION("问题"),
        ISSUE("问题"),
        APPROVAL("批准"),
        REJECTION("拒绝"),
        AI_GENERATED("AI生成");

        private final String description;

        CommentType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 评论严重程度枚举
    public enum CommentSeverity {
        INFO("信息"),
        LOW("低"),
        MEDIUM("中"),
        HIGH("高"),
        CRITICAL("严重");

        private final String description;

        CommentSeverity(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 评论状态枚举
    public enum CommentStatus {
        OPEN("开放"),
        IN_PROGRESS("处理中"),
        RESOLVED("已解决"),
        CLOSED("已关闭"),
        DEFERRED("延期");

        private final String description;

        CommentStatus(String description) {
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

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Long getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(Long parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    public ReviewComment getParentComment() {
        return parentComment;
    }

    public void setParentComment(ReviewComment parentComment) {
        this.parentComment = parentComment;
    }

    public List<ReviewComment> getReplies() {
        return replies;
    }

    public void setReplies(List<ReviewComment> replies) {
        this.replies = replies;
    }

    public CommentType getCommentType() {
        return commentType;
    }

    public void setCommentType(CommentType commentType) {
        this.commentType = commentType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPositionInfo() {
        return positionInfo;
    }

    public void setPositionInfo(String positionInfo) {
        this.positionInfo = positionInfo;
    }

    public String getQuotedText() {
        return quotedText;
    }

    public void setQuotedText(String quotedText) {
        this.quotedText = quotedText;
    }

    public CommentSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(CommentSeverity severity) {
        this.severity = severity;
    }

    public CommentStatus getStatus() {
        return status;
    }

    public void setStatus(CommentStatus status) {
        this.status = status;
    }

    public Boolean getIsResolved() {
        return isResolved;
    }

    public void setIsResolved(Boolean isResolved) {
        this.isResolved = isResolved;
    }

    public Long getResolvedById() {
        return resolvedById;
    }

    public void setResolvedById(Long resolvedById) {
        this.resolvedById = resolvedById;
    }

    public User getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(User resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    public String getMentionedUsers() {
        return mentionedUsers;
    }

    public void setMentionedUsers(String mentionedUsers) {
        this.mentionedUsers = mentionedUsers;
    }
}
