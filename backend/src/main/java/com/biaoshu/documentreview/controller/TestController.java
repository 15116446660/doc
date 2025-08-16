package com.biaoshu.documentreview.controller;

import com.biaoshu.documentreview.common.Result;
import com.biaoshu.documentreview.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 * 
 * @author biaoshu
 */
@Tag(name = "系统测试", description = "系统测试相关接口")
@RestController
@RequestMapping("/test")
public class TestController {

    @Operation(summary = "健康检查", description = "检查系统是否正常运行")
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", LocalDateTime.now());
        data.put("service", "Document Review System");
        data.put("version", "1.0.0");
        return Result.success("系统运行正常", data);
    }

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/current-user")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Result<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", userPrincipal.getId());
        data.put("username", userPrincipal.getUsername());
        data.put("realName", userPrincipal.getRealName());
        data.put("email", userPrincipal.getEmail());
        data.put("authorities", userPrincipal.getAuthorities());
        return Result.success("获取用户信息成功", data);
    }

    @Operation(summary = "管理员测试", description = "测试管理员权限")
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> adminTest() {
        return Result.success("管理员权限验证成功");
    }

    @Operation(summary = "审核员测试", description = "测试审核员权限")
    @GetMapping("/reviewer")
    @PreAuthorize("hasRole('REVIEWER')")
    public Result<String> reviewerTest() {
        return Result.success("审核员权限验证成功");
    }

    @Operation(summary = "文档查看权限测试", description = "测试文档查看权限")
    @GetMapping("/document-view")
    @PreAuthorize("hasAuthority('document:view')")
    public Result<String> documentViewTest() {
        return Result.success("文档查看权限验证成功");
    }

    @Operation(summary = "系统信息", description = "获取系统基本信息")
    @GetMapping("/system-info")
    public Result<Map<String, Object>> systemInfo() {
        Map<String, Object> data = new HashMap<>();
        data.put("applicationName", "企业文档审核系统");
        data.put("version", "1.0.0");
        data.put("buildTime", "2024-01-01");
        data.put("javaVersion", System.getProperty("java.version"));
        data.put("osName", System.getProperty("os.name"));
        data.put("serverTime", LocalDateTime.now());
        return Result.success("获取系统信息成功", data);
    }
}
