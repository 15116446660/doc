package com.biaoshu.documentreview.security;

import com.biaoshu.documentreview.entity.User;
import com.biaoshu.documentreview.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring Security用户主体
 * 
 * @author biaoshu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private Long id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private String employeeId;
    private Long departmentId;
    private String position;
    private UserStatus status;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> new SimpleGrantedAuthority(permission.getPermissionCode()))
                .collect(Collectors.toList());

        // 添加角色权限
        user.getRoles().forEach(role -> 
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleCode()))
        );

        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getEmail(),
                user.getPhone(),
                user.getEmployeeId(),
                user.getDepartmentId(),
                user.getPosition(),
                user.getStatus(),
                user.getPassword(),
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.LOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }

    /**
     * 检查是否有指定权限
     */
    public boolean hasPermission(String permission) {
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals(permission));
    }

    /**
     * 检查是否有指定角色
     */
    public boolean hasRole(String role) {
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
    }
}
