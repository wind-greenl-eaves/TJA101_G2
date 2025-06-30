// =======================================================================================
// 檔案: EmployeeService.java (已復原)
// 說明: 定義員工服務層所有公開方法的介面。已移除照片相關方法。
// =======================================================================================
package com.eatfast.employee.service;

import com.eatfast.employee.dto.CreateEmployeeRequest;
import com.eatfast.employee.dto.EmployeeDto;
import com.eatfast.employee.dto.UpdateEmployeeRequest;
import java.util.List;
import java.util.Map;

public interface EmployeeService {

	EmployeeDto findEmployeeById(Long id);
	List<EmployeeDto> searchEmployees(Map<String, Object> searchParams);
	EmployeeDto createEmployee(CreateEmployeeRequest request);

	/**
	 * 【方法已復原】: 不再處理 MultipartFile 檔案。
	 */
	EmployeeDto updateEmployee(Long id, UpdateEmployeeRequest request);
	
	void deleteEmployee(Long id);
	void grantPermissionToEmployee(Long employeeId, Long permissionId);
	void revokePermissionFromEmployee(Long employeeId, Long permissionId);
}