package com.biaoshu.documentreview.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审核流程状态枚举
 * 
 * @author biaoshu
 */
@Getter
public enum ReviewProcessStatus {

    /**
     * 待开始
     */
    PENDING("待开始"),

    /**
     * 进行中
     */
    IN_PROGRESS("进行中"),

    /**
     * 已完成
     */
    COMPLETED("已完成"),

    /**
     * 已驳回
     */
    REJECTED("已驳回"),

    /**
     * 已撤回
     */
    WITHDRAWN("已撤回"),

    /**
     * 已暂停
     */
    SUSPENDED("已暂停"),

    /**
     * 已取消
     */
    CANCELLED("已取消"),

    /**
     * 超时
     */
    TIMEOUT("超时");

    private final String description;

    ReviewProcessStatus(String description) {
        this.description = description;
    }
}
