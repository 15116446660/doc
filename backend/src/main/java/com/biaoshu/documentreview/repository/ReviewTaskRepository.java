package com.biaoshu.documentreview.repository;

import com.biaoshu.documentreview.entity.ReviewTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 评审任务Repository
 */
@Repository
public interface ReviewTaskRepository extends JpaRepository<ReviewTask, Long> {

    /**
     * 根据项目ID查找评审任务
     */
    Page<ReviewTask> findByProjectId(Long projectId, Pageable pageable);

    /**
     * 根据文档ID查找评审任务
     */
    Page<ReviewTask> findByDocumentId(Long documentId, Pageable pageable);

    /**
     * 根据创建者ID查找评审任务
     */
    Page<ReviewTask> findByCreatorId(Long creatorId, Pageable pageable);

    /**
     * 根据状态查找评审任务
     */
    Page<ReviewTask> findByStatus(ReviewTask.ReviewTaskStatus status, Pageable pageable);

    /**
     * 根据优先级查找评审任务
     */
    Page<ReviewTask> findByPriority(ReviewTask.ReviewTaskPriority priority, Pageable pageable);

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
