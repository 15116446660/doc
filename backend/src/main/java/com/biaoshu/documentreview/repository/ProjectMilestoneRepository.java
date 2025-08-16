package com.biaoshu.documentreview.repository;

import com.biaoshu.documentreview.entity.ProjectMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目里程碑数据访问接口
 */
@Repository
public interface ProjectMilestoneRepository extends JpaRepository<ProjectMilestone, Long> {

    /**
     * 根据项目ID查找所有里程碑（按排序号排序）
     */
    List<ProjectMilestone> findByProjectIdOrderBySortOrderAsc(Long projectId);

    /**
     * 根据项目ID和状态查找里程碑
     */
    List<ProjectMilestone> findByProjectIdAndStatus(Long projectId, ProjectMilestone.MilestoneStatus status);

    /**
     * 根据项目ID和类型查找里程碑
     */
    List<ProjectMilestone> findByProjectIdAndType(Long projectId, ProjectMilestone.MilestoneType type);

    /**
     * 根据负责人查找里程碑
     */
    List<ProjectMilestone> findByAssigneeId(Long assigneeId);

    /**
     * 根据负责人和状态查找里程碑
     */
    List<ProjectMilestone> findByAssigneeIdAndStatus(Long assigneeId, ProjectMilestone.MilestoneStatus status);

    /**
     * 查找即将到期的里程碑
     */
    @Query("SELECT m FROM ProjectMilestone m WHERE m.plannedEndTime BETWEEN :now AND :deadline AND m.status != 'COMPLETED' AND m.status != 'CANCELLED'")
    List<ProjectMilestone> findMilestonesNearDeadline(@Param("now") LocalDateTime now, 
                                                     @Param("deadline") LocalDateTime deadline);

    /**
     * 查找已逾期的里程碑
     */
    @Query("SELECT m FROM ProjectMilestone m WHERE m.plannedEndTime < :now AND m.status != 'COMPLETED' AND m.status != 'CANCELLED'")
    List<ProjectMilestone> findOverdueMilestones(@Param("now") LocalDateTime now);

    /**
     * 查找关键路径上的里程碑
     */
    List<ProjectMilestone> findByProjectIdAndIsCriticalTrue(Long projectId);

    /**
     * 统计项目里程碑各状态数量
     */
    @Query("SELECT m.status, COUNT(m) FROM ProjectMilestone m WHERE m.projectId = :projectId GROUP BY m.status")
    List<Object[]> countMilestonesByStatus(@Param("projectId") Long projectId);

    /**
     * 计算项目整体进度（基于里程碑完成情况）
     */
    @Query("SELECT AVG(m.progress) FROM ProjectMilestone m WHERE m.projectId = :projectId")
    Double calculateProjectProgress(@Param("projectId") Long projectId);

    /**
     * 查找项目中优先级最高的未完成里程碑
     */
    @Query("SELECT m FROM ProjectMilestone m WHERE m.projectId = :projectId AND m.status != 'COMPLETED' AND m.status != 'CANCELLED' ORDER BY m.priority DESC, m.plannedEndTime ASC")
    List<ProjectMilestone> findHighPriorityPendingMilestones(@Param("projectId") Long projectId);

    /**
     * 获取项目下一个排序号
     */
    @Query("SELECT COALESCE(MAX(m.sortOrder), 0) + 1 FROM ProjectMilestone m WHERE m.projectId = :projectId")
    Integer getNextSortOrder(@Param("projectId") Long projectId);

    /**
     * 批量更新里程碑排序号
     */
    @Modifying
    @Query("UPDATE ProjectMilestone m SET m.sortOrder = :sortOrder WHERE m.id = :id")
    int updateSortOrder(@Param("id") Long id, @Param("sortOrder") Integer sortOrder);

    /**
     * 删除项目的所有里程碑
     */
    @Modifying
    @Query("DELETE FROM ProjectMilestone m WHERE m.projectId = :projectId")
    int deleteByProjectId(@Param("projectId") Long projectId);

    /**
     * 统计用户负责的里程碑数量
     */
    @Query("SELECT COUNT(m) FROM ProjectMilestone m WHERE m.assigneeId = :userId")
    Long countMilestonesByAssignee(@Param("userId") Long userId);

    /**
     * 统计用户负责的未完成里程碑数量
     */
    @Query("SELECT COUNT(m) FROM ProjectMilestone m WHERE m.assigneeId = :userId AND m.status != 'COMPLETED' AND m.status != 'CANCELLED'")
    Long countPendingMilestonesByAssignee(@Param("userId") Long userId);
}
