package com.biaoshu.documentreview.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.biaoshu.documentreview.dto.*;
import com.biaoshu.documentreview.dto.request.CreateTaskRequest;
import com.biaoshu.documentreview.dto.request.TaskQueryRequest;
import com.biaoshu.documentreview.dto.request.UpdateTaskRequest;
import com.biaoshu.documentreview.dto.response.TaskDetailResponse;
import com.biaoshu.documentreview.dto.response.TaskStatisticsResponse;
import com.biaoshu.documentreview.entity.AIAnalysisResult;
import com.biaoshu.documentreview.entity.ReviewTask;
import com.biaoshu.documentreview.enums.TaskStatus;
import com.biaoshu.documentreview.service.ReviewTaskService;
import com.biaoshu.documentreview.service.ai.AIAnalysisService;
import com.biaoshu.documentreview.common.Result;
import com.biaoshu.common.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 评审任务控制器
 *
 * @author biaoshu
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/review-tasks")
@RequiredArgsConstructor
@Validated
@Tag(name = "评审任务管理", description = "评审任务的创建、查询、更新、删除等操作")
public class ReviewTaskController {

    private final ReviewTaskService reviewTaskService;
    private final AIAnalysisService aiAnalysisService;

    /**
     * 创建评审任务（新接口）
     */
    @PostMapping
    @Operation(summary = "创建评审任务", description = "创建新的评审任务")
    @PreAuthorize("hasAuthority('review:task:create')")
    public ResponseEntity<com.biaoshu.common.core.domain.Result<ReviewTask>> createTask(
            @Valid @RequestBody CreateTaskRequest request) {
        log.info("创建评审任务: {}", request.getTaskName());
        com.biaoshu.common.core.domain.Result<ReviewTask> result = reviewTaskService.createTask(request);
        return ResponseEntity.ok(result);
    }

    /**
     * 创建评审任务（兼容旧接口）
     */
    @PostMapping("/legacy")
    @Operation(summary = "创建评审任务（兼容接口）", description = "兼容旧版本的创建评审任务接口")
    public ResponseEntity<Result<ReviewTaskDTO>> createReviewTask(
            @Valid @RequestBody ReviewTaskCreateDTO createDTO,
            @Parameter(description = "创建者用户ID") @RequestParam(defaultValue = "1") Long creatorId) {

        ReviewTaskDTO result = reviewTaskService.createReviewTask(createDTO, creatorId);
        return ResponseEntity.ok(Result.success("评审任务创建成功", result));
    }

    /**
     * 更新评审任务（新接口）
     */
    @PutMapping("/{taskId}")
    @Operation(summary = "更新评审任务", description = "更新指定的评审任务")
    @PreAuthorize("hasAuthority('review:task:update')")
    public ResponseEntity<com.biaoshu.common.core.domain.Result<ReviewTask>> updateTask(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request) {
        log.info("更新评审任务: taskId={}", taskId);
        com.biaoshu.common.core.domain.Result<ReviewTask> result = reviewTaskService.updateTask(taskId, request);
        return ResponseEntity.ok(result);
    }

    /**
     * 更新评审任务（兼容旧接口）
     */
    @PutMapping("/{taskId}/legacy")
    @Operation(summary = "更新评审任务（兼容接口）", description = "兼容旧版本的更新评审任务接口")
    public ResponseEntity<Result<ReviewTaskDTO>> updateReviewTask(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @Valid @RequestBody ReviewTaskUpdateDTO updateDTO,
            @Parameter(description = "操作用户ID") @RequestParam(defaultValue = "1") Long userId) {

        ReviewTaskDTO result = reviewTaskService.updateReviewTask(taskId, updateDTO, userId);
        return ResponseEntity.ok(Result.success("评审任务更新成功", result));
    }

    /**
     * 删除评审任务
     */
    @DeleteMapping("/{taskId}")
    @Operation(summary = "删除评审任务", description = "删除指定的评审任务")
    @PreAuthorize("hasAuthority('review:task:delete')")
    public ResponseEntity<com.biaoshu.common.core.domain.Result<Void>> deleteTask(
            @Parameter(description = "任务ID") @PathVariable Long taskId) {
        log.info("删除评审任务: taskId={}", taskId);
        com.biaoshu.common.core.domain.Result<Void> result = reviewTaskService.deleteTask(taskId);
        return ResponseEntity.ok(result);
    }

