package com.biaoshu.documentreview.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审核模板状态枚举
 * 
 * @author biaoshu
 */
@Getter
@AllArgsConstructor
public enum ReviewTemplateStatus {

    /**
     * 草稿
     */
    DRAFT("草稿"),

    /**
     * 已发布
     */
    PUBLISHED("已发布"),

    /**
     * 已停用
     */
    DISABLED("已停用"),

    /**
     * 已归档
     */
    ARCHIVED("已归档");

    private final String description;
}
