package com.eatfast.employee.controller;

import com.eatfast.common.exception.ResourceNotFoundException;
import com.eatfast.employee.dto.EmployeeDTO;
import com.eatfast.employee.dto.EmployeeLoginRequest;
import com.eatfast.employee.service.EmployeeService;
import jakarta.servlet.http.HttpSession;
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
 * 員工登入控制器
 * 處理員工登入相關的頁面顯示和表單提交
 */
@Controller
@RequestMapping("/employee")
public class EmployeeLoginController {

    private static final Logger log = LoggerFactory.getLogger(EmployeeLoginController.class);
    
    private final EmployeeService employeeService;

    @Autowired
    public EmployeeLoginController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * 顯示員工登入頁面
     * 路徑: GET /employee/login
     */
    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "logout", required = false) String logout, 
                               @RequestParam(value = "shown", required = false) String shown,
                               Model model, RedirectAttributes redirectAttributes) {
        // 準備登入表單物件
        model.addAttribute("loginRequest", new EmployeeLoginRequest());
        
        // 處理登出成功訊息 - 只在第一次顯示，避免重新整理時重複顯示
        if ("success".equals(logout) && !"true".equals(shown)) {
            // 使用 RedirectAttributes 來傳遞訊息，這樣重定向後訊息不會丟失
            redirectAttributes.addFlashAttribute("successMessage", "登出成功！感謝您的使用。");
            // 重定向到同一頁面但加上 shown=true 參數，避免重新整理時重複顯示訊息
            return "redirect:/employee/login?shown=true";
        }
        
        // 獲取所有啟用狀態的員工列表，用於管理員小幫手
        try {
            List<EmployeeDTO> activeEmployees = employeeService.findAllActiveEmployees();
            model.addAttribute("employeeList", activeEmployees);
        } catch (Exception e) {
            log.warn("無法獲取員工列表: {}", e.getMessage());
            // 即使獲取員工列表失敗，也不影響登入頁面的顯示
        }
        
        // 獲取所有已停權員工列表，用於管理員小幫手
        try {
            List<EmployeeDTO> inactiveEmployees = employeeService.findAllInactiveEmployees();
            model.addAttribute("inactiveEmployeeList", inactiveEmployees);
        } catch (Exception e) {
            log.warn("無法獲取已停權員工列表: {}", e.getMessage());
            // 即使獲取已停權員工列表失敗，也不影響登入頁面的顯示
        }
        
        return "back-end/employee/login";
    }

    /**
     * 處理員工登入表單提交
     * 路徑: POST /employee/login
     */
    @PostMapping("/login")
    public String processLogin(@Valid @ModelAttribute("loginRequest") EmployeeLoginRequest loginRequest,
                              BindingResult bindingResult,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        
        // 如果表單驗證失敗，重新顯示登入頁面
        if (bindingResult.hasErrors()) {
            // 重新添加員工列表
            try {
                List<EmployeeDTO> activeEmployees = employeeService.findAllActiveEmployees();
                model.addAttribute("employeeList", activeEmployees);
            } catch (Exception e) {
                log.warn("無法獲取員工列表: {}", e.getMessage());
            }
            
            // 重新添加已停權員工列表
            try {
                List<EmployeeDTO> inactiveEmployees = employeeService.findAllInactiveEmployees();
                model.addAttribute("inactiveEmployeeList", inactiveEmployees);
            } catch (Exception e) {
                log.warn("無法獲取已停權員工列表: {}", e.getMessage());
            }
            
            return "back-end/employee/login";
        }

        try {
            // 進行登入驗證
            EmployeeDTO authenticatedEmployee = employeeService.authenticateEmployee(
                loginRequest.getAccount(), 
                loginRequest.getPassword()
            );

            // 登入成功，將員工資訊存入 Session
            session.setAttribute("loggedInEmployee", authenticatedEmployee);
            session.setAttribute("employeeId", authenticatedEmployee.getEmployeeId());
            session.setAttribute("employeeName", authenticatedEmployee.getUsername());
            session.setAttribute("employeeRole", authenticatedEmployee.getRole());

            log.info("員工登入成功 - ID: {}, 帳號: {}, 姓名: {}", 
                authenticatedEmployee.getEmployeeId(),
                authenticatedEmployee.getAccount(),
                authenticatedEmployee.getUsername());

            // 登入成功後重定向到員工管理主頁
            redirectAttributes.addFlashAttribute("successMessage", 
                "歡迎，" + authenticatedEmployee.getUsername() + "！登入成功。");
            
            return "redirect:/employee/select_page";

        } catch (ResourceNotFoundException e) {
            // 帳號不存在或密碼錯誤
            model.addAttribute("errorMessage", "帳號或密碼錯誤，請重新輸入");
            log.warn("登入失敗 - 帳號: {}, 原因: {}", loginRequest.getAccount(), e.getMessage());
            
        } catch (IllegalArgumentException e) {
            // 帳號被停用或其他業務邏輯錯誤
            model.addAttribute("errorMessage", e.getMessage());
            log.warn("登入失敗 - 帳號: {}, 原因: {}", loginRequest.getAccount(), e.getMessage());
            
        } catch (Exception e) {
            // 其他未預期的錯誤
            model.addAttribute("errorMessage", "登入過程中發生錯誤，請稍後再試");
            log.error("登入過程中發生未預期錯誤 - 帳號: {}", loginRequest.getAccount(), e);
        }

        // 登入失敗，重新顯示登入頁面並保留輸入的帳號
        try {
            List<EmployeeDTO> activeEmployees = employeeService.findAllActiveEmployees();
            model.addAttribute("employeeList", activeEmployees);
        } catch (Exception e) {
            log.warn("無法獲取員工列表: {}", e.getMessage());
        }
        
        // 重新添加已停權員工列表
        try {
            List<EmployeeDTO> inactiveEmployees = employeeService.findAllInactiveEmployees();
            model.addAttribute("inactiveEmployeeList", inactiveEmployees);
        } catch (Exception e) {
            log.warn("無法獲取已停權員工列表: {}", e.getMessage());
        }
        
        return "back-end/employee/login";
    }

    /**
     * 員工登出
     * 路徑: GET /employee/logout
     */
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        // 獲取當前登入的員工資訊（用於日誌記錄）
        EmployeeDTO loggedInEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
        
        // 清除 Session 中的員工資訊
        session.removeAttribute("loggedInEmployee");
        session.removeAttribute("employeeId");
        session.removeAttribute("employeeName");
        session.removeAttribute("employeeRole");
        
        // 也可以選擇完全銷毀 Session
        // session.invalidate();
        
        if (loggedInEmployee != null) {
            log.info("員工登出 - ID: {}, 帳號: {}, 姓名: {}", 
                loggedInEmployee.getEmployeeId(),
                loggedInEmployee.getAccount(),
                loggedInEmployee.getUsername());
        }

        redirectAttributes.addFlashAttribute("successMessage", "您已成功登出");
        return "redirect:/employee/login";
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
     */
    @PostMapping("/forgot-password")
    public String processForgotPassword(@Valid @ModelAttribute("forgotPasswordRequest") com.eatfast.employee.dto.ForgotPasswordRequest forgotPasswordRequest,
                                      BindingResult bindingResult,
                                      Model model) {
        
        // 如果表單驗證失敗，重新顯示忘記密碼頁面
        if (bindingResult.hasErrors()) {
            return "back-end/employee/forgot-password";
        }

        try {
            // 處理忘記密碼請求
            String resultMessage = employeeService.processForgotPassword(forgotPasswordRequest.getAccountOrEmail());
            
            // 判斷是否成功（根據訊息內容判斷）
            boolean isSuccess = resultMessage.contains("密碼重設成功");
            
            model.addAttribute("message", resultMessage);
            model.addAttribute("success", isSuccess);
            
            log.info("忘記密碼請求處理完成 - 輸入: {}, 結果: {}", 
                forgotPasswordRequest.getAccountOrEmail(), 
                isSuccess ? "成功" : "失敗");

        } catch (IllegalArgumentException e) {
            // 處理輸入參數錯誤
            model.addAttribute("message", e.getMessage());
            model.addAttribute("success", false);
            log.warn("忘記密碼請求參數錯誤 - 輸入: {}, 錯誤: {}", 
                forgotPasswordRequest.getAccountOrEmail(), e.getMessage());
            
        } catch (Exception e) {
            // 處理其他未預期的錯誤
            model.addAttribute("message", "系統處理忘記密碼請求時發生錯誤，請稍後再試或聯絡管理員");
            model.addAttribute("success", false);
            log.error("忘記密碼請求處理發生未預期錯誤 - 輸入: {}", 
                forgotPasswordRequest.getAccountOrEmail(), e);
        }

        return "back-end/employee/forgot-password";
    }
}