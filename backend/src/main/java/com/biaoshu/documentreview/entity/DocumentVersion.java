package com.biaoshu.documentreview.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 文档版本实体
 * 
 * @author biaoshu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "doc_document_version", indexes = {
    @Index(name = "idx_document_id", columnList = "document_id"),
    @Index(name = "idx_version_number", columnList = "document_id,version_number", unique = true)
})
public class DocumentVersion extends BaseEntity {

    /**
     * 文档ID
     */
    @NotNull(message = "文档ID不能为空")
    @Column(name = "document_id", nullable = false)
    private Long documentId;

    /**
     * 文档关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", insertable = false, updatable = false)
    private Document document;

    /**
     * 版本号
     */
    @NotNull(message = "版本号不能为空")
    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    /**
     * 版本标题
     */
    @Column(name = "title", length = 200)
    private String title;

    /**
     * 版本描述
     */
    @Column(name = "description", length = 1000)
    private String description;

    /**
     * 文档内容
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
     * 变更摘要（AI生成的变更总结）
     */
    @Column(name = "change_summary", length = 2000)
    private String changeSummary;

    /**
     * 变更类型（新增、修改、删除等）
     */
    @Column(name = "change_type", length = 50)
    private String changeType;

    /**
     * 是否当前版本
     */
    @Column(name = "is_current")
    private Boolean isCurrent = false;

    /**
     * 是否发布版本
     */
    @Column(name = "is_published")
    private Boolean isPublished = false;

    /**
     * 创建时间（版本创建时间）
     */
    @Column(name = "version_created_at")
    private LocalDateTime versionCreatedAt;

    /**
     * 创建人ID
     */
    @Column(name = "version_created_by")
    private Long versionCreatedBy;

    /**
     * 创建人信息
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_created_by", insertable = false, updatable = false)
    private User versionCreatedByUser;

    /**
     * 备注
     */
    @Column(name = "remark", length = 1000)
    private String remark;
}
