package com.biaoshu.documentreview.repository;

import com.biaoshu.documentreview.entity.AIAnalysisResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI分析结果Repository
 */
@Repository
public interface AIAnalysisResultRepository extends JpaRepository<AIAnalysisResult, Long> {

    /**
     * 根据评审任务ID查找AI分析结果
     */
    List<AIAnalysisResult> findByReviewTaskId(Long reviewTaskId);

    /**
     * 根据评审任务ID和分析类型查找AI分析结果
     */
    List<AIAnalysisResult> findByReviewTaskIdAndAnalysisType(Long reviewTaskId, 
                                                             AIAnalysisResult.AIAnalysisType analysisType);

    /**
     * 根据分析类型查找AI分析结果
     */
    Page<AIAnalysisResult> findByAnalysisType(AIAnalysisResult.AIAnalysisType analysisType, Pageable pageable);

    /**
     * 根据风险等级查找AI分析结果
     */
    Page<AIAnalysisResult> findByRiskLevel(AIAnalysisResult.RiskLevel riskLevel, Pageable pageable);

    /**
     * 查找高置信度的分析结果
     */
    @Query("SELECT ar FROM AIAnalysisResult ar WHERE ar.confidenceScore >= :minScore")
    List<AIAnalysisResult> findHighConfidenceResults(@Param("minScore") Double minScore);

    /**
     * 查找指定时间范围内的分析结果
     */
    @Query("SELECT ar FROM AIAnalysisResult ar " +
           "WHERE ar.processedAt BETWEEN :startTime AND :endTime")
    List<AIAnalysisResult> findByProcessedAtBetween(@Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 统计各分析类型的结果数量
     */
    @Query("SELECT ar.analysisType, COUNT(ar) FROM AIAnalysisResult ar GROUP BY ar.analysisType")
    List<Object[]> countByAnalysisType();

    /**
     * 统计各风险等级的结果数量
     */
    @Query("SELECT ar.riskLevel, COUNT(ar) FROM AIAnalysisResult ar GROUP BY ar.riskLevel")
    List<Object[]> countByRiskLevel();

    /**
     * 查找包含特定关键词的分析结果
     */
    @Query("SELECT ar FROM AIAnalysisResult ar " +
           "WHERE LOWER(ar.keywords) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(ar.analysisContent) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<AIAnalysisResult> findByKeyword(@Param("keyword") String keyword);

    /**
     * 查找评审任务的最新分析结果
     */
    @Query("SELECT ar FROM AIAnalysisResult ar " +
           "WHERE ar.reviewTaskId = :reviewTaskId " +
           "ORDER BY ar.processedAt DESC")
    List<AIAnalysisResult> findLatestByReviewTaskId(@Param("reviewTaskId") Long reviewTaskId);

    /**
     * 查找处理时间最长的分析结果
     */
    @Query("SELECT ar FROM AIAnalysisResult ar " +
           "WHERE ar.processingTimeMs IS NOT NULL " +
           "ORDER BY ar.processingTimeMs DESC")
    List<AIAnalysisResult> findByLongestProcessingTime(Pageable pageable);

    /**
     * 统计评审任务的分析结果数量
     */
    @Query("SELECT COUNT(ar) FROM AIAnalysisResult ar WHERE ar.reviewTaskId = :reviewTaskId")
    Long countByReviewTaskId(@Param("reviewTaskId") Long reviewTaskId);

    /**
     * 查找需要关注的高风险分析结果
     */
    @Query("SELECT ar FROM AIAnalysisResult ar " +
           "WHERE ar.riskLevel IN ('HIGH', 'CRITICAL') " +
           "AND ar.confidenceScore >= :minConfidence " +
           "ORDER BY ar.processedAt DESC")
    List<AIAnalysisResult> findHighRiskResults(@Param("minConfidence") Double minConfidence);

    /**
     * 删除指定评审任务的所有分析结果
     */
    void deleteByReviewTaskId(Long reviewTaskId);

    /**
     * 查找包含特定标签的分析结果
     */
    @Query("SELECT ar FROM AIAnalysisResult ar " +
           "WHERE LOWER(ar.tags) LIKE LOWER(CONCAT('%', :tag, '%'))")
    List<AIAnalysisResult> findByTag(@Param("tag") String tag);
}
