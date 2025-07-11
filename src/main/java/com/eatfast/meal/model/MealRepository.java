package com.eatfast.meal.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.eatfast.common.enums.MealStatus;

@Repository
public interface MealRepository extends JpaRepository<MealEntity, Long> {
	// 查特定類別餐點（自動命名規則）
	 @Query("SELECT m FROM MealEntity m JOIN FETCH m.mealType WHERE m.mealType.mealTypeId = :mealTypeId") 
    List<MealEntity> findByMealTypeMealTypeId(Long mealTypeId);

    // 依餐點狀態查詢（1:上架, 0:下架）
    List<MealEntity> findByStatus(MealStatus status);
    
    // --- 針對 LazyInitializationException 的解決方案 ---

    // 使用 JOIN FETCH 預先載入 mealType 資訊
    @Query("SELECT m FROM MealEntity m JOIN FETCH m.mealType")
    List<MealEntity> findAllWithMealType();
    
    // 查特定類別餐點 + 狀態（例如上架 / 下架）
    @Query("SELECT m FROM MealEntity m JOIN FETCH m.mealType WHERE m.mealType.mealTypeId = :mealTypeId AND m.status = :status")
    List<MealEntity> findByMealTypeMealTypeIdAndStatus(Long mealTypeId, MealStatus status);

}

