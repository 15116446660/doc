package com.biaoshu.documentreview.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文档状态枚举
 * 
 * @author biaoshu
 */
@Getter
public enum DocumentStatus {

    /**
     * 草稿
     */
    DRAFT("草稿"),

    /**
     * 审核中
     */
    REVIEWING("审核中"),

    /**
     * 审核通过
     */
    APPROVED("审核通过"),

    /**
     * 审核驳回
     */
    REJECTED("审核驳回"),

    /**
     * 已发布
     */
    PUBLISHED("已发布"),

    /**
     * 已撤回
     */
    WITHDRAWN("已撤回"),

    /**
     * 已归档
     */
    ARCHIVED("已归档"),

    /**
     * 已过期
     */
    EXPIRED("已过期");

    private final String description;

    DocumentStatus(String description) {
        this.description = description;
    }
}
