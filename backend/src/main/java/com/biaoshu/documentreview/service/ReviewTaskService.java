package com.biaoshu.documentreview.service;

import com.biaoshu.documentreview.dto.ReviewTaskCreateDTO;
import com.biaoshu.documentreview.dto.ReviewTaskDTO;
import com.biaoshu.documentreview.dto.ReviewTaskQueryDTO;
import com.biaoshu.documentreview.dto.ReviewTaskUpdateDTO;
import com.biaoshu.documentreview.entity.ReviewTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 评审任务服务接口
 */
public interface ReviewTaskService {

    /**
     * 创建评审任务
     * @param createDTO 创建DTO
     * @param creatorId 创建者ID
     * @return 评审任务DTO
     */
    ReviewTaskDTO createReviewTask(ReviewTaskCreateDTO createDTO, Long creatorId);

    /**
     * 更新评审任务
     * @param taskId 任务ID
     * @param updateDTO 更新DTO
     * @param userId 操作用户ID
     * @return 评审任务DTO
     */
    ReviewTaskDTO updateReviewTask(Long taskId, ReviewTaskUpdateDTO updateDTO, Long userId);

    /**
     * 删除评审任务
     * @param taskId 任务ID
     * @param userId 操作用户ID
     */
    void deleteReviewTask(Long taskId, Long userId);

    /**
     * 根据ID获取评审任务
     * @param taskId 任务ID
     * @return 评审任务DTO
     */
    ReviewTaskDTO getReviewTaskById(Long taskId);

    /**
     * 分页查询评审任务
     * @param queryDTO 查询条件
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<ReviewTaskDTO> getReviewTasks(ReviewTaskQueryDTO queryDTO, Pageable pageable);

    /**
     * 获取用户相关的评审任务
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<ReviewTaskDTO> getUserRelatedTasks(Long userId, Pageable pageable);

    /**
     * 获取用户分配的评审任务
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<ReviewTaskDTO> getAssignedTasks(Long userId, Pageable pageable);

    /**
     * 启动评审任务
     * @param taskId 任务ID
     * @param userId 操作用户ID
     * @return 评审任务DTO
     */
    ReviewTaskDTO startReviewTask(Long taskId, Long userId);

    /**
     * 完成评审任务
     * @param taskId 任务ID
     * @param userId 操作用户ID
     * @param completionNotes 完成备注
     * @return 评审任务DTO
     */
    ReviewTaskDTO completeReviewTask(Long taskId, Long userId, String completionNotes);

    /**
     * 取消评审任务
     * @param taskId 任务ID
     * @param userId 操作用户ID
     * @param reason 取消原因
     * @return 评审任务DTO
     */
    ReviewTaskDTO cancelReviewTask(Long taskId, Long userId, String reason);

    /**
     * 分配评审任务
     * @param taskId 任务ID
     * @param assigneeId 被分配人ID
     * @param assignerId 分配人ID
     * @param role 评审角色
     * @param notes 分配备注
     */
    void assignReviewTask(Long taskId, Long assigneeId, Long assignerId, 
                         String role, String notes);

    /**
     * 启动AI分析
     * @param taskId 任务ID
     * @param userId 操作用户ID
     * @return 是否成功启动
     */
    Boolean startAIAnalysis(Long taskId, Long userId);

    /**
     * 停止AI分析
     * @param taskId 任务ID
     * @param userId 操作用户ID
     * @return 是否成功停止
     */
    Boolean stopAIAnalysis(Long taskId, Long userId);

    /**
     * 获取AI分析进度
     * @param taskId 任务ID
     * @return 进度百分比
     */
    Integer getAIAnalysisProgress(Long taskId);

    /**
     * 获取即将到期的任务
     * @param days 天数
     * @return 任务列表
     */
    List<ReviewTaskDTO> getTasksDueSoon(Integer days);

    /**
     * 获取已过期的任务
     * @return 任务列表
     */
    List<ReviewTaskDTO> getOverdueTasks();

    /**
     * 统计用户任务数量
     * @param userId 用户ID
     * @return 统计信息
     */
    ReviewTaskStatistics getUserTaskStatistics(Long userId);

    /**
     * 统计项目任务数量
     * @param projectId 项目ID
     * @return 统计信息
     */
    ReviewTaskStatistics getProjectTaskStatistics(Long projectId);

    /**
     * 任务统计信息内部类
     */
    class ReviewTaskStatistics {
        private Long totalTasks;
        private Long pendingTasks;
        private Long inProgressTasks;
        private Long completedTasks;
        private Long cancelledTasks;
        private Long assignedTasks;
        private Long createdTasks;

        // Getters and Setters
        public Long getTotalTasks() {
            return totalTasks;
        }

        public void setTotalTasks(Long totalTasks) {
            this.totalTasks = totalTasks;
        }

        public Long getPendingTasks() {
            return pendingTasks;
        }

        public void setPendingTasks(Long pendingTasks) {
            this.pendingTasks = pendingTasks;
        }

        public Long getInProgressTasks() {
            return inProgressTasks;
        }

        public void setInProgressTasks(Long inProgressTasks) {
            this.inProgressTasks = inProgressTasks;
        }

        public Long getCompletedTasks() {
            return completedTasks;
        }

        public void setCompletedTasks(Long completedTasks) {
            this.completedTasks = completedTasks;
        }

        public Long getCancelledTasks() {
            return cancelledTasks;
        }

        public void setCancelledTasks(Long cancelledTasks) {
            this.cancelledTasks = cancelledTasks;
        }

        public Long getAssignedTasks() {
            return assignedTasks;
        }

        public void setAssignedTasks(Long assignedTasks) {
            this.assignedTasks = assignedTasks;
        }

        public Long getCreatedTasks() {
            return createdTasks;
        }

        public void setCreatedTasks(Long createdTasks) {
            this.createdTasks = createdTasks;
        }
    }
}
