// =================================================================================
// 檔案: StoreServiceImpl.java (★★ 企業級最終版 ★★)
// 路徑: src/main/java/com/eatfast/store/service/StoreServiceImpl.java
// 說明: 實作 StoreService 介面，包含完整的業務邏輯與 DTO 處理。
// =================================================================================
package com.eatfast.store.service;

import com.eatfast.common.enums.StoreStatus;
import com.eatfast.common.enums.StoreType;
import com.eatfast.store.dto.CreateStoreRequest;
import com.eatfast.store.dto.StoreDto;
import com.eatfast.store.dto.UpdateStoreRequest;
import com.eatfast.store.exception.StoreNotFoundException;
import com.eatfast.store.mapper.StoreMapper;
import com.eatfast.store.model.StoreEntity;
import com.eatfast.store.repository.StoreRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;
 // 【新增】使用 @Value 註解從 application.properties 注入 Google Maps API Key
    @Value("${google.maps.api.key}")
    private String googleMapsApiKey;

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
        // 【核心修改】在儲存到資料庫前，根據地址生成地圖 URL
        String mapUrl = generateGoogleMapEmbedUrl(request.getStoreLoc());
        newStore.setGoogleMapUrl(mapUrl);
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
        // 如果這次更新包含了地址，就要重新生成地圖 URL
        if (StringUtils.hasText(request.getStoreLoc())) {
            existingStore.setStoreLoc(request.getStoreLoc());
            // 呼叫輔助方法，用新的地址生成新的 URL
            String newMapUrl = generateGoogleMapEmbedUrl(request.getStoreLoc());
            existingStore.setGoogleMapUrl(newMapUrl);
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
    public List<StoreDto> findAllPublicStores() {
        // 【修改】呼叫我們在 Repository 中使用 @Query 定義的新方法
        List<StoreEntity> publicStores = storeRepository.findPublicAndActiveStores();
        return storeMapper.toDtoList(publicStores);
    }

    @Override
    public StoreDto findStoreById(Long storeId) {
        return storeRepository.findById(storeId)
                .map(storeMapper::toDto)
                .orElseThrow(() -> new StoreNotFoundException("找不到 ID 為 " + storeId + " 的門市"));
    }

    @Override
    public List<StoreDto> findAllStores() {
        // 【修改】從呼叫 findAll() 改為呼叫我們新定義的排序方法
        List<StoreEntity> sortedStores = storeRepository.findAllByOrderByStoreNameAsc();

        // 後續的 Mapper 轉換邏輯不變，將排序好的 Entity 列表轉為 DTO 列表
        return storeMapper.toDtoList(sortedStores); 
    }
    
    @Override
    public List<StoreDto> searchStores(String storeName, String storeLoc, String storeTime, StoreStatus storeStatus) {
        // 調用 Repository 中定義的命名查詢方法
        // Spring Data JPA 會自動處理 null 參數，不將其納入查詢條件
        List<StoreEntity> entities = storeRepository.findByStoreNameContainingAndStoreLocContainingAndStoreTimeContainingAndStoreStatus(
                storeName, storeLoc, storeTime, storeStatus);

        // 將查詢到的 Entity 列表轉換為 DTO 列表並返回
        return entities.stream()
                .map(storeMapper::toDto)
                .collect(Collectors.toList());
    }

   // @Override 會員拉取這方法可以先不用到了 前端不另外抓
 //   public List<StoreEntity> getAllStores() {
    //    return List.of();
    
//     @Override
//     public List<StoreDto> getAllStoreDTOs() {
//         List<StoreEntity> stores = storeRepository.findAll();
//         return stores.stream()
//                      .map(store -> new StoreDto(store.getStoreId(), store.getStoreName()))
//                      .toList();
//     }
    
    
    @Override
    public List<StoreDto> getAllStoreDTOs() {
        List<StoreEntity> stores = storeRepository.findAll();
        return stores.stream()
                     .map(store -> new StoreDto(store.getStoreId(), store.getStoreName()))
                     .toList();
    }
    
    
 // 【新增】一個私有的輔助方法，專門用來生成 Google Map 嵌入 URL
    private String generateGoogleMapEmbedUrl(String address) {
        // 如果地址為空，則返回空字串
        if (address == null || address.trim().isEmpty()) {
            return "";
        }
        try {
            // 對地址進行 URL 編碼，以處理特殊字元和空格
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            // 組合成標準的 Google Maps Embed API URL
            return String.format("https://www.google.com/maps/embed/v1/place?key=%s&q=%s",
                                 googleMapsApiKey, encodedAddress);
        } catch (Exception e) {
            // 在真實專案中，這裡應該記錄錯誤日誌 (log.error)
            System.err.println("Error encoding address: " + address);
            return ""; // 編碼失敗時返回空字串
        }
    }
    
    
    //  前端客戶用方法 (Customer-facing methods) - 【★★ 本次新增 ★★】
    // =================================================================================

    
//  前端客戶用方法
    /**
     * 【新增】提供給前端客戶頁面，根據條件搜尋可顯示的「分店」門市。
     * 這個方法會自動過濾掉類型為 HEADQUARTERS 的總部資料。
     * searchPublicStores 這個方法名稱是可以自定義的。
     * @return 不包含總部的符合條件門市 DTO 列表
     */
    @Override
    public List<StoreDto> searchPublicStores(String storeName, String storeLoc, String storeTime, StoreStatus storeStatus) {
        // 3. 呼叫我們在 Repository 中使用 @Query 定義的新方法
        List<StoreEntity> entities = storeRepository.searchPublicStores(storeName, storeLoc, storeTime, storeStatus);
        return storeMapper.toDtoList(entities);
    }

     @Override //會員拉取這方法可以先不用到了 前端不另外抓
       public List<StoreEntity> getAllStores() {
           return List.of();
     }
}
