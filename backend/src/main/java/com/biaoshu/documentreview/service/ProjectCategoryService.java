package com.biaoshu.documentreview.service;

import com.biaoshu.documentreview.entity.ProjectCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 项目品类服务接口
 */
public interface ProjectCategoryService {

    /**
     * 创建品类
     */
    ProjectCategory createCategory(ProjectCategory category);

    /**
     * 更新品类
     */
    ProjectCategory updateCategory(Long id, ProjectCategory category);

    /**
     * 删除品类（软删除）
     */
    void deleteCategory(Long id);

    /**
     * 根据ID查询品类
     */
    Optional<ProjectCategory> findById(Long id);

    /**
     * 查询所有品类（分页）
     */
    Page<ProjectCategory> findAll(Pageable pageable);

    /**
     * 根据部门ID查询品类列表
     */
    List<ProjectCategory> findByDepartmentId(Long departmentId);

    /**
     * 根据品类名称搜索
     */
    List<ProjectCategory> searchByName(String name);

    /**
     * 根据品类编码查询
     */
    Optional<ProjectCategory> findByCode(String code);

    /**
     * 检查品类编码是否存在
     */
    boolean existsByCode(String code);

    /**
     * 检查品类编码是否存在（排除指定ID）
     */
    boolean existsByCodeAndIdNot(String code, Long id);

    /**
     * 更新品类排序
     */
    void updateSortOrder(Long categoryId, Integer sortOrder);

    /**
     * 启用/禁用品类
     */
    ProjectCategory toggleEnabled(Long id, Boolean enabled);

    /**
     * 统计部门下的品类数量
     */
    long countByDepartmentId(Long departmentId);

    /**
     * 根据多个部门ID查询品类
     */
    List<ProjectCategory> findByDepartmentIds(List<Long> departmentIds);

    /**
     * 查询品类及其子品类
     */
    List<ProjectCategory> findCategoriesWithSubcategories(Long departmentId);
}
