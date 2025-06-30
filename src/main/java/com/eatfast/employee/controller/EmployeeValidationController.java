package com.eatfast.employee.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

import com.eatfast.employee.dto.EmployeeCreateDTO;
import com.eatfast.common.response.ValidationErrorResponse;
import com.eatfast.common.enums.EmployeeRole;
import com.eatfast.common.enums.Gender;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeValidationController {

    private final Validator validator;

    public EmployeeValidationController(Validator validator) {
        this.validator = validator;
    }

    @PostMapping("/validate/{field}")
    public ResponseEntity<?> validateField(
            @PathVariable String field,
            @RequestBody Map<String, Object> fieldValue) {
        
        // 創建一個臨時的 DTO 物件來進行驗證
        EmployeeCreateDTO dto = new EmployeeCreateDTO();
        
        try {
            // 根據欄位名稱設置值
            switch (field) {
                case "username":
                    dto.setUsername((String) fieldValue.get(field));
                    break;
                case "account":
                    dto.setAccount((String) fieldValue.get(field));
                    break;
                case "email":
                    dto.setEmail((String) fieldValue.get(field));
                    break;
                case "phone":
                    dto.setPhone((String) fieldValue.get(field));
                    break;
                case "nationalId":
                    dto.setNationalId((String) fieldValue.get(field));
                    break;
                case "password":
                    dto.setPassword((String) fieldValue.get(field));
                    break;
                case "role":
                    // 【修正】將 String 轉換為 EmployeeRole 枚舉
                    String roleValue = (String) fieldValue.get(field);
                    if (roleValue != null && !roleValue.trim().isEmpty()) {
                        try {
                            dto.setRole(EmployeeRole.valueOf(roleValue.toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            // 如果轉換失敗，設置為 null，讓驗證器處理
                            dto.setRole(null);
                        }
                    }
                    break;
                case "gender":
                    // 【修正】將 String 轉換為 Gender 枚舉
                    String genderValue = (String) fieldValue.get(field);
                    if (genderValue != null && !genderValue.trim().isEmpty()) {
                        try {
                            dto.setGender(Gender.valueOf(genderValue.toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            // 如果轉換失敗，設置為 null，讓驗證器處理
                            dto.setGender(null);
                        }
                    }
                    break;
                case "storeId":
                    if (fieldValue.get(field) != null) {
                        dto.setStoreId(Long.valueOf(fieldValue.get(field).toString()));
                    }
                    break;
                default:
                    return ResponseEntity.badRequest().body("未知的欄位名稱: " + field);
            }
        } catch (Exception e) {
            // 處理任何轉換錯誤
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "欄位值轉換失敗: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // 創建一個錯誤容器
        Errors errors = new BeanPropertyBindingResult(dto, "employeeCreateDTO");
        
        // 執行驗證
        validator.validate(dto, errors);
        
        // 如果有錯誤，回傳錯誤訊息
        if (errors.hasFieldErrors(field)) {
            Map<String, String> fieldErrors = new HashMap<>();
            fieldErrors.put(field, errors.getFieldError(field).getDefaultMessage());
            return ResponseEntity.badRequest().body(new ValidationErrorResponse(fieldErrors));
        }
        
        // 如果沒有錯誤，回傳成功
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("valid", true);
        successResponse.put("message", "欄位驗證通過");
        return ResponseEntity.ok(successResponse);
    }
}