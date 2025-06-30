// =======================================================================================
// 檔案: EmployeeController.java (已復原)
// 說明: 員工管理的 RESTful API 控制器。已移除檔案上傳相關邏輯。
// =======================================================================================
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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping
    public ResponseEntity<List<EmployeeDto>> searchEmployees(
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
        List<EmployeeDto> employees = employeeService.searchEmployees(searchParams);
        return ResponseEntity.ok(employees);
    }

    /**
     * 【API 修改】: 改為支援文件上傳的格式。
     */
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> updateEmployee(
            @PathVariable Long id,
            @ModelAttribute @Valid UpdateEmployeeRequest request) {
        EmployeeDto updatedEmployee = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(updatedEmployee);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
    
    // 【已移除】getEmployeePhoto API 端點
    
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
}