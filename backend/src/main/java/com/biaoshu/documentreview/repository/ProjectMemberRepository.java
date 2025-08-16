package com.biaoshu.documentreview.repository;

import com.biaoshu.documentreview.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 项目成员数据访问接口
 */
@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    /**
     * 根据项目ID查找所有活跃成员
     */
    List<ProjectMember> findByProjectIdAndIsActiveTrue(Long projectId);

    /**
     * 根据项目ID查找所有成员（包括非活跃）
     */
    List<ProjectMember> findByProjectId(Long projectId);

    /**
     * 根据用户ID查找所有活跃项目成员关系
     */
    List<ProjectMember> findByUserIdAndIsActiveTrue(Long userId);

    /**
     * 根据项目ID和用户ID查找成员关系
     */
    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);

    /**
     * 根据项目ID和用户ID查找活跃成员关系
     */
    Optional<ProjectMember> findByProjectIdAndUserIdAndIsActiveTrue(Long projectId, Long userId);

    /**
     * 根据项目ID和角色查找成员
     */
    List<ProjectMember> findByProjectIdAndRoleAndIsActiveTrue(Long projectId, ProjectMember.MemberRole role);

    /**
     * 根据项目ID和权限查找成员
     */
    List<ProjectMember> findByProjectIdAndPermissionAndIsActiveTrue(Long projectId, ProjectMember.MemberPermission permission);

    /**
     * 检查用户是否是项目成员
     */
    @Query("SELECT COUNT(pm) > 0 FROM ProjectMember pm WHERE pm.projectId = :projectId AND pm.userId = :userId AND pm.isActive = true")
    boolean isProjectMember(@Param("projectId") Long projectId, @Param("userId") Long userId);

    /**
     * 检查用户是否是项目管理员
     */
    @Query("SELECT COUNT(pm) > 0 FROM ProjectMember pm WHERE pm.projectId = :projectId AND pm.userId = :userId AND pm.role = 'MANAGER' AND pm.isActive = true")
    boolean isProjectManager(@Param("projectId") Long projectId, @Param("userId") Long userId);

    /**
     * 检查用户是否有项目写权限
     */
    @Query("SELECT COUNT(pm) > 0 FROM ProjectMember pm WHERE pm.projectId = :projectId AND pm.userId = :userId AND pm.permission IN ('WRITE', 'ADMIN') AND pm.isActive = true")
    boolean hasWritePermission(@Param("projectId") Long projectId, @Param("userId") Long userId);

    /**
     * 检查用户是否有项目管理权限
     */
    @Query("SELECT COUNT(pm) > 0 FROM ProjectMember pm WHERE pm.projectId = :projectId AND pm.userId = :userId AND pm.permission = 'ADMIN' AND pm.isActive = true")
    boolean hasAdminPermission(@Param("projectId") Long projectId, @Param("userId") Long userId);

    /**
     * 统计项目活跃成员数量
     */
    @Query("SELECT COUNT(pm) FROM ProjectMember pm WHERE pm.projectId = :projectId AND pm.isActive = true")
    Long countActiveMembers(@Param("projectId") Long projectId);

    /**
     * 统计用户参与的活跃项目数量
     */
    @Query("SELECT COUNT(pm) FROM ProjectMember pm WHERE pm.userId = :userId AND pm.isActive = true")
    Long countActiveProjects(@Param("userId") Long userId);

    /**
     * 批量更新成员状态为非活跃
     */
    @Modifying
    @Query("UPDATE ProjectMember pm SET pm.isActive = false, pm.leftTime = CURRENT_TIMESTAMP WHERE pm.projectId = :projectId AND pm.userId IN :userIds")
    int deactivateMembers(@Param("projectId") Long projectId, @Param("userIds") List<Long> userIds);

    /**
     * 删除项目的所有成员
     */
    @Modifying
    @Query("DELETE FROM ProjectMember pm WHERE pm.projectId = :projectId")
    int deleteByProjectId(@Param("projectId") Long projectId);

    /**
     * 查找项目的管理员成员
     */
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.projectId = :projectId AND (pm.role = 'MANAGER' OR pm.permission = 'ADMIN') AND pm.isActive = true")
    List<ProjectMember> findProjectAdmins(@Param("projectId") Long projectId);
}
