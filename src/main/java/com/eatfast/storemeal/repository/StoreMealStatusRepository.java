package com.eatfast.storemeal.repository;

import com.eatfast.storemeal.model.StoreMealStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * StoreMealStatusEntity 的資料存取庫 (Repository)。
 * 繼承 JpaRepository 來自動獲得所有標準的 CRUD 功能。
 */
@Repository // (可選) 標記這是一個 Spring Bean，語意上更清晰
public interface StoreMealStatusRepository extends JpaRepository<StoreMealStatusEntity, Long> {

    // --- 以下為自定義衍生查詢 (Derived Queries)，強烈建議加入 ---
    
    /**
     * 【核心查詢】根據門市 ID 和餐點 ID 查找唯一的供應狀態紀錄。
     * 這是最常用的查詢，因為您的 Entity 中有 store_id 和 meal_id 的唯一約束。
     * Spring Data JPA 會自動解析方法名稱，生成 "SELECT * FROM store_meal_status WHERE store_id = ? AND meal_id = ?" 的查詢。
     *
     * @param storeId 門市的 ID
     * @param mealId  餐點的 ID
     * @return Optional 包裝的 StoreMealStatusEntity 物件，用於優雅地處理可能找不到結果的情況
     */
    Optional<StoreMealStatusEntity> findByStore_StoreIdAndMeal_MealId(Integer storeId, Long mealId);

    /**
     * 查找某個特定門市所有的餐點供應狀態。
     * @param storeId 門市的 ID
     * @return 該門市所有餐點狀態的列表
     */
    List<StoreMealStatusEntity> findByStore_StoreId(Integer storeId);
    
    /**
     * 查找某個特定餐點在所有門市的供應狀態。
     * @param mealId 餐點的 ID
     * @return 該餐點在所有門市狀態的列表
     */
    List<StoreMealStatusEntity> findByMeal_MealId(Long mealId);
}