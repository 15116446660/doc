package com.biaoshu.documentreview.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 登录请求DTO
 * 
 * @author biaoshu
 */
@Data
@Schema(description = "登录请求")
public class LoginRequest {

    @NotBlank(message = "用户名或邮箱不能为空")
    @Size(min = 3, max = 100, message = "用户名或邮箱长度必须在3-100个字符之间")
    @Schema(description = "用户名或邮箱", example = "admin")
    private String usernameOrEmail;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    @Schema(description = "密码", example = "123456")
    private String password;

    @Schema(description = "记住我", example = "false")
    private Boolean rememberMe = false;
}
