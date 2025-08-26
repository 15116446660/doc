-- 企业文档质量评审系统 - 数据库初始化脚本
-- 版本: V1.0.0
-- 描述: 创建评审系统核心表结构

-- 1. 扩展现有用户表（假设已存在）
-- ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(500);
-- ALTER TABLE users ADD COLUMN IF NOT EXISTS phone VARCHAR(20);
-- ALTER TABLE users ADD COLUMN IF NOT EXISTS department VARCHAR(100);

-- 2. 评审任务表扩展
CREATE TABLE IF NOT EXISTS review_tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_name VARCHAR(200) NOT NULL COMMENT '任务名称',
    task_description TEXT COMMENT '任务描述',
    business_type ENUM('CONTRACT', 'TENDER', 'TECHNICAL_PROPOSAL', 'POLICY', 'MANUAL', 'OTHER') NOT NULL COMMENT '业务类型',
    confidentiality_level ENUM('PUBLIC', 'INTERNAL', 'CONFIDENTIAL', 'SECRET') DEFAULT 'INTERNAL' COMMENT '密级',
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM' COMMENT '优先级',
    status ENUM('DRAFT', 'PENDING', 'AI_ANALYZING', 'EXPERT_ASSIGNMENT', 'IN_REVIEW', 'REVISION_REQUIRED', 'VERIFICATION', 'COMPLETED', 'CANCELLED', 'ARCHIVED') DEFAULT 'DRAFT' COMMENT '任务状态',
    review_template_id BIGINT COMMENT '评审模板ID',
    project_id BIGINT COMMENT '关联项目ID',
    creator_id BIGINT NOT NULL COMMENT '创建人ID',
    deadline DATETIME COMMENT '截止时间',
    auto_assignment_enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用自动分派',
    convergence_gate_enabled BOOLEAN DEFAULT FALSE COMMENT '是否启用收敛闸门',
    convergence_deadline DATETIME COMMENT '收敛截止时间',
    review_conclusion ENUM('APPROVED', 'CONDITIONAL_APPROVED', 'REJECTED', 'NEED_MAJOR_REVISION') COMMENT '评审结论',
    conclusion_reason TEXT COMMENT '结论原因',
    progress_data JSON COMMENT '进度数据',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    
    INDEX idx_status (status),
    INDEX idx_business_type (business_type),
    INDEX idx_priority (priority),
    INDEX idx_creator_id (creator_id),
    INDEX idx_deadline (deadline),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (creator_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评审任务表';

-- 3. 专家档案表
CREATE TABLE IF NOT EXISTS expert_profiles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    expertise_areas JSON COMMENT '专业领域',
    skill_tags JSON COMMENT '技能标签',
    certification_level ENUM('JUNIOR', 'INTERMEDIATE', 'SENIOR', 'EXPERT') DEFAULT 'INTERMEDIATE' COMMENT '认证级别',
    current_workload INT DEFAULT 0 COMMENT '当前工作量',
    max_concurrent_reviews INT DEFAULT 5 COMMENT '最大并发评审数',
    average_review_time DECIMAL(5,2) COMMENT '平均评审时间(小时)',
    quality_score DECIMAL(3,1) DEFAULT 5.0 COMMENT '质量评分(1-10)',
    total_reviews_completed INT DEFAULT 0 COMMENT '完成评审总数',
    is_available BOOLEAN DEFAULT TRUE COMMENT '是否可用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (user_id),
    INDEX idx_available (is_available),
    INDEX idx_workload (current_workload),
    INDEX idx_quality_score (quality_score),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='专家档案表';

-- 4. 评审工作流模板表
CREATE TABLE IF NOT EXISTS review_workflows (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_name VARCHAR(200) NOT NULL COMMENT '流程名称',
    workflow_description TEXT COMMENT '流程描述',
    business_type ENUM('CONTRACT', 'TENDER', 'TECHNICAL_PROPOSAL', 'POLICY', 'MANUAL', 'OTHER') COMMENT '适用业务类型',
    workflow_type ENUM('LINEAR', 'PARALLEL', 'CONDITIONAL') DEFAULT 'LINEAR' COMMENT '流程类型',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否激活',
    version VARCHAR(20) DEFAULT '1.0' COMMENT '版本号',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    
    INDEX idx_business_type (business_type),
    INDEX idx_active (is_active),
    INDEX idx_workflow_type (workflow_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评审工作流模板表';

-- 5. 评审工作流步骤表
CREATE TABLE IF NOT EXISTS review_workflow_steps (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_id BIGINT NOT NULL COMMENT '工作流ID',
    step_name VARCHAR(200) NOT NULL COMMENT '步骤名称',
    step_order INT NOT NULL COMMENT '步骤顺序',
    step_type ENUM('AI_ANALYSIS', 'EXPERT_REVIEW', 'COMPLIANCE_CHECK', 'FINAL_APPROVAL') NOT NULL COMMENT '步骤类型',
    required_role VARCHAR(100) COMMENT '要求角色',
    required_skills JSON COMMENT '要求技能',
    sla_hours INT COMMENT 'SLA时间(小时)',
    is_mandatory BOOLEAN DEFAULT TRUE COMMENT '是否必须',
    approval_threshold INT COMMENT '通过阈值',
    condition_expression TEXT COMMENT '条件表达式',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_workflow_id (workflow_id),
    INDEX idx_step_order (step_order),
    INDEX idx_step_type (step_type),
    FOREIGN KEY (workflow_id) REFERENCES review_workflows(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评审工作流步骤表';

-- 6. 评审问题表扩展
CREATE TABLE IF NOT EXISTS review_issues (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    review_task_id BIGINT NOT NULL COMMENT '评审任务ID',
    issue_number VARCHAR(50) NOT NULL COMMENT '问题编号',
    title VARCHAR(500) NOT NULL COMMENT '问题标题',
    description TEXT COMMENT '问题描述',
    category ENUM('FORMAT', 'COMPLIANCE', 'CONTENT', 'LOGIC', 'REFERENCE', 'SECURITY', 'OTHER') NOT NULL COMMENT '问题类别',
    severity ENUM('INFO', 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM' COMMENT '严重程度',
    status ENUM('OPEN', 'IN_PROGRESS', 'RESOLVED', 'VERIFIED', 'CLOSED', 'DEFERRED') DEFAULT 'OPEN' COMMENT '问题状态',
    assigned_to_id BIGINT COMMENT '指派给',
    reporter_id BIGINT NOT NULL COMMENT '报告人ID',
    position_info JSON COMMENT '位置信息',
    original_text TEXT COMMENT '原始文本',
    suggested_text TEXT COMMENT '建议文本',
    resolution TEXT COMMENT '解决方案',
    verification_result ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'PARTIALLY_ACCEPTED') COMMENT '验证结果',
    due_date DATETIME COMMENT '截止日期',
    resolved_at DATETIME COMMENT '解决时间',
    ai_generated BOOLEAN DEFAULT FALSE COMMENT '是否AI生成',
    ai_confidence_score DECIMAL(3,2) COMMENT 'AI置信度',
    duplicate_of_issue_id BIGINT COMMENT '重复问题ID',
    estimated_effort_hours INT COMMENT '预估工作量(小时)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_review_task_id (review_task_id),
    INDEX idx_status (status),
    INDEX idx_severity (severity),
    INDEX idx_category (category),
    INDEX idx_assigned_to_id (assigned_to_id),
    INDEX idx_reporter_id (reporter_id),
    INDEX idx_issue_number (issue_number),
    FOREIGN KEY (review_task_id) REFERENCES review_tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_to_id) REFERENCES users(id),
    FOREIGN KEY (reporter_id) REFERENCES users(id),
    FOREIGN KEY (duplicate_of_issue_id) REFERENCES review_issues(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评审问题表';

-- 7. 评审评论表扩展
CREATE TABLE IF NOT EXISTS review_comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    review_task_id BIGINT COMMENT '评审任务ID',
    review_issue_id BIGINT COMMENT '关联问题ID',
    parent_comment_id BIGINT COMMENT '父评论ID',
    author_id BIGINT NOT NULL COMMENT '作者ID',
    content TEXT NOT NULL COMMENT '评论内容',
    comment_type ENUM('GENERAL', 'SUGGESTION', 'QUESTION', 'APPROVAL', 'REJECTION') DEFAULT 'GENERAL' COMMENT '评论类型',
    position_info JSON COMMENT '位置信息',
    quoted_text TEXT COMMENT '引用文本',
    ai_generated BOOLEAN DEFAULT FALSE COMMENT '是否AI生成',
    ai_confidence_score DECIMAL(3,2) COMMENT 'AI置信度',
    evidence_links JSON COMMENT '证据链接',
    is_resolved BOOLEAN DEFAULT FALSE COMMENT '是否已解决',
    visibility ENUM('PUBLIC', 'AUTHOR_ONLY', 'REVIEWERS_ONLY') DEFAULT 'PUBLIC' COMMENT '可见性',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_review_task_id (review_task_id),
    INDEX idx_review_issue_id (review_issue_id),
    INDEX idx_parent_comment_id (parent_comment_id),
    INDEX idx_author_id (author_id),
    INDEX idx_comment_type (comment_type),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (review_task_id) REFERENCES review_tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (review_issue_id) REFERENCES review_issues(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_comment_id) REFERENCES review_comments(id),
    FOREIGN KEY (author_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评审评论表';

-- 8. 评审指标表
CREATE TABLE IF NOT EXISTS review_metrics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    review_task_id BIGINT NOT NULL COMMENT '评审任务ID',
    total_issues_found INT DEFAULT 0 COMMENT '发现问题总数',
    critical_issues_count INT DEFAULT 0 COMMENT '严重问题数',
    high_issues_count INT DEFAULT 0 COMMENT '高级问题数',
    medium_issues_count INT DEFAULT 0 COMMENT '中级问题数',
    low_issues_count INT DEFAULT 0 COMMENT '低级问题数',
    issues_resolved_count INT DEFAULT 0 COMMENT '已解决问题数',
    ai_analysis_duration_minutes INT COMMENT 'AI分析耗时(分钟)',
    expert_review_duration_minutes INT COMMENT '专家评审耗时(分钟)',
    total_review_duration_minutes INT COMMENT '总评审耗时(分钟)',
    quality_score DECIMAL(3,1) COMMENT '质量评分',
    compliance_score DECIMAL(3,1) COMMENT '合规评分',
    expert_satisfaction_score DECIMAL(3,1) COMMENT '专家满意度',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_review_task_id (review_task_id),
    FOREIGN KEY (review_task_id) REFERENCES review_tasks(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评审指标表';

-- 9. 任务专家分派表
CREATE TABLE IF NOT EXISTS review_task_assignments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    review_task_id BIGINT NOT NULL COMMENT '评审任务ID',
    expert_id BIGINT NOT NULL COMMENT '专家ID',
    assignment_role ENUM('PRIMARY_REVIEWER', 'SECONDARY_REVIEWER', 'COMPLIANCE_REVIEWER', 'FINAL_APPROVER') NOT NULL COMMENT '分派角色',
    assignment_status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'COMPLETED') DEFAULT 'PENDING' COMMENT '分派状态',
    assigned_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '分派时间',
    accepted_at DATETIME COMMENT '接受时间',
    completed_at DATETIME COMMENT '完成时间',
    assignment_reason TEXT COMMENT '分派原因',
    rejection_reason TEXT COMMENT '拒绝原因',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_review_task_id (review_task_id),
    INDEX idx_expert_id (expert_id),
    INDEX idx_assignment_status (assignment_status),
    INDEX idx_assignment_role (assignment_role),
    FOREIGN KEY (review_task_id) REFERENCES review_tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (expert_id) REFERENCES users(id),
    UNIQUE KEY uk_task_expert_role (review_task_id, expert_id, assignment_role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务专家分派表';

-- 10. 文档版本表
CREATE TABLE IF NOT EXISTS document_versions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    document_id BIGINT NOT NULL COMMENT '文档ID',
    review_task_id BIGINT COMMENT '评审任务ID',
    version_number VARCHAR(50) NOT NULL COMMENT '版本号',
    version_type ENUM('ORIGINAL', 'REVIEW_SNAPSHOT', 'REVISION', 'FINAL') DEFAULT 'ORIGINAL' COMMENT '版本类型',
    file_path VARCHAR(1000) NOT NULL COMMENT '文件路径',
    file_size BIGINT COMMENT '文件大小',
    file_hash VARCHAR(128) COMMENT '文件哈希',
    is_readonly BOOLEAN DEFAULT FALSE COMMENT '是否只读',
    change_log TEXT COMMENT '变更日志',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    
    INDEX idx_document_id (document_id),
    INDEX idx_review_task_id (review_task_id),
    INDEX idx_version_type (version_type),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (review_task_id) REFERENCES review_tasks(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档版本表';

-- 插入默认数据
-- 默认评审工作流模板
INSERT INTO review_workflows (workflow_name, workflow_description, business_type, workflow_type, created_by) VALUES
('标准合同评审流程', '适用于一般商务合同的评审流程', 'CONTRACT', 'LINEAR', 1),
('技术方案评审流程', '适用于技术方案文档的评审流程', 'TECHNICAL_PROPOSAL', 'PARALLEL', 1),
('政策文件评审流程', '适用于政策文件的评审流程', 'POLICY', 'LINEAR', 1);

-- 默认工作流步骤
INSERT INTO review_workflow_steps (workflow_id, step_name, step_order, step_type, sla_hours, is_mandatory) VALUES
(1, 'AI初步分析', 1, 'AI_ANALYSIS', 2, TRUE),
(1, '法务专家评审', 2, 'EXPERT_REVIEW', 24, TRUE),
(1, '业务专家评审', 3, 'EXPERT_REVIEW', 24, TRUE),
(1, '最终审批', 4, 'FINAL_APPROVAL', 12, TRUE);
