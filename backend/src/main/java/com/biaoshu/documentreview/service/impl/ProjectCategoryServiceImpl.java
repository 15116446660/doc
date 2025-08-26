package com.biaoshu.documentreview.service.impl;

import com.biaoshu.documentreview.entity.ProjectCategory;
import com.biaoshu.documentreview.repository.ProjectCategoryRepository;
import com.biaoshu.documentreview.service.ProjectCategoryService;
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
 * 项目品类服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectCategoryServiceImpl implements ProjectCategoryService {

    private final ProjectCategoryRepository categoryRepository;

    @Override
    @Transactional
    public ProjectCategory createCategory(ProjectCategory category) {
        log.info("创建品类: {}", category.getName());
        
        // 检查编码唯一性
        if (StringUtils.hasText(category.getCode()) && 
            categoryRepository.existsByCodeAndEnabledTrue(category.getCode())) {
            throw new IllegalArgumentException("品类编码已存在: " + category.getCode());
        }

        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public ProjectCategory updateCategory(Long id, ProjectCategory category) {
        log.info("更新品类: {}", id);
        
        ProjectCategory existingCategory = categoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("品类不存在: " + id));

        // 检查编码唯一性
        if (StringUtils.hasText(category.getCode()) && 
            categoryRepository.existsByCodeAndIdNot(category.getCode(), id)) {
            throw new IllegalArgumentException("品类编码已存在: " + category.getCode());
        }

        // 更新字段
        existingCategory.setName(category.getName());
        existingCategory.setCode(category.getCode());
        existingCategory.setDescription(category.getDescription());
        existingCategory.setManagerId(category.getManagerId());
        existingCategory.setSortOrder(category.getSortOrder());
        existingCategory.setIcon(category.getIcon());
        existingCategory.setColor(category.getColor());
        existingCategory.setUpdatedAt(LocalDateTime.now());

        return categoryRepository.save(existingCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        log.info("删除品类: {}", id);
        
        ProjectCategory category = categoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("品类不存在: " + id));

        // 软删除
        category.setEnabled(false);
        category.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(category);
    }

    @Override
    public Optional<ProjectCategory> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Page<ProjectCategory> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Override
    public List<ProjectCategory> findByDepartmentId(Long departmentId) {
        return categoryRepository.findByDepartmentIdAndEnabledTrueOrderBySortOrderAsc(departmentId);
    }

    @Override
    public List<ProjectCategory> searchByName(String name) {
        return categoryRepository.findByNameContainingAndEnabledTrueOrderBySortOrderAsc(name);
    }

    @Override
    public Optional<ProjectCategory> findByCode(String code) {
        return categoryRepository.findByCodeAndEnabledTrue(code);
    }

    @Override
    public boolean existsByCode(String code) {
        return categoryRepository.existsByCodeAndEnabledTrue(code);
    }

    @Override
    public boolean existsByCodeAndIdNot(String code, Long id) {
        return categoryRepository.existsByCodeAndIdNot(code, id);
    }

    @Override
    @Transactional
    public void updateSortOrder(Long categoryId, Integer sortOrder) {
        log.info("更新品类排序: {} -> {}", categoryId, sortOrder);
        
        ProjectCategory category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("品类不存在: " + categoryId));

        category.setSortOrder(sortOrder);
        category.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public ProjectCategory toggleEnabled(Long id, Boolean enabled) {
        log.info("切换品类状态: {} -> {}", id, enabled);
        
        ProjectCategory category = categoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("品类不存在: " + id));

        category.setEnabled(enabled);
        category.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(category);
    }

    @Override
    public long countByDepartmentId(Long departmentId) {
        return categoryRepository.countByDepartmentId(departmentId);
    }

    @Override
    public List<ProjectCategory> findByDepartmentIds(List<Long> departmentIds) {
        return categoryRepository.findByDepartmentIdInAndEnabledTrue(departmentIds);
    }

    @Override
    public List<ProjectCategory> findCategoriesWithSubcategories(Long departmentId) {
        return categoryRepository.findByDepartmentIdAndEnabledTrueOrderBySortOrderAsc(departmentId);
    }
}
