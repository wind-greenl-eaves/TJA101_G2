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
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable Long id) {
        EmployeeDTO employeeDto = employeeService.findEmployeeById(id);
        return ResponseEntity.ok(employeeDto);
    }
    
    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> searchEmployees(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) EmployeeRole role,
            @RequestParam(required = false) AccountStatus status,
            @RequestParam(required = false) Long storeId
    ) {
        Map<String, Object> searchParams = new HashMap<>();
        if (StringUtils.hasText(username)) searchParams.put("username", username);
        if (role != null) searchParams.put("role", role);
        if (status != null) searchParams.put("status", status);
        if (storeId != null) searchParams.put("storeId", storeId);
        List<EmployeeDTO> employees = employeeService.searchEmployees(searchParams);
        return ResponseEntity.ok(employees);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @PathVariable Long id,
            @ModelAttribute @Valid UpdateEmployeeRequest request) {
        EmployeeDTO updatedEmployee = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(updatedEmployee);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
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
     * 【新增 API 端點】: 即時欄位驗證
     * 這個端點專門用來回應前端 add.js 發起的即時驗證請求。
     * * - @PostMapping("/validate-field"): (不可變動) 建立一個 POST 請求的 API，路徑為 /api/v1/employees/validate-field。
     * - @RequestBody: (不可變動) 告訴 Spring Boot 要從請求的 body 中讀取 JSON 資料，並轉換為 ValidateFieldRequest 物件。
     *
     * @param request (可自定義的參數名): 一個包含 "field" 和 "value" 的請求物件。
     * @return 返回一個 JSON 物件，格式為 { "isAvailable": boolean, "message": "..." }。
     */
    @PostMapping("/validate-field")
    public ResponseEntity<Map<String, Object>> validateField(@RequestBody ValidateFieldRequest request) {
        // 1. 呼叫 Service 層進行業務邏輯判斷
        boolean isAvailable = employeeService.isFieldAvailable(request.getField(), request.getValue());
        
        // 2. 準備要回傳給前端的 JSON 資料
        Map<String, Object> response = new HashMap<>();
        response.put("isAvailable", isAvailable);
        
        // 3. 如果欄位不可用 (已被註冊)，則附上錯誤訊息
        if (!isAvailable) {
            String fieldName = switch (request.getField()) {
                case "account" -> "登入帳號";
                case "email" -> "電子郵件";
                case "nationalId" -> "身分證字號";
                default -> "該欄位";
            };
            response.put("message", fieldName + "「" + request.getValue() + "」已被使用。");
        }
        
        // 4. 將結果用 ResponseEntity 包裝後回傳
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
}