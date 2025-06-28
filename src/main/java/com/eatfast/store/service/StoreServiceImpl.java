/*
 * ================================================================
 * 檔案 5: StoreServiceImpl.java (★★ 核心重構 ★★)
 * ================================================================
 * - 存放目錄: src/main/java/com/eatfast/store/service/StoreServiceImpl.java
 * - 核心改動:
 * 1. 實作介面: `implements StoreService`。
 * 2. 型別修正: 所有 `Integer` ID 都修正為 `Long`。
 * 3. 邏輯強化:
 * - `createStore`: 移除多餘的 `setCreateTime`，並增加對店名的重複檢查。
 * - `updateStore`: 確保更新邏輯正確。
 * - `findStoresByCriteria`: 完成 `storeStatus` 的查詢邏輯。
 * 4. 遵循最佳實踐: 使用建構子注入，並妥善管理交易。
 */
package com.eatfast.store.service;

import com.eatfast.store.exception.StoreNotFoundException;
import com.eatfast.store.model.StoreEntity;
import com.eatfast.store.model.StoreStatus;
import com.eatfast.store.repository.StoreRepository;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class StoreServiceImpl implements StoreService {

    private static final Logger log = LoggerFactory.getLogger(StoreServiceImpl.class);
    private final StoreRepository storeRepository;

    public StoreServiceImpl(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @Override
    @Transactional
    public StoreEntity createStore(StoreEntity storeEntity) {
        log.info("準備新增門市：{}", storeEntity.getStoreName());
        // 【優化】: 新增前檢查店名是否重複。
        storeRepository.findByStoreName(storeEntity.getStoreName()).ifPresent(s -> {
            throw new IllegalArgumentException("門市名稱 '" + s.getStoreName() + "' 已存在。");
        });
        // createTime 由 @CreationTimestamp 自動生成，無需手動設定。
        return storeRepository.save(storeEntity);
    }

    @Override
    @Transactional
    public StoreEntity updateStore(Long storeId, StoreEntity detailsToUpdate) {
        log.info("準備更新 ID 為 {} 的門市", storeId);
        StoreEntity existingStore = storeRepository.findById(storeId)
            .orElseThrow(() -> new StoreNotFoundException("找不到 ID 為 " + storeId + " 的門市"));

        // 【優化】: 更新前也檢查新店名是否與「其他」店家衝突。
        storeRepository.findByStoreName(detailsToUpdate.getStoreName()).ifPresent(s -> {
            if (!s.getStoreId().equals(storeId)) {
                throw new IllegalArgumentException("門市名稱 '" + s.getStoreName() + "' 已被其他門市使用。");
            }
        });
        
        existingStore.setStoreName(detailsToUpdate.getStoreName());
        existingStore.setStoreLoc(detailsToUpdate.getStoreLoc());
        existingStore.setStorePhone(detailsToUpdate.getStorePhone());
        existingStore.setStoreTime(detailsToUpdate.getStoreTime());
        existingStore.setStoreStatus(detailsToUpdate.getStoreStatus());

        return storeRepository.save(existingStore);
    }

    @Override
    @Transactional
    public void deleteStore(Long storeId) {
        log.info("準備刪除 ID 為 {} 的門市", storeId);
        if (!storeRepository.existsById(storeId)) {
            throw new StoreNotFoundException("無法刪除：找不到 ID 為 " + storeId + " 的門市");
        }
        try {
            storeRepository.deleteById(storeId);
            log.info("已成功刪除 ID 為 {} 的門市", storeId);
        } catch (DataIntegrityViolationException e) {
            log.warn("刪除門市 {} 失敗，因為尚有關聯資料。", storeId, e);
            throw new IllegalStateException("刪除失敗：此門市尚有關聯的員工或訂單，無法刪除。");
        }
    }

    @Override
    public Optional<StoreEntity> getStoreById(Long storeId) {
        return storeRepository.findById(storeId);
    }

    @Override
    public List<StoreEntity> getAllStores() {
        return storeRepository.findAll();
    }

    @Override
    public List<StoreEntity> findStoresByCriteria(Map<String, String> params) {
        // ... (複合查詢邏輯，完成 status 部分)
        Specification<StoreEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(params.get("storeName"))) {
                predicates.add(cb.like(root.get("storeName"), "%" + params.get("storeName") + "%"));
            }
            if (StringUtils.hasText(params.get("storeLoc"))) {
                predicates.add(cb.like(root.get("storeLoc"), "%" + params.get("storeLoc") + "%"));
            }
            if (StringUtils.hasText(params.get("storeStatus"))) {
                try {
                    StoreStatus status = StoreStatus.valueOf(params.get("storeStatus").toUpperCase());
                    predicates.add(cb.equal(root.get("storeStatus"), status));
                } catch (IllegalArgumentException e) {
                    log.warn("提供的門市狀態無效：{}", params.get("storeStatus"));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return storeRepository.findAll(spec);
    }
}