package com.biaoshu.documentreview.controller;

import com.biaoshu.documentreview.common.Result;
import com.biaoshu.documentreview.dto.ProjectMemberDTO;
import com.biaoshu.documentreview.service.ProjectMemberService;
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
 * 项目成员管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/projects/{projectId}/members")
@RequiredArgsConstructor
@Tag(name = "项目成员管理", description = "项目成员的增删改查和权限管理")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    @Operation(summary = "添加项目成员", description = "向项目中添加新成员")
    @PostMapping
    public Result<ProjectMemberDTO> addProjectMember(
            @Parameter(description = "项目ID") @PathVariable Long projectId,
            @Valid @RequestBody ProjectMemberDTO.AddRequest request) {
        log.info("添加项目成员请求: 项目ID={}, 用户ID={}", projectId, request.getUserId());
        ProjectMemberDTO member = projectMemberService.addProjectMember(projectId, request);
        return Result.success(member);
    }

    @Operation(summary = "批量添加项目成员", description = "批量向项目中添加多个成员")
    @PostMapping("/batch")
    public Result<List<ProjectMemberDTO>> batchAddProjectMembers(
            @Parameter(description = "项目ID") @PathVariable Long projectId,
            @Valid @RequestBody ProjectMemberDTO.BatchAddRequest request) {
        log.info("批量添加项目成员请求: 项目ID={}, 成员数量={}", projectId, request.getMembers().size());
        List<ProjectMemberDTO> members = projectMemberService.batchAddProjectMembers(projectId, request);
        return Result.success(members);
    }

    @Operation(summary = "更新项目成员", description = "更新项目成员的角色和权限")
    @PutMapping("/{userId}")
    public Result<ProjectMemberDTO> updateProjectMember(
            @Parameter(description = "项目ID") @PathVariable Long projectId,
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Valid @RequestBody ProjectMemberDTO.UpdateRequest request) {
        log.info("更新项目成员请求: 项目ID={}, 用户ID={}", projectId, userId);
        ProjectMemberDTO member = projectMemberService.updateProjectMember(projectId, userId, request);
        return Result.success(member);
    }

    @Operation(summary = "移除项目成员", description = "从项目中移除指定成员")
    @DeleteMapping("/{userId}")
    public Result<Void> removeProjectMember(
            @Parameter(description = "项目ID") @PathVariable Long projectId,
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        log.info("移除项目成员请求: 项目ID={}, 用户ID={}", projectId, userId);
        projectMemberService.removeProjectMember(projectId, userId);
        return Result.success();
    }

    @Operation(summary = "批量移除项目成员", description = "批量从项目中移除多个成员")
    @DeleteMapping("/batch")
    public Result<Void> batchRemoveProjectMembers(
            @Parameter(description = "项目ID") @PathVariable Long projectId,
            @RequestBody List<Long> userIds) {
        log.info("批量移除项目成员请求: 项目ID={}, 用户数量={}", projectId, userIds.size());
        projectMemberService.batchRemoveProjectMembers(projectId, userIds);
        return Result.success();
    }

    @Operation(summary = "获取项目成员列表", description = "获取项目的所有成员")
    @GetMapping
    public Result<List<ProjectMemberDTO>> getProjectMembers(
            @Parameter(description = "项目ID") @PathVariable Long projectId,
            @Parameter(description = "是否只获取活跃成员") @RequestParam(required = false) Boolean isActive) {
        List<ProjectMemberDTO> members = projectMemberService.getProjectMembers(projectId, isActive);
        return Result.success(members);
    }

    @Operation(summary = "分页查询项目成员", description = "分页查询项目成员列表")
    @GetMapping("/page")
    public Result<Page<ProjectMemberDTO>> getProjectMembersPage(
            @Parameter(description = "项目ID") @PathVariable Long projectId,
            @Valid ProjectMemberDTO.QueryRequest request) {
        // 设置项目ID
        request.setProjectId(projectId);
        Page<ProjectMemberDTO> members = projectMemberService.getProjectMembers(request);
        return Result.success(members);
    }

    @Operation(summary = "获取项目成员统计", description = "获取项目成员的统计信息")
    @GetMapping("/statistics")
    public Result<ProjectMemberDTO.Statistics> getProjectMemberStatistics(
            @Parameter(description = "项目ID") @PathVariable Long projectId) {
        ProjectMemberDTO.Statistics statistics = projectMemberService.getProjectMemberStatistics(projectId);
        return Result.success(statistics);
    }
}
