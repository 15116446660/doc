package com.biaoshu.documentreview.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 分派状态枚举
 * 
 * @author biaoshu
 * @since 2024-01-01
 */
@Getter
public enum AssignmentStatus {

    /**
     * 待接受
     */
    PENDING("PENDING", "待接受"),

    /**
     * 已接受
     */
    ACCEPTED("ACCEPTED", "已接受"),

    /**
     * 进行中
     */
    IN_PROGRESS("IN_PROGRESS", "进行中"),

    /**
     * 已完成
     */
    COMPLETED("COMPLETED", "已完成"),

    /**
     * 已拒绝
     */
    REJECTED("REJECTED", "已拒绝"),

    /**
     * 已取消
     */
    CANCELLED("CANCELLED", "已取消"),

    /**
     * 已超时
     */
    TIMEOUT("TIMEOUT", "已超时");

    @EnumValue
    @JsonValue
    private final String code;

    private final String description;

    AssignmentStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     */
    public static AssignmentStatus fromCode(String code) {
        for (AssignmentStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown AssignmentStatus code: " + code);
    }

    /**
     * 是否为活跃状态
     */
    public boolean isActive() {
        return this == ACCEPTED || this == IN_PROGRESS;
    }

    /**
     * 是否为终态
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == REJECTED || this == CANCELLED || this == TIMEOUT;
    }

    /**
     * 是否可以开始工作
     */
    public boolean canStartWork() {
        return this == ACCEPTED;
    }
}
