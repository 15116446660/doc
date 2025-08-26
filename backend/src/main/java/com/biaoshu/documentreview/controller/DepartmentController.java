package com.biaoshu.documentreview.controller;

import com.biaoshu.documentreview.entity.Department;
import com.biaoshu.documentreview.service.DepartmentService;
import com.biaoshu.documentreview.security.RequireHierarchyPermission;
import com.biaoshu.documentreview.enums.HierarchyPermission;
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
 * 部门管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Tag(name = "部门管理", description = "部门管理相关接口")
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @Operation(summary = "创建部门")
    @RequireHierarchyPermission(HierarchyPermission.DEPARTMENT_CREATE)
    public ResponseEntity<Department> createDepartment(@Valid @RequestBody Department department) {
        log.info("创建部门请求: {}", department.getName());
        Department created = departmentService.createDepartment(department);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新部门")
    @RequireHierarchyPermission(value = HierarchyPermission.DEPARTMENT_UPDATE,
                               checkDataPermission = true,
                               dataIdParam = "id",
                               nodeType = "department")
    public ResponseEntity<Department> updateDepartment(
            @Parameter(description = "部门ID") @PathVariable Long id,
            @Valid @RequestBody Department department) {
        log.info("更新部门请求: {}", id);
        Department updated = departmentService.updateDepartment(id, department);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除部门")
    @RequireHierarchyPermission(value = HierarchyPermission.DEPARTMENT_DELETE,
                               checkDataPermission = true,
                               dataIdParam = "id",
                               nodeType = "department")
    public ResponseEntity<Void> deleteDepartment(@Parameter(description = "部门ID") @PathVariable Long id) {
        log.info("删除部门请求: {}", id);
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询部门")
    @RequireHierarchyPermission(HierarchyPermission.DEPARTMENT_VIEW)
    public ResponseEntity<Department> getDepartment(@Parameter(description = "部门ID") @PathVariable Long id) {
        return departmentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "分页查询部门列表")
    public ResponseEntity<Page<Department>> getDepartments(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Department> departments = departmentService.findAll(pageable);
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/tree")
    @Operation(summary = "获取部门树形结构")
    public ResponseEntity<List<Department>> getDepartmentTree() {
        List<Department> tree = departmentService.getDepartmentTree();
        return ResponseEntity.ok(tree);
    }

    @GetMapping("/roots")
    @Operation(summary = "获取根部门列表")
    public ResponseEntity<List<Department>> getRootDepartments() {
        List<Department> roots = departmentService.findRootDepartments();
        return ResponseEntity.ok(roots);
    }

    @GetMapping("/children/{parentId}")
    @Operation(summary = "根据父部门ID查询子部门")
    public ResponseEntity<List<Department>> getChildDepartments(
            @Parameter(description = "父部门ID") @PathVariable Long parentId) {
        List<Department> children = departmentService.findByParentId(parentId);
        return ResponseEntity.ok(children);
    }

    @GetMapping("/search")
    @Operation(summary = "根据名称搜索部门")
    public ResponseEntity<List<Department>> searchDepartments(
            @Parameter(description = "搜索关键词") @RequestParam String name) {
        List<Department> departments = departmentService.searchByName(name);
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "根据编码查询部门")
    public ResponseEntity<Department> getDepartmentByCode(
            @Parameter(description = "部门编码") @PathVariable String code) {
        return departmentService.findByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/check-code")
    @Operation(summary = "检查部门编码是否存在")
    public ResponseEntity<Map<String, Boolean>> checkCode(
            @Parameter(description = "部门编码") @RequestParam String code,
            @Parameter(description = "排除的部门ID") @RequestParam(required = false) Long excludeId) {
        boolean exists = excludeId != null 
            ? departmentService.existsByCodeAndIdNot(code, excludeId)
            : departmentService.existsByCode(code);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @PutMapping("/{id}/move")
    @Operation(summary = "移动部门")
    public ResponseEntity<Department> moveDepartment(
            @Parameter(description = "部门ID") @PathVariable Long id,
            @Parameter(description = "新父部门ID") @RequestParam(required = false) Long newParentId) {
        Department moved = departmentService.moveDepartment(id, newParentId);
        return ResponseEntity.ok(moved);
    }

    @PutMapping("/{id}/sort")
    @Operation(summary = "更新部门排序")
    public ResponseEntity<Void> updateSortOrder(
            @Parameter(description = "部门ID") @PathVariable Long id,
            @Parameter(description = "排序号") @RequestParam Integer sortOrder) {
        departmentService.updateSortOrder(id, sortOrder);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/toggle")
    @Operation(summary = "启用/禁用部门")
    public ResponseEntity<Department> toggleEnabled(
            @Parameter(description = "部门ID") @PathVariable Long id,
            @Parameter(description = "是否启用") @RequestParam Boolean enabled) {
        Department updated = departmentService.toggleEnabled(id, enabled);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}/path")
    @Operation(summary = "获取部门路径")
    public ResponseEntity<Map<String, String>> getDepartmentPath(
            @Parameter(description = "部门ID") @PathVariable Long id) {
        String path = departmentService.getDepartmentPath(id);
        return ResponseEntity.ok(Map.of("path", path));
    }

    @GetMapping("/{id}/children/all")
    @Operation(summary = "获取部门的所有子部门（递归）")
    public ResponseEntity<List<Department>> getAllChildren(
            @Parameter(description = "部门ID") @PathVariable Long id) {
        List<Department> children = departmentService.findAllChildren(id);
        return ResponseEntity.ok(children);
    }

    @GetMapping("/{id}/children/count")
    @Operation(summary = "统计部门下的子部门数量")
    public ResponseEntity<Map<String, Long>> countChildren(
            @Parameter(description = "部门ID") @PathVariable Long id) {
        long count = departmentService.countChildren(id);
        return ResponseEntity.ok(Map.of("count", count));
    }
}
