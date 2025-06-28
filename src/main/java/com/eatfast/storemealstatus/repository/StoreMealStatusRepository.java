package com.eatfast.storemealstatus.repository;

import com.eatfast.storemealstatus.model.StoreMealStatusEntity;
import com.eatfast.storemealstatus.model.StoreMealStatusId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional; // 引入 Optional，用於查無資料時返回Optional.empty()

@Repository
public interface StoreMealStatusRepository extends JpaRepository<StoreMealStatusEntity, StoreMealStatusId> {

    // --- 查詢多筆結果的方法 ---
    // 這些方法在查詢無資料時會返回一個空的 List (而不是 null)，因此無需使用 Optional。

    /**
     * 根據門市ID查詢所有餐點狀態。
     * @param storeId 門市ID
     * @return 該門市下的所有餐點狀態列表，若無資料則返回空列表。
     */
    List<StoreMealStatusEntity> findByStoreId(Integer storeId); // 變數名稱 `storeId` 是可以自定義的

    /**
     * 根據餐點狀態查詢所有餐點。
     * @param status 餐點狀態字串
     * @return 該狀態下的所有餐點列表，若無資料則返回空列表。
     */
    List<StoreMealStatusEntity> findByStatus(String status); // 變數名稱 `status` 是可以自定義的

    /**
     * 根據門市ID和餐點狀態查詢對應餐點ID（即符合條件的餐點狀態記錄）。
     * @param storeId 門市ID
     * @param status 餐點狀態字串
     * @return 符合條件的餐點狀態列表，若無資料則返回空列表。
     */
    List<StoreMealStatusEntity> findByStoreIdAndStatus(Integer storeId, String status);


    // --- 查詢單一結果的方法 ---
    // 這些方法在查詢無資料時應返回 Optional.empty()，以避免 NullPointerException。

    /**
     * 根據 storeId 和 mealId 查詢單一餐點狀態。
     * @param storeId 門市ID
     * @param mealId 餐點ID
     * @return 包含 StoreMealStatusEntity 的 Optional 物件，若無資料則返回 Optional.empty()。
     * 使用 Optional 能更安全地處理查詢結果可能為空的情況。
     */
    Optional<StoreMealStatusEntity> findByStoreIdAndMealId(Integer storeId, Integer mealId); // 變數名稱 `storeId` 和 `mealId` 是可以自定義的
}