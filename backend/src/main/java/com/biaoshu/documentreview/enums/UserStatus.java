package com.biaoshu.documentreview.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户状态枚举
 * 
 * @author biaoshu
 */
@Getter
public enum UserStatus {

    /**
     * 激活
     */
    ACTIVE("激活"),

    /**
     * 禁用
     */
    DISABLED("禁用"),

    /**
     * 锁定
     */
    LOCKED("锁定"),

    /**
     * 待激活
     */
    PENDING("待激活"),

    /**
     * 已删除
     */
    DELETED("已删除");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }
}
