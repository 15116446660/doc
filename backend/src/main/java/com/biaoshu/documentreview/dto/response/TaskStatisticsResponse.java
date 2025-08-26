package com.biaoshu.documentreview.dto.response;

import com.biaoshu.documentreview.enums.TaskStatus;
import com.biaoshu.documentreview.enums.Priority;
import com.biaoshu.documentreview.enums.BusinessType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 任务统计响应DTO
 * 
 * @author biaoshu
 * @since 2024-01-01
 */
@Data
public class TaskStatisticsResponse {

    /**
     * 总体统计
     */
    private OverallStatistics overall;

    /**
     * 状态统计
     */
    private List<StatusStatistics> statusStats;

    /**
     * 优先级统计
     */
    private List<PriorityStatistics> priorityStats;

    /**
     * 业务类型统计
     */
    private List<BusinessTypeStatistics> businessTypeStats;

    /**
     * 时间趋势统计
     */
    private List<TimeTrendStatistics> timeTrends;

    /**
     * 性能指标
     */
    private PerformanceMetrics performance;

    /**
     * 总体统计信息
     */
    @Data
    public static class OverallStatistics {
        private Long totalTasks;
        private Long activeTasks;
        private Long completedTasks;
        private Long overdueTasks;
        private Long dueSoonTasks;
        private Double averageCompletionTime;
        private Double completionRate;
        private LocalDateTime lastUpdated;
    }

    /**
     * 状态统计
     */
    @Data
    public static class StatusStatistics {
        private TaskStatus status;
        private String statusName;
        private Long count;
        private Double percentage;
        private Long trend; // 相比上期变化
    }

    /**
     * 优先级统计
     */
    @Data
    public static class PriorityStatistics {
        private Priority priority;
        private String priorityName;
        private Long count;
        private Double percentage;
        private Double averageCompletionTime;
    }

    /**
     * 业务类型统计
     */
    @Data
    public static class BusinessTypeStatistics {
        private BusinessType businessType;
        private String businessTypeName;
        private Long count;
        private Double percentage;
        private Double averageCompletionTime;
    }

    /**
     * 时间趋势统计
     */
    @Data
    public static class TimeTrendStatistics {
        private String period; // 时间段（如：2024-01）
        private Long createdCount;
        private Long completedCount;
        private Double completionRate;
        private Double averageCompletionTime;
    }

    /**
     * 性能指标
     */
    @Data
    public static class PerformanceMetrics {
        private Double averageResponseTime; // 平均响应时间（小时）
        private Double averageReviewTime; // 平均评审时间（小时）
        private Double slaComplianceRate; // SLA达成率
        private Double expertUtilizationRate; // 专家利用率
        private Double issueResolutionRate; // 问题解决率
        private Map<String, Double> qualityMetrics; // 质量指标
    }
}
