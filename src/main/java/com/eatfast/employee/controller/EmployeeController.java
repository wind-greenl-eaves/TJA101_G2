// =======================================================================================
// 檔案: EmployeeController.java
// 說明: 這是「員工管理」的主控制器，負責處理所有與員工有關的 API 請求。
//      你可以把它想像成「員工櫃檯」，前端有任何員工資料的需求，都會先來這裡報到。
//      這個類別會呼叫 EmployeeService（服務層）來處理實際的商業邏輯。
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

    // 這個屬性是服務層（Service），負責處理員工的商業邏輯。
    // 你可以把 Service 想像成「資料處理部門」，Controller 只負責接收與回應，Service 負責做事。
    private final EmployeeService employeeService;

    // 這是建構子注入（Constructor Injection），Spring Boot 會自動幫我們把 EmployeeService 傳進來。
    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // ========================
    // 1. 新增員工（Create）
    // ========================
    // 這個方法負責處理「新增員工」的請求。
    // 前端會送出一個表單（multipart/form-data），包含員工資料。
    // 對應 API 路徑：POST /api/v1/employees
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createEmployee(@Valid @ModelAttribute CreateEmployeeRequest request) {
        try {
            // 呼叫服務層，新增員工
            EmployeeDTO createdEmployee = employeeService.createEmployee(request);
            // 回傳 201 Created 狀態與新員工資料
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
        } catch (Exception e) {
            // 若有錯誤，回傳錯誤訊息
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
        // 取得目前登入的員工（從 session 取出）
        EmployeeDTO currentEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
        if (currentEmployee == null) {
            // 沒有登入就拒絕存取
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 查詢目標員工資料
        EmployeeDTO employeeDto = employeeService.findEmployeeById(id);
        
        // 根據角色決定是否有權限
        EmployeeRole currentRole = currentEmployee.getRole();
        switch (currentRole) {
            case HEADQUARTERS_ADMIN:
                // 總部管理員：可以看全部
                break;
            case MANAGER:
                // 經理只能看自己門市
                if (!employeeDto.getStoreId().equals(currentEmployee.getStoreId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
                break;
            case STAFF:
                // 一般員工不能看
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            default:
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(employeeDto);
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
        // 取得目前登入的員工
        EmployeeDTO currentEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
        if (currentEmployee == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // 根據角色決定查詢範圍
        EmployeeRole currentRole = currentEmployee.getRole();
        Map<String, Object> searchParams = new HashMap<>();
        switch (currentRole) {
            case HEADQUARTERS_ADMIN:
                // 總部管理員：可查全部
                if (StringUtils.hasText(username)) searchParams.put("username", username);
                if (role != null) searchParams.put("role", role);
                if (status != null) searchParams.put("status", status);
                if (storeId != null) searchParams.put("storeId", storeId);
                break;
            case MANAGER:
                // 經理只能查自己門市
                if (StringUtils.hasText(username)) searchParams.put("username", username);
                if (role != null) searchParams.put("role", role);
                if (status != null) searchParams.put("status", status);
                searchParams.put("storeId", currentEmployee.getStoreId());
                break;
            case STAFF:
                // 一般員工不能查
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            default:
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        // 呼叫服務層查詢
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
        // 呼叫服務層更新
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
        // 呼叫服務層刪除
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