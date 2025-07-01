package com.eatfast.storemeal.service;

import com.eatfast.common.enums.SupplyStatus;
import com.eatfast.storemeal.exception.MealNotFoundException;
import com.eatfast.meal.model.MealEntity;
import com.eatfast.meal.repository.MealRepository;
import com.eatfast.store.exception.StoreNotFoundException;
import com.eatfast.store.model.StoreEntity;
import com.eatfast.store.repository.StoreRepository;
import com.eatfast.storemeal.model.StoreMealStatusEntity;
import com.eatfast.storemeal.repository.StoreMealStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 門市餐點供應狀態服務層 (Service)。
 * 處理所有與門市餐點狀態相關的商業邏輯。
 */
@Service
@Transactional(readOnly = true)
public class StoreMealStatusService {

    private static final Logger log = LoggerFactory.getLogger(StoreMealStatusService.class);

    private final StoreMealStatusRepository statusRepository;
    private final StoreRepository storeRepository;
    private final MealRepository mealRepository;

    /**
     * 建構子注入 (Constructor Injection)。
     * 注入所有需要的 Repository。
     */
    public StoreMealStatusService(StoreMealStatusRepository statusRepository,
                                  StoreRepository storeRepository,
                                  MealRepository mealRepository) {
        this.statusRepository = statusRepository;
        this.storeRepository = storeRepository;
        this.mealRepository = mealRepository;
    }

    /**
     * 【核心業務】更新或建立一筆門市餐點的供應狀態。
     * 此方法會自動判斷：如果狀態紀錄已存在，則更新；如果不存在，則建立一筆新的。
     *
     * @param storeId 門市 ID
     * @param mealId  餐點 ID
     * @param newStatus 新的供應狀態 (SupplyStatus.AVAILABLE 或 SupplyStatus.UNAVAILABLE)
     * @return 更新或建立後的 StoreMealStatusEntity 物件
     * @throws StoreNotFoundException 如果提供的 storeId 無效
     * @throws MealNotFoundException 如果提供的 mealId 無效
     */
    @Transactional // 標記為可寫入交易
    public StoreMealStatusEntity updateSupplyStatus(Integer storeId, Long mealId, SupplyStatus newStatus) {
        log.info("準備更新門市 ID: {}, 餐點 ID: {} 的供應狀態為: {}", storeId, mealId, newStatus);
        
        // 嘗試查找現有的狀態紀錄
        Optional<StoreMealStatusEntity> existingStatusOpt =
            statusRepository.findByStore_StoreIdAndMeal_MealId(storeId, mealId);

        if (existingStatusOpt.isPresent()) {
            // --- 情況 1: 紀錄已存在，執行更新 ---
            StoreMealStatusEntity existingStatus = existingStatusOpt.get();
            log.debug("找到現有紀錄，進行狀態更新。");
            existingStatus.setStatus(newStatus);
            // existingStatus.setLastUpdatedAt(...) 會由 @UpdateTimestamp 自動處理
            return statusRepository.save(existingStatus);
        } else {
            // --- 情況 2: 紀錄不存在，執行新增 ---
            log.debug("未找到現有紀錄，準備建立新紀錄。");
            
            // 為了建立關聯，需要先從資料庫取得 Store 和 Meal 的實體物件
            StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("找不到 ID 為 " + storeId + " 的門市"));
            
            MealEntity meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new MealNotFoundException("找不到 ID 為 " + mealId + " 的餐點"));

            // 建立新的狀態實體並設定所有必要屬性
            StoreMealStatusEntity newStatusEntity = new StoreMealStatusEntity();
            newStatusEntity.setStore(store);
            newStatusEntity.setMeal(meal);
            newStatusEntity.setStatus(newStatus);
            
            return statusRepository.save(newStatusEntity);
        }
    }

    /**
     * 【查詢】獲取特定門市特定餐點的供應狀態。
     * @param storeId 門市 ID
     * @param mealId 餐點 ID
     * @return Optional 包裝的 StoreMealStatusEntity 物件
     */
    public Optional<StoreMealStatusEntity> getStatusForStoreAndMeal(Integer storeId, Long mealId) {
        log.debug("查詢門市 ID: {}, 餐點 ID: {} 的供應狀態", storeId, mealId);
        return statusRepository.findByStore_StoreIdAndMeal_MealId(storeId, mealId);
    }
    
    /**
     * 【查詢】獲取特定門市所有餐點的供應狀態列表。
     * @param storeId 門市 ID
     * @return 狀態列表
     */
    public List<StoreMealStatusEntity> getAllStatusesForStore(Integer storeId) {
        log.debug("查詢門市 ID: {} 的所有餐點供應狀態", storeId);
        return statusRepository.findByStore_StoreId(storeId);
    }
}
