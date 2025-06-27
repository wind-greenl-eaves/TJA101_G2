package com.eatfast.store.service;

import com.eatfast.store.model.StoreEntity;
import com.eatfast.store.exception.StoreNotFoundException;
import com.eatfast.store.repository.StoreRepository;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 門市服務層 (Service) - 遵循最佳實踐重構。
 * 包含完整的 CRUD 操作、日誌紀錄與明確的例外處理。
 */
@Service
@Transactional(readOnly = true) // 預設所有方法都是唯讀交易，可提升查詢效能
public class StoreService {

    private static final Logger log = LoggerFactory.getLogger(StoreService.class);

    private final StoreRepository storeRepository;

    /**
     * 建構子注入 (Constructor Injection)。
     * Spring 會自動傳入 StoreRepository 的實例。
     * @param storeRepository 資料存取庫
     */
    public StoreService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    /**
     * 【新增】一間新門市。
     * @param storeEntity 包含新門市資料的物件
     * @return 儲存後包含 ID 的門市物件
     */
    @Transactional // 覆蓋類別層級的設定，標記為可寫入交易
    public StoreEntity createStore(StoreEntity storeEntity) {
        log.info("準備新增門市：{}", storeEntity.getStoreName());
        storeEntity.setCreateTime(LocalDateTime.now());
        return storeRepository.save(storeEntity);
    }

    /**
     * 【更新】現有門市的資料。
     * 採用 findById -> map -> save 的模式，程式碼更簡潔且安全。
     * @param storeId 欲更新的門市 ID
     * @param detailsToUpdate 包含要更新資料的物件
     * @return 更新後的門市物件
     * @throws StoreNotFoundException 如果找不到對應 ID 的門市
     */
    @Transactional
    public StoreEntity updateStore(Integer storeId, StoreEntity detailsToUpdate) {
        log.info("準備更新 ID 為 {} 的門市", storeId);
        // 1. 先從資料庫根據 ID 查找現有門市
        StoreEntity existingStore = storeRepository.findById(storeId)
            // 2. 如果找不到，拋出我們自定義的例外
            .orElseThrow(() -> new StoreNotFoundException("找不到 ID 為 " + storeId + " 的門市"));

        // 3. 如果找到了，將傳入的更新資料設定到現有物件上
        existingStore.setStoreName(detailsToUpdate.getStoreName());
        existingStore.setStoreLoc(detailsToUpdate.getStoreLoc());
        existingStore.setStorePhone(detailsToUpdate.getStorePhone());
        existingStore.setStoreTime(detailsToUpdate.getStoreTime());
        existingStore.setStoreStatus(detailsToUpdate.getStoreStatus());

        // 4. 儲存更新後的物件，JPA 會自動執行 UPDATE
        return storeRepository.save(existingStore);
    }

    /**
     * 【刪除】指定 ID 的門市。
     * @param storeId 欲刪除的門市 ID
     * @throws StoreNotFoundException 如果找不到對應 ID 的門市
     */
    @Transactional
    public void deleteStore(Integer storeId) {
        log.info("準備刪除 ID 為 {} 的門市", storeId);
        // 先檢查是否存在，若不存在則拋出例外，避免執行無效的刪除操作
        if (!storeRepository.existsById(storeId)) {
            throw new StoreNotFoundException("無法刪除：找不到 ID 為 " + storeId + " 的門市");
        }
        storeRepository.deleteById(storeId);
        log.info("已成功刪除 ID 為 {} 的門市", storeId);
    }

    // --- 以下為查詢方法 ---

    /**
     * 【查詢】根據 ID 查找門市。
     * @param storeId 門市 ID
     * @return Optional 包裝的門市物件
     */
    public Optional<StoreEntity> getStoreById(Integer storeId) {
        log.debug("正在根據 ID 查詢門市：{}", storeId);
        return storeRepository.findById(storeId);
    }
    
    /**
     * 【查詢】所有門市。
     * @return 門市列表
     */
    public List<StoreEntity> getAllStores() {
        log.debug("正在查詢所有門市");
        return storeRepository.findAll();
    }
    
    /**
     * 【複合查詢】根據傳入的多個條件動態查詢門市。
     * @param params 包含查詢條件的 Map，Key 為欄位名，Value 為查詢值
     * @return 符合條件的門市列表
     */
    public List<StoreEntity> findStoresByCriteria(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return getAllStores();
        }
        log.info("執行門市複合查詢，條件為：{}", params);

        Specification<StoreEntity> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 根據門市名稱模糊查詢
            if (StringUtils.hasText(params.get("storeName"))) {
                predicates.add(criteriaBuilder.like(root.get("storeName"), "%" + params.get("storeName") + "%"));
            }

            // 根據門市地點模糊查詢
            if (StringUtils.hasText(params.get("storeLoc"))) {
                predicates.add(criteriaBuilder.like(root.get("storeLoc"), "%" + params.get("storeLoc") + "%"));
            }
            
            // 根據門市狀態精確查詢
            if (StringUtils.hasText(params.get("storeStatus"))) {
                try {
                    // 這邊需要您的 StoreStatus Enum 來做轉換
                    // StoreStatus status = StoreStatus.valueOf(params.get("storeStatus").toUpperCase());
                    // predicates.add(criteriaBuilder.equal(root.get("storeStatus"), status));
                } catch (IllegalArgumentException e) {
                    log.warn("提供的門市狀態無效：{}", params.get("storeStatus"));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return storeRepository.findAll(spec);
    }
}
