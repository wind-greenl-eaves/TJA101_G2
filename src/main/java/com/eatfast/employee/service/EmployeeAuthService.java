package com.eatfast.employee.service;

import com.eatfast.employee.model.EmployeeEntity;
import com.eatfast.employee.dto.EmployeeDTO;
import com.eatfast.employee.repository.EmployeeRepository;
import com.eatfast.common.enums.AccountStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 員工認證服務層
 * 處理員工登入認證、密碼驗證、登入失敗次數追蹤等功能
 */
@Service
@Transactional
public class EmployeeAuthService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeAuthService.class);
    private static final int MAX_LOGIN_ATTEMPTS = 8; // 最大登入失敗次數

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public EmployeeAuthService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 根據帳號查找員工
     */
    @Transactional(readOnly = true)
    public EmployeeEntity findEmployeeByAccount(String account) {
        logger.debug("查找員工帳號: {}", account);
        
        Optional<EmployeeEntity> employeeOpt = employeeRepository.findByAccount(account);
        if (employeeOpt.isPresent()) {
            EmployeeEntity employee = employeeOpt.get();
            logger.debug("找到員工: ID={}, 姓名={}, 狀態={}, 失敗次數={}", 
                        employee.getEmployeeId(), employee.getUsername(), 
                        employee.getStatus(), employee.getLoginFailureCount());
            return employee;
        } else {
            logger.debug("未找到帳號: {}", account);
            return null;
        }
    }

    /**
     * 驗證密碼
     * 支援明文密碼和BCrypt加密密碼的混合驗證
     */
    public boolean validatePassword(String inputPassword, String storedPassword) {
        logger.debug("開始密碼驗證，存儲密碼長度: {}", storedPassword != null ? storedPassword.length() : 0);
        
        if (inputPassword == null || storedPassword == null) {
            logger.warn("密碼驗證失敗：輸入密碼或存儲密碼為空");
            return false;
        }
        
        try {
            // 檢查是否為BCrypt格式
            if (storedPassword.startsWith("$2a$") || 
                storedPassword.startsWith("$2b$") || 
                storedPassword.startsWith("$2y$")) {
                // BCrypt格式密碼驗證
                logger.debug("使用BCrypt格式驗證");
                return passwordEncoder.matches(inputPassword, storedPassword);
            } else {
                // 明文密碼驗證（向下相容）
                logger.debug("使用明文密碼驗證");
                return inputPassword.equals(storedPassword);
            }
        } catch (Exception e) {
            logger.error("密碼驗證過程中發生錯誤: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 增加登入失敗次數
     * @param employeeId 員工ID
     * @return 新的失敗次數
     */
    public int incrementLoginFailureCount(Long employeeId) {
        logger.info("增加員工 {} 的登入失敗次數", employeeId);
        
        try {
            EmployeeEntity employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("找不到員工 ID: " + employeeId));
            
            int currentCount = employee.getLoginFailureCount() != null ? employee.getLoginFailureCount() : 0;
            int newCount = currentCount + 1;
            
            employee.setLoginFailureCount(newCount);
            employee.setLastLoginFailureAt(LocalDateTime.now());
            
            employeeRepository.save(employee);
            
            logger.info("員工 {} 登入失敗次數已更新: {} -> {}", employeeId, currentCount, newCount);
            return newCount;
            
        } catch (Exception e) {
            logger.error("更新登入失敗次數時發生錯誤: {}", e.getMessage(), e);
            throw new RuntimeException("更新登入失敗次數失敗", e);
        }
    }

    /**
     * 重置登入失敗次數
     * 登入成功時調用
     */
    public void resetLoginFailureCount(Long employeeId) {
        logger.info("重置員工 {} 的登入失敗次數", employeeId);
        
        try {
            EmployeeEntity employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("找不到員工 ID: " + employeeId));
            
            int oldCount = employee.getLoginFailureCount() != null ? employee.getLoginFailureCount() : 0;
            
            if (oldCount > 0) {
                employee.setLoginFailureCount(0);
                employee.setLastLoginFailureAt(null);
                employee.setAccountLockedAt(null);
                
                employeeRepository.save(employee);
                
                logger.info("員工 {} 登入失敗次數已重置: {} -> 0", employeeId, oldCount);
            } else {
                logger.debug("員工 {} 登入失敗次數已為 0，無需重置", employeeId);
            }
            
        } catch (Exception e) {
            logger.error("重置登入失敗次數時發生錯誤: {}", e.getMessage(), e);
            throw new RuntimeException("重置登入失敗次數失敗", e);
        }
    }

    /**
     * 鎖定帳號
     * 當登入失敗次數達到上限時調用
     */
    public void lockAccount(Long employeeId) {
        logger.warn("鎖定員工帳號: {}", employeeId);
        
        try {
            EmployeeEntity employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("找不到員工 ID: " + employeeId));
            
            employee.setStatus(AccountStatus.INACTIVE);
            employee.setAccountLockedAt(LocalDateTime.now());
            
            employeeRepository.save(employee);
            
            logger.warn("員工 {} 帳號已被鎖定，失敗次數: {}", 
                       employeeId, employee.getLoginFailureCount());
            
        } catch (Exception e) {
            logger.error("鎖定帳號時發生錯誤: {}", e.getMessage(), e);
            throw new RuntimeException("鎖定帳號失敗", e);
        }
    }

    /**
     * 解鎖帳號
     * 管理員可以調用此方法重置員工帳號
     */
    public void unlockAccount(Long employeeId) {
        logger.info("解鎖員工帳號: {}", employeeId);
        
        try {
            EmployeeEntity employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("找不到員工 ID: " + employeeId));
            
            employee.setStatus(AccountStatus.ACTIVE);
            employee.setLoginFailureCount(0);
            employee.setLastLoginFailureAt(null);
            employee.setAccountLockedAt(null);
            
            employeeRepository.save(employee);
            
            logger.info("員工 {} 帳號已解鎖並重置登入失敗次數", employeeId);
            
        } catch (Exception e) {
            logger.error("解鎖帳號時發生錯誤: {}", e.getMessage(), e);
            throw new RuntimeException("解鎖帳號失敗", e);
        }
    }

    /**
     * 檢查帳號是否因登入失敗過多而被鎖定
     */
    @Transactional(readOnly = true)
    public boolean isAccountLockedDueToFailures(Long employeeId) {
        try {
            EmployeeEntity employee = employeeRepository.findById(employeeId)
                    .orElse(null);
            
            if (employee == null) {
                return false;
            }
            
            return employee.getStatus() == AccountStatus.INACTIVE && 
                   employee.getLoginFailureCount() != null && 
                   employee.getLoginFailureCount() >= MAX_LOGIN_ATTEMPTS;
                   
        } catch (Exception e) {
            logger.error("檢查帳號鎖定狀態時發生錯誤: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 獲取剩餘登入嘗試次數
     */
    @Transactional(readOnly = true)
    public int getRemainingAttempts(Long employeeId) {
        try {
            EmployeeEntity employee = employeeRepository.findById(employeeId)
                    .orElse(null);
            
            if (employee == null) {
                return MAX_LOGIN_ATTEMPTS;
            }
            
            int currentFailures = employee.getLoginFailureCount() != null ? employee.getLoginFailureCount() : 0;
            return Math.max(0, MAX_LOGIN_ATTEMPTS - currentFailures);
            
        } catch (Exception e) {
            logger.error("計算剩餘登入次數時發生錯誤: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 將員工實體轉換為DTO
     */
    public EmployeeDTO convertToDTO(EmployeeEntity employee) {
        if (employee == null) {
            return null;
        }
        
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmployeeId(employee.getEmployeeId());
        dto.setUsername(employee.getUsername());
        dto.setAccount(employee.getAccount());
        dto.setEmail(employee.getEmail());
        dto.setPhone(employee.getPhone());
        dto.setRole(employee.getRole());
        dto.setStatus(employee.getStatus());
        dto.setGender(employee.getGender());
        dto.setPhotoUrl(employee.getPhotoUrl());
        dto.setCreateTime(employee.getCreateTime());
        
        // 設定門市資訊
        if (employee.getStore() != null) {
            dto.setStoreId(employee.getStore().getStoreId());
            dto.setStoreName(employee.getStore().getStoreName());
        }
        
        return dto;
    }
}