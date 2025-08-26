package com.biaoshu.documentreview.dto.request;

import com.biaoshu.documentreview.enums.BusinessType;
import com.biaoshu.documentreview.enums.ConfidentialityLevel;
import com.biaoshu.documentreview.enums.Priority;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建评审任务请求DTO
 * 
 * @author biaoshu
 * @since 2024-01-01
 */
@Data
public class CreateTaskRequest {

    /**
     * 任务名称
     */
    @NotBlank(message = "任务名称不能为空")
    @Size(max = 200, message = "任务名称长度不能超过200个字符")
    private String taskName;

    /**
     * 任务描述
     */
    @Size(max = 2000, message = "任务描述长度不能超过2000个字符")
    private String taskDescription;

    /**
     * 业务类型
     */
    @NotNull(message = "业务类型不能为空")
    private BusinessType businessType;

    /**
     * 密级
     */
    private ConfidentialityLevel confidentialityLevel = ConfidentialityLevel.INTERNAL;

    /**
     * 优先级
     */
    private Priority priority = Priority.MEDIUM;

    /**
     * 评审模板ID
     */
    private Long reviewTemplateId;

    /**
     * 关联项目ID
     */
    private Long projectId;

    /**
     * 截止时间
     */
    private LocalDateTime deadline;

    /**
     * 是否启用自动分派
     */
    private Boolean autoAssignmentEnabled = true;

    /**
     * 是否启用收敛闸门
     */
    private Boolean convergenceGateEnabled = false;

    /**
     * 收敛截止时间
     */
    private LocalDateTime convergenceDeadline;

    /**
     * 文档ID列表
     */
    @NotNull(message = "文档列表不能为空")
    @Size(min = 1, message = "至少需要上传一个文档")
    private List<Long> documentIds;

    /**
     * 指定的专家ID列表（可选）
     */
    private List<Long> expertIds;

    /**
     * 专家要求
     */
    private ExpertRequirement expertRequirement;

    /**
     * 是否启用AI分析
     */
    private Boolean aiAnalysisEnabled = true;

    /**
     * 特殊说明
     */
    private String specialInstructions;

    /**
     * 专家要求内部类
     */
    @Data
    public static class ExpertRequirement {
        /**
         * 要求的专业领域
         */
        private List<String> expertiseAreas;

        /**
         * 要求的技能标签
         */
        private List<String> skillTags;

        /**
         * 最低认证级别
         */
        private String minCertificationLevel;

        /**
         * 专家数量要求
         */
        private Integer expertCount = 1;

        /**
         * 是否需要避免冲突
         */
        private Boolean avoidConflicts = true;
    }
}
