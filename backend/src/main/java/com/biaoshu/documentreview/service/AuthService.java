package com.biaoshu.documentreview.service;

import com.biaoshu.documentreview.dto.auth.LoginRequest;
import com.biaoshu.documentreview.dto.auth.LoginResponse;

/**
 * 认证服务接口
 * 
 * @author biaoshu
 */
public interface AuthService {

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * 刷新Token
     */
    LoginResponse refreshToken(String refreshToken);

    /**
     * 用户登出
     */
    void logout(String token);

    /**
     * 验证Token
     */
    boolean validateToken(String token);
}
