package com.eatfast.meal.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eatfast.meal.dto.MealRatingDto;

@Service
public class MealRatingService {

    @Autowired
    private MealRatingRepository mealRatingRepository; 

    // 每個餐點的平均評分
    public List<MealRatingDto> getAllMealAvgStars() {
        List<Object[]> result = mealRatingRepository.findAllMealAvgStars(); // 使用自定義查詢方法獲取平均評分數據
        List<MealRatingDto> dtoList = new ArrayList<>(); // 用於存儲轉換後的 MealRatingDto 列表
        
        
        for(Object[] row : result) {
            Long mealId = ((Number)row[0]).longValue();
            Double avgStars = row[1] != null ? ((Number)row[1]).doubleValue() : 0.0;
            dtoList.add(new MealRatingDto(mealId, avgStars));
        }
        return dtoList;
    }
}

