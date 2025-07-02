// =================================================================================
// 檔案: StoreServiceImpl.java (★★ 企業級最終版 ★★)
// 路徑: src/main/java/com/eatfast/store/service/StoreServiceImpl.java
// 說明: 實作 StoreService 介面，包含完整的業務邏輯與 DTO 處理。
// =================================================================================
package com.eatfast.store.service;

import com.eatfast.store.dto.CreateStoreRequest;
import com.eatfast.store.dto.StoreDto;
import com.eatfast.store.dto.UpdateStoreRequest;
import com.eatfast.store.exception.StoreNotFoundException;
import com.eatfast.store.mapper.StoreMapper;
import com.eatfast.store.model.StoreEntity;
import com.eatfast.store.repository.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;

    public StoreServiceImpl(StoreRepository storeRepository, StoreMapper storeMapper) {
        this.storeRepository = storeRepository;
        this.storeMapper = storeMapper;
    }

    @Override
    @Transactional
    public StoreDto createStore(CreateStoreRequest request) {
        // 驗證店名是否重複
        storeRepository.findByStoreName(request.getStoreName()).ifPresent(s -> {
            throw new IllegalArgumentException("門市名稱 '" + request.getStoreName() + "' 已存在。");
        });
        
        StoreEntity newStore = storeMapper.toEntity(request);
        StoreEntity savedStore = storeRepository.save(newStore);
        return storeMapper.toDto(savedStore);
    }

    @Override
    @Transactional
    public StoreDto updateStore(Long storeId, UpdateStoreRequest request) {
        StoreEntity existingStore = storeRepository.findById(storeId)
            .orElseThrow(() -> new StoreNotFoundException("找不到 ID 為 " + storeId + " 的門市"));

        // 檢查新店名是否與其他門市重複
        if (StringUtils.hasText(request.getStoreName())) {
            storeRepository.findByStoreName(request.getStoreName()).ifPresent(s -> {
                if (!s.getStoreId().equals(storeId)) {
                    throw new IllegalArgumentException("門市名稱 '" + request.getStoreName() + "' 已被其他門市使用。");
                }
            });
            existingStore.setStoreName(request.getStoreName());
        }
        
        // 只更新 UpdateStoreRequest 中有提供值的欄位
        if (StringUtils.hasText(request.getStoreLoc())) {
            existingStore.setStoreLoc(request.getStoreLoc());
        }
        if (StringUtils.hasText(request.getStorePhone())) {
            existingStore.setStorePhone(request.getStorePhone());
        }
        if (StringUtils.hasText(request.getStoreTime())) {
            existingStore.setStoreTime(request.getStoreTime());
        }
        if (request.getStoreStatus() != null) {
            existingStore.setStoreStatus(request.getStoreStatus());
        }

        StoreEntity updatedStore = storeRepository.save(existingStore);
        return storeMapper.toDto(updatedStore);
    }

    @Override
    @Transactional
    public void deleteStore(Long storeId) {
        // 在刪除前，先檢查是否有依賴此門市的員工存在
        StoreEntity storeToDelete = storeRepository.findById(storeId)
            .orElseThrow(() -> new StoreNotFoundException("無法刪除：找不到 ID 為 " + storeId + " 的門市"));

        if (storeToDelete.getEmployees() != null && !storeToDelete.getEmployees().isEmpty()) {
            throw new IllegalStateException("無法刪除：此門市尚有關聯的員工，請先轉移或刪除員工。");
        }
        
        // 此處可擴充對其他關聯 (如訂單) 的檢查
        
        storeRepository.deleteById(storeId);
    }

    @Override
    public StoreDto findStoreById(Long storeId) {
        return storeRepository.findById(storeId)
                .map(storeMapper::toDto)
                .orElseThrow(() -> new StoreNotFoundException("找不到 ID 為 " + storeId + " 的門市"));
    }

    @Override
    public List<StoreDto> findAllStores() {
        return storeRepository.findAll().stream()
                .map(storeMapper::toDto)
                .collect(Collectors.toList());
    }
}
