package com.biaoshu.documentreview.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.biaoshu.documentreview.entity.ReviewTask;
import com.biaoshu.documentreview.enums.TaskStatus;
import com.biaoshu.documentreview.enums.Priority;
import com.biaoshu.documentreview.enums.BusinessType;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评审任务Repository
 *
 * @author biaoshu
 * @since 2024-01-01
 */
@Repository
@Mapper
public interface ReviewTaskRepository extends BaseMapper<ReviewTask> {

    /**
     * 分页查询评审任务（带关联信息）
     */
    @Select("""
        <script>
        SELECT
            rt.*,
            u.username as creator_name,
            u.real_name as creator_real_name,
            p.project_name,
            rw.workflow_name
        FROM review_tasks rt
        LEFT JOIN users u ON rt.creator_id = u.id
        LEFT JOIN projects p ON rt.project_id = p.id
        LEFT JOIN review_workflows rw ON rt.review_template_id = rw.id
        <where>
            <if test="status != null">
                AND rt.status = #{status}
            </if>
            <if test="priority != null">
                AND rt.priority = #{priority}
            </if>
            <if test="businessType != null">
                AND rt.business_type = #{businessType}
            </if>
            <if test="creatorId != null">
                AND rt.creator_id = #{creatorId}
            </if>
            <if test="projectId != null">
                AND rt.project_id = #{projectId}
            </if>
            <if test="keyword != null and keyword != ''">
                AND (rt.task_name LIKE CONCAT('%', #{keyword}, '%')
                     OR rt.task_description LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test="startDate != null">
                AND rt.created_at >= #{startDate}
            </if>
            <if test="endDate != null">
                AND rt.created_at <= #{endDate}
            </if>
            <if test="overdue != null and overdue == true">
                AND rt.deadline < NOW()
                AND rt.status NOT IN ('COMPLETED', 'CANCELLED', 'ARCHIVED')
            </if>
        </where>
        ORDER BY
            CASE rt.priority
                WHEN 'URGENT' THEN 4
                WHEN 'HIGH' THEN 3
                WHEN 'MEDIUM' THEN 2
                WHEN 'LOW' THEN 1
                ELSE 0
            END DESC,
            rt.created_at DESC
        </script>
    """)
    IPage<ReviewTask> selectTasksWithDetails(
        Page<ReviewTask> page,
        @Param("status") TaskStatus status,
        @Param("priority") Priority priority,
        @Param("businessType") BusinessType businessType,
        @Param("creatorId") Long creatorId,
        @Param("projectId") Long projectId,
        @Param("keyword") String keyword,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("overdue") Boolean overdue
    );

    /**
     * 查找指定用户分配的评审任务
     */
    @Query("SELECT DISTINCT rt FROM ReviewTask rt " +
           "JOIN rt.assignments a " +
           "WHERE a.assigneeId = :userId " +
           "AND a.status IN ('ACCEPTED', 'IN_PROGRESS')")
    Page<ReviewTask> findAssignedToUser(@Param("userId") Long userId, Pageable pageable);

    /**
     * 查找即将到期的评审任务
     */
    @Query("SELECT rt FROM ReviewTask rt " +
           "WHERE rt.deadline BETWEEN :startTime AND :endTime " +
           "AND rt.status IN ('PENDING', 'IN_PROGRESS')")
    List<ReviewTask> findTasksDueSoon(@Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 查找已过期的评审任务
     */
    @Query("SELECT rt FROM ReviewTask rt " +
           "WHERE rt.deadline < :currentTime " +
           "AND rt.status IN ('PENDING', 'IN_PROGRESS')")
    List<ReviewTask> findOverdueTasks(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 根据任务名称模糊查询
     */
    Page<ReviewTask> findByTaskNameContainingIgnoreCase(String taskName, Pageable pageable);

    /**
     * 复合查询：根据多个条件查找评审任务
     */
    @Query("SELECT rt FROM ReviewTask rt " +
           "WHERE (:projectId IS NULL OR rt.projectId = :projectId) " +
           "AND (:status IS NULL OR rt.status = :status) " +
           "AND (:priority IS NULL OR rt.priority = :priority) " +
           "AND (:creatorId IS NULL OR rt.creatorId = :creatorId) " +
           "AND (:taskName IS NULL OR LOWER(rt.taskName) LIKE LOWER(CONCAT('%', :taskName, '%')))")
    Page<ReviewTask> findByMultipleConditions(@Param("projectId") Long projectId,
                                              @Param("status") ReviewTask.ReviewTaskStatus status,
                                              @Param("priority") ReviewTask.ReviewTaskPriority priority,
                                              @Param("creatorId") Long creatorId,
                                              @Param("taskName") String taskName,
                                              Pageable pageable);

    /**
     * 统计项目的评审任务数量
     */
    @Query("SELECT COUNT(rt) FROM ReviewTask rt WHERE rt.projectId = :projectId")
    Long countByProjectId(@Param("projectId") Long projectId);

    /**
     * 统计用户创建的评审任务数量
     */
    @Query("SELECT COUNT(rt) FROM ReviewTask rt WHERE rt.creatorId = :userId")
    Long countByCreatorId(@Param("userId") Long userId);

    /**
     * 统计用户分配的评审任务数量
     */
    @Query("SELECT COUNT(DISTINCT rt) FROM ReviewTask rt " +
           "JOIN rt.assignments a " +
           "WHERE a.assigneeId = :userId " +
           "AND a.status IN ('ACCEPTED', 'IN_PROGRESS')")
    Long countAssignedToUser(@Param("userId") Long userId);

    /**
     * 查找需要AI分析的任务
     */
    @Query("SELECT rt FROM ReviewTask rt " +
           "WHERE rt.aiAnalysisEnabled = true " +
           "AND rt.aiAnalysisStatus = 'PENDING'")
    List<ReviewTask> findTasksNeedingAIAnalysis();

    /**
     * 查找AI分析进行中的任务
     */
    @Query("SELECT rt FROM ReviewTask rt " +
           "WHERE rt.aiAnalysisStatus = 'RUNNING'")
    List<ReviewTask> findTasksWithRunningAIAnalysis();

    /**
     * 根据AI分析状态查找任务
     */
    Page<ReviewTask> findByAiAnalysisStatus(ReviewTask.AIAnalysisStatus aiAnalysisStatus, Pageable pageable);

    /**
     * 查找最近创建的评审任务
     */
    @Query("SELECT rt FROM ReviewTask rt " +
           "WHERE rt.createdAt >= :since " +
           "ORDER BY rt.createdAt DESC")
    List<ReviewTask> findRecentTasks(@Param("since") LocalDateTime since);

    /**
     * 查找用户参与的所有评审任务（创建的或分配的）
     */
    @Query("SELECT DISTINCT rt FROM ReviewTask rt " +
           "LEFT JOIN rt.assignments a " +
           "WHERE rt.creatorId = :userId " +
           "OR (a.assigneeId = :userId AND a.status IN ('ACCEPTED', 'IN_PROGRESS'))")
    Page<ReviewTask> findUserRelatedTasks(@Param("userId") Long userId, Pageable pageable);
}
