package com.eatfast.store.repository;

import com.eatfast.common.enums.StoreStatus;
import com.eatfast.store.model.StoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // 1. 引入 JpaSpecificationExecutor
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * StoreEntity 的資料存取庫 (Repository)。
 *
 * 【核心修正】
 * 除了繼承 JpaRepository 來獲得標準 CRUD 功能外，
 * 還必須繼承 JpaSpecificationExecutor<StoreEntity> 來獲得執行 Specification 動態查詢的能力。
 * 如此一來，這個介面才能使用 findAll(Specification<T> spec) 方法。
 */
@Repository
public interface StoreRepository extends JpaRepository<StoreEntity, Long>, JpaSpecificationExecutor<StoreEntity> { // 2. 在此處加上繼承

    /**
     * 根據門市名稱查詢門市資訊。
     * 這是 Spring Data JPA 的衍生查詢 (Derived Query)，此功能不受影響。
     * @param storeName 欲查詢的門市名稱
     * @return 一個包含 StoreEntity 的 Optional 物件
     */
    Optional<StoreEntity> findByStoreName(String storeName);
    Optional<StoreEntity> findByStoreId(Long storeId);
    
    // 根據多個條件查詢門市列表
    List<StoreEntity> findByStoreNameContainingAndStoreLocContainingAndStoreTimeContainingAndStoreStatus(
            String storeName, String storeLoc, String storeTime, StoreStatus storeStatus);
    
    // 定義只根據狀態查詢的方法 (精確匹配)
    List<StoreEntity> findByStoreStatus(StoreStatus storeStatus);
}
