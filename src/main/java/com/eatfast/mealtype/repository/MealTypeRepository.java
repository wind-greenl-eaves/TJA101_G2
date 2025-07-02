/*
 * ================================================================
 * 檔案 2: MealTypeRepository.java (★★ 核心修正 ★★)
 * ================================================================
 * - 存放目錄: src/main/java/com/eatfast/mealtype/repository/MealTypeRepository.java
 * - 核心改動:
 * 1. 修正 JpaRepository 的主鍵泛型，從 Integer 改為 Long。
 * 2. 調整 package 路徑至 repository，與其他模組保持一致。
 */
package com.eatfast.mealtype.repository;

import com.eatfast.mealtype.model.MealTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
// 【關鍵修正】: 主鍵型別從 Integer 改為 Long，必須與 MealTypeEntity 的 @Id 欄位型別一致。
public interface MealTypeRepository extends JpaRepository<MealTypeEntity, Long> {
    
    // 【優化】: 新增一個衍生查詢，用於在新增或修改時，檢查名稱是否已存在。
    Optional<MealTypeEntity> findByMealName(String mealName);
}