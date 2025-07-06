// =======================================================================================
// 檔案: EmployeeController.java (已修正)
// 說明: 新增了 /validate-field 端點，用於處理前端的即時唯一性驗證請求。
// =======================================================================================
package com.eatfast.employee.controller;

import com.eatfast.common.enums.AccountStatus;
import com.eatfast.common.enums.EmployeeRole;
import com.eatfast.employee.dto.CreateEmployeeRequest;
import com.eatfast.employee.dto.EmployeeDTO;
import com.eatfast.employee.dto.UpdateEmployeeRequest;
import com.eatfast.employee.service.EmployeeService;
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

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // (原有的 create, get, search, update, delete, grant/revoke permission 等 API 維持不變)
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createEmployee(@Valid @ModelAttribute CreateEmployeeRequest request) {
        try {
            EmployeeDTO createdEmployee = employeeService.createEmployee(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable Long id, HttpSession session) {
        // 權限檢查：獲取當前登入員工資訊
        EmployeeDTO currentEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
        if (currentEmployee == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        EmployeeDTO employeeDto = employeeService.findEmployeeById(id);
        
        // 根據員工角色進行權限控制
        EmployeeRole currentRole = currentEmployee.getRole();
        switch (currentRole) {
            case HEADQUARTERS_ADMIN:
                // 總部管理員：可以查看所有員工
                break;
                
            case MANAGER:
                // 門市經理：只能查看自己門市的員工
                if (!employeeDto.getStoreId().equals(currentEmployee.getStoreId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
                break;
                
            case STAFF:
                // 一般員工：無權限查看員工資料
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                
            default:
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(employeeDto);
    }
    
    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> searchEmployees(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) EmployeeRole role,
            @RequestParam(required = false) AccountStatus status,
            @RequestParam(required = false) Long storeId,
            HttpSession session
    ) {
        // 權限檢查：獲取當前登入員工資訊
        EmployeeDTO currentEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
        if (currentEmployee == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 根據員工角色進行權限控制
        EmployeeRole currentRole = currentEmployee.getRole();
        Map<String, Object> searchParams = new HashMap<>();
        
        switch (currentRole) {
            case HEADQUARTERS_ADMIN:
                // 總部管理員：可以查看所有員工
                if (StringUtils.hasText(username)) searchParams.put("username", username);
                if (role != null) searchParams.put("role", role);
                if (status != null) searchParams.put("status", status);
                if (storeId != null) searchParams.put("storeId", storeId);
                break;
                
            case MANAGER:
                // 門市經理：只能查看自己門市的員工
                if (StringUtils.hasText(username)) searchParams.put("username", username);
                if (role != null) searchParams.put("role", role);
                if (status != null) searchParams.put("status", status);
                // 強制限制為當前員工的門市
                searchParams.put("storeId", currentEmployee.getStoreId());
                break;
                
            case STAFF:
                // 一般員工：無權限查看員工資料
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                
            default:
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<EmployeeDTO> employees = employeeService.searchEmployees(searchParams);
        return ResponseEntity.ok(employees);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @PathVariable Long id,
            @ModelAttribute @Valid UpdateEmployeeRequest request,
            HttpSession session) {
        // 權限檢查：獲取當前登入員工資訊
        EmployeeDTO currentEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
        if (currentEmployee == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 先查詢要更新的員工資料
        EmployeeDTO targetEmployee = employeeService.findEmployeeById(id);
        
        // 根據員工角色進行權限控制
        EmployeeRole currentRole = currentEmployee.getRole();
        switch (currentRole) {
            case HEADQUARTERS_ADMIN:
                // 總部管理員：可以修改所有員工
                break;
                
            case MANAGER:
                // 門市經理：只能修改自己門市的員工
                if (!targetEmployee.getStoreId().equals(currentEmployee.getStoreId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
                break;
                
            case STAFF:
                // 一般員工：無權限修改員工資料
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                
            default:
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        EmployeeDTO updatedEmployee = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(updatedEmployee);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id, HttpSession session) {
        // 權限檢查：獲取當前登入員工資訊
        EmployeeDTO currentEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
        if (currentEmployee == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 先查詢要刪除的員工資料
        EmployeeDTO targetEmployee = employeeService.findEmployeeById(id);
        
        // 根據員工角色進行權限控制
        EmployeeRole currentRole = currentEmployee.getRole();
        switch (currentRole) {
            case HEADQUARTERS_ADMIN:
                // 總部管理員：可以刪除所有員工
                break;
                
            case MANAGER:
                // 門市經理：只能刪除自己門市的員工
                if (!targetEmployee.getStoreId().equals(currentEmployee.getStoreId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
                break;
                
            case STAFF:
                // 一般員工：無權限刪除員工資料
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                
            default:
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{employeeId}/permissions/{permissionId}")
    public ResponseEntity<Void> grantPermission(@PathVariable Long employeeId, @PathVariable Long permissionId) {
        employeeService.grantPermissionToEmployee(employeeId, permissionId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{employeeId}/permissions/{permissionId}")
    public ResponseEntity<Void> revokePermission(@PathVariable Long employeeId, @PathVariable Long permissionId) {
        employeeService.revokePermissionFromEmployee(employeeId, permissionId);
        return ResponseEntity.noContent().build();
    }


    /**
     * 【整合驗證端點】: 將驗證功能整合到主控制器
     * 這個端點同時支援即時驗證和完整欄位驗證
     */
    @PostMapping("/validate-field")
    public ResponseEntity<Map<String, Object>> validateField(@RequestBody ValidateFieldRequest request) {
        // 1. 基本參數驗證
        if (!StringUtils.hasText(request.getField()) || !StringUtils.hasText(request.getValue())) {
            Map<String, Object> response = new HashMap<>();
            response.put("isAvailable", false);
            response.put("message", "欄位名稱和值不可為空");
            return ResponseEntity.badRequest().body(response);
        }

        // 2. 呼叫 Service 層進行業務邏輯判斷
        boolean isAvailable = employeeService.isFieldAvailable(request.getField(), request.getValue());
        
        // 3. 準備要回傳給前端的 JSON 資料
        Map<String, Object> response = new HashMap<>();
        response.put("isAvailable", isAvailable);
        
        // 4. 如果欄位不可用，附上詳細錯誤訊息
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

    /**
     * 【新增批量驗證端點】: 支援一次驗證多個欄位
     */
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

    /**
     * [可自定義的類別名稱]: ValidateFieldRequest
     * 一個靜態內部類別，專門用來接收 /validate-field 端點的請求資料。
     * 這麼做可以避免污染主要的 DTOs，讓職責更單純。
     */
    static class ValidateFieldRequest {
        private String field;
        private String value;

        // Getters and Setters
        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    @PutMapping("/{id}/photo")
    public ResponseEntity<?> updateEmployeePhoto(
            @PathVariable Long id,
            @RequestParam("photo") MultipartFile photo,
            HttpSession session) {
        try {
            // 權限檢查：獲取當前登入員工資訊
            EmployeeDTO currentEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
            if (currentEmployee == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // 先查詢要更新的員工資料
            EmployeeDTO targetEmployee = employeeService.findEmployeeById(id);
            
            // 根據員工角色進行權限控制
            EmployeeRole currentRole = currentEmployee.getRole();
            switch (currentRole) {
                case HEADQUARTERS_ADMIN:
                    // 總部管理員：可以修改所有員工照片
                    break;
                    
                case MANAGER:
                    // 門市經理：只能修改自己門市的員工照片
                    if (!targetEmployee.getStoreId().equals(currentEmployee.getStoreId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                    break;
                    
                case STAFF:
                    // 一般員工：無權限修改員工照片
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

            // 更新員工照片
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