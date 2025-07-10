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
        // ✅ 建立一個「虛擬路徑」到「實體路徑」的映射
        // 1. addResourceHandler("/uploads/images/**")
        //    這行定義了網址。當瀏覽器請求 http://.../uploads/images/任何東西 時，
        //    這個設定就會生效。
        //
        // 2. addResourceLocations("file:" + uploadDir)
        //    這行定義了上面那個網址要對應到電腦上的哪個實體資料夾。
        //    "file:" 這個前綴是告訴 Spring Boot 這是一個檔案系統的路徑。
        //    它會組合出像 "file:C:/eatfast_uploads/images/" 這樣的路徑。
        registry.addResourceHandler("/uploads/images/**")
                .addResourceLocations("file:" + uploadDir);
    }
}