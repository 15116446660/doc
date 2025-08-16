package com.biaoshu.documentreview.repository;

import com.biaoshu.documentreview.entity.ReviewTemplate;
import com.biaoshu.documentreview.enums.DocumentType;
import com.biaoshu.documentreview.enums.ReviewTemplateStatus;
import com.biaoshu.documentreview.enums.SecurityLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 审核模板Repository
 * 
 * @author biaoshu
 */
@Repository
public interface ReviewTemplateRepository extends JpaRepository<ReviewTemplate, Long>, JpaSpecificationExecutor<ReviewTemplate> {

    /**
     * 根据模板编码查找模板
     */
    Optional<ReviewTemplate> findByTemplateCodeAndIsDeletedFalse(String templateCode);

    /**
     * 根据状态查找模板
     */
    Page<ReviewTemplate> findByStatusAndIsDeletedFalse(ReviewTemplateStatus status, Pageable pageable);

    /**
     * 根据文档类型查找模板
     */
    List<ReviewTemplate> findByDocumentTypeAndIsDeletedFalse(DocumentType documentType);

    /**
     * 根据安全级别查找模板
     */
    List<ReviewTemplate> findBySecurityLevelAndIsDeletedFalse(SecurityLevel securityLevel);

    /**
     * 根据部门查找模板
     */
    List<ReviewTemplate> findByDepartmentIdAndIsDeletedFalse(Long departmentId);

    /**
     * 查找启用的模板
     */
    List<ReviewTemplate> findByIsEnabledTrueAndIsDeletedFalse();

    /**
     * 查找默认模板
     */
    List<ReviewTemplate> findByIsDefaultTrueAndIsDeletedFalse();

    /**
     * 根据文档类型和安全级别查找模板
     */
    @Query("SELECT rt FROM ReviewTemplate rt WHERE " +
           "(rt.documentType = :documentType OR rt.documentType IS NULL) AND " +
           "(rt.securityLevel = :securityLevel OR rt.securityLevel IS NULL) AND " +
           "(rt.departmentId = :departmentId OR rt.departmentId IS NULL) AND " +
           "rt.isEnabled = true AND rt.status = 'PUBLISHED' AND rt.isDeleted = false " +
           "ORDER BY rt.isDefault DESC, rt.sortOrder ASC")
    List<ReviewTemplate> findMatchingTemplates(@Param("documentType") DocumentType documentType,
                                             @Param("securityLevel") SecurityLevel securityLevel,
                                             @Param("departmentId") Long departmentId);

    /**
     * 查找最佳匹配的模板
     */
    @Query("SELECT rt FROM ReviewTemplate rt WHERE " +
           "rt.documentType = :documentType AND " +
           "rt.securityLevel = :securityLevel AND " +
           "rt.departmentId = :departmentId AND " +
           "rt.isEnabled = true AND rt.status = 'PUBLISHED' AND rt.isDeleted = false " +
           "ORDER BY rt.isDefault DESC, rt.sortOrder ASC")
    Optional<ReviewTemplate> findBestMatchTemplate(@Param("documentType") DocumentType documentType,
                                                  @Param("securityLevel") SecurityLevel securityLevel,
                                                  @Param("departmentId") Long departmentId);

    /**
     * 根据模板名称模糊查询
     */
    @Query("SELECT rt FROM ReviewTemplate rt WHERE rt.templateName LIKE %:templateName% AND rt.isDeleted = false")
    Page<ReviewTemplate> findByTemplateNameContaining(@Param("templateName") String templateName, Pageable pageable);

    /**
     * 统计各状态模板数量
     */
    @Query("SELECT rt.status, COUNT(rt) FROM ReviewTemplate rt WHERE rt.isDeleted = false GROUP BY rt.status")
    List<Object[]> countByStatus();

    /**
     * 统计各文档类型模板数量
     */
    @Query("SELECT rt.documentType, COUNT(rt) FROM ReviewTemplate rt WHERE rt.isDeleted = false GROUP BY rt.documentType")
    List<Object[]> countByDocumentType();

    /**
     * 检查模板编码是否存在
     */
    boolean existsByTemplateCodeAndIsDeletedFalse(String templateCode);

    /**
     * 查找可用的模板（已发布且启用）
     */
    @Query("SELECT rt FROM ReviewTemplate rt WHERE rt.status = 'PUBLISHED' AND rt.isEnabled = true AND rt.isDeleted = false ORDER BY rt.sortOrder ASC")
    List<ReviewTemplate> findAvailableTemplates();

    /**
     * 根据创建人查找模板
     */
    Page<ReviewTemplate> findByCreatedByAndIsDeletedFalse(Long createdBy, Pageable pageable);
}
