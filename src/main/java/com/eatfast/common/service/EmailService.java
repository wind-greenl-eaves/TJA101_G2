package com.eatfast.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 郵件服務類
 * 負責處理系統中所有郵件發送功能
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.mail.system-name:早餐店管理系統}")
    private String systemName;

    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * 發送密碼重設郵件（統一發送到指定郵箱）
     * 
     * @param memberEmail 會員原本的電子郵件（用於識別）
     * @param memberAccount 會員帳號
     * @param memberName 會員姓名
     * @param resetToken 重設密碼的 Token
     * @param resetUrl 完整的重設密碼 URL
     */
    public void sendPasswordResetEmail(String memberEmail, String memberAccount, String memberName, String resetToken, String resetUrl) {
        if (!mailEnabled) {
            log.warn("郵件服務已停用，跳過發送密碼重設郵件");
            return;
        }

        try {
            // 統一發送到指定郵箱
            String targetEmail = "young19960127@gmail.com";
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, systemName);
            helper.setTo(targetEmail);
            helper.setSubject("【" + systemName + "】會員密碼重設通知");

            // 創建 HTML 郵件內容
            String htmlContent = createPasswordResetEmailContent(memberEmail, memberAccount, memberName, resetUrl);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            
            log.info("密碼重設郵件已發送 - 會員: {} ({}) -> 目標郵箱: {}", memberAccount, memberName, targetEmail);

        } catch (Exception e) {
            log.error("發送密碼重設郵件失敗 - 會員: {} ({}), 錯誤: {}", memberAccount, memberName, e.getMessage(), e);
            throw new RuntimeException("郵件發送失敗: " + e.getMessage(), e);
        }
    }

    /**
     * 創建密碼重設郵件的 HTML 內容
     */
    private String createPasswordResetEmailContent(String memberEmail, String memberAccount, String memberName, String resetUrl) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        return String.format("""
            <!DOCTYPE html>
            <html lang="zh-TW">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>密碼重設通知</title>
                <style>
                    body {
                        font-family: 'Microsoft JhengHei', Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                        background-color: #f5f5f5;
                    }
                    .container {
                        background-color: white;
                        padding: 30px;
                        border-radius: 10px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    .header {
                        text-align: center;
                        border-bottom: 3px solid #A67B5B;
                        padding-bottom: 20px;
                        margin-bottom: 30px;
                    }
                    .header h1 {
                        color: #A67B5B;
                        margin: 0;
                        font-size: 24px;
                    }
                    .content {
                        margin-bottom: 30px;
                    }
                    .member-info {
                        background-color: #f8f9fa;
                        border-left: 4px solid #A67B5B;
                        padding: 15px;
                        margin: 20px 0;
                    }
                    .member-info h3 {
                        color: #A67B5B;
                        margin-top: 0;
                    }
                    .reset-button {
                        display: inline-block;
                        background-color: #A67B5B;
                        color: white;
                        padding: 15px 30px;
                        text-decoration: none;
                        border-radius: 5px;
                        font-weight: bold;
                        margin: 20px 0;
                        text-align: center;
                    }
                    .reset-button:hover {
                        background-color: #8C684A;
                    }
                    .warning {
                        background-color: #fff3cd;
                        border: 1px solid #ffeaa7;
                        color: #856404;
                        padding: 15px;
                        border-radius: 5px;
                        margin: 20px 0;
                    }
                    .footer {
                        border-top: 1px solid #ddd;
                        padding-top: 20px;
                        text-align: center;
                        color: #666;
                        font-size: 14px;
                    }
                    .url-box {
                        background-color: #f8f9fa;
                        border: 1px solid #ddd;
                        padding: 10px;
                        border-radius: 5px;
                        word-break: break-all;
                        font-family: monospace;
                        margin: 10px 0;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🍳 %s</h1>
                        <p>會員密碼重設通知</p>
                    </div>
                    
                    <div class="content">
                        <h2>會員密碼重設請求</h2>
                        <p>系統收到了一個密碼重設請求，詳細資訊如下：</p>
                        
                        <div class="member-info">
                            <h3>會員資訊</h3>
                            <p><strong>會員帳號：</strong>%s</p>
                            <p><strong>會員姓名：</strong>%s</p>
                            <p><strong>會員信箱：</strong>%s</p>
                            <p><strong>請求時間：</strong>%s</p>
                        </div>
                        
                        <div class="warning">
                            <strong>⚠️ 重要提醒：</strong>
                            <ul>
                                <li>此連結僅在 24 小時內有效</li>
                                <li>每個連結只能使用一次</li>
                                <li>如果不是本人操作，請忽略此郵件</li>
                            </ul>
                        </div>
                        
                        <div style="text-align: center;">
                            <p><strong>請點擊以下按鈕重設密碼：</strong></p>
                            <a href="%s" class="reset-button">🔐 立即重設密碼</a>
                        </div>
                        
                        <p>如果按鈕無法點擊，請複製以下網址到瀏覽器中開啟：</p>
                        <div class="url-box">%s</div>
                    </div>
                    
                    <div class="footer">
                        <p>此郵件由系統自動發送，請勿直接回覆</p>
                        <p>如有疑問，請聯繫系統管理員</p>
                        <p>&copy; 2025 %s</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            systemName, memberAccount, memberName, memberEmail, currentTime,
            resetUrl, resetUrl, systemName
        );
    }

    /**
     * 發送簡單文字郵件（通用方法）
     */
    public void sendSimpleEmail(String to, String subject, String content) {
        if (!mailEnabled) {
            log.warn("郵件服務已停用，跳過發送郵件到: {}", to);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);
            log.info("簡單郵件已發送到: {}", to);

        } catch (Exception e) {
            log.error("發送簡單郵件失敗到: {}, 錯誤: {}", to, e.getMessage(), e);
            throw new RuntimeException("郵件發送失敗: " + e.getMessage(), e);
        }
    }

    /**
     * 測試郵件連接
     */
    public boolean testEmailConnection() {
        try {
            mailSender.createMimeMessage();
            log.info("郵件服務連接測試成功");
            return true;
        } catch (Exception e) {
            log.error("郵件服務連接測試失敗: {}", e.getMessage(), e);
            return false;
        }
    }
}