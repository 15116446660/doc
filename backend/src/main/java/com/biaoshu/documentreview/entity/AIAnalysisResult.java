package com.biaoshu.documentreview.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * AI分析结果实体
 */
@Entity
@Table(name = "ai_analysis_results")
public class AIAnalysisResult extends BaseEntity {

    @Column(name = "review_task_id", nullable = false)
    private Long reviewTaskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_task_id", insertable = false, updatable = false)
    private ReviewTask reviewTask;

    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_type", nullable = false)
    private AIAnalysisType analysisType;

    @Column(name = "analysis_title", length = 200)
    private String analysisTitle;

    @Column(name = "analysis_content", columnDefinition = "TEXT")
    private String analysisContent;

    @Column(name = "analysis_summary", columnDefinition = "TEXT")
    private String analysisSummary;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "risk_level")
    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;

    @Column(name = "suggestions", columnDefinition = "TEXT")
    private String suggestions;

    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords;

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    @Column(name = "position_info", columnDefinition = "TEXT")
    private String positionInfo;

    @Column(name = "source_references", columnDefinition = "TEXT")
    private String sourceReferences;

    @Column(name = "analysis_metadata", columnDefinition = "TEXT")
    private String analysisMetadata;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    // AI分析类型枚举
    public enum AIAnalysisType {
        CONTENT_REVERSE_INSERTION("AI内容反向插入"),
        INTELLIGENT_SLICE_TAGGING("智能切片标记"),
        PROMPT_TESTING("提示词测试"),
        CITATION_SOURCE_MARKING("引用来源标记"),
        SMART_FORMATTING("AI智能格式化"),
        LONG_TEXT_INTERACTION("长文本交互"),
        DOCUMENT_DIFFERENCE_COMPARISON("文档差异对比");

        private final String description;

        AIAnalysisType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 风险等级枚举
    public enum RiskLevel {
        LOW("低风险"),
        MEDIUM("中风险"),
        HIGH("高风险"),
        CRITICAL("严重风险");

        private final String description;

        RiskLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Getters and Setters
    public Long getReviewTaskId() {
        return reviewTaskId;
    }

    public void setReviewTaskId(Long reviewTaskId) {
        this.reviewTaskId = reviewTaskId;
    }

    public ReviewTask getReviewTask() {
        return reviewTask;
    }

    public void setReviewTask(ReviewTask reviewTask) {
        this.reviewTask = reviewTask;
    }

    public AIAnalysisType getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(AIAnalysisType analysisType) {
        this.analysisType = analysisType;
    }

    public String getAnalysisTitle() {
        return analysisTitle;
    }

    public void setAnalysisTitle(String analysisTitle) {
        this.analysisTitle = analysisTitle;
    }

    public String getAnalysisContent() {
        return analysisContent;
    }

    public void setAnalysisContent(String analysisContent) {
        this.analysisContent = analysisContent;
    }

    public String getAnalysisSummary() {
        return analysisSummary;
    }

    public void setAnalysisSummary(String analysisSummary) {
        this.analysisSummary = analysisSummary;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(String suggestions) {
        this.suggestions = suggestions;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getPositionInfo() {
        return positionInfo;
    }

    public void setPositionInfo(String positionInfo) {
        this.positionInfo = positionInfo;
    }

    public String getSourceReferences() {
        return sourceReferences;
    }

    public void setSourceReferences(String sourceReferences) {
        this.sourceReferences = sourceReferences;
    }

    public String getAnalysisMetadata() {
        return analysisMetadata;
    }

    public void setAnalysisMetadata(String analysisMetadata) {
        this.analysisMetadata = analysisMetadata;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
}
