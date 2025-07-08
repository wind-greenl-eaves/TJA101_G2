package com.eatfast.meal.controller;

import com.eatfast.meal.model.MealEntity; 
import com.eatfast.meal.model.MealRepository; 

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
        byte[] imageBytes = meal.getMealPic(); // 假設 MealEntity 有 getMealPic() 方法返回 byte[]

        if (imageBytes == null || imageBytes.length == 0) {
            // 如果資料庫中沒有圖片，返回預設圖片的 HTTP 轉向 (重定向)
            // 實際項目中，可能會提供一個預設圖片的位元組陣列，或直接返回 404
            // 這裡選擇返回 404，前端使用默認佔位圖
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); 
        }

        HttpHeaders headers = new HttpHeaders();
        // 根據實際圖片類型設定 Content-Type，這裡假設是 JPEG 或 PNG
        // 在 MealEntity 中儲存圖片類型會更健壯
        // 如果所有圖片都是 JPEG，則使用 MediaType.IMAGE_JPEG
        headers.setContentType(MediaType.IMAGE_JPEG); // 假設圖片是 JPEG，如果需要 PNG 則使用 MediaType.IMAGE_PNG
        headers.setContentLength(imageBytes.length);

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }

}