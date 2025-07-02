package com.eatfast.meal.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MealRepository extends JpaRepository<MealEntity, Long> {
	// 查特定類別餐點（自動命名規則）
    List<MealEntity> findByMealTypeMealTypeId(Long mealTypeId);

    // 依餐點狀態查詢（1:上架, 0:下架）
    List<MealEntity> findByStatus(Integer status);
}
