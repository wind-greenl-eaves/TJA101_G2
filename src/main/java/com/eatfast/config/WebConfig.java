package com.eatfast.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 標註此類別為 Spring 的組態類別
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 從 application.properties 讀取 app.upload.employee-photos 屬性，指定員工照片上傳路徑
    @Value("${app.upload.employee-photos}")
    private String uploadPath;
    
    @Value("${app.upload.meal-pic}")
    private String mealUploadPath;

    // 設定靜態資源處理器，將 /employee-photos/** 映射到本地檔案系統的指定資料夾
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/employee-photos/**")
                .addResourceLocations("file:" + uploadPath + "/")
                .setCachePeriod(3600) // 設定快取時間為 3600 秒
                .resourceChain(true); // 啟用資源鏈
    
        // 餐點圖片上傳路徑
		registry.addResourceHandler("/meal-pic/**")
				.addResourceLocations("file:" + mealUploadPath + "/")
				.setCachePeriod(3600)
				.resourceChain(true);
		
		// 靜態資源處理
		registry.addResourceHandler("/images/**")
        .addResourceLocations("classpath:/static/images/")
        .setCachePeriod(3600)
        .resourceChain(true);

		// 其他靜態資源直接放在 src/main/resources/static/ 下
		registry.addResourceHandler("/**") 
		        .addResourceLocations("classpath:/static/")
		        .setCachePeriod(3600)
		        .resourceChain(true);
	}
}