    /**
     * 删除评审任务（兼容旧接口）
     */
    @DeleteMapping("/{taskId}/legacy")
    @Operation(summary = "删除评审任务（兼容接口）", description = "兼容旧版本的删除评审任务接口")
    public ResponseEntity<Result<Void>> deleteReviewTask(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @Parameter(description = "操作用户ID") @RequestParam(defaultValue = "1") Long userId) {

        reviewTaskService.deleteReviewTask(taskId, userId);
        return ResponseEntity.ok(Result.success("评审任务删除成功", null));
    }

    @Operation(summary = "获取评审任务详情", description = "根据ID获取评审任务详细信息")
    @GetMapping("/{taskId}")
    public ResponseEntity<Result<ReviewTaskDTO>> getReviewTask(
            @Parameter(description = "任务ID") @PathVariable Long taskId) {
        
        ReviewTaskDTO result = reviewTaskService.getReviewTaskById(taskId);
        return ResponseEntity.ok(Result.success("获取评审任务成功", result));
    }

    @Operation(summary = "分页查询评审任务", description = "根据条件分页查询评审任务")
    @GetMapping
    public ResponseEntity<Result<Page<ReviewTaskDTO>>> getReviewTasks(
            @Parameter(description = "任务名称") @RequestParam(required = false) String taskName,
            @Parameter(description = "项目ID") @RequestParam(required = false) Long projectId,
            @Parameter(description = "文档ID") @RequestParam(required = false) Long documentId,
            @Parameter(description = "创建者ID") @RequestParam(required = false) Long creatorId,
            @Parameter(description = "分配人ID") @RequestParam(required = false) Long assigneeId,
            @Parameter(description = "任务状态") @RequestParam(required = false) String status,
            @Parameter(description = "优先级") @RequestParam(required = false) String priority,
            @Parameter(description = "AI分析状态") @RequestParam(required = false) String aiAnalysisStatus,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir) {
        
        // 构建查询条件
        ReviewTaskQueryDTO queryDTO = new ReviewTaskQueryDTO();
        queryDTO.setTaskName(taskName);
        queryDTO.setProjectId(projectId);
        queryDTO.setDocumentId(documentId);
        queryDTO.setCreatorId(creatorId);
        queryDTO.setAssigneeId(assigneeId);
        queryDTO.setKeyword(keyword);
        
        // 构建分页参数
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ReviewTaskDTO> result = reviewTaskService.getReviewTasks(queryDTO, pageable);
        return ResponseEntity.ok(Result.success("查询评审任务成功", result));
    }

    @Operation(summary = "获取用户相关任务", description = "获取用户创建或分配的所有任务")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Result<Page<ReviewTaskDTO>>> getUserRelatedTasks(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ReviewTaskDTO> result = reviewTaskService.getUserRelatedTasks(userId, pageable);
        return ResponseEntity.ok(Result.success("获取用户相关任务成功", result));
    }

    @Operation(summary = "获取分配给用户的任务", description = "获取分配给指定用户的任务")
    @GetMapping("/assigned/{userId}")
    public ResponseEntity<Result<Page<ReviewTaskDTO>>> getAssignedTasks(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ReviewTaskDTO> result = reviewTaskService.getAssignedTasks(userId, pageable);
        return ResponseEntity.ok(Result.success("获取分配任务成功", result));
    }

    @Operation(summary = "启动评审任务", description = "启动指定的评审任务")
    @PostMapping("/{taskId}/start")
    public ResponseEntity<Result<ReviewTaskDTO>> startReviewTask(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @Parameter(description = "操作用户ID") @RequestParam(defaultValue = "1") Long userId) {
        
        ReviewTaskDTO result = reviewTaskService.startReviewTask(taskId, userId);
        return ResponseEntity.ok(Result.success("评审任务启动成功", result));
    }

    @Operation(summary = "完成评审任务", description = "完成指定的评审任务")
    @PostMapping("/{taskId}/complete")
    public ResponseEntity<Result<ReviewTaskDTO>> completeReviewTask(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @Parameter(description = "操作用户ID") @RequestParam(defaultValue = "1") Long userId,
            @Parameter(description = "完成备注") @RequestParam(required = false) String completionNotes) {
        
        ReviewTaskDTO result = reviewTaskService.completeReviewTask(taskId, userId, completionNotes);
        return ResponseEntity.ok(Result.success("评审任务完成成功", result));
    }

