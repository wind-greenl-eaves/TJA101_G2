package com.eatfast.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
// [修正] 匯入正確的完整路徑參考
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 設定檔
 * @Configuration - 標記此類別為 Spring 的設定檔。
 * @EnableWebSecurity - 啟用 Spring Security 的 Web 安全性功能。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder myPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain myApiFilterChain(HttpSecurity http) throws Exception {
        
        // CSRF 設定 (此部分原先正確，保持不變)
        http.csrf(new Customizer<CsrfConfigurer<HttpSecurity>>() {
            @Override
            public void customize(CsrfConfigurer<HttpSecurity> csrf) {
                csrf.disable(); 
            }
        });

        // [修正] 使用完整、正確的泛型路徑來定義匿名函式
        http.authorizeHttpRequests(new Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry>() {
            @Override
            public void customize(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
                // 現在編譯器能正確識別 auth 的型別，因此 .anyRequest().permitAll() 可以被正確解析
                auth.anyRequest().permitAll();
            }
        });

        return http.build();
    }
}