package com.biaoshu.documentreview.dto.request;

import com.biaoshu.documentreview.enums.BusinessType;
import com.biaoshu.documentreview.enums.ConfidentialityLevel;
import com.biaoshu.documentreview.enums.Priority;
import lombok.Data;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 更新评审任务请求DTO
 * 
 * @author biaoshu
 * @since 2024-01-01
 */
@Data
public class UpdateTaskRequest {

    /**
     * 任务名称
     */
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
    private BusinessType businessType;

    /**
     * 密级
     */
    private ConfidentialityLevel confidentialityLevel;

    /**
     * 优先级
     */
    private Priority priority;

    /**
     * 截止时间
     */
    private LocalDateTime deadline;

    /**
     * 是否启用自动分派
     */
    private Boolean autoAssignmentEnabled;

    /**
     * 是否启用收敛闸门
     */
    private Boolean convergenceGateEnabled;

    /**
     * 收敛截止时间
     */
    private LocalDateTime convergenceDeadline;

    /**
     * 特殊说明
     */
    private String specialInstructions;
}
