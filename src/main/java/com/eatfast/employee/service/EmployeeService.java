// =======================================================================================
// 檔案: EmployeeService.java (服務層介面)
// 功能說明: 員工服務層介面定義，規範所有員工相關的業務操作
// 架構層級: 服務層介面 (Service Interface Layer)
// 配對關係:
//   - 實作類: EmployeeServiceImpl - 具體業務邏輯實現
//   - 控制器: EmployeeController - 呼叫此介面的方法
//   - 資料層: EmployeeRepository - 透過實作類間接使用
//   - 映射器: EmployeeMapper - 透過實作類進行 Entity 與 DTO 轉換
// 設計模式: 
//   - Interface Segregation Principle (介面隔離原則)
//   - Dependency Inversion Principle (依賴反轉原則)
//   - Service Layer Pattern (服務層模式)
// 業務功能覆蓋:
//   - CRUD 操作 (創建、讀取、更新、刪除)
//   - 認證與授權管理
//   - 密碼重設與忘記密碼處理
//   - 欄位唯一性驗證
//   - 檔案上傳處理
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

	/**
	 * 【新增登入安全方法】: 重置員工登入失敗次數
	 * 管理員可以使用此方法重置員工的登入失敗次數，解鎖被鎖定的帳號
	 * 
	 * @param employeeId 員工編號
	 */
	void resetEmployeeLoginFailureCount(Long employeeId);

	/**
	 * 【新增登入安全方法】: 解鎖員工帳號
	 * 管理員可以使用此方法解鎖因登入失敗過多而被停用的員工帳號
	 * 
	 * @param employeeId 員工編號
	 */
	void unlockEmployeeAccount(Long employeeId);

	/**
	 * 【新增登入安全方法】: 獲取員工登入失敗資訊
	 * 用於查看員工的登入失敗次數和鎖定狀態
	 * 
	 * @param employeeId 員工編號
	 * @return 包含登入失敗資訊的 Map
	 */
	Map<String, Object> getEmployeeLoginFailureInfo(Long employeeId);

	/**
	 * 【新增登入安全方法】: 獲取員工登入狀態（用於 SecurityController）
	 * 提供員工登入狀態的詳細資訊，包括失敗次數、鎖定狀態等
	 * 
	 * @param employeeId 員工編號
	 * @return 包含登入狀態資訊的 Map
	 */
	Map<String, Object> getEmployeeLoginStatus(Long employeeId);

	/**
	 * 【新增登入安全方法】: 重置員工登入失敗次數（用於 SecurityController）
	 * 管理員可以使用此方法重置員工的登入失敗次數
	 * 
	 * @param employeeId 員工編號
	 */
	void resetLoginFailureCount(Long employeeId);

	/**
	 * 【新增查詢方法】: 查找所有啟用狀態的員工實體（返回 Entity）
	 * 專門用於登入頁面顯示員工列表
	 * 
	 * @return 所有啟用狀態的員工實體列表
	 */
	List<com.eatfast.employee.model.EmployeeEntity> findAllActiveEmployeeEntities();

	/**
	 * 【新增查詢方法】: 查找所有已停權員工實體（返回 Entity）
	 * 專門用於登入頁面顯示已停權員工列表
	 * 
	 * @return 所有已停權的員工實體列表
	 */
	List<com.eatfast.employee.model.EmployeeEntity> findAllInactiveEmployeeEntities();

	/**
	 * 【修正批量上傳方法】: 批量上傳員工照片
	 * 一次性為多個員工上傳照片，主要用於初始化或批量更新
	 * 
	 * @param photos 照片檔案陣列
	 * @param employeeIds 對應的員工編號陣列
	 * @return 包含上傳結果的 Map，包括成功數量、失敗數量等資訊
	 * @throws IOException 檔案處理異常
	 */
	Map<String, Object> batchUploadPhotos(MultipartFile[] photos, Long[] employeeIds) throws IOException;
}