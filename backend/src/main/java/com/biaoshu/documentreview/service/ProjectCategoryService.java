package com.biaoshu.documentreview.service;

import com.biaoshu.documentreview.dto.ProjectCategoryDTO;
import com.biaoshu.documentreview.entity.ProjectCategory;

import java.util.List;

/**
 * 项目分类服务接口
 */
public interface ProjectCategoryService {

    /**
     * 创建一个新的项目分类
     * @param createDTO 创建信息
     * @return 创建的分类实体
     */
    ProjectCategory createCategory(ProjectCategoryDTO.Create createDTO);

    /**
     * 更新项目分类
     * @param id 要更新的分类ID
     * @param updateDTO 更新信息
     * @return 更新后的分类实体
     */
    ProjectCategory updateCategory(Long id, ProjectCategoryDTO.Update updateDTO);

    /**
     * 删除项目分类
     * @param id 要删除的分类ID
     */
    void deleteCategory(Long id);

    /**
     * 根据ID获取项目分类
     * @param id 分类ID
     * @return 分类实体
     */
    ProjectCategory getCategoryById(Long id);

    /**
     * 获取项目分类的树形结构
     * @return 树形结构的根节点列表
     */
    List<ProjectCategoryDTO.TreeNode> getCategoryTree();
}
