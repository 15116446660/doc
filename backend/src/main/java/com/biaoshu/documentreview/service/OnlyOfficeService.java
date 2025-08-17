package com.biaoshu.documentreview.service;

import com.biaoshu.documentreview.dto.OnlyOfficeDTO;

import java.io.IOException;

/**
 * OnlyOffice服务接口
 */
public interface OnlyOfficeService {

    /**
     * 获取OnlyOffice编辑器的配置
     * @param documentId 文档ID
     * @return 编辑器配置
     */
    OnlyOfficeDTO.EditorConfig getEditorConfig(Long documentId);

    /**
     * 处理OnlyOffice服务器的回调
     * @param callbackData 回调数据
     * @throws IOException
     */
    void processCallback(OnlyOfficeDTO.Callback callbackData) throws IOException;
}
