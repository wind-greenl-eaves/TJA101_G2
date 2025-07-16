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

    private final JavaMailSender mailSender; // JavaMailSender 用於發送郵件

    @Value("${spring.mail.username}") // 寄件者郵箱地址
    private String fromEmail; // 寄件者郵箱地址

    @Value("${app.mail.system-name:早餐店管理系統}")
    private String systemName;

    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled; // 是否啟用郵件服務，默認為 true

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
            // 【關鍵修正】設定 UTF-8 編碼，不使用 multipart
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            // 【修正】設定寄件者信息，不使用個人姓名避免編碼問題
            helper.setFrom(fromEmail);
            helper.setTo(targetEmail);
            
            // 【修正】確保郵件主旨的中文字符正確編碼
            String subject = "【EATFAST早安通】會員密碼重設通知";
            helper.setSubject(subject);

            // 創建 HTML 郵件內容
            String htmlContent = createPasswordResetEmailContent(memberEmail, memberAccount, memberName, resetUrl);
            
            // 【關鍵修正】設定 HTML 內容，明確指定為 HTML 格式
            helper.setText(htmlContent, true);
            
            // 【新增】確保郵件編碼正確
            message.setHeader("Content-Type", "text/html; charset=UTF-8");

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
                        font-family: 'Microsoft JhengHei', 'Segoe UI', Arial, sans-serif;
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
                    /* 【修復】改善按鈕樣式，確保相容性 */
                    .reset-button {
                        display: inline-block;
                        background-color: #A67B5B !important;
                        color: white !important;
                        padding: 15px 30px;
                        text-decoration: none !important;
                        border-radius: 5px;
                        font-weight: bold;
                        margin: 20px 0;
                        text-align: center;
                        font-size: 16px;
                        border: none;
                        cursor: pointer;
                        /* 【新增】確保按鈕在各種郵件客戶端中都能正常顯示 */
                        -webkit-text-size-adjust: none;
                        -ms-text-size-adjust: none;
                        mso-line-height-rule: exactly;
                    }
                    .reset-button:hover {
                        background-color: #8C684A !important;
                    }
                    /* 【新增】為Outlook等郵件客戶端提供額外支援 */
                    .reset-button:visited {
                        color: white !important;
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
                        font-family: 'Courier New', monospace;
                        margin: 10px 0;
                        font-size: 14px;
                    }
                    /* 【新增】確保在深色模式下也能正常顯示 */
                    @media (prefers-color-scheme: dark) {
                        .reset-button {
                            background-color: #A67B5B !important;
                            color: white !important;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🍳 EatFast早安通</h1>
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
                                <li>請勿將此連結分享給他人</li>
                            </ul>
                        </div>
                        
                        <div style="text-align: center; margin: 30px 0;">
                            <p style="font-size: 18px; margin-bottom: 20px;"><strong>請點擊以下按鈕重設密碼：</strong></p>
                            <!-- 【修復】使用表格佈局確保按鈕相容性 -->
                            <table cellpadding="0" cellspacing="0" border="0" style="margin: 0 auto;">
                                <tr>
                                    <td style="background-color: #A67B5B; border-radius: 5px; padding: 0;">
                                        <a href="%s" class="reset-button" style="display: block; color: white; text-decoration: none; padding: 15px 30px; font-weight: bold; font-size: 16px;">
                                            🔐 立即重設密碼
                                        </a>
                                    </td>
                                </tr>
                            </table>
                        </div>
                        
                        <p style="margin-top: 30px;"><strong>如果按鈕無法點擊，請複製以下網址到瀏覽器中開啟：</strong></p>
                        <div class="url-box">
                            <a href="%s" style="color: #A67B5B; text-decoration: none;">%s</a>
                        </div>
                        
                        <div class="warning" style="margin-top: 30px;">
                            <strong>🛡️ 安全提醒：</strong>
                            <p>為了您的帳號安全，建議設定強密碼：</p>
                            <ul>
                                <li>至少8個字元，包含大小寫字母、數字和特殊符號</li>
                                <li>不要使用容易猜到的個人資訊</li>
                                <li>定期更換密碼</li>
                            </ul>
                        </div>
                    </div>
                    
                    <div class="footer">
                        <p>此郵件由系統自動發送，請勿直接回覆</p>
                        <p>如有疑問，請聯繫系統管理員</p>
                        <p>&copy; 2025 【EATFAST早安通】 - 早餐美味，服務貼心</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            memberAccount,  // 第1個 %s：會員帳號
            memberName,     // 第2個 %s：會員姓名
            memberEmail,    // 第3個 %s：會員信箱
            currentTime,    // 第4個 %s：請求時間
            resetUrl,       // 第5個 %s：重設連結（按鈕）
            resetUrl,       // 第6個 %s：重設連結（網址框內的連結）
            resetUrl,       // 第7個 %s：重設連結（網址框顯示的文字）
            systemName      // 第8個 %s：系統名稱（頁尾）
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