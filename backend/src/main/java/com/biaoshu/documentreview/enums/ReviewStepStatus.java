package com.biaoshu.documentreview.enums;

/**
 * 审核步骤状态枚举
 * 
 * @author biaoshu
 */
public enum ReviewStepStatus {
    /**
     * 等待中
     */
    PENDING("等待中"),

    /**
     * 进行中
     */
    IN_PROGRESS("进行中"),

    /**
     * 已完成
     */
    COMPLETED("已完成"),

    /**
     * 已跳过
     */
    SKIPPED("已跳过"),

    /**
     * 已取消
     */
    CANCELLED("已取消"),

    /**
     * 已超时
     */
    TIMEOUT("已超时");

    private final String description;

    ReviewStepStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
