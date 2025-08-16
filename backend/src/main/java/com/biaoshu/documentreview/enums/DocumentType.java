package com.biaoshu.documentreview.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文档类型枚举
 * 
 * @author biaoshu
 */
@Getter
@AllArgsConstructor
public enum DocumentType {

    /**
     * 合同文档
     */
    CONTRACT("合同文档"),

    /**
     * 技术文档
     */
    TECHNICAL("技术文档"),

    /**
     * 业务文档
     */
    BUSINESS("业务文档"),

    /**
     * 法律文档
     */
    LEGAL("法律文档"),

    /**
     * 财务文档
     */
    FINANCIAL("财务文档"),

    /**
     * 人事文档
     */
    HR("人事文档"),

    /**
     * 项目文档
     */
    PROJECT("项目文档"),

    /**
     * 制度文档
     */
    POLICY("制度文档"),

    /**
     * 培训文档
     */
    TRAINING("培训文档"),

    /**
     * 其他文档
     */
    OTHER("其他文档");

    private final String description;
}
