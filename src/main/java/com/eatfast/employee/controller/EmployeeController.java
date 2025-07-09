// =======================================================================================
// 檔案: EmployeeController.java
// 功能說明: 員工管理核心控制器 - 處理所有與員工 CRUD 相關的 RESTful API 請求
// 架構層級: 控制器層 (Controller Layer)
// 配對關係:
//   - 服務層: EmployeeService - 處理業務邏輯
//   - DTO層: CreateEmployeeRequest, UpdateEmployeeRequest, EmployeeDTO - 資料傳輸
//   - 實體層: EmployeeEntity - 透過服務層間接操作
//   - 權限控制: 基於 Session 中的 loggedInEmployee 進行角色權限驗證
// 設計模式: 
//   - RESTful API Pattern
//   - Role-Based Access Control (RBAC)
//   - Request-Response Pattern
// API 路徑: /api/v1/employees
// 支援功能: 
//   - CRUD 操作 (新增、查詢、修改、刪除)
//   - 權限管理 (授權、收回)
//   - 欄位驗證 (即時驗證)
//   - 照片上傳
// =======================================================================================
package com.eatfast.employee.controller;

import com.eatfast.common.enums.AccountStatus;
import com.eatfast.common.enums.EmployeeRole;
import com.eatfast.employee.dto.CreateEmployeeRequest;
import com.eatfast.employee.dto.EmployeeDTO;
import com.eatfast.employee.dto.UpdateEmployeeRequest;
import com.eatfast.employee.service.EmployeeService;
import com.eatfast.employee.service.EmployeePermissionService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeePermissionService permissionService; // 新增權限服務

    @Autowired
    public EmployeeController(EmployeeService employeeService, EmployeePermissionService permissionService) {
        this.employeeService = employeeService;
        this.permissionService = permissionService;
    }

    // ========================
    // 1. 新增員工（Create）
    // ========================
    // 這個方法負責處理「新增員工」的請求。
    // 前端會送出一個表單（multipart/form-data），包含員工資料。
    // 對應 API 路徑：POST /api/v1/employees
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createEmployee(@Valid @ModelAttribute CreateEmployeeRequest request, HttpSession session) {
        try {
            EmployeeDTO currentEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
            if (currentEmployee == null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "請重新登入");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // 【修正】使用統一權限服務檢查
            if (!permissionService.canCreateEmployee(currentEmployee)) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "權限不足：您無法新增員工");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // 【修正】門市經理強制設定門市ID
            if (currentEmployee.getRole() == EmployeeRole.MANAGER) {
                request.setStoreId(currentEmployee.getStoreId());
            }

            EmployeeDTO createdEmployee = employeeService.createEmployee(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ========================
    // 2. 查詢單一員工（Read）
    // ========================
    // 這個方法用來查詢特定員工的詳細資料。
    // 對應 API 路徑：GET /api/v1/employees/{id}
    // 權限說明：
    //   - 總部管理員可查所有員工
    //   - 門市經理只能查自己門市的員工
    //   - 一般員工無法查詢
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable Long id, HttpSession session) {
        EmployeeDTO currentEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
        if (currentEmployee == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        EmployeeDTO targetEmployee = employeeService.findEmployeeById(id);
        
        // 【修正】使用統一權限服務檢查
        if (!permissionService.canViewEmployee(currentEmployee, targetEmployee)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(targetEmployee);
    }
    
    // ========================
    // 3. 查詢多位員工（搜尋）
    // ========================
    // 這個方法用來搜尋員工清單，可依姓名、角色、狀態、門市等條件查詢。
    // 對應 API 路徑：GET /api/v1/employees
    // 權限說明同上。
    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> searchEmployees(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) EmployeeRole role,
            @RequestParam(required = false) AccountStatus status,
            @RequestParam(required = false) Long storeId,
            HttpSession session
    ) {
        EmployeeDTO currentEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
        if (currentEmployee == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 【修正】使用統一權限服務檢查
        if (!permissionService.canAccessEmployeeList(currentEmployee)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Map<String, Object> searchParams = new HashMap<>();
        if (StringUtils.hasText(username)) searchParams.put("username", username);
        if (role != null) searchParams.put("role", role);
        if (status != null) searchParams.put("status", status);
        
        // 【修正】根據權限限制查詢範圍
        Long[] manageableStoreIds = permissionService.getManageableStoreIds(currentEmployee);
        if (manageableStoreIds != null && manageableStoreIds.length > 0) {
            // 門市經理只能查詢自己門市的員工
            searchParams.put("storeId", manageableStoreIds[0]);
        } else if (storeId != null && currentEmployee.getRole() == EmployeeRole.HEADQUARTERS_ADMIN) {
            // 總部管理員可以指定門市查詢
            searchParams.put("storeId", storeId);
        }

        List<EmployeeDTO> employees = employeeService.searchEmployees(searchParams);
        return ResponseEntity.ok(employees);
    }

    // ========================
    // 4. 修改員工資料（Update）
    // ========================
    // 這個方法用來更新員工的基本資料。
    // 對應 API 路徑：PUT /api/v1/employees/{id}
    // 權限說明同上。
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @PathVariable Long id,
            @ModelAttribute @Valid UpdateEmployeeRequest request,
            HttpSession session) {
        
        EmployeeDTO currentEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
        if (currentEmployee == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        EmployeeDTO targetEmployee = employeeService.findEmployeeById(id);
        
        // 【修正】使用統一權限服務檢查
        if (!permissionService.canEditEmployee(currentEmployee, targetEmployee)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        EmployeeDTO updatedEmployee = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(updatedEmployee);
    }
    
    // ========================
    // 5. 刪除員工（Delete）
    // ========================
    // 這個方法用來刪除員工。
    // 對應 API 路徑：DELETE /api/v1/employees/{id}
    // 權限說明同上。
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id, HttpSession session) {
        EmployeeDTO currentEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
        if (currentEmployee == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        EmployeeDTO targetEmployee = employeeService.findEmployeeById(id);
        
        // 【修正】使用統一權限服務檢查
        if (!permissionService.canDeleteEmployee(currentEmployee, targetEmployee)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
    
    // ========================
    // 6. 權限管理（授權/收回）
    // ========================
    // 這兩個方法分別用來「授權」和「收回」員工的特殊權限。
    // 對應 API 路徑：
    //   - POST /api/v1/employees/{employeeId}/permissions/{permissionId}（授權）
    //   - DELETE /api/v1/employees/{employeeId}/permissions/{permissionId}（收回）
    @PostMapping("/{employeeId}/permissions/{permissionId}")
    public ResponseEntity<Void> grantPermission(@PathVariable Long employeeId, @PathVariable Long permissionId) {
        // 直接呼叫服務層進行授權
        employeeService.grantPermissionToEmployee(employeeId, permissionId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{employeeId}/permissions/{permissionId}")
    public ResponseEntity<Void> revokePermission(@PathVariable Long employeeId, @PathVariable Long permissionId) {
        // 直接呼叫服務層收回權限
        employeeService.revokePermissionFromEmployee(employeeId, permissionId);
        return ResponseEntity.noContent().build();
    }

    // ========================
    // 7. 欄位即時驗證（AJAX）
    // ========================
    // 這個方法讓前端可以即時檢查「帳號、Email、身分證」等欄位是否已被使用。
    // 對應 API 路徑：POST /api/v1/employees/validate-field
    // 前端會傳入欄位名稱與值，這裡會呼叫服務層檢查資料庫。
    @PostMapping("/validate-field")
    public ResponseEntity<Map<String, Object>> validateField(@RequestBody ValidateFieldRequest request) {
        // 檢查參數是否有填寫
        if (!StringUtils.hasText(request.getField()) || !StringUtils.hasText(request.getValue())) {
            Map<String, Object> response = new HashMap<>();
            response.put("isAvailable", false);
            response.put("message", "欄位名稱和值不可為空");
            return ResponseEntity.badRequest().body(response);
        }
        // 呼叫服務層檢查唯一性
        boolean isAvailable = employeeService.isFieldAvailable(request.getField(), request.getValue());
        Map<String, Object> response = new HashMap<>();
        response.put("isAvailable", isAvailable);
        if (!isAvailable) {
            String fieldName = switch (request.getField()) {
                case "account" -> "登入帳號";
                case "email" -> "電子郵件";
                case "nationalId" -> "身分證字號";
                default -> "該欄位";
            };
            response.put("message", fieldName + "「" + request.getValue() + "」已被使用或格式不正確。");
        }
        return ResponseEntity.ok(response);
    }

    // ========================
    // 8. 批次欄位驗證
    // ========================
    // 這個方法支援一次檢查多個欄位（例如註冊時同時檢查帳號、Email...）。
    // 對應 API 路徑：POST /api/v1/employees/validate-multiple
    @PostMapping("/validate-multiple")
    public ResponseEntity<Map<String, Object>> validateMultipleFields(
            @RequestBody Map<String, String> fieldValues) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Boolean> fieldResults = new HashMap<>();
        Map<String, String> errorMessages = new HashMap<>();
        boolean allValid = true;
        for (Map.Entry<String, String> entry : fieldValues.entrySet()) {
            String field = entry.getKey();
            String value = entry.getValue();
            boolean isAvailable = employeeService.isFieldAvailable(field, value);
            fieldResults.put(field, isAvailable);
            if (!isAvailable) {
                allValid = false;
                String fieldName = switch (field) {
                    case "account" -> "登入帳號";
                    case "email" -> "電子郵件";
                    case "nationalId" -> "身分證字號";
                    default -> field;
                };
                errorMessages.put(field, fieldName + "「" + value + "」已被使用或格式不正確");
            }
        }
        response.put("allValid", allValid);
        response.put("fieldResults", fieldResults);
        response.put("errorMessages", errorMessages);
        return ResponseEntity.ok(response);
    }

    // ========================
    // 9. 靜態內部類別：ValidateFieldRequest
    // ========================
    // 這個類別專門用來接收欄位驗證的請求資料。
    // 你可以把它想像成「表單小包裹」，只裝欄位名稱與值。
    static class ValidateFieldRequest {
        private String field; // 欄位名稱（如 account、email...）
        private String value; // 欄位值
        // Getter/Setter
        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    // ========================
    // 10. 上傳員工照片
    // ========================
    // 這個方法讓你可以上傳/更新員工的照片。
    // 對應 API 路徑：PUT /api/v1/employees/{id}/photo
    // 會檢查檔案格式、大小與權限。
    @PutMapping("/{id}/photo")
    public ResponseEntity<?> updateEmployeePhoto(
            @PathVariable Long id,
            @RequestParam("photo") MultipartFile photo,
            HttpSession session) {
        try {
            // 取得目前登入的員工
            EmployeeDTO currentEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
            if (currentEmployee == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            // 查詢要更新的員工
            EmployeeDTO targetEmployee = employeeService.findEmployeeById(id);
            // 權限判斷
            EmployeeRole currentRole = currentEmployee.getRole();
            switch (currentRole) {
                case HEADQUARTERS_ADMIN:
                    break;
                case MANAGER:
                    if (!targetEmployee.getStoreId().equals(currentEmployee.getStoreId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                    break;
                case STAFF:
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                default:
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            // 驗證照片檔案
            if (photo.isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "請選擇要上傳的照片檔案");
                return ResponseEntity.badRequest().body(response);
            }
            // 檢查檔案格式
            String contentType = photo.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "只支援 JPG 或 PNG 格式的圖片");
                return ResponseEntity.badRequest().body(response);
            }
            // 檢查檔案大小 (5MB)
            if (photo.getSize() > 5 * 1024 * 1024) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "圖片大小不能超過 5MB");
                return ResponseEntity.badRequest().body(response);
            }
            // 呼叫服務層更新照片
            EmployeeDTO updatedEmployee = employeeService.updateEmployeePhoto(id, photo);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "照片上傳成功");
            response.put("employee", updatedEmployee);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "照片上傳失敗：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}