package com.eatfast.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
 * 專業郵件發送服務
 * 提供忘記密碼、通知等郵件發送功能
 */
@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.mail.system-name:早餐店管理系統}")
    private String systemName;

    /**
     * 發送忘記密碼郵件
     * @param toEmail 收件人郵箱
     * @param employeeName 員工姓名
     * @param employeeAccount 員工帳號
     * @param temporaryPassword 臨時密碼
     * @return 發送是否成功
     */
    public boolean sendForgotPasswordEmail(String toEmail, String employeeName, String employeeAccount, String temporaryPassword) {
        try {
            String subject = String.format("【%s】密碼重設通知", systemName);
            String content = buildForgotPasswordEmailContent(employeeName, employeeAccount, temporaryPassword);
            
            sendHtmlEmail(toEmail, subject, content);
            
            log.info("忘記密碼郵件發送成功 - 收件人: {}, 員工: {}", toEmail, employeeName);
            return true;
            
        } catch (Exception e) {
            log.error("忘記密碼郵件發送失敗 - 收件人: {}, 員工: {}, 錯誤: {}", toEmail, employeeName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 發送簡單文字郵件
     * @param to 收件人
     * @param subject 主旨
     * @param text 內容
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            javaMailSender.send(message);
            log.info("簡單郵件發送成功 - 收件人: {}, 主旨: {}", to, subject);
            
        } catch (Exception e) {
            log.error("簡單郵件發送失敗 - 收件人: {}, 主旨: {}, 錯誤: {}", to, subject, e.getMessage(), e);
            throw new RuntimeException("郵件發送失敗: " + e.getMessage(), e);
        }
    }

    /**
     * 發送 HTML 格式郵件
     * @param to 收件人
     * @param subject 主旨
     * @param htmlContent HTML 內容
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            log.info("準備發送 HTML 郵件 - 收件人: {}, 主旨: {}", to, subject);
            
            // 驗證收件人郵箱格式
            if (!isValidEmail(to)) {
                throw new IllegalArgumentException("無效的收件人郵箱地址: " + to);
            }
            
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true 表示使用 HTML 格式
            
            log.debug("郵件配置完成，開始發送...");
            javaMailSender.send(message);
            log.info("HTML 郵件發送成功 - 收件人: {}, 主旨: {}", to, subject);
            
        } catch (MessagingException e) {
            log.error("HTML 郵件發送失敗 - 收件人: {}, 主旨: {}, 錯誤類型: MessagingException", to, subject);
            log.error("詳細錯誤信息: {}", e.getMessage(), e);
            
            // 檢查常見的 Gmail 錯誤
            if (e.getMessage().contains("Username and Password not accepted")) {
                log.error("❌ Gmail 認證失敗 - 請檢查:");
                log.error("1. 郵箱帳號是否正確: {}", fromEmail);
                log.error("2. 應用程式密碼是否正確（不是一般登入密碼）");
                log.error("3. 是否已啟用 Gmail 的「兩步驟驗證」");
                log.error("4. 是否已產生並使用「應用程式密碼」");
            } else if (e.getMessage().contains("Could not connect to SMTP host")) {
                log.error("❌ SMTP 連接失敗 - 請檢查網路連接和防火牆設定");
            }
            
            throw new RuntimeException("郵件發送失敗: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("HTML 郵件發送失敗 - 收件人: {}, 主旨: {}, 錯誤類型: {}", to, subject, e.getClass().getSimpleName());
            log.error("詳細錯誤信息: {}", e.getMessage(), e);
            throw new RuntimeException("郵件發送失敗: " + e.getMessage(), e);
        }
    }

    /**
     * 建構忘記密碼郵件的 HTML 內容
     * @param employeeName 員工姓名
     * @param employeeAccount 員工帳號
     * @param temporaryPassword 臨時密碼
     * @return HTML 格式的郵件內容
     */
    private String buildForgotPasswordEmailContent(String employeeName, String employeeAccount, String temporaryPassword) {
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
                        background-color: #ffffff;
                        border-radius: 8px;
                        padding: 30px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    .header {
                        text-align: center;
                        border-bottom: 2px solid #A67B5B;
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
                    .password-box {
                        background-color: #f8f9fa;
                        border: 2px solid #A67B5B;
                        border-radius: 6px;
                        padding: 15px;
                        text-align: center;
                        margin: 20px 0;
                    }
                    .password {
                        font-size: 24px;
                        font-weight: bold;
                        color: #A67B5B;
                        font-family: 'Courier New', monospace;
                        letter-spacing: 2px;
                    }
                    .warning {
                        background-color: #fff3cd;
                        border: 1px solid #ffeaa7;
                        border-radius: 4px;
                        padding: 15px;
                        margin: 20px 0;
                    }
                    .footer {
                        border-top: 1px solid #ddd;
                        padding-top: 20px;
                        text-align: center;
                        font-size: 12px;
                        color: #666;
                    }
                    .info-table {
                        width: 100%%;
                        border-collapse: collapse;
                        margin: 20px 0;
                    }
                    .info-table td {
                        padding: 8px;
                        border-bottom: 1px solid #eee;
                    }
                    .info-table .label {
                        font-weight: bold;
                        width: 120px;
                        color: #555;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔑 %s</h1>
                        <p>密碼重設通知</p>
                    </div>
                    
                    <div class="content">
                        <p>親愛的 <strong>%s</strong> 您好，</p>
                        
                        <p>我們收到了您的密碼重設請求。為了確保您的帳戶安全，系統已為您生成一組新的臨時密碼。</p>
                        
                        <table class="info-table">
                            <tr>
                                <td class="label">員工姓名：</td>
                                <td>%s</td>
                            </tr>
                            <tr>
                                <td class="label">登入帳號：</td>
                                <td>%s</td>
                            </tr>
                            <tr>
                                <td class="label">重設時間：</td>
                                <td>%s</td>
                            </tr>
                        </table>
                        
                        <div class="password-box">
                            <p style="margin: 0; font-size: 16px;">您的新臨時密碼為：</p>
                            <div class="password">%s</div>
                        </div>
                        
                        <div class="warning">
                            <h4 style="margin-top: 0; color: #856404;">⚠️ 重要提醒</h4>
                            <ul style="margin-bottom: 0;">
                                <li>請立即使用此臨時密碼登入系統</li>
                                <li>登入後請儘快修改為您的個人密碼</li>
                                <li>請勿將此密碼透露給他人</li>
                                <li>如果這不是您本人的操作，請立即聯繫系統管理員</li>
                            </ul>
                        </div>
                        
                        <p>如有任何問題，請聯繫系統管理員。</p>
                        
                        <p>感謝您的使用！<br>
                        %s 團隊</p>
                    </div>
                    
                    <div class="footer">
                        <p>此郵件由系統自動發送，請勿直接回覆。</p>
                        <p>發送時間：%s</p>
                    </div>
                </div>
            </body>
            </html>
            """, 
            systemName, employeeName, employeeName, employeeAccount, currentTime, 
            temporaryPassword, systemName, currentTime);
    }

    /**
     * 驗證郵箱地址格式
     * @param email 郵箱地址
     * @return 是否為有效的郵箱格式
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }
}