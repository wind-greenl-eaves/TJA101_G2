package com.eatfast.employee.controller;

import com.eatfast.employee.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * 員工登入安全測試控制器
 * 用於測試和管理員工登入失敗次數限制功能
 */
@RestController
@RequestMapping("/employee/security")
public class EmployeeSecurityController {
    
    @Autowired
    private EmployeeService employeeService;
    
    /**
     * 檢查員工登入狀態
     * GET /employee/security/status/{id}
     * ResponseEntity<Map<String, Object>> 包含員工登入狀態資訊
     */
    @GetMapping("/status/{id}")
    public ResponseEntity<Map<String, Object>> getEmployeeLoginStatus(@PathVariable Long id) {
        try {
            Map<String, Object> status = employeeService.getEmployeeLoginStatus(id);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 重置員工登入失敗次數
     * POST /employee/security/reset/{id}
     */
    @PostMapping("/reset/{id}")
    public ResponseEntity<Map<String, String>> resetLoginFailureCount(@PathVariable Long id) {
        try {
            employeeService.resetLoginFailureCount(id);
            return ResponseEntity.ok(Map.of("message", "登入失敗次數已重置"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}