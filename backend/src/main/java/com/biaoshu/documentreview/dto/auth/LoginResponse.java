package com.biaoshu.documentreview.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录响应DTO
 * 
 * @author biaoshu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginResponse {

    @Schema(description = "访问令牌")
    private String accessToken;

    @Schema(description = "刷新令牌")
    private String refreshToken;

    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "过期时间")
    private LocalDateTime expiresAt;

    @Schema(description = "用户信息")
    private UserInfo userInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户信息")
    public static class UserInfo {
        @Schema(description = "用户ID")
        private Long id;

        @Schema(description = "用户名")
        private String username;

        @Schema(description = "真实姓名")
        private String realName;

        @Schema(description = "邮箱")
        private String email;

        @Schema(description = "手机号")
        private String phone;

        @Schema(description = "员工编号")
        private String employeeId;

        @Schema(description = "部门ID")
        private Long departmentId;

        @Schema(description = "职位")
        private String position;

        @Schema(description = "头像URL")
        private String avatarUrl;

        @Schema(description = "角色列表")
        private List<String> roles;

        @Schema(description = "权限列表")
        private List<String> permissions;
    }
}
