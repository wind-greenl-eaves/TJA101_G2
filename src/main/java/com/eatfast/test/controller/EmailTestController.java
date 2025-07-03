package com.eatfast.test.controller;

import com.eatfast.common.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 郵件測試控制器
 * 用於測試郵件服務功能
 */
@Controller
@RequestMapping("/test/email")
public class EmailTestController {

    private static final Logger log = LoggerFactory.getLogger(EmailTestController.class);
    
    private final EmailService emailService;

    public EmailTestController(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * 顯示郵件測試頁面
     */
    @GetMapping("/test-page")
    public String showEmailTestPage() {
        return "test/email-test";
    }

    /**
     * 測試郵件連接
     */
    @PostMapping("/test-connection")
    @ResponseBody
    public Map<String, Object> testEmailConnection() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean isConnected = emailService.testEmailConnection();
            result.put("success", isConnected);
            result.put("message", isConnected ? "郵件服務連接成功" : "郵件服務連接失敗");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "連接測試失敗: " + e.getMessage());
            log.error("郵件連接測試失敗", e);
        }
        
        return result;
    }

    /**
     * 發送測試郵件
     */
    @PostMapping("/send-test-email")
    @ResponseBody
    public Map<String, Object> sendTestEmail() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String testSubject = "【早餐店管理系統】郵件服務測試";
            String testContent = String.format("""
                這是一封測試郵件
                
                發送時間: %s
                系統版本: Spring Boot 3.4.1
                郵件服務: 已成功整合
                
                如果您收到這封郵件，表示郵件服務運作正常！
                
                此郵件由系統自動發送，請勿回覆。
                """, 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );
            
            emailService.sendSimpleEmail("young19960127@gmail.com", testSubject, testContent);
            
            result.put("success", true);
            result.put("message", "測試郵件發送成功！請檢查 young19960127@gmail.com 收件匣");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "測試郵件發送失敗: " + e.getMessage());
            log.error("測試郵件發送失敗", e);
        }
        
        return result;
    }

    /**
     * 發送密碼重設測試郵件
     */
    @PostMapping("/send-password-reset-test")
    @ResponseBody
    public Map<String, Object> sendPasswordResetTest() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 模擬密碼重設郵件
            String testToken = "TEST_TOKEN_" + System.currentTimeMillis();
            String testResetUrl = "http://localhost:8080/api/v1/auth/reset-password?token=" + testToken;
            
            emailService.sendPasswordResetEmail(
                "test@example.com",      // 模擬會員信箱
                "testuser123",           // 模擬會員帳號
                "測試用戶",               // 模擬會員姓名
                testToken,               // 測試 Token
                testResetUrl             // 測試 URL
            );
            
            result.put("success", true);
            result.put("message", "密碼重設測試郵件發送成功！請檢查 young19960127@gmail.com 收件匣");
            result.put("testToken", testToken);
            result.put("testUrl", testResetUrl);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "密碼重設測試郵件發送失敗: " + e.getMessage());
            log.error("密碼重設測試郵件發送失敗", e);
        }
        
        return result;
    }
}