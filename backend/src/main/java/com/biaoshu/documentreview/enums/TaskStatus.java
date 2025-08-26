package com.biaoshu.documentreview.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 评审任务状态枚举
 * 
 * @author biaoshu
 * @since 2024-01-01
 */
@Getter
public enum TaskStatus {

    /**
     * 草稿
     */
    DRAFT("DRAFT", "草稿"),

    /**
     * 待开始
     */
    PENDING("PENDING", "待开始"),

    /**
     * AI分析中
     */
    AI_ANALYZING("AI_ANALYZING", "AI分析中"),

    /**
     * 专家分派中
     */
    EXPERT_ASSIGNMENT("EXPERT_ASSIGNMENT", "专家分派中"),

    /**
     * 评审中
     */
    IN_REVIEW("IN_REVIEW", "评审中"),

    /**
     * 需要修订
     */
    REVISION_REQUIRED("REVISION_REQUIRED", "需要修订"),

    /**
     * 验证中
     */
    VERIFICATION("VERIFICATION", "验证中"),

    /**
     * 已完成
     */
    COMPLETED("COMPLETED", "已完成"),

    /**
     * 已取消
     */
    CANCELLED("CANCELLED", "已取消"),

    /**
     * 已归档
     */
    ARCHIVED("ARCHIVED", "已归档");

    @EnumValue
    @JsonValue
    private final String code;

    private final String description;

    TaskStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     */
    public static TaskStatus fromCode(String code) {
        for (TaskStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown TaskStatus code: " + code);
    }

    /**
     * 是否为终态
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED || this == ARCHIVED;
    }

    /**
     * 是否可以取消
     */
    public boolean canCancel() {
        return this != COMPLETED && this != CANCELLED && this != ARCHIVED;
    }

    /**
     * 是否可以开始评审
     */
    public boolean canStartReview() {
        return this == PENDING || this == EXPERT_ASSIGNMENT;
    }

    /**
     * 是否正在进行中
     */
    public boolean isInProgress() {
        return this == AI_ANALYZING || this == EXPERT_ASSIGNMENT || 
               this == IN_REVIEW || this == REVISION_REQUIRED || this == VERIFICATION;
    }
}
