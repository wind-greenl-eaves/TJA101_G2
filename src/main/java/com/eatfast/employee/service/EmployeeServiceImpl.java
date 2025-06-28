package com.eatfast.employee.service;

import com.eatfast.common.exception.DuplicateResourceException;
import com.eatfast.common.exception.ResourceNotFoundException;
import com.eatfast.employee.dto.CreateEmployeeRequest;
import com.eatfast.employee.dto.EmployeeDto;
import com.eatfast.employee.dto.UpdateEmployeeRequest;
import com.eatfast.employee.mapper.EmployeeMapper;
import com.eatfast.employee.model.EmployeeEntity;
import com.eatfast.employee.repository.EmployeeRepository;
import com.eatfast.store.model.StoreEntity;
import com.eatfast.store.repository.StoreRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * [可自定義的類別名稱]: EmployeeServiceImpl 員工服務的實作類別，封裝所有與員工相關的商業邏輯。 
 * - @Service: 標記此類別為Spring 的服務層元件。
 */
@Service
public class EmployeeServiceImpl implements EmployeeService {

	// 依賴注入 (Dependency Injection)
	private final EmployeeRepository employeeRepository;
	private final StoreRepository storeRepository;
	private final EmployeeMapper employeeMapper;
	private final PasswordEncoder passwordEncoder;

	/**
	 * [不可變動的關鍵字]: @Autowired [說明]: 使用建構子注入所有必要的依賴，是 Spring 推薦的最佳實踐。
	 */
	@Autowired
	public EmployeeServiceImpl(EmployeeRepository employeeRepository, StoreRepository storeRepository,
			EmployeeMapper employeeMapper, PasswordEncoder passwordEncoder) {
		this.employeeRepository = employeeRepository;
		this.storeRepository = storeRepository;
		this.employeeMapper = employeeMapper;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * [不可變動的關鍵字]: @Override, @Transactional [說明]: 根據 ID 查詢員工。
	 * - @Transactional(readOnly = true): 標記為唯讀交易，可優化資料庫查詢效能。
	 */
	@Override
	@Transactional(readOnly = true)
	public EmployeeDto findEmployeeById(Long id) {
		// 從資料庫查找員工，若不存在則拋出 ResourceNotFoundException。
		EmployeeEntity employeeEntity = employeeRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("找不到 ID 為 " + id + " 的員工"));
		// 將 Entity 轉換為 DTO 後回傳。
		return employeeMapper.toDto(employeeEntity);
	}

	/**
	 * 查詢所有員工列表。
	 */
	@Override
	@Transactional(readOnly = true)
	public List<EmployeeDto> findAllEmployees() {
		// 取得所有員工 Entity，並透過 Stream API 轉換為 DTO 列表。
		return employeeRepository.findAll().stream().map(employeeMapper::toDto).collect(Collectors.toList());
	}

	/**
	 * 新增一名員工。 - @Transactional: 寫入操作需開啟交易，確保資料一致性。
	 */
	@Override
	@Transactional
	public EmployeeDto createEmployee(CreateEmployeeRequest request) {
		// 步驟 1: 驗證帳號、信箱、身分證號是否已存在，防止資料重複。
		validateUniqueness(request.getAccount(), request.getEmail(), request.getNationalId());

		// 步驟 2: 驗證其所屬的門市 ID 是否真實存在。
		StoreEntity store = storeRepository.findById(request.getStoreId())
				.orElseThrow(() -> new ResourceNotFoundException("找不到 ID 為 " + request.getStoreId() + " 的門市"));

		// 步驟 3: 將傳入的 Request DTO 轉換為 Entity。
		EmployeeEntity newEmployee = employeeMapper.toEntity(request);

		// 步驟 4: 將明文密碼加密後再設定回 Entity。
		newEmployee.setPassword(passwordEncoder.encode(request.getPassword()));

		// 步驟 5: 設定員工與門市的關聯。
		newEmployee.setStore(store);

		// 步驟 6: 將 Entity 存入資料庫。
		EmployeeEntity savedEmployee = employeeRepository.save(newEmployee);

		// 步驟 7: 將儲存成功後、帶有 DB 生成資訊 (如 ID) 的 Entity 轉為 DTO 回傳。
		return employeeMapper.toDto(savedEmployee);
	}

