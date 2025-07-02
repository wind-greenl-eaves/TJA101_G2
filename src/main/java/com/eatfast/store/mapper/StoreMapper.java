// =================================================================================
// 檔案 2/5: StoreMapper.java (★★ 功能擴充 ★★)
// 路徑: src/main/java/com/eatfast/store/mapper/StoreMapper.java
// 說明: 新增了 toUpdateRequest 方法，用於從 StoreDto 生成 UpdateStoreRequest。
// =================================================================================
package com.eatfast.store.mapper;

import com.eatfast.store.dto.CreateStoreRequest;
import com.eatfast.store.dto.StoreDto;
import com.eatfast.store.dto.UpdateStoreRequest;
import com.eatfast.store.model.StoreEntity;
import org.springframework.stereotype.Component;

@Component
public class StoreMapper {

    public StoreDto toDto(StoreEntity entity) {
        if (entity == null) return null;
        StoreDto dto = new StoreDto();
        dto.setStoreId(entity.getStoreId());
        dto.setStoreName(entity.getStoreName());
        dto.setStoreLoc(entity.getStoreLoc());
        dto.setStorePhone(entity.getStorePhone());
        dto.setStoreTime(entity.getStoreTime());
        dto.setStoreStatus(entity.getStoreStatus());
        dto.setCreateTime(entity.getCreateTime());
        return dto;
    }

    public StoreEntity toEntity(CreateStoreRequest request) {
        if (request == null) return null;
        StoreEntity entity = new StoreEntity();
        entity.setStoreName(request.getStoreName());
        entity.setStoreLoc(request.getStoreLoc());
        entity.setStorePhone(request.getStorePhone());
        entity.setStoreTime(request.getStoreTime());
        entity.setStoreStatus(request.getStoreStatus());
        return entity;
    }
    
    /**
     * 【全新方法】將 StoreDto 轉換為 UpdateStoreRequest。
     * 說明: 當使用者進入「修改頁面」時，此方法用於將查詢到的門市資料預先填入表單中。
     * @param dto 來源 StoreDto 物件。
     * @return 轉換後的 UpdateStoreRequest 物件。
     */
    public UpdateStoreRequest toUpdateRequest(StoreDto dto) {
        if (dto == null) return null;
        UpdateStoreRequest request = new UpdateStoreRequest();
        request.setStoreId(dto.getStoreId());
        request.setStoreName(dto.getStoreName());
        request.setStoreLoc(dto.getStoreLoc());
        request.setStorePhone(dto.getStorePhone());
        request.setStoreTime(dto.getStoreTime());
        request.setStoreStatus(dto.getStoreStatus());
        return request;
    }
}