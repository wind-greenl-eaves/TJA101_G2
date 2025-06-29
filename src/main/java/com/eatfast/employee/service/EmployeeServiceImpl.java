package com.eatfast.employee.service;

import com.eatfast.common.exception.DuplicateResourceException;
import com.eatfast.common.exception.ResourceNotFoundException;
import com.eatfast.employee.dto.CreateEmployeeRequest;
import com.eatfast.employee.dto.EmployeeDto;
import com.eatfast.employee.dto.UpdateEmployeeRequest;
import com.eatfast.employee.mapper.EmployeeMapper;
import com.eatfast.employee.model.EmployeeEntity;
import com.eatfast.employee.repository.EmployeeRepository;
// 【檔案路徑配對】: 匯入新增的 Repository 和 Permission 相關類別
import com.eatfast.employee.permission.model.EmployeePermissionEntity;
import com.eatfast.employee.permission.repository.EmployeePermissionRepository;
import com.eatfast.permission.model.PermissionEntity;
import com.eatfast.permission.repository.PermissionRepository;
import com.eatfast.store.model.StoreEntity;
import com.eatfast.store.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// ... (其他 import)

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    // 【新增依賴】
    private final PermissionRepository permissionRepository;
    private final EmployeePermissionRepository employeePermissionRepository;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository, StoreRepository storeRepository,
                               EmployeeMapper employeeMapper, PasswordEncoder passwordEncoder,
                               // 【新增依賴注入】
                               PermissionRepository permissionRepository,
                               EmployeePermissionRepository employeePermissionRepository) {
        this.employeeRepository = employeeRepository;
        this.storeRepository = storeRepository;
        this.employeeMapper = employeeMapper;
        this.passwordEncoder = passwordEncoder;
        // 【新增依賴初始化】
        this.permissionRepository = permissionRepository;
        this.employeePermissionRepository = employeePermissionRepository;
    }

    // ... (原有的 findEmployeeById, searchEmployees, createEmployee, updateEmployee, deleteEmployee, 及輔助方法)
    @Override
    @Transactional(readOnly = true)
    public EmployeeDto findEmployeeById(Long id) {
        EmployeeEntity employeeEntity = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 ID 為 " + id + " 的員工"));
        return employeeMapper.toDto(employeeEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDto> searchEmployees(Map<String, Object> searchParams) {
        Specification<EmployeeEntity> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (searchParams.containsKey("username")) {
                String username = (String) searchParams.get("username");
                if (StringUtils.hasText(username)) {
                    predicates.add(criteriaBuilder.like(root.get("username"), "%" + username + "%"));
                }
            }
            if (searchParams.containsKey("role")) {
                predicates.add(criteriaBuilder.equal(root.get("role"), searchParams.get("role")));
            }
            if (searchParams.containsKey("status")) {
                predicates.add(criteriaBuilder.equal(root.get("status"), searchParams.get("status")));
            }
            if (searchParams.containsKey("storeId")) {
                predicates.add(criteriaBuilder.equal(root.get("store").get("storeId"), searchParams.get("storeId")));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
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


    // ================================================================
    //         【新增區塊】權限指派與移除的邏輯實作
    // ================================================================

    @Override
    @Transactional
    public void grantPermissionToEmployee(Long employeeId, Long permissionId) {
        // 1. 檢查權限是否已存在，若存在則直接拋出例外，避免後續不必要的資料庫查詢。
        if (employeePermissionRepository.existsByEmployeeEmployeeIdAndPermissionPermissionId(employeeId, permissionId)) {
            throw new DuplicateResourceException("員工 (ID: " + employeeId + ") 已擁有權限 (ID: " + permissionId + ")");
        }

        // 2. 查找員工和權限實體。若任一不存在，findById().orElseThrow() 會自動拋出例外。
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到員工 (ID: " + employeeId + ")"));

        PermissionEntity permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到權限 (ID: " + permissionId + ")"));

        // 3. 建立新的關聯實體
        EmployeePermissionEntity newAssociation = new EmployeePermissionEntity();
        newAssociation.setEmployee(employee);
        newAssociation.setPermission(permission);

        // 4. 儲存關聯實體到資料庫，完成授權。
        employeePermissionRepository.save(newAssociation);
    }

    @Override
    @Transactional
    public void revokePermissionFromEmployee(Long employeeId, Long permissionId) {
        // 1. 使用我們在 Repository 中定義的客製化方法，直接查找需要刪除的關聯實體。
        EmployeePermissionEntity associationToRemove = employeePermissionRepository
                .findByEmployeeEmployeeIdAndPermissionPermissionId(employeeId, permissionId)
                // 2. 如果找不到對應的關聯，說明權限關係不存在，拋出例外。
                .orElseThrow(() -> new ResourceNotFoundException(
                        "找不到員工 (ID: " + employeeId + ") 與權限 (ID: " + permissionId + ") 之間的關聯"));

        // 3. 若找到，則直接刪除該關聯實體，完成撤銷。
        employeePermissionRepository.delete(associationToRemove);
    }

    // --- (原有的輔助方法) ---
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
