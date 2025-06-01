# 前端API接口规范

## 一、项目管理接口

### 1.1 项目列表
```typescript
GET /api/v1/projects

// 请求参数
interface ProjectListParams {
  page: number;          // 页码
  pageSize: number;      // 每页条数
  status?: string;       // 项目状态：进行中/已完成/已废标
  type?: string;         // 项目类型：产品类/服务类/工程类
  keyword?: string;      // 关键词：项目名称/编号/客户名称
  dateRange?: string[];  // 时间范围：[startDate, endDate]
  department?: string;   // 所属部门
  amount?: number[];     // 金额范围：[minAmount, maxAmount]
  manager?: string;      // 负责人ID
  sortField?: string;    // 排序字段
  sortOrder?: 'asc' | 'desc'; // 排序方向
}

// 响应数据
interface ProjectListResponse {
  total: number;
  list: Array<{
    id: string;
    name: string;
    code: string;
    type: string;
    status: string;
    customer: string;
    amount: number;
    createTime: string;
    deadline: string;
    manager: {
      id: string;
      name: string;
      avatar: string;
    };
    department: string;
    progress: number;
    tags: string[];
  }>;
}
```

### 1.2 项目详情
```typescript
GET /api/v1/projects/:id

// 响应数据
interface ProjectDetailResponse {
  id: string;
  name: string;
  code: string;
  type: string;
  status: string;
  customer: {
    id: string;
    name: string;
    contact: string;
    phone: string;
    email: string;
  };
  amount: number;
  createTime: string;
  deadline: string;
  manager: {
    id: string;
    name: string;
    avatar: string;
    phone: string;
    email: string;
  };
  department: string;
  description: string;
  tags: string[];
  team: Array<{
    id: string;
    name: string;
    avatar: string;
    role: string;
    department: string;
  }>;
  documents: Array<{
    id: string;
    title: string;
    type: string;
    status: string;
    updateTime: string;
    creator: {
      id: string;
      name: string;
    };
  }>;
  timeline: Array<{
    id: string;
    time: string;
    type: string;
    content: string;
    operator: {
      id: string;
      name: string;
    };
  }>;
}
```

### 1.3 创建项目
```typescript
POST /api/v1/projects

// 请求参数
interface CreateProjectParams {
  name: string;         // 项目名称
  type: string;         // 项目类型
  customerId: string;   // 客户ID
  amount: number;       // 项目金额
  deadline: string;     // 截止时间
  managerId: string;    // 负责人ID
  departmentId: string; // 所属部门ID
  description?: string; // 项目描述
  tags?: string[];      // 项目标签
  teamMembers?: Array<{
    userId: string;
    role: string;
  }>;
}

// 响应数据
interface CreateProjectResponse {
  id: string;           // 新创建的项目ID
  code: string;         // 项目编号
}
```

### 1.4 更新项目
```typescript
PUT /api/v1/projects/:id

// 请求参数
interface UpdateProjectParams {
  name?: string;
  type?: string;
  customerId?: string;
  amount?: number;
  deadline?: string;
  managerId?: string;
  departmentId?: string;
  description?: string;
  tags?: string[];
  status?: string;
}

// 响应数据
interface UpdateProjectResponse {
  id: string;
  updateTime: string;
}
```

## 二、文档管理接口

### 2.1 文档列表
```typescript
GET /api/v1/documents

// 请求参数
interface DocumentListParams {
  page: number;
  pageSize: number;
  projectId?: string;    // 项目ID
  type?: string;         // 文档类型
  status?: string;       // 文档状态
  keyword?: string;      // 关键词
  dateRange?: string[];  // 时间范围
  creator?: string;      // 创建人ID
  reviewer?: string;     // 审核人ID
}

// 响应数据
interface DocumentListResponse {
  total: number;
  list: Array<{
    id: string;
    title: string;
    type: string;
    status: string;
    projectId: string;
    projectName: string;
    createTime: string;
    updateTime: string;
    creator: {
      id: string;
      name: string;
      avatar: string;
    };
    reviewers: Array<{
      id: string;
      name: string;
      status: string;
    }>;
    version: number;
    size: number;
  }>;
}
```

