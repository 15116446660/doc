package com.biaoshu.documentreview.repository;

import com.biaoshu.documentreview.entity.ProjectCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 项目品类数据访问层
 */
@Repository
public interface ProjectCategoryRepository extends JpaRepository<ProjectCategory, Long> {

    /**
     * 根据部门ID查询品类列表
     */
    List<ProjectCategory> findByDepartmentIdAndEnabledTrueOrderBySortOrderAsc(Long departmentId);

    /**
     * 根据品类编码查询品类
     */
    Optional<ProjectCategory> findByCodeAndEnabledTrue(String code);

    /**
     * 根据品类名称模糊查询
     */
    List<ProjectCategory> findByNameContainingAndEnabledTrueOrderBySortOrderAsc(String name);

    /**
     * 根据负责人ID查询品类
     */
    List<ProjectCategory> findByManagerIdAndEnabledTrue(Long managerId);

    /**
     * 检查品类编码是否存在（排除指定ID）
     */
    @Query("SELECT COUNT(c) > 0 FROM ProjectCategory c WHERE c.code = :code AND c.id != :id AND c.enabled = true")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("id") Long id);

    /**
     * 检查品类编码是否存在
     */
    boolean existsByCodeAndEnabledTrue(String code);

    /**
     * 查询部门下的品类数量
     */
    @Query("SELECT COUNT(c) FROM ProjectCategory c WHERE c.departmentId = :departmentId AND c.enabled = true")
    long countByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * 根据部门ID列表查询品类
     */
    @Query("SELECT c FROM ProjectCategory c WHERE c.departmentId IN :departmentIds AND c.enabled = true ORDER BY c.sortOrder ASC")
    List<ProjectCategory> findByDepartmentIdInAndEnabledTrue(@Param("departmentIds") List<Long> departmentIds);

    /**
     * 查询所有启用的品类
     */
    List<ProjectCategory> findByEnabledTrueOrderBySortOrderAsc();

    /**
     * 根据部门ID和品类名称查询
     */
    @Query("SELECT c FROM ProjectCategory c WHERE c.departmentId = :departmentId AND c.name LIKE %:name% AND c.enabled = true ORDER BY c.sortOrder ASC")
    List<ProjectCategory> findByDepartmentIdAndNameContaining(@Param("departmentId") Long departmentId, @Param("name") String name);
}
