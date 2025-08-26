package com.biaoshu.documentreview.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 业务类型枚举
 * 
 * @author biaoshu
 * @since 2024-01-01
 */
@Getter
public enum BusinessType {

    /**
     * 合同
     */
    CONTRACT("CONTRACT", "合同"),

    /**
     * 招标文件
     */
    TENDER("TENDER", "招标文件"),

    /**
     * 技术方案
     */
    TECHNICAL_PROPOSAL("TECHNICAL_PROPOSAL", "技术方案"),

    /**
     * 政策文件
     */
    POLICY("POLICY", "政策文件"),

    /**
     * 操作手册
     */
    MANUAL("MANUAL", "操作手册"),

    /**
     * 其他
     */
    OTHER("OTHER", "其他");

    @EnumValue
    @JsonValue
    private final String code;

    private final String description;

    BusinessType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     */
    public static BusinessType fromCode(String code) {
        for (BusinessType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown BusinessType code: " + code);
    }
}
