package com.biaoshu.documentreview.security;

import com.biaoshu.documentreview.enums.HierarchyPermission;
import com.biaoshu.documentreview.service.HierarchyPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 项目层级权限检查切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class HierarchyPermissionAspect {

    private final HierarchyPermissionService hierarchyPermissionService;

    @Around("@annotation(requireHierarchyPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequireHierarchyPermission requireHierarchyPermission) throws Throwable {
        // 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("用户未认证");
        }

        String username = authentication.getName();
        Long userId = getCurrentUserId(authentication);

        // 检查功能权限
        boolean hasPermission = checkFunctionalPermission(userId, requireHierarchyPermission);
        if (!hasPermission) {
            log.warn("用户 {} 缺少必要权限: {}", username, requireHierarchyPermission.value());
            throw new AccessDeniedException("权限不足");
        }

        // 检查数据权限
        if (requireHierarchyPermission.checkDataPermission()) {
            boolean hasDataPermission = checkDataPermission(joinPoint, userId, requireHierarchyPermission);
            if (!hasDataPermission) {
                log.warn("用户 {} 缺少数据权限", username);
                throw new AccessDeniedException("数据权限不足");
            }
        }

        return joinPoint.proceed();
    }

    /**
     * 检查功能权限
     */
    private boolean checkFunctionalPermission(Long userId, RequireHierarchyPermission annotation) {
        HierarchyPermission[] requiredPermissions = annotation.value();
        if (requiredPermissions.length == 0) {
            return true;
        }

        // 检查是否为系统管理员
        if (hierarchyPermissionService.hasPermission(userId, HierarchyPermission.SYSTEM_ADMIN)) {
            return true;
        }

        // 根据逻辑操作符检查权限
        if (annotation.logical() == RequireHierarchyPermission.LogicalOperator.AND) {
            // 需要所有权限
            for (HierarchyPermission permission : requiredPermissions) {
                if (!hierarchyPermissionService.hasPermission(userId, permission)) {
                    return false;
                }
            }
            return true;
        } else {
            // 需要任一权限
            for (HierarchyPermission permission : requiredPermissions) {
                if (hierarchyPermissionService.hasPermission(userId, permission)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 检查数据权限
     */
    private boolean checkDataPermission(ProceedingJoinPoint joinPoint, Long userId, RequireHierarchyPermission annotation) {
        try {
            // 获取数据ID参数
            Object dataId = getParameterValue(joinPoint, annotation.dataIdParam());
            if (dataId == null) {
                return true; // 如果没有数据ID参数，跳过数据权限检查
            }

            Long nodeId = Long.valueOf(dataId.toString());
            String nodeType = annotation.nodeType();

            // 检查用户是否有权限操作该数据
            return hierarchyPermissionService.hasDataPermission(userId, nodeType, nodeId);
        } catch (Exception e) {
            log.error("数据权限检查失败", e);
            return false;
        }
    }

    /**
     * 获取方法参数值
     */
    private Object getParameterValue(ProceedingJoinPoint joinPoint, String paramName) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getName().equals(paramName)) {
                return args[i];
            }
        }
        return null;
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId(Authentication authentication) {
        // 这里需要根据实际的用户认证实现来获取用户ID
        // 假设用户详情中包含用户ID
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getUserId();
        }
        return null;
    }

    /**
     * 用户主体类（示例）
     */
    public static class UserPrincipal {
        private Long userId;
        private String username;

        public UserPrincipal(Long userId, String username) {
            this.userId = userId;
            this.username = username;
        }

        public Long getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }
    }
}
