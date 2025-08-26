import request from '@/utils/request'

// AI服务状态相关接口
export interface AIServiceStatus {
  available: boolean
  service: string
  timestamp: number
}

export interface AIServiceConfig {
  enabled: boolean
  service_type: string
  model: string
  features: string[]
}

// AI分析结果接口
export interface AIAnalysisResult {
  id?: number
  reviewTaskId: number
  analysisType: string
  analysisTitle: string
  analysisContent: string
  analysisSummary: string
  confidenceScore: number
  riskLevel: string
  suggestions?: string
  keywords?: string
  tags?: string
  positionInfo?: string
  sourceReferences?: string
  analysisMetadata?: string
  processedAt?: string
  processingTimeMs?: number
  createdAt?: string
  updatedAt?: string
}

// 评审任务相关接口
export interface ReviewTask {
  id?: number
  taskName: string
  taskDescription?: string
  documentId: number
  projectId: number
  creatorId?: number
  status?: string
  priority?: string
  deadline?: string
  aiAnalysisEnabled?: boolean
  aiAnalysisStatus?: string
  aiAnalysisProgress?: number
  createdAt?: string
  updatedAt?: string
}

export interface ReviewTaskCreateDTO {
  taskName: string
  taskDescription?: string
  documentId: number
  projectId: number
  priority?: string
  deadline?: string
  aiAnalysisEnabled?: boolean
}

// AI服务状态API
export const aiStatusApi = {
  // 获取AI服务状态
  getStatus(): Promise<AIServiceStatus> {
    return request.get('/api/ai/status')
  },

  // 测试AI服务连接
  testConnection(): Promise<string> {
    return request.post('/api/ai/status/test')
  },

  // 获取AI服务配置
  getConfig(): Promise<AIServiceConfig> {
    return request.get('/api/ai/status/config')
  }
}

// AI功能测试API
export const aiTestApi = {
  // AI内容反向插入
  testContentInsertion(): Promise<string> {
    return request.post('/test/ai/content-insertion')
  },

  // 智能切片标记
  testSliceTagging(): Promise<string> {
    return request.post('/test/ai/slice-tagging')
  },

  // 提示词测试工具
  testPromptTesting(): Promise<string> {
    return request.post('/test/ai/prompt-testing')
  },

  // 引用来源标记
  testCitationMarking(): Promise<string> {
    return request.post('/test/ai/citation-marking')
  },

  // AI智能格式化
  testSmartFormatting(): Promise<string> {
    return request.post('/test/ai/smart-formatting')
  },

  // 长文本交互
  testLongTextInteraction(): Promise<string> {
    return request.post('/test/ai/long-text-interaction')
  },

  // 文档差异对比
  testDifferenceComparison(): Promise<string> {
    return request.post('/test/ai/difference-comparison')
  },

  // 完整AI分析
  testFullAnalysis(): Promise<string> {
    return request.post('/test/ai/full-analysis')
  },

  // 获取AI分析进度
  getAnalysisProgress(taskId: number): Promise<number> {
    return request.get(`/test/ai/progress/${taskId}`)
  },

  // 取消AI分析
  cancelAnalysis(taskId: number): Promise<boolean> {
    return request.post(`/test/ai/cancel/${taskId}`)
  }
}

// 评审任务API
export const reviewTaskApi = {
  // 创建评审任务
  createTask(data: ReviewTaskCreateDTO): Promise<ReviewTask> {
    return request.post('/api/review-tasks', data)
  },

  // 获取评审任务详情
  getTask(taskId: number): Promise<ReviewTask> {
    return request.get(`/api/review-tasks/${taskId}`)
  },

  // 获取评审任务列表
  getTasks(params?: any): Promise<{ content: ReviewTask[], totalElements: number }> {
    return request.get('/api/review-tasks', { params })
  },

  // 启动AI分析
  startAIAnalysis(taskId: number): Promise<boolean> {
    return request.post(`/api/review-tasks/${taskId}/ai-analysis/start`)
  },

  // 停止AI分析
  stopAIAnalysis(taskId: number): Promise<boolean> {
    return request.post(`/api/review-tasks/${taskId}/ai-analysis/stop`)
  },

  // 获取AI分析进度
  getAIAnalysisProgress(taskId: number): Promise<number> {
    return request.get(`/api/review-tasks/${taskId}/ai-analysis/progress`)
  },

  // 获取AI分析结果
  getAIAnalysisResults(taskId: number, analysisType?: string): Promise<AIAnalysisResult[]> {
    const params = analysisType ? { analysisType } : {}
    return request.get(`/api/review-tasks/${taskId}/ai-analysis/results`, { params })
  }
}

// AI分析类型枚举
export const AIAnalysisType = {
  CONTENT_REVERSE_INSERTION: 'CONTENT_REVERSE_INSERTION',
  INTELLIGENT_SLICE_TAGGING: 'INTELLIGENT_SLICE_TAGGING',
  PROMPT_TESTING: 'PROMPT_TESTING',
  CITATION_SOURCE_MARKING: 'CITATION_SOURCE_MARKING',
  SMART_FORMATTING: 'SMART_FORMATTING',
  LONG_TEXT_INTERACTION: 'LONG_TEXT_INTERACTION',
  DOCUMENT_DIFFERENCE_COMPARISON: 'DOCUMENT_DIFFERENCE_COMPARISON'
} as const

// AI分析类型标签映射
export const AIAnalysisTypeLabels = {
  [AIAnalysisType.CONTENT_REVERSE_INSERTION]: 'AI内容反向插入',
  [AIAnalysisType.INTELLIGENT_SLICE_TAGGING]: '智能切片标记',
  [AIAnalysisType.PROMPT_TESTING]: '提示词测试工具',
  [AIAnalysisType.CITATION_SOURCE_MARKING]: '引用来源标记',
  [AIAnalysisType.SMART_FORMATTING]: 'AI智能格式化',
  [AIAnalysisType.LONG_TEXT_INTERACTION]: '长文本交互',
  [AIAnalysisType.DOCUMENT_DIFFERENCE_COMPARISON]: '文档差异对比'
} as const

// 风险等级枚举
export const RiskLevel = {
  LOW: 'LOW',
  MEDIUM: 'MEDIUM',
  HIGH: 'HIGH',
  CRITICAL: 'CRITICAL'
} as const

// 风险等级标签映射
export const RiskLevelLabels = {
  [RiskLevel.LOW]: '低风险',
  [RiskLevel.MEDIUM]: '中风险',
  [RiskLevel.HIGH]: '高风险',
  [RiskLevel.CRITICAL]: '严重风险'
} as const

// 风险等级颜色映射
export const RiskLevelColors = {
  [RiskLevel.LOW]: 'success',
  [RiskLevel.MEDIUM]: 'warning',
  [RiskLevel.HIGH]: 'danger',
  [RiskLevel.CRITICAL]: 'danger'
} as const
