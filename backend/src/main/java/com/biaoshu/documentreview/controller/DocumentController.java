package com.biaoshu.documentreview.controller;

import com.biaoshu.documentreview.common.Result;
import com.biaoshu.documentreview.dto.DocumentCreateDTO;
import com.biaoshu.documentreview.dto.DocumentQueryDTO;
import com.biaoshu.documentreview.dto.DocumentUpdateDTO;
import com.biaoshu.documentreview.dto.DocumentVO;
import com.biaoshu.documentreview.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

/**
 * 文档管理控制器
 * 
 * @author biaoshu
 */
@Tag(name = "文档管理", description = "文档管理相关接口")
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @Operation(summary = "获取文档列表", description = "分页查询文档列表，支持多条件筛选")
    @GetMapping
    public Result<Page<DocumentVO>> getDocuments(
            @Parameter(description = "查询条件") DocumentQueryDTO queryDTO,
            @Parameter(description = "分页参数") @PageableDefault(size = 10) Pageable pageable) {
        Page<DocumentVO> documents = documentService.getDocuments(queryDTO, pageable);
        return Result.success(documents);
    }

    @Operation(summary = "获取文档详情", description = "根据ID获取文档详细信息")
    @GetMapping("/{id}")
    public Result<DocumentVO> getDocument(
            @Parameter(description = "文档ID") @PathVariable Long id) {
        DocumentVO document = documentService.getDocumentById(id);
        return Result.success(document);
    }

    @Operation(summary = "创建文档", description = "创建新的文档")
    @PostMapping
    public Result<DocumentVO> createDocument(
            @Parameter(description = "文档创建信息") @Valid @RequestBody DocumentCreateDTO createDTO) {
        DocumentVO document = documentService.createDocument(createDTO);
        return Result.success(document);
    }

    @Operation(summary = "上传文档文件", description = "上传文档文件并创建文档记录")
    @PostMapping("/upload")
    public Result<DocumentVO> uploadDocument(
            @Parameter(description = "文档文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "文档标题") @RequestParam("title") String title,
            @Parameter(description = "文档类型") @RequestParam("documentType") String documentType,
            @Parameter(description = "文档摘要") @RequestParam(value = "summary", required = false) String summary,
            @Parameter(description = "安全级别") @RequestParam(value = "securityLevel", defaultValue = "PUBLIC") String securityLevel,
            @Parameter(description = "项目ID") @RequestParam(value = "projectId", required = false) Long projectId,
            @Parameter(description = "标签") @RequestParam(value = "tags", required = false) String tags) {
        DocumentVO document = documentService.uploadDocument(file, title, documentType, summary, securityLevel, projectId, tags);
        return Result.success(document);
    }

    @Operation(summary = "更新文档", description = "更新文档信息")
    @PutMapping("/{id}")
    public Result<DocumentVO> updateDocument(
            @Parameter(description = "文档ID") @PathVariable Long id,
            @Parameter(description = "文档更新信息") @Valid @RequestBody DocumentUpdateDTO updateDTO) {
        DocumentVO document = documentService.updateDocument(id, updateDTO);
        return Result.success(document);
    }

    @Operation(summary = "删除文档", description = "软删除文档")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDocument(
            @Parameter(description = "文档ID") @PathVariable Long id) {
        documentService.deleteDocument(id);
        return Result.success();
    }

    @Operation(summary = "下载文档", description = "下载文档文件")
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(
            @Parameter(description = "文档ID") @PathVariable Long id) {
        return documentService.downloadDocument(id);
    }
}
