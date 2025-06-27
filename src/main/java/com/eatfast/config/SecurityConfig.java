package com.eatfast.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * <h1>Spring Security 安全組態設定</h1>
 * <p>
 * 職責：
 * <ul>
 * <li><b>定義密碼編碼器 (PasswordEncoder)</b>：註冊一個 BCrypt 編碼器的 Bean。</li>
 * <li><b>設定 HTTP 安全規則 (SecurityFilterChain)</b>：定義 URL 的存取權限。</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * <h2>定義密碼編碼器 Bean</h2>
     * <p>
     * <b>核心功能</b>：
     * 選擇 BCrypt 演算法作為密碼的雜湊標準。
     * <p>
     * <b>命名慣例</b>:
     * <ul>
     * <li><b>可自定義</b>: <code>passwordEncoder</code> (方法名稱)</li>
     * <li><b>不可變</b>: <code>@Bean</code>, <code>public</code>, <code>PasswordEncoder</code> (Spring 關鍵字與 Java 語法)</li>
     * </ul>
     * @return 一個 BCryptPasswordEncoder 的實例。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * <h2>設定 HTTP 安全過濾鏈 (Lambda DSL 風格)</h2>
     * <p>
     * <b>核心功能</b>：
     * 使用新版的 Lambda DSL 風格進行設定，不再需要 .and() 方法。
     * <p>
     * <b>本次範例設定說明</b>：
     * 為確保您現有的會員功能 (路徑: <code>/member/**</code>) 能正常運作，
     * 我們暫時將所有請求都設定為允許存取，並關閉 CSRF 保護以便表單能順利提交。
     * <p>
     * <b>命名慣例</b>:
     * <ul>
     * <li><b>可自定義</b>: <code>securityFilterChain</code> (方法名稱), <code>http</code> (參數名稱)</li>
     * <li><b>不可變</b>: <code>@Bean</code>, <code>public</code>, <code>SecurityFilterChain</code>, <code>HttpSecurity</code> (Spring 關鍵字與 Java 語法)</li>
     * </ul>
     * @param http HttpSecurity 物件，由 Spring Security 傳入。
     * @return 一個設定好的 SecurityFilterChain。
     * @throws Exception 可能拋出的異常。
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 步驟1：開始設定授權規則
            .authorizeHttpRequests(authorize -> authorize
                // 步驟1.1：定義可公開存取的路徑
                // requestMatchers 是不可變的框架方法
                // "/css/**", "/member/**" 等是您專案中定義的 URL 路徑，需要完全匹配
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                .requestMatchers("/member/**").permitAll()
                .requestMatchers("/").permitAll()
                // 步驟1.2：其他任何請求暫時都允許存取 (在正式環境應改為 .authenticated())
                .anyRequest().permitAll()
            )
            // 步驟2：【已修正】直接設定 CSRF，不再需要 .and()
            .csrf(csrf -> csrf.disable());

        // 步驟3：建構並返回 SecurityFilterChain 物件
        return http.build();
    }
}
