package com.eatfast.config;

import com.eatfast.employee.security.EmployeePermissionEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * [類別名稱]: SecurityConfig
 * Spring Security 的核心設定檔，集中管理所有安全性相關的配置。
 *
 * - @Configuration: 標記此類別為 Spring 設定檔。
 * - @EnableWebSecurity: 啟用 Spring Security 的 Web 安全性功能。
 * - @EnableMethodSecurity: 啟用方法級安全性，支援 @PreAuthorize, @PostAuthorize 等註解
 * 
 * 注意：PasswordEncoder 已移至 PasswordEncoderConfig 類中，避免循環依賴
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private EmployeePermissionEvaluator permissionEvaluator;

    /**
     * 配置方法級安全性表達式處理器
     * 註冊自定義的權限評估器，支援複雜的權限邏輯
     */
    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(permissionEvaluator);
        return expressionHandler;
    }

    /**
     * [方法名稱]: securityFilterChain
     * [說明]: 設定 HTTP 請求的安全過濾規則鏈，定義哪些路徑需要被保護、哪些可以公開存取。
     * 這個方法整合了原本兩個分散的 filter chain 設定。
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
                        // 允許對首頁、測試、會員功能、API認證路徑的公開存取
                        .requestMatchers("/", "/testMember1", "/testEmployee", "/member/**", "/cart", "/api/v1/auth/**").permitAll()
                        // ★★★ 合併規則 ★★★: 設定 /feedback/ 路徑下的所有請求都需要 "ADMIN" 角色
                
                        // 【開發階段設定】暫時允許所有其他未明確指定的請求。
                        // 【警告】在正式上線前，應將此行改為 .anyRequest().authenticated()，強制所有其他請求都需登入驗證。
                        .anyRequest().permitAll()
                )
                // 步驟 2: 設定表單登入 (Form Login)
                // 如果你需要一個登入頁面，可以取消註解以下這行。
                // .formLogin(form -> form.loginPage("/login").permitAll())

                // 步驟 3: 設定登出處理 (Logout)
                .logout(logout -> logout
                        .logoutUrl("/logout")  // 設定觸發登出的 URL
                        .logoutSuccessUrl("/api/v1/auth/member-login?message=logout_success")  // 登出成功後重定向的 URL
                        .invalidateHttpSession(true)  // 登出時讓 HttpSession 失效
                        .deleteCookies("JSESSIONID")  // 登出時刪除 JSESSIONID cookie
                )

                // 步驟 4: 關閉 CSRF (跨站請求偽造) 保護。
                // 說明: 對於無狀態的 RESTful API，通常會關閉 CSRF。
                // 如果你的應用程式是傳統的網頁應用程式，建議開啟並進行正確設定。
                .csrf(csrf -> csrf.disable());

        // 步驟 5: 建構並返回設定好的過濾鏈
        return http.build();
    }
}