package com.biaoshu.documentreview.controller;

import com.biaoshu.documentreview.common.Result;
import com.biaoshu.documentreview.dto.auth.LoginRequest;
import com.biaoshu.documentreview.dto.auth.LoginResponse;
import com.biaoshu.documentreview.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 认证控制器
 * 
 * @author biaoshu
 */
@Tag(name = "认证管理", description = "用户认证相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录", description = "用户名密码登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return Result.success("登录成功", response);
    }

    @Operation(summary = "刷新Token", description = "使用刷新Token获取新的访问Token")
    @PostMapping("/refresh")
    public Result<LoginResponse> refreshToken(
            @Parameter(description = "刷新Token") @RequestParam String refreshToken) {
        LoginResponse response = authService.refreshToken(refreshToken);
        return Result.success("Token刷新成功", response);
    }

    @Operation(summary = "用户登出", description = "用户登出，使Token失效")
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token != null) {
            authService.logout(token);
        }
        return Result.success("登出成功", "登出成功");
    }

    @Operation(summary = "验证Token", description = "验证Token是否有效")
    @GetMapping("/validate")
    public Result<Boolean> validateToken(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        boolean isValid = token != null && authService.validateToken(token);
        return Result.success("Token验证完成", isValid);
    }

    /**
     * 从请求中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
