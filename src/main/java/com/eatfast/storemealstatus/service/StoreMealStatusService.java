package com.eatfast.storemealstatus.service;

import com.eatfast.storemealstatus.model.StoreMealStatusEntity;
import com.eatfast.storemealstatus.model.StoreMealStatusId; // 引入複合主鍵類別
import com.eatfast.storemealstatus.repository.StoreMealStatusRepository; // 引入 Repository 介面
import org.springframework.beans.factory.annotation.Autowired; // 引入自動注入註解
import org.springframework.stereotype.Service; // 引入 @Service 註解

import java.time.LocalDateTime; // 引入 LocalDateTime 用於時間戳
import java.util.List; // 引入 List 集合
import java.util.Optional; // 引入 Optional 處理可能為空的值

/**
 * StoreMealStatusService 負責處理門市餐點狀態的業務邏輯。
 * 透過注入 StoreMealStatusRepository 來執行資料庫操作。
 */
@Service // 標記這是一個 Spring 服務組件
public class StoreMealStatusService {

    @Autowired // 自動注入 StoreMealStatusRepository 實作
    private StoreMealStatusRepository storeMealStatusRepository; // `storeMealStatusRepository` 是可以自定義的變數名稱

    /**
     * 新增或更新門市餐點狀態。
     * 如果複合主鍵 (storeId, mealId) 存在則更新，不存在則新增。
     *
     * @param storeMealStatus 要儲存的 StoreMealStatusEntity 物件
     * @return 儲存後的 StoreMealStatusEntity 物件 (可能包含資料庫自動生成的值)
     */
    public StoreMealStatusEntity saveStoreMealStatus(StoreMealStatusEntity storeMealStatus) { // `storeMealStatus` 是可以自定義的變數名稱
        // 在新增資料時，如果 createTime 為空，則設定為當前時間
        if (storeMealStatus.getCreateTime() == null) {
            storeMealStatus.setCreateTime(LocalDateTime.now()); // `LocalDateTime.now()` 是不可變的語法
        }
        return storeMealStatusRepository.save(storeMealStatus); // `save()` 是 JpaRepository 提供的方法，不可變
    }

    /**
     * 根據門市ID和餐點ID查詢單一餐點狀態。
     * 由於複合主鍵查詢結果可能是空的，因此返回 Optional<StoreMealStatusEntity>。
     *
     * @param storeId 門市ID
     * @param mealId 餐點ID
     * @return 包含 StoreMealStatusEntity 的 Optional 物件，若查無資料則返回 Optional.empty()。
     */
    public Optional<StoreMealStatusEntity> getSingleMealStatus(Integer storeId, Integer mealId) { // `getSingleMealStatus`, `storeId`, `mealId` 是可以自定義的變數名稱
        // 建立複合主鍵物件以供 findById 使用
        StoreMealStatusId id = new StoreMealStatusId(storeId, mealId); // `id` 是可以自定義的變數名稱
        return storeMealStatusRepository.findById(id); // `findById()` 是 JpaRepository 提供的方法，不可變
    }

    /**
     * 查詢所有門市餐點狀態。
     * 若無任何資料，則返回一個空的列表，而非 null。
     *
     * @return 所有 StoreMealStatusEntity 的列表
     */
    public List<StoreMealStatusEntity> getAllMealStatuses() { // `getAllMealStatuses` 是可以自定義的變數名稱
        return storeMealStatusRepository.findAll(); // `findAll()` 是 JpaRepository 提供的方法，不可變
    }

    /**
     * 根據門市ID查詢所有餐點狀態。
     * 若該門市無任何餐點狀態資料，則返回一個空的列表。
     *
     * @param storeId 門市ID
     * @return 該門市下的所有餐點狀態列表
     */
    public List<StoreMealStatusEntity> getMealStatusesByStoreId(Integer storeId) { // `getMealStatusesByStoreId` 是可以自定義的變數名稱
        return storeMealStatusRepository.findByStoreId(storeId); // `findByStoreId()` 是自定義的 Repository 方法，不可變
    }

    /**
     * 根據餐點狀態查詢所有餐點。
     * 若無任何符合該狀態的資料，則返回一個空的列表。
     *
     * @param status 餐點狀態字串
     * @return 符合該狀態的所有餐點列表
     */
    public List<StoreMealStatusEntity> getMealStatusesByStatus(String status) { // `getMealStatusesByStatus`, `status` 是可以自定義的變數名稱
        return storeMealStatusRepository.findByStatus(status); // `findByStatus()` 是自定義的 Repository 方法，不可變
    }

    /**
     * 根據門市ID和餐點狀態查詢對應餐點。
     * 若無任何符合條件的資料，則返回一個空的列表。
     *
     * @param storeId 門市ID
     * @param status 餐點狀態字串
     * @return 符合門市ID和餐點狀態的所有餐點列表
     */
    public List<StoreMealStatusEntity> getMealStatusesByStoreIdAndStatus(Integer storeId, String status) { // `getMealStatusesByStoreIdAndStatus`, `storeId`, `status` 是可以自定義的變數名稱
        return storeMealStatusRepository.findByStoreIdAndStatus(storeId, status); // `findByStoreIdAndStatus()` 是自定義的 Repository 方法，不可變
    }


    /**
     * 根據門市ID和餐點ID刪除單一餐點狀態。
     *
     * @param storeId 門市ID
     * @param mealId 餐點ID
     */
    public void deleteStoreMealStatus(Integer storeId, Integer mealId) { // `deleteStoreMealStatus`, `storeId`, `mealId` 是可以自定義的變數名稱
        StoreMealStatusId id = new StoreMealStatusId(storeId, mealId); // 建立複合主鍵物件
        storeMealStatusRepository.deleteById(id); // `deleteById()` 是 JpaRepository 提供的方法，不可變
    }

    /**
     * 刪除所有門市餐點狀態。
     */
    public void deleteAllMealStatuses() { // `deleteAllMealStatuses` 是可以自定義的變數名稱
        storeMealStatusRepository.deleteAll(); // `deleteAll()` 是 JpaRepository 提供的方法，不可變
    }
}