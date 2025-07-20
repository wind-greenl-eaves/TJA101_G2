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
    
    // 查詢門市名稱資料時，會按照門市Id排序
    List<StoreEntity> findAllByOrderByStoreIdAsc();
    
    /**
     * 使用 JPQL 進行動態的多條件查詢。
     * 這個查詢會檢查每個傳入的參數：
     * - 如果參數是 NULL，則該條件會被忽略。
     * - 如果參數有值，則會被納入 WHERE 子句中。
     * 這完美地解決了因部分參數為 NULL 而導致查詢失敗的問題。
     *
     * @param storeName 門市名稱 (模糊查詢)
     * @param storeLoc 門市地點 (模糊查詢)
     * @param storeTime 營業時間 (模糊查詢)
     * @param storeStatus 營業狀態 (精確匹配)
     * @return 符合條件的門市實體列表
     */
    @Query("SELECT s FROM StoreEntity s WHERE " +
            "(:storeName IS NULL OR s.storeName LIKE %:storeName%) AND " +
            "(:storeLoc IS NULL OR s.storeLoc LIKE %:storeLoc%) AND " +
            "(:storeTime IS NULL OR s.storeTime LIKE %:storeTime%) AND " +
            "(:storeStatus IS NULL OR s.storeStatus = :storeStatus)")
     List<StoreEntity> searchStores(
             @Param("storeName") String storeName,
             @Param("storeLoc") String storeLoc,
             @Param("storeTime") String storeTime,
             @Param("storeStatus") StoreStatus storeStatus);

    
    
 // 【新增方法 1】查詢所有「非總部」的門市，並按名稱排序
    // Spring Data JPA 會自動解析方法名稱，產生對應的 JPQL
    List<StoreEntity> findAllByStoreTypeNotOrderByStoreIdAsc(StoreType storeType);

    /**
    /**
     * 【新增 @Query 註解】
     * 為這個複雜的搜尋方法明確指定 JPQL 查詢語句。
     * Spring Data JPA 將會執行這個查詢，而不是去解析方法名稱。
     * 這個查詢會動態地根據傳入的參數是否為 null 來組合查詢條件。
     * 同時，它會過濾掉「總部」和「已歇業」的門市。
     */
    @Query("SELECT s FROM StoreEntity s WHERE " +
           "(:storeName IS NULL OR s.storeName LIKE %:storeName%) AND " +
           "(:storeLoc IS NULL OR s.storeLoc LIKE %:storeLoc%) AND " +
           "(:storeTime IS NULL OR s.storeTime LIKE %:storeTime%) AND " +
           "(:storeStatus IS NULL OR s.storeStatus = :storeStatus) AND " +
           "s.storeType != com.eatfast.common.enums.StoreType.HEADQUARTERS AND " +
           "s.storeStatus != com.eatfast.common.enums.StoreStatus.ENDED")
    List<StoreEntity> searchPublicStores(
            @Param("storeName") String storeName,
            @Param("storeLoc") String storeLoc,
            @Param("storeTime") String storeTime,
            @Param("storeStatus") StoreStatus storeStatus);
    
    
    /**
     * 【修改】查詢所有「非總部」且「非已歇業」的公開門市，並按名稱排序。
     * 我們使用 @Query 來明確定義查詢邏輯，取代原先冗長的方法名稱。
     * 這裡的 com.eatfast.common.enums.StoreType.HEADQUARTERS 是 Enum 的完整路徑，
     * 確保 JPQL 能夠正確解析。
     * @return 符合條件的公開門市實體列表
     */
    @Query("SELECT s FROM StoreEntity s WHERE " +
           "s.storeType != com.eatfast.common.enums.StoreType.HEADQUARTERS AND " +
           "s.storeStatus != com.eatfast.common.enums.StoreStatus.ENDED " +
           "ORDER BY s.storeName ASC")
    List<StoreEntity> findPublicAndActiveStores();


}

