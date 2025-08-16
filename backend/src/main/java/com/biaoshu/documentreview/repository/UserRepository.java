package com.biaoshu.documentreview.repository;

import com.biaoshu.documentreview.entity.User;
import com.biaoshu.documentreview.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户Repository
 * 
 * @author biaoshu
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsernameAndIsDeletedFalse(String username);

    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmailAndIsDeletedFalse(String email);

    /**
     * 根据员工编号查找用户
     */
    Optional<User> findByEmployeeIdAndIsDeletedFalse(String employeeId);

    /**
     * 根据用户名或邮箱查找用户
     */
    @Query("SELECT u FROM User u WHERE (u.username = :usernameOrEmail OR u.email = :usernameOrEmail) AND u.isDeleted = false")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

    /**
     * 根据状态查找用户
     */
    List<User> findByStatusAndIsDeletedFalse(UserStatus status);

    /**
     * 根据部门ID查找用户
     */
    List<User> findByDepartmentIdAndIsDeletedFalse(Long departmentId);

    /**
     * 根据角色查找用户
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.id = :roleId AND u.isDeleted = false")
    List<User> findByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据角色编码查找用户
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.roleCode = :roleCode AND u.isDeleted = false")
    List<User> findByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsernameAndIsDeletedFalse(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmailAndIsDeletedFalse(String email);

    /**
     * 检查员工编号是否存在
     */
    boolean existsByEmployeeIdAndIsDeletedFalse(String employeeId);

    /**
     * 根据真实姓名模糊查询用户
     */
    @Query("SELECT u FROM User u WHERE u.realName LIKE %:realName% AND u.isDeleted = false")
    Page<User> findByRealNameContaining(@Param("realName") String realName, Pageable pageable);

    /**
     * 根据部门和状态查找用户
     */
    Page<User> findByDepartmentIdAndStatusAndIsDeletedFalse(Long departmentId, UserStatus status, Pageable pageable);

    /**
     * 统计部门用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.departmentId = :departmentId AND u.isDeleted = false")
    long countByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * 统计各状态用户数量
     */
    @Query("SELECT u.status, COUNT(u) FROM User u WHERE u.isDeleted = false GROUP BY u.status")
    List<Object[]> countByStatus();
}
