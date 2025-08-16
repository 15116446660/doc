package com.biaoshu.documentreview.entity;

import com.biaoshu.documentreview.enums.DocumentStatus;
import com.biaoshu.documentreview.enums.DocumentType;
import com.biaoshu.documentreview.enums.SecurityLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文档实体
 * 
 * @author biaoshu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "doc_document", indexes = {
    @Index(name = "idx_document_number", columnList = "document_number", unique = true),
    @Index(name = "idx_title", columnList = "title"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_author_id", columnList = "author_id"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
public class Document extends BaseEntity {

    /**
     * 文档编号
     */
    @NotBlank(message = "文档编号不能为空")
    @Size(max = 100, message = "文档编号长度不能超过100个字符")
    @Column(name = "document_number", nullable = false, unique = true, length = 100)
    private String documentNumber;

    /**
     * 文档标题
     */
    @NotBlank(message = "文档标题不能为空")
    @Size(max = 200, message = "文档标题长度不能超过200个字符")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * 文档摘要
     */
    @Column(name = "summary", length = 1000)
    private String summary;

    /**
     * 文档类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    /**
     * 文档状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DocumentStatus status = DocumentStatus.DRAFT;

    /**
     * 安全级别
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "security_level", nullable = false)
    private SecurityLevel securityLevel = SecurityLevel.PUBLIC;

    /**
     * 作者ID
     */
    @Column(name = "author_id", nullable = false)
    private Long authorId;

    /**
     * 作者信息
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", insertable = false, updatable = false)
    private User author;

    /**
     * 部门ID
     */
    @Column(name = "department_id")
    private Long departmentId;

    /**
     * 项目ID
     */
    @Column(name = "project_id")
    private Long projectId;

    /**
     * 关联业务编号（工单号、需求编号、合同编号等）
     */
    @Column(name = "business_number", length = 100)
    private String businessNumber;

    /**
     * 文档标签（JSON格式存储）
     */
    @Column(name = "tags", columnDefinition = "JSON")
    private String tags;

    /**
     * 文档内容（存储OnlyOffice文档内容或文件路径）
     */
    @Lob
    @Column(name = "content")
    private String content;

    /**
     * 文件路径
     */
    @Column(name = "file_path")
    private String filePath;

    /**
     * 文件大小（字节）
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * 文件MD5哈希值
     */
    @Column(name = "file_hash", length = 32)
    private String fileHash;

    /**
     * 当前版本号
     */
    @Column(name = "current_version", nullable = false)
    private Integer currentVersion = 1;

    /**
     * 是否锁定（编辑锁定）
     */
    @Column(name = "is_locked")
    private Boolean isLocked = false;

    /**
     * 锁定人ID
     */
    @Column(name = "locked_by")
    private Long lockedBy;

    /**
     * 锁定时间
     */
    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    /**
     * 提交审核时间
     */
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    /**
     * 发布时间
     */
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    /**
     * 过期时间
     */
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    /**
     * 文档版本列表
     */
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DocumentVersion> versions;

    /**
     * 审核流程实例列表
     */
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReviewProcess> reviewProcesses;

    /**
     * 备注
     */
    @Column(name = "remark", length = 1000)
    private String remark;
}
