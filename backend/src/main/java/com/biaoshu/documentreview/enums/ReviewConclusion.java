package com.biaoshu.documentreview.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 评审结论枚举
 * 
 * @author biaoshu
 * @since 2024-01-01
 */
@Getter
public enum ReviewConclusion {

    /**
     * 通过
     */
    APPROVED("APPROVED", "通过"),

    /**
     * 有条件通过
     */
    CONDITIONAL_APPROVED("CONDITIONAL_APPROVED", "有条件通过"),

    /**
     * 拒绝
     */
    REJECTED("REJECTED", "拒绝"),

    /**
     * 需要重大修订
     */
    NEED_MAJOR_REVISION("NEED_MAJOR_REVISION", "需要重大修订");

    @EnumValue
    @JsonValue
    private final String code;

    private final String description;

    ReviewConclusion(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     */
    public static ReviewConclusion fromCode(String code) {
        for (ReviewConclusion conclusion : values()) {
            if (conclusion.getCode().equals(code)) {
                return conclusion;
            }
        }
        throw new IllegalArgumentException("Unknown ReviewConclusion code: " + code);
    }

    /**
     * 是否为通过状态
     */
    public boolean isApproved() {
        return this == APPROVED || this == CONDITIONAL_APPROVED;
    }

    /**
     * 是否需要修订
     */
    public boolean needsRevision() {
        return this == CONDITIONAL_APPROVED || this == NEED_MAJOR_REVISION;
    }
}
