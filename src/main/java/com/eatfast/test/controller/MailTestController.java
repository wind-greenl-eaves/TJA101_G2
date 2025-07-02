package com.eatfast.test.controller;

import com.eatfast.common.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 郵件測試控制器
 * 提供網頁介面來測試郵件發送功能
 */
@Controller
@RequestMapping("/test")
public class MailTestController {
    
    private static final Logger log = LoggerFactory.getLogger(MailTestController.class);
    
    @Autowired
    private MailService mailService;
    
    /**
     * 顯示郵件測試頁面
     */
    @GetMapping("/mail")
    public String showMailTestPage(Model model) {
        model.addAttribute("defaultEmail", "young19960127@gmail.com");
        return "test/mail-test";
    }
    
    /**
     * 測試發送忘記密碼郵件
     */
    @PostMapping("/mail/forgot-password")
    public String testForgotPasswordEmail(
            @RequestParam String email,
            @RequestParam(defaultValue = "測試員工") String employeeName,
            @RequestParam(defaultValue = "testuser") String employeeAccount,
            Model model) {
        
        try {
            log.info("開始測試忘記密碼郵件發送到: {}", email);
            
            // 生成測試密碼
            String testPassword = "Test" + System.currentTimeMillis() % 10000;
            
            boolean result = mailService.sendForgotPasswordEmail(
                email, 
                employeeName, 
                employeeAccount, 
                testPassword
            );
            
            if (result) {
                model.addAttribute("success", true);
                model.addAttribute("message", "忘記密碼郵件發送成功！請檢查收件箱: " + email);
                model.addAttribute("tempPassword", testPassword);
                log.info("✅ 忘記密碼郵件測試成功");
            } else {
                model.addAttribute("success", false);
                model.addAttribute("message", "忘記密碼郵件發送失敗！請檢查日誌以了解詳細錯誤信息。");
                log.error("❌ 忘記密碼郵件測試失敗");
            }
            
        } catch (Exception e) {
            model.addAttribute("success", false);
            model.addAttribute("message", "郵件發送異常: " + e.getMessage());
            log.error("忘記密碼郵件測試異常", e);
        }
        
        model.addAttribute("defaultEmail", email);
        return "test/mail-test";
    }
    
    /**
     * 測試發送簡單文字郵件
     */
    @PostMapping("/mail/simple")
    public String testSimpleEmail(
            @RequestParam String email,
            @RequestParam(defaultValue = "測試郵件 - 早餐店管理系統") String subject,
            @RequestParam(defaultValue = "這是一封測試郵件，用來確認郵件服務是否正常運作。") String content,
            Model model) {
        
        try {
            log.info("開始測試簡單文字郵件發送到: {}", email);
            
            mailService.sendSimpleEmail(email, subject, content);
            
            model.addAttribute("success", true);
            model.addAttribute("message", "簡單文字郵件發送成功！請檢查收件箱: " + email);
            log.info("✅ 簡單文字郵件測試成功");
            
        } catch (Exception e) {
            model.addAttribute("success", false);
            model.addAttribute("message", "簡單文字郵件發送失敗: " + e.getMessage());
            log.error("簡單文字郵件測試失敗", e);
        }
        
        model.addAttribute("defaultEmail", email);
        return "test/mail-test";
    }
}