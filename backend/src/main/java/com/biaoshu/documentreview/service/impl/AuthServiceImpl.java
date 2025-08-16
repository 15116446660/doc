package com.biaoshu.documentreview.service.impl;

import com.biaoshu.documentreview.common.ResultCode;
import com.biaoshu.documentreview.dto.auth.LoginRequest;
import com.biaoshu.documentreview.dto.auth.LoginResponse;
import com.biaoshu.documentreview.entity.User;
import com.biaoshu.documentreview.exception.BusinessException;
import com.biaoshu.documentreview.repository.UserRepository;
import com.biaoshu.documentreview.security.JwtTokenProvider;
import com.biaoshu.documentreview.security.UserPrincipal;
import com.biaoshu.documentreview.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证服务实现
 * 
 * @author biaoshu
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TOKEN_BLACKLIST_PREFIX = "blacklist:token:";
    private static final String USER_LOGIN_PREFIX = "user:login:";

    @Override
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        // 认证用户
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // 更新用户登录信息
        updateUserLoginInfo(userPrincipal.getId());

        // 生成Token
        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        // 缓存用户登录状态
        cacheUserLoginStatus(userPrincipal.getId(), accessToken);

        // 构建响应
        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresAt(LocalDateTime.now().plusSeconds(86400)); // 24小时后过期

        // 构建用户信息
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(userPrincipal.getId());
        userInfo.setUsername(userPrincipal.getUsername());
        userInfo.setRealName(userPrincipal.getRealName());
        userInfo.setEmail(userPrincipal.getEmail());
        userInfo.setPhone(userPrincipal.getPhone());
        userInfo.setEmployeeId(userPrincipal.getEmployeeId());
        userInfo.setDepartmentId(userPrincipal.getDepartmentId());
        userInfo.setPosition(userPrincipal.getPosition());

        // 获取角色和权限
        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5))
                .collect(Collectors.toList());

        List<String> permissions = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> !auth.startsWith("ROLE_"))
                .collect(Collectors.toList());

        userInfo.setRoles(roles);
        userInfo.setPermissions(permissions);
        response.setUserInfo(userInfo);

        log.info("用户登录成功: {}", userPrincipal.getUsername());
        return response;
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken) || !tokenProvider.isRefreshToken(refreshToken)) {
            throw new BusinessException(ResultCode.REFRESH_TOKEN_INVALID);
        }

        Long userId = tokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities());

        // 生成新的Token
        String newAccessToken = tokenProvider.generateToken(authentication);
        String newRefreshToken = tokenProvider.generateRefreshToken(authentication);

        // 更新缓存
        cacheUserLoginStatus(userId, newAccessToken);

        LoginResponse response = new LoginResponse();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(newRefreshToken);
        response.setExpiresAt(LocalDateTime.now().plusSeconds(86400));

        log.info("Token刷新成功: {}", user.getUsername());
        return response;
    }

    @Override
    public void logout(String token) {
        if (tokenProvider.validateToken(token)) {
            Long userId = tokenProvider.getUserIdFromToken(token);
            
            // 将Token加入黑名单
            long expiration = tokenProvider.getExpirationDateFromToken(token).getTime() - System.currentTimeMillis();
            if (expiration > 0) {
                redisTemplate.opsForValue().set(
                        TOKEN_BLACKLIST_PREFIX + token, 
                        true, 
                        expiration, 
                        TimeUnit.MILLISECONDS
                );
            }

            // 清除用户登录状态缓存
            redisTemplate.delete(USER_LOGIN_PREFIX + userId);

            log.info("用户登出成功: userId={}", userId);
        }
    }

    @Override
    public boolean validateToken(String token) {
        if (!tokenProvider.validateToken(token)) {
            return false;
        }

        // 检查Token是否在黑名单中
        Boolean isBlacklisted = (Boolean) redisTemplate.opsForValue().get(TOKEN_BLACKLIST_PREFIX + token);
        return isBlacklisted == null || !isBlacklisted;
    }

    /**
     * 更新用户登录信息
     */
    private void updateUserLoginInfo(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastLoginAt(LocalDateTime.now());
            // 这里可以设置登录IP等信息
            userRepository.save(user);
        });
    }

    /**
     * 缓存用户登录状态
     */
    private void cacheUserLoginStatus(Long userId, String token) {
        redisTemplate.opsForValue().set(
                USER_LOGIN_PREFIX + userId, 
                token, 
                24, 
                TimeUnit.HOURS
        );
    }
}
