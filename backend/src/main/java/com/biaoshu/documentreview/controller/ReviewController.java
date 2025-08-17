package com.biaoshu.documentreview.controller;

import com.biaoshu.documentreview.common.Result;
import com.biaoshu.documentreview.entity.ReviewProcess;
import com.biaoshu.documentreview.service.ReviewService;
import com.biaoshu.documentreview.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "审核流程管理", description = "用于提交审核、处理审核任务等")
public class ReviewController {

    private final ReviewService reviewService;

    @Data
    static class SubmitRequest {
        private Long documentId;
        private Long templateId;
    }

    @PostMapping("/submit")
    @Operation(summary = "提交审核", description = "为一个文档启动一个新的审核流程")
    public Result<ReviewProcess> submitForReview(@RequestBody SubmitRequest request) {
        Long initiatorId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("无法获取当前用户ID"));
        ReviewProcess process = reviewService.submitForReview(request.getDocumentId(), request.getTemplateId(), initiatorId);
        return Result.success(process);
    }

    @Data
    static class ApproveRequest {
        private String comment;
    }

    @PostMapping("/steps/{stepId}/approve")
    @Operation(summary = "批准审核步骤")
    public Result<Void> approveStep(@PathVariable Long stepId, @RequestBody ApproveRequest request) {
        Long reviewerId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("无法获取当前用户ID"));
        reviewService.approveStep(stepId, reviewerId, request.getComment());
        return Result.success();
    }

    @Data
    static class RejectRequest {
        private String comment;
    }

    @PostMapping("/steps/{stepId}/reject")
    @Operation(summary = "驳回审核步骤")
    public Result<Void> rejectStep(@PathVariable Long stepId, @RequestBody RejectRequest request) {
        Long reviewerId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("无法获取当前用户ID"));
        reviewService.rejectStep(stepId, reviewerId, request.getComment());
        return Result.success();
    }
}
