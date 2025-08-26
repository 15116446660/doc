package com.biaoshu.documentreview.service.impl;

import com.biaoshu.documentreview.dto.ProjectHierarchyTreeDTO;
import com.biaoshu.documentreview.entity.Department;
import com.biaoshu.documentreview.entity.Project;
import com.biaoshu.documentreview.entity.ProjectCategory;
import com.biaoshu.documentreview.entity.ProjectSubcategory;
import com.biaoshu.documentreview.repository.DepartmentRepository;
import com.biaoshu.documentreview.repository.ProjectCategoryRepository;
import com.biaoshu.documentreview.repository.ProjectRepository;
import com.biaoshu.documentreview.repository.ProjectSubcategoryRepository;
import com.biaoshu.documentreview.service.ProjectHierarchyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 项目层级管理服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectHierarchyServiceImpl implements ProjectHierarchyService {

    private final DepartmentRepository departmentRepository;
    private final ProjectCategoryRepository categoryRepository;
    private final ProjectSubcategoryRepository subcategoryRepository;
    private final ProjectRepository projectRepository;

    @Override
    public List<ProjectHierarchyTreeDTO> getProjectHierarchyTree() {
        log.info("获取完整的项目层级树");
        
        // 获取所有部门
        List<Department> departments = departmentRepository.findByParentIdIsNullAndEnabledTrueOrderBySortOrderAsc();
        
        return departments.stream()
                .map(this::buildDepartmentTree)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectHierarchyTreeDTO getDepartmentHierarchyTree(Long departmentId) {
        log.info("获取部门 {} 的层级树", departmentId);
        
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("部门不存在: " + departmentId));
        
        return buildDepartmentTree(department);
    }

    @Override
    public ProjectHierarchyTreeDTO getCategoryHierarchyTree(Long categoryId) {
        log.info("获取品类 {} 的层级树", categoryId);
        
        ProjectCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("品类不存在: " + categoryId));
        
        return buildCategoryTree(category);
    }

    @Override
    public ProjectHierarchyTreeDTO getSubcategoryHierarchyTree(Long subcategoryId) {
        log.info("获取子品类 {} 的层级树", subcategoryId);
        
        ProjectSubcategory subcategory = subcategoryRepository.findById(subcategoryId)
                .orElseThrow(() -> new IllegalArgumentException("子品类不存在: " + subcategoryId));
        
        return buildSubcategoryTree(subcategory);
    }

    @Override
    public List<ProjectHierarchyTreeDTO> searchHierarchy(String keyword, String type) {
        log.info("搜索项目层级结构: {} - {}", keyword, type);
        
        List<ProjectHierarchyTreeDTO> results = new ArrayList<>();
        
        if ("all".equals(type) || "department".equals(type)) {
            List<Department> departments = departmentRepository.findByNameContainingAndEnabledTrueOrderBySortOrderAsc(keyword);
            results.addAll(departments.stream().map(this::convertDepartmentToDTO).collect(Collectors.toList()));
        }
        
        if ("all".equals(type) || "category".equals(type)) {
            List<ProjectCategory> categories = categoryRepository.findByNameContainingAndEnabledTrueOrderBySortOrderAsc(keyword);
            results.addAll(categories.stream().map(this::convertCategoryToDTO).collect(Collectors.toList()));
        }
        
        if ("all".equals(type) || "subcategory".equals(type)) {
            List<ProjectSubcategory> subcategories = subcategoryRepository.findByNameContainingAndEnabledTrueOrderBySortOrderAsc(keyword);
            results.addAll(subcategories.stream().map(this::convertSubcategoryToDTO).collect(Collectors.toList()));
        }
        
        if ("all".equals(type) || "project".equals(type)) {
            // TODO: 实现项目搜索
        }
        
        return results;
    }

    @Override
    public void moveNode(String nodeType, Long nodeId, Long targetParentId, Integer newSortOrder) {
        log.info("移动节点: {} {} 到父节点 {}, 排序 {}", nodeType, nodeId, targetParentId, newSortOrder);
        
        // TODO: 实现节点移动逻辑
        switch (nodeType.toLowerCase()) {
            case "department":
                // 移动部门
                break;
            case "category":
                // 移动品类
                break;
            case "subcategory":
                // 移动子品类
                break;
            case "project":
                // 移动项目
                break;
        }
    }

    @Override
    public void batchUpdateSortOrder(String nodeType, List<Map<String, Object>> sortItems) {
        log.info("批量更新排序: {} - {}", nodeType, sortItems.size());
        
        // TODO: 实现批量排序更新
    }

    @Override
    public Map<String, Object> getHierarchyStatistics() {
        log.info("获取层级统计信息");
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalDepartments", departmentRepository.count());
        statistics.put("totalCategories", categoryRepository.count());
        statistics.put("totalSubcategories", subcategoryRepository.count());
        statistics.put("totalProjects", projectRepository.count());
        
        return statistics;
    }

    @Override
    public Map<String, Object> getDepartmentStatistics(Long departmentId) {
        log.info("获取部门 {} 的统计信息", departmentId);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("categoryCount", categoryRepository.countByDepartmentId(departmentId));
        // TODO: 添加更多统计信息
        
        return statistics;
    }

    @Override
    public List<Map<String, Object>> getBreadcrumb(String nodeType, Long nodeId) {
        log.info("获取节点面包屑: {} - {}", nodeType, nodeId);
        
        List<Map<String, Object>> breadcrumb = new ArrayList<>();
        
        // TODO: 实现面包屑导航
        
        return breadcrumb;
    }

    @Override
    public Map<String, Object> validateMove(String nodeType, Long nodeId, Long targetParentId) {
        log.info("验证移动操作: {} {} 到 {}", nodeType, nodeId, targetParentId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);
        result.put("message", "");
        
        // TODO: 实现移动验证逻辑
        
        return result;
    }

    @Override
    public List<ProjectHierarchyTreeDTO> buildTree(List<ProjectHierarchyTreeDTO> nodes) {
        // TODO: 实现通用的树构建逻辑
        return nodes;
    }

    @Override
    public List<ProjectHierarchyTreeDTO> getAllChildren(String nodeType, Long nodeId) {
        // TODO: 实现获取所有子节点
        return new ArrayList<>();
    }

    @Override
    public List<ProjectHierarchyTreeDTO> getAllParents(String nodeType, Long nodeId) {
        // TODO: 实现获取所有父节点
        return new ArrayList<>();
    }

    // 私有辅助方法
    private ProjectHierarchyTreeDTO buildDepartmentTree(Department department) {
        ProjectHierarchyTreeDTO dto = convertDepartmentToDTO(department);
        
        // 获取子部门
        List<Department> childDepartments = departmentRepository.findByParentIdAndEnabledTrueOrderBySortOrderAsc(department.getId());
        List<ProjectHierarchyTreeDTO> children = new ArrayList<>();
        
        // 添加子部门
        children.addAll(childDepartments.stream().map(this::buildDepartmentTree).collect(Collectors.toList()));
        
        // 获取品类
        List<ProjectCategory> categories = categoryRepository.findByDepartmentIdAndEnabledTrueOrderBySortOrderAsc(department.getId());
        children.addAll(categories.stream().map(this::buildCategoryTree).collect(Collectors.toList()));
        
        dto.setChildren(children);
        dto.setChildrenCount(children.size());
        dto.setHasChildren(!children.isEmpty());
        
        return dto;
    }

    private ProjectHierarchyTreeDTO buildCategoryTree(ProjectCategory category) {
        ProjectHierarchyTreeDTO dto = convertCategoryToDTO(category);
        
        // 获取子品类
        List<ProjectSubcategory> subcategories = subcategoryRepository.findByCategoryIdAndEnabledTrueOrderBySortOrderAsc(category.getId());
        List<ProjectHierarchyTreeDTO> children = subcategories.stream()
                .map(this::buildSubcategoryTree)
                .collect(Collectors.toList());
        
        dto.setChildren(children);
        dto.setChildrenCount(children.size());
        dto.setHasChildren(!children.isEmpty());
        
        return dto;
    }

    private ProjectHierarchyTreeDTO buildSubcategoryTree(ProjectSubcategory subcategory) {
        ProjectHierarchyTreeDTO dto = convertSubcategoryToDTO(subcategory);
        
        // TODO: 获取项目列表
        List<ProjectHierarchyTreeDTO> children = new ArrayList<>();
        
        dto.setChildren(children);
        dto.setChildrenCount(children.size());
        dto.setHasChildren(!children.isEmpty());
        
        return dto;
    }

    private ProjectHierarchyTreeDTO convertDepartmentToDTO(Department department) {
        return ProjectHierarchyTreeDTO.fromDepartment(
                department.getId(),
                department.getName(),
                department.getCode(),
                department.getDescription(),
                department.getParentId(),
                department.getSortOrder(),
                department.getEnabled(),
                department.getManagerId(),
                null, // managerName - TODO: 从用户表获取
                department.getLevel(),
                department.getPath(),
                department.getCreatedAt(),
                department.getUpdatedAt()
        );
    }

    private ProjectHierarchyTreeDTO convertCategoryToDTO(ProjectCategory category) {
        return ProjectHierarchyTreeDTO.fromCategory(
                category.getId(),
                category.getName(),
                category.getCode(),
                category.getDescription(),
                category.getDepartmentId(),
                category.getSortOrder(),
                category.getEnabled(),
                category.getIcon(),
                category.getColor(),
                category.getManagerId(),
                null, // managerName - TODO: 从用户表获取
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    private ProjectHierarchyTreeDTO convertSubcategoryToDTO(ProjectSubcategory subcategory) {
        return ProjectHierarchyTreeDTO.fromSubcategory(
                subcategory.getId(),
                subcategory.getName(),
                subcategory.getCode(),
                subcategory.getDescription(),
                subcategory.getCategoryId(),
                subcategory.getSortOrder(),
                subcategory.getEnabled(),
                subcategory.getIcon(),
                subcategory.getColor(),
                subcategory.getManagerId(),
                null, // managerName - TODO: 从用户表获取
                subcategory.getCreatedAt(),
                subcategory.getUpdatedAt()
        );
    }
}
