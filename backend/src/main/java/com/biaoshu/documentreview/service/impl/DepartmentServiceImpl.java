package com.biaoshu.documentreview.service.impl;

import com.biaoshu.documentreview.entity.Department;
import com.biaoshu.documentreview.repository.DepartmentRepository;
import com.biaoshu.documentreview.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 部门服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public Department createDepartment(Department department) {
        log.info("创建部门: {}", department.getName());
        
        // 检查编码唯一性
        if (StringUtils.hasText(department.getCode()) && 
            departmentRepository.existsByCodeAndEnabledTrue(department.getCode())) {
            throw new IllegalArgumentException("部门编码已存在: " + department.getCode());
        }

        // 设置层级和路径
        if (department.getParentId() != null) {
            Optional<Department> parent = departmentRepository.findById(department.getParentId());
            if (parent.isPresent()) {
                department.setLevel(parent.get().getLevel() + 1);
                department.setPath(parent.get().getPath() + "/" + department.getCode());
            }
        } else {
            department.setLevel(1);
            department.setPath("/" + department.getCode());
        }

        department.setCreatedAt(LocalDateTime.now());
        department.setUpdatedAt(LocalDateTime.now());
        
        return departmentRepository.save(department);
    }

    @Override
    @Transactional
    public Department updateDepartment(Long id, Department department) {
        log.info("更新部门: {}", id);
        
        Department existingDepartment = departmentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("部门不存在: " + id));

        // 检查编码唯一性
        if (StringUtils.hasText(department.getCode()) && 
            departmentRepository.existsByCodeAndIdNot(department.getCode(), id)) {
            throw new IllegalArgumentException("部门编码已存在: " + department.getCode());
        }

        // 更新字段
        existingDepartment.setName(department.getName());
        existingDepartment.setCode(department.getCode());
        existingDepartment.setDescription(department.getDescription());
        existingDepartment.setManagerId(department.getManagerId());
        existingDepartment.setSortOrder(department.getSortOrder());
        existingDepartment.setUpdatedAt(LocalDateTime.now());

        return departmentRepository.save(existingDepartment);
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        log.info("删除部门: {}", id);
        
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("部门不存在: " + id));

        // 检查是否有子部门
        long childrenCount = departmentRepository.countChildrenByParentId(id);
        if (childrenCount > 0) {
            throw new IllegalArgumentException("存在子部门，无法删除");
        }

        // 软删除
        department.setEnabled(false);
        department.setUpdatedAt(LocalDateTime.now());
        departmentRepository.save(department);
    }

    @Override
    public Optional<Department> findById(Long id) {
        return departmentRepository.findById(id);
    }

    @Override
    public Page<Department> findAll(Pageable pageable) {
        return departmentRepository.findAll(pageable);
    }

    @Override
    public List<Department> getDepartmentTree() {
        return departmentRepository.findDepartmentTree();
    }

    @Override
    public List<Department> findByParentId(Long parentId) {
        return departmentRepository.findByParentIdAndEnabledTrueOrderBySortOrderAsc(parentId);
    }

    @Override
    public List<Department> findRootDepartments() {
        return departmentRepository.findByParentIdIsNullAndEnabledTrueOrderBySortOrderAsc();
    }

    @Override
    public List<Department> searchByName(String name) {
        return departmentRepository.findByNameContainingAndEnabledTrueOrderBySortOrderAsc(name);
    }

    @Override
    public Optional<Department> findByCode(String code) {
        return departmentRepository.findByCodeAndEnabledTrue(code);
    }

    @Override
    public boolean existsByCode(String code) {
        return departmentRepository.existsByCodeAndEnabledTrue(code);
    }

    @Override
    public boolean existsByCodeAndIdNot(String code, Long id) {
        return departmentRepository.existsByCodeAndIdNot(code, id);
    }

    @Override
    @Transactional
    public Department moveDepartment(Long departmentId, Long newParentId) {
        log.info("移动部门: {} 到父部门: {}", departmentId, newParentId);
        
        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new IllegalArgumentException("部门不存在: " + departmentId));

        // 更新父部门
        department.setParentId(newParentId);
        
        // 重新计算层级和路径
        if (newParentId != null) {
            Optional<Department> parent = departmentRepository.findById(newParentId);
            if (parent.isPresent()) {
                department.setLevel(parent.get().getLevel() + 1);
                department.setPath(parent.get().getPath() + "/" + department.getCode());
            }
        } else {
            department.setLevel(1);
            department.setPath("/" + department.getCode());
        }

        department.setUpdatedAt(LocalDateTime.now());
        return departmentRepository.save(department);
    }

    @Override
    @Transactional
    public void updateSortOrder(Long departmentId, Integer sortOrder) {
        log.info("更新部门排序: {} -> {}", departmentId, sortOrder);
        
        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new IllegalArgumentException("部门不存在: " + departmentId));

        department.setSortOrder(sortOrder);
        department.setUpdatedAt(LocalDateTime.now());
        departmentRepository.save(department);
    }

    @Override
    @Transactional
    public Department toggleEnabled(Long id, Boolean enabled) {
        log.info("切换部门状态: {} -> {}", id, enabled);
        
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("部门不存在: " + id));

        department.setEnabled(enabled);
        department.setUpdatedAt(LocalDateTime.now());
        return departmentRepository.save(department);
    }

    @Override
    public String getDepartmentPath(Long departmentId) {
        Optional<Department> department = departmentRepository.findById(departmentId);
        return department.map(Department::getPath).orElse("");
    }

    @Override
    public List<Department> findAllChildren(Long departmentId) {
        Optional<Department> department = departmentRepository.findById(departmentId);
        if (department.isPresent()) {
            return departmentRepository.findAllChildrenByPath(department.get().getPath());
        }
        return List.of();
    }

    @Override
    public long countChildren(Long departmentId) {
        return departmentRepository.countChildrenByParentId(departmentId);
    }
}
