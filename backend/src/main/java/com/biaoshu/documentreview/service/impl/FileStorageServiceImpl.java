package com.biaoshu.documentreview.service.impl;

import com.biaoshu.documentreview.exception.BusinessException;
import com.biaoshu.documentreview.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件存储服务实现类
 * 
 * @author biaoshu
 */
@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.file.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public String storeFile(MultipartFile file) {
        // 验证文件
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        String originalFileName = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFileName)) {
            throw new BusinessException("文件名不能为空");
        }

        try {
            // 创建上传目录
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 按日期创建子目录
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path datePath = uploadPath.resolve(dateDir);
            if (!Files.exists(datePath)) {
                Files.createDirectories(datePath);
            }

            // 生成唯一文件名
            String fileExtension = getFileExtension(originalFileName);
            String fileName = UUID.randomUUID().toString() + fileExtension;
            Path targetLocation = datePath.resolve(fileName);

            // 复制文件
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 返回相对路径
            return dateDir + "/" + fileName;

        } catch (IOException e) {
            log.error("存储文件失败: {}", originalFileName, e);
            throw new BusinessException("存储文件失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Resource> loadFileAsResource(String filePath, String fileName) {
        try {
            Path file = Paths.get(uploadDir).resolve(filePath).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                // 确定文件类型
                String contentType = Files.probeContentType(file);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, 
                               "attachment; filename=\"" + fileName + "\"")
                        .body(resource);
            } else {
                throw new BusinessException("文件不存在或不可读: " + filePath);
            }
        } catch (MalformedURLException e) {
            log.error("加载文件失败: {}", filePath, e);
            throw new BusinessException("加载文件失败: " + e.getMessage());
        } catch (IOException e) {
            log.error("读取文件类型失败: {}", filePath, e);
            throw new BusinessException("读取文件失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String filePath) {
        try {
            Path file = Paths.get(uploadDir).resolve(filePath).normalize();
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.error("删除文件失败: {}", filePath, e);
            throw new BusinessException("删除文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex);
    }
}
