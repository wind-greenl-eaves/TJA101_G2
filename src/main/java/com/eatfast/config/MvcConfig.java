package com.eatfast.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    // 從 application.properties 注入我們設定好的實體路徑
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        System.out.println(">>> [MvcConfig] 正在設定資源處理器...");
        System.out.println(">>> [MvcConfig] 虛擬路徑: /uploads/images/**");
        System.out.println(">>> [MvcConfig] 實體路徑: file:" + uploadDir);

        // 這行是重點：
        // 1. addResourceHandler("/uploads/images/**")：定義了外部存取用的「虛擬路徑」
        // 2. addResourceLocations("file:" + uploadDir)：對應到伺服器上的「實體路徑」
        registry.addResourceHandler("/uploads/images/**")
                .addResourceLocations("file:" + uploadDir);
    }
}
