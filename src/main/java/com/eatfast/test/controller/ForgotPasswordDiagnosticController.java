package com.eatfast.test.controller;

import com.eatfast.employee.service.EmployeeService;
import com.eatfast.employee.repository.EmployeeRepository;
import com.eatfast.employee.model.EmployeeEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * 忘記密碼功能診斷工具
 * 用於檢查為什麼正式忘記密碼頁面無法發送郵件
 */
@Controller
@RequestMapping("/test")
public class ForgotPasswordDiagnosticController {
    
    private static final Logger log = LoggerFactory.getLogger(ForgotPasswordDiagnosticController.class);
    
    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    /**
     * 顯示忘記密碼診斷頁面
     */
    @GetMapping("/forgot-password-diagnostic")
    public String showDiagnosticPage(Model model) {
        model.addAttribute("defaultInput", "young19960127@gmail.com");
        return "test/forgot-password-diagnostic";
    }
    
    /**
     * 執行忘記密碼流程診斷
     */
    @PostMapping("/forgot-password-diagnostic")
    public String runDiagnostic(@RequestParam String accountOrEmail, Model model) {
        
        StringBuilder diagnosticResult = new StringBuilder();
        boolean overallSuccess = true;
        
        try {
            log.info("🔍 開始診斷忘記密碼流程 - 輸入: {}", accountOrEmail);
            diagnosticResult.append("=== 忘記密碼流程診斷報告 ===\n\n");
            
            // 步驟 1: 檢查輸入格式
            diagnosticResult.append("📝 步驟 1: 檢查輸入參數\n");
            if (accountOrEmail == null || accountOrEmail.trim().isEmpty()) {
                diagnosticResult.append("❌ 輸入為空\n");
                overallSuccess = false;
            } else {
                diagnosticResult.append("✅ 輸入不為空: ").append(accountOrEmail).append("\n");
            }
            
            String input = accountOrEmail.trim();
            
            // 步驟 2: 檢查是否為郵件格式
            boolean isEmailFormat = input.contains("@");
            diagnosticResult.append("📧 輸入格式: ").append(isEmailFormat ? "郵件格式" : "帳號格式").append("\n\n");
            
            // 步驟 3: 查找員工資料
            diagnosticResult.append("🔍 步驟 2: 查找員工資料\n");
            EmployeeEntity employee = null;
            
            // 按帳號查找
            Optional<EmployeeEntity> employeeByAccount = employeeRepository.findByAccount(input);
            if (employeeByAccount.isPresent()) {
                employee = employeeByAccount.get();
                diagnosticResult.append("✅ 按帳號找到員工: ").append(employee.getUsername()).append("\n");
            } else {
                diagnosticResult.append("⚠️ 按帳號查找無結果\n");
                
                // 按郵件查找
                Optional<EmployeeEntity> employeeByEmail = employeeRepository.findByEmail(input.toLowerCase());
                if (employeeByEmail.isPresent()) {
                    employee = employeeByEmail.get();
                    diagnosticResult.append("✅ 按郵件找到員工: ").append(employee.getUsername()).append("\n");
                } else {
                    diagnosticResult.append("❌ 按郵件查找也無結果\n");
                    overallSuccess = false;
                }
            }
            
            if (employee == null) {
                diagnosticResult.append("❌ 未找到對應的員工資料！\n");
                diagnosticResult.append("💡 建議檢查:\n");
                diagnosticResult.append("   - 確認輸入的帳號或郵件是否正確\n");
                diagnosticResult.append("   - 確認該員工是否已在系統中註冊\n\n");
                
                // 列出資料庫中的所有員工（僅用於測試）
                diagnosticResult.append("📋 系統中現有的員工資料:\n");
                employeeRepository.findAll().forEach(emp -> {
                    diagnosticResult.append("   - 帳號: ").append(emp.getAccount())
                                  .append(", 郵件: ").append(emp.getEmail())
                                  .append(", 姓名: ").append(emp.getUsername())
                                  .append(", 狀態: ").append(emp.getStatus()).append("\n");
                });
            } else {
                // 步驟 4: 檢查員工狀態
                diagnosticResult.append("\n👤 步驟 3: 檢查員工狀態\n");
                diagnosticResult.append("員工資訊:\n");
                diagnosticResult.append("   - 帳號: ").append(employee.getAccount()).append("\n");
                diagnosticResult.append("   - 姓名: ").append(employee.getUsername()).append("\n");
                diagnosticResult.append("   - 郵件: ").append(employee.getEmail()).append("\n");
                diagnosticResult.append("   - 狀態: ").append(employee.getStatus()).append("\n");
                
                if (employee.getStatus() != com.eatfast.common.enums.AccountStatus.ACTIVE) {
                    diagnosticResult.append("❌ 員工狀態不是 ACTIVE，無法重設密碼\n");
                    overallSuccess = false;
                } else {
                    diagnosticResult.append("✅ 員工狀態正常\n");
                }
                
                // 步驟 5: 測試實際的忘記密碼流程
                if (overallSuccess) {
                    diagnosticResult.append("\n🔄 步驟 4: 執行實際忘記密碼流程\n");
                    try {
                        String result = employeeService.processForgotPassword(accountOrEmail);
                        diagnosticResult.append("✅ 忘記密碼流程執行完成\n");
                        diagnosticResult.append("📋 返回訊息: ").append(result).append("\n");
                        
                        // 檢查是否包含成功關鍵字
                        if (result.contains("密碼重設成功")) {
                            diagnosticResult.append("✅ 密碼重設成功！\n");
                            if (result.contains("郵件發送成功") || result.contains("已發送至您的電子郵件")) {
                                diagnosticResult.append("✅ 郵件發送成功！\n");
                            } else if (result.contains("郵件發送失敗") || result.contains("郵件發送遇到問題")) {
                                diagnosticResult.append("⚠️ 密碼重設成功但郵件發送失敗\n");
                                diagnosticResult.append("💡 可能原因：SMTP 配置問題或網路問題\n");
                            }
                        } else {
                            diagnosticResult.append("❌ 密碼重設失敗\n");
                            overallSuccess = false;
                        }
                        
                    } catch (Exception e) {
                        diagnosticResult.append("❌ 忘記密碼流程執行異常: ").append(e.getMessage()).append("\n");
                        log.error("診斷過程中執行忘記密碼流程異常", e);
                        overallSuccess = false;
                    }
                }
            }
            
            // 總結
            diagnosticResult.append("\n=== 診斷總結 ===\n");
            if (overallSuccess) {
                diagnosticResult.append("✅ 忘記密碼功能運作正常\n");
                diagnosticResult.append("💡 如果仍收不到郵件，請檢查垃圾郵件資料夾\n");
            } else {
                diagnosticResult.append("❌ 發現問題，請根據上述診斷結果進行修正\n");
            }
            
        } catch (Exception e) {
            diagnosticResult.append("❌ 診斷過程發生異常: ").append(e.getMessage()).append("\n");
            log.error("診斷忘記密碼流程時發生異常", e);
            overallSuccess = false;
        }
        
        model.addAttribute("diagnosticResult", diagnosticResult.toString());
        model.addAttribute("success", overallSuccess);
        model.addAttribute("defaultInput", accountOrEmail);
        
        return "test/forgot-password-diagnostic";
    }
}