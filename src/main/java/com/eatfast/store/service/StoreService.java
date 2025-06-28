/*
 * ================================================================
 * 檔案 4: StoreService.java (★★ 架構調整 ★★)
 * ================================================================
 * - 存放目錄: src/main/java/com/eatfast/store/service/StoreService.java
 * - 核心改動: 改為介面 (Interface)，只定義服務的「契約」，不包含實作。
 * 這是 Spring 的最佳實踐，有利於解耦與測試。
 */
package com.eatfast.store.service;

import com.eatfast.store.model.StoreEntity;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface StoreService {
	StoreEntity createStore(StoreEntity storeEntity);

	StoreEntity updateStore(Long storeId, StoreEntity detailsToUpdate);

	void deleteStore(Long storeId);

	Optional<StoreEntity> getStoreById(Long storeId);

	List<StoreEntity> getAllStores();

	List<StoreEntity> findStoresByCriteria(Map<String, String> params);
}