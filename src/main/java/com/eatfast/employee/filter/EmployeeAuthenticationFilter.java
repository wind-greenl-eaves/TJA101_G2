package com.eatfast.employee.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 員工模組登入狀態檢查過濾器
 * 
 * 功能說明：
 * - 專門針對員工模組的所有請求進行登入狀態檢查
 * - 未登入的使用者將被自動重定向至員工登入頁面
 * - 支援 Session 超時檢測和友善的錯誤訊息
 * - 排除不需要驗證的公開路徑（如登入頁面、忘記密碼等）
 * 
 * 適用路徑：/employee/** (除了排除的公開路徑)
 */
@WebFilter(urlPatterns = "/employee/*")
public class EmployeeAuthenticationFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(EmployeeAuthenticationFilter.class);

    // 不需要驗證的公開路徑
    private static final String[] EXCLUDED_PATHS = {
        "/employee/login",              // 登入頁面
        "/employee/forgot-password",    // 忘記密碼頁面
        "/employee/reset-password"      // 重設密碼頁面（如果有的話）
    };

    // API 路徑前綴
    private static final String API_PREFIX = "/api/v1/employees";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("員工認證過濾器初始化完成");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = requestURI.substring(contextPath.length());

        log.debug("員工認證過濾器處理請求: {}", path);

        // 檢查是否為需要排除的路徑
        if (isExcludedPath(path)) {
            log.debug("路徑 {} 無需驗證，直接通過", path);
            chain.doFilter(request, response);
            return;
        }

        // 檢查是否為員工模組相關路徑
        if (!isEmployeeModulePath(path)) {
            log.debug("路徑 {} 不屬於員工模組，直接通過", path);
            chain.doFilter(request, response);
            return;
        }

        // 進行登入狀態檢查
        HttpSession session = httpRequest.getSession(false);
        Object loggedInEmployee = null;
        
        if (session != null) {
            loggedInEmployee = session.getAttribute("loggedInEmployee");
        }

        if (loggedInEmployee == null) {
            log.info("未登入使用者嘗試存取員工模組: {} - IP: {}", 
                path, getClientIP(httpRequest));
            
            handleUnauthenticatedRequest(httpRequest, httpResponse, path);
            return;
        }

        // 檢查 Session 是否即將過期（提前5分鐘警告）
        if (session != null) {
            long lastAccessTime = session.getLastAccessedTime();
            long currentTime = System.currentTimeMillis();
            long sessionTimeout = session.getMaxInactiveInterval() * 1000L; // 轉換為毫秒
            long timeElapsed = currentTime - lastAccessTime;
            
            if (timeElapsed > (sessionTimeout - 300000)) { // 提前5分鐘警告
                log.warn("員工 Session 即將過期 - 使用者: {}, 剩餘時間: {} 分鐘", 
                    session.getAttribute("employeeName"), 
                    (sessionTimeout - timeElapsed) / 60000);
            }
        }

        log.debug("員工 {} 通過認證檢查，存取路徑: {}", 
            session.getAttribute("employeeName"), path);

        // 更新 Session 的最後存取時間
        if (session != null) {
            session.setAttribute("lastAccessPath", path);
            session.setAttribute("lastAccessTime", System.currentTimeMillis());
        }

        // 通過驗證，繼續處理請求
        chain.doFilter(request, response);
    }

    /**
     * 檢查路徑是否需要排除驗證
     */
    private boolean isExcludedPath(String path) {
        for (String excludedPath : EXCLUDED_PATHS) {
            if (path.equals(excludedPath) || path.startsWith(excludedPath + "/")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 檢查是否為員工模組相關路徑
     */
    private boolean isEmployeeModulePath(String path) {
        return path.startsWith("/employee/") || path.startsWith(API_PREFIX);
    }

    /**
     * 處理未認證的請求
     */
    private void handleUnauthenticatedRequest(HttpServletRequest request, 
                                            HttpServletResponse response, 
                                            String requestedPath) throws IOException {
        
        String contextPath = request.getContextPath();
        
        // 判斷是否為 AJAX/API 請求
        if (isAjaxRequest(request) || requestedPath.startsWith(API_PREFIX)) {
            // API 請求返回 JSON 錯誤響應
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                "{\"error\":\"AUTHENTICATION_REQUIRED\"," +
                "\"message\":\"請重新登入\"," +
                "\"redirectUrl\":\"" + contextPath + "/employee/login\"}"
            );
            log.info("API 請求未認證，返回 401 錯誤: {}", requestedPath);
        } else {
            // 網頁請求重定向到登入頁面
            String loginUrl = contextPath + "/employee/login";
            
            // 添加超時訊息和原始請求路徑
            StringBuilder redirectUrl = new StringBuilder(loginUrl);
            redirectUrl.append("?timeout=true");
            
            // 編碼錯誤訊息
            String timeoutMessage = "請正常登入系統，重新操作";
            String encodedMessage = URLEncoder.encode(timeoutMessage, StandardCharsets.UTF_8);
            redirectUrl.append("&message=").append(encodedMessage);
            
            // 保存原始請求路徑（用於登入後重定向）
            if (!requestedPath.equals("/employee/select_page")) {
                String encodedPath = URLEncoder.encode(requestedPath, StandardCharsets.UTF_8);
                redirectUrl.append("&returnUrl=").append(encodedPath);
            }
            
            log.info("重定向未認證使用者到登入頁面: {} -> {}", requestedPath, redirectUrl);
            response.sendRedirect(redirectUrl.toString());
        }
    }

    /**
     * 判斷是否為 AJAX 請求
     */
    private boolean isAjaxRequest(HttpServletRequest request) {
        String xRequestedWith = request.getHeader("X-Requested-With");
        String contentType = request.getContentType();
        String accept = request.getHeader("Accept");
        
        return "XMLHttpRequest".equals(xRequestedWith) ||
               (contentType != null && contentType.contains("application/json")) ||
               (accept != null && accept.contains("application/json"));
    }

    /**
     * 獲取客戶端 IP 地址
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty() && !"unknown".equalsIgnoreCase(xRealIP)) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }

    @Override
    public void destroy() {
        log.info("員工認證過濾器銷毀");
    }
}
