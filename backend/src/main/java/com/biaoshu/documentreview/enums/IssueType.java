package com.biaoshu.documentreview.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 问题类型枚举
 * 
 * @author biaoshu
 * @since 2024-01-01
 */
@Getter
public enum IssueType {

    /**
     * 内容错误
     */
    CONTENT_ERROR("CONTENT_ERROR", "内容错误"),

    /**
     * 格式问题
     */
    FORMAT_ISSUE("FORMAT_ISSUE", "格式问题"),

    /**
     * 逻辑错误
     */
    LOGIC_ERROR("LOGIC_ERROR", "逻辑错误"),

    /**
     * 合规问题
     */
    COMPLIANCE_ISSUE("COMPLIANCE_ISSUE", "合规问题"),

    /**
     * 技术问题
     */
    TECHNICAL_ISSUE("TECHNICAL_ISSUE", "技术问题"),

    /**
     * 质量问题
     */
    QUALITY_ISSUE("QUALITY_ISSUE", "质量问题"),

    /**
     * 安全问题
     */
    SECURITY_ISSUE("SECURITY_ISSUE", "安全问题"),

    /**
     * 法律问题
     */
    LEGAL_ISSUE("LEGAL_ISSUE", "法律问题"),

    /**
     * 数据问题
     */
    DATA_ISSUE("DATA_ISSUE", "数据问题"),

    /**
     * 引用问题
     */
    REFERENCE_ISSUE("REFERENCE_ISSUE", "引用问题"),

    /**
     * 语言问题
     */
    LANGUAGE_ISSUE("LANGUAGE_ISSUE", "语言问题"),

    /**
     * 结构问题
     */
    STRUCTURE_ISSUE("STRUCTURE_ISSUE", "结构问题"),

    /**
     * 完整性问题
     */
    COMPLETENESS_ISSUE("COMPLETENESS_ISSUE", "完整性问题"),

    /**
     * 一致性问题
     */
    CONSISTENCY_ISSUE("CONSISTENCY_ISSUE", "一致性问题"),

    /**
     * 其他问题
     */
    OTHER("OTHER", "其他问题");

    @EnumValue
    @JsonValue
    private final String code;

    private final String description;

    IssueType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     */
    public static IssueType fromCode(String code) {
        for (IssueType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown IssueType code: " + code);
    }

    /**
     * 是否为严重问题类型
     */
    public boolean isCriticalType() {
        return this == SECURITY_ISSUE || this == LEGAL_ISSUE || this == COMPLIANCE_ISSUE;
    }

    /**
     * 是否需要专业审核
     */
    public boolean requiresSpecializedReview() {
        return this == TECHNICAL_ISSUE || this == LEGAL_ISSUE || this == SECURITY_ISSUE || this == COMPLIANCE_ISSUE;
    }

    /**
     * 获取默认严重程度
     */
    public IssueSeverity getDefaultSeverity() {
        return switch (this) {
            case SECURITY_ISSUE, LEGAL_ISSUE -> IssueSeverity.CRITICAL;
            case COMPLIANCE_ISSUE, TECHNICAL_ISSUE -> IssueSeverity.HIGH;
            case CONTENT_ERROR, LOGIC_ERROR, DATA_ISSUE -> IssueSeverity.MEDIUM;
            default -> IssueSeverity.LOW;
        };
    }
}