    @Operation(summary = "取消评审任务", description = "取消指定的评审任务")
    @PostMapping("/{taskId}/cancel")
    public ResponseEntity<Result<ReviewTaskDTO>> cancelReviewTask(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @Parameter(description = "操作用户ID") @RequestParam(defaultValue = "1") Long userId,
            @Parameter(description = "取消原因") @RequestParam(required = false) String reason) {
        
        ReviewTaskDTO result = reviewTaskService.cancelReviewTask(taskId, userId, reason);
        return ResponseEntity.ok(Result.success("评审任务取消成功", result));
    }

    @Operation(summary = "分配评审任务", description = "将评审任务分配给指定用户")
    @PostMapping("/{taskId}/assign")
    public ResponseEntity<Result<Void>> assignReviewTask(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @Parameter(description = "被分配人ID") @RequestParam Long assigneeId,
            @Parameter(description = "分配人ID") @RequestParam(defaultValue = "1") Long assignerId,
            @Parameter(description = "评审角色") @RequestParam String role,
            @Parameter(description = "分配备注") @RequestParam(required = false) String notes) {
        
        reviewTaskService.assignReviewTask(taskId, assigneeId, assignerId, role, notes);
        return ResponseEntity.ok(Result.success("评审任务分配成功", null));
    }

    // AI分析相关接口
    @Operation(summary = "启动AI分析", description = "启动评审任务的AI分析")
    @PostMapping("/{taskId}/ai-analysis/start")
    public ResponseEntity<Result<Boolean>> startAIAnalysis(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @Parameter(description = "操作用户ID") @RequestParam(defaultValue = "1") Long userId) {
        
        Boolean result = reviewTaskService.startAIAnalysis(taskId, userId);
        return ResponseEntity.ok(Result.success("AI分析启动成功", result));
    }

    @Operation(summary = "停止AI分析", description = "停止评审任务的AI分析")
    @PostMapping("/{taskId}/ai-analysis/stop")
    public ResponseEntity<Result<Boolean>> stopAIAnalysis(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @Parameter(description = "操作用户ID") @RequestParam(defaultValue = "1") Long userId) {
        
        Boolean result = reviewTaskService.stopAIAnalysis(taskId, userId);
        return ResponseEntity.ok(Result.success("AI分析停止成功", result));
    }

    @Operation(summary = "获取AI分析进度", description = "获取评审任务的AI分析进度")
    @GetMapping("/{taskId}/ai-analysis/progress")
    public ResponseEntity<Result<Integer>> getAIAnalysisProgress(
            @Parameter(description = "任务ID") @PathVariable Long taskId) {
        
        Integer progress = reviewTaskService.getAIAnalysisProgress(taskId);
        return ResponseEntity.ok(Result.success("获取AI分析进度成功", progress));
    }

    @Operation(summary = "获取AI分析结果", description = "获取评审任务的AI分析结果")
    @GetMapping("/{taskId}/ai-analysis/results")
    public ResponseEntity<Result<List<AIAnalysisResult>>> getAIAnalysisResults(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @Parameter(description = "分析类型") @RequestParam(required = false) String analysisType) {
        
        List<AIAnalysisResult> results;
        if (analysisType != null) {
            AIAnalysisResult.AIAnalysisType type = AIAnalysisResult.AIAnalysisType.valueOf(analysisType);
            results = aiAnalysisService.getAnalysisResultsByType(taskId, type);
        } else {
            results = aiAnalysisService.getAnalysisResults(taskId);
        }
        
        return ResponseEntity.ok(Result.success("获取AI分析结果成功", results));
    }

    @Operation(summary = "获取即将到期的任务", description = "获取即将到期的评审任务")
    @GetMapping("/due-soon")
    public ResponseEntity<Result<List<ReviewTaskDTO>>> getTasksDueSoon(
            @Parameter(description = "天数") @RequestParam(defaultValue = "7") Integer days) {
        
        List<ReviewTaskDTO> result = reviewTaskService.getTasksDueSoon(days);
        return ResponseEntity.ok(Result.success("获取即将到期任务成功", result));
    }

    @Operation(summary = "获取已过期的任务", description = "获取已过期的评审任务")
    @GetMapping("/overdue")
    public ResponseEntity<Result<List<ReviewTaskDTO>>> getOverdueTasks() {
        
        List<ReviewTaskDTO> result = reviewTaskService.getOverdueTasks();
        return ResponseEntity.ok(Result.success("获取已过期任务成功", result));
    }

