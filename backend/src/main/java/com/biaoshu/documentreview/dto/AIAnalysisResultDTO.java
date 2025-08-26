package com.biaoshu.documentreview.dto;

import com.biaoshu.documentreview.entity.AIAnalysisResult;

import java.time.LocalDateTime;

/**
 * AI分析结果DTO
 */
public class AIAnalysisResultDTO {

    private Long id;
    private Long reviewTaskId;
    private AIAnalysisResult.AIAnalysisType analysisType;
    private String analysisTitle;
    private String analysisContent;
    private String analysisSummary;
    private Double confidenceScore;
    private AIAnalysisResult.RiskLevel riskLevel;
    private String suggestions;
    private String keywords;
    private String tags;
    private String positionInfo;
    private String sourceReferences;
    private String analysisMetadata;
    private LocalDateTime processedAt;
    private Long processingTimeMs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReviewTaskId() {
        return reviewTaskId;
    }

    public void setReviewTaskId(Long reviewTaskId) {
        this.reviewTaskId = reviewTaskId;
    }

    public AIAnalysisResult.AIAnalysisType getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(AIAnalysisResult.AIAnalysisType analysisType) {
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

    public AIAnalysisResult.RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(AIAnalysisResult.RiskLevel riskLevel) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
