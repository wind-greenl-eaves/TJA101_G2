package com.eatfast.employee.controller;

import com.eatfast.common.enums.AccountStatus;
import com.eatfast.common.exception.ResourceNotFoundException;
import com.eatfast.employee.dto.EmployeeDTO;
import com.eatfast.employee.dto.EmployeeLoginRequest;
import com.eatfast.employee.model.EmployeeEntity;
import com.eatfast.employee.service.EmployeeService;
import com.eatfast.employee.service.EmployeeAuthService;
import com.eatfast.employee.service.ForgotPasswordRateLimitService; // 新增
import com.eatfast.employee.util.EmployeeLogger;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest; // 新增
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 員工登入控制器 - 整合登入次數限制功能
 * 處理員工登入相關的頁面顯示和表單提交
 * 包含8次登入失敗自動鎖定帳號的安全機制
 */
@Controller
@RequestMapping("/employee") // 統一路徑前綴
public class EmployeeLoginController {

    private static final Logger log = LoggerFactory.getLogger(EmployeeLoginController.class);
    private static final int MAX_LOGIN_ATTEMPTS = 8; // 最大登入失敗次數
    
    private final EmployeeService employeeService;
    private final EmployeeAuthService employeeAuthService;
    private final ForgotPasswordRateLimitService rateLimitService;
    private final EmployeeLogger employeeLogger;

    @Autowired
    public EmployeeLoginController(EmployeeService employeeService, 
                                  EmployeeAuthService employeeAuthService,
                                  ForgotPasswordRateLimitService rateLimitService,
                                  EmployeeLogger employeeLogger) {
        this.employeeService = employeeService;
        this.employeeAuthService = employeeAuthService;
        this.rateLimitService = rateLimitService;
        this.employeeLogger = employeeLogger;
    }

