package com.biaoshu.documentreview.config;

import com.biaoshu.documentreview.security.CustomUserDetailsService;
import com.biaoshu.documentreview.security.JwtAuthenticationEntryPoint;
import com.biaoshu.documentreview.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security配置
 * 
 * @author biaoshu
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
    securedEnabled = true,
    jsr250Enabled = true,
    prePostEnabled = true
)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.disable())
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                    // 公开接口
                    .requestMatchers("/auth/**").permitAll()
                    .requestMatchers("/public/**").permitAll()
                    .requestMatchers("/test/health", "/test/system-info").permitAll()
                    // 项目管理API（临时开放用于测试）
                    .requestMatchers("/api/projects/**").permitAll()
                    // 文档管理API（临时开放用于测试）
                    .requestMatchers("/api/documents/**").permitAll()
                    // Swagger文档
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                    // 健康检查
                    .requestMatchers("/actuator/health").permitAll()
                    // H2控制台
                    .requestMatchers("/h2-console/**").permitAll()
                    // 静态资源
                    .requestMatchers("/uploads/**").permitAll()
                    // OPTIONS请求
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    // 其他请求需要认证
                    .anyRequest().authenticated()
                );

        // 添加JWT过滤器
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