### 2.2 文档详情
```typescript
GET /api/v1/documents/:id

// 响应数据
interface DocumentDetailResponse {
  id: string;
  title: string;
  type: string;
  status: string;
  content: string;
  projectId: string;
  projectName: string;
  createTime: string;
  updateTime: string;
  creator: {
    id: string;
    name: string;
    avatar: string;
  };
  reviewers: Array<{
    id: string;
    name: string;
    avatar: string;
    status: string;
    comment?: string;
    reviewTime?: string;
  }>;
  version: number;
  history: Array<{
    version: number;
    updateTime: string;
    operator: {
      id: string;
      name: string;
    };
    comment: string;
  }>;
  attachments: Array<{
    id: string;
    name: string;
    type: string;
    size: number;
    uploadTime: string;
    url: string;
  }>;
}
```

### 2.3 创建文档
```typescript
POST /api/v1/documents

// 请求参数
interface CreateDocumentParams {
  projectId: string;    // 项目ID
  title: string;        // 文档标题
  type: string;         // 文档类型
  content: string;      // 文档内容
  reviewers?: string[]; // 审核人ID列表
  attachments?: Array<{
    name: string;
    type: string;
    size: number;
    url: string;
  }>;
}

// 响应数据
interface CreateDocumentResponse {
  id: string;           // 新创建的文档ID
  version: number;      // 文档版本号
}
```

### 2.4 更新文档
```typescript
PUT /api/v1/documents/:id

// 请求参数
interface UpdateDocumentParams {
  title?: string;
  content?: string;
  reviewers?: string[];
  attachments?: Array<{
    name: string;
    type: string;
    size: number;
    url: string;
  }>;
  comment?: string;     // 更新说明
}

// 响应数据
interface UpdateDocumentResponse {
  id: string;
  version: number;
  updateTime: string;
}
```

## 三、审核流程接口

### 3.1 提交审核
```typescript
POST /api/v1/documents/:id/review

// 请求参数
interface SubmitReviewParams {
  reviewers: string[];  // 审核人ID列表
  comment?: string;     // 提交说明
  notifyType?: string[]; // 通知方式：email/sms/system
}

// 响应数据
interface SubmitReviewResponse {
  reviewId: string;     // 审核流程ID
  submitTime: string;   // 提交时间
}
```

### 3.2 审核操作
```typescript
POST /api/v1/reviews/:id/action

// 请求参数
interface ReviewActionParams {
  action: 'approve' | 'reject'; // 审核动作
  comment?: string;            // 审核意见
  attachments?: Array<{        // 相关附件
    name: string;
    type: string;
    size: number;
    url: string;
  }>;
}

// 响应数据
interface ReviewActionResponse {
  reviewId: string;
  status: string;
  actionTime: string;
}
```

### 3.3 审核历史
```typescript
GET /api/v1/documents/:id/review-history

// 响应数据
interface ReviewHistoryResponse {
  list: Array<{
    id: string;
    version: number;
    submitTime: string;
    submitter: {
      id: string;
      name: string;
      avatar: string;
    };
    reviewers: Array<{
      id: string;
      name: string;
      avatar: string;
      status: string;
      comment?: string;
      actionTime?: string;
    }>;
    status: string;
    comment: string;
    attachments: Array<{
      name: string;
      type: string;
      size: number;
      url: string;
    }>;
  }>;
}
```

## 四、团队协作接口

### 4.1 团队成员列表
```typescript
GET /api/v1/projects/:id/team

// 响应数据
interface TeamMemberListResponse {
  list: Array<{
    id: string;
    name: string;
    avatar: string;
    role: string;
    department: string;
    joinTime: string;
    tasks: Array<{
      id: string;
      title: string;
      status: string;
      deadline: string;
    }>;
    documents: Array<{
      id: string;
      title: string;
      type: string;
      status: string;
    }>;
  }>;
}
```

### 4.2 添加团队成员
```typescript
POST /api/v1/projects/:id/team

// 请求参数
interface AddTeamMemberParams {
  members: Array<{
    userId: string;
    role: string;
    tasks?: Array<{
      title: string;
      description?: string;
      deadline?: string;
    }>;
  }>;
}

// 响应数据
interface AddTeamMemberResponse {
  success: boolean;
  failedMembers?: Array<{
    userId: string;
    reason: string;
  }>;
}
```

### 4.3 更新成员角色
```typescript
PUT /api/v1/projects/:id/team/:userId

// 请求参数
interface UpdateTeamMemberParams {
  role: string;
  tasks?: Array<{
    title: string;
    description?: string;
    deadline?: string;
  }>;
}

// 响应数据
interface UpdateTeamMemberResponse {
  userId: string;
  updateTime: string;
}
```

## 五、统计分析接口

