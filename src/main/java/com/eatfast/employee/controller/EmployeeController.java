package com.eatfast.employee.controller;

import com.eatfast.employee.dto.CreateEmployeeRequest;
import com.eatfast.employee.dto.EmployeeDto;
import com.eatfast.employee.dto.UpdateEmployeeRequest;
import com.eatfast.employee.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * [可自定義的類別名稱]: EmployeeController
 * 員工管理的 RESTful API 控制器。
 * - @RestController: Spring 註解，結合了 @Controller 和 @ResponseBody，表明此類別處理 REST 請求。
 * - @RequestMapping: 定義此控制器下所有 API 的基礎路徑。
 */
@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * [不可變動的關鍵字]: @Autowired
     * [說明]: 透過建構子注入 EmployeeService，符合依賴注入的最佳實踐。
     */
    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * [不可變動的關鍵字]: @PostMapping
     * [說明]: 建立新員工 (Create)。
     * - @Valid: 觸發對 CreateEmployeeRequest 物件的驗證規則。
     * - @RequestBody: 將 HTTP 請求的 JSON Body 轉為 Java 物件。
     * @return ResponseEntity<EmployeeDto> - HTTP 狀態碼 201 (Created) 及新建的員工資料。
     */
    @PostMapping
    public ResponseEntity<EmployeeDto> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        EmployeeDto createdEmployee = employeeService.createEmployee(request);
        return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
    }

    /**
     * [不可變動的關鍵字]: @GetMapping("/{id}")
     * [說明]: 根據 ID 查詢特定員工 (Read)。
     * - @PathVariable: 從 URL 路徑中獲取 id 值。
     * @return ResponseEntity<EmployeeDto> - HTTP 狀態碼 200 (OK) 及查詢到的員工資料。
     */
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable Long id) {
        EmployeeDto employeeDto = employeeService.findEmployeeById(id);
        return ResponseEntity.ok(employeeDto);
    }

    /**
     * [不可變動的關鍵字]: @GetMapping
     * [說明]: 查詢所有員工列表 (Read)。
     * @return ResponseEntity<List<EmployeeDto>> - HTTP 狀態碼 200 (OK) 及員工列表。
     */
    @GetMapping
    public ResponseEntity<List<EmployeeDto>> getAllEmployees() {
        List<EmployeeDto> employees = employeeService.findAllEmployees();
        return ResponseEntity.ok(employees);
    }

    /**
     * [不可變動的關鍵字]: @PutMapping("/{id}")
     * [說明]: 更新指定 ID 的員工資料 (Update)。
     * @return ResponseEntity<EmployeeDto> - HTTP 狀態碼 200 (OK) 及更新後的員工資料。
     */
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> updateEmployee(@PathVariable Long id, @Valid @RequestBody UpdateEmployeeRequest request) {
        EmployeeDto updatedEmployee = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(updatedEmployee);
    }

    /**
     * [不可變動的關鍵字]: @DeleteMapping("/{id}")
     * [說明]: 刪除指定 ID 的員工 (Delete)。
     * @return ResponseEntity<Void> - HTTP 狀態碼 204 (No Content)，代表成功處理但無內容返回。
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
