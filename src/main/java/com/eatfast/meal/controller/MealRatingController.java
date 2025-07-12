package com.eatfast.meal.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eatfast.meal.dto.MealRatingDto;
import com.eatfast.meal.model.MealRatingService;

@RestController
@RequestMapping("/api/meals")
public class MealRatingController {

    @Autowired
    private MealRatingService mealRatingService;

    @GetMapping("/ratings")
    public List<MealRatingDto> getMealRatings() {      // 獲取所有餐點的平均評分
        return mealRatingService.getAllMealAvgStars(); // 調用服務層方法獲取平均評分列表
    }
}