    /**
     * 顯示員工登入頁面
     * 路徑: GET /employee/login
     * @RequestParam : 用於處理登出、超時、訊息和返回URL等參數
     */
    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "logout", required = false) String logout, 
                               @RequestParam(value = "shown", required = false) String shown,
                               @RequestParam(value = "timeout", required = false) String timeout,
                               @RequestParam(value = "message", required = false) String message,
                               @RequestParam(value = "returnUrl", required = false) String returnUrl,
                               Model model, RedirectAttributes redirectAttributes) {
        log.info("=== 員工登入頁面訪問開始 ===");
        
        // 準備登入表單物件
        model.addAttribute("loginRequest", new EmployeeLoginRequest());
        
        // 處理Session超時訊息 - 修正邏輯
        if ("true".equals(timeout)) {
            String timeoutMessage = "系統已自動登出，請重新操作"; // 預設訊息
            
            // 如果有自定義訊息，嘗試解碼
            if (message != null && !message.trim().isEmpty()) {
                try {
                    timeoutMessage = java.net.URLDecoder.decode(message, "UTF-8");
                    log.info("顯示Session超時訊息: {}", timeoutMessage);
                } catch (Exception e) {
                    log.warn("URL解碼失敗，使用預設訊息: {}", e.getMessage());
                }
            } else {
                log.info("檢測到timeout=true但無message參數，顯示預設超時訊息");
            }
            
            model.addAttribute("errorMessage", timeoutMessage);
        }
        
        // 保存原始請求路徑到 Session，用於登入成功後重定向
        if (returnUrl != null && !returnUrl.trim().isEmpty()) {
            try {
                String decodedReturnUrl = java.net.URLDecoder.decode(returnUrl, "UTF-8");
                model.addAttribute("returnUrl", decodedReturnUrl);
                log.info("保存登入後重定向路徑: {}", decodedReturnUrl);
            } catch (Exception e) {
                log.warn("解碼返回URL失敗: {}", e.getMessage());
            }
        }
        
        // 處理登出成功訊息 - 只在第一次顯示，避免重新整理時重複顯示
        if ("success".equals(logout) && !"true".equals(shown)) {
            // 使用 RedirectAttributes 來傳遞訊息，這樣重定向後訊息不會丟失
            redirectAttributes.addFlashAttribute("successMessage", "登出成功！感謝您的使用。");
            // 重定向到同一頁面但加上 shown=true 參數，避免重新整理時重複顯示訊息
            return "redirect:/employee/login?shown=true";
        }
        
        // 獲取所有啟用狀態的員工列表，用於管理員小幫手
        try {
            log.info("開始獲取啟用狀態的員工列表...");
            List<EmployeeDTO> activeEmployees = employeeService.findAllActiveEmployees();
            log.info("成功獲取啟用員工列表，數量: {}", activeEmployees != null ? activeEmployees.size() : 0);
            
            if (activeEmployees != null && !activeEmployees.isEmpty()) {
                model.addAttribute("employeeList", activeEmployees);
                log.info("已將員工列表添加到模型中，員工數量: {}", activeEmployees.size());
                
                // 記錄前幾個員工的基本信息用於調試
                for (int i = 0; i < Math.min(3, activeEmployees.size()); i++) {
                    EmployeeDTO emp = activeEmployees.get(i);
                    log.debug("員工[{}]: ID={}, 帳號={}, 姓名={}, 狀態={}", 
                             i+1, emp.getEmployeeId(), emp.getAccount(), emp.getUsername(), emp.getStatus());
                }
            } else {
                log.warn("沒有找到任何啟用狀態的員工");
                model.addAttribute("noActiveEmployees", true);
            }
        } catch (Exception e) {
            log.error("獲取員工列表時發生異常: {}", e.getMessage(), e);
            model.addAttribute("employeeListError", "無法載入員工列表: " + e.getMessage());
        }
        
        // 獲取所有已停權員工列表，用於管理員小幫手
        try {
            log.info("開始獲取已停權的員工列表...");
            List<EmployeeDTO> inactiveEmployees = employeeService.findAllInactiveEmployees();
            log.info("成功獲取已停權員工列表，數量: {}", inactiveEmployees != null ? inactiveEmployees.size() : 0);
            
            if (inactiveEmployees != null && !inactiveEmployees.isEmpty()) {
                model.addAttribute("inactiveEmployeeList", inactiveEmployees);
                log.info("已將已停權員工列表添加到模型中，員工數量: {}", inactiveEmployees.size());
            } else {
                log.info("沒有找到任何已停權的員工");
            }
        } catch (Exception e) {
            log.error("獲取已停權員工列表時發生異常: {}", e.getMessage(), e);
            model.addAttribute("inactiveEmployeeListError", "無法載入已停權員工列表: " + e.getMessage());
        }
        
        log.info("=== 員工登入頁面準備完成，返回視圖 ===");
        return "back-end/employee/login";
    }

    /**
     * 處理員工登入表單提交 - 整合登入次數限制功能
     * 路徑: POST /employee/login
     */
    @PostMapping("/login")
    public String processLogin(@Valid @ModelAttribute("loginRequest") EmployeeLoginRequest loginRequest,
                              BindingResult bindingResult,
                              @RequestParam(value = "returnUrl", required = false) String returnUrl,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        
        log.info("🔍 員工登入嘗試 - 帳號: {}", loginRequest.getAccount());
        
        // 【第一步：表單驗證】
        if (bindingResult.hasErrors()) {
            log.warn("❌ 登入表單驗證失敗 - 帳號: {}", loginRequest.getAccount());
            prepareLoginPageModel(model, returnUrl);
            return "back-end/employee/login";
        }
        
        try {
            // 【第二步：帳號存在性檢查】
            EmployeeEntity employee = employeeAuthService.findEmployeeByAccount(loginRequest.getAccount());
            if (employee == null) {
                log.warn("❌ 員工帳號不存在 - 帳號: {}", loginRequest.getAccount());
                model.addAttribute("errorMessage", "帳號或密碼錯誤");
                prepareLoginPageModel(model, returnUrl);
                return "back-end/employee/login";
            }
            
            // 【第三步：帳號狀態檢查】
            if (employee.getStatus() == AccountStatus.INACTIVE) {
                log.warn("❌ 員工帳號已被停用 - 帳號: {}, ID: {}", employee.getAccount(), employee.getEmployeeId());
                
                // 檢查是否因登入失敗過多而被停用
                if (employee.getLoginFailureCount() >= MAX_LOGIN_ATTEMPTS) {
                    model.addAttribute("errorMessage", 
                        "您的帳號因登入失敗次數過多已被停用，請聯絡系統管理員解鎖帳號");
                    model.addAttribute("isAccountLocked", true);
                    model.addAttribute("showAccountLocked", true);
                } else {
                    model.addAttribute("errorMessage", 
                        "您的帳號已被停用，請聯絡系統管理員");
                }
                
                prepareLoginPageModel(model, returnUrl);
                return "back-end/employee/login";
            }
            
            // 【第四步：密碼驗證】
            boolean passwordValid = employeeAuthService.validatePassword(loginRequest.getPassword(), employee.getPassword());
            
            if (!passwordValid) {
                // 【登入失敗處理】
                return handleLoginFailure(employee, loginRequest.getAccount(), returnUrl, model);
            }
            
            // 【第五步：登入成功處理】
            return handleLoginSuccess(employee, returnUrl, session, redirectAttributes);
            
        } catch (Exception e) {
            log.error("💥 員工登入處理過程中發生錯誤: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "系統錯誤，請稍後再試");
            prepareLoginPageModel(model, returnUrl);
            return "back-end/employee/login";
        }
    }
    
    /**
     * 處理登入失敗邏輯
     * 增加失敗次數，達到上限時停用帳號
     */
    private String handleLoginFailure(EmployeeEntity employee, String account, String returnUrl, Model model) {
        log.warn("❌ 密碼驗證失敗 - 帳號: {}, 當前失敗次數: {}", account, employee.getLoginFailureCount());
        
        try {
            // 增加登入失敗次數
            int newFailureCount = employeeAuthService.incrementLoginFailureCount(employee.getEmployeeId());
            
            if (newFailureCount >= MAX_LOGIN_ATTEMPTS) {
                // 【達到上限，停用帳號】
                employeeAuthService.lockAccount(employee.getEmployeeId());
                log.error("🚫 帳號已被鎖定 - 帳號: {}, 失敗次數: {}", account, newFailureCount);
                
                model.addAttribute("errorMessage", 
                    "登入失敗次數過多，您的帳號已被停用，請聯絡系統管理員解鎖");
                model.addAttribute("isAccountLocked", true);
                model.addAttribute("showAccountLocked", true);
            } else {
                // 【未達上限，顯示警告】
                int remainingAttempts = MAX_LOGIN_ATTEMPTS - newFailureCount;
                log.warn("⚠️ 登入失敗 - 帳號: {}, 失敗次數: {}/{}, 還有 {} 次登入機會", 
                           account, newFailureCount, MAX_LOGIN_ATTEMPTS, remainingAttempts);
                
                String errorMessage = String.format("帳號或密碼錯誤，您還有 %d 次登入機會，達到 %d 次失敗將自動停用帳號", 
                                                   remainingAttempts, MAX_LOGIN_ATTEMPTS);
                model.addAttribute("errorMessage", errorMessage);
                model.addAttribute("isLoginWarning", true);
                model.addAttribute("showFailureCount", true);
                model.addAttribute("remainingAttempts", remainingAttempts);
                model.addAttribute("maxAttempts", MAX_LOGIN_ATTEMPTS);
            }
            
        } catch (Exception e) {
            log.error("處理登入失敗時發生錯誤: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "帳號或密碼錯誤");
        }
        
        prepareLoginPageModel(model, returnUrl);
        return "back-end/employee/login";
    }
    
    /**
     * 處理登入成功邏輯
     * 重置失敗次數，建立 Session
     */
    private String handleLoginSuccess(EmployeeEntity employee, String returnUrl, HttpSession session, RedirectAttributes redirectAttributes) {
        log.info("✅ 員工登入成功 - 帳號: {}, 姓名: {}, 角色: {}", 
                   employee.getAccount(), employee.getUsername(), employee.getRole());
        
        try {
            // 【重置登入失敗次數】
            employeeAuthService.resetLoginFailureCount(employee.getEmployeeId());
            
            // 【建立 Session】
            EmployeeDTO employeeDTO = employeeAuthService.convertToDTO(employee);
            session.setAttribute("loggedInEmployee", employeeDTO);
            session.setAttribute("employeeId", employee.getEmployeeId());
            session.setAttribute("employeeAccount", employee.getAccount());
            session.setAttribute("employeeName", employee.getUsername());
            session.setAttribute("employeeRole", employee.getRole());
            session.setAttribute("storeId", employee.getStore().getStoreId());
            session.setAttribute("isEmployeeLoggedIn", true);
            session.setAttribute("loginTime", System.currentTimeMillis());
            
            // 設定 Session 過期時間（4小時）
            session.setMaxInactiveInterval(4 * 60 * 60);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "歡迎回來，" + employee.getUsername() + "！");
            
            // 【重定向處理】
            if (returnUrl != null && !returnUrl.isEmpty() && !returnUrl.contains("login")) {
                log.info("重定向到指定頁面: {}", returnUrl);
                return "redirect:" + returnUrl;
            } else {
                log.info("重定向到員工後台首頁");
                return "redirect:/employee/select_page?welcome=true";
            }
            
        } catch (Exception e) {
            log.error("建立登入 Session 時發生錯誤: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "登入成功但系統錯誤，請重新登入");
            return "redirect:/employee/login";
        }
    }
    
    /**
     * 準備登入頁面所需的模型資料
     */
    private void prepareLoginPageModel(Model model, String returnUrl) {
        try {
            // 【修正】使用返回 DTO 的方法，而不是 Entity 方法
            List<EmployeeDTO> activeEmployees = employeeService.findAllActiveEmployees();
            if (activeEmployees != null && !activeEmployees.isEmpty()) {
                model.addAttribute("employeeList", activeEmployees);
                log.debug("成功載入 {} 筆啟用員工資料", activeEmployees.size());
            } else {
                model.addAttribute("noActiveEmployees", true);
                log.warn("沒有找到任何啟用狀態的員工");
            }
            
            // 【修正】使用返回 DTO 的方法，而不是 Entity 方法
            List<EmployeeDTO> inactiveEmployees = employeeService.findAllInactiveEmployees();
            if (inactiveEmployees != null && !inactiveEmployees.isEmpty()) {
                model.addAttribute("inactiveEmployeeList", inactiveEmployees);
                log.debug("成功載入 {} 筆已停權員工資料", inactiveEmployees.size());
            }
            
        } catch (Exception e) {
            log.error("準備登入頁面模型資料時發生錯誤: {}", e.getMessage(), e);
            model.addAttribute("employeeListError", "載入員工列表失敗: " + e.getMessage());
        }
        
        // 保持 returnUrl 參數
        if (returnUrl != null) {
            model.addAttribute("returnUrl", returnUrl);
        }
    }

    /**
     * 員工登出
     * 路徑: GET /employee/logout
     */
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        // 獲取當前登入的員工資訊（用於日誌記錄）
        EmployeeDTO loggedInEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
        
        // 完全銷毀 Session - 這是最安全的做法
        session.invalidate();
        
        if (loggedInEmployee != null) {
            log.info("員工登出 - ID: {}, 帳號: {}, 姓名: {}", 
                loggedInEmployee.getEmployeeId(),
                loggedInEmployee.getAccount(),
                loggedInEmployee.getUsername());
        } else {
            log.info("登出請求 - 無有效的員工 Session");
        }

        redirectAttributes.addFlashAttribute("successMessage", "您已成功登出");
        return "redirect:/employee/login?logout=success";
    }

    /**
     * 顯示忘記密碼頁面
     * 路徑: GET /employee/forgot-password
     */
    @GetMapping("/forgot-password")
    public String showForgotPasswordPage(Model model) {
        // 準備忘記密碼表單物件
        model.addAttribute("forgotPasswordRequest", new com.eatfast.employee.dto.ForgotPasswordRequest());
        return "back-end/employee/forgot-password";
    }

    /**
     * 處理忘記密碼表單提交
     * 路徑: POST /employee/forgot-password
     * 新增頻率限制功能，防止同一帳號或IP在30秒內重複請求
     * @BindingResult : 用於處理表單驗證錯誤
     * @ModelAttribute : 用於綁定表單數據到 DTO
     * @POSTMapping : 處理忘記密碼請求的表單提交
     */
    @PostMapping("/forgot-password")
    public String processForgotPassword(@Valid @ModelAttribute("forgotPasswordRequest") com.eatfast.employee.dto.ForgotPasswordRequest forgotPasswordRequest,
                                      BindingResult bindingResult,
                                      Model model,
                                      HttpServletRequest request) { // 新增HttpServletRequest參數
        
        // 如果表單驗證失敗，重新顯示忘記密碼頁面
        if (bindingResult.hasErrors()) {
            return "back-end/employee/forgot-password";
        }

        String accountOrEmail = forgotPasswordRequest.getAccountOrEmail();
        String clientIP = getClientIP(request); // 獲取客戶端IP

        try {
            // 檢查帳號請求頻率限制
            if (!rateLimitService.canSendRequest(accountOrEmail)) {
                long remainingTime = rateLimitService.getRemainingWaitTime(accountOrEmail);
                String errorMessage = String.format("請求過於頻繁，請等待 %d 秒後再試。為了帳號安全，每個帳號30秒內只能發送一次忘記密碼請求。", remainingTime);
                
                model.addAttribute("message", errorMessage);
                model.addAttribute("success", false);
                
                log.warn("忘記密碼請求被限制 - 帳號: {}, IP: {}, 剩餘等待時間: {}秒", 
                    accountOrEmail, clientIP, remainingTime);
                
                return "back-end/employee/forgot-password";
            }

            // 檢查IP請求頻率限制
            if (!rateLimitService.canSendRequestFromIP(clientIP)) {
                long remainingTime = rateLimitService.getRemainingWaitTimeForIP(clientIP);
                String errorMessage = String.format("請求過於頻繁，請等待 %d 秒後再試。為了系統安全，每個IP30秒內只能發送一次忘記密碼請求。", remainingTime);
                
                model.addAttribute("message", errorMessage);
                model.addAttribute("success", false);
                
                log.warn("忘記密碼請求被限制 - IP: {}, 帳號: {}, 剩餘等待時間: {}秒", 
                    clientIP, accountOrEmail, remainingTime);
                
                return "back-end/employee/forgot-password";
            }

            // 記錄本次請求（無論後續處理成功與否都記錄，防止重複請求）
            rateLimitService.recordAccountRequest(accountOrEmail);
            rateLimitService.recordIPRequest(clientIP);

            // 處理忘記密碼請求
            String resultMessage = employeeService.processForgotPassword(accountOrEmail);
            
            // 判斷是否成功（根據訊息內容判斷）
            boolean isSuccess = resultMessage.contains("密碼重設成功");
            
            model.addAttribute("message", resultMessage);
            model.addAttribute("success", isSuccess);
            
            log.info("忘記密碼請求處理完成 - 輸入: {}, IP: {}, 結果: {}", 
                accountOrEmail, clientIP, isSuccess ? "成功" : "失敗");

        } catch (IllegalArgumentException e) {
            // 處理輸入參數錯誤
            model.addAttribute("message", e.getMessage());
            model.addAttribute("success", false);
            log.warn("忘記密碼請求參數錯誤 - 輸入: {}, IP: {}, 錯誤: {}", 
                accountOrEmail, clientIP, e.getMessage());
            
        } catch (Exception e) {
            // 處理其他未預期的錯誤
            model.addAttribute("message", "系統處理忘記密碼請求時發生錯誤，請稍後再試或聯絡管理員");
            model.addAttribute("success", false);
            log.error("忘記密碼請求處理發生未預期錯誤 - 輸入: {}, IP: {}", 
                accountOrEmail, clientIP, e);
        }

        return "back-end/employee/forgot-password";
    }

    /**
     * 獲取客戶端真實IP地址
     * 考慮代理服務器和負載平衡器的情況
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        String xRealIP = request.getHeader("X-Real-IP");
        String xForwardedProto = request.getHeader("X-Forwarded-Proto");
        
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            // X-Forwarded-For 可能包含多個IP，取第一個
            return xForwardedFor.split(",")[0].trim();
        }
        
        if (xRealIP != null && !xRealIP.isEmpty() && !"unknown".equalsIgnoreCase(xRealIP)) {
            return xRealIP;
        }
        
        // 如果沒有代理，直接返回請求的遠程地址
        String remoteAddr = request.getRemoteAddr();
        
        // 處理IPv6本地回環地址
        if ("0:0:0:0:0:0:0:1".equals(remoteAddr)) {
            return "127.0.0.1";
        }
        
        return remoteAddr;
    }
}