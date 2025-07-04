package com.eatfast.member.controller;

import com.eatfast.member.service.VerificationCodeService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis 測試控制器 - 用於診斷 Redis 連接問題
 */
@Controller
@RequestMapping("/test")
public class RedisTestController {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final VerificationCodeService verificationCodeService;
    
    public RedisTestController(RedisTemplate<String, Object> redisTemplate, 
                              VerificationCodeService verificationCodeService) {
        this.redisTemplate = redisTemplate;
        this.verificationCodeService = verificationCodeService;
    }
    
    /**
     * 顯示驗證碼測試頁面
     */
    @GetMapping("/test-page")
    public String showTestPage() {
        return "test/verification-test";
    }
    
    /**
     * 測試 Redis 連接
     */
    @GetMapping("/redis-connection")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testRedisConnection() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 測試基本連接
            redisTemplate.opsForValue().set("test_key", "test_value");
            String testValue = (String) redisTemplate.opsForValue().get("test_key");
            
            result.put("connection", "成功");
            result.put("testWrite", "成功");
            result.put("testRead", testValue != null ? "成功" : "失敗");
            result.put("testValue", testValue);
            
            // 清理測試數據
            redisTemplate.delete("test_key");
            
        } catch (Exception e) {
            result.put("connection", "失敗");
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 測試驗證碼功能 - 支援 GET 和 POST
     */
    @GetMapping("/verification-code")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testVerificationCodeGet(@RequestParam String email) {
        return testVerificationCodeInternal(email);
    }
    
    @PostMapping("/verification-code")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testVerificationCodePost(@RequestParam String email) {
        return testVerificationCodeInternal(email);
    }
    
    /**
     * 內部驗證碼測試邏輯
     */
    private ResponseEntity<Map<String, Object>> testVerificationCodeInternal(String email) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 生成驗證碼
            String code = verificationCodeService.generateVerificationCode();
            result.put("generatedCode", code);
            
            // 存儲驗證碼
            verificationCodeService.storeVerificationCode(email, code);
            result.put("storeResult", "成功");
            
            // 檢查是否存在
            boolean exists = verificationCodeService.hasVerificationCode(email);
            result.put("existsCheck", exists ? "成功" : "失敗");
            
            // 驗證驗證碼
            boolean verifyResult = verificationCodeService.verifyCode(email, code);
            result.put("verifyResult", verifyResult ? "成功" : "失敗");
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 查看所有 Redis keys
     */
    @GetMapping("/redis-keys")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAllRedisKeys() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Set<String> allKeys = redisTemplate.keys("*");
            Set<String> verificationKeys = redisTemplate.keys("verification_code:*");
            
            result.put("totalKeys", allKeys != null ? allKeys.size() : 0);
            result.put("allKeys", allKeys);
            result.put("verificationKeys", verificationKeys);
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 手動設置驗證碼（測試用）
     */
    @PostMapping("/set-verification-code")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> setVerificationCode(
            @RequestParam String email, 
            @RequestParam String code) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            verificationCodeService.storeVerificationCode(email, code);
            result.put("result", "設置成功");
            result.put("email", email);
            result.put("code", code);
            
            // 驗證是否設置成功
            boolean exists = verificationCodeService.hasVerificationCode(email);
            result.put("verifyExists", exists);
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
}