package com.eatfast.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * [可自定義的類別名稱]: SecurityConfig
 * Spring Security 的核心設定檔，集中管理所有安全性相關的配置。
 *
 * - @Configuration: 標記此類別為 Spring 設定檔。
 * - @EnableWebSecurity: 啟用 Spring Security 的 Web 安全性功能。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * [可自定義的方法名稱]: passwordEncoder
     * [說明]: 註冊一個 PasswordEncoder Bean，供整個應用程式注入並用於密碼加密。
     * - @Bean: 將此方法的返回物件交由 Spring 容器管理。
     * - BCryptPasswordEncoder: 採用業界推薦的 BCrypt 強雜湊演算法。
     * @return 一個 PasswordEncoder 的實例。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * [可自定義的方法名稱]: securityFilterChain
     * [說明]: 設定 HTTP 請求的安全過濾規則鏈，定義哪些路徑需要被保護、哪些可以公開存取。
     * @param http HttpSecurity 物件，由 Spring Security 提供用於鏈式設定。
     * @return 一個設定完成的 SecurityFilterChain 物件。
     * @throws Exception 可能拋出的設定異常。
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 步驟 1: 設定請求授權規則 (Authorization)
            .authorizeHttpRequests(authorize -> authorize
                // 允許對靜態資源 (css, js 等) 的公開存取
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                // 允許對測試控制器、會員相關功能、首頁的公開存取
                .requestMatchers("/", "/testMember1", "/testEmployee", "/member/**").permitAll()
                // 【開發階段設定】暫時允許所有其他未明確指定的請求。
                // 【警告】在正式上線前，應將此行改為 .anyRequest().authenticated()，強制所有其他請求都需登入驗證。
                .anyRequest().permitAll()
            )
            // 步驟 2: 設定表單登入 (若需要)
            // .formLogin(form -> form.loginPage("/login").permitAll())
            
            // 步驟 3: 關閉 CSRF (跨站請求偽造) 保護。
            // 說明: 由於我們是開發無狀態的 RESTful API，或是在開發初期為了方便測試表單，
            // 會先將其關閉。若未來有狀態的網頁表單，則應開啟並正確配置。
            .csrf(csrf -> csrf.disable());

        // 步驟 4: 建構並返回設定好的過濾鏈
        return http.build();
    }
}