    @Operation(summary = "获取用户任务统计", description = "获取用户的任务统计信息")
    @GetMapping("/statistics/user/{userId}")
    public ResponseEntity<Result<ReviewTaskService.ReviewTaskStatistics>> getUserTaskStatistics(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        
        ReviewTaskService.ReviewTaskStatistics result = reviewTaskService.getUserTaskStatistics(userId);
        return ResponseEntity.ok(Result.success("获取用户任务统计成功", result));
    }

    @Operation(summary = "获取项目任务统计", description = "获取项目的任务统计信息")
    @GetMapping("/statistics/project/{projectId}")
    public ResponseEntity<Result<ReviewTaskService.ReviewTaskStatistics>> getProjectTaskStatistics(
            @Parameter(description = "项目ID") @PathVariable Long projectId) {

        ReviewTaskService.ReviewTaskStatistics result = reviewTaskService.getProjectTaskStatistics(projectId);
        return ResponseEntity.ok(Result.success("获取项目任务统计成功", result));
    }

    // ========== 新增API接口 ==========

    /**
     * 获取任务详情（新接口）
     */
    @GetMapping("/{taskId}/detail")
    @Operation(summary = "获取任务详情", description = "根据ID获取评审任务的详细信息")
    @PreAuthorize("hasAuthority('review:task:view')")
    public ResponseEntity<com.biaoshu.common.core.domain.Result<TaskDetailResponse>> getTaskDetail(
            @Parameter(description = "任务ID") @PathVariable Long taskId) {
        com.biaoshu.common.core.domain.Result<TaskDetailResponse> result = reviewTaskService.getTaskDetail(taskId);
        return ResponseEntity.ok(result);
    }