### 5.1 项目统计
```typescript
GET /api/v1/statistics/projects

// 请求参数
interface ProjectStatisticsParams {
  dateRange: string[];  // 统计时间范围
  type?: string;        // 项目类型
  department?: string;  // 所属部门
}

// 响应数据
interface ProjectStatisticsResponse {
  total: {
    count: number;
    amount: number;
  };
  byStatus: Array<{
    status: string;
    count: number;
    amount: number;
  }>;
  byType: Array<{
    type: string;
    count: number;
    amount: number;
  }>;
  byDepartment: Array<{
    department: string;
    count: number;
    amount: number;
  }>;
  trend: Array<{
    date: string;
    count: number;
    amount: number;
  }>;
}
```

### 5.2 文档统计
```typescript
GET /api/v1/statistics/documents

// 请求参数
interface DocumentStatisticsParams {
  dateRange: string[];
  type?: string;
  projectId?: string;
}

// 响应数据
interface DocumentStatisticsResponse {
  total: {
    count: number;
    size: number;
  };
  byType: Array<{
    type: string;
    count: number;
    size: number;
  }>;
  byStatus: Array<{
    status: string;
    count: number;
  }>;
  byCreator: Array<{
    creator: {
      id: string;
      name: string;
    };
    count: number;
  }>;
  reviewStats: {
    total: number;
    approved: number;
    rejected: number;
    pending: number;
    avgReviewTime: number;
  };
}
```

## 六、系统配置接口

### 6.1 获取系统配置
```typescript
GET /api/v1/system/config

// 响应数据
interface SystemConfigResponse {
  basic: {
    companyName: string;
    logo: string;
    theme: string;
    language: string;
  };
  security: {
    passwordPolicy: {
      minLength: number;
      requireNumbers: boolean;
      requireLowercase: boolean;
      requireUppercase: boolean;
      requireSpecialChars: boolean;
    };
    sessionTimeout: number;
    maxLoginAttempts: number;
  };
  notification: {
    channels: Array<{
      type: string;
      enabled: boolean;
      config: Record<string, any>;
    }>;
    templates: Array<{
      type: string;
      title: string;
      content: string;
    }>;
  };
  workflow: {
    defaultReviewers: Array<{
      role: string;
      count: number;
    }>;
    autoReminder: {
      enabled: boolean;
      beforeDays: number;
    };
  };
}
```

### 6.2 更新系统配置
```typescript
PUT /api/v1/system/config

// 请求参数
interface UpdateSystemConfigParams {
  basic?: {
    companyName?: string;
    logo?: string;
    theme?: string;
    language?: string;
  };
  security?: {
    passwordPolicy?: {
      minLength?: number;
      requireNumbers?: boolean;
      requireLowercase?: boolean;
      requireUppercase?: boolean;
      requireSpecialChars?: boolean;
    };
    sessionTimeout?: number;
    maxLoginAttempts?: number;
  };
  notification?: {
    channels?: Array<{
      type: string;
      enabled: boolean;
      config: Record<string, any>;
    }>;
    templates?: Array<{
      type: string;
      title: string;
      content: string;
    }>;
  };
  workflow?: {
    defaultReviewers?: Array<{
      role: string;
      count: number;
    }>;
    autoReminder?: {
      enabled: boolean;
      beforeDays: number;
    };
  };
}

// 响应数据
interface UpdateSystemConfigResponse {
  updateTime: string;
}
```

## 七、日志审计接口

### 7.1 操作日志
```typescript
GET /api/v1/audit/operations

// 请求参数
interface OperationLogParams {
  page: number;
  pageSize: number;
  dateRange?: string[];
  operator?: string;
  type?: string;
  module?: string;
  status?: string;
}

// 响应数据
interface OperationLogResponse {
  total: number;
  list: Array<{
    id: string;
    time: string;
    operator: {
      id: string;
      name: string;
      department: string;
    };
    type: string;
    module: string;
    action: string;
    status: string;
    detail: {
      before?: any;
      after?: any;
      reason?: string;
    };
    ip: string;
    userAgent: string;
  }>;
}
```

### 7.2 登录日志
```typescript
GET /api/v1/audit/logins

// 请求参数
interface LoginLogParams {
  page: number;
  pageSize: number;
  dateRange?: string[];
  username?: string;
  status?: string;
  ip?: string;
}

// 响应数据
interface LoginLogResponse {
  total: number;
  list: Array<{
    id: string;
    time: string;
    username: string;
    status: string;
    ip: string;
    location: string;
    userAgent: string;
    device: string;
    failReason?: string;
  }>;
}
``` 