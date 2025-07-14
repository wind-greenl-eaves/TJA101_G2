package com.eatfast.employee.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 忘記密碼請求頻率限制服務
 * 防止同一帳號或IP在短時間內重複發送忘記密碼請求
 */
@Service
public class ForgotPasswordRateLimitService {
    
    // 帳號請求記錄 - 記錄每個帳號的最後請求時間
    private final Map<String, LocalDateTime> accountRequestMap = new ConcurrentHashMap<>();
    
    // IP 請求記錄 - 記錄每個IP的最後請求時間
    private final Map<String, LocalDateTime> ipRequestMap = new ConcurrentHashMap<>();
    
    // 請求間隔限制（秒）
    private static final long REQUEST_INTERVAL_SECONDS = 30;
    
    // 最大記錄數量，防止內存溢出
    private static final int MAX_RECORDS = 10000;

    /**
     * 檢查指定帳號是否可以發送忘記密碼請求
     * @param account 帳號或郵件
     * @return true 如果可以發送請求，false 如果需要等待
     */
    public boolean canSendRequest(String account) {
        if (account == null || account.trim().isEmpty()) {
            return true;
        }
        
        String normalizedAccount = account.trim().toLowerCase();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastRequestTime = accountRequestMap.get(normalizedAccount);
        
        if (lastRequestTime == null) {
            return true;
        }
        
        // 計算距離上次請求的秒數
        long secondsSinceLastRequest = java.time.Duration.between(lastRequestTime, now).getSeconds();
        return secondsSinceLastRequest >= REQUEST_INTERVAL_SECONDS;
    }

    /**
     * 檢查指定IP是否可以發送忘記密碼請求
     * @param ipAddress IP地址
     * @return true 如果可以發送請求，false 如果需要等待
     */
    public boolean canSendRequestFromIP(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return true;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastRequestTime = ipRequestMap.get(ipAddress);
        
        if (lastRequestTime == null) {
            return true;
        }
        
        // 計算距離上次請求的秒數
        long secondsSinceLastRequest = java.time.Duration.between(lastRequestTime, now).getSeconds();
        return secondsSinceLastRequest >= REQUEST_INTERVAL_SECONDS;
    }

    /**
     * 記錄帳號的請求時間
     * @param account 帳號或郵件
     */
    public void recordAccountRequest(String account) {
        if (account == null || account.trim().isEmpty()) {
            return;
        }
        
        String normalizedAccount = account.trim().toLowerCase();
        
        // 清理過期記錄
        cleanupExpiredRecords();
        
        // 記錄請求時間
        accountRequestMap.put(normalizedAccount, LocalDateTime.now());
    }

    /**
     * 記錄IP的請求時間
     * @param ipAddress IP地址
     */
    public void recordIPRequest(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return;
        }
        
        // 清理過期記錄
        cleanupExpiredRecords();
        
        // 記錄請求時間
        ipRequestMap.put(ipAddress, LocalDateTime.now());
    }

    /**
     * 獲取帳號剩餘等待時間（秒）
     * @param account 帳號或郵件
     * @return 剩餘等待時間，0表示可以立即發送
     */
    public long getRemainingWaitTime(String account) {
        if (account == null || account.trim().isEmpty()) {
            return 0;
        }
        
        String normalizedAccount = account.trim().toLowerCase();
        LocalDateTime lastRequestTime = accountRequestMap.get(normalizedAccount);
        
        if (lastRequestTime == null) {
            return 0;
        }
        
        LocalDateTime now = LocalDateTime.now();
        long secondsSinceLastRequest = java.time.Duration.between(lastRequestTime, now).getSeconds();
        long remainingTime = REQUEST_INTERVAL_SECONDS - secondsSinceLastRequest;
        
        return Math.max(0, remainingTime);
    }

    /**
     * 獲取IP剩餘等待時間（秒）
     * @param ipAddress IP地址
     * @return 剩餘等待時間，0表示可以立即發送
     */
    public long getRemainingWaitTimeForIP(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return 0;
        }
        
        LocalDateTime lastRequestTime = ipRequestMap.get(ipAddress);
        
        if (lastRequestTime == null) {
            return 0;
        }
        
        LocalDateTime now = LocalDateTime.now();
        long secondsSinceLastRequest = java.time.Duration.between(lastRequestTime, now).getSeconds();
        long remainingTime = REQUEST_INTERVAL_SECONDS - secondsSinceLastRequest;
        
        return Math.max(0, remainingTime);
    }

    /**
     * 清理過期的請求記錄
     * 避免內存無限增長
     */
    private void cleanupExpiredRecords() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1); // 清理1小時前的記錄
        
        // 如果記錄數量過多，進行清理
        if (accountRequestMap.size() > MAX_RECORDS) {
            accountRequestMap.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoffTime));
        }
        
        if (ipRequestMap.size() > MAX_RECORDS) {
            ipRequestMap.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoffTime));
        }
    }

    /**
     * 清除指定帳號的請求記錄（用於測試或管理員重置）
     * @param account 帳號或郵件
     */
    public void clearAccountRecord(String account) {
        if (account != null && !account.trim().isEmpty()) {
            String normalizedAccount = account.trim().toLowerCase();
            accountRequestMap.remove(normalizedAccount);
        }
    }

    /**
     * 清除指定IP的請求記錄（用於測試或管理員重置）
     * @param ipAddress IP地址
     */
    public void clearIPRecord(String ipAddress) {
        if (ipAddress != null && !ipAddress.trim().isEmpty()) {
            ipRequestMap.remove(ipAddress);
        }
    }

    /**
     * 獲取請求間隔限制時間
     * @return 請求間隔限制（秒）
     */
    public long getRequestIntervalSeconds() {
        return REQUEST_INTERVAL_SECONDS;
    }
}