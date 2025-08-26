package com.biaoshu.documentreview.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 优先级枚举
 * 
 * @author biaoshu
 * @since 2024-01-01
 */
@Getter
public enum Priority {

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
     * 紧急
     */
    URGENT("URGENT", "紧急", 4);

    @EnumValue
    @JsonValue
    private final String code;

    private final String description;

    private final Integer level;

    Priority(String code, String description, Integer level) {
        this.code = code;
        this.description = description;
        this.level = level;
    }

    /**
     * 根据代码获取枚举
     */
    public static Priority fromCode(String code) {
        for (Priority priority : values()) {
            if (priority.getCode().equals(code)) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Unknown Priority code: " + code);
    }

    /**
     * 是否为高优先级
     */
    public boolean isHigh() {
        return this == HIGH || this == URGENT;
    }

    /**
     * 是否为紧急
     */
    public boolean isUrgent() {
        return this == URGENT;
    }
}
