-- 创建数据库
CREATE DATABASE IF NOT EXISTS document_review_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE document_review_dev;

-- 创建用户（如果需要）
-- CREATE USER 'document_review'@'localhost' IDENTIFIED BY 'password123';
-- GRANT ALL PRIVILEGES ON document_review_dev.* TO 'document_review'@'localhost';
-- FLUSH PRIVILEGES;

-- 创建部门表
CREATE TABLE IF NOT EXISTS department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '部门名称',
    code VARCHAR(50) UNIQUE COMMENT '部门编码',
    description VARCHAR(500) COMMENT '部门描述',
    manager_id BIGINT COMMENT '部门负责人ID',
    parent_id BIGINT COMMENT '父部门ID',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    level INT DEFAULT 1 COMMENT '部门层级',
    path VARCHAR(500) COMMENT '部门路径，用于快速查询层级关系',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    INDEX idx_parent_id (parent_id),
    INDEX idx_manager_id (manager_id),
    INDEX idx_code (code),
    INDEX idx_enabled (enabled),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

-- 创建项目品类表
CREATE TABLE IF NOT EXISTS project_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '品类名称',
    code VARCHAR(50) UNIQUE COMMENT '品类编码',
    description VARCHAR(500) COMMENT '品类描述',
    department_id BIGINT NOT NULL COMMENT '所属部门ID',
    manager_id BIGINT COMMENT '品类负责人ID',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    icon VARCHAR(100) COMMENT '品类图标',
    color VARCHAR(20) COMMENT '品类颜色',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    INDEX idx_department_id (department_id),
    INDEX idx_manager_id (manager_id),
    INDEX idx_code (code),
    INDEX idx_enabled (enabled),
    INDEX idx_sort_order (sort_order),
    FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目品类表';

-- 创建项目子品类表
CREATE TABLE IF NOT EXISTS project_subcategory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '子品类名称',
    code VARCHAR(50) UNIQUE COMMENT '子品类编码',
    description VARCHAR(500) COMMENT '子品类描述',
    category_id BIGINT NOT NULL COMMENT '所属品类ID',
    manager_id BIGINT COMMENT '子品类负责人ID',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    icon VARCHAR(100) COMMENT '子品类图标',
    color VARCHAR(20) COMMENT '子品类颜色',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    INDEX idx_category_id (category_id),
    INDEX idx_manager_id (manager_id),
    INDEX idx_code (code),
    INDEX idx_enabled (enabled),
    INDEX idx_sort_order (sort_order),
    FOREIGN KEY (category_id) REFERENCES project_category(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目子品类表';

-- 更新项目表，添加子品类关联
ALTER TABLE project ADD COLUMN subcategory_id BIGINT COMMENT '所属子品类ID';
ALTER TABLE project ADD COLUMN sort_order INT DEFAULT 0 COMMENT '排序号';
ALTER TABLE project ADD INDEX idx_subcategory_id (subcategory_id);
ALTER TABLE project ADD CONSTRAINT fk_project_subcategory FOREIGN KEY (subcategory_id) REFERENCES project_subcategory(id) ON DELETE SET NULL;
