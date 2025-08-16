package com.biaoshu.documentreview.service;

import com.biaoshu.documentreview.dto.DocumentCreateDTO;
import com.biaoshu.documentreview.dto.DocumentQueryDTO;
import com.biaoshu.documentreview.dto.DocumentUpdateDTO;
import com.biaoshu.documentreview.dto.DocumentVO;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档服务接口
 * 
 * @author biaoshu
 */
public interface DocumentService {

    /**
     * 分页查询文档列表
     * 
     * @param queryDTO 查询条件
     * @param pageable 分页参数
     * @return 文档列表
     */
    Page<DocumentVO> getDocuments(DocumentQueryDTO queryDTO, Pageable pageable);

    /**
     * 根据ID获取文档详情
     * 
     * @param id 文档ID
     * @return 文档详情
     */
    DocumentVO getDocumentById(Long id);

    /**
     * 创建文档
     * 
     * @param createDTO 创建信息
     * @return 文档详情
     */
    DocumentVO createDocument(DocumentCreateDTO createDTO);

    /**
     * 上传文档文件
     * 
     * @param file 文档文件
     * @param title 文档标题
     * @param documentType 文档类型
     * @param summary 文档摘要
     * @param securityLevel 安全级别
     * @param projectId 项目ID
     * @param tags 标签
     * @return 文档详情
     */
    DocumentVO uploadDocument(MultipartFile file, String title, String documentType, 
                             String summary, String securityLevel, Long projectId, String tags);

    /**
     * 更新文档
     * 
     * @param id 文档ID
     * @param updateDTO 更新信息
     * @return 文档详情
     */
    DocumentVO updateDocument(Long id, DocumentUpdateDTO updateDTO);

    /**
     * 删除文档
     * 
     * @param id 文档ID
     */
    void deleteDocument(Long id);

    /**
     * 下载文档文件
     * 
     * @param id 文档ID
     * @return 文件响应
     */
    ResponseEntity<Resource> downloadDocument(Long id);
}
