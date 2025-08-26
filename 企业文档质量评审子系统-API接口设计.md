# 企业文档质量评审子系统 - API接口设计

## 1. API设计原则

基于现有系统的RESTful风格，遵循以下原则：
- 统一的响应格式（Result<T>）
- 标准HTTP状态码
- 分页查询支持
- 统一异常处理
- API版本控制

## 2. 核心API接口

### 2.1 评审任务管理 (/api/v1/review-tasks)

#### 创建评审任务
```http
POST /api/v1/review-tasks
Content-Type: application/json

{
  "taskName": "合同评审-XX项目",
  "taskDescription": "针对XX项目合同进行全面评审",
  "documentIds": [1, 2, 3],
  "projectId": 100,
  "businessType": "CONTRACT",
  "confidentialityLevel": "CONFIDENTIAL",
  "workflowId": 1,
  "deadline": "2025-09-01T18:00:00",
  "priority": "HIGH",
  "aiAnalysisEnabled": true,
  "autoAssignmentEnabled": true,
  "expertRequirements": {
    "requiredSkills": ["合同法", "商务谈判"],
    "certificationLevel": "SENIOR",
    "excludeUserIds": [10, 20]
  }
}

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "taskId": 12345,
    "status": "PENDING",
    "aiAnalysisStatus": "PENDING"
  }
}
```

#### 获取评审任务列表
```http
GET /api/v1/review-tasks?status=IN_REVIEW&businessType=CONTRACT&page=1&size=20

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "taskId": 12345,
        "taskName": "合同评审-XX项目",
        "status": "IN_REVIEW",
        "priority": "HIGH",
        "deadline": "2025-09-01T18:00:00",
        "createdAt": "2025-08-26T10:00:00",
        "assignedExperts": [
          {
            "userId": 101,
            "userName": "张专家",
            "role": "PRIMARY_REVIEWER"
          }
        ],
        "progress": {
          "totalIssues": 15,
          "resolvedIssues": 8,
          "completionRate": 53.3
        }
      }
    ],
    "totalElements": 100,
    "totalPages": 5,
    "currentPage": 1
  }
}
```

#### 更新评审任务状态
```http
PUT /api/v1/review-tasks/{taskId}/status
Content-Type: application/json

{
  "status": "COMPLETED",
  "conclusion": "APPROVED",
  "conclusionReason": "所有问题已解决，符合发布标准"
}
```

### 2.2 AI分析服务 (/api/v1/ai-analysis)

#### 启动AI分析
```http
POST /api/v1/ai-analysis/start
Content-Type: application/json

{
  "reviewTaskId": 12345,
  "analysisTypes": ["FORMAT_CHECK", "COMPLIANCE_CHECK", "CONTENT_ANALYSIS"],
  "customPrompts": [
    {
      "type": "COMPLIANCE_CHECK",
      "prompt": "重点检查合同条款的法律合规性"
    }
  ]
}

Response:
{
  "code": 200,
  "message": "AI分析已启动",
  "data": {
    "analysisId": "ai_analysis_67890",
    "estimatedDuration": 300
  }
}
```

#### 获取AI分析进度
```http
GET /api/v1/ai-analysis/{analysisId}/progress

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "analysisId": "ai_analysis_67890",
    "status": "RUNNING",
    "progress": 65,
    "currentStep": "内容分析中",
    "estimatedRemaining": 120,
    "completedSteps": [
      {
        "step": "FORMAT_CHECK",
        "status": "COMPLETED",
        "issuesFound": 3
      },
      {
        "step": "COMPLIANCE_CHECK", 
        "status": "COMPLETED",
        "issuesFound": 7
      }
    ]
  }
}
```

#### 获取AI分析结果
```http
GET /api/v1/ai-analysis/{analysisId}/results

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "analysisId": "ai_analysis_67890",
    "status": "COMPLETED",
    "summary": {
      "totalIssues": 15,
      "criticalIssues": 2,
      "highIssues": 5,
      "mediumIssues": 6,
      "lowIssues": 2,
      "overallScore": 7.5,
      "complianceScore": 8.2
    },
    "issues": [
      {
        "issueId": "ai_issue_001",
        "category": "COMPLIANCE",
        "severity": "CRITICAL",
        "title": "缺少必要的法律条款",
        "description": "合同中缺少争议解决条款",
        "position": {
          "documentId": 1,
          "page": 3,
          "paragraph": 2,
          "startOffset": 150,
          "endOffset": 200
        },
        "suggestedAction": "ADD",
        "suggestedText": "建议添加仲裁条款",
        "confidenceScore": 0.95,
        "evidenceLinks": ["法律法规链接"]
      }
    ],
    "recommendations": [
      "建议重点关注第3页的法律条款",
      "建议邀请法务专家参与评审"
    ]
  }
}
```

### 2.3 专家管理 (/api/v1/experts)

#### 智能推荐专家
```http
POST /api/v1/experts/recommend
Content-Type: application/json

{
  "reviewTaskId": 12345,
  "requiredSkills": ["合同法", "商务谈判"],
  "certificationLevel": "SENIOR",
  "maxRecommendations": 5,
  "excludeUserIds": [10, 20],
  "workloadThreshold": 80
}

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "recommendations": [
      {
        "userId": 101,
        "userName": "张专家",
        "expertiseMatch": 95.5,
        "currentWorkload": 60,
        "averageReviewTime": 4.5,
        "qualityScore": 9.2,
        "availabilityScore": 85.0,
        "overallScore": 89.3,
        "matchedSkills": ["合同法", "商务谈判"],
        "recentReviews": 15,
        "estimatedCompletionTime": "2025-08-28T16:00:00"
      }
    ]
  }
}
```

