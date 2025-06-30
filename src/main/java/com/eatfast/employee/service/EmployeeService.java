// =======================================================================================
// 檔案: EmployeeService.java (已修正)
// 說明: 在員工服務介面中，新增了用於即時欄位唯一性驗證的方法。
// =======================================================================================
package com.eatfast.employee.service;

import com.eatfast.employee.dto.CreateEmployeeRequest;
import com.eatfast.employee.dto.EmployeeDto;
import com.eatfast.employee.dto.UpdateEmployeeRequest;
import java.util.List;
import java.util.Map;

/**
 * [可自定義的介面名稱]: EmployeeService
 * 定義員工服務層所有公開方法的介面。
 */
public interface EmployeeService {

	EmployeeDto findEmployeeById(Long id);
	List<EmployeeDto> searchEmployees(Map<String, Object> searchParams);
	EmployeeDto createEmployee(CreateEmployeeRequest request);
	EmployeeDto updateEmployee(Long id, UpdateEmployeeRequest request);
	void deleteEmployee(Long id);
	void grantPermissionToEmployee(Long employeeId, Long permissionId);
	void revokePermissionFromEmployee(Long employeeId, Long permissionId);

	/**
	 * 【新增方法】: 檢查指定欄位的值是否在資料庫中已存在。
	 * 這個方法專門用來支援前端的即時驗證功能。
	 *
	 * @param field (可自定義的參數名): 要驗證的欄位名稱 (例如 "account", "email")。
	 * @param value (可自定義的參數名): 要驗證的值。
	 * @return 如果值尚未使用 (可用)，則返回 true；否則返回 false。
	 */
	boolean isFieldAvailable(String field, String value);
}
