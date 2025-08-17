package com.biaoshu.documentreview.repository;

import com.biaoshu.documentreview.entity.ProjectCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ProjectCategory的JPA仓库
 */
@Repository
public interface ProjectCategoryRepository extends JpaRepository<ProjectCategory, Long>, JpaSpecificationExecutor<ProjectCategory> {

    /**
     * 根据父ID查找所有子分类
     * @param parentId 父ID
     * @return 子分类列表
     */
    List<ProjectCategory> findByParentId(Long parentId);

    /**
     * 根据父ID和类型查找所有子分类
     * @param parentId 父ID
     * @param type 分类类型
     * @return 子分类列表
     */
    List<ProjectCategory> findByParentIdAndType(Long parentId, ProjectCategory.CategoryType type);

    /**
     * 根据名称和类型查找分类
     * @param name 分类名称
     * @param type 分类类型
     * @return 分类实体
     */
    Optional<ProjectCategory> findByNameAndType(String name, ProjectCategory.CategoryType type);

    /**
     * 查找所有顶级分类（父ID为null）
     * @return 顶级分类列表
     */
    List<ProjectCategory> findByParentIdIsNull();
}