#### 分派专家
```http
POST /api/v1/experts/assign
Content-Type: application/json

{
  "reviewTaskId": 12345,
  "assignments": [
    {
      "userId": 101,
      "role": "PRIMARY_REVIEWER",
      "deadline": "2025-08-30T18:00:00"
    },
    {
      "userId": 102,
      "role": "SECONDARY_REVIEWER",
      "deadline": "2025-08-30T18:00:00"
    }
  ],
  "notificationEnabled": true
}
```

#### 获取专家工作量看板
```http
GET /api/v1/experts/workload-dashboard

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "totalExperts": 50,
    "availableExperts": 35,
    "overloadedExperts": 8,
    "averageWorkload": 65.5,
    "expertStats": [
      {
        "userId": 101,
        "userName": "张专家",
        "currentWorkload": 80,
        "activeReviews": 4,
        "pendingReviews": 2,
        "avgResponseTime": 2.5,
        "status": "BUSY"
      }
    ]
  }
}
```

### 2.4 问题管理 (/api/v1/review-issues)

#### 创建问题
```http
POST /api/v1/review-issues
Content-Type: application/json

{
  "reviewTaskId": 12345,
  "title": "合同条款不明确",
  "description": "第3条款的表述存在歧义",
  "category": "CONTENT",
  "severity": "HIGH",
  "position": {
    "documentId": 1,
    "page": 3,
    "paragraph": 2,
    "startOffset": 150,
    "endOffset": 200
  },
  "originalText": "原始文本内容",
  "suggestedText": "建议修改的文本",
  "assignedToId": 201,
  "dueDate": "2025-08-28T18:00:00",
  "tags": ["urgent", "legal"]
}
```

#### 批量操作问题
```http
POST /api/v1/review-issues/batch-operation
Content-Type: application/json

{
  "operation": "ASSIGN",
  "issueIds": [1001, 1002, 1003],
  "assignedToId": 201,
  "dueDate": "2025-08-28T18:00:00"
}
```

#### 问题去重检测
```http
POST /api/v1/review-issues/duplicate-detection
Content-Type: application/json

{
  "reviewTaskId": 12345,
  "newIssue": {
    "title": "合同条款不明确",
    "description": "第3条款的表述存在歧义",
    "position": {
      "documentId": 1,
      "page": 3,
      "paragraph": 2
    }
  }
}

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "isDuplicate": true,
    "similarIssues": [
      {
        "issueId": 1001,
        "similarity": 0.85,
        "title": "第3条款表述模糊",
        "reporter": "李专家"
      }
    ],
    "recommendation": "MERGE_WITH_EXISTING"
  }
}
```

### 2.5 流程配置 (/api/v1/workflows)

#### 创建评审流程模板
```http
POST /api/v1/workflows
Content-Type: application/json

{
  "workflowName": "标准合同评审流程",
  "workflowDescription": "适用于一般商务合同的评审流程",
  "businessType": "CONTRACT",
  "workflowType": "LINEAR",
  "steps": [
    {
      "stepName": "AI初步分析",
      "stepOrder": 1,
      "stepType": "AI_ANALYSIS",
      "slaHours": 2,
      "isMandatory": true
    },
    {
      "stepName": "法务专家评审",
      "stepOrder": 2,
      "stepType": "EXPERT_REVIEW",
      "requiredRole": "LEGAL_EXPERT",
      "requiredSkills": ["合同法"],
      "slaHours": 24,
      "isMandatory": true
    },
    {
      "stepName": "业务专家评审",
      "stepOrder": 3,
      "stepType": "EXPERT_REVIEW",
      "requiredRole": "BUSINESS_EXPERT",
      "slaHours": 24,
      "isMandatory": true
    }
  ]
}
```

## 3. WebSocket实时通信

### 3.1 实时通知
```javascript
// 前端连接WebSocket
const ws = new WebSocket('ws://localhost:8080/ws/notifications');

// 接收实时消息
ws.onmessage = function(event) {
  const notification = JSON.parse(event.data);
  /*
  {
    "type": "REVIEW_ASSIGNED",
    "reviewTaskId": 12345,
    "message": "您有新的评审任务",
    "timestamp": "2025-08-26T10:00:00"
  }
  */
};
```

### 3.2 协同编辑状态同步
```javascript
// 同步评审状态
ws.send(JSON.stringify({
  "type": "REVIEW_STATUS_UPDATE",
  "reviewTaskId": 12345,
  "status": "IN_PROGRESS",
  "userId": 101
}));
```

## 4. 错误处理和状态码

```java
// 统一响应格式
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    private long timestamp;
}

// 错误码定义
public enum ErrorCode {
    SUCCESS(200, "操作成功"),
    INVALID_PARAM(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    INTERNAL_ERROR(500, "系统内部错误"),
    
    // 业务错误码
    REVIEW_TASK_NOT_FOUND(1001, "评审任务不存在"),
    EXPERT_NOT_AVAILABLE(1002, "专家不可用"),
    WORKFLOW_INVALID(1003, "流程配置无效"),
    AI_ANALYSIS_FAILED(1004, "AI分析失败");
}
```
