package com.biaoshu.documentreview.dto;

import com.biaoshu.documentreview.entity.ReviewComment;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评审评论DTO
 */
public class ReviewCommentDTO {

    private Long id;
    private Long reviewTaskId;
    private Long authorId;
    private String authorName;
    private String authorAvatar;
    private Long parentCommentId;
    private ReviewComment.CommentType commentType;
    private String content;
    private String positionInfo;
    private String quotedText;
    private ReviewComment.CommentSeverity severity;
    private ReviewComment.CommentStatus status;
    private Boolean isResolved;
    private Long resolvedById;
    private String resolvedByName;
    private LocalDateTime resolvedAt;
    private String tags;
    private String attachments;
    private String mentionedUsers;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<ReviewCommentDTO> replies;
    private Integer replyCount;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReviewTaskId() {
        return reviewTaskId;
    }

    public void setReviewTaskId(Long reviewTaskId) {
        this.reviewTaskId = reviewTaskId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorAvatar() {
        return authorAvatar;
    }

    public void setAuthorAvatar(String authorAvatar) {
        this.authorAvatar = authorAvatar;
    }

    public Long getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(Long parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    public ReviewComment.CommentType getCommentType() {
        return commentType;
    }

    public void setCommentType(ReviewComment.CommentType commentType) {
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

    public ReviewComment.CommentSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(ReviewComment.CommentSeverity severity) {
        this.severity = severity;
    }

    public ReviewComment.CommentStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewComment.CommentStatus status) {
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

    public String getResolvedByName() {
        return resolvedByName;
    }

    public void setResolvedByName(String resolvedByName) {
        this.resolvedByName = resolvedByName;
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

    public List<ReviewCommentDTO> getReplies() {
        return replies;
    }

    public void setReplies(List<ReviewCommentDTO> replies) {
        this.replies = replies;
    }

    public Integer getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(Integer replyCount) {
        this.replyCount = replyCount;
    }
}
