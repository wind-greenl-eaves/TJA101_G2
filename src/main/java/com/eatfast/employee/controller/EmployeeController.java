package com.eatfast.employee.controller;

import com.eatfast.common.enums.AccountStatus;
import com.eatfast.common.enums.EmployeeRole;
import com.eatfast.employee.dto.CreateEmployeeRequest;
import com.eatfast.employee.dto.EmployeeDto;
import com.eatfast.employee.dto.UpdateEmployeeRequest;
import com.eatfast.employee.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 員工管理的 RESTful API 控制器。
 */
@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {
	
	private final EmployeeService employeeService;

	@Autowired
	public EmployeeController(EmployeeService employeeService) {
		this.employeeService = employeeService;
	}

	@PostMapping
	public ResponseEntity<EmployeeDto> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
		EmployeeDto createdEmployee = employeeService.createEmployee(request);
		return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
	}

	@GetMapping("/{id}")
	public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable Long id) {
		EmployeeDto employeeDto = employeeService.findEmployeeById(id);
		return ResponseEntity.ok(employeeDto);
	}

    /**
     * 【方法更新】: 此端點現在同時處理「查詢全部」與「複合條件查詢」。
     * [前端配對]: 'listAllEmployees.html' 頁面的 JavaScript 會根據 URL 是否有查詢參數來呼叫此 API。
     * - 無參數時 (GET /api/v1/employees): 查詢所有員工。
     * - 有參數時 (GET /api/v1/employees?username=...&role=...): 進行複合查詢。
     */
    @GetMapping
    public ResponseEntity<List<EmployeeDto>> searchEmployees(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) EmployeeRole role,
            @RequestParam(required = false) AccountStatus status,
            @RequestParam(required = false) Long storeId
    ) {
        // [可自定義的變數]: searchParams
        // 說明: 將所有請求參數打包成一個 Map，方便傳遞給 Service 層。
        Map<String, Object> searchParams = new HashMap<>();
        if (username != null) searchParams.put("username", username);
        if (role != null) searchParams.put("role", role);
        if (status != null) searchParams.put("status", status);
        if (storeId != null) searchParams.put("storeId", storeId);
        
        List<EmployeeDto> employees = employeeService.searchEmployees(searchParams);
        return ResponseEntity.ok(employees);
    }


	@PutMapping("/{id}")
	public ResponseEntity<EmployeeDto> updateEmployee(@PathVariable Long id, @Valid @RequestBody UpdateEmployeeRequest request) {
		EmployeeDto updatedEmployee = employeeService.updateEmployee(id, request);
		return ResponseEntity.ok(updatedEmployee);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
		employeeService.deleteEmployee(id);
		return ResponseEntity.noContent().build();
	}
}
