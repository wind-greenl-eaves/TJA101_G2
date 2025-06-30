// =======================================================================================
// 檔案: EmployeeServiceImpl.java (已強化權限校驗)
// 說明: 員工服務層介面的完整實作。
// =======================================================================================
package com.eatfast.employee.service;

import com.eatfast.common.exception.DuplicateResourceException;
import com.eatfast.common.exception.ResourceNotFoundException;
import com.eatfast.employee.dto.CreateEmployeeRequest;
import com.eatfast.employee.dto.EmployeeDto;
import com.eatfast.employee.dto.UpdateEmployeeRequest;
import com.eatfast.employee.mapper.EmployeeMapper;
import com.eatfast.employee.model.EmployeeEntity;
import com.eatfast.employee.repository.EmployeeRepository;
import com.eatfast.employee.permission.model.EmployeePermissionEntity;
import com.eatfast.employee.permission.repository.EmployeePermissionRepository;
import com.eatfast.permission.model.PermissionEntity;
import com.eatfast.permission.repository.PermissionRepository;
import com.eatfast.permission.service.PermissionService; // 【新增注入】: 為了獲取角色的預設權限
import com.eatfast.permission.dto.PermissionDto; // 【新增注入】
import com.eatfast.store.model.StoreEntity;
import com.eatfast.store.repository.StoreRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set; // 【新增注入】
import java.util.stream.Collectors;


/**
 * [可自定義的類別名稱]: EmployeeServiceImpl
 * 員工服務 (EmployeeService) 的主要實作類別。
 * @Service: (不可變動) Spring 關鍵字，標記此類別為服務層的 Bean。
 */
@Service
public class EmployeeServiceImpl implements EmployeeService {
    
    private static final Logger log = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final EmployeeRepository employeeRepository;
    private final StoreRepository storeRepository;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;
    private final PermissionRepository permissionRepository;
    private final EmployeePermissionRepository employeePermissionRepository;
    private final PermissionService permissionService; // 【新增注入】

