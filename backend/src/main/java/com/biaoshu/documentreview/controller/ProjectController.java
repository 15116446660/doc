package com.biaoshu.documentreview.controller;

import com.biaoshu.documentreview.common.Result;
import com.biaoshu.documentreview.dto.ProjectDTO;
import com.biaoshu.documentreview.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "项目管理", description = "项目CRUD操作、状态管理、成员管理等")
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "创建项目", description = "创建新的项目")
    @PostMapping
    public Result<ProjectDTO> createProject(@Valid @RequestBody ProjectDTO.CreateRequest request) {
        log.info("创建项目请求: {}", request.getName());
        ProjectDTO project = projectService.createProject(request);
        return Result.success(project);
    }

    @Operation(summary = "更新项目", description = "更新项目信息")
    @PutMapping("/{id}")
    public Result<ProjectDTO> updateProject(
            @Parameter(description = "项目ID") @PathVariable Long id,
            @Valid @RequestBody ProjectDTO.UpdateRequest request) {
        log.info("更新项目请求: {}", id);
        ProjectDTO project = projectService.updateProject(id, request);
        return Result.success(project);
    }

    @Operation(summary = "删除项目", description = "删除指定项目")
    @DeleteMapping("/{id}")
    public Result<Void> deleteProject(@Parameter(description = "项目ID") @PathVariable Long id) {
        log.info("删除项目请求: {}", id);
        projectService.deleteProject(id);
        return Result.success();
    }

    @Operation(summary = "获取项目详情", description = "根据ID获取项目详细信息")
    @GetMapping("/{id}")
    public Result<ProjectDTO> getProject(@Parameter(description = "项目ID") @PathVariable Long id) {
        ProjectDTO project = projectService.getProject(id);
        return Result.success(project);
    }

    @Operation(summary = "分页查询项目", description = "根据条件分页查询项目列表")
    @GetMapping
    public Result<Page<ProjectDTO>> getProjects(@Valid ProjectDTO.QueryRequest request) {
        Page<ProjectDTO> projects = projectService.getProjects(request);
        return Result.success(projects);
    }

    @Operation(summary = "获取用户参与的项目", description = "获取指定用户参与的所有项目")
    @GetMapping("/user/{userId}")
    public Result<List<ProjectDTO>> getUserProjects(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        List<ProjectDTO> projects = projectService.getUserProjects(userId);
        return Result.success(projects);
    }

    @Operation(summary = "获取用户管理的项目", description = "获取指定用户管理的所有项目")
    @GetMapping("/managed/{userId}")
    public Result<List<ProjectDTO>> getManagedProjects(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        List<ProjectDTO> projects = projectService.getManagedProjects(userId);
        return Result.success(projects);
    }

    @Operation(summary = "获取即将到期的项目", description = "获取指定天数内即将到期的项目")
    @GetMapping("/near-deadline")
    public Result<List<ProjectDTO>> getProjectsNearDeadline(
            @Parameter(description = "天数", example = "7") @RequestParam(defaultValue = "7") int days) {
        List<ProjectDTO> projects = projectService.getProjectsNearDeadline(days);
        return Result.success(projects);
    }

    @Operation(summary = "获取项目状态统计", description = "获取各状态项目的数量统计")
    @GetMapping("/statistics/status")
    public Result<List<Object[]>> getProjectStatusStatistics() {
        List<Object[]> statistics = projectService.getProjectStatusStatistics();
        return Result.success(statistics);
    }
}
