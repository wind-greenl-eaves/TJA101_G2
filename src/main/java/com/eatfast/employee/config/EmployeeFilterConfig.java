package com.eatfast.employee.config;

import com.eatfast.employee.filter.EmployeeAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 員工模組過濾器配置
 * 
 * 功能說明：
 * - 註冊員工認證過濾器
 * - 設定過濾器的執行順序和適用路徑
 * - 確保過濾器在 Spring Security 之前執行
 */
@Configuration
public class EmployeeFilterConfig {

    /**
     * 建立員工認證過濾器實例
     */
    @Bean
    public EmployeeAuthenticationFilter employeeAuthenticationFilter() {
        return new EmployeeAuthenticationFilter();
    }

    /**
     * 註冊員工認證過濾器
     * 
     * @param employeeAuthFilter 員工認證過濾器實例
     * @return 過濾器註冊 Bean
     */
    @Bean
    public FilterRegistrationBean<EmployeeAuthenticationFilter> employeeAuthenticationFilterRegistration(
            EmployeeAuthenticationFilter employeeAuthFilter) {
        
        FilterRegistrationBean<EmployeeAuthenticationFilter> registrationBean = 
            new FilterRegistrationBean<>();
        
        registrationBean.setFilter(employeeAuthFilter);
        
        // 設定過濾器適用的 URL 模式
        registrationBean.addUrlPatterns(
            "/employee/*",           // 員工模組所有頁面
            "/api/v1/employees/*",   // 員工 API 端點
            "/member/*",             // 會員管理頁面
            "/back-end/member/*"     // 後台會員管理頁面
        );
        
        // 設定過濾器執行順序（數字越小優先級越高）
        // 確保在 Spring Security 過濾器之前執行
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        
        // 設定過濾器名稱
        registrationBean.setName("employeeAuthenticationFilter");
        
        return registrationBean;
    }
}