    @Value("${app.upload.employee-photos}")
    private String uploadPath;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository, StoreRepository storeRepository,
                               EmployeeMapper employeeMapper, PasswordEncoder passwordEncoder,
                               PermissionRepository permissionRepository,
                               EmployeePermissionRepository employeePermissionRepository,
                               PermissionService permissionService) { // 【新增注入】
        this.employeeRepository = employeeRepository;
        this.storeRepository = storeRepository;
        this.employeeMapper = employeeMapper;
        this.passwordEncoder = passwordEncoder;
        this.permissionRepository = permissionRepository;
        this.employeePermissionRepository = employeePermissionRepository;
        this.permissionService = permissionService; // 【新增注入】
    }

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
            if (searchParams.containsKey("username") && StringUtils.hasText((String) searchParams.get("username"))) {
                predicates.add(criteriaBuilder.like(root.get("username"), "%" + searchParams.get("username") + "%"));
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
        
        // 處理照片上傳
        if (request.getPhoto() != null && !request.getPhoto().isEmpty()) {
            String photoUrl = handlePhotoUpload(request.getPhoto(), employeeToUpdate.getEmployeeId());
            employeeToUpdate.setPhotoUrl(photoUrl);
        }
        
        EmployeeEntity updatedEmployee = employeeRepository.save(employeeToUpdate);
        return employeeMapper.toDto(updatedEmployee);
    }

    private String handlePhotoUpload(MultipartFile photo, Long employeeId) {
        String contentType = photo.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new IllegalArgumentException("只支援 JPG 或 PNG 格式的圖片");
        }
        if (photo.getSize() > 5 * 1024 * 1024) { // 5MB
            throw new IllegalArgumentException("圖片大小不能超過 5MB");
        }
        try {
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            String fileExtension = StringUtils.getFilenameExtension(photo.getOriginalFilename());
            String newFileName = "employee_" + employeeId + "_" + System.currentTimeMillis() + "." + fileExtension;
            Path filePath = uploadDir.resolve(newFileName);
            Files.copy(photo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            deleteOldPhoto(employeeId);
            return "/employee-photos/" + newFileName;
        } catch (IOException e) {
            throw new RuntimeException("無法保存照片文件", e);
        }
    }

    private void deleteOldPhoto(Long employeeId) {
        employeeRepository.findById(employeeId).ifPresent(employee -> {
            String oldPhotoUrl = employee.getPhotoUrl();
            if (oldPhotoUrl != null && !oldPhotoUrl.isEmpty()) {
                try {
                    String fileName = oldPhotoUrl.substring(oldPhotoUrl.lastIndexOf('/') + 1);
                    Path oldPhotoPath = Paths.get(uploadPath, fileName);
                    Files.deleteIfExists(oldPhotoPath);
                } catch (IOException e) {
                    log.warn("刪除舊照片失敗: {}", e.getMessage());
                }
            }
        });
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new ResourceNotFoundException("找不到 ID 為 " + id + " 的員工，無法刪除");
        }
        employeeRepository.deleteById(id);
    }
    
    /**
     * 【方法已強化】: grantPermissionToEmployee
     * 為員工授予一項權限，並增加了業務邏輯校驗。
     * * @param employeeId (可自定義的參數名) 員工 ID
     * @param permissionId (可自定義的參數名) 權限 ID
     */
    @Override
    @Transactional
    public void grantPermissionToEmployee(Long employeeId, Long permissionId) {
        // 1. 查找主要的實體物件
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到員工 (ID: " + employeeId + ")"));
        PermissionEntity permissionToGrant = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到權限 (ID: " + permissionId + ")"));

        // 2. 檢查權限是否已存在，避免重複操作
        if (employeePermissionRepository.existsByEmployeeEmployeeIdAndPermissionPermissionId(employeeId, permissionId)) {
            throw new DuplicateResourceException("員工 (ID: " + employeeId + ") 已擁有權限 (ID: " + permissionId + ")");
        }
        
        // 3. 【新增的業務邏輯校驗】
        // 檢查欲授予的權限是否符合該員工角色的預設範圍
        validatePermissionAssignment(employee, permissionToGrant);

        // 4. 建立並儲存新的關聯
        EmployeePermissionEntity newAssociation = new EmployeePermissionEntity();
        newAssociation.setEmployee(employee);
        newAssociation.setPermission(permissionToGrant);
        
        employeePermissionRepository.save(newAssociation);
    }

    /**
     * 【新增的私有輔助方法】
     * 驗證「權限指派」的合法性。
     * @param employee (可自定義的參數名) 目標員工實體
     * @param permissionToGrant (可自定義的參數名) 準備授予的權限實體
     */
    private void validatePermissionAssignment(EmployeeEntity employee, PermissionEntity permissionToGrant) {
        // [不可變動]: 呼叫 permissionService 的核心方法
        // 說明: 獲取該員工角色(Role)所有預設就應該擁有的權限。
        Set<PermissionDto> allowedPermissions = permissionService.findPermissionsByRole(employee.getRole());

        // [不可變動]: 使用 Stream API 進行判斷
        // 說明: 檢查 `allowedPermissions` 集合中，是否有任何一個 PermissionDto 的 ID
        // 與我們想授予的 `permissionToGrant` 的 ID 相符。
        boolean isAllowed = allowedPermissions.stream()
                .anyMatch(dto -> dto.getPermissionId().equals(permissionToGrant.getPermissionId()));

        // 如果 `isAllowed` 為 false，代表這是一個「越級」的授權操作，應拋出例外。
        if (!isAllowed) {
            // [不可變動]: throw new ... 是一個關鍵的程式中斷語法。
            throw new IllegalArgumentException(
                "權限指派無效：權限 '" + permissionToGrant.getDescription() + 
                "' 不適用於角色 '" + employee.getRole().name() + "'。"
            );
        }
    }

    @Override
    @Transactional
    public void revokePermissionFromEmployee(Long employeeId, Long permissionId) {
        EmployeePermissionEntity associationToRemove = employeePermissionRepository
                .findByEmployeeEmployeeIdAndPermissionPermissionId(employeeId, permissionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "找不到員工 (ID: " + employeeId + ") 與權限 (ID: " + permissionId + ") 之間的關聯"));
        employeePermissionRepository.delete(associationToRemove);
    }

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
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            if (request.getUsername().trim().length() < 2) {
                throw new IllegalArgumentException("員工姓名至少需要2個字");
            }
            employeeToUpdate.setUsername(request.getUsername().trim());
        }

        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            String phone = request.getPhone().trim();
            String cleanPhone = phone.replaceAll("[^0-9]", "");
            
            if (cleanPhone.matches("^09\\d{8}$") ||
                cleanPhone.matches("^0[2-8]\\d{7,8}$")) {
                employeeToUpdate.setPhone(phone); 
            } else {
                throw new IllegalArgumentException("請輸入有效的台灣手機或市話號碼");
            }
        }

        if (request.getRole() != null) {
            employeeToUpdate.setRole(request.getRole());
        }
        if (request.getStatus() != null) {
            employeeToUpdate.setStatus(request.getStatus());
        }

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            if (!request.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")) {
                throw new IllegalArgumentException("密碼需至少8個字元，且包含至少一個字母和一個數字");
            }
            employeeToUpdate.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (StringUtils.hasText(request.getEmail())) {
            String email = request.getEmail().trim().toLowerCase();
            if (!email.matches("[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$")) {
                throw new IllegalArgumentException("請輸入有效的電子郵件地址");
            }
            
            employeeRepository.findByEmail(email).ifPresent(existingEmployee -> {
                if (!existingEmployee.getEmployeeId().equals(employeeToUpdate.getEmployeeId())) {
                    throw new DuplicateResourceException("電子郵件 " + email + " 已被其他員工使用");
                }
            });
            employeeToUpdate.setEmail(email);
        }

        if (request.getStoreId() != null && 
            !request.getStoreId().equals(employeeToUpdate.getStore().getStoreId())) {
            StoreEntity store = storeRepository.findById(request.getStoreId())
                    .orElseThrow(() -> new ResourceNotFoundException("找不到 ID 為 " + request.getStoreId() + " 的門市"));
            employeeToUpdate.setStore(store);
        }
    }
}
