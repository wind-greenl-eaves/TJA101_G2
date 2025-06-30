// =======================================================================================
// 檔案: EmployeeServiceImpl.java (已修正 - 移除密碼加密)
// 說明: 移除密碼加密功能，改為使用明文密碼存儲和處理
// =======================================================================================
package com.eatfast.employee.service;

import com.eatfast.common.exception.DuplicateResourceException;
import com.eatfast.common.exception.ResourceNotFoundException;
import com.eatfast.common.service.FileService;
import com.eatfast.employee.dto.CreateEmployeeRequest;
import com.eatfast.employee.dto.EmployeeDTO;
import com.eatfast.employee.dto.UpdateEmployeeRequest;
import com.eatfast.employee.mapper.EmployeeMapper;
import com.eatfast.employee.model.EmployeeEntity;
import com.eatfast.employee.repository.EmployeeRepository;
import com.eatfast.employee.permission.model.EmployeePermissionEntity;
import com.eatfast.employee.permission.repository.EmployeePermissionRepository;
import com.eatfast.permission.model.PermissionEntity;
import com.eatfast.permission.repository.PermissionRepository;
import com.eatfast.permission.service.PermissionService;
import com.eatfast.permission.dto.PermissionDto;
import com.eatfast.store.model.StoreEntity;
import com.eatfast.store.repository.StoreRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    
    private static final Logger log = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final EmployeeRepository employeeRepository;
    private final StoreRepository storeRepository;
    private final EmployeeMapper employeeMapper;
    private final PermissionRepository permissionRepository;
    private final EmployeePermissionRepository employeePermissionRepository;
    private final PermissionService permissionService;

    @Value("${app.upload.employee-photos}")
    private String uploadPath;

    @Autowired
    private FileService fileService;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository, StoreRepository storeRepository,
                               EmployeeMapper employeeMapper,
                               PermissionRepository permissionRepository,
                               EmployeePermissionRepository employeePermissionRepository,
                               PermissionService permissionService) {
        this.employeeRepository = employeeRepository;
        this.storeRepository = storeRepository;
        this.employeeMapper = employeeMapper;
        this.permissionRepository = permissionRepository;
        this.employeePermissionRepository = employeePermissionRepository;
        this.permissionService = permissionService;
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDTO findEmployeeById(Long id) {
        EmployeeEntity employeeEntity = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 ID 為 " + id + " 的員工"));
        return employeeMapper.toDto(employeeEntity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDTO> searchEmployees(Map<String, Object> searchParams) {
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
    public EmployeeDTO createEmployee(CreateEmployeeRequest request) {
        validateUniqueness(request.getAccount(), request.getEmail(), request.getNationalId());
        StoreEntity store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("找不到 ID 為 " + request.getStoreId() + " 的門市"));
        
        EmployeeEntity newEmployee = employeeMapper.toEntity(request);
        // 【修改】直接使用明文密碼，不進行加密
        newEmployee.setPassword(request.getPassword());
        newEmployee.setStore(store);
        
        // 處理照片上傳
        String photoUrl = null;
        if (request.getPhoto() != null && !request.getPhoto().isEmpty()) {
            try {
                String fileName = fileService.saveEmployeePhoto(request.getPhoto());
                photoUrl = "/employee-photos/" + fileName;
            } catch (IOException e) {
                throw new RuntimeException("照片上傳失敗", e);
            }
        }
        newEmployee.setPhotoUrl(photoUrl);
        
        EmployeeEntity savedEmployee = employeeRepository.save(newEmployee);
        return employeeMapper.toDto(savedEmployee);
    }

    @Override
    @Transactional
    public EmployeeDTO updateEmployee(Long id, UpdateEmployeeRequest request) {
        EmployeeEntity employeeToUpdate = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 ID 為 " + id + " 的員工"));

        updateFields(employeeToUpdate, request);
        
        if (request.getPhoto() != null && !request.getPhoto().isEmpty()) {
            String photoUrl = handlePhotoUpload(request.getPhoto(), employeeToUpdate.getEmployeeId());
            employeeToUpdate.setPhotoUrl(photoUrl);
        }
        
        EmployeeEntity updatedEmployee = employeeRepository.save(employeeToUpdate);
        return employeeMapper.toDto(updatedEmployee);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        EmployeeEntity employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("找不到 ID 為 " + id + " 的員工，無法刪除"));

        // 刪除關聯的照片檔案
        if (employee.getPhotoUrl() != null) {
            String fileName = employee.getPhotoUrl().substring(employee.getPhotoUrl().lastIndexOf("/") + 1);
            fileService.deleteEmployeePhoto(fileName);
        }

        employeeRepository.delete(employee);
    }
    
    @Override
    @Transactional
    public void grantPermissionToEmployee(Long employeeId, Long permissionId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到員工 (ID: " + employeeId + ")"));
        PermissionEntity permissionToGrant = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到權限 (ID: " + permissionId + ")"));

        if (employeePermissionRepository.existsByEmployeeEmployeeIdAndPermissionPermissionId(employeeId, permissionId)) {
            throw new DuplicateResourceException("員工 (ID: " + employeeId + ") 已擁有權限 (ID: " + permissionId + ")");
        }
        
        validatePermissionAssignment(employee, permissionToGrant);

        EmployeePermissionEntity newAssociation = new EmployeePermissionEntity();
        newAssociation.setEmployee(employee);
        newAssociation.setPermission(permissionToGrant);
        
        employeePermissionRepository.save(newAssociation);
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

    /**
     * 【新增方法實作】- 改進版本
     * 檢查指定欄位的值是否可用 (尚未被註冊)。
     * 加入更嚴格的輸入驗證和日誌記錄
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isFieldAvailable(String field, String value) {
        // 輸入參數驗證
        if (!StringUtils.hasText(field) || !StringUtils.hasText(value)) {
            log.warn("欄位驗證請求包含空值 - field: {}, value: {}", field, value);
            return false;
        }
        
        // 值的基本格式預檢
        String trimmedValue = value.trim();
        if (trimmedValue.isEmpty()) {
            return false;
        }
        
        // 排除不需要驗證的欄位
        if ("photo".equals(field)) {
            return true;
        }
        
        try {
            // 使用 switch 陳述式來動態呼叫對應的 repository 方法
            boolean isAvailable = switch (field) {
                case "account" -> {
                    // 帳號格式預檢
                    if (!trimmedValue.matches("^[a-zA-Z0-9_.-]+$")) {
                        yield false;
                    }
                    yield !employeeRepository.existsByAccount(trimmedValue);
                }
                case "email" -> {
                    // Email 格式預檢
                    if (!trimmedValue.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                        yield false;
                    }
                    yield !employeeRepository.existsByEmail(trimmedValue.toLowerCase());
                }
                case "nationalId" -> {
                    // 身分證格式預檢
                    if (!trimmedValue.matches("^[A-Z][1-2]\\d{8}$")) {
                        yield false;
                    }
                    yield !employeeRepository.existsByNationalId(trimmedValue.toUpperCase());
                }
                default -> {
                    log.warn("不支援的欄位驗證類型: {}", field);
                    yield true;
                }
            };
            
            log.debug("欄位可用性檢查 - field: {}, value: {}, available: {}", field, trimmedValue, isAvailable);
            return isAvailable;
            
        } catch (Exception e) {
            log.error("欄位驗證過程中發生錯誤 - field: {}, value: {}", field, trimmedValue, e);
            return false;
        }
    }
    
    /**
     * 【新增方法實作】- 員工登入驗證
     * 驗證員工帳號密碼（明文比對），並返回員工資訊
     */
    @Override
    @Transactional(readOnly = true)
    public EmployeeDTO authenticateEmployee(String account, String password) {
        // 輸入參數驗證
        if (!StringUtils.hasText(account) || !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("帳號和密碼不可為空");
        }
        
        // 根據帳號查找員工
        EmployeeEntity employee = employeeRepository.findByAccount(account.trim())
                .orElseThrow(() -> new ResourceNotFoundException("帳號或密碼錯誤"));
        
        // 檢查帳號狀態
        if (employee.getStatus() != com.eatfast.common.enums.AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("此帳號已被停用，請聯絡管理員");
        }
        
        // 明文密碼比對
        if (!password.equals(employee.getPassword())) {
            throw new IllegalArgumentException("帳號或密碼錯誤");
        }
        
        log.info("員工登入成功 - 帳號: {}, 姓名: {}", employee.getAccount(), employee.getUsername());
        return employeeMapper.toDto(employee);
    }

    /**
     * 【新增方法實作】- 獲取所有啟用狀態的員工列表
     * 用於登入頁面的管理員小幫手功能
     */
    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDTO> findAllActiveEmployees() {
        return employeeRepository.findAll().stream()
                .filter(employee -> employee.getStatus() == com.eatfast.common.enums.AccountStatus.ACTIVE)
                .map(employeeMapper::toDto)
                .collect(Collectors.toList());
    }
    
    // --- 私有輔助方法 ---

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

        // 【修改】密碼更新邏輯 - 使用明文密碼
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            if (!request.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")) {
                throw new IllegalArgumentException("密碼需至少8個字元，且包含至少一個字母和一個數字");
            }
            // 【修改】直接設置明文密碼，不進行加密
            employeeToUpdate.setPassword(request.getPassword());
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
    
    private String handlePhotoUpload(MultipartFile photo, Long employeeId) {
        String contentType = photo.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new IllegalArgumentException("只支援 JPG 或 PNG 格式的圖片");
        }
        if (photo.getSize() > 5 * 1024 * 1024) {
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

    private void validatePermissionAssignment(EmployeeEntity employee, PermissionEntity permissionToGrant) {
        Set<PermissionDto> allowedPermissions = permissionService.findPermissionsByRole(employee.getRole());
        boolean isAllowed = allowedPermissions.stream()
                .anyMatch(dto -> dto.getPermissionId().equals(permissionToGrant.getPermissionId()));
        if (!isAllowed) {
            throw new IllegalArgumentException(
                "權限指派無效：權限 '" + permissionToGrant.getDescription() + 
                "' 不適用於角色 '" + employee.getRole().name() + "'。"
            );
        }
    }
}