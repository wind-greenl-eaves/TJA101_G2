package com.eatfast.store.repository;

import com.eatfast.common.enums.StoreStatus;
import com.eatfast.common.enums.StoreType;
import com.eatfast.store.model.StoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // 1. 引入 JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    // 查詢門市名稱資料時，會按照中文字的排序
    List<StoreEntity> findAllByOrderByStoreNameAsc();
    
    
 // 【新增方法 1】查詢所有「非總部」的門市，並按名稱排序
    // Spring Data JPA 會自動解析方法名稱，產生對應的 JPQL
    List<StoreEntity> findAllByStoreTypeNotOrderByStoreNameAsc(StoreType storeType);

    // 【新增方法 2】提供給前端的複雜搜尋，自動排除總部
    // 使用 @Query 讓我們可以撰寫更複雜的 JPQL
    @Query("SELECT s FROM StoreEntity s WHERE " +
           "(:storeName IS NULL OR s.storeName LIKE %:storeName%) AND " +
           "(:storeLoc IS NULL OR s.storeLoc LIKE %:storeLoc%) AND " +
           "(:storeTime IS NULL OR s.storeTime LIKE %:storeTime%) AND " +
           "(:storeStatus IS NULL OR s.storeStatus = :storeStatus) AND " +
           "s.storeType != 'HEADQUARTERS'") // 核心過濾條件，排除總部可以被前端查詢
    List<StoreEntity> searchPublicStores(
            @Param("storeName") String storeName,
            @Param("storeLoc") String storeLoc,
            @Param("storeTime") String storeTime,
            @Param("storeStatus") StoreStatus storeStatus);
}
