package com.biaoshu.documentreview.repository;

import com.biaoshu.documentreview.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 部门数据访问层
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /**
     * 根据父部门ID查询子部门列表
     */
    List<Department> findByParentIdAndEnabledTrueOrderBySortOrderAsc(Long parentId);

    /**
     * 查询所有根部门（父部门ID为空）
     */
    List<Department> findByParentIdIsNullAndEnabledTrueOrderBySortOrderAsc();

    /**
     * 根据部门编码查询部门
     */
    Optional<Department> findByCodeAndEnabledTrue(String code);

    /**
     * 根据部门名称查询部门
     */
    List<Department> findByNameContainingAndEnabledTrueOrderBySortOrderAsc(String name);

    /**
     * 查询指定部门的所有子部门（递归查询）
     */
    @Query("SELECT d FROM Department d WHERE d.path LIKE CONCAT(:path, '%') AND d.enabled = true ORDER BY d.sortOrder ASC")
    List<Department> findAllChildrenByPath(@Param("path") String path);

    /**
     * 查询部门层级树
     */
    @Query("SELECT d FROM Department d WHERE d.enabled = true ORDER BY d.level ASC, d.sortOrder ASC")
    List<Department> findDepartmentTree();

    /**
     * 根据负责人ID查询部门
     */
    List<Department> findByManagerIdAndEnabledTrue(Long managerId);

    /**
     * 检查部门编码是否存在（排除指定ID）
     */
    @Query("SELECT COUNT(d) > 0 FROM Department d WHERE d.code = :code AND d.id != :id AND d.enabled = true")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("id") Long id);

    /**
     * 检查部门编码是否存在
     */
    boolean existsByCodeAndEnabledTrue(String code);

    /**
     * 查询指定层级的部门
     */
    List<Department> findByLevelAndEnabledTrueOrderBySortOrderAsc(Integer level);

    /**
     * 统计部门下的子部门数量
     */
    @Query("SELECT COUNT(d) FROM Department d WHERE d.parentId = :parentId AND d.enabled = true")
    long countChildrenByParentId(@Param("parentId") Long parentId);
}
