package com.biaoshu.documentreview.controller;

import com.biaoshu.documentreview.common.Result;
import com.biaoshu.documentreview.dto.ReviewIssueCreateDTO;
import com.biaoshu.documentreview.dto.ReviewIssueDTO;
import com.biaoshu.documentreview.service.ReviewIssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 评审问题控制器
 *
 * @author Jules
 * @since 2025-08-26
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/review-issues")
@RequiredArgsConstructor
@Validated
@Tag(name = "评审问题管理", description = "评审问题的创建、查询、更新等操作")
public class ReviewIssueController {

    private final ReviewIssueService reviewIssueService;

    @PostMapping
    @Operation(summary = "创建评审问题", description = "为评审任务创建新的问题")
    @PreAuthorize("hasAuthority('review:issue:create')")
    public ResponseEntity<Result<ReviewIssueDTO>> createIssue(
            @Valid @RequestBody ReviewIssueCreateDTO createDTO) {

        // Assuming SecurityUtils can get the current user's ID
        // Long currentUserId = SecurityUtils.getCurrentUserId();
        Long currentUserId = 1L; // Placeholder for now

        log.info("用户 {} 为任务 {} 创建问题: {}", currentUserId, createDTO.getReviewTaskId(), createDTO.getTitle());
        ReviewIssueDTO result = reviewIssueService.createIssue(createDTO, currentUserId);
        return ResponseEntity.ok(Result.success("问题创建成功", result));
    }

    @GetMapping("/task/{taskId}")
    @Operation(summary = "获取任务的问题列表", description = "根据评审任务ID获取所有关联的问题")
    @PreAuthorize("hasAuthority('review:issue:view')")
    public ResponseEntity<Result<List<ReviewIssueDTO>>> getIssuesByTask(
            @Parameter(description = "任务ID") @PathVariable Long taskId) {

        log.info("查询任务 {} 的问题列表", taskId);
        List<ReviewIssueDTO> results = reviewIssueService.getIssuesByTaskId(taskId);
        return ResponseEntity.ok(Result.success("查询问题列表成功", results));
    }

    @GetMapping("/{issueId}")
    @Operation(summary = "获取问题详情", description = "根据ID获取问题的详细信息")
    @PreAuthorize("hasAuthority('review:issue:view')")
    public ResponseEntity<Result<ReviewIssueDTO>> getIssueById(
            @Parameter(description = "问题ID") @PathVariable Long issueId) {

        log.info("查询问题详情: {}", issueId);
        ReviewIssueDTO result = reviewIssueService.getIssueById(issueId);
        return ResponseEntity.ok(Result.success("获取问题详情成功", result));
    }

    @DeleteMapping("/{issueId}")
    @Operation(summary = "删除问题", description = "删除指定的问题")
    @PreAuthorize("hasAuthority('review:issue:delete')")
    public ResponseEntity<Result<Void>> deleteIssue(
            @Parameter(description = "问题ID") @PathVariable Long issueId) {

        // Assuming SecurityUtils can get the current user's ID
        // Long currentUserId = SecurityUtils.getCurrentUserId();
        Long currentUserId = 1L; // Placeholder for now

        log.info("用户 {} 删除问题: {}", currentUserId, issueId);
        reviewIssueService.deleteIssue(issueId, currentUserId);
        return ResponseEntity.ok(Result.success("问题删除成功", null));
    }
}
