-- 企业文档质量评审系统 - 评审任务相关表结构
-- 版本: V1.1
-- 创建时间: 2024-01-01

-- 评审任务表
CREATE TABLE IF NOT EXISTS review_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    task_name VARCHAR(200) NOT NULL COMMENT '任务名称',
    task_description TEXT COMMENT '任务描述',
    business_type VARCHAR(50) NOT NULL DEFAULT 'OTHER' COMMENT '业务类型',
    confidentiality_level VARCHAR(50) NOT NULL DEFAULT 'INTERNAL' COMMENT '密级',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' COMMENT '优先级',
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT' COMMENT '任务状态',
    review_template_id BIGINT COMMENT '评审模板ID',
    project_id BIGINT COMMENT '关联项目ID',
    creator_id BIGINT NOT NULL COMMENT '创建人ID',
    deadline DATETIME COMMENT '截止时间',
    auto_assignment_enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用自动分派',
    convergence_gate_enabled BOOLEAN DEFAULT FALSE COMMENT '是否启用收敛闸门',
    convergence_deadline DATETIME COMMENT '收敛截止时间',
    review_conclusion VARCHAR(50) COMMENT '评审结论',
    conclusion_reason TEXT COMMENT '结论原因',
    progress_data JSON COMMENT '进度数据(JSON格式)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '更新人',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    
    INDEX idx_creator_id (creator_id),
    INDEX idx_project_id (project_id),
    INDEX idx_status (status),
    INDEX idx_priority (priority),
    INDEX idx_business_type (business_type),
    INDEX idx_deadline (deadline),
    INDEX idx_created_at (created_at),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评审任务表';

