package com.eatfast.store.service;

import com.eatfast.common.enums.StoreStatus;
import com.eatfast.store.dto.CreateStoreRequest;
import com.eatfast.store.dto.StoreDto;
import com.eatfast.store.dto.UpdateStoreRequest;
import com.eatfast.store.exception.StoreNotFoundException;
import com.eatfast.store.mapper.StoreMapper;
import com.eatfast.store.model.StoreEntity; // 確保引入 StoreEntity
import com.eatfast.store.repository.StoreRepository; // 確保引入 StoreRepository

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors; // 引入 Collectors


	public interface StoreService {

	    // 【可自定義名稱】: createStore, updateStore 等方法名稱
	    // 【不可變動的關鍵字/語法】: public, StoreDto, List 等返回類型和參數類型
	    StoreDto createStore(CreateStoreRequest request);

	    List<StoreDto> findAllStores();

	    StoreDto findStoreById(Long storeId);

	    StoreDto updateStore(Long storeId, UpdateStoreRequest request);

	    void deleteStore(Long storeId);

	    // 確保這裡的 searchStores 方法簽名與 StoreServiceImpl 中的實作完全一致
	    // 參數 storeStatus 應該是 StoreStatus 枚舉類型
	    List<StoreDto> searchStores(String storeName, String storeLoc, String storeTime, StoreStatus storeStatus);
	}