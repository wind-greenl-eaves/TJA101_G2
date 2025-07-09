package com.eatfast.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MealPicStorageInit {

    @Value("${app.upload.meal-pic}")
    private String mealPicUploadDir;

    @Bean("mealPicStorageBean")
    CommandLineRunner init() {
        return args -> {
            Path path = Paths.get(mealPicUploadDir); // 取得 meal-pic 上傳目錄的路徑
            if (!Files.exists(path)) { // 檢查目錄是否存在，不存在，則創建目錄
                Files.createDirectories(path); 
            }
        };
    }
}
