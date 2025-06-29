package com.eatfast.permission.mapper; // 可自定義的 package 路徑

import com.eatfast.permission.dto.PermissionDto;
import com.eatfast.permission.model.PermissionEntity;
import org.springframework.stereotype.Component;

/**
 * [可自定義的類別名稱]: PermissionMapper
 * 權限物件的轉換器 (Mapper)，負責 Entity 與 DTO 之間的轉換。
 *
 * - @Component: (不可變動) 標記此類別為 Spring Bean，交由容器管理。
 */
@Component
public class PermissionMapper {

    /**
     * [可自定義的方法名稱]: toDto
     * 將資料庫實體 (PermissionEntity) 轉換為資料傳輸物件 (PermissionDto)。
     *
     * @param entity (可自定義的參數名): 來源 PermissionEntity 物件。
     * @return 轉換後的 PermissionDto 物件；若來源為 null 則回傳 null。
     */
    public PermissionDto toDto(PermissionEntity entity) {
        // 防呆設計，避免 NullPointerException
        if (entity == null) {
            return null;
        }

        PermissionDto dto = new PermissionDto();
        dto.setPermissionId(entity.getPermissionId());
        dto.setDescription(entity.getDescription());
        return dto;
    }
}
