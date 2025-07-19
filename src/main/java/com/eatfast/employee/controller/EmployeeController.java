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
import com.eatfast.employee.dto.EmployeeApplicationDTO;
import com.eatfast.employee.dto.UpdateEmployeeRequest;
import com.eatfast.employee.service.EmployeeService;
import com.eatfast.employee.service.EmployeePermissionService;
import com.eatfast.employee.service.EmployeeApplicationService;
import com.eatfast.employee.util.EmployeeLogger;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);
    
    private final EmployeeService employeeService;
    private final EmployeePermissionService permissionService;
    private final EmployeeLogger employeeLogger;
    private final EmployeeApplicationService employeeApplicationService;

    @Autowired
    public EmployeeController(EmployeeService employeeService, 
                             EmployeePermissionService permissionService, 
                             EmployeeLogger employeeLogger,
                             EmployeeApplicationService employeeApplicationService) {
        this.employeeService = employeeService;
        this.permissionService = permissionService;
        this.employeeLogger = employeeLogger;
        this.employeeApplicationService = employeeApplicationService;
    }

    // ========================
    // 1. 新增員工（Create）- 修改為支援申請流程
    // ========================
    // 這個方法負責處理「新增員工」的請求。
    // 前端會送出一個表單（multipart/form-data），包含員工資料。
    // 對應 API 路徑：POST /api/v1/employees
    //@ModleAttribute 用來處理 multipart/form-data 的表單資料。
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createEmployee(@Valid @ModelAttribute CreateEmployeeRequest request, HttpSession session) {
        employeeLogger.logInfo("收到新增員工請求: username={}, email={}, role={}", 
                   request.getUsername(), request.getEmail(), request.getRole());
        
        try {
            EmployeeDTO currentEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
            if (currentEmployee == null) {
                employeeLogger.logWarn("未登入用戶嘗試新增員工");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "請重新登入"));
            }

            employeeLogger.logDebug("當前登入用戶: ID={}, username={}, role={}", 
                        currentEmployee.getEmployeeId(), currentEmployee.getUsername(), currentEmployee.getRole());

            // 手動檢查權限
            if (!permissionService.canCreateEmployee(currentEmployee)) {
                employeeLogger.logWarn("用戶 {} (ID: {}) 嘗試新增員工但權限不足", 
                           currentEmployee.getUsername(), currentEmployee.getEmployeeId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "權限不足：您無法新增員工"));
            }

            // 【核心修改】根據角色決定是直接創建還是提交申請
            if (currentEmployee.getRole() == EmployeeRole.MANAGER) {
                // 門市經理：提交申請單
                request.setStoreId(currentEmployee.getStoreId());
                employeeLogger.logInfo("門市經理提交新增員工申請: username={}", request.getUsername());
                
                EmployeeApplicationDTO application = employeeApplicationService.submitApplication(
                    request, currentEmployee.getEmployeeId());
                
                employeeLogger.logInfo("【成功】提交員工申請: applicationId={}, username={}", 
                           application.getApplicationId(), request.getUsername());
                
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(Map.of(
                            "message", "員工申請已提交，等待總部管理員審核",
                            "applicationId", application.getApplicationId(),
                            "type", "application"
                        ));
                
            } else if (currentEmployee.getRole() == EmployeeRole.HEADQUARTERS_ADMIN) {
                // 總部管理員：直接創建員工
                employeeLogger.logInfo("總部管理員直接創建新員工: username={}", request.getUsername());
                EmployeeDTO createdEmployee = employeeService.createEmployee(request);
                
                employeeLogger.logInfo("【成功】創建員工: ID={}, username={}, role={}, storeId={}", 
                           createdEmployee.getEmployeeId(), createdEmployee.getUsername(), 
                           createdEmployee.getRole(), createdEmployee.getStoreId());
                
                return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "權限不足：只有門市經理和總部管理員可以新增員工"));
            }
            
        } catch (Exception e) {
            employeeLogger.logError("【失敗】處理員工請求失敗: username={}, error={}", 
                    request.getUsername(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
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
        logger.info("收到查詢員工請求: employeeId={}", id);
        
        EmployeeDTO currentEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
        if (currentEmployee == null) {
            logger.warn("未登入用戶嘗試查詢員工: employeeId={}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        logger.debug("當前登入用戶: ID={}, username={}, role={}", 
                    currentEmployee.getEmployeeId(), currentEmployee.getUsername(), currentEmployee.getRole());

        try {
            EmployeeDTO targetEmployee = employeeService.findEmployeeById(id);
            logger.debug("找到目標員工: ID={}, username={}, storeId={}", 
                        targetEmployee.getEmployeeId(), targetEmployee.getUsername(), targetEmployee.getStoreId());
            
            // 手動檢查權限
            if (!permissionService.canViewEmployee(currentEmployee, targetEmployee)) {
                logger.warn("用戶 {} (ID: {}) 嘗試查看員工 {} (ID: {}) 但權限不足", 
                           currentEmployee.getUsername(), currentEmployee.getEmployeeId(),
                           targetEmployee.getUsername(), targetEmployee.getEmployeeId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            logger.info("成功查詢員工: ID={}, username={}", targetEmployee.getEmployeeId(), targetEmployee.getUsername());
            return ResponseEntity.ok(targetEmployee);
        } catch (Exception e) {
            logger.error("查詢員工失敗: employeeId={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
            @RequestParam(required = false) String gender,
            HttpSession session
    ) {
        logger.info("收到搜尋員工請求: username={}, role={}, status={}, storeId={}, gender={}", 
                   username, role, status, storeId, gender);
        
        EmployeeDTO currentEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
        if (currentEmployee == null) {
            logger.warn("未登入用戶嘗試搜尋員工");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        logger.debug("當前登入用戶: ID={}, username={}, role={}", 
                    currentEmployee.getEmployeeId(), currentEmployee.getUsername(), currentEmployee.getRole());

        // 手動檢查權限
        if (!permissionService.canAccessEmployeeList(currentEmployee)) {
            logger.warn("用戶 {} (ID: {}) 嘗試存取員工清單但權限不足", 
                       currentEmployee.getUsername(), currentEmployee.getEmployeeId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Map<String, Object> searchParams = new HashMap<>();
            if (StringUtils.hasText(username)) searchParams.put("username", username);
            if (role != null) searchParams.put("role", role);
            if (status != null) searchParams.put("status", status);
            if (StringUtils.hasText(gender)) searchParams.put("gender", gender);
            
            // 【修正】根據權限限制查詢範圍
            Long[] manageableStoreIds = permissionService.getManageableStoreIds(currentEmployee);
            if (manageableStoreIds != null && manageableStoreIds.length > 0) {
                // 門市經理只能查詢自己門市的員工
                searchParams.put("storeId", manageableStoreIds[0]);
                logger.debug("門市經理查詢限制: storeId={}", manageableStoreIds[0]);
            } else if (storeId != null && currentEmployee.getRole() == EmployeeRole.HEADQUARTERS_ADMIN) {
                // 總部管理員可以指定門市查詢
                searchParams.put("storeId", storeId);
                logger.debug("總部管理員指定門市查詢: storeId={}", storeId);
            }

            logger.debug("搜尋參數: {}", searchParams);
            List<EmployeeDTO> employees = employeeService.searchEmployees(searchParams);
            
            logger.info("成功搜尋員工: 找到 {} 位員工", employees.size());
            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            logger.error("搜尋員工失敗: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
        
        logger.info("收到修改員工請求: employeeId={}, updateFields={}", id, request.toString());
        
        EmployeeDTO currentEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
        if (currentEmployee == null) {
            logger.warn("未登入用戶嘗試修改員工: employeeId={}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        logger.debug("當前登入用戶: ID={}, username={}, role={}", 
                    currentEmployee.getEmployeeId(), currentEmployee.getUsername(), currentEmployee.getRole());
        
        try {
            EmployeeDTO targetEmployee = employeeService.findEmployeeById(id);
            logger.debug("找到目標員工: ID={}, username={}, storeId={}", 
                        targetEmployee.getEmployeeId(), targetEmployee.getUsername(), targetEmployee.getStoreId());
            
            // 手動檢查權限
            if (!permissionService.canEditEmployee(currentEmployee, targetEmployee)) {
                logger.warn("用戶 {} (ID: {}) 嘗試修改員工 {} (ID: {}) 但權限不足", 
                           currentEmployee.getUsername(), currentEmployee.getEmployeeId(),
                           targetEmployee.getUsername(), targetEmployee.getEmployeeId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            logger.debug("開始更新員工資料: employeeId={}", id);
            EmployeeDTO updatedEmployee = employeeService.updateEmployee(id, request);
            
            logger.info("成功更新員工: ID={}, username={}", updatedEmployee.getEmployeeId(), updatedEmployee.getUsername());
            return ResponseEntity.ok(updatedEmployee);
        } catch (Exception e) {
            logger.error("更新員工失敗: employeeId={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // ========================
    // 5. 刪除員工（Delete）
    // ========================
    // 這個方法用來刪除員工。
    // 對應 API 路徑：DELETE /api/v1/employees/{id}
    // 權限說明同上。
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id, HttpSession session) {
        logger.info("收到刪除員工請求: employeeId={}", id);
        
        EmployeeDTO currentEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
        if (currentEmployee == null) {
            logger.warn("未登入用戶嘗試刪除員工: employeeId={}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        logger.debug("當前登入用戶: ID={}, username={}, role={}", 
                    currentEmployee.getEmployeeId(), currentEmployee.getUsername(), currentEmployee.getRole());
        
        try {
            EmployeeDTO targetEmployee = employeeService.findEmployeeById(id);
            logger.debug("找到目標員工: ID={}, username={}, storeId={}", 
                        targetEmployee.getEmployeeId(), targetEmployee.getUsername(), targetEmployee.getStoreId());
            
            // 手動檢查權限
            if (!permissionService.canDeleteEmployee(currentEmployee, targetEmployee)) {
                logger.warn("用戶 {} (ID: {}) 嘗試刪除員工 {} (ID: {}) 但權限不足", 
                           currentEmployee.getUsername(), currentEmployee.getEmployeeId(),
                           targetEmployee.getUsername(), targetEmployee.getEmployeeId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            logger.debug("開始刪除員工: employeeId={}", id);
            employeeService.deleteEmployee(id);
            
            logger.info("成功刪除員工: ID={}, username={}", targetEmployee.getEmployeeId(), targetEmployee.getUsername());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("刪除員工失敗: employeeId={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // ========================
    // 6. 權限管理（授權/收回）
    // ========================
    // 這兩個方法分別用來「授權」和「收回」員工的特殊權限。
    // 對應 API 路徑：
    //   - POST /api/v1/employees/{employeeId}/permissions/{permissionId}（授權）
    //   - DELETE /api/v1/employees/{employeeId}/permissions/{permissionId}（收回）
    @PostMapping("/{employeeId}/permissions/{permissionId}")
    public ResponseEntity<Void> grantPermission(@PathVariable Long employeeId, @PathVariable Long permissionId, HttpSession session) {
        logger.info("收到授權請求: employeeId={}, permissionId={}", employeeId, permissionId);
        
        EmployeeDTO currentEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
        if (currentEmployee == null) {
            logger.warn("未登入用戶嘗試授權員工: employeeId={}", employeeId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            EmployeeDTO targetEmployee = employeeService.findEmployeeById(employeeId);
            
            // 手動檢查權限
            if (!permissionService.canEditEmployee(currentEmployee, targetEmployee)) {
                logger.warn("用戶 {} (ID: {}) 嘗試授權員工 {} (ID: {}) 但權限不足", 
                           currentEmployee.getUsername(), currentEmployee.getEmployeeId(),
                           targetEmployee.getUsername(), targetEmployee.getEmployeeId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // 直接呼叫服務層進行授權
            employeeService.grantPermissionToEmployee(employeeId, permissionId);
            logger.info("成功授權: employeeId={}, permissionId={}", employeeId, permissionId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("授權失敗: employeeId={}, permissionId={}, error={}", employeeId, permissionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{employeeId}/permissions/{permissionId}")
    public ResponseEntity<Void> revokePermission(@PathVariable Long employeeId, @PathVariable Long permissionId, HttpSession session) {
        logger.info("收到收回權限請求: employeeId={}, permissionId={}", employeeId, permissionId);
        
        EmployeeDTO currentEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
        if (currentEmployee == null) {
            logger.warn("未登入用戶嘗試收回員工權限: employeeId={}", employeeId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            EmployeeDTO targetEmployee = employeeService.findEmployeeById(employeeId);
            
            // 手動檢查權限
            if (!permissionService.canEditEmployee(currentEmployee, targetEmployee)) {
                logger.warn("用戶 {} (ID: {}) 嘗試收回員工 {} (ID: {}) 權限但權限不足", 
                           currentEmployee.getUsername(), currentEmployee.getEmployeeId(),
                           targetEmployee.getUsername(), targetEmployee.getEmployeeId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // 直接呼叫服務層收回權限
            employeeService.revokePermissionFromEmployee(employeeId, permissionId);
            logger.info("成功收回權限: employeeId={}, permissionId={}", employeeId, permissionId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("收回權限失敗: employeeId={}, permissionId={}, error={}", employeeId, permissionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========================
    // 7. 欄位即時驗證（AJAX）
    // ========================
    // 這個方法讓前端可以即時檢查「帳號、Email、身分證」等欄位是否已被使用。
    // 對應 API 路徑：POST /api/v1/employees/validate-field
    // 前端會傳入欄位名稱與值，這裡會呼叫服務層檢查資料庫。
    @PostMapping("/validate-field")
    public ResponseEntity<Map<String, Object>> validateField(@RequestBody ValidateFieldRequest request) {
        logger.debug("收到欄位驗證請求: field={}, value={}", request.getField(), request.getValue());
        
        // 檢查參數是否有填寫
        if (!StringUtils.hasText(request.getField()) || !StringUtils.hasText(request.getValue())) {
            logger.warn("欄位驗證請求參數不完整: field={}, value={}", request.getField(), request.getValue());
            Map<String, Object> response = new HashMap<>();
            response.put("isAvailable", false);
            response.put("message", "欄位名稱和值不可為空");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // 呼叫服務層檢查唯一性
            boolean isAvailable = employeeService.isFieldAvailable(request.getField(), request.getValue());
            logger.debug("欄位驗證結果: field={}, value={}, isAvailable={}", 
                        request.getField(), request.getValue(), isAvailable);
            
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
        } catch (Exception e) {
            logger.error("欄位驗證失敗: field={}, value={}, error={}", request.getField(), request.getValue(), e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("isAvailable", false);
            response.put("message", "驗證過程中發生錯誤");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========================
    // 8. 批次欄位驗證
    // ========================
    // 這個方法支援一次檢查多個欄位（例如註冊時同時檢查帳號、Email...）。
    // 對應 API 路徑：POST /api/v1/employees/validate-multiple
    @PostMapping("/validate-multiple")
    public ResponseEntity<Map<String, Object>> validateMultipleFields(
            @RequestBody Map<String, String> fieldValues) {
        logger.debug("收到批次欄位驗證請求: fields={}", fieldValues.keySet());
        
        try {
            Map<String, Object> response = new HashMap<>();
            Map<String, Boolean> fieldResults = new HashMap<>();
            Map<String, String> errorMessages = new HashMap<>();
            boolean allValid = true;
            
            for (Map.Entry<String, String> entry : fieldValues.entrySet()) {
                String field = entry.getKey();
                String value = entry.getValue();
                boolean isAvailable = employeeService.isFieldAvailable(field, value);
                fieldResults.put(field, isAvailable);
                
                logger.debug("批次驗證結果: field={}, value={}, isAvailable={}", field, value, isAvailable);
                
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
            
            logger.info("批次欄位驗證完成: allValid={}, validatedFields={}", allValid, fieldValues.keySet().size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("批次欄位驗證失敗: fields={}, error={}", fieldValues.keySet(), e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("allValid", false);
            response.put("message", "批次驗證過程中發生錯誤");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
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
        logger.info("收到上傳員工照片請求: employeeId={}, fileName={}, fileSize={} bytes", 
                   id, photo.getOriginalFilename(), photo.getSize());
        
        try {
            // 取得目前登入的員工
            EmployeeDTO currentEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
            if (currentEmployee == null) {
                logger.warn("未登入用戶嘗試上傳員工照片: employeeId={}", id);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            logger.debug("當前登入用戶: ID={}, username={}, role={}", 
                        currentEmployee.getEmployeeId(), currentEmployee.getUsername(), currentEmployee.getRole());
            
            // 查詢要更新的員工
            EmployeeDTO targetEmployee = employeeService.findEmployeeById(id);
            logger.debug("找到目標員工: ID={}, username={}, storeId={}", 
                        targetEmployee.getEmployeeId(), targetEmployee.getUsername(), targetEmployee.getStoreId());
            
            // 手動檢查權限
            if (!permissionService.canEditEmployee(currentEmployee, targetEmployee)) {
                logger.warn("用戶 {} (ID: {}) 嘗試上傳員工 {} (ID: {}) 照片但權限不足", 
                           currentEmployee.getUsername(), currentEmployee.getEmployeeId(),
                           targetEmployee.getUsername(), targetEmployee.getEmployeeId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // 驗證照片檔案
            if (photo.isEmpty()) {
                logger.warn("上傳的照片檔案為空: employeeId={}", id);
                Map<String, String> response = new HashMap<>();
                response.put("message", "請選擇要上傳的照片檔案");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 檢查檔案格式
            String contentType = photo.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                logger.warn("上傳的照片格式不支援: employeeId={}, contentType={}", id, contentType);
                Map<String, String> response = new HashMap<>();
                response.put("message", "只支援 JPG 或 PNG 格式的圖片");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 檢查檔案大小 (5MB)
            if (photo.getSize() > 5 * 1024 * 1024) {
                logger.warn("上傳的照片檔案過大: employeeId={}, fileSize={} bytes", id, photo.getSize());
                Map<String, String> response = new HashMap<>();
                response.put("message", "圖片大小不能超過 5MB");
                return ResponseEntity.badRequest().body(response);
            }
            
            logger.debug("開始處理照片上傳: employeeId={}, fileName={}", id, photo.getOriginalFilename());
            
            // 呼叫服務層更新照片
            EmployeeDTO updatedEmployee = employeeService.updateEmployeePhoto(id, photo);
            
            logger.info("成功上傳員工照片: employeeId={}, fileName={}, photoUrl={}", 
                       id, photo.getOriginalFilename(), updatedEmployee.getPhotoUrl());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "照片上傳成功");
            response.put("employee", updatedEmployee);
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            logger.error("照片上傳 IO 錯誤: employeeId={}, fileName={}, error={}", 
                        id, photo.getOriginalFilename(), e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "照片上傳失敗：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            logger.error("照片上傳失敗: employeeId={}, fileName={}, error={}", 
                        id, photo.getOriginalFilename(), e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}