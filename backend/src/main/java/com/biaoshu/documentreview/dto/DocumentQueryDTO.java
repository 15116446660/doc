package com.biaoshu.documentreview.dto;

import com.biaoshu.documentreview.enums.DocumentStatus;
import com.biaoshu.documentreview.enums.DocumentType;
import com.biaoshu.documentreview.enums.SecurityLevel;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 文档查询DTO
 * 
 * @author biaoshu
 */
@Data
public class DocumentQueryDTO {

    /**
     * 文档标题（模糊查询）
     */
    private String title;

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
     * 项目ID
     */
    private Long projectId;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 标签（模糊查询）
     */
    private String tags;

    /**
     * 创建时间开始
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAtStart;

    /**
     * 创建时间结束
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAtEnd;

    /**
     * 更新时间开始
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAtStart;

    /**
     * 更新时间结束
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAtEnd;

    /**
     * 关键词搜索（标题、摘要、内容）
     */
    private String keyword;
}
