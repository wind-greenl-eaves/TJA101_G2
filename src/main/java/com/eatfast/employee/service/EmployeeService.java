package com.eatfast.employee.service;

import com.eatfast.common.exception.DuplicateResourceException;
import com.eatfast.common.exception.ResourceNotFoundException;
import com.eatfast.employee.dto.CreateEmployeeRequest;
import com.eatfast.employee.dto.EmployeeDto;
import com.eatfast.employee.dto.UpdateEmployeeRequest;
import java.util.List;

/**
 * [可自定義的介面名稱]: EmployeeService
 * 員工服務層的公開介面 (Public Interface)。
 * 定義了所有與員工管理相關的業務方法，作為 Controller 層的合約。
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
     * 查詢所有員工的資料列表。
     *
     * @return 返回包含所有員工 DTO 的列表。
     */
    List<EmployeeDto> findAllEmployees();

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
     * @throws ResourceNotFoundException 若找不到對應 ID 的員工。
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
}
