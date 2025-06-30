package com.eatfast.test;

import com.eatfast.common.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 郵件服務測試類
 * 用於測試 MailService 是否正常工作
 */
@Component
public class MailServiceTest implements CommandLineRunner {
    
    private static final Logger log = LoggerFactory.getLogger(MailServiceTest.class);
    
    @Autowired
    private MailService mailService;

    @Override
    public void run(String... args) throws Exception {
        // 註解掉這部分，避免每次啟動都發送測試郵件
        // testSendEmail();
    }
    
    /**
     * 測試發送郵件功能
     * 可以手動調用此方法進行測試
     */
    public void testSendEmail() {
        try {
            log.info("開始測試郵件發送功能...");
            
            // 測試發送忘記密碼郵件
            String testEmail = "young19960127@gmail.com";
            String testEmployeeName = "測試員工";
            String testAccount = "testuser";
            String testPassword = "Test123456";
            
            log.info("準備發送測試郵件到: {}", testEmail);
            
            boolean result = mailService.sendForgotPasswordEmail(
                testEmail, 
                testEmployeeName, 
                testAccount, 
                testPassword
            );
            
            if (result) {
                log.info("✅ 測試郵件發送成功！請檢查收件箱: {}", testEmail);
            } else {
                log.error("❌ 測試郵件發送失敗！");
            }
            
        } catch (Exception e) {
            log.error("測試郵件發送時發生異常: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 測試簡單文字郵件
     */
    public void testSimpleEmail() {
        try {
            log.info("開始測試簡單文字郵件發送...");
            
            mailService.sendSimpleEmail(
                "young19960127@gmail.com",
                "測試郵件 - 早餐店管理系統",
                "這是一封測試郵件，用來確認郵件服務是否正常運作。\n\n如果您收到這封郵件，表示郵件服務設定正確。"
            );
            
            log.info("✅ 簡單文字郵件發送完成！");
            
        } catch (Exception e) {
            log.error("❌ 簡單文字郵件發送失敗: {}", e.getMessage(), e);
        }
    }
}