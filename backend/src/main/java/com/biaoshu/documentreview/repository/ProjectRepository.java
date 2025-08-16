package com.biaoshu.documentreview.repository;

import com.biaoshu.documentreview.entity.Project;
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
 * 项目数据访问接口
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * 根据项目编号查找项目
     */
    Optional<Project> findByCode(String code);

    /**
     * 根据项目名称查找项目
     */
    List<Project> findByNameContainingIgnoreCase(String name);

    /**
     * 根据项目状态查找项目
     */
    List<Project> findByStatus(Project.ProjectStatus status);

    /**
     * 根据项目负责人查找项目
     */
    List<Project> findByManagerId(Long managerId);

    /**
     * 根据项目优先级查找项目
     */
    List<Project> findByPriority(Project.ProjectPriority priority);

    /**
     * 查找指定时间范围内的项目
     */
    @Query("SELECT p FROM Project p WHERE p.plannedStartTime >= :startTime AND p.plannedEndTime <= :endTime")
    List<Project> findByPlannedTimeRange(@Param("startTime") LocalDateTime startTime, 
                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 查找即将到期的项目（计划结束时间在指定天数内）
     */
    @Query("SELECT p FROM Project p WHERE p.plannedEndTime BETWEEN :now AND :deadline AND p.status != 'COMPLETED' AND p.status != 'ARCHIVED'")
    List<Project> findProjectsNearDeadline(@Param("now") LocalDateTime now, 
                                         @Param("deadline") LocalDateTime deadline);

    /**
     * 查找用户参与的项目
     */
    @Query("SELECT DISTINCT p FROM Project p JOIN p.members m WHERE m.userId = :userId AND m.isActive = true")
    List<Project> findProjectsByUserId(@Param("userId") Long userId);

    /**
     * 查找用户管理的项目
     */
    @Query("SELECT p FROM Project p WHERE p.managerId = :userId OR EXISTS (SELECT 1 FROM ProjectMember pm WHERE pm.projectId = p.id AND pm.userId = :userId AND pm.role = 'MANAGER' AND pm.isActive = true)")
    List<Project> findManagedProjectsByUserId(@Param("userId") Long userId);

    /**
     * 分页查询项目（支持多条件搜索）
     */
    @Query("SELECT p FROM Project p WHERE " +
           "(:name IS NULL OR p.name LIKE %:name%) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:managerId IS NULL OR p.managerId = :managerId) AND " +
           "(:priority IS NULL OR p.priority = :priority)")
    Page<Project> findProjectsWithConditions(@Param("name") String name,
                                            @Param("status") Project.ProjectStatus status,
                                            @Param("managerId") Long managerId,
                                            @Param("priority") Project.ProjectPriority priority,
                                            Pageable pageable);

    /**
     * 统计各状态项目数量
     */
    @Query("SELECT p.status, COUNT(p) FROM Project p GROUP BY p.status")
    List<Object[]> countProjectsByStatus();

    /**
     * 统计用户参与的项目数量
     */
    @Query("SELECT COUNT(DISTINCT p) FROM Project p JOIN p.members m WHERE m.userId = :userId AND m.isActive = true")
    Long countProjectsByUserId(@Param("userId") Long userId);

    /**
     * 检查项目编号是否存在
     */
    boolean existsByCode(String code);

    /**
     * 检查项目编号是否存在（排除指定ID）
     */
    @Query("SELECT COUNT(p) > 0 FROM Project p WHERE p.code = :code AND p.id != :id")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("id") Long id);
}
