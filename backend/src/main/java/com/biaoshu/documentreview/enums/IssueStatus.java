package com.biaoshu.documentreview.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 问题状态枚举
 * 
 * @author biaoshu
 * @since 2024-01-01
 */
@Getter
public enum IssueStatus {

    /**
     * 开放
     */
    OPEN("OPEN", "开放"),

    /**
     * 处理中
     */
    IN_PROGRESS("IN_PROGRESS", "处理中"),

    /**
     * 已解决
     */
    RESOLVED("RESOLVED", "已解决"),

    /**
     * 已验证
     */
    VERIFIED("VERIFIED", "已验证"),

    /**
     * 已关闭
     */
    CLOSED("CLOSED", "已关闭"),

    /**
     * 延期处理
     */
    DEFERRED("DEFERRED", "延期处理");

    @EnumValue
    @JsonValue
    private final String code;

    private final String description;

    IssueStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     */
    public static IssueStatus fromCode(String code) {
        for (IssueStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown IssueStatus code: " + code);
    }

    /**
     * 是否为开放状态
     */
    public boolean isOpen() {
        return this == OPEN || this == IN_PROGRESS;
    }

    /**
     * 是否已解决
     */
    public boolean isResolved() {
        return this == RESOLVED || this == VERIFIED || this == CLOSED;
    }

    /**
     * 是否为终态
     */
    public boolean isTerminal() {
        return this == VERIFIED || this == CLOSED || this == DEFERRED;
    }
}
