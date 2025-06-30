package com.eatfast.employee.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

import com.eatfast.employee.dto.EmployeeCreateDTO;
import com.eatfast.common.response.ValidationErrorResponse;

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
                dto.setRole((String) fieldValue.get(field));
                break;
            case "gender":
                dto.setGender((String) fieldValue.get(field));
                break;
            case "storeId":
                if (fieldValue.get(field) != null) {
                    dto.setStoreId(Long.valueOf(fieldValue.get(field).toString()));
                }
                break;
            default:
                return ResponseEntity.badRequest().body("未知的欄位名稱");
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
        return ResponseEntity.ok().build();
    }
}