package com.biaoshu.documentreview.dto.request;

import com.biaoshu.documentreview.enums.BusinessType;
import com.biaoshu.documentreview.enums.Priority;
import com.biaoshu.documentreview.enums.TaskStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务查询请求DTO
 * 
 * @author biaoshu
 * @since 2024-01-01
 */
@Data
public class TaskQueryRequest {

    /**
     * 页码（从1开始）
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer size = 20;

    /**
     * 任务状态
     */
    private TaskStatus status;

    /**
     * 优先级
     */
    private Priority priority;

    /**
     * 业务类型
     */
    private BusinessType businessType;

    /**
     * 创建人ID
     */
    private Long creatorId;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 关键词搜索（任务名称或描述）
     */
    private String keyword;

    /**
     * 开始时间
     */
    private LocalDateTime startDate;

    /**
     * 结束时间
     */
    private LocalDateTime endDate;

    /**
     * 是否只查询超时任务
     */
    private Boolean overdue;

    /**
     * 是否只查询我创建的任务
     */
    private Boolean myCreated;

    /**
     * 是否只查询分派给我的任务
     */
    private Boolean assignedToMe;

    /**
     * 排序字段
     */
    private String sortBy = "createdAt";

    /**
     * 排序方向（asc/desc）
     */
    private String sortDirection = "desc";

    /**
     * 是否包含已归档的任务
     */
    private Boolean includeArchived = false;

    /**
     * 密级过滤
     */
    private String confidentialityLevel;

    /**
     * 标签过滤
     */
    private String tags;
}
