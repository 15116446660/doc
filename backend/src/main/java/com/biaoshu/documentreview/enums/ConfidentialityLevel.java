package com.biaoshu.documentreview.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 密级枚举
 * 
 * @author biaoshu
 * @since 2024-01-01
 */
@Getter
public enum ConfidentialityLevel {

    /**
     * 公开
     */
    PUBLIC("PUBLIC", "公开", 1),

    /**
     * 内部
     */
    INTERNAL("INTERNAL", "内部", 2),

    /**
     * 机密
     */
    CONFIDENTIAL("CONFIDENTIAL", "机密", 3),

    /**
     * 秘密
     */
    SECRET("SECRET", "秘密", 4);

    @EnumValue
    @JsonValue
    private final String code;

    private final String description;

    private final Integer level;

    ConfidentialityLevel(String code, String description, Integer level) {
        this.code = code;
        this.description = description;
        this.level = level;
    }

    /**
     * 根据代码获取枚举
     */
    public static ConfidentialityLevel fromCode(String code) {
        for (ConfidentialityLevel level : values()) {
            if (level.getCode().equals(code)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown ConfidentialityLevel code: " + code);
    }

    /**
     * 是否为机密级别
     */
    public boolean isConfidential() {
        return this == CONFIDENTIAL || this == SECRET;
    }

    /**
     * 是否需要特殊权限
     */
    public boolean requiresSpecialPermission() {
        return this.level >= CONFIDENTIAL.level;
    }
}
