package com.biaoshu.documentreview.service;

import com.biaoshu.documentreview.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 部门服务接口
 */
public interface DepartmentService {

    /**
     * 创建部门
     */
    Department createDepartment(Department department);

    /**
     * 更新部门
     */
    Department updateDepartment(Long id, Department department);

    /**
     * 删除部门（软删除）
     */
    void deleteDepartment(Long id);

    /**
     * 根据ID查询部门
     */
    Optional<Department> findById(Long id);

    /**
     * 查询所有部门（分页）
     */
    Page<Department> findAll(Pageable pageable);

    /**
     * 查询部门树形结构
     */
    List<Department> getDepartmentTree();

    /**
     * 根据父部门ID查询子部门
     */
    List<Department> findByParentId(Long parentId);

    /**
     * 查询根部门列表
     */
    List<Department> findRootDepartments();

    /**
     * 根据部门名称搜索
     */
    List<Department> searchByName(String name);

    /**
     * 根据部门编码查询
     */
    Optional<Department> findByCode(String code);

    /**
     * 检查部门编码是否存在
     */
    boolean existsByCode(String code);

    /**
     * 检查部门编码是否存在（排除指定ID）
     */
    boolean existsByCodeAndIdNot(String code, Long id);

    /**
     * 移动部门到新的父部门下
     */
    Department moveDepartment(Long departmentId, Long newParentId);

    /**
     * 更新部门排序
     */
    void updateSortOrder(Long departmentId, Integer sortOrder);

    /**
     * 启用/禁用部门
     */
    Department toggleEnabled(Long id, Boolean enabled);

    /**
     * 获取部门的完整路径
     */
    String getDepartmentPath(Long departmentId);

    /**
     * 查询指定部门的所有子部门（递归）
     */
    List<Department> findAllChildren(Long departmentId);

    /**
     * 统计部门下的子部门数量
     */
    long countChildren(Long departmentId);
}
