package com.biaoshu.documentreview.controller;

import com.biaoshu.documentreview.entity.ProjectCategory;
import com.biaoshu.documentreview.service.ProjectCategoryService;
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
 * 项目品类管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/project-categories")
@RequiredArgsConstructor
@Tag(name = "项目品类管理", description = "项目品类管理相关接口")
public class ProjectCategoryController {

    private final ProjectCategoryService categoryService;

    @PostMapping
    @Operation(summary = "创建品类")
    public ResponseEntity<ProjectCategory> createCategory(@Valid @RequestBody ProjectCategory category) {
        log.info("创建品类请求: {}", category.getName());
        ProjectCategory created = categoryService.createCategory(category);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新品类")
    public ResponseEntity<ProjectCategory> updateCategory(
            @Parameter(description = "品类ID") @PathVariable Long id,
            @Valid @RequestBody ProjectCategory category) {
        log.info("更新品类请求: {}", id);
        ProjectCategory updated = categoryService.updateCategory(id, category);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除品类")
    public ResponseEntity<Void> deleteCategory(@Parameter(description = "品类ID") @PathVariable Long id) {
        log.info("删除品类请求: {}", id);
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询品类")
    public ResponseEntity<ProjectCategory> getCategory(@Parameter(description = "品类ID") @PathVariable Long id) {
        return categoryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "分页查询品类列表")
    public ResponseEntity<Page<ProjectCategory>> getCategories(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProjectCategory> categories = categoryService.findAll(pageable);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "根据部门ID查询品类列表")
    public ResponseEntity<List<ProjectCategory>> getCategoriesByDepartment(
            @Parameter(description = "部门ID") @PathVariable Long departmentId) {
        List<ProjectCategory> categories = categoryService.findByDepartmentId(departmentId);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/search")
    @Operation(summary = "根据名称搜索品类")
    public ResponseEntity<List<ProjectCategory>> searchCategories(
            @Parameter(description = "搜索关键词") @RequestParam String name) {
        List<ProjectCategory> categories = categoryService.searchByName(name);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "根据编码查询品类")
    public ResponseEntity<ProjectCategory> getCategoryByCode(
            @Parameter(description = "品类编码") @PathVariable String code) {
        return categoryService.findByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/check-code")
    @Operation(summary = "检查品类编码是否存在")
    public ResponseEntity<Map<String, Boolean>> checkCode(
            @Parameter(description = "品类编码") @RequestParam String code,
            @Parameter(description = "排除的品类ID") @RequestParam(required = false) Long excludeId) {
        boolean exists = excludeId != null 
            ? categoryService.existsByCodeAndIdNot(code, excludeId)
            : categoryService.existsByCode(code);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @PutMapping("/{id}/sort")
    @Operation(summary = "更新品类排序")
    public ResponseEntity<Void> updateSortOrder(
            @Parameter(description = "品类ID") @PathVariable Long id,
            @Parameter(description = "排序号") @RequestParam Integer sortOrder) {
        categoryService.updateSortOrder(id, sortOrder);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/toggle")
    @Operation(summary = "启用/禁用品类")
    public ResponseEntity<ProjectCategory> toggleEnabled(
            @Parameter(description = "品类ID") @PathVariable Long id,
            @Parameter(description = "是否启用") @RequestParam Boolean enabled) {
        ProjectCategory updated = categoryService.toggleEnabled(id, enabled);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/department/{departmentId}/count")
    @Operation(summary = "统计部门下的品类数量")
    public ResponseEntity<Map<String, Long>> countByDepartment(
            @Parameter(description = "部门ID") @PathVariable Long departmentId) {
        long count = categoryService.countByDepartmentId(departmentId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/departments")
    @Operation(summary = "根据多个部门ID查询品类")
    public ResponseEntity<List<ProjectCategory>> getCategoriesByDepartments(
            @Parameter(description = "部门ID列表") @RequestParam List<Long> departmentIds) {
        List<ProjectCategory> categories = categoryService.findByDepartmentIds(departmentIds);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/department/{departmentId}/with-subcategories")
    @Operation(summary = "查询品类及其子品类")
    public ResponseEntity<List<ProjectCategory>> getCategoriesWithSubcategories(
            @Parameter(description = "部门ID") @PathVariable Long departmentId) {
        List<ProjectCategory> categories = categoryService.findCategoriesWithSubcategories(departmentId);
        return ResponseEntity.ok(categories);
    }
}
