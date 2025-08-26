package com.biaoshu.documentreview.repository;

import com.biaoshu.documentreview.entity.ProjectSubcategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 项目子品类数据访问层
 */
@Repository
public interface ProjectSubcategoryRepository extends JpaRepository<ProjectSubcategory, Long> {

    /**
     * 根据品类ID查询子品类列表
     */
    List<ProjectSubcategory> findByCategoryIdAndEnabledTrueOrderBySortOrderAsc(Long categoryId);

    /**
     * 根据子品类编码查询子品类
     */
    Optional<ProjectSubcategory> findByCodeAndEnabledTrue(String code);

    /**
     * 根据子品类名称模糊查询
     */
    List<ProjectSubcategory> findByNameContainingAndEnabledTrueOrderBySortOrderAsc(String name);

    /**
     * 根据负责人ID查询子品类
     */
    List<ProjectSubcategory> findByManagerIdAndEnabledTrue(Long managerId);

    /**
     * 检查子品类编码是否存在（排除指定ID）
     */
    @Query("SELECT COUNT(s) > 0 FROM ProjectSubcategory s WHERE s.code = :code AND s.id != :id AND s.enabled = true")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("id") Long id);

    /**
     * 检查子品类编码是否存在
     */
    boolean existsByCodeAndEnabledTrue(String code);

    /**
     * 查询品类下的子品类数量
     */
    @Query("SELECT COUNT(s) FROM ProjectSubcategory s WHERE s.categoryId = :categoryId AND s.enabled = true")
    long countByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 根据品类ID列表查询子品类
     */
    @Query("SELECT s FROM ProjectSubcategory s WHERE s.categoryId IN :categoryIds AND s.enabled = true ORDER BY s.sortOrder ASC")
    List<ProjectSubcategory> findByCategoryIdInAndEnabledTrue(@Param("categoryIds") List<Long> categoryIds);

    /**
     * 查询所有启用的子品类
     */
    List<ProjectSubcategory> findByEnabledTrueOrderBySortOrderAsc();

    /**
     * 根据品类ID和子品类名称查询
     */
    @Query("SELECT s FROM ProjectSubcategory s WHERE s.categoryId = :categoryId AND s.name LIKE %:name% AND s.enabled = true ORDER BY s.sortOrder ASC")
    List<ProjectSubcategory> findByCategoryIdAndNameContaining(@Param("categoryId") Long categoryId, @Param("name") String name);

    /**
     * 根据部门ID查询所有子品类（通过品类关联）
     */
    @Query("SELECT s FROM ProjectSubcategory s JOIN ProjectCategory c ON s.categoryId = c.id WHERE c.departmentId = :departmentId AND s.enabled = true ORDER BY s.sortOrder ASC")
    List<ProjectSubcategory> findByDepartmentIdThroughCategory(@Param("departmentId") Long departmentId);

    /**
     * 根据部门ID查询所有子品类（通过品类关联）
     */
    @Query("SELECT s FROM ProjectSubcategory s JOIN s.category c WHERE c.departmentId = :departmentId AND s.enabled = true AND c.enabled = true ORDER BY s.sortOrder ASC")
    List<ProjectSubcategory> findByDepartmentId(@Param("departmentId") Long departmentId);
}
