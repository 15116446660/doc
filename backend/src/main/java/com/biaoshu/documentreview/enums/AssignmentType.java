package com.biaoshu.documentreview.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 分派类型枚举
 * 
 * @author biaoshu
 * @since 2024-01-01
 */
@Getter
public enum AssignmentType {

    /**
     * 评审员
     */
    REVIEWER("REVIEWER", "评审员"),

    /**
     * 主评审员
     */
    PRIMARY_REVIEWER("PRIMARY_REVIEWER", "主评审员"),

    /**
     * 副评审员
     */
    SECONDARY_REVIEWER("SECONDARY_REVIEWER", "副评审员"),

    /**
     * 专家顾问
     */
    EXPERT_ADVISOR("EXPERT_ADVISOR", "专家顾问"),

    /**
     * 合规审核员
     */
    COMPLIANCE_REVIEWER("COMPLIANCE_REVIEWER", "合规审核员"),

    /**
     * 技术审核员
     */
    TECHNICAL_REVIEWER("TECHNICAL_REVIEWER", "技术审核员"),

    /**
     * 质量审核员
     */
    QUALITY_REVIEWER("QUALITY_REVIEWER", "质量审核员"),

    /**
     * 观察员
     */
    OBSERVER("OBSERVER", "观察员");

    @EnumValue
    @JsonValue
    private final String code;

    private final String description;

    AssignmentType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     */
    public static AssignmentType fromCode(String code) {
        for (AssignmentType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown AssignmentType code: " + code);
    }

    /**
     * 是否为主要评审员
     */
    public boolean isPrimary() {
        return this == PRIMARY_REVIEWER || this == REVIEWER;
    }

    /**
     * 是否有投票权
     */
    public boolean hasVotingRight() {
        return this != OBSERVER;
    }

    /**
     * 是否为专业审核员
     */
    public boolean isSpecializedReviewer() {
        return this == COMPLIANCE_REVIEWER || this == TECHNICAL_REVIEWER || this == QUALITY_REVIEWER;
    }
}
