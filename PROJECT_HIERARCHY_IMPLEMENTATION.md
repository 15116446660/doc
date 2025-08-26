# 项目管理四级层级结构重构实现文档

## 概述

本文档描述了AI文档处理系统中项目管理模块的四级层级结构重构实现，包括部门-品类-子品类-项目的完整层级管理体系。

## 系统架构

### 层级结构设计
```
第一级：部门（Department）
├── 第二级：品类（Category）
    ├── 第三级：子品类（Subcategory）
        ├── 第四级：项目（Project）
```

### 技术栈
- **前端**: Vue3 + TypeScript + Element Plus + Pinia
- **后端**: Spring Boot + JPA + MySQL
- **权限**: 基于注解的权限控制系统

## 数据库设计

### 核心表结构

#### 1. 部门表 (department)
```sql
CREATE TABLE department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '部门名称',
    code VARCHAR(50) UNIQUE COMMENT '部门编码',
    description VARCHAR(500) COMMENT '部门描述',
    manager_id BIGINT COMMENT '部门负责人ID',
    parent_id BIGINT COMMENT '父部门ID',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    level INT DEFAULT 1 COMMENT '部门层级',
    path VARCHAR(500) COMMENT '部门路径',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);
```

#### 2. 项目品类表 (project_category)
```sql
CREATE TABLE project_category (
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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE CASCADE
);
```

#### 3. 项目子品类表 (project_subcategory)
```sql
CREATE TABLE project_subcategory (
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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES project_category(id) ON DELETE CASCADE
);
```

#### 4. 项目表更新
```sql
-- 为现有项目表添加子品类关联
ALTER TABLE project ADD COLUMN subcategory_id BIGINT COMMENT '所属子品类ID';
ALTER TABLE project ADD COLUMN sort_order INT DEFAULT 0 COMMENT '排序号';
ALTER TABLE project ADD CONSTRAINT fk_project_subcategory 
    FOREIGN KEY (subcategory_id) REFERENCES project_subcategory(id) ON DELETE SET NULL;
```

## 后端实现

### 1. 实体类设计
- `Department.java` - 部门实体，支持自关联层级结构
- `ProjectCategory.java` - 品类实体，关联部门
- `ProjectSubcategory.java` - 子品类实体，关联品类
- `Project.java` - 项目实体，关联子品类

### 2. Repository层
- `DepartmentRepository` - 部门数据访问，支持层级查询
- `ProjectCategoryRepository` - 品类数据访问
- `ProjectSubcategoryRepository` - 子品类数据访问
- 扩展现有 `ProjectRepository`

### 3. Service层
- `DepartmentService` - 部门业务逻辑
- `ProjectCategoryService` - 品类业务逻辑
- `ProjectSubcategoryService` - 子品类业务逻辑
- `ProjectHierarchyService` - 层级管理综合服务

### 4. Controller层
- `DepartmentController` - 部门管理API
- `ProjectCategoryController` - 品类管理API
- `ProjectSubcategoryController` - 子品类管理API
- `ProjectHierarchyController` - 层级管理综合API

### 5. 权限控制
- `HierarchyPermission` - 权限枚举定义
- `@RequireHierarchyPermission` - 权限检查注解
- `HierarchyPermissionAspect` - 权限检查切面
- `HierarchyPermissionService` - 权限服务

## 前端实现

### 1. API接口层
- 扩展 `project.ts`，添加四级层级管理API
- 支持CRUD操作、层级查询、树形结构返回

### 2. 组件设计
- `ProjectHierarchyTree.vue` - 主要的树形组件
- `ProjectHierarchyFormDialog.vue` - 创建/编辑表单对话框
- 各层级专门管理页面

### 3. 页面结构
```
/project/hierarchy - 层级管理主页面
/project/department - 部门管理页面
/project/category - 品类管理页面
/project/subcategory - 子品类管理页面（待实现）
```

### 4. 权限控制
- `usePermission.ts` - 权限管理组合式函数
- 权限指令支持
- 基于角色的UI控制

## 核心功能特性

### 1. 树形结构管理
- ✅ 四级层级展示
- ✅ 展开/收缩控制
- ✅ 拖拽排序支持
- ✅ 搜索筛选功能

### 2. CRUD操作
- ✅ 创建各层级节点
- ✅ 编辑节点信息
- ✅ 删除节点（带约束检查）
- ✅ 批量操作支持

### 3. 权限控制
- ✅ 基于角色的功能权限
- ✅ 基于数据的操作权限
- ✅ 细粒度权限控制
- ✅ 前后端权限一致性

### 4. 数据完整性
- ✅ 外键约束
- ✅ 级联删除控制
- ✅ 数据验证
- ✅ 事务支持

### 5. 用户体验
- ✅ 响应式设计
- ✅ 加载状态提示
- ✅ 错误处理
- ✅ 操作反馈

## 路由配置

### 新增路由
```typescript
{
  path: 'hierarchy',
  name: 'ProjectHierarchy',
  component: () => import('../views/project/hierarchy/index.vue'),
  meta: { title: '层级管理', icon: 'Operation' }
},
{
  path: 'department',
  name: 'ProjectDepartment', 
  component: () => import('../views/project/department/index.vue'),
  meta: { title: '部门管理', icon: 'OfficeBuilding' }
},
{
  path: 'category',
  name: 'ProjectCategory',
  component: () => import('../views/project/category/index.vue'),
  meta: { title: '品类管理', icon: 'Collection' }
}
```

## 权限体系

### 权限分类
1. **功能权限**: 控制用户可以访问的功能模块
2. **数据权限**: 控制用户可以操作的具体数据
3. **操作权限**: 控制用户可以执行的具体操作

### 权限级别
- `view` - 查看权限
- `create` - 创建权限
- `update` - 更新权限
- `delete` - 删除权限
- `manage` - 管理权限（包含所有操作）

## 部署说明

### 数据库迁移
1. 执行 `backend/create_database.sql` 中的建表语句
2. 确保外键约束正确设置
3. 初始化基础数据（如默认部门）

### 后端部署
1. 确保Spring Boot应用包含所有新增的类
2. 配置权限相关的Bean
3. 更新API文档

### 前端部署
1. 安装新增的依赖
2. 确保路由配置正确
3. 配置权限相关的全局组件

## 测试建议

### 单元测试
- Repository层的数据访问测试
- Service层的业务逻辑测试
- 权限检查逻辑测试

### 集成测试
- API接口测试
- 权限控制测试
- 数据完整性测试

### 前端测试
- 组件功能测试
- 用户交互测试
- 权限控制测试

## 后续优化

### 性能优化
- 树形数据的懒加载
- 大数据量的分页处理
- 缓存策略优化

### 功能扩展
- 批量导入/导出
- 历史版本管理
- 审计日志记录
- 数据统计分析

### 用户体验
- 更丰富的交互动画
- 快捷键支持
- 个性化设置
- 移动端适配

## 总结

本次重构成功实现了项目管理的四级层级结构，提供了完整的CRUD操作、权限控制和用户友好的界面。系统具有良好的扩展性和维护性，为后续的功能扩展奠定了坚实的基础。
