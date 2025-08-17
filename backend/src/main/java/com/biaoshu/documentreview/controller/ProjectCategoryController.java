package com.biaoshu.documentreview.controller;

import com.biaoshu.documentreview.common.Result;
import com.biaoshu.documentreview.dto.ProjectCategoryDTO;
import com.biaoshu.documentreview.entity.ProjectCategory;
import com.biaoshu.documentreview.service.ProjectCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project-categories")
@Tag(name = "项目分类管理", description = "用于管理项目层级结构，如部门、品类、子品类等")
public class ProjectCategoryController {

    @Autowired
    private ProjectCategoryService projectCategoryService;

    @Operation(summary = "创建项目分类")
    @PostMapping
    public Result<ProjectCategory> createCategory(@Valid @RequestBody ProjectCategoryDTO.Create createDTO) {
        ProjectCategory category = projectCategoryService.createCategory(createDTO);
        return Result.success(category);
    }

    @Operation(summary = "更新项目分类")
    @PutMapping("/{id}")
    public Result<ProjectCategory> updateCategory(@PathVariable Long id, @Valid @RequestBody ProjectCategoryDTO.Update updateDTO) {
        ProjectCategory category = projectCategoryService.updateCategory(id, updateDTO);
        return Result.success(category);
    }

    @Operation(summary = "删除项目分类")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        projectCategoryService.deleteCategory(id);
        return Result.success();
    }

    @Operation(summary = "获取单个项目分类详情")
    @GetMapping("/{id}")
    public Result<ProjectCategory> getCategoryById(@PathVariable Long id) {
        ProjectCategory category = projectCategoryService.getCategoryById(id);
        return Result.success(category);
    }

    @Operation(summary = "获取项目分类树形结构")
    @GetMapping("/tree")
    public Result<List<ProjectCategoryDTO.TreeNode>> getCategoryTree() {
        List<ProjectCategoryDTO.TreeNode> tree = projectCategoryService.getCategoryTree();
        return Result.success(tree);
    }
}
