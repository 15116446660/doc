package com.biaoshu.documentreview.repository;

import com.biaoshu.documentreview.entity.Document;
import com.biaoshu.documentreview.enums.DocumentStatus;
import com.biaoshu.documentreview.enums.DocumentType;
import com.biaoshu.documentreview.enums.SecurityLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 文档Repository
 * 
 * @author biaoshu
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {

    /**
     * 根据文档编号查找文档
     */
    Optional<Document> findByDocumentNumberAndIsDeletedFalse(String documentNumber);

    /**
     * 根据状态查找文档
     */
    Page<Document> findByStatusAndIsDeletedFalse(DocumentStatus status, Pageable pageable);

    /**
     * 根据作者查找文档
     */
    Page<Document> findByAuthorIdAndIsDeletedFalse(Long authorId, Pageable pageable);

    /**
     * 根据文档类型查找文档
     */
    Page<Document> findByDocumentTypeAndIsDeletedFalse(DocumentType documentType, Pageable pageable);

    /**
     * 根据安全级别查找文档
     */
    Page<Document> findBySecurityLevelAndIsDeletedFalse(SecurityLevel securityLevel, Pageable pageable);

    /**
     * 根据部门查找文档
     */
    Page<Document> findByDepartmentIdAndIsDeletedFalse(Long departmentId, Pageable pageable);

    /**
     * 根据项目查找文档
     */
    Page<Document> findByProjectIdAndIsDeletedFalse(Long projectId, Pageable pageable);

    /**
     * 根据标题模糊查询文档
     */
    @Query("SELECT d FROM Document d WHERE d.title LIKE %:title% AND d.isDeleted = false")
    Page<Document> findByTitleContaining(@Param("title") String title, Pageable pageable);

    /**
     * 根据业务编号查找文档
     */
    List<Document> findByBusinessNumberAndIsDeletedFalse(String businessNumber);

    /**
     * 查找锁定的文档
     */
    @Query("SELECT d FROM Document d WHERE d.isLocked = true AND d.lockedBy = :userId AND d.isDeleted = false")
    List<Document> findLockedByUser(@Param("userId") Long userId);

    /**
     * 查找超时锁定的文档
     */
    @Query("SELECT d FROM Document d WHERE d.isLocked = true AND d.lockedAt < :timeoutTime AND d.isDeleted = false")
    List<Document> findTimeoutLockedDocuments(@Param("timeoutTime") LocalDateTime timeoutTime);

    /**
     * 查找即将过期的文档
     */
    @Query("SELECT d FROM Document d WHERE d.expiredAt BETWEEN :startTime AND :endTime AND d.isDeleted = false")
    List<Document> findExpiringDocuments(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计各状态文档数量
     */
    @Query("SELECT d.status, COUNT(d) FROM Document d WHERE d.isDeleted = false GROUP BY d.status")
    List<Object[]> countByStatus();

    /**
     * 统计各类型文档数量
     */
    @Query("SELECT d.documentType, COUNT(d) FROM Document d WHERE d.isDeleted = false GROUP BY d.documentType")
    List<Object[]> countByType();

    /**
     * 统计用户文档数量
     */
    @Query("SELECT COUNT(d) FROM Document d WHERE d.authorId = :authorId AND d.isDeleted = false")
    long countByAuthorId(@Param("authorId") Long authorId);

    /**
     * 统计部门文档数量
     */
    @Query("SELECT COUNT(d) FROM Document d WHERE d.departmentId = :departmentId AND d.isDeleted = false")
    long countByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * 查找需要审核的文档
     */
    @Query("SELECT d FROM Document d WHERE d.status = 'REVIEWING' AND d.isDeleted = false")
    List<Document> findDocumentsNeedingReview();

    /**
     * 根据文件哈希查找文档
     */
    List<Document> findByFileHashAndIsDeletedFalse(String fileHash);

    /**
     * 检查文档编号是否存在
     */
    boolean existsByDocumentNumberAndIsDeletedFalse(String documentNumber);
}
