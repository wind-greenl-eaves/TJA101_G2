/*
 * ================================================================
 * 檔案: 認證控制層 (AuthController)
 * ================================================================
 * - 存放位置：src/main/java/com/eatfast/auth/controller/AuthController.java
 * - 主要功能：處理所有與認證相關的請求，包括：
 *   1. 登入頁面的顯示
 *   2. 登出功能的處理
 */
package com.eatfast.auth.controller;

// 【Spring 框架相關】引入必要的 Spring 類別
import org.springframework.http.ResponseEntity;    // 用於 RESTful API 響應
import org.springframework.stereotype.Controller;   // 標記這是一個控制器
import org.springframework.web.bind.annotation.GetMapping;     // 處理 GET 請求
import org.springframework.web.bind.annotation.PostMapping;    // 處理 POST 請求
import org.springframework.web.bind.annotation.RequestMapping; // 設定基礎 URL 路徑
import org.springframework.web.bind.annotation.ResponseBody;   // 標記直接返回數據而非視圖

// 【Jakarta EE 相關】處理 HTTP 請求與 Session
import jakarta.servlet.http.HttpServletRequest;  // 處理 HTTP 請求
import jakarta.servlet.http.HttpSession;         // 管理用戶 Session

/**
 * 認證控制器：處理所有與用戶認證相關的請求
 * 
 * @Controller: 標記這是一個 Spring MVC 控制器
 * @RequestMapping("/api/v1/auth"): 設定此控制器的基礎 URL 路徑
 * - 完整 URL 示例：http://localhost:8080/api/v1/auth/login
 */
@Controller
@RequestMapping("/api/v1/auth")
public class AuthController {

    /**
     * 處理登出請求
     * 
     * 路徑說明：
     * - URL: POST /api/v1/auth/logout
     * - 完整 URL: http://localhost:8080/api/v1/auth/logout
     * 
     * 功能說明：
     * 1. 接收登出請求
     * 2. 清除用戶的 Session
     * 3. 返回成功響應
     * 
     * @ResponseBody: 直接返回響應體，不進行視圖解析
     * @param request HTTP 請求對象，用於獲取 Session
     * @return ResponseEntity<Void> 空響應體，狀態碼 200 表示成功
     */
    @PostMapping("/logout")
    @ResponseBody
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        // 獲取當前 Session（如果存在）
        // false 參數表示：如果 Session 不存在，則返回 null 而不是創建新的
        HttpSession session = request.getSession(false);
        
        // 如果 Session 存在，則使其失效（清除所有 Session 數據）
        if (session != null) {
            session.invalidate();
        }
        
        // 返回 HTTP 200 OK 響應
        return ResponseEntity.ok().build();
    }

    /**
     * 顯示登入頁面
     * 
     * 路徑說明：
     * - URL: GET /api/v1/auth/login
     * - 完整 URL: http://localhost:8080/api/v1/auth/login
     * - 視圖路徑: src/main/resources/templates/auth/login.html
     * 
     * 視圖解析說明：
     * 1. 返回字符串 "auth/login"
     * 2. Thymeleaf 視圖解析器會：
     *    - 在 src/main/resources/templates/ 目錄下
     *    - 尋找 auth/login.html 文件
     *    - 將其解析為完整的 HTML 頁面返回給用戶
     * 
     * @return String 視圖名稱，會被解析到 templates/auth/login.html
     */
    @GetMapping("/login")
    public String loginPage() {
        // 返回視圖名稱，會被 Thymeleaf 解析
        // 實際檔案位置：src/main/resources/templates/auth/login.html
        return "auth/login";
    }
}