// =======================================================================================
// 檔案: EmployeeServiceImpl.java (已復原)
// 說明: 員工服務層介面的完整實作。已移除照片處理邏輯。
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    
    private static final Logger log = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final EmployeeRepository employeeRepository;
    private final StoreRepository storeRepository;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;
    private final PermissionRepository permissionRepository;
    private final EmployeePermissionRepository employeePermissionRepository;

    @Value("${app.upload.employee-photos}")
    private String uploadPath;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository, StoreRepository storeRepository,
                               EmployeeMapper employeeMapper, PasswordEncoder passwordEncoder,
                               PermissionRepository permissionRepository,
                               EmployeePermissionRepository employeePermissionRepository) {
        this.employeeRepository = employeeRepository;
        this.storeRepository = storeRepository;
        this.employeeMapper = employeeMapper;
        this.passwordEncoder = passwordEncoder;
        this.permissionRepository = permissionRepository;
        this.employeePermissionRepository = employeePermissionRepository;
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

    /**
     * 處理員工照片上傳
     * @param photo MultipartFile 上傳的照片文件
     * @param employeeId 員工ID
     * @return 照片的訪問URL
     */
    private String handlePhotoUpload(MultipartFile photo, Long employeeId) {
        // 驗證文件類型
        String contentType = photo.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new IllegalArgumentException("只支援 JPG 或 PNG 格式的圖片");
        }

        // 驗證文件大小
        if (photo.getSize() > 5 * 1024 * 1024) { // 5MB
            throw new IllegalArgumentException("圖片大小不能超過 5MB");
        }

        try {
            // 創建上傳目錄
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成唯一的文件名
            String fileExtension = StringUtils.getFilenameExtension(photo.getOriginalFilename());
            String newFileName = "employee_" + employeeId + "_" + System.currentTimeMillis() + "." + fileExtension;
            Path filePath = uploadDir.resolve(newFileName);

            // 保存文件
            Files.copy(photo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 刪除舊照片
            deleteOldPhoto(employeeId);

            // 返回照片URL（修改為正確的訪問路徑）
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
                    // 從 URL 中獲取文件名
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
    
    // 【已移除】getEmployeePhotoById 方法
    
    @Override
    @Transactional
    public void grantPermissionToEmployee(Long employeeId, Long permissionId) {
        if (employeePermissionRepository.existsByEmployeeEmployeeIdAndPermissionPermissionId(employeeId, permissionId)) {
            throw new DuplicateResourceException("員工 (ID: " + employeeId + ") 已擁有權限 (ID: " + permissionId + ")");
        }
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到員工 (ID: " + employeeId + ")"));
        PermissionEntity permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到權限 (ID: " + permissionId + ")"));
        EmployeePermissionEntity newAssociation = new EmployeePermissionEntity();
        newAssociation.setEmployee(employee);
        newAssociation.setPermission(permission);
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
        // 基本資料驗證
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            if (request.getUsername().trim().length() < 2) {
                throw new IllegalArgumentException("員工姓名至少需要2個字");
            }
            employeeToUpdate.setUsername(request.getUsername().trim());
        }

        // 電話號碼驗證 - 更新為更靈活的格式
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            String phone = request.getPhone().trim();
            // 移除所有非數字字符
            String cleanPhone = phone.replaceAll("[^0-9]", "");
            
            // 驗證清理後的號碼格式
            if (cleanPhone.matches("^09\\d{8}$") || // 手機格式
                cleanPhone.matches("^0[2-8]\\d{7,8}$")) { // 市話格式
                employeeToUpdate.setPhone(phone); // 保存原始格式，包含橫線
            } else {
                throw new IllegalArgumentException("請輸入有效的台灣手機或市話號碼");
            }
        }

        // 角色和狀態更新
        if (request.getRole() != null) {
            employeeToUpdate.setRole(request.getRole());
        }
        if (request.getStatus() != null) {
            employeeToUpdate.setStatus(request.getStatus());
        }

        // 密碼更新驗證（僅在有輸入新密碼時才進行驗證和更新）
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            // 只有當密碼不為空時才進行驗證
            if (!request.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")) {
                throw new IllegalArgumentException("密碼需至少8個字元，且包含至少一個字母和一個數字");
            }
            employeeToUpdate.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        // 如果密碼為空，則保持原密碼不變

        // 電子郵件更新驗證
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

        // 門市關聯更新
        if (request.getStoreId() != null && 
            !request.getStoreId().equals(employeeToUpdate.getStore().getStoreId())) {
            StoreEntity store = storeRepository.findById(request.getStoreId())
                    .orElseThrow(() -> new ResourceNotFoundException("找不到 ID 為 " + request.getStoreId() + " 的門市"));
            employeeToUpdate.setStore(store);
        }
    }
}