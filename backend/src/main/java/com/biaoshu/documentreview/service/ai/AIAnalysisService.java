package com.biaoshu.documentreview.service.ai;

import com.biaoshu.documentreview.entity.AIAnalysisResult;
import com.biaoshu.documentreview.entity.ReviewTask;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * AI分析服务接口
 * 提供7个核心AI功能的统一接口
 */
public interface AIAnalysisService {

    /**
     * 执行完整的AI分析流程
     * @param reviewTask 评审任务
     * @return 异步分析结果
     */
    CompletableFuture<List<AIAnalysisResult>> performFullAnalysis(ReviewTask reviewTask);

    /**
     * 1. AI内容反向插入
     * 基于文档内容和上下文，智能插入相关内容建议
     * @param reviewTask 评审任务
     * @param targetPosition 目标位置信息
     * @param context 上下文信息
     * @return 分析结果
     */
    CompletableFuture<AIAnalysisResult> performContentReverseInsertion(ReviewTask reviewTask, 
                                                                       String targetPosition, 
                                                                       String context);

    /**
     * 2. 智能切片标记
     * 将文档内容智能分割并标记重要片段
     * @param reviewTask 评审任务
     * @param sliceStrategy 切片策略
     * @return 分析结果
     */
    CompletableFuture<AIAnalysisResult> performIntelligentSliceTagging(ReviewTask reviewTask, 
                                                                       String sliceStrategy);

    /**
     * 3. 提示词测试工具
     * 测试和优化AI提示词的效果
     * @param reviewTask 评审任务
     * @param prompts 测试提示词列表
     * @return 分析结果
     */
    CompletableFuture<AIAnalysisResult> performPromptTesting(ReviewTask reviewTask, 
                                                             List<String> prompts);

    /**
     * 4. 引用来源标记
     * 自动识别和标记文档中的引用来源
     * @param reviewTask 评审任务
     * @param sourceDatabase 来源数据库
     * @return 分析结果
     */
    CompletableFuture<AIAnalysisResult> performCitationSourceMarking(ReviewTask reviewTask, 
                                                                     String sourceDatabase);

    /**
     * 5. AI智能格式化
     * 智能优化文档格式和结构
     * @param reviewTask 评审任务
     * @param formatRules 格式规则
     * @return 分析结果
     */
    CompletableFuture<AIAnalysisResult> performSmartFormatting(ReviewTask reviewTask, 
                                                               String formatRules);

    /**
     * 6. 长文本交互
     * 处理长文本的智能交互和分析
     * @param reviewTask 评审任务
     * @param interactionType 交互类型
     * @param query 查询内容
     * @return 分析结果
     */
    CompletableFuture<AIAnalysisResult> performLongTextInteraction(ReviewTask reviewTask, 
                                                                   String interactionType, 
                                                                   String query);

    /**
     * 7. 文档差异对比
     * 智能对比文档版本差异
     * @param reviewTask 评审任务
     * @param baseDocumentId 基准文档ID
     * @param compareDocumentId 对比文档ID
     * @return 分析结果
     */
    CompletableFuture<AIAnalysisResult> performDocumentDifferenceComparison(ReviewTask reviewTask, 
                                                                            Long baseDocumentId, 
                                                                            Long compareDocumentId);

    /**
     * 获取分析进度
     * @param reviewTaskId 评审任务ID
     * @return 进度百分比
     */
    Integer getAnalysisProgress(Long reviewTaskId);

    /**
     * 取消分析任务
     * @param reviewTaskId 评审任务ID
     * @return 是否成功取消
     */
    Boolean cancelAnalysis(Long reviewTaskId);

    /**
     * 获取分析结果
     * @param reviewTaskId 评审任务ID
     * @return 分析结果列表
     */
    List<AIAnalysisResult> getAnalysisResults(Long reviewTaskId);

    /**
     * 获取特定类型的分析结果
     * @param reviewTaskId 评审任务ID
     * @param analysisType 分析类型
     * @return 分析结果
     */
    List<AIAnalysisResult> getAnalysisResultsByType(Long reviewTaskId, 
                                                    AIAnalysisResult.AIAnalysisType analysisType);
}