-- 评审任务分派表
CREATE TABLE IF NOT EXISTS review_task_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    review_task_id BIGINT NOT NULL COMMENT '评审任务ID',
    expert_id BIGINT NOT NULL COMMENT '专家ID',
    assignment_type VARCHAR(30) NOT NULL DEFAULT 'REVIEWER' COMMENT '分派类型',
    assignment_role VARCHAR(50) COMMENT '评审角色',
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING' COMMENT '分派状态',
    assigned_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '分派时间',
    accepted_at DATETIME COMMENT '接受时间',
    completed_at DATETIME COMMENT '完成时间',
    assignment_notes TEXT COMMENT '分派说明',
    completion_notes TEXT COMMENT '完成说明',
    workload_hours DECIMAL(5,2) COMMENT '预估工作量(小时)',
    actual_hours DECIMAL(5,2) COMMENT '实际工作量(小时)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '更新人',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    
    INDEX idx_review_task_id (review_task_id),
    INDEX idx_expert_id (expert_id),
    INDEX idx_status (status),
    INDEX idx_assignment_type (assignment_type),
    INDEX idx_assigned_at (assigned_at),
    INDEX idx_deleted (deleted),
    UNIQUE KEY uk_task_expert (review_task_id, expert_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评审任务分派表';

-- 评审问题表
CREATE TABLE IF NOT EXISTS review_issues (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    review_task_id BIGINT NOT NULL COMMENT '评审任务ID',
    document_id BIGINT COMMENT '文档ID',
    issue_title VARCHAR(200) NOT NULL COMMENT '问题标题',
    issue_description TEXT COMMENT '问题描述',
    issue_type VARCHAR(50) NOT NULL COMMENT '问题类型',
    severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' COMMENT '严重程度',
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN' COMMENT '问题状态',
    location_info JSON COMMENT '位置信息(页码、段落等)',
    reporter_id BIGINT NOT NULL COMMENT '报告人ID',
    assignee_id BIGINT COMMENT '负责人ID',
    resolver_id BIGINT COMMENT '解决人ID',
    reported_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报告时间',
    resolved_at DATETIME COMMENT '解决时间',
    verified_at DATETIME COMMENT '验证时间',
    resolution_notes TEXT COMMENT '解决说明',
    verification_notes TEXT COMMENT '验证说明',
    tags JSON COMMENT '标签(JSON数组)',
    attachments JSON COMMENT '附件信息(JSON数组)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '更新人',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    
    INDEX idx_review_task_id (review_task_id),
    INDEX idx_document_id (document_id),
    INDEX idx_issue_type (issue_type),
    INDEX idx_severity (severity),
    INDEX idx_status (status),
    INDEX idx_reporter_id (reporter_id),
    INDEX idx_assignee_id (assignee_id),
    INDEX idx_reported_at (reported_at),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评审问题表';

-- 评审评论表
CREATE TABLE IF NOT EXISTS review_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    review_task_id BIGINT NOT NULL COMMENT '评审任务ID',
    document_id BIGINT COMMENT '文档ID',
    issue_id BIGINT COMMENT '关联问题ID',
    parent_comment_id BIGINT COMMENT '父评论ID',
    comment_type VARCHAR(30) NOT NULL DEFAULT 'GENERAL' COMMENT '评论类型',
    comment_content TEXT NOT NULL COMMENT '评论内容',
    location_info JSON COMMENT '位置信息',
    commenter_id BIGINT NOT NULL COMMENT '评论人ID',
    visibility VARCHAR(20) NOT NULL DEFAULT 'PUBLIC' COMMENT '可见性',
    is_resolved BOOLEAN DEFAULT FALSE COMMENT '是否已解决',
    resolved_by BIGINT COMMENT '解决人ID',
    resolved_at DATETIME COMMENT '解决时间',
    attachments JSON COMMENT '附件信息',
    mentions JSON COMMENT '提及的用户ID列表',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '更新人',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    
    INDEX idx_review_task_id (review_task_id),
    INDEX idx_document_id (document_id),
    INDEX idx_issue_id (issue_id),
    INDEX idx_parent_comment_id (parent_comment_id),
    INDEX idx_commenter_id (commenter_id),
    INDEX idx_comment_type (comment_type),
    INDEX idx_created_at (created_at),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评审评论表';

-- 评审工作流模板表
CREATE TABLE IF NOT EXISTS review_workflows (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    workflow_name VARCHAR(100) NOT NULL COMMENT '工作流名称',
    workflow_description TEXT COMMENT '工作流描述',
    workflow_type VARCHAR(30) NOT NULL DEFAULT 'LINEAR' COMMENT '工作流类型',
    business_type VARCHAR(50) COMMENT '适用业务类型',
    confidentiality_level VARCHAR(50) COMMENT '适用密级',
    is_default BOOLEAN DEFAULT FALSE COMMENT '是否默认模板',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    workflow_config JSON NOT NULL COMMENT '工作流配置(JSON)',
    sla_hours INT DEFAULT 72 COMMENT '默认SLA时间(小时)',
    auto_assignment_enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用自动分派',
    convergence_gate_enabled BOOLEAN DEFAULT FALSE COMMENT '是否启用收敛闸门',
    ai_analysis_enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用AI分析',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '更新人',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    
    INDEX idx_workflow_type (workflow_type),
    INDEX idx_business_type (business_type),
    INDEX idx_is_default (is_default),
    INDEX idx_is_active (is_active),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评审工作流模板表';

-- 评审指标表
CREATE TABLE IF NOT EXISTS review_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    review_task_id BIGINT NOT NULL COMMENT '评审任务ID',
    metric_type VARCHAR(50) NOT NULL COMMENT '指标类型',
    metric_name VARCHAR(100) NOT NULL COMMENT '指标名称',
    metric_value DECIMAL(10,4) COMMENT '指标值',
    metric_unit VARCHAR(20) COMMENT '指标单位',
    metric_description TEXT COMMENT '指标描述',
    calculated_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '计算时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_review_task_id (review_task_id),
    INDEX idx_metric_type (metric_type),
    INDEX idx_calculated_at (calculated_at),
    UNIQUE KEY uk_task_metric (review_task_id, metric_type, metric_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评审指标表';

-- 任务文档关联表
CREATE TABLE IF NOT EXISTS review_task_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    review_task_id BIGINT NOT NULL COMMENT '评审任务ID',
    document_id BIGINT NOT NULL COMMENT '文档ID',
    document_role VARCHAR(30) NOT NULL DEFAULT 'PRIMARY' COMMENT '文档角色',
    document_version VARCHAR(50) COMMENT '文档版本',
    is_primary BOOLEAN DEFAULT FALSE COMMENT '是否主要文档',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    created_by BIGINT COMMENT '创建人',
    
    INDEX idx_review_task_id (review_task_id),
    INDEX idx_document_id (document_id),
    INDEX idx_document_role (document_role),
    UNIQUE KEY uk_task_document (review_task_id, document_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务文档关联表';

-- 添加外键约束
ALTER TABLE review_task_assignments 
ADD CONSTRAINT fk_assignment_task 
FOREIGN KEY (review_task_id) REFERENCES review_tasks(id) ON DELETE CASCADE;

ALTER TABLE review_issues 
ADD CONSTRAINT fk_issue_task 
FOREIGN KEY (review_task_id) REFERENCES review_tasks(id) ON DELETE CASCADE;

ALTER TABLE review_comments 
ADD CONSTRAINT fk_comment_task 
FOREIGN KEY (review_task_id) REFERENCES review_tasks(id) ON DELETE CASCADE;

ALTER TABLE review_comments 
ADD CONSTRAINT fk_comment_issue 
FOREIGN KEY (issue_id) REFERENCES review_issues(id) ON DELETE CASCADE;

ALTER TABLE review_metrics 
ADD CONSTRAINT fk_metric_task 
FOREIGN KEY (review_task_id) REFERENCES review_tasks(id) ON DELETE CASCADE;

ALTER TABLE review_task_documents 
ADD CONSTRAINT fk_task_doc_task 
FOREIGN KEY (review_task_id) REFERENCES review_tasks(id) ON DELETE CASCADE;
