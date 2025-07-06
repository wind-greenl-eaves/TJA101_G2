// =======================================================================================
// 檔案: EmployeeService.java (已修正)
// 說明: 在員工服務介面中，新增了用於即時欄位唯一性驗證的方法。
// =======================================================================================
package com.eatfast.employee.service;

import com.eatfast.employee.dto.CreateEmployeeRequest;
import com.eatfast.employee.dto.EmployeeDTO;
import com.eatfast.employee.dto.UpdateEmployeeRequest;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * [可自定義的介面名稱]: EmployeeService
 * 定義員工服務層所有公開方法的介面。
 */
public interface EmployeeService {

	EmployeeDTO findEmployeeById(Long id);
	List<EmployeeDTO> searchEmployees(Map<String, Object> searchParams);
	EmployeeDTO createEmployee(CreateEmployeeRequest request);
	EmployeeDTO updateEmployee(Long id, UpdateEmployeeRequest request);
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

	/**
	 * 【新增登入方法】: 員工登入驗證
	 * 驗證員工帳號密碼，並返回員工資訊
	 *
	 * @param account 員工帳號
	 * @param password 員工密碼（明文）
	 * @return 登入成功返回員工 DTO，失敗則拋出異常
	 */
	EmployeeDTO authenticateEmployee(String account, String password);

	/**
	 * 【新增方法】: 獲取所有啟用狀態的員工列表（用於登入頁面的管理員小幫手）
	 * @return 所有啟用狀態的員工列表
	 */
	List<EmployeeDTO> findAllActiveEmployees();

	/**
	 * 【新增方法】: 獲取所有已停權員工列表（用於登入頁面的管理員小幫手）
	 * @return 所有已停權的員工列表
	 */
	List<EmployeeDTO> findAllInactiveEmployees();

	/**
	 * 【新增忘記密碼方法】: 處理忘記密碼請求
	 * 根據帳號或郵件查找員工，並生成新的臨時密碼
	 *
	 * @param accountOrEmail 員工帳號或電子郵件
	 * @return 處理結果訊息
	 */
	String processForgotPassword(String accountOrEmail);

	/**
	 * 【新增照片上傳方法】: 更新員工照片
	 * 
	 * @param employeeId 員工編號
	 * @param photo 照片檔案
	 * @return 更新後的員工資訊
	 * @throws IOException 檔案處理異常
	 */
	EmployeeDTO updateEmployeePhoto(Long employeeId, MultipartFile photo) throws IOException;
}