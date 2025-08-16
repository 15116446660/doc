package com.biaoshu.documentreview.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 安全级别枚举
 * 
 * @author biaoshu
 */
@Getter
@AllArgsConstructor
public enum SecurityLevel {

    /**
     * 公开
     */
    PUBLIC("公开"),

    /**
     * 内部
     */
    INTERNAL("内部"),

    /**
     * 机密
     */
    CONFIDENTIAL("机密"),

    /**
     * 绝密
     */
    SECRET("绝密"),

    /**
     * 限制
     */
    RESTRICTED("限制");

    private final String description;
}
