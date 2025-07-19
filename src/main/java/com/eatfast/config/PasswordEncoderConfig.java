package com.eatfast.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * [類別名稱]: PasswordEncoderConfig
 * 密碼編碼器配置類，獨立於 SecurityConfig 以避免循環依賴
 * 
 * 分離原因：
 * - SecurityConfig 依賴 EmployeePermissionEvaluator
 * - EmployeePermissionEvaluator 依賴 EmployeeService
 * - EmployeeService 依賴 PasswordEncoder
 * - 如果 PasswordEncoder 在 SecurityConfig 中定義，會形成循環依賴
 * 
 * 解決方案：
 * - 將 PasswordEncoder 獨立成一個配置類
 * - 讓 SecurityConfig 和 EmployeeService 都可以注入 PasswordEncoder
 * - 打破循環依賴鏈
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * [方法名稱]: passwordEncoder
     * [說明]: 註冊一個 PasswordEncoder Bean，供整個應用程式注入並用於密碼加密。
     * - @Bean: 將此方法的返回物件交由 Spring 容器管理。
     * - BCryptPasswordEncoder: 採用業界推薦的 BCrypt 強雜湊演算法。
     * @return 一個 PasswordEncoder 的實例。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}