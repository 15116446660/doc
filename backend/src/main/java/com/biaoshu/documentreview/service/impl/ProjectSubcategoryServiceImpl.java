package com.biaoshu.documentreview.service.impl;

import com.biaoshu.documentreview.entity.ProjectSubcategory;
import com.biaoshu.documentreview.repository.ProjectSubcategoryRepository;
import com.biaoshu.documentreview.service.ProjectSubcategoryService;
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
 * 项目子品类服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectSubcategoryServiceImpl implements ProjectSubcategoryService {

    private final ProjectSubcategoryRepository subcategoryRepository;

    @Override
    @Transactional
    public ProjectSubcategory createSubcategory(ProjectSubcategory subcategory) {
        log.info("创建子品类: {}", subcategory.getName());
        
        // 检查编码唯一性
        if (StringUtils.hasText(subcategory.getCode()) && 
            subcategoryRepository.existsByCodeAndEnabledTrue(subcategory.getCode())) {
            throw new IllegalArgumentException("子品类编码已存在: " + subcategory.getCode());
        }

        subcategory.setCreatedAt(LocalDateTime.now());
        subcategory.setUpdatedAt(LocalDateTime.now());
        
        return subcategoryRepository.save(subcategory);
    }

    @Override
    @Transactional
    public ProjectSubcategory updateSubcategory(Long id, ProjectSubcategory subcategory) {
        log.info("更新子品类: {}", id);
        
        ProjectSubcategory existingSubcategory = subcategoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("子品类不存在: " + id));

        // 检查编码唯一性
        if (StringUtils.hasText(subcategory.getCode()) && 
            subcategoryRepository.existsByCodeAndIdNot(subcategory.getCode(), id)) {
            throw new IllegalArgumentException("子品类编码已存在: " + subcategory.getCode());
        }

        // 更新字段
        existingSubcategory.setName(subcategory.getName());
        existingSubcategory.setCode(subcategory.getCode());
        existingSubcategory.setDescription(subcategory.getDescription());
        existingSubcategory.setManagerId(subcategory.getManagerId());
        existingSubcategory.setSortOrder(subcategory.getSortOrder());
        existingSubcategory.setIcon(subcategory.getIcon());
        existingSubcategory.setColor(subcategory.getColor());
        existingSubcategory.setUpdatedAt(LocalDateTime.now());

        return subcategoryRepository.save(existingSubcategory);
    }

    @Override
    @Transactional
    public void deleteSubcategory(Long id) {
        log.info("删除子品类: {}", id);
        
        ProjectSubcategory subcategory = subcategoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("子品类不存在: " + id));

        // 软删除
        subcategory.setEnabled(false);
        subcategory.setUpdatedAt(LocalDateTime.now());
        subcategoryRepository.save(subcategory);
    }

    @Override
    public Optional<ProjectSubcategory> findById(Long id) {
        return subcategoryRepository.findById(id);
    }

    @Override
    public Page<ProjectSubcategory> findAll(Pageable pageable) {
        return subcategoryRepository.findAll(pageable);
    }

    @Override
    public List<ProjectSubcategory> findByCategoryId(Long categoryId) {
        return subcategoryRepository.findByCategoryIdAndEnabledTrueOrderBySortOrderAsc(categoryId);
    }

    @Override
    public List<ProjectSubcategory> searchByName(String name) {
        return subcategoryRepository.findByNameContainingAndEnabledTrueOrderBySortOrderAsc(name);
    }

    @Override
    public Optional<ProjectSubcategory> findByCode(String code) {
        return subcategoryRepository.findByCodeAndEnabledTrue(code);
    }

    @Override
    public boolean existsByCode(String code) {
        return subcategoryRepository.existsByCodeAndEnabledTrue(code);
    }

    @Override
    public boolean existsByCodeAndIdNot(String code, Long id) {
        return subcategoryRepository.existsByCodeAndIdNot(code, id);
    }

    @Override
    @Transactional
    public void updateSortOrder(Long subcategoryId, Integer sortOrder) {
        log.info("更新子品类排序: {} -> {}", subcategoryId, sortOrder);
        
        ProjectSubcategory subcategory = subcategoryRepository.findById(subcategoryId)
            .orElseThrow(() -> new IllegalArgumentException("子品类不存在: " + subcategoryId));

        subcategory.setSortOrder(sortOrder);
        subcategory.setUpdatedAt(LocalDateTime.now());
        subcategoryRepository.save(subcategory);
    }

    @Override
    @Transactional
    public ProjectSubcategory toggleEnabled(Long id, Boolean enabled) {
        log.info("切换子品类状态: {} -> {}", id, enabled);
        
        ProjectSubcategory subcategory = subcategoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("子品类不存在: " + id));

        subcategory.setEnabled(enabled);
        subcategory.setUpdatedAt(LocalDateTime.now());
        return subcategoryRepository.save(subcategory);
    }

    @Override
    public long countByCategoryId(Long categoryId) {
        return subcategoryRepository.countByCategoryId(categoryId);
    }

    @Override
    public List<ProjectSubcategory> findByCategoryIds(List<Long> categoryIds) {
        return subcategoryRepository.findByCategoryIdInAndEnabledTrue(categoryIds);
    }

    @Override
    public List<ProjectSubcategory> findSubcategoriesWithProjects(Long categoryId) {
        return subcategoryRepository.findByCategoryIdAndEnabledTrueOrderBySortOrderAsc(categoryId);
    }

    @Override
    public List<ProjectSubcategory> findByDepartmentId(Long departmentId) {
        return subcategoryRepository.findByDepartmentIdThroughCategory(departmentId);
    }
}
