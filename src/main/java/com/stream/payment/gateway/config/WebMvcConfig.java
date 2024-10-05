package com.stream.payment.gateway.config;

import com.stream.payment.gateway.interceptor.ApiPerformanceInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private ApiPerformanceInterceptor apiPerformanceInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Register the interceptor for all API endpoints
        registry.addInterceptor(apiPerformanceInterceptor)
                .addPathPatterns("/api/**"); // Apply to all API endpoints
    }
}

