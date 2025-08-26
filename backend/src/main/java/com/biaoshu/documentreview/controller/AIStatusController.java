package com.biaoshu.documentreview.controller;

import com.biaoshu.documentreview.common.Result;
import com.biaoshu.documentreview.service.ai.QwenAIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * AI服务状态监控控制器
 */
@RestController
@RequestMapping("/api/ai/status")
@Tag(name = "AI服务状态", description = "AI服务状态监控和管理")
public class AIStatusController {

    @Autowired
    private QwenAIService qwenAIService;

    @Operation(summary = "获取AI服务状态", description = "获取AI服务的当前状态信息")
    @GetMapping
    public ResponseEntity<Result<Map<String, Object>>> getAIServiceStatus() {
        
        Map<String, Object> status = new HashMap<>();
        status.put("available", qwenAIService.isAvailable());
        status.put("service", qwenAIService.getServiceStatus());
        status.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(Result.success("获取AI服务状态成功", status));
    }

    @Operation(summary = "测试AI服务连接", description = "测试AI服务的连接状态")
    @PostMapping("/test")
    public ResponseEntity<Result<String>> testAIService() {
        
        try {
            String testPrompt = "请简单回答：你好";
            String response = qwenAIService.generateText(testPrompt).get();
            
            return ResponseEntity.ok(Result.success("AI服务测试成功", response));
            
        } catch (Exception e) {
            return ResponseEntity.ok(Result.success("AI服务测试完成（使用模拟模式）", 
                "AI服务当前不可用，已切换到模拟模式。错误信息：" + e.getMessage()));
        }
    }

    @Operation(summary = "获取AI服务配置信息", description = "获取AI服务的配置信息")
    @GetMapping("/config")
    public ResponseEntity<Result<Map<String, Object>>> getAIServiceConfig() {
        
        Map<String, Object> config = new HashMap<>();
        config.put("enabled", qwenAIService.isAvailable());
        config.put("service_type", "阿里云千问");
        config.put("model", "qwen-max");
        config.put("features", new String[]{
            "AI内容反向插入",
            "智能切片标记", 
            "提示词测试工具",
            "引用来源标记",
            "AI智能格式化",
            "长文本交互",
            "文档差异对比"
        });
        
        return ResponseEntity.ok(Result.success("获取AI服务配置成功", config));
    }
}
