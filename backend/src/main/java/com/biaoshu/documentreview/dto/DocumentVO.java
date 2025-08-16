package com.biaoshu.documentreview.dto;

import com.biaoshu.documentreview.enums.DocumentStatus;
import com.biaoshu.documentreview.enums.DocumentType;
import com.biaoshu.documentreview.enums.SecurityLevel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档视图对象
 * 
 * @author biaoshu
 */
@Data
public class DocumentVO {

    /**
     * 文档ID
     */
    private Long id;

    /**
     * 文档编号
     */
    private String documentNumber;

    /**
     * 文档标题
     */
    private String title;

    /**
     * 文档摘要
     */
    private String summary;

    /**
     * 文档类型
     */
    private DocumentType documentType;

    /**
     * 文档状态
     */
    private DocumentStatus status;

    /**
     * 安全级别
     */
    private SecurityLevel securityLevel;

    /**
     * 作者ID
     */
    private Long authorId;

    /**
     * 作者姓名
     */
    private String authorName;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 关联业务编号
     */
    private String businessNumber;

    /**
     * 文档标签
     */
    private String tags;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件MD5哈希值
     */
    private String fileHash;

    /**
     * 当前版本号
     */
    private Integer currentVersion;

    /**
     * 是否锁定
     */
    private Boolean isLocked;

    /**
     * 锁定人ID
     */
    private Long lockedBy;

    /**
     * 锁定人姓名
     */
    private String lockedByName;

    /**
     * 锁定时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lockedAt;

    /**
     * 提交审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submittedAt;

    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedAt;

    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiredAt;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 创建人姓名
     */
    private String createdByName;

    /**
     * 更新人ID
     */
    private Long updatedBy;

    /**
     * 更新人姓名
     */
    private String updatedByName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否可以下载
     */
    private Boolean canDownload;

    /**
     * 是否可以编辑
     */
    private Boolean canEdit;

    /**
     * 是否可以删除
     */
    private Boolean canDelete;
}
