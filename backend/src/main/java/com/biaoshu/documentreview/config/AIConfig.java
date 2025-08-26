package com.biaoshu.documentreview.config;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.models.QwenParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * AI配置类
 */
@Configuration
public class AIConfig {

    @Value("${ai.qwen.api-key:}")
    private String qwenApiKey;

    @Value("${ai.qwen.model:qwen-max}")
    private String qwenModel;

    @Value("${ai.qwen.max-tokens:2000}")
    private Integer maxTokens;

    @Value("${ai.qwen.temperature:0.7}")
    private Float temperature;

    @Value("${ai.enabled:true}")
    private Boolean aiEnabled;

    @Bean
    public Generation qwenGeneration() {
        if (aiEnabled && qwenApiKey != null && !qwenApiKey.isEmpty()) {
            try {
                System.setProperty("DASHSCOPE_API_KEY", qwenApiKey);
                return new Generation();
            } catch (Exception e) {
                System.err.println("Failed to initialize Qwen Generation: " + e.getMessage());
                return null;
            }
        }
        return null;
    }

    @Bean
    public GenerationParam defaultGenerationParam() {
        return GenerationParam.builder()
                .model(qwenModel)
                .maxTokens(maxTokens)
                .temperature(temperature)
                .build();
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    // Getters
    public String getQwenApiKey() {
        return qwenApiKey;
    }

    public String getQwenModel() {
        return qwenModel;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public Float getTemperature() {
        return temperature;
    }

    public Boolean getAiEnabled() {
        return aiEnabled;
    }
}
