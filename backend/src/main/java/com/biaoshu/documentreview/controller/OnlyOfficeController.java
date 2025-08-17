package com.biaoshu.documentreview.controller;

import com.biaoshu.documentreview.common.Result;
import com.biaoshu.documentreview.dto.OnlyOfficeDTO;
import com.biaoshu.documentreview.service.OnlyOfficeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/onlyoffice")
@RequiredArgsConstructor
@Tag(name = "OnlyOffice集成", description = "与OnlyOffice Document Server交互的接口")
public class OnlyOfficeController {

    private final OnlyOfficeService onlyOfficeService;

    @Operation(summary = "获取编辑器配置", description = "为指定文档获取OnlyOffice编辑器的配置信息")
    @GetMapping("/config/{documentId}")
    public Result<OnlyOfficeDTO.EditorConfig> getEditorConfig(@PathVariable Long documentId) {
        log.info("Requesting OnlyOffice config for documentId: {}", documentId);
        OnlyOfficeDTO.EditorConfig config = onlyOfficeService.getEditorConfig(documentId);
        return Result.success(config);
    }

    @Operation(summary = "处理编辑器回调", description = "接收来自OnlyOffice Document Server的回调通知")
    @PostMapping("/callback")
    public Result<Object> handleCallback(@RequestBody OnlyOfficeDTO.Callback callbackData) {
        log.info("Received callback from OnlyOffice: status {}", callbackData.getStatus());
        try {
            onlyOfficeService.processCallback(callbackData);
            // OnlyOffice expects a JSON response with {"error": 0} on success
            return Result.success(java.util.Map.of("error", 0));
        } catch (Exception e) {
            log.error("Error processing OnlyOffice callback", e);
            // Return an error response
            return Result.error(java.util.Map.of("error", 1, "message", e.getMessage()));
        }
    }
}
