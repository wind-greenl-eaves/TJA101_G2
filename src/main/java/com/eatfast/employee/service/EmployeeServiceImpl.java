// =======================================================================================
// 檔案: EmployeeServiceImpl.java (已修正 - 移除密碼加密)
// 說明: 移除密碼加密功能，改為使用明文密碼存儲和處理
// =======================================================================================
package com.eatfast.employee.service;

import com.eatfast.common.exception.DuplicateResourceException;
import com.eatfast.common.exception.ResourceNotFoundException;
import com.eatfast.common.service.FileService;
import com.eatfast.common.service.MailService;
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
import java.util.Optional;
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
    private MailService mailService;

    @Autowired
    private PasswordEncoder passwordEncoder; // 新增密碼加密器

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
        // 【修正】統一使用BCrypt加密密碼
        newEmployee.setPassword(passwordEncoder.encode(request.getPassword()));
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
     * 【修正方法實作】- 員工登入驗證 + 自動密碼升級
     * 驗證員工帳號密碼，並自動將明文密碼升級為BCrypt加密
     */
    @Override
    @Transactional // 【關鍵修正】移除 readOnly = true，允許寫入操作
    public EmployeeDTO authenticateEmployee(String account, String password) {
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
        
        // 檢查是否因登入失敗次數過多而被鎖定
        if (employee.getLoginFailureCount() >= 8) {
            log.warn("帳號因登入失敗次數過多已被鎖定 - 帳號: {}, 失敗次數: {}", 
                employee.getAccount(), employee.getLoginFailureCount());
            throw new IllegalArgumentException("帳號因連續登入失敗次數過多已被停用，請聯絡管理員");
        }
        
        boolean passwordMatches = false;
        boolean needsPasswordUpgrade = false;
        
        // 【修正】智慧密碼驗證 - 判斷是否為明文密碼
        if (employee.getPassword().startsWith("$2a$") || 
            employee.getPassword().startsWith("$2b$") || 
            employee.getPassword().startsWith("$2y$")) {
            // BCrypt格式密碼 - 使用加密比對
            log.debug("偵測到BCrypt格式密碼，使用加密比對");
            passwordMatches = passwordEncoder.matches(password, employee.getPassword());
        } else {
            // 明文密碼 - 直接比對並標記需要升級
            log.info("偵測到明文密碼，使用直接比對並準備升級 - 帳號: {}", employee.getAccount());
            passwordMatches = password.equals(employee.getPassword());
            needsPasswordUpgrade = passwordMatches; // 只有密碼正確時才升級
        }
        
        // 如果密碼驗證失敗，處理失敗次數
        if (!passwordMatches) {
            int newFailureCount = employee.getLoginFailureCount() + 1;
            employee.setLoginFailureCount(newFailureCount);
            employee.setLastFailureTime(java.time.LocalDateTime.now());
            
            log.warn("密碼驗證失敗 - 帳號: {}, 失敗次數: {}", employee.getAccount(), newFailureCount);
            
            // 檢查是否達到停用條件 (8次)
            if (newFailureCount >= 8) {
                employee.setStatus(com.eatfast.common.enums.AccountStatus.INACTIVE);
                employee.setAccountLockedTime(java.time.LocalDateTime.now());
                employeeRepository.save(employee);
                
                log.error("帳號因連續登入失敗達到8次已自動停用 - 帳號: {}", employee.getAccount());
                throw new IllegalArgumentException("帳號因連續登入失敗次數過多已被停用，請聯絡管理員");
            }
            // 修正：在第6次失敗時開始警告 (剩餘2次機會)
            else if (newFailureCount >= 6) {
                int remainingAttempts = 8 - newFailureCount;
                employeeRepository.save(employee);
                
                log.warn("帳號登入失敗警告 - 帳號: {}, 剩餘嘗試次數: {}", employee.getAccount(), remainingAttempts);
                throw new IllegalArgumentException(
                    String.format("帳號或密碼錯誤！警告：還有 %d 次登入機會，超過將停用帳號", remainingAttempts));
            }
            // 一般失敗情況
            else {
                employeeRepository.save(employee);
                throw new IllegalArgumentException("帳號或密碼錯誤");
            }
        }
        
        // 登入成功 - 重置失敗次數
        if (employee.getLoginFailureCount() > 0) {
            log.info("登入成功，重置失敗次數 - 帳號: {}, 原失敗次數: {}", 
                employee.getAccount(), employee.getLoginFailureCount());
            employee.setLoginFailureCount(0);
            employee.setLastFailureTime(null);
            employee.setAccountLockedTime(null);
        }
        
        // 【關鍵修正】如果需要升級密碼，在同一個事務中直接升級
        if (needsPasswordUpgrade) {
            try {
                log.info("開始自動升級明文密碼為BCrypt - 帳號: {}", employee.getAccount());
                
                // 【修正】直接在當前事務中進行密碼升級
                String encryptedPassword = passwordEncoder.encode(password);
                employee.setPassword(encryptedPassword);
                
                log.info("密碼升級成功 - 帳號: {}, 原密碼: {}, 新密碼前綴: {}", 
                    employee.getAccount(), password, encryptedPassword.substring(0, 10));
                    
            } catch (Exception e) {
                log.error("密碼升級失敗，但不影響登入 - 帳號: {}, 錯誤: {}", 
                    employee.getAccount(), e.getMessage());
                // 密碼升級失敗不影響登入流程，但要記錄錯誤
                e.printStackTrace();
            }
        }
        
        // 保存所有變更（重置失敗次數和可能的密碼升級）
        employeeRepository.save(employee);
        
        log.info("員工登入成功 - 帳號: {}, 姓名: {}", employee.getAccount(), employee.getUsername());
        return employeeMapper.toDto(employee);
    }
    
    /**
     * 【新增私有方法】在獨立事務中執行密碼升級
     * 這個方法使用新的事務來避免只讀事務的限制
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    private void upgradePasswordToBCrypt(Long employeeId, String plainPassword) {
        try {
            // 重新查詢員工實體以確保在新事務中操作
            EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("員工不存在"));
            
            // 生成BCrypt加密密碼
            String encryptedPassword = passwordEncoder.encode(plainPassword);
            
            // 更新密碼
            employee.setPassword(encryptedPassword);
            
            // 保存到資料庫
            employeeRepository.save(employee);
            
            log.info("密碼升級完成 - 員工ID: {}, 原密碼長度: {}, 新密碼長度: {}", 
                employeeId, plainPassword.length(), encryptedPassword.length());
                
        } catch (Exception e) {
            log.error("密碼升級過程中發生錯誤 - 員工ID: {}", employeeId, e);
            throw e; // 重新拋出異常以便上層處理
        }
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

    /**
     * 【新增方法實作】- 獲取所有已停權員工列表
     * 用於登入頁面的管理員小幫手功能
     */
    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDTO> findAllInactiveEmployees() {
        return employeeRepository.findAll().stream()
                .filter(employee -> employee.getStatus() == com.eatfast.common.enums.AccountStatus.INACTIVE)
                .map(employeeMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 【修改方法實作】- 處理忘記密碼請求並發送郵件
     * 根據帳號或郵件查找員工，生成臨時密碼，並發送郵件通知
     */
    @Override
    @Transactional
    public String processForgotPassword(String accountOrEmail) {
        // 輸入參數驗證
        if (!StringUtils.hasText(accountOrEmail)) {
            throw new IllegalArgumentException("帳號或電子郵件不可為空");
        }
        
        String input = accountOrEmail.trim();
        EmployeeEntity employee = null;
        
        // 嘗試按帳號查找
        Optional<EmployeeEntity> employeeByAccount = employeeRepository.findByAccount(input);
        if (employeeByAccount.isPresent()) {
            employee = employeeByAccount.get();
        } else {
            // 如果按帳號找不到，嘗試按郵件查找
            Optional<EmployeeEntity> employeeByEmail = employeeRepository.findByEmail(input.toLowerCase());
            if (employeeByEmail.isPresent()) {
                employee = employeeByEmail.get();
            }
        }
        
        // 如果找不到員工
        if (employee == null) {
            log.warn("忘記密碼請求 - 查無此帳號或郵件: {}", input);
            return "查無此帳號或電子郵件，請確認輸入正確或聯絡系統管理員。";
        }
        
        // 檢查帳號狀態
        if (employee.getStatus() != com.eatfast.common.enums.AccountStatus.ACTIVE) {
            log.warn("忘記密碼請求 - 帳號已停用: {}", employee.getAccount());
            return "此帳號已被停用，請聯絡管理員。";
        }
        
        // 生成新的臨時密碼（8位數英數混合）
        String temporaryPassword = generateTemporaryPassword();
        
        // 【修正】更新員工密碼 - 使用加密存儲確保一致性
        employee.setPassword(passwordEncoder.encode(temporaryPassword));
        employeeRepository.save(employee);
        
        // 【新增】發送郵件通知
        try {
            // 測試用郵箱地址
            String testEmail = "young19960127@gmail.com";
            
            boolean emailSent = mailService.sendForgotPasswordEmail(
                testEmail,  // 改為發送到測試郵箱
                employee.getUsername(),
                employee.getAccount(),
                temporaryPassword
            );
            
            if (emailSent) {
                log.info("忘記密碼處理成功且郵件發送成功 - 帳號: {}, 姓名: {}, 原郵件: {}, 實際發送至: {}", 
                    employee.getAccount(), employee.getUsername(), employee.getEmail(), testEmail);
                
                return "密碼重設成功！新的臨時密碼已發送至測試郵箱 " + testEmail + "\n" +
                    "請檢查該郵箱（包含垃圾郵件資料夾）並使用新密碼登入。\n" +
                    "您的臨時密碼是：" + temporaryPassword;
            } else {
                log.warn("忘記密碼處理成功但郵件發送失敗 - 帳號: {}, 測試郵件: {}", 
                    employee.getAccount(), testEmail);
                
                return "密碼重設成功！但郵件發送失敗。\n" +
                    "您的新臨時密碼是：" + temporaryPassword + "\n" +
                    "請妥善保存並儘快登入後修改密碼。";
            }
        } catch (Exception e) {
            log.error("忘記密碼郵件發送異常 - 帳號: {}, 錯誤: {}", employee.getAccount(), e.getMessage(), e);
            
            return "密碼重設成功！但郵件發送遇到問題。\n" +
                "您的新臨時密碼是：" + temporaryPassword + "\n" +
                "請妥善保存並儘快登入後修改密碼。";
        }
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

        // 【修正】密碼更新統一使用BCrypt加密
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

    /**
     * 生成臨時密碼
     * 格式：8位數英數混合（至少包含一個字母和一個數字）
     */
    private String generateTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        
        // 確保至少有一個大寫字母
        password.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt((int) (Math.random() * 26)));
        // 確保至少有一個小寫字母
        password.append("abcdefghijklmnopqrstuvwxyz".charAt((int) (Math.random() * 26)));
        // 確保至少有一個數字
        password.append("0123456789".charAt((int) (Math.random() * 10)));
        
        // 填滿剩餘的5位
        for (int i = 3; i < 8; i++) {
            password.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        
        // 隨機打亂字符順序
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = (int) (Math.random() * (i + 1));
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }
        
        return new String(passwordArray);
    }
    
    /**
     * 遮罩電子郵件地址以保護隱私
     * 例如: john.doe@example.com -> j***@example.com
     */
    private String maskEmail(String email) {
        if (!StringUtils.hasText(email) || !email.contains("@")) {
            return email;
        }
        
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];
        
        if (localPart.length() <= 1) {
            return email;
        } else if (localPart.length() <= 3) {
            return localPart.charAt(0) + "***@" + domain;
        } else {
            return localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1) + "@" + domain;
        }
    }

    /**
     * 【新增方法實作】- 更新員工照片
     * 處理員工照片的上傳和更新
     */
    @Override
    @Transactional
    public EmployeeDTO updateEmployeePhoto(Long employeeId, MultipartFile photo) throws IOException {
        // 查找員工
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 ID 為 " + employeeId + " 的員工"));
        
        // 驗證照片檔案
        if (photo == null || photo.isEmpty()) {
            throw new IllegalArgumentException("照片檔案不能為空");
        }
        
        // 檢查檔案格式
        String contentType = photo.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new IllegalArgumentException("只支援 JPG 或 PNG 格式的圖片");
        }
        
        // 檢查檔案大小 (5MB)
        if (photo.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("圖片大小不能超過 5MB");
        }
        
        try {
            // 刪除舊照片（如果存在）
            if (employee.getPhotoUrl() != null) {
                String oldFileName = employee.getPhotoUrl().substring(employee.getPhotoUrl().lastIndexOf("/") + 1);
                fileService.deleteEmployeePhoto(oldFileName);
            }
            
            // 上傳新照片
            String fileName = fileService.saveEmployeePhoto(photo);
            String photoUrl = "/employee-photos/" + fileName;
            
            // 更新員工照片URL
            employee.setPhotoUrl(photoUrl);
            EmployeeEntity updatedEmployee = employeeRepository.save(employee);
            
            log.info("員工照片更新成功 - 員工ID: {}, 檔案名稱: {}", employeeId, fileName);
            return employeeMapper.toDto(updatedEmployee);
            
        } catch (IOException e) {
            log.error("員工照片上傳失敗 - 員工ID: {}", employeeId, e);
            throw new IOException("照片上傳失敗: " + e.getMessage(), e);
        }
    }

    /**
     * 【新增方法實作】- 重置員工登入失敗次數
     * 管理員可以使用此方法重置員工的登入失敗次數，解鎖被鎖定的帳號
     */
    @Override
    @Transactional
    public void resetLoginFailureCount(Long employeeId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 ID 為 " + employeeId + " 的員工"));
        
        // 記錄重置前的狀態
        int originalCount = employee.getLoginFailureCount();
        com.eatfast.common.enums.AccountStatus originalStatus = employee.getStatus();
        
        // 重置失敗次數
        employee.setLoginFailureCount(0);
        employee.setLastFailureTime(null);
        employee.setAccountLockedTime(null);
        
        // 如果帳號因為失敗次數過多而被停用，則重新啟用
        if (employee.getStatus() == com.eatfast.common.enums.AccountStatus.INACTIVE && originalCount >= 8) {
            employee.setStatus(com.eatfast.common.enums.AccountStatus.ACTIVE);
            log.info("重置登入失敗次數並重新啟用帳號 - 員工ID: {}, 帳號: {}, 原失敗次數: {}", 
                employeeId, employee.getAccount(), originalCount);
        } else {
            log.info("重置登入失敗次數 - 員工ID: {}, 帳號: {}, 原失敗次數: {}", 
                employeeId, employee.getAccount(), originalCount);
        }
        
        employeeRepository.save(employee);
    }

    /**
     * 【新增方法實作】- 檢查員工帳號登入狀態
     * 返回員工的登入失敗次數和帳號狀態資訊
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getEmployeeLoginStatus(Long employeeId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 ID 為 " + employeeId + " 的員工"));
        
        Map<String, Object> status = new java.util.HashMap<>();
        status.put("employeeId", employee.getEmployeeId());
        status.put("account", employee.getAccount());
        status.put("username", employee.getUsername());
        status.put("status", employee.getStatus());
        status.put("loginFailureCount", employee.getLoginFailureCount());
        status.put("lastFailureTime", employee.getLastFailureTime());
        status.put("accountLockedTime", employee.getAccountLockedTime());
        status.put("isLocked", employee.getLoginFailureCount() >= 8);
        status.put("remainingAttempts", Math.max(0, 8 - employee.getLoginFailureCount()));
        
        return status;
    }
}
