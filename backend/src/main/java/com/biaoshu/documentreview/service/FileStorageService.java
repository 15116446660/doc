package com.biaoshu.documentreview.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务接口
 * 
 * @author biaoshu
 */
public interface FileStorageService {

    /**
     * 存储文件
     * 
     * @param file 文件
     * @return 文件路径
     */
    String storeFile(MultipartFile file);

    /**
     * 加载文件作为资源
     * 
     * @param filePath 文件路径
     * @param fileName 文件名
     * @return 文件响应
     */
    ResponseEntity<Resource> loadFileAsResource(String filePath, String fileName);

    /**
     * 删除文件
     * 
     * @param filePath 文件路径
     */
    void deleteFile(String filePath);
}
