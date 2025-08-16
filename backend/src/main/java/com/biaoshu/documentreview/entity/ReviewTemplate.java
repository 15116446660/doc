package com.biaoshu.documentreview.entity;

import com.biaoshu.documentreview.enums.DocumentType;
import com.biaoshu.documentreview.enums.ReviewTemplateStatus;
import com.biaoshu.documentreview.enums.SecurityLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 审核模板实体
 * 
 * @author biaoshu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "review_template", indexes = {
    @Index(name = "idx_template_code", columnList = "template_code", unique = true),
    @Index(name = "idx_document_type", columnList = "document_type"),
    @Index(name = "idx_status", columnList = "status")
})
public class ReviewTemplate extends BaseEntity {

    /**
     * 模板编码
     */
    @NotBlank(message = "模板编码不能为空")
    @Size(max = 50, message = "模板编码长度不能超过50个字符")
    @Column(name = "template_code", nullable = false, unique = true, length = 50)
    private String templateCode;

    /**
     * 模板名称
     */
    @NotBlank(message = "模板名称不能为空")
    @Size(max = 100, message = "模板名称长度不能超过100个字符")
    @Column(name = "template_name", nullable = false, length = 100)
    private String templateName;

    /**
     * 模板描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 适用文档类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type")
    private DocumentType documentType;

    /**
     * 适用安全级别
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "security_level")
    private SecurityLevel securityLevel;

    /**
     * 适用部门ID（为空表示全部门适用）
     */
    @Column(name = "department_id")
    private Long departmentId;

    /**
     * 模板状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReviewTemplateStatus status = ReviewTemplateStatus.DRAFT;

    /**
     * 是否默认模板
     */
    @Column(name = "is_default")
    private Boolean isDefault = false;

    /**
     * 是否启用
     */
    @Column(name = "is_enabled")
    private Boolean isEnabled = true;

    /**
     * 版本号
     */
    @NotNull(message = "版本号不能为空")
    @Column(name = "template_version", nullable = false)
    private Integer templateVersion = 1;

    /**
     * 总超时时间（小时）
     */
    @Column(name = "total_timeout_hours")
    private Integer totalTimeoutHours = 72;

    /**
     * 提醒间隔（小时，JSON格式存储）
     */
    @Column(name = "reminder_intervals", columnDefinition = "JSON")
    private String reminderIntervals;

    /**
     * 自动升级规则（JSON格式存储）
     */
    @Column(name = "escalation_rules", columnDefinition = "JSON")
    private String escalationRules;

    /**
     * 模板配置（JSON格式存储额外配置）
     */
    @Column(name = "template_config", columnDefinition = "JSON")
    private String templateConfig;

    /**
     * 排序
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * 审核步骤列表
     */
    @OneToMany(mappedBy = "reviewTemplate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("stepOrder ASC")
    private List<ReviewTemplateStep> steps;

    /**
     * 备注
     */
    @Column(name = "remark", length = 1000)
    private String remark;
}
