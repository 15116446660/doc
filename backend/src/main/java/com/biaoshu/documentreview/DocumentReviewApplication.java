package com.biaoshu.documentreview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 企业文档审核系统主启动类
 * 
 * @author biaoshu
 * @version 1.0.0
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class DocumentReviewApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentReviewApplication.class, args);
    }
}
