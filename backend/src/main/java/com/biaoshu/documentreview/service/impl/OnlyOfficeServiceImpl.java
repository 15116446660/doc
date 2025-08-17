package com.biaoshu.documentreview.service.impl;

import com.biaoshu.documentreview.dto.OnlyOfficeDTO;
import com.biaoshu.documentreview.entity.Document;
import com.biaoshu.documentreview.entity.User;
import com.biaoshu.documentreview.exception.BusinessException;
import com.biaoshu.documentreview.repository.DocumentRepository;
import com.biaoshu.documentreview.service.FileStorageService;
import com.biaoshu.documentreview.service.OnlyOfficeService;
import com.biaoshu.documentreview.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnlyOfficeServiceImpl implements OnlyOfficeService {

    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;

    @Value("${app.backend-url}")
    private String backendUrl;

    @Value("${onlyoffice.doc-server.url}")
    private String docServerUrl;

    @Override
    public OnlyOfficeDTO.EditorConfig getEditorConfig(Long documentId) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException("文档不存在"));

        User currentUser = SecurityUtils.getCurrentUserEntity()
                .orElseThrow(() -> new BusinessException("用户未登录"));

        // Build the configuration object
        OnlyOfficeDTO.EditorConfig config = new OnlyOfficeDTO.EditorConfig();

        OnlyOfficeDTO.Document officeDoc = new OnlyOfficeDTO.Document();
        officeDoc.setKey(doc.getDocumentNumber() + "_" + doc.getCurrentVersion()); // Unique key for this version
        officeDoc.setTitle(doc.getTitle());
        officeDoc.setUrl(backendUrl + "/api/documents/" + doc.getId() + "/download"); // URL to download the file
        String fileExtension = doc.getFilePath().substring(doc.getFilePath().lastIndexOf('.') + 1);
        officeDoc.setFileType(fileExtension);

        OnlyOfficeDTO.Permissions permissions = new OnlyOfficeDTO.Permissions();
        permissions.setEdit(true); // For now, allow editing
        officeDoc.setPermissions(permissions);
        config.setDocument(officeDoc);

        OnlyOfficeDTO.EditorConfigInternal editorConfigInternal = new OnlyOfficeDTO.EditorConfigInternal();
        editorConfigInternal.setCallbackUrl(backendUrl + "/api/onlyoffice/callback"); // Callback URL

        OnlyOfficeDTO.User officeUser = new OnlyOfficeDTO.User();
        officeUser.setId(String.valueOf(currentUser.getId()));
        officeUser.setName(currentUser.getRealName());
        editorConfigInternal.setUser(officeUser);

        OnlyOfficeDTO.Customization customization = new OnlyOfficeDTO.Customization();
        customization.setForcesave(true);
        editorConfigInternal.setCustomization(customization);

        config.setEditorConfig(editorConfigInternal);

        // In a real scenario, a JWT should be generated to secure the communication
        // with the document server. For now, it's null.
        config.setToken(null);

        return config;
    }

    @Override
    @Transactional
    public void processCallback(OnlyOfficeDTO.Callback callbackData) throws IOException {
        log.info("Received OnlyOffice callback: status={}", callbackData.getStatus());

        // Status 2 means the document is ready to be saved
        if (callbackData.getStatus() == 2) {
            String documentKey = callbackData.getKey();
            String downloadUrl = callbackData.getUrl();

            log.info("Document with key '{}' is ready to be saved from URL: {}", documentKey, downloadUrl);

            // Find the original document based on the key
            // The key is formatted as "documentNumber_version"
            String documentNumber = documentKey.split("_")[0];
            Document originalDoc = documentRepository.findByDocumentNumber(documentNumber)
                    .orElseThrow(() -> new BusinessException("Callback received for unknown document: " + documentNumber));

            // Download the updated file from OnlyOffice server
            try (InputStream in = new URL(downloadUrl).openStream()) {
                // Store the new file as a new version
                // This logic needs to be more sophisticated to handle versioning correctly
                // For now, just overwrite the existing file
                Path newPath = fileStorageService.storeFile(in, originalDoc.getFilePath());

                // Update document entity
                originalDoc.setUpdatedAt(java.time.LocalDateTime.now());
                // In a real scenario, you would create a new DocumentVersion record here.
                originalDoc.setCurrentVersion(originalDoc.getCurrentVersion() + 1);
                documentRepository.save(originalDoc);

                log.info("Successfully saved updated document for key '{}' to path '{}'", documentKey, newPath);
            }
        } else {
            log.warn("Received callback with unhandled status: {}", callbackData.getStatus());
        }
    }
}