	/**
	 * 根據 ID 更新指定的員工資料。
	 */
	@Override
	@Transactional
	public EmployeeDto updateEmployee(Long id, UpdateEmployeeRequest request) {
		// 步驟 1: 取得要更新的目標員工 Entity。
		EmployeeEntity employeeToUpdate = employeeRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("找不到 ID 為 " + id + " 的員工"));

		// 步驟 2: 逐一檢查請求中的欄位，僅更新有提供值的欄位。
		updateFields(employeeToUpdate, request);

		// 步驟 3: 儲存更新後的 Entity。
		EmployeeEntity updatedEmployee = employeeRepository.save(employeeToUpdate);

		return employeeMapper.toDto(updatedEmployee);
	}

	/**
	 * 根據 ID 刪除員工。
	 */
	@Override
	@Transactional
	public void deleteEmployee(Long id) {
		// 在刪除前先進行存在性檢查，以便拋出更明確的錯誤訊息。
		if (!employeeRepository.existsById(id)) {
			throw new ResourceNotFoundException("找不到 ID 為 " + id + " 的員工，無法刪除");
		}
		employeeRepository.deleteById(id);
	}

	// ================================================================
	// 輔助方法 (Helper Methods)
	// ================================================================

	/**
	 * 輔助方法：執行新增員工前的唯一性驗證。
	 */
	private void validateUniqueness(String account, String email, String nationalId) {
		if (employeeRepository.existsByAccount(account)) {
			throw new DuplicateResourceException("登入帳號 " + account + " 已被註冊");
		}
		if (employeeRepository.existsByEmail(email)) {
			throw new DuplicateResourceException("電子郵件 " + email + " 已被註冊");
		}
		if (employeeRepository.existsByNationalId(nationalId)) {
			throw new DuplicateResourceException("身分證字號已被註冊");
		}
	}

	/**
	 * 輔助方法：執行更新員工時的欄位更新邏輯。
	 */
	private void updateFields(EmployeeEntity employeeToUpdate, UpdateEmployeeRequest request) {
		// 使用 Spring 的 StringUtils.hasText 檢查字串是否為 null 或空。
		if (StringUtils.hasText(request.getUsername())) {
			employeeToUpdate.setUsername(request.getUsername());
		}
		if (StringUtils.hasText(request.getPassword())) {
			employeeToUpdate.setPassword(passwordEncoder.encode(request.getPassword()));
		}
		if (StringUtils.hasText(request.getEmail())) {
			// 驗證新 Email 是否已被「其他」員工使用。
			employeeRepository.findByEmail(request.getEmail()).ifPresent(existingEmployee -> {
				if (!existingEmployee.getEmployeeId().equals(employeeToUpdate.getEmployeeId())) {
					throw new DuplicateResourceException("電子郵件 " + request.getEmail() + " 已被其他員工使用");
				}
			});
			employeeToUpdate.setEmail(request.getEmail());
		}
		if (StringUtils.hasText(request.getPhone())) {
			employeeToUpdate.setPhone(request.getPhone());
		}
		if (request.getRole() != null) {
			employeeToUpdate.setRole(request.getRole());
		}
		if (request.getStatus() != null) {
			employeeToUpdate.setStatus(request.getStatus());
		}
		if (request.getStoreId() != null) {
			StoreEntity store = storeRepository.findById(request.getStoreId())
					.orElseThrow(() -> new ResourceNotFoundException("找不到 ID 為 " + request.getStoreId() + " 的門市"));
			employeeToUpdate.setStore(store);
		}
	}
}
