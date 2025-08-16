package com.biaoshu.documentreview.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA配置类
 * 
 * @author biaoshu
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.biaoshu.documentreview.repository")
public class JpaConfig {
}
