package com.biaoshu.documentreview.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * 安全工具类
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            // 这里假设用户名就是用户ID，实际项目中可能需要从UserDetails中获取用户ID
            try {
                return Long.parseLong(userDetails.getUsername());
            } catch (NumberFormatException e) {
                // 如果用户名不是数字，可能需要通过其他方式获取用户ID
                return null;
            }
        }
        
        return null;
    }

    /**
     * 获取当前登录用户名
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }
        
        return null;
    }

    /**
     * 获取当前用户权限
     */
    public static Collection<? extends GrantedAuthority> getCurrentUserAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        return authentication.getAuthorities();
    }

    /**
     * 检查当前用户是否有指定角色
     */
    public static boolean hasRole(String role) {
        Collection<? extends GrantedAuthority> authorities = getCurrentUserAuthorities();
        if (authorities == null) {
            return false;
        }
        
        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals(roleWithPrefix));
    }

    /**
     * 检查当前用户是否有指定权限
     */
    public static boolean hasAuthority(String authority) {
        Collection<? extends GrantedAuthority> authorities = getCurrentUserAuthorities();
        if (authorities == null) {
            return false;
        }
        
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals(authority));
    }

    /**
     * 检查当前用户是否是管理员
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN") || hasRole("SYSTEM_ADMIN");
    }

    /**
     * 检查当前用户是否已认证
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && 
               !"anonymousUser".equals(authentication.getPrincipal());
    }
}
