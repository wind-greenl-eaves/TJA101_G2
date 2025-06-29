package com.eatfast.employee.service;

import com.eatfast.common.enums.AccountStatus;
import com.eatfast.common.enums.EmployeeRole;
import com.eatfast.common.exception.DuplicateResourceException;
import com.eatfast.common.exception.ResourceNotFoundException;
import com.eatfast.employee.dto.CreateEmployeeRequest;
import com.eatfast.employee.dto.EmployeeDto;
import com.eatfast.employee.dto.UpdateEmployeeRequest;
import java.util.List;
import java.util.Map;

/**
 * [可自定義的介面名稱]: EmployeeService 員工服務層的公開介面。
 */
public interface EmployeeService {

	/**
	 * 根據員工 ID 查詢員工資料。
	 * 
	 * @param id 員工主鍵 ID。
	 * @return 返回對應的員工資料傳輸物件 (EmployeeDto)。
	 * @throws ResourceNotFoundException 若找不到對應 ID 的員工。
	 */
	EmployeeDto findEmployeeById(Long id);

	/**
	 * 【方法更新】: 查詢所有員工的資料列表，並支援可選的複合查詢條件。 如果所有參數皆為 null，則回傳所有員工。
	 *
	 * @param searchParams 包含查詢條件的 Map，key 為欄位名 (如 "username", "role"), value 為查詢值。
	 * @return 返回符合條件的員工 DTO 列表。
	 */
	List<EmployeeDto> searchEmployees(Map<String, Object> searchParams);

	/**
	 * 新增一名員工。
	 * 
	 * @param request 包含新員工所有必要資訊的請求物件。
	 * @return 返回成功建立後、包含資料庫生成資訊 (如 ID) 的員工 DTO。
	 * @throws DuplicateResourceException 若帳號、信箱或身分證號已存在。
	 */
	EmployeeDto createEmployee(CreateEmployeeRequest request);

	/**
	 * 根據 ID 更新指定的員工資料。
	 * 
	 * @param id      要更新的員工主鍵 ID。
	 * @param request 包含要更新欄位的請求物件。
	 * @return 返回更新成功後的員工 DTO。
	 * @throws ResourceNotFoundException  若找不到對應 ID 的員工。
	 * @throws DuplicateResourceException 若更新的 email 與其他員工重複。
	 */
	EmployeeDto updateEmployee(Long id, UpdateEmployeeRequest request);

	/**
	 * 根據 ID 刪除一名員工。
	 * 
	 * @param id 要刪除的員工主鍵 ID。
	 * @throws ResourceNotFoundException 若找不到對應 ID 的員工。
	 */
	void deleteEmployee(Long id);

	// ================================================================
	// 【新增區塊】權限指派與移除方法 (Permission Management)
	// ================================================================

	/**
	 * 授予指定員工一項權限。
	 *
	 * @param employeeId   (可自定義的參數名) 員工的主鍵 ID。
	 * @param permissionId (可自定義的參數名) 要授予的權限主鍵 ID。
	 * @throws ResourceNotFoundException  如果找不到對應的員工或權限。
	 * @throws DuplicateResourceException 如果該員工已擁有此權限。
	 */
	void grantPermissionToEmployee(Long employeeId, Long permissionId);

	/**
	 * 撤銷指定員工的一項權限。
	 *
	 * @param employeeId   (可自定義的參數名) 員工的主鍵 ID。
	 * @param permissionId (可自定義的參數名) 要撤銷的權限主鍵 ID。
	 * @throws ResourceNotFoundException 如果找不到該員工、權限，或兩者之間不存在指定的權限關聯。
	 */
	void revokePermissionFromEmployee(Long employeeId, Long permissionId);
}
