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
import org.springframework.validation.BindingResult;           // 表單驗證結果
import org.springframework.validation.annotation.Validated;   // 驗證註解
import org.springframework.web.bind.annotation.ModelAttribute; // 模型屬性綁定

// 【Jakarta EE 相關】處理 HTTP 請求與 Session
import jakarta.servlet.http.HttpServletRequest;  // 處理 HTTP 請求
import jakarta.servlet.http.HttpSession;         // 管理用戶 Session
import jakarta.persistence.EntityNotFoundException; // JPA 實體未找到異常

// 【會員系統相關】引入會員相關的類別
import com.eatfast.member.service.MemberService;  // 會員業務邏輯服務
import com.eatfast.member.dto.MemberUpdateRequest;
import com.eatfast.member.dto.ForgotPasswordRequest;
import com.eatfast.member.dto.ResetPasswordRequest;
import com.eatfast.member.model.MemberEntity;      // 會員實體類
import org.springframework.security.crypto.password.PasswordEncoder;  // 密碼加密器
import org.springframework.ui.Model;               // 用於傳遞資料到視圖
import org.springframework.web.bind.annotation.RequestParam;  // 獲取請求參數
import org.springframework.web.servlet.mvc.support.RedirectAttributes;  // 重定向屬性
import org.springframework.web.context.request.RequestContextHolder;  // 用於獲取當前請求上下文

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
     * 【依賴注入】會員服務和密碼加密器
     * - 使用 private final 確保服務在建構後不可變，符合最佳實踐
     */
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 【建構子注入】透過建構子注入必要的服務
     * Spring 容器會自動完成依賴注入
     */
    public AuthController(MemberService memberService, PasswordEncoder passwordEncoder) {
        this.memberService = memberService;
        this.passwordEncoder = passwordEncoder;
    }

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
    
    /**
     * 顯示會員登入頁面
     * 
     * 路徑說明：
     * - URL: GET /api/v1/auth/member-login
     * - 完整 URL: http://localhost:8080/api/v1/auth/member-login
     * - 視圖路徑: src/main/resources/templates/front-end/member/member-login.html
     * 
     * 視圖解析說明：
     * 1. 返回字符串 "front-end/member/member-login"
     * 2. Thymeleaf 視圖解析器會：
     *    - 在 src/main/resources/templates/ 目錄下
     *    - 尋找 front-end/member/member-login.html 文件
     *    - 將其解析為完整的 HTML 頁面返回給用戶
     * 
     * @param model 用於傳遞資料到視圖的模型對象
     * @return String 視圖名稱，會被解析到 templates/front-end/member/member-login.html
     */
    @GetMapping("/member-login")
    public String memberLoginPage(Model model) {
        // 在開發環境中顯示測試帳號 (可以透過配置來控制)
        model.addAttribute("showDemoAccounts", true);
        
        // 返回會員專用的登入頁面
        return "front-end/member/member-login";
    }
    
    /**
     * 處理會員登入請求 - 增強調試版
     * 
     * 路徑說明：
     * - URL: POST /api/v1/auth/process-login
     * - 完整 URL: http://localhost:8080/api/v1/auth/process-login
     */
    @PostMapping("/process-login")
    public String processLogin(@RequestParam("account") String account,
                              @RequestParam("password") String password,
                              @RequestParam(value = "rememberMe", required = false) boolean rememberMe,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        
        // 【調試日誌】記錄登入嘗試
        System.out.println("🔍 登入嘗試 - 帳號: " + account);
        System.out.println("🔍 密碼長度: " + (password != null ? password.length() : "null"));
        
        try {
            // 【第一步：資料驗證】
            if (account == null || account.trim().isEmpty()) {
                System.out.println("❌ 登入失敗: 帳號為空");
                redirectAttributes.addFlashAttribute("loginError", "請輸入帳號");
                return "redirect:/api/v1/auth/member-login";
            }
            
            if (password == null || password.trim().isEmpty()) {
                System.out.println("❌ 登入失敗: 密碼為空");
                redirectAttributes.addFlashAttribute("loginError", "請輸入密碼");
                return "redirect:/api/v1/auth/member-login";
            }
            
            // 【第二步：查詢會員資料】
            System.out.println("🔍 開始查詢會員: " + account.trim());
            // 【修正】使用 getMemberByAccountIncludeDisabled 來查詢包括停權會員在內的所有會員
            var memberOptional = memberService.getMemberByAccountIncludeDisabled(account.trim());
            
            if (memberOptional.isEmpty()) {
                System.out.println("❌ 會員不存在: " + account);
                redirectAttributes.addFlashAttribute("loginError", "帳號或密碼錯誤");
                redirectAttributes.addFlashAttribute("account", account);
                return "redirect:/api/v1/auth/member-login";
            }
            
            MemberEntity member = memberOptional.get();
            System.out.println("✅ 找到會員: " + member.getUsername() + " (ID: " + member.getMemberId() + ")");
            System.out.println("🔍 會員狀態: " + (member.isEnabled() ? "啟用" : "停權"));
            
            // 【第三步：檢查帳號狀態】
            if (!member.isEnabled()) {
                System.out.println("❌ 帳號已停權: " + account);
                redirectAttributes.addFlashAttribute("loginError", "此帳號已停權，請連絡EatFast");
                redirectAttributes.addFlashAttribute("account", account);
                return "redirect:/api/v1/auth/member-login";
            }
            
            // 【第四步：密碼驗證 - 增強調試 + 相容性處理】
            System.out.println("🔍 開始密碼驗證...");
            System.out.println("🔍 資料庫密碼長度: " + member.getPassword().length());
            System.out.println("🔍 資料庫密碼前綴: " + (member.getPassword().length() > 10 ? 
                member.getPassword().substring(0, 10) : member.getPassword()));
            
            boolean passwordMatches = false;
            
            // 【智慧密碼驗證】根據密碼格式自動選擇驗證方式
            if (member.getPassword().startsWith("$2a$") || 
                member.getPassword().startsWith("$2b$") || 
                member.getPassword().startsWith("$2y$")) {
                // BCrypt格式密碼 - 使用加密比對
                System.out.println("🔍 偵測到BCrypt格式，使用加密比對");
                passwordMatches = passwordEncoder.matches(password, member.getPassword());
            } else {
                // 明文密碼 - 直接比對（相容性處理）
                System.out.println("⚠️ 偵測到明文密碼，使用直接比對");
                passwordMatches = password.equals(member.getPassword());
                
                if (passwordMatches) {
                    System.out.println("✅ 明文密碼驗證成功");
                    System.out.println("💡 建議：登入成功後將密碼升級為BCrypt格式");
                    
                    // 【自動升級密碼】登入成功時自動將明文密碼升級為BCrypt
                    try {
                        String encryptedPassword = passwordEncoder.encode(password);
                        member.setPassword(encryptedPassword);
                        
                        // 【修正】正確創建 MemberUpdateRequest 對象
                        MemberUpdateRequest updateRequest = new MemberUpdateRequest();
                        updateRequest.setMemberId(member.getMemberId());
                        updateRequest.setUsername(member.getUsername());
                        updateRequest.setEmail(member.getEmail());
                        updateRequest.setPhone(member.getPhone());
                        updateRequest.setBirthday(member.getBirthday());
                        updateRequest.setGender(member.getGender());
                        updateRequest.setIsEnabled(member.isEnabled());
                        
                        memberService.updateMemberDetails(updateRequest);
                        System.out.println("✅ 密碼已自動升級為BCrypt格式");
                    } catch (Exception e) {
                        System.err.println("⚠️ 密碼升級失敗，但不影響登入：" + e.getMessage());
                    }
                }
            }
            
            System.out.println("🔍 密碼比對結果: " + passwordMatches);
            
            if (!passwordMatches) {
                System.out.println("❌ 密碼驗證失敗");
                
                redirectAttributes.addFlashAttribute("loginError", "帳號或密碼錯誤");
                redirectAttributes.addFlashAttribute("account", account);
                return "redirect:/api/v1/auth/member-login";
            }
            
            // 【第五步：建立登入 Session - 修正安全性】
            System.out.println("✅ 密碼驗證成功，建立 Session");
            
            // 【安全改進】重新獲取Session以防止Session固定攻擊
            session.invalidate();
            // 重新獲取新的Session
            HttpServletRequest request = 
                ((org.springframework.web.context.request.ServletRequestAttributes) 
                org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest();
            session = request.getSession(true); // 創建新的Session
            
            // 設定Session屬性
            session.setAttribute("loggedInMemberId", member.getMemberId());
            session.setAttribute("loggedInMemberAccount", member.getAccount());
            session.setAttribute("loggedInMemberName", member.getUsername());
            session.setAttribute("memberName", member.getUsername()); // 【新增】為前端模板提供一致的屬性名稱
            session.setAttribute("isLoggedIn", true);
            session.setAttribute("loginTime", System.currentTimeMillis());
            
            // 設定 Session 過期時間
            if (rememberMe) {
                session.setMaxInactiveInterval(30 * 24 * 60 * 60); // 30天
                System.out.println("🔍 Session 設定為 30 天");
            } else {
                session.setMaxInactiveInterval(2 * 60 * 60); // 2小時
                System.out.println("🔍 Session 設定為 2 小時");
            }
            
            // 【第六步：登入成功處理 - 確保重定向路徑正確】
            System.out.println("🎉 會員登入成功：" + member.getAccount() + " (" + member.getUsername() + ")");
            redirectAttributes.addFlashAttribute("successMessage", "歡迎回來，" + member.getUsername() + "！");
            
            // 【修正】確保重定向路徑與MemberController的路徑一致
            return "redirect:/member/dashboard";
            
        } catch (Exception e) {
            // 【異常處理】
            System.err.println("💥 登入處理過程中發生錯誤：" + e.getMessage());
            e.printStackTrace();
            
            redirectAttributes.addFlashAttribute("loginError", "系統錯誤，請稍後再試");
            redirectAttributes.addFlashAttribute("account", account);
            return "redirect:/api/v1/auth/member-login";
        }
    }
    
    /**
     * 處理會員中心重定向 - 增加此方法以處理登入後的跳轉
     * 
     * 路徑說明：
     * - URL: GET /api/v1/auth/member-center
     * - 完整 URL: http://localhost:8080/api/v1/auth/member-center
     * - 功能：作為登入成功後的中轉站，重定向到會員專區
     */
    @GetMapping("/member-center")
    public String memberCenter() {
        // 重定向到會員專區
        return "redirect:/member/dashboard";
    }
    
    /**
     * 顯示忘記密碼頁面
     * 
     * 路徑說明：
     * - URL: GET /api/v1/auth/forgot-password
     * - 完整 URL: http://localhost:8080/api/v1/auth/forgot-password
     * - 視圖路徑: src/main/resources/templates/front-end/member/forgot-password.html
     */
    @GetMapping("/forgot-password")
    public String showForgotPasswordPage(Model model) {
        model.addAttribute("forgotPasswordRequest", new ForgotPasswordRequest());
        return "front-end/member/forgot-password";
    }
    
    /**
     * 處理忘記密碼請求
     * 
     * 路徑說明：
     * - URL: POST /api/v1/auth/forgot-password
     * - 功能：接收會員的電子郵件，生成重設密碼連結
     */
    @PostMapping("/forgot-password")
    public String processForgotPassword(@Validated @ModelAttribute("forgotPasswordRequest") ForgotPasswordRequest request,
                                      BindingResult result,
                                      RedirectAttributes redirectAttributes,
                                      Model model) {
        
        // 檢查表單驗證錯誤
        if (result.hasErrors()) {
            return "front-end/member/forgot-password";
        }
        
        try {
            // 處理忘記密碼請求
            String resetToken = memberService.processForgotPassword(request);
            
            // 在開發環境中，我們直接顯示重設連結
            // 實際部署時應該透過郵件發送
            String resetUrl = "/api/v1/auth/reset-password?token=" + resetToken;
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "重設密碼連結已生成！在實際環境中會透過郵件發送給您。");
            redirectAttributes.addFlashAttribute("resetUrl", resetUrl);
            redirectAttributes.addFlashAttribute("showResetLink", true);
            
            return "redirect:/api/v1/auth/forgot-password-success";
            
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "front-end/member/forgot-password";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "front-end/member/forgot-password";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "系統錯誤，請稍後再試");
            return "front-end/member/forgot-password";
        }
    }
    
    /**
     * 顯示忘記密碼成功頁面
     */
    @GetMapping("/forgot-password-success")
    public String showForgotPasswordSuccessPage() {
        return "front-end/member/forgot-password-success";
    }
    
    /**
     * 顯示密碼重設頁面
     * 
     * 路徑說明：
     * - URL: GET /api/v1/auth/reset-password?token=xxx
     * - 功能：驗證 token 並顯示重設密碼表單
     */
    @GetMapping("/reset-password")
    public String showResetPasswordPage(@RequestParam("token") String token,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        
        try {
            // 這裡可以預先驗證 token 的格式
            if (token == null || token.trim().isEmpty()) {
                throw new IllegalArgumentException("無效的重設連結");
            }
            
            ResetPasswordRequest resetRequest = new ResetPasswordRequest();
            resetRequest.setToken(token);
            
            model.addAttribute("resetPasswordRequest", resetRequest);
            model.addAttribute("token", token);
            
            return "front-end/member/reset-password";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "重設連結無效或已過期，請重新申請");
            return "redirect:/api/v1/auth/forgot-password";
        }
    }
    
    /**
     * 處理密碼重設請求
     * 
     * 路徑說明：
     * - URL: POST /api/v1/auth/reset-password
     * - 功能：驗證新密碼並更新會員密碼
     */
    @PostMapping("/reset-password")
    public String processResetPassword(@Validated @ModelAttribute("resetPasswordRequest") ResetPasswordRequest request,
                                     BindingResult result,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        
        // 檢查表單驗證錯誤
        if (result.hasErrors()) {
            model.addAttribute("token", request.getToken());
            return "front-end/member/reset-password";
        }
        
        try {
            // 處理密碼重設
            memberService.processResetPassword(request);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "密碼重設成功！請使用新密碼登入。");
            
            return "redirect:/api/v1/auth/member-login";
            
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("token", request.getToken());
            return "front-end/member/reset-password";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "重設連結無效，請重新申請");
            return "redirect:/api/v1/auth/forgot-password";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "系統錯誤，請稍後再試");
            model.addAttribute("token", request.getToken());
            return "front-end/member/reset-password";
        }
    }
}
