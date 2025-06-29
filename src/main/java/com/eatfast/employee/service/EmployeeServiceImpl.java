package com.eatfast.employee.service;

import com.eatfast.common.enums.AccountStatus;
import com.eatfast.common.enums.EmployeeRole;
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

import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 員工服務的實作類別。
 */
@Service
public class EmployeeServiceImpl implements EmployeeService {

	private final EmployeeRepository employeeRepository;
	private final StoreRepository storeRepository;
	private final EmployeeMapper employeeMapper;
	private final PasswordEncoder passwordEncoder;

	@Autowired
	public EmployeeServiceImpl(EmployeeRepository employeeRepository, StoreRepository storeRepository,
			EmployeeMapper employeeMapper, PasswordEncoder passwordEncoder) {
		this.employeeRepository = employeeRepository;
		this.storeRepository = storeRepository;
		this.employeeMapper = employeeMapper;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional(readOnly = true)
	public EmployeeDto findEmployeeById(Long id) {
		EmployeeEntity employeeEntity = employeeRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("找不到 ID 為 " + id + " 的員工"));
		return employeeMapper.toDto(employeeEntity);
	}

    /**
     * 【核心實作】: 使用 JPA Specification 動態建構複合查詢。
     */
    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDto> searchEmployees(Map<String, Object> searchParams) {
        // [不可變動的關鍵字]: Specification
        // 說明: 這是 JPA 中用於定義動態查詢的標準介面。
        Specification<EmployeeEntity> spec = (root, query, criteriaBuilder) -> {
            // [可自定義的變數]: predicates
            // 說明: 一個用來存放所有查詢條件的列表。
            List<Predicate> predicates = new ArrayList<>();

            // 1. 根據「員工姓名」進行模糊查詢 (like)
            if (searchParams.containsKey("username")) {
                String username = (String) searchParams.get("username");
                if (StringUtils.hasText(username)) {
                    predicates.add(criteriaBuilder.like(root.get("username"), "%" + username + "%"));
                }
            }
            
            // 2. 根據「員工角色」進行精確查詢 (equal)
            if (searchParams.containsKey("role")) {
                predicates.add(criteriaBuilder.equal(root.get("role"), searchParams.get("role")));
            }

            // 3. 根據「帳號狀態」進行精確查詢 (equal)
            if (searchParams.containsKey("status")) {
                predicates.add(criteriaBuilder.equal(root.get("status"), searchParams.get("status")));
            }

            // 4. 根據「所屬門市」進行精確查詢 (equal)
            if (searchParams.containsKey("storeId")) {
                predicates.add(criteriaBuilder.equal(root.get("store").get("storeId"), searchParams.get("storeId")));
            }

            // [不可變動的關鍵字]: criteriaBuilder.and
            // 說明: 將所有條件用 AND 邏輯組合在一起，回傳最終的查詢斷言。
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 執行查詢並將結果轉換為 DTO 列表
        return employeeRepository.findAll(spec).stream()
                .map(employeeMapper::toDto)
                .collect(Collectors.toList());
    }

	@Override
	@Transactional
	public EmployeeDto createEmployee(CreateEmployeeRequest request) {
		validateUniqueness(request.getAccount(), request.getEmail(), request.getNationalId());
		StoreEntity store = storeRepository.findById(request.getStoreId())
				.orElseThrow(() -> new ResourceNotFoundException("找不到 ID 為 " + request.getStoreId() + " 的門市"));
		EmployeeEntity newEmployee = employeeMapper.toEntity(request);
		newEmployee.setPassword(passwordEncoder.encode(request.getPassword()));
		newEmployee.setStore(store);
		EmployeeEntity savedEmployee = employeeRepository.save(newEmployee);
		return employeeMapper.toDto(savedEmployee);
	}

	@Override
	@Transactional
	public EmployeeDto updateEmployee(Long id, UpdateEmployeeRequest request) {
		EmployeeEntity employeeToUpdate = employeeRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("找不到 ID 為 " + id + " 的員工"));
		updateFields(employeeToUpdate, request);
		EmployeeEntity updatedEmployee = employeeRepository.save(employeeToUpdate);
		return employeeMapper.toDto(updatedEmployee);
	}

	@Override
	@Transactional
	public void deleteEmployee(Long id) {
		if (!employeeRepository.existsById(id)) {
			throw new ResourceNotFoundException("找不到 ID 為 " + id + " 的員工，無法刪除");
		}
		employeeRepository.deleteById(id);
	}

	// --- 輔助方法 (Helper Methods) ---
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

	private void updateFields(EmployeeEntity employeeToUpdate, UpdateEmployeeRequest request) {
		if (StringUtils.hasText(request.getUsername())) {
			employeeToUpdate.setUsername(request.getUsername());
		}
		if (StringUtils.hasText(request.getPassword())) {
			employeeToUpdate.setPassword(passwordEncoder.encode(request.getPassword()));
		}
		if (StringUtils.hasText(request.getEmail())) {
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
