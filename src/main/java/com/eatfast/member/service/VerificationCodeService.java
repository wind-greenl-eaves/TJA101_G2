package com.eatfast.member.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * 驗證碼服務 - 使用 Redis 存儲驗證碼
 */
@Service
public class VerificationCodeService {
    
    private static final Logger log = LoggerFactory.getLogger(VerificationCodeService.class);
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();
    
    // 驗證碼有效期（分鐘）
    private static final int VERIFICATION_CODE_EXPIRE_MINUTES = 15;
    
    public VerificationCodeService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
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
     * 存儲驗證碼到 Redis
     * @param email 會員email
     * @param code 驗證碼
     */
    public void storeVerificationCode(String email, String code) {
        String normalizedEmail = email.trim().toLowerCase(); // 標準化 email
        String key = "verification_code:" + normalizedEmail;
        
        try {
            redisTemplate.opsForValue().set(key, code, VERIFICATION_CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            log.info("驗證碼已存儲到 Redis - Key: {}, Code: {}, TTL: {} 分鐘", key, code, VERIFICATION_CODE_EXPIRE_MINUTES);
            
            // 驗證是否真的存儲成功
            String storedValue = (String) redisTemplate.opsForValue().get(key);
            if (storedValue != null) {
                log.info("驗證存儲成功 - 從 Redis 讀取到: {}", storedValue);
            } else {
                log.error("驗證碼存儲失敗 - 無法從 Redis 讀取");
            }
        } catch (Exception e) {
            log.error("存儲驗證碼到 Redis 失敗 - Email: {}, 錯誤: {}", email, e.getMessage(), e);
        }
    }
    
    /**
     * 驗證驗證碼
     * @param email 會員email
     * @param inputCode 使用者輸入的驗證碼
     * @return 驗證是否成功
     */
    public boolean verifyCode(String email, String inputCode) {
        String normalizedEmail = email.trim().toLowerCase(); // 標準化 email
        String key = "verification_code:" + normalizedEmail;
        
        log.info("開始驗證驗證碼 - Email: {}, 輸入的驗證碼: {}, Redis Key: {}", email, inputCode, key);
        
        try {
            // 檢查 Redis 連接
            String storedCode = (String) redisTemplate.opsForValue().get(key);
            log.info("從 Redis 讀取的驗證碼: {}", storedCode);
            
            if (storedCode == null) {
                log.warn("Redis 中沒有找到驗證碼 - Key: {}", key);
                
                // 列出所有相關的 key 來調試
                try {
                    var keys = redisTemplate.keys("verification_code:*");
                    log.info("Redis 中現有的驗證碼 keys: {}", keys);
                } catch (Exception e) {
                    log.error("無法列出 Redis keys: {}", e.getMessage());
                }
                
                return false;
            }
            
            // 去除空格並比較
            String trimmedStoredCode = storedCode.trim();
            String trimmedInputCode = inputCode.trim();
            
            log.info("比較驗證碼 - 存儲的: '{}', 輸入的: '{}', 長度比較: {} vs {}", 
                     trimmedStoredCode, trimmedInputCode, trimmedStoredCode.length(), trimmedInputCode.length());
            
            if (trimmedStoredCode.equals(trimmedInputCode)) {
                log.info("驗證碼驗證成功 - Email: {}", email);
                // 驗證成功後刪除驗證碼
                redisTemplate.delete(key);
                log.info("已刪除使用過的驗證碼 - Key: {}", key);
                return true;
            } else {
                log.warn("驗證碼不匹配 - Email: {}, 存儲的: '{}', 輸入的: '{}'", email, trimmedStoredCode, trimmedInputCode);
                return false;
            }
            
        } catch (Exception e) {
            log.error("驗證驗證碼時發生錯誤 - Email: {}, 錯誤: {}", email, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 檢查驗證碼是否存在
     */
    public boolean hasVerificationCode(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        String key = "verification_code:" + normalizedEmail;
        boolean exists = redisTemplate.hasKey(key);
        log.info("檢查驗證碼是否存在 - Email: {}, Key: {}, 存在: {}", email, key, exists);
        return exists;
    }
    
    /**
     * 刪除驗證碼
     */
    public void deleteVerificationCode(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        String key = "verification_code:" + normalizedEmail;
        redisTemplate.delete(key);
        log.info("已刪除驗證碼 - Email: {}, Key: {}", email, key);
    }
}