    /**
     * 分页查询评审任务（新接口）
     */
    @GetMapping("/search")
    @Operation(summary = "分页查询评审任务", description = "根据条件分页查询评审任务列表")
    @PreAuthorize("hasAuthority('review:task:list')")
    public ResponseEntity<com.biaoshu.common.core.domain.Result<IPage<ReviewTask>>> getTasks(
            @Valid TaskQueryRequest request) {
        com.biaoshu.common.core.domain.Result<IPage<ReviewTask>> result = reviewTaskService.getTasks(request);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取用户的评审任务
     */
    @GetMapping("/my-tasks")
    @Operation(summary = "获取我的评审任务", description = "获取当前用户相关的评审任务")
    public ResponseEntity<com.biaoshu.common.core.domain.Result<List<ReviewTask>>> getMyTasks(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "20") Integer limit) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        com.biaoshu.common.core.domain.Result<List<ReviewTask>> result = reviewTaskService.getUserTasks(currentUserId, limit);
        return ResponseEntity.ok(result);
    }

    /**
     * 更新任务状态
     */
    @PutMapping("/{taskId}/status")
    @Operation(summary = "更新任务状态", description = "更新评审任务的状态")
    @PreAuthorize("hasAuthority('review:task:status')")
    public ResponseEntity<com.biaoshu.common.core.domain.Result<ReviewTask>> updateTaskStatus(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @Parameter(description = "新状态") @RequestParam TaskStatus status,
            @Parameter(description = "状态变更原因") @RequestParam(required = false) String reason) {
        com.biaoshu.common.core.domain.Result<ReviewTask> result = reviewTaskService.updateTaskStatus(taskId, status, reason);
        return ResponseEntity.ok(result);
    }

    /**
     * 启动任务
     */
    @PostMapping("/{taskId}/start")
    @Operation(summary = "启动任务", description = "启动评审任务")
    @PreAuthorize("hasAuthority('review:task:start')")
    public ResponseEntity<com.biaoshu.common.core.domain.Result<ReviewTask>> startTask(
            @Parameter(description = "任务ID") @PathVariable Long taskId) {
        com.biaoshu.common.core.domain.Result<ReviewTask> result = reviewTaskService.startTask(taskId);
        return ResponseEntity.ok(result);
    }

    /**
     * 完成任务
     */
    @PostMapping("/{taskId}/complete")
    @Operation(summary = "完成任务", description = "完成评审任务")
    @PreAuthorize("hasAuthority('review:task:complete')")
    public ResponseEntity<com.biaoshu.common.core.domain.Result<ReviewTask>> completeTask(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @Parameter(description = "评审结论") @RequestParam String conclusion,
            @Parameter(description = "结论原因") @RequestParam(required = false) String reason) {
        com.biaoshu.common.core.domain.Result<ReviewTask> result = reviewTaskService.completeTask(taskId, conclusion, reason);
        return ResponseEntity.ok(result);
    }

    /**
     * 取消任务
     */
    @PostMapping("/{taskId}/cancel")
    @Operation(summary = "取消任务", description = "取消评审任务")
    @PreAuthorize("hasAuthority('review:task:cancel')")
    public ResponseEntity<com.biaoshu.common.core.domain.Result<ReviewTask>> cancelTask(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @Parameter(description = "取消原因") @RequestParam String reason) {
        com.biaoshu.common.core.domain.Result<ReviewTask> result = reviewTaskService.cancelTask(taskId, reason);
        return ResponseEntity.ok(result);
    }

    /**
     * 分派专家
     */
    @PostMapping("/{taskId}/assign-experts")
    @Operation(summary = "分派专家", description = "为评审任务分派专家")
    @PreAuthorize("hasAuthority('review:task:assign')")
    public ResponseEntity<com.biaoshu.common.core.domain.Result<Void>> assignExperts(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @Parameter(description = "专家ID列表") @RequestBody @NotNull List<Long> expertIds) {
        com.biaoshu.common.core.domain.Result<Void> result = reviewTaskService.assignExperts(taskId, expertIds);
        return ResponseEntity.ok(result);
    }

    /**
     * 自动分派专家
     */
    @PostMapping("/{taskId}/auto-assign")
    @Operation(summary = "自动分派专家", description = "自动为评审任务分派专家")
    @PreAuthorize("hasAuthority('review:task:assign')")
    public ResponseEntity<com.biaoshu.common.core.domain.Result<Void>> autoAssignExperts(
            @Parameter(description = "任务ID") @PathVariable Long taskId) {
        com.biaoshu.common.core.domain.Result<Void> result = reviewTaskService.autoAssignExperts(taskId);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取任务统计信息
     */
    @GetMapping("/statistics/new")
    @Operation(summary = "获取任务统计信息", description = "获取评审任务的统计信息")
    @PreAuthorize("hasAuthority('review:task:statistics')")
    public ResponseEntity<com.biaoshu.common.core.domain.Result<TaskStatisticsResponse>> getTaskStatistics(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "项目ID") @RequestParam(required = false) Long projectId) {
        com.biaoshu.common.core.domain.Result<TaskStatisticsResponse> result = reviewTaskService.getTaskStatistics(userId, projectId);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取超时任务
     */
    @GetMapping("/overdue/new")
    @Operation(summary = "获取超时任务", description = "获取已超时的评审任务列表")
    @PreAuthorize("hasAuthority('review:task:list')")
    public ResponseEntity<com.biaoshu.common.core.domain.Result<List<ReviewTask>>> getOverdueTasks() {
        com.biaoshu.common.core.domain.Result<List<ReviewTask>> result = reviewTaskService.getOverdueTasks();
        return ResponseEntity.ok(result);
    }

    /**
     * 获取即将超时的任务
     */
    @GetMapping("/due-soon/new")
    @Operation(summary = "获取即将超时的任务", description = "获取即将超时的评审任务列表")
    @PreAuthorize("hasAuthority('review:task:list')")
    public ResponseEntity<com.biaoshu.common.core.domain.Result<List<ReviewTask>>> getTasksDueSoon(
            @Parameter(description = "小时数") @RequestParam(defaultValue = "24") Integer hours) {
        com.biaoshu.common.core.domain.Result<List<ReviewTask>> result = reviewTaskService.getTasksDueSoon(hours);
        return ResponseEntity.ok(result);
    }

    /**
     * 批量更新任务状态
     */
    @PutMapping("/batch/status")
    @Operation(summary = "批量更新任务状态", description = "批量更新多个评审任务的状态")
    @PreAuthorize("hasAuthority('review:task:batch')")
    public ResponseEntity<com.biaoshu.common.core.domain.Result<Void>> batchUpdateTaskStatus(
            @Parameter(description = "任务ID列表") @RequestBody @NotNull List<Long> taskIds,
            @Parameter(description = "新状态") @RequestParam TaskStatus status) {
        com.biaoshu.common.core.domain.Result<Void> result = reviewTaskService.batchUpdateTaskStatus(taskIds, status);
        return ResponseEntity.ok(result);
    }
}
