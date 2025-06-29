package com.eatfast.permission.service; // 可自定義的 package 路徑

import com.eatfast.permission.dto.PermissionDto;
import java.util.List;

/**
 * [可自定義的介面名稱]: PermissionService
 * 權限服務層的公開介面。
 * 它定義了所有與權限相關的業務邏輯方法，供 Controller 層呼叫。
 */
public interface PermissionService {

    /**
     * 查詢系統中所有可用的權限。
     * 這個方法通常用於前端頁面，例如在為員工分配權限時，需要顯示一個所有權限的列表。
     *
     * @return 返回一個包含所有權限 DTO 的列表 (List<PermissionDto>)。
     */
    List<PermissionDto> findAllPermissions();

}
