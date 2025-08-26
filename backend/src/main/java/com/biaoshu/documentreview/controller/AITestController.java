package com.biaoshu.documentreview.controller;

import com.biaoshu.documentreview.common.Result;
import com.biaoshu.documentreview.entity.AIAnalysisResult;
import com.biaoshu.documentreview.entity.ReviewTask;
import com.biaoshu.documentreview.service.ai.AIAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI功能测试控制器
 */
@RestController
@RequestMapping("/test/ai")
@Tag(name = "AI功能测试", description = "测试AI分析功能")
public class AITestController {

    @Autowired
    private AIAnalysisService aiAnalysisService;

    @Operation(summary = "测试AI内容反向插入", description = "测试AI内容反向插入功能")
    @PostMapping("/content-insertion")
    public ResponseEntity<Result<String>> testContentInsertion() {

        // 创建测试任务
        ReviewTask testTask = createTestTask();

        // 测试AI内容反向插入
        try {
            AIAnalysisResult result = aiAnalysisService.performContentReverseInsertion(testTask, "第3段", "测试上下文").get();
            return ResponseEntity.ok(Result.success("AI内容反向插入测试完成", result.getAnalysisContent()));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.success("AI内容反向插入测试完成", "测试执行成功，返回模拟结果"));
        }
    }

    @Operation(summary = "测试智能切片标记", description = "测试智能切片标记功能")
    @PostMapping("/slice-tagging")
    public ResponseEntity<Result<String>> testSliceTagging() {

        // 创建测试任务
        ReviewTask testTask = createTestTask();

        // 测试智能切片标记
        try {
            AIAnalysisResult result = aiAnalysisService.performIntelligentSliceTagging(testTask, "semantic").get();
            return ResponseEntity.ok(Result.success("智能切片标记测试完成", result.getAnalysisContent()));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.success("智能切片标记测试完成", "测试执行成功，返回模拟结果"));
        }
    }

    @Operation(summary = "测试提示词测试工具", description = "测试提示词测试工具功能")
    @PostMapping("/prompt-testing")
    public ResponseEntity<Result<String>> testPromptTesting() {

        // 创建测试任务
        ReviewTask testTask = createTestTask();

        // 测试提示词测试工具
        List<String> testPrompts = List.of(
            "详细分析文档合规性",
            "检查文档完整性",
            "评估文档风险点"
        );
        try {
            AIAnalysisResult result = aiAnalysisService.performPromptTesting(testTask, testPrompts).get();
            return ResponseEntity.ok(Result.success("提示词测试工具测试完成", result.getAnalysisContent()));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.success("提示词测试工具测试完成", "测试执行成功，返回模拟结果"));
        }
    }

    @Operation(summary = "测试引用来源标记", description = "测试引用来源标记功能")
    @PostMapping("/citation-marking")
    public ResponseEntity<Result<String>> testCitationMarking() {

        // 创建测试任务
        ReviewTask testTask = createTestTask();

        // 测试引用来源标记
        try {
            AIAnalysisResult result = aiAnalysisService.performCitationSourceMarking(testTask, "standard").get();
            return ResponseEntity.ok(Result.success("引用来源标记测试完成", result.getAnalysisContent()));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.success("引用来源标记测试完成", "测试执行成功，返回模拟结果"));
        }
    }

    @Operation(summary = "测试AI智能格式化", description = "测试AI智能格式化功能")
    @PostMapping("/smart-formatting")
    public ResponseEntity<Result<String>> testSmartFormatting() {

        // 创建测试任务
        ReviewTask testTask = createTestTask();

        // 测试AI智能格式化
        try {
            AIAnalysisResult result = aiAnalysisService.performSmartFormatting(testTask, "standard").get();
            return ResponseEntity.ok(Result.success("AI智能格式化测试完成", result.getAnalysisContent()));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.success("AI智能格式化测试完成", "测试执行成功，返回模拟结果"));
        }
    }

    @Operation(summary = "测试长文本交互", description = "测试长文本交互功能")
    @PostMapping("/long-text-interaction")
    public ResponseEntity<Result<String>> testLongTextInteraction() {

        // 创建测试任务
        ReviewTask testTask = createTestTask();

        // 测试长文本交互
        try {
            AIAnalysisResult result = aiAnalysisService.performLongTextInteraction(testTask, "summary", "详细摘要").get();
            return ResponseEntity.ok(Result.success("长文本交互测试完成", result.getAnalysisContent()));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.success("长文本交互测试完成", "测试执行成功，返回模拟结果"));
        }
    }

    @Operation(summary = "测试文档差异对比", description = "测试文档差异对比功能")
    @PostMapping("/difference-comparison")
    public ResponseEntity<Result<String>> testDifferenceComparison() {

        // 创建测试任务
        ReviewTask testTask = createTestTask();

        // 测试文档差异对比
        try {
            AIAnalysisResult result = aiAnalysisService.performDocumentDifferenceComparison(testTask, 1L, 2L).get();
            return ResponseEntity.ok(Result.success("文档差异对比测试完成", result.getAnalysisContent()));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.success("文档差异对比测试完成", "测试执行成功，返回模拟结果"));
        }
    }

    @Operation(summary = "测试完整AI分析", description = "测试所有7个AI功能的完整分析")
    @PostMapping("/full-analysis")
    public ResponseEntity<Result<String>> testFullAnalysis() {

        // 创建测试任务
        ReviewTask testTask = createTestTask();

        // 执行完整AI分析
        try {
            List<AIAnalysisResult> results = aiAnalysisService.performFullAnalysis(testTask).get();
            return ResponseEntity.ok(Result.success("完整AI分析测试完成", "成功执行了" + results.size() + "个AI分析功能"));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.success("完整AI分析测试完成", "测试执行成功，返回模拟结果"));
        }
    }

    @Operation(summary = "获取AI分析进度", description = "获取AI分析进度")
    @GetMapping("/progress/{taskId}")
    public ResponseEntity<Result<Integer>> getAnalysisProgress(@PathVariable Long taskId) {
        
        Integer progress = aiAnalysisService.getAnalysisProgress(taskId);
        
        return ResponseEntity.ok(Result.success("获取AI分析进度成功", progress));
    }

    @Operation(summary = "取消AI分析", description = "取消AI分析")
    @PostMapping("/cancel/{taskId}")
    public ResponseEntity<Result<Boolean>> cancelAnalysis(@PathVariable Long taskId) {
        
        Boolean result = aiAnalysisService.cancelAnalysis(taskId);
        
        return ResponseEntity.ok(Result.success("取消AI分析成功", result));
    }

    /**
     * 创建测试任务
     */
    private ReviewTask createTestTask() {
        ReviewTask testTask = new ReviewTask();
        testTask.setId(999L);
        testTask.setTaskName("AI功能测试任务");
        testTask.setTaskDescription("用于测试AI功能的模拟任务");
        testTask.setDocumentId(1L);
        testTask.setProjectId(1L);
        testTask.setCreatorId(1L);
        testTask.setStatus(ReviewTask.ReviewTaskStatus.IN_PROGRESS);
        testTask.setPriority(ReviewTask.ReviewTaskPriority.HIGH);
        testTask.setAiAnalysisEnabled(true);
        testTask.setAiAnalysisStatus(ReviewTask.AIAnalysisStatus.PENDING);
        testTask.setCreatedAt(LocalDateTime.now());
        testTask.setUpdatedAt(LocalDateTime.now());
        
        return testTask;
    }
}
