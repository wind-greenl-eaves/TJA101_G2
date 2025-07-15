package com.eatfast.employee.service;

import com.eatfast.common.exception.DuplicateResourceException;
import com.eatfast.common.exception.ResourceNotFoundException;
import com.eatfast.common.service.FileService;
import com.eatfast.employee.dto.CreateEmployeeRequest;
import com.eatfast.employee.dto.EmployeeApplicationDTO;
import com.eatfast.employee.dto.ReviewApplicationRequest;
import com.eatfast.employee.mapper.EmployeeMapper;
import com.eatfast.employee.model.EmployeeApplicationEntity;
import com.eatfast.employee.model.EmployeeApplicationEntity.ApplicationStatus;
import com.eatfast.employee.model.EmployeeEntity;
import com.eatfast.employee.repository.EmployeeApplicationRepository;
import com.eatfast.employee.repository.EmployeeRepository;
import com.eatfast.store.model.StoreEntity;
import com.eatfast.store.repository.StoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeApplicationServiceImpl implements EmployeeApplicationService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeApplicationServiceImpl.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final EmployeeApplicationRepository applicationRepository;
    private final EmployeeRepository employeeRepository;
    private final StoreRepository storeRepository;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;
    private final EmployeeService employeeService;

    @Autowired
    public EmployeeApplicationServiceImpl(
            EmployeeApplicationRepository applicationRepository,
            EmployeeRepository employeeRepository,
            StoreRepository storeRepository,
            EmployeeMapper employeeMapper,
            PasswordEncoder passwordEncoder,
            FileService fileService,
            EmployeeService employeeService) {
        this.applicationRepository = applicationRepository;
        this.employeeRepository = employeeRepository;
        this.storeRepository = storeRepository;
        this.employeeMapper = employeeMapper;
        this.passwordEncoder = passwordEncoder;
        this.fileService = fileService;
        this.employeeService = employeeService;
    }

    @Override
    @Transactional
    public EmployeeApplicationDTO submitApplication(CreateEmployeeRequest request, Long applicantId) {
        log.info("門市經理提交新增員工申請: 申請人ID={}, 員工姓名={}", applicantId, request.getUsername());

        // 驗證申請人是否存在且為門市經理
        EmployeeEntity applicant = employeeRepository.findById(applicantId)
                .orElseThrow(() -> new ResourceNotFoundException("申請人不存在"));

        if (applicant.getRole() != com.eatfast.common.enums.EmployeeRole.MANAGER) {
            throw new IllegalArgumentException("只有門市經理可以提交新增員工申請");
        }

        // 驗證門市存在
        StoreEntity store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("找不到指定的門市"));

        // 檢查門市經理是否只能為自己的門市申請
        if (!applicant.getStore().getStoreId().equals(request.getStoreId())) {
            throw new IllegalArgumentException("門市經理只能為自己的門市申請新增員工");
        }

        // 檢查唯一性約束（包含已存在員工和待審核申請）
        validateUniquenessForApplication(request.getAccount(), request.getEmail(), request.getNationalId());

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

        // 創建申請實體
        EmployeeApplicationEntity application = new EmployeeApplicationEntity(
                applicantId,
                applicant.getUsername(),
                request.getUsername(),
                request.getAccount(),
                request.getEmail(),
                request.getPhone(),
                request.getNationalId(),
                passwordEncoder.encode(request.getPassword()),
                request.getRole(),
                request.getGender(),
                request.getStoreId(),
                store.getStoreName(),
                photoUrl
        );

        EmployeeApplicationEntity savedApplication = applicationRepository.save(application);
        log.info("員工申請已提交: applicationId={}", savedApplication.getApplicationId());

        return convertToDTO(savedApplication);
    }

    @Override
    @Transactional
    public EmployeeApplicationDTO reviewApplication(ReviewApplicationRequest request, Long reviewerId) {
        log.info("總部管理員審核員工申請: applicationId={}, status={}, reviewerId={}", 
                request.getApplicationId(), request.getStatus(), reviewerId);

        // 查詢申請
        EmployeeApplicationEntity application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("找不到指定的申請"));

        // 檢查申請狀態
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new IllegalStateException("該申請已被審核，無法重複審核");
        }

        // 查詢審核者
        EmployeeEntity reviewer = employeeRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("審核者不存在"));

        if (reviewer.getRole() != com.eatfast.common.enums.EmployeeRole.HEADQUARTERS_ADMIN) {
            throw new IllegalArgumentException("只有總部管理員可以審核申請");
        }

        // 更新申請狀態
        application.setStatus(request.getStatus());
        application.setReviewerId(reviewerId);
        application.setReviewerName(reviewer.getUsername());
        application.setReviewComment(request.getReviewComment());
        application.setReviewedAt(LocalDateTime.now());

        // 如果核准，則創建員工
        if (request.getStatus() == ApplicationStatus.APPROVED) {
            createEmployeeFromApplication(application);
        }

        EmployeeApplicationEntity savedApplication = applicationRepository.save(application);
        log.info("申請審核完成: applicationId={}, status={}", savedApplication.getApplicationId(), savedApplication.getStatus());

        return convertToDTO(savedApplication);
    }

    @Override
    public List<EmployeeApplicationDTO> getAllPendingApplications() {
        List<EmployeeApplicationEntity> applications = applicationRepository.findByStatusOrderByCreatedAtDesc(ApplicationStatus.PENDING);
        return applications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeApplicationDTO> getApplicationsByApplicant(Long applicantId) {
        List<EmployeeApplicationEntity> applications = applicationRepository.findByApplicantIdOrderByCreatedAtDesc(applicantId);
        return applications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeApplicationDTO> getApplicationsByStore(Long storeId) {
        List<EmployeeApplicationEntity> applications = applicationRepository.findByStoreIdOrderByCreatedAtDesc(storeId);
        return applications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeApplicationDTO> getApplicationsByStatus(ApplicationStatus status) {
        List<EmployeeApplicationEntity> applications = applicationRepository.findByStatusOrderByCreatedAtDesc(status);
        return applications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeApplicationDTO getApplicationById(Long applicationId) {
        EmployeeApplicationEntity application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到指定的申請"));
        return convertToDTO(application);
    }

    @Override
    public List<EmployeeApplicationDTO> getAllApplications() {
        List<EmployeeApplicationEntity> applications = applicationRepository.findAllOrderByCreatedAtDesc();
        return applications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isFieldAvailableInApplications(String field, String value) {
        ApplicationStatus pendingStatus = ApplicationStatus.PENDING;
        
        return switch (field) {
            case "account" -> !applicationRepository.existsByAccountAndStatus(value, pendingStatus);
            case "email" -> !applicationRepository.existsByEmailAndStatus(value, pendingStatus);
            case "nationalId" -> !applicationRepository.existsByNationalIdAndStatus(value, pendingStatus);
            default -> true;
        };
    }

    private void validateUniquenessForApplication(String account, String email, String nationalId) {
        // 檢查員工表中的唯一性
        if (employeeRepository.existsByAccount(account)) {
            throw new DuplicateResourceException("帳號已被使用");
        }
        if (employeeRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email已被使用");
        }
        if (employeeRepository.existsByNationalId(nationalId)) {
            throw new DuplicateResourceException("身分證字號已被使用");
        }

        // 檢查申請表中的唯一性（待審核狀態）
        if (applicationRepository.existsByAccountAndStatus(account, ApplicationStatus.PENDING)) {
            throw new DuplicateResourceException("該帳號已有待審核的申請");
        }
        if (applicationRepository.existsByEmailAndStatus(email, ApplicationStatus.PENDING)) {
            throw new DuplicateResourceException("該Email已有待審核的申請");
        }
        if (applicationRepository.existsByNationalIdAndStatus(nationalId, ApplicationStatus.PENDING)) {
            throw new DuplicateResourceException("該身分證字號已有待審核的申請");
        }
    }

    @Transactional
    private void createEmployeeFromApplication(EmployeeApplicationEntity application) {
        log.info("從申請創建員工: applicationId={}, username={}", 
                application.getApplicationId(), application.getUsername());

        try {
            // 再次檢查唯一性（防止並發問題）
            if (employeeRepository.existsByAccount(application.getAccount()) ||
                employeeRepository.existsByEmail(application.getEmail()) ||
                employeeRepository.existsByNationalId(application.getNationalId())) {
                throw new DuplicateResourceException("員工資料已存在，無法創建");
            }

            // 查詢門市
            StoreEntity store = storeRepository.findById(application.getStoreId())
                    .orElseThrow(() -> new ResourceNotFoundException("找不到指定的門市"));

            // 創建員工實體
            EmployeeEntity employee = new EmployeeEntity();
            employee.setUsername(application.getUsername());
            employee.setAccount(application.getAccount());
            employee.setEmail(application.getEmail());
            employee.setPhone(application.getPhone());
            employee.setNationalId(application.getNationalId());
            employee.setPassword(application.getPassword()); // 已加密
            employee.setRole(application.getRole());
            employee.setGender(application.getGender());
            employee.setStore(store);
            employee.setPhotoUrl(application.getPhotoUrl());
            employee.setStatus(com.eatfast.common.enums.AccountStatus.ACTIVE);
            // 移除 setCreatedAt 調用，因為使用 @CreationTimestamp 自動設定

            employeeRepository.save(employee);
            log.info("員工創建成功: account={}, username={}", employee.getAccount(), employee.getUsername());

        } catch (Exception e) {
            log.error("從申請創建員工失敗: applicationId={}, error={}", 
                    application.getApplicationId(), e.getMessage());
            throw new RuntimeException("創建員工失敗: " + e.getMessage(), e);
        }
    }

    private EmployeeApplicationDTO convertToDTO(EmployeeApplicationEntity entity) {
        EmployeeApplicationDTO dto = new EmployeeApplicationDTO();
        dto.setApplicationId(entity.getApplicationId());
        dto.setApplicantId(entity.getApplicantId());
        dto.setApplicantName(entity.getApplicantName());
        dto.setUsername(entity.getUsername());
        dto.setAccount(entity.getAccount());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());
        dto.setNationalId(entity.getNationalId());
        dto.setRole(entity.getRole());
        dto.setGender(entity.getGender());
        dto.setStoreId(entity.getStoreId());
        dto.setStoreName(entity.getStoreName());
        dto.setPhotoUrl(entity.getPhotoUrl());
        dto.setStatus(entity.getStatus());
        dto.setStatusDisplayName(entity.getStatus().getDisplayName());
        dto.setReviewerId(entity.getReviewerId());
        dto.setReviewerName(entity.getReviewerName());
        dto.setReviewComment(entity.getReviewComment());
        dto.setReviewedAt(entity.getReviewedAt());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        
        // 格式化時間顯示
        if (entity.getCreatedAt() != null) {
            dto.setCreatedAtFormatted(entity.getCreatedAt().format(FORMATTER));
        }
        if (entity.getReviewedAt() != null) {
            dto.setReviewedAtFormatted(entity.getReviewedAt().format(FORMATTER));
        }
        
        return dto;
    }

    @Override
    @Transactional
    public void clearProcessedApplications() {
        log.info("開始清空已審核完成的申請列表");
        
        try {
            // 查詢所有已審核完成的申請（APPROVED 和 REJECTED 狀態）
            List<EmployeeApplicationEntity> processedApplications = applicationRepository.findByStatusIn(
                    Arrays.asList(ApplicationStatus.APPROVED, ApplicationStatus.REJECTED));
            
            if (processedApplications.isEmpty()) {
                log.info("沒有已審核完成的申請需要清空");
                return;
            }
            
            // 刪除已審核完成的申請
            applicationRepository.deleteAll(processedApplications);
            
            log.info("成功清空已審核完成的申請列表，共刪除 {} 筆申請", processedApplications.size());
            
        } catch (Exception e) {
            log.error("清空已審核完成的申請列表失敗: {}", e.getMessage());
            throw new RuntimeException("清空已審核完成的申請列表失敗: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    public void clearAllApplications() {
        log.info("開始清空所有申請列表");
        
        try {
            // 查詢所有申請
            List<EmployeeApplicationEntity> allApplications = applicationRepository.findAll();
            
            if (allApplications.isEmpty()) {
                log.info("沒有申請需要清空");
                return;
            }
            
            // 刪除所有申請
            applicationRepository.deleteAll();
            
            log.info("成功清空所有申請列表，共刪除 {} 筆申請", allApplications.size());
            
        } catch (Exception e) {
            log.error("清空所有申請列表失敗: {}", e.getMessage());
            throw new RuntimeException("清空所有申請列表失敗: " + e.getMessage(), e);
        }
    }
}