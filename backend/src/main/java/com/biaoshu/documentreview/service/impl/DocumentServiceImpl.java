package com.biaoshu.documentreview.service.impl;

import com.biaoshu.documentreview.dto.DocumentCreateDTO;
import com.biaoshu.documentreview.dto.DocumentQueryDTO;
import com.biaoshu.documentreview.dto.DocumentUpdateDTO;
import com.biaoshu.documentreview.dto.DocumentVO;
import com.biaoshu.documentreview.entity.Document;
import com.biaoshu.documentreview.entity.User;
import com.biaoshu.documentreview.enums.DocumentStatus;
import com.biaoshu.documentreview.enums.DocumentType;
import com.biaoshu.documentreview.enums.SecurityLevel;
import com.biaoshu.documentreview.exception.BusinessException;
import com.biaoshu.documentreview.repository.DocumentRepository;
import com.biaoshu.documentreview.repository.UserRepository;
import com.biaoshu.documentreview.service.DocumentService;
import com.biaoshu.documentreview.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 文档服务实现类
 * 
 * @author biaoshu
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Override
    public Page<DocumentVO> getDocuments(DocumentQueryDTO queryDTO, Pageable pageable) {
        Specification<Document> spec = buildSpecification(queryDTO);
        Page<Document> documents = documentRepository.findAll(spec, pageable);
        return documents.map(this::convertToVO);
    }

    @Override
    public DocumentVO getDocumentById(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文档不存在"));
        return convertToVO(document);
    }

    @Override
    @Transactional
    public DocumentVO createDocument(DocumentCreateDTO createDTO) {
        Document document = new Document();
        BeanUtils.copyProperties(createDTO, document);
        
        // 生成文档编号
        document.setDocumentNumber(generateDocumentNumber());
        
        // 设置默认值
        document.setStatus(DocumentStatus.DRAFT);
        document.setCurrentVersion(1);
        document.setIsLocked(false);
        document.setIsDeleted(false);
        
        // TODO: 从当前用户上下文获取作者ID
        // 临时使用默认用户ID，如果不存在则创建
        document.setAuthorId(getOrCreateDefaultUser());
        
        document = documentRepository.save(document);
        return convertToVO(document);
    }

    @Override
    @Transactional
    public DocumentVO uploadDocument(MultipartFile file, String title, String documentType, 
                                   String summary, String securityLevel, Long projectId, String tags) {
        try {
            // 保存文件
            String filePath = fileStorageService.storeFile(file);
            
            // 创建文档记录
            Document document = new Document();
            document.setDocumentNumber(generateDocumentNumber());
            document.setTitle(title);
            document.setSummary(summary);
            document.setDocumentType(DocumentType.valueOf(documentType));
            document.setSecurityLevel(SecurityLevel.valueOf(securityLevel));
            document.setProjectId(projectId);
            document.setTags(tags);
            document.setFilePath(filePath);
            document.setFileSize(file.getSize());
            document.setFileHash(calculateFileHash(file));
            document.setStatus(DocumentStatus.DRAFT);
            document.setCurrentVersion(1);
            document.setIsLocked(false);
            document.setIsDeleted(false);
            
            // TODO: 从当前用户上下文获取作者ID
            // 临时使用默认用户ID，如果不存在则创建
            document.setAuthorId(getOrCreateDefaultUser());
            
            document = documentRepository.save(document);
            return convertToVO(document);
            
        } catch (Exception e) {
            log.error("上传文档失败", e);
            throw new BusinessException("上传文档失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public DocumentVO updateDocument(Long id, DocumentUpdateDTO updateDTO) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文档不存在"));
        
        // 检查是否被锁定
        if (Boolean.TRUE.equals(document.getIsLocked())) {
            throw new BusinessException("文档已被锁定，无法编辑");
        }
        
        // 更新字段
        if (StringUtils.hasText(updateDTO.getTitle())) {
            document.setTitle(updateDTO.getTitle());
        }
        if (StringUtils.hasText(updateDTO.getSummary())) {
            document.setSummary(updateDTO.getSummary());
        }
        if (updateDTO.getDocumentType() != null) {
            document.setDocumentType(updateDTO.getDocumentType());
        }
        if (updateDTO.getStatus() != null) {
            document.setStatus(updateDTO.getStatus());
        }
        if (updateDTO.getSecurityLevel() != null) {
            document.setSecurityLevel(updateDTO.getSecurityLevel());
        }
        if (updateDTO.getProjectId() != null) {
            document.setProjectId(updateDTO.getProjectId());
        }
        if (updateDTO.getDepartmentId() != null) {
            document.setDepartmentId(updateDTO.getDepartmentId());
        }
        if (StringUtils.hasText(updateDTO.getBusinessNumber())) {
            document.setBusinessNumber(updateDTO.getBusinessNumber());
        }
        if (StringUtils.hasText(updateDTO.getTags())) {
            document.setTags(updateDTO.getTags());
        }
        if (StringUtils.hasText(updateDTO.getContent())) {
            document.setContent(updateDTO.getContent());
        }
        if (StringUtils.hasText(updateDTO.getRemark())) {
            document.setRemark(updateDTO.getRemark());
        }
        
        document = documentRepository.save(document);
        return convertToVO(document);
    }

    @Override
    @Transactional
    public void deleteDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文档不存在"));
        
        // 软删除
        document.setIsDeleted(true);
        documentRepository.save(document);
    }

    @Override
    public ResponseEntity<Resource> downloadDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文档不存在"));
        
        if (!StringUtils.hasText(document.getFilePath())) {
            throw new BusinessException("文档文件不存在");
        }
        
        return fileStorageService.loadFileAsResource(document.getFilePath(), document.getTitle());
    }

    /**
     * 构建查询条件
     */
    private Specification<Document> buildSpecification(DocumentQueryDTO queryDTO) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 默认只查询未删除的文档
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));
            
            if (queryDTO != null) {
                // 标题模糊查询
                if (StringUtils.hasText(queryDTO.getTitle())) {
                    predicates.add(criteriaBuilder.like(root.get("title"), "%" + queryDTO.getTitle() + "%"));
                }
                
                // 文档类型
                if (queryDTO.getDocumentType() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("documentType"), queryDTO.getDocumentType()));
                }
                
                // 文档状态
                if (queryDTO.getStatus() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("status"), queryDTO.getStatus()));
                }
                
                // 安全级别
                if (queryDTO.getSecurityLevel() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("securityLevel"), queryDTO.getSecurityLevel()));
                }
                
                // 作者ID
                if (queryDTO.getAuthorId() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("authorId"), queryDTO.getAuthorId()));
                }
                
                // 项目ID
                if (queryDTO.getProjectId() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("projectId"), queryDTO.getProjectId()));
                }
                
                // 部门ID
                if (queryDTO.getDepartmentId() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("departmentId"), queryDTO.getDepartmentId()));
                }
                
                // 标签模糊查询
                if (StringUtils.hasText(queryDTO.getTags())) {
                    predicates.add(criteriaBuilder.like(root.get("tags"), "%" + queryDTO.getTags() + "%"));
                }
                
                // 创建时间范围
                if (queryDTO.getCreatedAtStart() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), queryDTO.getCreatedAtStart()));
                }
                if (queryDTO.getCreatedAtEnd() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), queryDTO.getCreatedAtEnd()));
                }
                
                // 更新时间范围
                if (queryDTO.getUpdatedAtStart() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("updatedAt"), queryDTO.getUpdatedAtStart()));
                }
                if (queryDTO.getUpdatedAtEnd() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("updatedAt"), queryDTO.getUpdatedAtEnd()));
                }
                
                // 关键词搜索（标题、摘要）
                if (StringUtils.hasText(queryDTO.getKeyword())) {
                    Predicate titlePredicate = criteriaBuilder.like(root.get("title"), "%" + queryDTO.getKeyword() + "%");
                    Predicate summaryPredicate = criteriaBuilder.like(root.get("summary"), "%" + queryDTO.getKeyword() + "%");
                    predicates.add(criteriaBuilder.or(titlePredicate, summaryPredicate));
                }
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 转换为VO对象
     */
    private DocumentVO convertToVO(Document document) {
        DocumentVO vo = new DocumentVO();
        BeanUtils.copyProperties(document, vo);
        
        // 设置作者姓名
        if (document.getAuthor() != null) {
            vo.setAuthorName(document.getAuthor().getUsername());
        } else if (document.getAuthorId() != null) {
            userRepository.findById(document.getAuthorId())
                    .ifPresent(user -> vo.setAuthorName(user.getUsername()));
        }
        
        // 设置权限标识（简单实现，实际应根据用户权限判断）
        vo.setCanDownload(StringUtils.hasText(document.getFilePath()));
        vo.setCanEdit(!Boolean.TRUE.equals(document.getIsLocked()));
        vo.setCanDelete(document.getStatus() == DocumentStatus.DRAFT);
        
        return vo;
    }

    /**
     * 生成文档编号
     */
    private String generateDocumentNumber() {
        return "DOC" + System.currentTimeMillis();
    }

    /**
     * 计算文件哈希值
     */
    private String calculateFileHash(MultipartFile file) {
        // TODO: 实现MD5计算
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 获取或创建默认用户
     */
    private Long getOrCreateDefaultUser() {
        // 先尝试查找默认用户
        User defaultUser = userRepository.findByUsernameAndIsDeletedFalse("system")
                .orElse(null);

        if (defaultUser == null) {
            // 创建默认系统用户
            defaultUser = new User();
            defaultUser.setUsername("system");
            defaultUser.setRealName("系统用户");
            defaultUser.setEmail("system@example.com");
            defaultUser.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi"); // 默认密码：123456
            defaultUser.setIsDeleted(false);
            defaultUser = userRepository.save(defaultUser);
        }

        return defaultUser.getId();
    }
}
