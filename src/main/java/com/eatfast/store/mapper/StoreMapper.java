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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    
    public List<StoreDto> toDtoList(List<StoreEntity> entities) {
        // 防呆設計：如果傳入的列表是 null，就回傳一個空的列表
        if (entities == null) {
            return Collections.emptyList();
        }

        // 使用 Java Stream API:
        // 1. .stream()       : 將 Entity 列表轉換為一個串流。
        // 2. .map(this::toDto): 對串流中的「每一個」Entity 物件，都去呼叫 toDto 方法進行轉換。
        // 3. .collect(...)   : 將轉換後的所有 Dto 物件，收集起來變成一個新的 List<StoreDto>。
        return entities.stream()
                     .map(this::toDto)
                     .collect(Collectors.toList());
    }
}