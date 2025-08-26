package com.biaoshu.documentreview.controller;

import com.biaoshu.documentreview.dto.ProjectHierarchyTreeDTO;
import com.biaoshu.documentreview.service.ProjectHierarchyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 项目层级管理控制器
 * 处理部门-品类-子品类-项目的四级层级结构
 */
@Slf4j
@RestController
@RequestMapping("/api/project-hierarchy")
@RequiredArgsConstructor
@Tag(name = "项目层级管理", description = "项目四级层级结构管理相关接口")
public class ProjectHierarchyController {

    private final ProjectHierarchyService hierarchyService;

    @GetMapping("/tree")
    @Operation(summary = "获取完整的项目层级树")
    public ResponseEntity<List<ProjectHierarchyTreeDTO>> getProjectHierarchyTree() {
        log.info("获取项目层级树");
        List<ProjectHierarchyTreeDTO> tree = hierarchyService.getProjectHierarchyTree();
        return ResponseEntity.ok(tree);
    }

    @GetMapping("/tree/department/{departmentId}")
    @Operation(summary = "获取指定部门的项目层级树")
    public ResponseEntity<ProjectHierarchyTreeDTO> getDepartmentHierarchyTree(
            @Parameter(description = "部门ID") @PathVariable Long departmentId) {
        log.info("获取部门{}的项目层级树", departmentId);
        ProjectHierarchyTreeDTO tree = hierarchyService.getDepartmentHierarchyTree(departmentId);
        return ResponseEntity.ok(tree);
    }

    @GetMapping("/tree/category/{categoryId}")
    @Operation(summary = "获取指定品类的层级树")
    public ResponseEntity<ProjectHierarchyTreeDTO> getCategoryHierarchyTree(
            @Parameter(description = "品类ID") @PathVariable Long categoryId) {
        log.info("获取品类{}的层级树", categoryId);
        ProjectHierarchyTreeDTO tree = hierarchyService.getCategoryHierarchyTree(categoryId);
        return ResponseEntity.ok(tree);
    }

    @GetMapping("/tree/subcategory/{subcategoryId}")
    @Operation(summary = "获取指定子品类的层级树")
    public ResponseEntity<ProjectHierarchyTreeDTO> getSubcategoryHierarchyTree(
            @Parameter(description = "子品类ID") @PathVariable Long subcategoryId) {
        log.info("获取子品类{}的层级树", subcategoryId);
        ProjectHierarchyTreeDTO tree = hierarchyService.getSubcategoryHierarchyTree(subcategoryId);
        return ResponseEntity.ok(tree);
    }

    @GetMapping("/search")
    @Operation(summary = "搜索项目层级结构")
    public ResponseEntity<List<ProjectHierarchyTreeDTO>> searchHierarchy(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "搜索类型：all, department, category, subcategory, project") 
            @RequestParam(defaultValue = "all") String type) {
        log.info("搜索项目层级结构: {} - {}", keyword, type);
        List<ProjectHierarchyTreeDTO> results = hierarchyService.searchHierarchy(keyword, type);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/move")
    @Operation(summary = "移动层级节点")
    public ResponseEntity<Void> moveNode(
            @RequestBody Map<String, Object> moveRequest) {
        log.info("移动层级节点: {}", moveRequest);
        String nodeType = (String) moveRequest.get("nodeType");
        Long nodeId = Long.valueOf(moveRequest.get("nodeId").toString());
        Long targetParentId = moveRequest.get("targetParentId") != null 
            ? Long.valueOf(moveRequest.get("targetParentId").toString()) : null;
        Integer newSortOrder = moveRequest.get("newSortOrder") != null 
            ? Integer.valueOf(moveRequest.get("newSortOrder").toString()) : null;
        
        hierarchyService.moveNode(nodeType, nodeId, targetParentId, newSortOrder);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/batch-sort")
    @Operation(summary = "批量更新排序")
    public ResponseEntity<Void> batchUpdateSortOrder(
            @RequestBody Map<String, Object> sortRequest) {
        log.info("批量更新排序: {}", sortRequest);
        String nodeType = (String) sortRequest.get("nodeType");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> sortItems = (List<Map<String, Object>>) sortRequest.get("items");
        
        hierarchyService.batchUpdateSortOrder(nodeType, sortItems);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取层级统计信息")
    public ResponseEntity<Map<String, Object>> getHierarchyStatistics() {
        log.info("获取层级统计信息");
        Map<String, Object> statistics = hierarchyService.getHierarchyStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/statistics/department/{departmentId}")
    @Operation(summary = "获取指定部门的统计信息")
    public ResponseEntity<Map<String, Object>> getDepartmentStatistics(
            @Parameter(description = "部门ID") @PathVariable Long departmentId) {
        log.info("获取部门{}的统计信息", departmentId);
        Map<String, Object> statistics = hierarchyService.getDepartmentStatistics(departmentId);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/breadcrumb/{nodeType}/{nodeId}")
    @Operation(summary = "获取节点的面包屑导航")
    public ResponseEntity<List<Map<String, Object>>> getBreadcrumb(
            @Parameter(description = "节点类型") @PathVariable String nodeType,
            @Parameter(description = "节点ID") @PathVariable Long nodeId) {
        log.info("获取节点面包屑: {} - {}", nodeType, nodeId);
        List<Map<String, Object>> breadcrumb = hierarchyService.getBreadcrumb(nodeType, nodeId);
        return ResponseEntity.ok(breadcrumb);
    }

    @PostMapping("/validate-move")
    @Operation(summary = "验证移动操作是否合法")
    public ResponseEntity<Map<String, Object>> validateMove(
            @RequestBody Map<String, Object> validateRequest) {
        log.info("验证移动操作: {}", validateRequest);
        String nodeType = (String) validateRequest.get("nodeType");
        Long nodeId = Long.valueOf(validateRequest.get("nodeId").toString());
        Long targetParentId = validateRequest.get("targetParentId") != null 
            ? Long.valueOf(validateRequest.get("targetParentId").toString()) : null;
        
        Map<String, Object> result = hierarchyService.validateMove(nodeType, nodeId, targetParentId);
        return ResponseEntity.ok(result);
    }
}
