package com.biaoshu.documentreview.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 问题严重程度枚举
 * 
 * @author biaoshu
 * @since 2024-01-01
 */
@Getter
public enum IssueSeverity {

    /**
     * 低
     */
    LOW("LOW", "低", 1),

    /**
     * 中
     */
    MEDIUM("MEDIUM", "中", 2),

    /**
     * 高
     */
    HIGH("HIGH", "高", 3),

    /**
     * 严重
     */
    CRITICAL("CRITICAL", "严重", 4),

    /**
     * 阻塞
     */
    BLOCKER("BLOCKER", "阻塞", 5);

    @EnumValue
    @JsonValue
    private final String code;

    private final String description;

    private final Integer level;

    IssueSeverity(String code, String description, Integer level) {
        this.code = code;
        this.description = description;
        this.level = level;
    }

    /**
     * 根据代码获取枚举
     */
    public static IssueSeverity fromCode(String code) {
        for (IssueSeverity severity : values()) {
            if (severity.getCode().equals(code)) {
                return severity;
            }
        }
        throw new IllegalArgumentException("Unknown IssueSeverity code: " + code);
    }

    /**
     * 是否为高严重级别
     */
    public boolean isHigh() {
        return this.level >= HIGH.level;
    }

    /**
     * 是否为严重级别
     */
    public boolean isCritical() {
        return this == CRITICAL || this == BLOCKER;
    }

    /**
     * 是否为阻塞级别
     */
    public boolean isBlocker() {
        return this == BLOCKER;
    }

    /**
     * 获取SLA时间（小时）
     */
    public int getSlaHours() {
        return switch (this) {
            case BLOCKER -> 4;
            case CRITICAL -> 24;
            case HIGH -> 48;
            case MEDIUM -> 72;
            case LOW -> 168; // 7天
        };
    }
}
