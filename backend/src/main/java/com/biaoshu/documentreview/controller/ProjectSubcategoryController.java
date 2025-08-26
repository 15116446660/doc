package com.biaoshu.documentreview.controller;

import com.biaoshu.documentreview.entity.ProjectSubcategory;
import com.biaoshu.documentreview.service.ProjectSubcategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 项目子品类管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/project-subcategories")
@RequiredArgsConstructor
@Tag(name = "项目子品类管理", description = "项目子品类管理相关接口")
public class ProjectSubcategoryController {

    private final ProjectSubcategoryService subcategoryService;

    @PostMapping
    @Operation(summary = "创建子品类")
    public ResponseEntity<ProjectSubcategory> createSubcategory(@Valid @RequestBody ProjectSubcategory subcategory) {
        log.info("创建子品类请求: {}", subcategory.getName());
        ProjectSubcategory created = subcategoryService.createSubcategory(subcategory);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新子品类")
    public ResponseEntity<ProjectSubcategory> updateSubcategory(
            @Parameter(description = "子品类ID") @PathVariable Long id,
            @Valid @RequestBody ProjectSubcategory subcategory) {
        log.info("更新子品类请求: {}", id);
        ProjectSubcategory updated = subcategoryService.updateSubcategory(id, subcategory);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除子品类")
    public ResponseEntity<Void> deleteSubcategory(@Parameter(description = "子品类ID") @PathVariable Long id) {
        log.info("删除子品类请求: {}", id);
        subcategoryService.deleteSubcategory(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询子品类")
    public ResponseEntity<ProjectSubcategory> getSubcategory(@Parameter(description = "子品类ID") @PathVariable Long id) {
        return subcategoryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "分页查询子品类列表")
    public ResponseEntity<Page<ProjectSubcategory>> getSubcategories(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProjectSubcategory> subcategories = subcategoryService.findAll(pageable);
        return ResponseEntity.ok(subcategories);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "根据品类ID查询子品类列表")
    public ResponseEntity<List<ProjectSubcategory>> getSubcategoriesByCategory(
            @Parameter(description = "品类ID") @PathVariable Long categoryId) {
        List<ProjectSubcategory> subcategories = subcategoryService.findByCategoryId(categoryId);
        return ResponseEntity.ok(subcategories);
    }

    @GetMapping("/search")
    @Operation(summary = "根据名称搜索子品类")
    public ResponseEntity<List<ProjectSubcategory>> searchSubcategories(
            @Parameter(description = "搜索关键词") @RequestParam String name) {
        List<ProjectSubcategory> subcategories = subcategoryService.searchByName(name);
        return ResponseEntity.ok(subcategories);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "根据编码查询子品类")
    public ResponseEntity<ProjectSubcategory> getSubcategoryByCode(
            @Parameter(description = "子品类编码") @PathVariable String code) {
        return subcategoryService.findByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/check-code")
    @Operation(summary = "检查子品类编码是否存在")
    public ResponseEntity<Map<String, Boolean>> checkCode(
            @Parameter(description = "子品类编码") @RequestParam String code,
            @Parameter(description = "排除的子品类ID") @RequestParam(required = false) Long excludeId) {
        boolean exists = excludeId != null 
            ? subcategoryService.existsByCodeAndIdNot(code, excludeId)
            : subcategoryService.existsByCode(code);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @PutMapping("/{id}/sort")
    @Operation(summary = "更新子品类排序")
    public ResponseEntity<Void> updateSortOrder(
            @Parameter(description = "子品类ID") @PathVariable Long id,
            @Parameter(description = "排序号") @RequestParam Integer sortOrder) {
        subcategoryService.updateSortOrder(id, sortOrder);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/toggle")
    @Operation(summary = "启用/禁用子品类")
    public ResponseEntity<ProjectSubcategory> toggleEnabled(
            @Parameter(description = "子品类ID") @PathVariable Long id,
            @Parameter(description = "是否启用") @RequestParam Boolean enabled) {
        ProjectSubcategory updated = subcategoryService.toggleEnabled(id, enabled);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/category/{categoryId}/count")
    @Operation(summary = "统计品类下的子品类数量")
    public ResponseEntity<Map<String, Long>> countByCategory(
            @Parameter(description = "品类ID") @PathVariable Long categoryId) {
        long count = subcategoryService.countByCategoryId(categoryId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/categories")
    @Operation(summary = "根据多个品类ID查询子品类")
    public ResponseEntity<List<ProjectSubcategory>> getSubcategoriesByCategories(
            @Parameter(description = "品类ID列表") @RequestParam List<Long> categoryIds) {
        List<ProjectSubcategory> subcategories = subcategoryService.findByCategoryIds(categoryIds);
        return ResponseEntity.ok(subcategories);
    }

    @GetMapping("/category/{categoryId}/with-projects")
    @Operation(summary = "查询子品类及其项目")
    public ResponseEntity<List<ProjectSubcategory>> getSubcategoriesWithProjects(
            @Parameter(description = "品类ID") @PathVariable Long categoryId) {
        List<ProjectSubcategory> subcategories = subcategoryService.findSubcategoriesWithProjects(categoryId);
        return ResponseEntity.ok(subcategories);
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "根据部门ID查询所有子品类")
    public ResponseEntity<List<ProjectSubcategory>> getSubcategoriesByDepartment(
            @Parameter(description = "部门ID") @PathVariable Long departmentId) {
        List<ProjectSubcategory> subcategories = subcategoryService.findByDepartmentId(departmentId);
        return ResponseEntity.ok(subcategories);
    }
}
