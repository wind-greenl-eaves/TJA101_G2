package com.eatfast.permission.repository; // 可自定義的 package 路徑

import com.eatfast.permission.model.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 權限資料存取層 (Repository)。
 *
 * - @Repository: (不可變動) 標記此介面為 Spring 管理的資料存取層 Bean。
 * - extends JpaRepository: (不可變動) 繼承後，自動獲得標準 CRUD 功能。
 * - PermissionEntity: (不可變動) 此 Repository 對應的實體類別。
 * - Long: (不可變動) 實體主鍵的型別。
 */
@Repository
public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {

    // JpaRepository 已提供如 findAll(), findById(), save() 等常用方法。
    // 如有特殊需求，可在此定義自訂查詢方法。
    // 範例：未來若需依據權限描述查詢，可新增
    // Optional<PermissionEntity> findByDescription(String description);
}
