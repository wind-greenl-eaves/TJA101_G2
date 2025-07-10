package com.eatfast.meal.controller;

import com.eatfast.meal.model.MealEntity; 
import com.eatfast.meal.model.MealRepository; 
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType; 
import org.springframework.http.ResponseEntity; 
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/meals") // 餐點 API 的基礎路徑
public class MealRestController {

    private final MealRepository mealRepository;

    public MealRestController(MealRepository mealRepository) {
        this.mealRepository = mealRepository;
    }

    /**
     * 獲取餐點圖片的 API。
     * GET /api/meals/{mealId}/image
     *
     * @param mealId 餐點 ID。
     * @return 圖片的位元組陣列，附帶正確的 Content-Type，如果圖片不存在則返回 404 Not Found。
     */
    @GetMapping(value = "/{mealId}/image", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public ResponseEntity<byte[]> getMealImage(@PathVariable Long mealId) {
        Optional<MealEntity> mealOpt = mealRepository.findById(mealId);

        if (mealOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        MealEntity meal = mealOpt.get();
        String fileName = meal.getMealPic(); // 取得檔案名稱，如 "milktea.jpg"

        if (fileName == null || fileName.isBlank()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        try {
            // 假設圖片存在於專案根目錄下的 /images 目錄
            Path imagePath = Paths.get("images", fileName); 
            byte[] imageBytes = Files.readAllBytes(imagePath);

            HttpHeaders headers = new HttpHeaders();

            // 根據副檔名判斷內容類型
            if (fileName.toLowerCase().endsWith(".png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
            } else {
                headers.setContentType(MediaType.IMAGE_JPEG); // 預設 JPEG
            }

            headers.setContentLength(imageBytes.length);

            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(); // log error
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}