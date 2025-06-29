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
 * 員工管理的 RESTful API 控制器。
 * * @RequestMapping("/api/v1/employees")
 * ↳ [全局基礎路徑]: 此控制器下的所有 API 端點，其 URL 都會以 "/api/v1/employees" 作為開頭。
 */
@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {
	
	private final EmployeeService employeeService;

	@Autowired
	public EmployeeController(EmployeeService employeeService) {
		this.employeeService = employeeService;
	}

	/**
     * 										[C] Create - 建立新員工。
     * * [HTTP 方法]: POST
     * [完整路徑]: /api/v1/employees
     * [前端配對]: 通常由一個 'addEmployee.html' 頁面的新增表單來呼叫。
     * [說明]: 接收一個包含新員工資料的 JSON 物件，並在成功建立後回傳新員工的完整資料。
     */
	@PostMapping
	public ResponseEntity<EmployeeDto> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
		EmployeeDto createdEmployee = employeeService.createEmployee(request);
		return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
	}

	/**
     *										 [R] Read - 根據 ID 查詢特定員工。
     *
     * [HTTP 方法]: GET
     * [完整路徑]: /api/v1/employees/{id} (例如: /api/v1/employees/5)
     * [前端配對]: 在 'select_page_employee.html' 中，當使用者選擇一個員工並送出查詢後，
     * 前端 JavaScript 會將頁面導向到 `employee_details.html?id={id}`，
     * 該詳情頁面接著會呼叫此 API 來獲取該員工的詳細資料。
     * [說明]: 從 URL 路徑中獲取 id，查詢並回傳單一員工的資料。
     */
	@GetMapping("/{id}")
	public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable Long id) {
		EmployeeDto employeeDto = employeeService.findEmployeeById(id);
		return ResponseEntity.ok(employeeDto);
	}

	/**
     *										 [R] Read - 查詢所有員工列表。
     *
     * [HTTP 方法]: GET
     * [完整路徑]: /api/v1/employees
     * [前端配對]: ★★★ 這正是 'select_page_employee.html' 頁面載入時，
     * 其 JavaScript 中的 `fetchAndPopulateEmployees` 函式所呼叫的 API，
     * 用於動態填充「選擇員工編號」和「選擇員工姓名」的下拉式選單。
     * [說明]: 回傳一個包含所有員工資料的列表。
     */
	@GetMapping
	public ResponseEntity<List<EmployeeDto>> getAllEmployees() {
		List<EmployeeDto> employees = employeeService.findAllEmployees();
		return ResponseEntity.ok(employees);
	}

	/**
     * 										[U] Update - 更新指定 ID 的員工資料。
     *
     * [HTTP 方法]: PUT
     * [完整路徑]: /api/v1/employees/{id} (例如: /api/v1/employees/5)
     * [前端配對]: 通常由一個 'editEmployee.html' 或員工詳情頁面中的「儲存變更」按鈕來呼叫。
     * [說明]: 接收要更新的員工 ID 及其部分或全部的新資料，回傳更新後的完整資料。
     */
	@PutMapping("/{id}")
	public ResponseEntity<EmployeeDto> updateEmployee(@PathVariable Long id,
			@Valid @RequestBody UpdateEmployeeRequest request) {
		EmployeeDto updatedEmployee = employeeService.updateEmployee(id, request);
		return ResponseEntity.ok(updatedEmployee);
	}

	/**
     * 										[D] Delete - 刪除指定 ID 的員工。
     *
     * [HTTP 方法]: DELETE
     * [完整路徑]: /api/v1/employees/{id} (例如: /api/v1/employees/5)
     * [前端配對]: 通常由員工列表頁或詳情頁中的「刪除」按鈕（通常會搭配一個確認對話框）來呼叫。
     * [說明]: 根據 URL 中的 ID 刪除對應的員工，成功後不返回任何內容主體 (No Content)。
     */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
		employeeService.deleteEmployee(id);
		return ResponseEntity.noContent().build();
	}
}
