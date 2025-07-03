package com.eatfast.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * 郵件服務配置類
 * 專門處理郵件服務的編碼設定，確保中文字符正確顯示
 */
@Configuration
public class MailConfig {

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    /**
     * 配置 JavaMailSender Bean
     * 特別針對中文編碼進行優化
     */
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        // 基本 SMTP 設定
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        
        // 【關鍵】設定預設編碼為 UTF-8
        mailSender.setDefaultEncoding("UTF-8");
        
        // 配置 SMTP 屬性
        Properties props = mailSender.getJavaMailProperties();
        
        // SSL/TLS 設定
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.ssl.required", "true");
        props.put("mail.smtp.socketFactory.port", String.valueOf(port));
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        
        // 【關鍵】中文編碼相關設定 - 防止亂碼的核心配置
        props.put("mail.mime.charset", "UTF-8");
        props.put("mail.mime.splitlongparameters", "false");
        props.put("mail.smtp.sendpartial", "true");
        
        // 連線超時設定
        props.put("mail.smtp.timeout", "25000");
        props.put("mail.smtp.connectiontimeout", "25000");
        props.put("mail.smtp.writetimeout", "25000");
        
        // 【新增】防止編碼問題的額外設定
        props.put("mail.mime.encodefilename", "true");
        props.put("mail.mime.decodefilename", "true");
        props.put("mail.mime.encodeparameters", "true");
        props.put("mail.mime.decodeparameters", "true");
        
        // 【重要】強制使用 UTF-8 編碼傳輸
        props.put("mail.mime.multipart.allowempty", "true");
        props.put("mail.mime.multipart.ignoreexistingboundaryparameter", "true");
        
        // Debug 設定（開發環境使用）
        props.put("mail.debug", "false");
        
        return mailSender;
    }
}