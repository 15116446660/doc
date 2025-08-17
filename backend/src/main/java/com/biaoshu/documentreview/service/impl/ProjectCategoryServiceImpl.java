package com.biaoshu.documentreview.service.impl;

import com.biaoshu.documentreview.dto.ProjectCategoryDTO;
import com.biaoshu.documentreview.entity.ProjectCategory;
import com.biaoshu.documentreview.exception.BusinessException;
import com.biaoshu.documentreview.repository.ProjectCategoryRepository;
import com.biaoshu.documentreview.service.ProjectCategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProjectCategoryServiceImpl implements ProjectCategoryService {

    @Autowired
    private ProjectCategoryRepository categoryRepository;

    @Override
    @Transactional
    public ProjectCategory createCategory(ProjectCategoryDTO.Create createDTO) {
        ProjectCategory category = new ProjectCategory();
        BeanUtils.copyProperties(createDTO, category);
        if (createDTO.getParentId() != null && !categoryRepository.existsById(createDTO.getParentId())) {
            throw new BusinessException("父分类不存在");
        }
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public ProjectCategory updateCategory(Long id, ProjectCategoryDTO.Update updateDTO) {
        ProjectCategory category = getCategoryById(id);
        if (updateDTO.getName() != null) {
            category.setName(updateDTO.getName());
        }
        if (updateDTO.getParentId() != null) {
            if (id.equals(updateDTO.getParentId())) {
                throw new BusinessException("不能将分类设置到自身之下");
            }
            if (!categoryRepository.existsById(updateDTO.getParentId())) {
                throw new BusinessException("父分类不存在");
            }
            category.setParentId(updateDTO.getParentId());
        }
        if (updateDTO.getSortOrder() != null) {
            category.setSortOrder(updateDTO.getSortOrder());
        }
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        ProjectCategory category = getCategoryById(id);
        // 检查是否有子分类
        if (!CollectionUtils.isEmpty(category.getChildren())) {
            throw new BusinessException("该分类下有子分类，不能删除");
        }
        // 检查是否关联了项目
        if (!CollectionUtils.isEmpty(category.getProjects())) {
            throw new BusinessException("该分类下有关联的项目，不能删除");
        }
        categoryRepository.deleteById(id);
    }

    @Override
    public ProjectCategory getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("ID为 " + id + " 的项目分类不存在"));
    }

    @Override
    public List<ProjectCategoryDTO.TreeNode> getCategoryTree() {
        List<ProjectCategory> allCategories = categoryRepository.findAll();
        List<ProjectCategoryDTO.TreeNode> allNodes = allCategories.stream().map(this::convertToTreeNode).collect(Collectors.toList());

        Map<Long, ProjectCategoryDTO.TreeNode> nodeMap = allNodes.stream()
                .collect(Collectors.toMap(ProjectCategoryDTO.TreeNode::getId, node -> node));

        List<ProjectCategoryDTO.TreeNode> tree = new ArrayList<>();
        for (ProjectCategoryDTO.TreeNode node : allNodes) {
            if (node.getParentId() == null) {
                tree.add(node);
            } else {
                ProjectCategoryDTO.TreeNode parent = nodeMap.get(node.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(node);
                    parent.setHasChildren(true);
                }
            }
        }
        return tree;
    }

    private ProjectCategoryDTO.TreeNode convertToTreeNode(ProjectCategory category) {
        ProjectCategoryDTO.TreeNode node = new ProjectCategoryDTO.TreeNode();
        BeanUtils.copyProperties(category, node);
        return node;
    }
}
