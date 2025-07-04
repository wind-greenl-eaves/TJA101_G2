package com.eatfast.member.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 內存版驗證碼服務 - 當 Redis 不可用時的備選方案
 */
@Service
@ConditionalOnProperty(name = "verification.storage.type", havingValue = "memory", matchIfMissing = false)
public class InMemoryVerificationCodeService {
    
    private static final Logger log = LoggerFactory.getLogger(InMemoryVerificationCodeService.class);
    
    private final SecureRandom secureRandom = new SecureRandom();
    private final ConcurrentHashMap<String, VerificationData> verificationCodes = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    // 驗證碼有效期（分鐘）
    private static final int VERIFICATION_CODE_EXPIRE_MINUTES = 15;
    
    public InMemoryVerificationCodeService() {
        // 每分鐘清理過期的驗證碼
        scheduler.scheduleAtFixedRate(this::cleanupExpiredCodes, 1, 1, TimeUnit.MINUTES);
        log.info("內存版驗證碼服務已啟動 - 每分鐘清理過期驗證碼");
    }
    
    /**
     * 驗證碼資料結構
     */
    private static class VerificationData {
        private final String code;
        private final long expiryTime;
        
        public VerificationData(String code, long expiryTime) {
            this.code = code;
            this.expiryTime = expiryTime;
        }
        
        public String getCode() {
            return code;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }
    
    /**
     * 生成6位數字驗證碼
     */
    public String generateVerificationCode() {
        int code = 100000 + secureRandom.nextInt(900000);
        String codeStr = String.valueOf(code);
        log.info("生成驗證碼: {}", codeStr);
        return codeStr;
    }
    
    /**
     * 存儲驗證碼到內存
     */
    public void storeVerificationCode(String email, String code) {
        String normalizedEmail = email.trim().toLowerCase();
        long expiryTime = System.currentTimeMillis() + (VERIFICATION_CODE_EXPIRE_MINUTES * 60 * 1000);
        
        verificationCodes.put(normalizedEmail, new VerificationData(code, expiryTime));
        log.info("驗證碼已存儲到內存 - Email: {}, Code: {}, 過期時間: {} 分鐘後", 
                 normalizedEmail, code, VERIFICATION_CODE_EXPIRE_MINUTES);
    }
    
    /**
     * 驗證驗證碼
     */
    public boolean verifyCode(String email, String inputCode) {
        String normalizedEmail = email.trim().toLowerCase();
        
        log.info("開始驗證驗證碼 - Email: {}, 輸入的驗證碼: {}", email, inputCode);
        
        VerificationData data = verificationCodes.get(normalizedEmail);
        
        if (data == null) {
            log.warn("內存中沒有找到驗證碼 - Email: {}", normalizedEmail);
            log.info("內存中現有的驗證碼 emails: {}", verificationCodes.keySet());
            return false;
        }
        
        if (data.isExpired()) {
            log.warn("驗證碼已過期 - Email: {}", normalizedEmail);
            verificationCodes.remove(normalizedEmail);
            return false;
        }
        
        String trimmedStoredCode = data.getCode().trim();
        String trimmedInputCode = inputCode.trim();
        
        log.info("比較驗證碼 - 存儲的: '{}', 輸入的: '{}', 長度比較: {} vs {}", 
                 trimmedStoredCode, trimmedInputCode, trimmedStoredCode.length(), trimmedInputCode.length());
        
        if (trimmedStoredCode.equals(trimmedInputCode)) {
            log.info("驗證碼驗證成功 - Email: {}", email);
            // 驗證成功後刪除驗證碼
            verificationCodes.remove(normalizedEmail);
            return true;
        } else {
            log.warn("驗證碼不匹配 - Email: {}, 存儲的: '{}', 輸入的: '{}'", 
                     email, trimmedStoredCode, trimmedInputCode);
            return false;
        }
    }
    
    /**
     * 檢查驗證碼是否存在
     */
    public boolean hasVerificationCode(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        VerificationData data = verificationCodes.get(normalizedEmail);
        boolean exists = data != null && !data.isExpired();
        log.info("檢查驗證碼是否存在 - Email: {}, 存在: {}", email, exists);
        
        if (data != null && data.isExpired()) {
            verificationCodes.remove(normalizedEmail);
        }
        
        return exists;
    }
    
    /**
     * 刪除驗證碼
     */
    public void deleteVerificationCode(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        verificationCodes.remove(normalizedEmail);
        log.info("已刪除驗證碼 - Email: {}", email);
    }
    
    /**
     * 清理過期的驗證碼
     */
    private void cleanupExpiredCodes() {
        int removedCount = 0;
        long currentTime = System.currentTimeMillis();
        
        verificationCodes.entrySet().removeIf(entry -> {
            if (entry.getValue().isExpired()) {
                return true;
            }
            return false;
        });
        
        if (removedCount > 0) {
            log.info("清理了 {} 個過期的驗證碼", removedCount);
        }
    }
    
    /**
     * 獲取統計信息
     */
    public int getActiveCodesCount() {
        cleanupExpiredCodes();
        return verificationCodes.size();
    }
}