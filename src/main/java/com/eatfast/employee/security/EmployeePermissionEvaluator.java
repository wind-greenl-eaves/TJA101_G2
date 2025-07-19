package com.eatfast.employee.security;

import com.eatfast.employee.dto.EmployeeDTO;
import com.eatfast.employee.service.EmployeePermissionService;
import com.eatfast.employee.service.EmployeeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.Serializable;

/**
 * 自定義權限評估器，用於 @PreAuthorize 註解中的複雜權限邏輯
 * 
 * 支援的權限檢查方法：
 * - hasPermission(authentication, 'CREATE_EMPLOYEE', 'EMPLOYEE')
 * - hasPermission(authentication, targetEmployeeId, 'VIEW_EMPLOYEE')
 * - hasPermission(authentication, targetEmployeeId, 'EDIT_EMPLOYEE')
 * - hasPermission(authentication, targetEmployeeId, 'DELETE_EMPLOYEE')
 */
@Component
public class EmployeePermissionEvaluator implements PermissionEvaluator {

    @Autowired
    private EmployeePermissionService permissionService;
    
    @Autowired
    private EmployeeService employeeService;

    /**
     * 評估對特定物件的權限
     * @param authentication 認證物件（在我們的情況下可能為 null，因為使用 Session）
     * @param targetDomainObject 目標物件（通常是員工ID）
     * @param permission 權限名稱
     * @return 是否有權限
     */
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (permission == null) {
            return false;
        }
        
        // 從 Session 中獲取當前登入的員工
        EmployeeDTO currentEmployee = getCurrentEmployeeFromSession();
        if (currentEmployee == null) {
            return false;
        }
        
        String permissionStr = permission.toString();
        
        try {
            switch (permissionStr) {
                case "CREATE_EMPLOYEE":
                    return permissionService.canCreateEmployee(currentEmployee);
                
                case "VIEW_EMPLOYEE":
                    if (targetDomainObject instanceof Long) {
                        EmployeeDTO targetEmployee = employeeService.findEmployeeById((Long) targetDomainObject);
                        return permissionService.canViewEmployee(currentEmployee, targetEmployee);
                    }
                    return false;
                
                case "EDIT_EMPLOYEE":
                    if (targetDomainObject instanceof Long) {
                        EmployeeDTO targetEmployee = employeeService.findEmployeeById((Long) targetDomainObject);
                        return permissionService.canEditEmployee(currentEmployee, targetEmployee);
                    }
                    return false;
                
                case "DELETE_EMPLOYEE":
                    if (targetDomainObject instanceof Long) {
                        EmployeeDTO targetEmployee = employeeService.findEmployeeById((Long) targetDomainObject);
                        return permissionService.canDeleteEmployee(currentEmployee, targetEmployee);
                    }
                    return false;
                
                case "ACCESS_EMPLOYEE_LIST":
                    return permissionService.canAccessEmployeeList(currentEmployee);
                
                default:
                    return false;
            }
        } catch (Exception e) {
            // 發生異常時拒絕存取
            return false;
        }
    }

    /**
     * 評估對特定類型的權限
     * @param authentication 認證物件
     * @param targetType 目標類型
     * @param permission 權限名稱
     * @return 是否有權限
     */
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if ("EMPLOYEE".equals(targetType)) {
            return hasPermission(authentication, targetId, permission);
        }
        return false;
    }

    /**
     * 從 Session 中獲取當前登入的員工
     * @return 當前登入的員工DTO，如果未登入則返回 null
     */
    private EmployeeDTO getCurrentEmployeeFromSession() {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            if (attr != null) {
                HttpSession session = attr.getRequest().getSession(false);
                if (session != null) {
                    return (EmployeeDTO) session.getAttribute("loggedInEmployee");
                }
            }
        } catch (Exception e) {
            // 無法獲取 Session，返回 null
        }
        return null;
    }
}