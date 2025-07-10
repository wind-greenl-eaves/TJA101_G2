package com.eatfast.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MealPicResourceConfig implements WebMvcConfigurer {

    @Value("${app.upload.meal-pic}")
    private String mealPicUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(mealPicUploadDir); // 取得 meal-pic 上傳目錄的路徑
        String uploadAbsolutePath = uploadPath.toFile().getAbsolutePath(); // 取得絕對路徑

        // /meal-pic/** 會對應 uploads/meal_pic 資料夾
        registry.addResourceHandler("/meal-pic/**")
                .addResourceLocations("file:" + uploadAbsolutePath + "/");
    }
}
