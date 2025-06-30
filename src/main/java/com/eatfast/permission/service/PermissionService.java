package com.eatfast.permission.service;

import com.eatfast.common.enums.EmployeeRole;
import com.eatfast.permission.dto.PermissionDto;
import java.util.List;
import java.util.Set;

/**
 * [可自定義的介面名稱]: PermissionService - 【已重構】
 * 權限服務層的公開介面。
 */
public interface PermissionService {

    /**
     * 查詢系統中所有可用的權限。
     * @return 返回一個包含所有權限 DTO 的列表。
     */
    List<PermissionDto> findAllPermissions();

    /**
     * 【新增方法】: 根據員工角色查詢其擁有的所有權限。
     * @param role (可自定義的參數名): 要查詢的員工角色。
     * @return 返回一個包含該角色所有權限 DTO 的 Set 集合。
     */
    Set<PermissionDto> findPermissionsByRole(EmployeeRole role);
}
