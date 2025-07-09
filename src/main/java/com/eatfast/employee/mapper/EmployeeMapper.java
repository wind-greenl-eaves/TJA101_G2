// ================================================================
// 檔案名稱: EmployeeMapper.java
// 功能說明: 員工實體與DTO之間的對象映射轉換器
// 架構層級: 映射層 (Mapper Layer)
// 配對關係:
//   - 實體層: EmployeeEntity - 資料庫實體對象
//   - DTO層: EmployeeDTO, CreateEmployeeRequest - 資料傳輸對象
//   - 服務層: EmployeeServiceImpl - 使用此映射器進行轉換
//   - 控制器: 間接使用 - 透過服務層獲得轉換後的DTO
// 設計模式: 
//   - Mapper Pattern (映射器模式)
//   - Object-to-Object Mapping
//   - Data Transfer Object Pattern
// 轉換方向:
//   - Entity → DTO: toDto() - 用於資料查詢與API響應
//   - DTO → Entity: toEntity() - 用於資料新增與更新
// 安全考量: 確保敏感資料的正確映射與過濾
// ================================================================
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
        dto.setPhotoUrl(entity.getPhotoUrl());
        
        // 【恢復】現在使用明文密碼，可以安全地將密碼映射到 DTO
        dto.setPassword(entity.getPassword());

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