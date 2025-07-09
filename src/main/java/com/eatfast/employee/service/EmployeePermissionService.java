// ================================================================
// 檔案名稱: EmployeePermissionService.java
// 功能說明: 員工權限控制統一服務
// 架構層級: 服務層 (Service Layer)
// 設計模式: Strategy Pattern + Factory Pattern
// ================================================================
package com.eatfast.employee.service;

import com.eatfast.common.enums.EmployeeRole;
import com.eatfast.employee.dto.EmployeeDTO;
import org.springframework.stereotype.Service;

/**
 * 員工權限控制統一服務
 * 統一管理所有員工相關的權限檢查邏輯
 */
@Service
public class EmployeePermissionService {

    /**
     * 檢查員工是否有查看其他員工的權限
     */
    public boolean canViewEmployee(EmployeeDTO currentEmployee, EmployeeDTO targetEmployee) {
        if (currentEmployee == null) {
            return false;
        }

        return switch (currentEmployee.getRole()) {
            case HEADQUARTERS_ADMIN -> true; // 總部管理員可查看所有員工
            case MANAGER -> currentEmployee.getStoreId().equals(targetEmployee.getStoreId()); // 門市經理只能查看同門市員工
            case STAFF -> false; // 一般員工無查看權限
        };
    }

    /**
     * 檢查員工是否有新增員工的權限
     */
    public boolean canCreateEmployee(EmployeeDTO currentEmployee) {
        if (currentEmployee == null) {
            return false;
        }

        return switch (currentEmployee.getRole()) {
            case HEADQUARTERS_ADMIN, MANAGER -> true; // 總部管理員和門市經理可新增員工
            case STAFF -> false; // 一般員工無新增權限
        };
    }

    /**
     * 檢查員工是否有修改指定員工的權限
     */
    public boolean canEditEmployee(EmployeeDTO currentEmployee, EmployeeDTO targetEmployee) {
        if (currentEmployee == null || targetEmployee == null) {
            return false;
        }

        return switch (currentEmployee.getRole()) {
            case HEADQUARTERS_ADMIN -> true; // 總部管理員可修改所有員工
            case MANAGER -> currentEmployee.getStoreId().equals(targetEmployee.getStoreId()); // 門市經理只能修改同門市員工
            case STAFF -> false; // 一般員工無修改權限
        };
    }

    /**
     * 檢查員工是否有刪除指定員工的權限
     */
    public boolean canDeleteEmployee(EmployeeDTO currentEmployee, EmployeeDTO targetEmployee) {
        if (currentEmployee == null || targetEmployee == null) {
            return false;
        }

        return switch (currentEmployee.getRole()) {
            case HEADQUARTERS_ADMIN -> true; // 總部管理員可刪除所有員工
            case MANAGER -> {
                // 門市經理只能刪除同門市的一般員工，不能刪除其他經理
                yield currentEmployee.getStoreId().equals(targetEmployee.getStoreId()) 
                      && targetEmployee.getRole() == EmployeeRole.STAFF;
            }
            case STAFF -> false; // 一般員工無刪除權限
        };
    }

    /**
     * 檢查員工是否有訪問員工列表的權限
     */
    public boolean canAccessEmployeeList(EmployeeDTO currentEmployee) {
        if (currentEmployee == null) {
            return false;
        }

        return switch (currentEmployee.getRole()) {
            case HEADQUARTERS_ADMIN, MANAGER -> true; // 總部管理員和門市經理可訪問列表
            case STAFF -> false; // 一般員工無訪問權限
        };
    }

    /**
     * 檢查員工是否有分配門市的權限
     */
    public boolean canAssignStore(EmployeeDTO currentEmployee) {
        if (currentEmployee == null) {
            return false;
        }

        return currentEmployee.getRole() == EmployeeRole.HEADQUARTERS_ADMIN; // 只有總部管理員可分配門市
    }

    /**
     * 獲取員工可管理的門市ID範圍
     */
    public Long[] getManageableStoreIds(EmployeeDTO currentEmployee) {
        if (currentEmployee == null) {
            return new Long[0];
        }

        return switch (currentEmployee.getRole()) {
            case HEADQUARTERS_ADMIN -> null; // null 表示可管理所有門市
            case MANAGER -> new Long[]{currentEmployee.getStoreId()}; // 只能管理自己的門市
            case STAFF -> new Long[0]; // 無管理權限
        };
    }

    /**
     * 檢查員工是否有權限設定指定角色
     */
    public boolean canAssignRole(EmployeeDTO currentEmployee, EmployeeRole targetRole) {
        if (currentEmployee == null || targetRole == null) {
            return false;
        }

        return switch (currentEmployee.getRole()) {
            case HEADQUARTERS_ADMIN -> true; // 總部管理員可設定任何角色
            case MANAGER -> targetRole == EmployeeRole.STAFF; // 門市經理只能設定一般員工角色
            case STAFF -> false; // 一般員工無權限設定角色
        };
    }
}