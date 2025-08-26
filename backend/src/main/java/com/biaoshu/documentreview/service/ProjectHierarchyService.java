package com.biaoshu.documentreview.service;

import com.biaoshu.documentreview.dto.ProjectHierarchyTreeDTO;

import java.util.List;
import java.util.Map;

/**
 * 项目层级管理服务接口
 */
public interface ProjectHierarchyService {

    /**
     * 获取完整的项目层级树
     */
    List<ProjectHierarchyTreeDTO> getProjectHierarchyTree();

    /**
     * 获取指定部门的项目层级树
     */
    ProjectHierarchyTreeDTO getDepartmentHierarchyTree(Long departmentId);

    /**
     * 获取指定品类的层级树
     */
    ProjectHierarchyTreeDTO getCategoryHierarchyTree(Long categoryId);

    /**
     * 获取指定子品类的层级树
     */
    ProjectHierarchyTreeDTO getSubcategoryHierarchyTree(Long subcategoryId);

    /**
     * 搜索项目层级结构
     */
    List<ProjectHierarchyTreeDTO> searchHierarchy(String keyword, String type);

    /**
     * 移动层级节点
     */
    void moveNode(String nodeType, Long nodeId, Long targetParentId, Integer newSortOrder);

    /**
     * 批量更新排序
     */
    void batchUpdateSortOrder(String nodeType, List<Map<String, Object>> sortItems);

    /**
     * 获取层级统计信息
     */
    Map<String, Object> getHierarchyStatistics();

    /**
     * 获取指定部门的统计信息
     */
    Map<String, Object> getDepartmentStatistics(Long departmentId);

    /**
     * 获取节点的面包屑导航
     */
    List<Map<String, Object>> getBreadcrumb(String nodeType, Long nodeId);

    /**
     * 验证移动操作是否合法
     */
    Map<String, Object> validateMove(String nodeType, Long nodeId, Long targetParentId);

    /**
     * 构建树形结构
     */
    List<ProjectHierarchyTreeDTO> buildTree(List<ProjectHierarchyTreeDTO> nodes);

    /**
     * 获取节点的所有子节点（递归）
     */
    List<ProjectHierarchyTreeDTO> getAllChildren(String nodeType, Long nodeId);

    /**
     * 获取节点的所有父节点（递归）
     */
    List<ProjectHierarchyTreeDTO> getAllParents(String nodeType, Long nodeId);
}
