package com.eatfast.employee.mapper;

import com.eatfast.common.enums.AccountStatus;
import com.eatfast.employee.dto.CreateEmployeeRequest;
import com.eatfast.employee.dto.EmployeeDTO;
import com.eatfast.employee.model.EmployeeEntity;
import org.springframework.stereotype.Component;

/**
 * [可自定義的類別名稱]: EmployeeMapper
 * 物件轉換器 (Mapper)，負責處理 Entity 與 DTO 之間的轉換。
 * @Component: 將此類別標記為 Spring Bean，交由容器管理。
 */
@Component
public class EmployeeMapper {

    /**
     * [可自定義的方法]: toDto
     * 將資料庫實體 (EmployeeEntity) 轉換為資料傳輸物件 (EmployeeDto)。
     *
     * @param entity (可自定義的參數名): 來源 EmployeeEntity 物件。
     * @return 轉換後的 EmployeeDto 物件。
     */
    public EmployeeDTO toDto(EmployeeEntity entity) {
        // 防呆設計：若來源實體為 null，直接回傳 null，避免後續發生 NullPointerException。
        if (entity == null) {
            return null;
        }

        // 建立 DTO 物件並逐一映射欄位值。
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmployeeId(entity.getEmployeeId());
        dto.setUsername(entity.getUsername());
        dto.setAccount(entity.getAccount());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());
        dto.setRole(entity.getRole());
        dto.setStatus(entity.getStatus());
        dto.setGender(entity.getGender());
        dto.setNationalId(entity.getNationalId());
        dto.setCreateTime(entity.getCreateTime());
        // 添加照片 URL 映射
        dto.setPhotoUrl(entity.getPhotoUrl());

        // 安全地處理關聯物件：確認 StoreEntity 不為 null 才進行取值，避免 NullPointerException。
        if (entity.getStore() != null) {
            dto.setStoreId(entity.getStore().getStoreId());
            dto.setStoreName(entity.getStore().getStoreName());
        }

        return dto;
    }

    /** 將新增請求 DTO 轉換為 Entity */
    public EmployeeEntity toEntity(CreateEmployeeRequest request) {
        if (request == null) {
            return null;
        }
        EmployeeEntity entity = new EmployeeEntity();
        entity.setUsername(request.getUsername());
        entity.setAccount(request.getAccount());
        entity.setPassword(request.getPassword()); // 注意：此處為未加密密碼
        entity.setEmail(request.getEmail());
        entity.setPhone(request.getPhone());
        entity.setRole(request.getRole());
        entity.setGender(request.getGender());
        entity.setNationalId(request.getNationalId());
        // 預設新增的員工狀態為啟用
        entity.setStatus(AccountStatus.ACTIVE);
        return entity;
    }
}