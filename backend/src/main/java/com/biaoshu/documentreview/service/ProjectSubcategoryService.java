package com.biaoshu.documentreview.service;

import com.biaoshu.documentreview.entity.ProjectSubcategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 项目子品类服务接口
 */
public interface ProjectSubcategoryService {

    /**
     * 创建子品类
     */
    ProjectSubcategory createSubcategory(ProjectSubcategory subcategory);

    /**
     * 更新子品类
     */
    ProjectSubcategory updateSubcategory(Long id, ProjectSubcategory subcategory);

    /**
     * 删除子品类（软删除）
     */
    void deleteSubcategory(Long id);

    /**
     * 根据ID查询子品类
     */
    Optional<ProjectSubcategory> findById(Long id);

    /**
     * 查询所有子品类（分页）
     */
    Page<ProjectSubcategory> findAll(Pageable pageable);

    /**
     * 根据品类ID查询子品类列表
     */
    List<ProjectSubcategory> findByCategoryId(Long categoryId);

    /**
     * 根据子品类名称搜索
     */
    List<ProjectSubcategory> searchByName(String name);

    /**
     * 根据子品类编码查询
     */
    Optional<ProjectSubcategory> findByCode(String code);

    /**
     * 检查子品类编码是否存在
     */
    boolean existsByCode(String code);

    /**
     * 检查子品类编码是否存在（排除指定ID）
     */
    boolean existsByCodeAndIdNot(String code, Long id);

    /**
     * 更新子品类排序
     */
    void updateSortOrder(Long subcategoryId, Integer sortOrder);

    /**
     * 启用/禁用子品类
     */
    ProjectSubcategory toggleEnabled(Long id, Boolean enabled);

    /**
     * 统计品类下的子品类数量
     */
    long countByCategoryId(Long categoryId);

    /**
     * 根据多个品类ID查询子品类
     */
    List<ProjectSubcategory> findByCategoryIds(List<Long> categoryIds);

    /**
     * 查询子品类及其项目
     */
    List<ProjectSubcategory> findSubcategoriesWithProjects(Long categoryId);

    /**
     * 根据部门ID查询所有子品类
     */
    List<ProjectSubcategory> findByDepartmentId(Long departmentId);
}
