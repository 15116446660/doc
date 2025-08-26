package com.biaoshu.documentreview.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审核步骤类型枚举
 * 
 * @author biaoshu
 */
@Getter
public enum ReviewStepType {

    /**
     * 初审
     */
    INITIAL_REVIEW("初审"),

    /**
     * 复审
     */
    SECONDARY_REVIEW("复审"),

    /**
     * 终审
     */
    FINAL_REVIEW("终审"),

    /**
     * 专业审核
     */
    PROFESSIONAL_REVIEW("专业审核"),

    /**
     * 合规审核
     */
    COMPLIANCE_REVIEW("合规审核"),

    /**
     * 法务审核
     */
    LEGAL_REVIEW("法务审核"),

    /**
     * 财务审核
     */
    FINANCIAL_REVIEW("财务审核"),

    /**
     * 技术审核
     */
    TECHNICAL_REVIEW("技术审核"),

    /**
     * 业务审核
     */
    BUSINESS_REVIEW("业务审核"),

    /**
     * 管理审核
     */
    MANAGEMENT_REVIEW("管理审核"),

    /**
     * 自定义审核
     */
    CUSTOM_REVIEW("自定义审核");

    private final String description;

    ReviewStepType(String description) {
        this.description = description;
    }
}
