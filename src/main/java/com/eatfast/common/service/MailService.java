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
            // 修復亂碼問題：使用字符串連接而非 String.format()
            String subject = "【早餐店管理系統】密碼重設通知";
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
            // 指定編碼為 UTF-8，並啟用多部分郵件支援
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
        
        // 修復亂碼問題：使用 StringBuilder 而非 String.format()
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<!DOCTYPE html>\n")
            .append("<html lang=\"zh-TW\">\n")
            .append("<head>\n")
            .append("    <meta charset=\"UTF-8\">\n")
            .append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
            .append("    <title>密碼重設通知</title>\n")
            .append("    <style>\n")
            .append("        body {\n")
            .append("            font-family: 'Microsoft JhengHei', Arial, sans-serif;\n")
            .append("            line-height: 1.6;\n")
            .append("            color: #333;\n")
            .append("            max-width: 600px;\n")
            .append("            margin: 0 auto;\n")
            .append("            padding: 20px;\n")
            .append("            background-color: #f5f5f5;\n")
            .append("        }\n")
            .append("        .container {\n")
            .append("            background-color: #ffffff;\n")
            .append("            border-radius: 8px;\n")
            .append("            padding: 30px;\n")
            .append("            box-shadow: 0 2px 10px rgba(0,0,0,0.1);\n")
            .append("        }\n")
            .append("        .header {\n")
            .append("            text-align: center;\n")
            .append("            border-bottom: 2px solid #A67B5B;\n")
            .append("            padding-bottom: 20px;\n")
            .append("            margin-bottom: 30px;\n")
            .append("        }\n")
            .append("        .header h1 {\n")
            .append("            color: #A67B5B;\n")
            .append("            margin: 0;\n")
            .append("            font-size: 24px;\n")
            .append("        }\n")
            .append("        .content {\n")
            .append("            margin-bottom: 30px;\n")
            .append("        }\n")
            .append("        .password-box {\n")
            .append("            background-color: #f8f9fa;\n")
            .append("            border: 2px solid #A67B5B;\n")
            .append("            border-radius: 6px;\n")
            .append("            padding: 15px;\n")
            .append("            text-align: center;\n")
            .append("            margin: 20px 0;\n")
            .append("        }\n")
            .append("        .password {\n")
            .append("            font-size: 24px;\n")
            .append("            font-weight: bold;\n")
            .append("            color: #A67B5B;\n")
            .append("            font-family: 'Courier New', monospace;\n")
            .append("            letter-spacing: 2px;\n")
            .append("        }\n")
            .append("        .warning {\n")
            .append("            background-color: #fff3cd;\n")
            .append("            border: 1px solid #ffeaa7;\n")
            .append("            border-radius: 4px;\n")
            .append("            padding: 15px;\n")
            .append("            margin: 20px 0;\n")
            .append("        }\n")
            .append("        .footer {\n")
            .append("            border-top: 1px solid #ddd;\n")
            .append("            padding-top: 20px;\n")
            .append("            text-align: center;\n")
            .append("            font-size: 12px;\n")
            .append("            color: #666;\n")
            .append("        }\n")
            .append("        .info-table {\n")
            .append("            width: 100%;\n")
            .append("            border-collapse: collapse;\n")
            .append("            margin: 20px 0;\n")
            .append("        }\n")
            .append("        .info-table td {\n")
            .append("            padding: 8px;\n")
            .append("            border-bottom: 1px solid #eee;\n")
            .append("        }\n")
            .append("        .info-table .label {\n")
            .append("            font-weight: bold;\n")
            .append("            width: 120px;\n")
            .append("            color: #555;\n")
            .append("        }\n")
            .append("    </style>\n")
            .append("</head>\n")
            .append("<body>\n")
            .append("    <div class=\"container\">\n")
            .append("        <div class=\"header\">\n")
            .append("            <h1>🔑 【早餐店管理系統】").append("專題使用").append("</h1>\n")
            .append("            <p>密碼重設通知</p>\n")
            .append("        </div>\n")
            .append("        \n")
            .append("        <div class=\"content\">\n")
            .append("            <p>親愛的 <strong>").append(employeeName).append("</strong> 您好，</p>\n")
            .append("            \n")
            .append("            <p>我們收到了您的密碼重設請求。為了確保您的帳戶安全，系統已為您生成一組新的臨時密碼。</p>\n")
            .append("            \n")
            .append("            <table class=\"info-table\">\n")
            .append("                <tr>\n")
            .append("                    <td class=\"label\">員工姓名：</td>\n")
            .append("                    <td>").append(employeeName).append("</td>\n")
            .append("                </tr>\n")
            .append("                <tr>\n")
            .append("                    <td class=\"label\">登入帳號：</td>\n")
            .append("                    <td>").append(employeeAccount).append("</td>\n")
            .append("                </tr>\n")
            .append("                <tr>\n")
            .append("                    <td class=\"label\">重設時間：</td>\n")
            .append("                    <td>").append(currentTime).append("</td>\n")
            .append("                </tr>\n")
            .append("            </table>\n")
            .append("            \n")
            .append("            <div class=\"password-box\">\n")
            .append("                <p style=\"margin: 0; font-size: 16px;\">您的新臨時密碼為：</p>\n")
            .append("                <div class=\"password\">").append(temporaryPassword).append("</div>\n")
            .append("            </div>\n")
            .append("            \n")
            .append("            <div class=\"warning\">\n")
            .append("                <h4 style=\"margin-top: 0; color: #856404;\">⚠️ 重要提醒</h4>\n")
            .append("                <ul style=\"margin-bottom: 0;\">\n")
            .append("                    <li>請立即使用此臨時密碼登入系統</li>\n")
            .append("                    <li>登入後請儘快修改為您的個人密碼</li>\n")
            .append("                    <li>請勿將此密碼透露給他人</li>\n")
            .append("                    <li>如果這不是您本人的操作，請立即聯繫系統管理員</li>\n")
            .append("                </ul>\n")
            .append("            </div>\n")
            .append("            \n")
            .append("            <p>如有任何問題，請聯繫系統管理員。</p>\n")
            .append("            \n")
            .append("            <p>感謝您的使用！<br>\n")
            .append("            ").append("EatFast").append(" 團隊</p>\n")
            .append("        </div>\n")
            .append("        \n")
            .append("        <div class=\"footer\">\n")
            .append("            <p>此郵件由系統自動發送，請勿直接回覆。</p>\n")
            .append("            <p>發送時間：").append(currentTime).append("</p>\n")
            .append("        </div>\n")
            .append("    </div>\n")
            .append("</body>\n")
            .append("</html>");
        
        return htmlBuilder.toString();
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