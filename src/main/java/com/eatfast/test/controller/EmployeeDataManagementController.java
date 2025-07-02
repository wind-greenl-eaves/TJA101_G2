package com.eatfast.test.controller;

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

import java.util.List;
import java.util.Optional;

/**
 * 員工資料管理工具
 * 用於更新員工的郵件地址等資訊
 */
@Controller
@RequestMapping("/test")
public class EmployeeDataManagementController {
    
    private static final Logger log = LoggerFactory.getLogger(EmployeeDataManagementController.class);
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    /**
     * 顯示員工資料管理頁面
     */
    @GetMapping("/employee-data")
    public String showEmployeeDataPage(Model model) {
        // 列出所有員工
        List<EmployeeEntity> employees = employeeRepository.findAll();
        model.addAttribute("employees", employees);
        model.addAttribute("targetEmail", "young19960127@gmail.com");
        return "test/employee-data-management";
    }
    
    /**
     * 更新員工郵件地址
     */
    @PostMapping("/employee-data/update-email")
    public String updateEmployeeEmail(
            @RequestParam String account,
            @RequestParam String newEmail,
            Model model) {
        
        try {
            log.info("準備更新員工郵件地址 - 帳號: {}, 新郵件: {}", account, newEmail);
            
            // 查找員工
            Optional<EmployeeEntity> employeeOpt = employeeRepository.findByAccount(account);
            if (employeeOpt.isEmpty()) {
                model.addAttribute("error", "找不到帳號為 " + account + " 的員工");
                model.addAttribute("employees", employeeRepository.findAll());
                return "test/employee-data-management";
            }
            
            EmployeeEntity employee = employeeOpt.get();
            String oldEmail = employee.getEmail();
            
            // 檢查新郵件格式
            if (!isValidEmail(newEmail)) {
                model.addAttribute("error", "郵件地址格式不正確: " + newEmail);
                model.addAttribute("employees", employeeRepository.findAll());
                return "test/employee-data-management";
            }
            
            // 檢查郵件是否已被其他員工使用
            Optional<EmployeeEntity> existingEmployee = employeeRepository.findByEmail(newEmail.toLowerCase());
            if (existingEmployee.isPresent() && !existingEmployee.get().getEmployeeId().equals(employee.getEmployeeId())) {
                model.addAttribute("error", "郵件地址 " + newEmail + " 已被其他員工使用");
                model.addAttribute("employees", employeeRepository.findAll());
                return "test/employee-data-management";
            }
            
            // 更新郵件地址
            employee.setEmail(newEmail.toLowerCase());
            employeeRepository.save(employee);
            
            log.info("員工郵件地址更新成功 - 帳號: {}, 舊郵件: {}, 新郵件: {}", 
                account, oldEmail, newEmail);
            
            model.addAttribute("success", String.format(
                "✅ 員工 %s (帳號: %s) 的郵件地址已成功更新！\n" +
                "舊郵件: %s\n" +
                "新郵件: %s\n\n" +
                "現在可以使用忘記密碼功能測試郵件發送了。",
                employee.getUsername(), employee.getAccount(), oldEmail, newEmail
            ));
            
        } catch (Exception e) {
            log.error("更新員工郵件地址失敗", e);
            model.addAttribute("error", "更新失敗: " + e.getMessage());
        }
        
        // 重新載入員工列表
        List<EmployeeEntity> employees = employeeRepository.findAll();
        model.addAttribute("employees", employees);
        model.addAttribute("targetEmail", "young19960127@gmail.com");
        
        return "test/employee-data-management";
    }
    
    /**
     * 創建測試員工
     */
    @PostMapping("/employee-data/create-test-employee")
    public String createTestEmployee(Model model) {
        try {
            // 檢查是否已存在測試員工
            Optional<EmployeeEntity> existingEmployee = employeeRepository.findByAccount("testuser");
            if (existingEmployee.isPresent()) {
                model.addAttribute("error", "測試員工已存在，請直接使用更新功能");
            } else {
                // 創建新的測試員工
                EmployeeEntity testEmployee = new EmployeeEntity();
                testEmployee.setAccount("testuser");
                testEmployee.setUsername("測試員工");
                testEmployee.setEmail("young19960127@gmail.com");
                testEmployee.setPassword("Test123456");
                testEmployee.setPhone("0912345678");
                testEmployee.setNationalId("A123456789");
                testEmployee.setRole(com.eatfast.common.enums.EmployeeRole.STAFF);
                testEmployee.setStatus(com.eatfast.common.enums.AccountStatus.ACTIVE);
                
                // 需要設置門市，這裡使用第一個門市
                testEmployee.setStore(employeeRepository.findAll().get(0).getStore());
                
                employeeRepository.save(testEmployee);
                
                log.info("測試員工創建成功 - 帳號: testuser, 郵件: young19960127@gmail.com");
                model.addAttribute("success", "✅ 測試員工創建成功！\n帳號: testuser\n郵件: young19960127@gmail.com");
            }
        } catch (Exception e) {
            log.error("創建測試員工失敗", e);
            model.addAttribute("error", "創建測試員工失敗: " + e.getMessage());
        }
        
        // 重新載入員工列表
        List<EmployeeEntity> employees = employeeRepository.findAll();
        model.addAttribute("employees", employees);
        model.addAttribute("targetEmail", "young19960127@gmail.com");
        
        return "test/employee-data-management";
    }
    
    /**
     * 批量更新所有員工的郵件地址 - 修正版本
     * 解決唯一性約束衝突問題
     */
    @PostMapping("/employee-data/batch-update-email")
    public String batchUpdateAllEmployeesEmail(
            @RequestParam String newEmail,
            Model model) {
        
        try {
            log.info("開始批量更新所有員工的郵件地址為: {}", newEmail);
            
            // 檢查新郵件格式
            if (!isValidEmail(newEmail)) {
                model.addAttribute("error", "郵件地址格式不正確: " + newEmail);
                model.addAttribute("employees", employeeRepository.findAll());
                model.addAttribute("targetEmail", "young19960127@gmail.com");
                return "test/employee-data-management";
            }
            
            // 獲取所有員工
            List<EmployeeEntity> allEmployees = employeeRepository.findAll();
            if (allEmployees.isEmpty()) {
                model.addAttribute("error", "系統中沒有員工資料");
                model.addAttribute("employees", allEmployees);
                model.addAttribute("targetEmail", newEmail);
                return "test/employee-data-management";
            }
            
            // 使用事務批量更新，避免唯一性約束衝突
            StringBuilder updateLog = new StringBuilder();
            int updatedCount = 0;
            
            // 方案：為每個員工設置獨特的郵件地址，但都會轉發到目標地址
            for (int i = 0; i < allEmployees.size(); i++) {
                EmployeeEntity employee = allEmployees.get(i);
                String oldEmail = employee.getEmail();
                
                // 如果是第一個員工，使用原始郵件地址
                // 其他員工使用帶編號的郵件地址格式
                String uniqueEmail;
                if (i == 0) {
                    uniqueEmail = newEmail.toLowerCase();
                } else {
                    // 為了避免重複，給其他員工加上編號
                    // 例如: young19960127+emp2@gmail.com, young19960127+emp3@gmail.com
                    String[] emailParts = newEmail.toLowerCase().split("@");
                    uniqueEmail = emailParts[0] + "+emp" + (i + 1) + "@" + emailParts[1];
                }
                
                employee.setEmail(uniqueEmail);
                employeeRepository.save(employee);
                
                updateLog.append(String.format("✅ %s (帳號: %s) - 舊郵件: %s → 新郵件: %s\n", 
                    employee.getUsername(), employee.getAccount(), oldEmail, uniqueEmail));
                updatedCount++;
            }
            
            log.info("批量更新完成 - 共更新 {} 位員工的郵件地址", updatedCount);
            
            model.addAttribute("success", String.format(
                "🎉 批量更新成功！\n\n" +
                "共更新了 %d 位員工的郵件地址\n" +
                "主要收件地址: %s\n" +
                "其他員工使用別名格式（Gmail會自動轉發到主地址）\n\n" +
                "更新詳情:\n%s\n" +
                "💡 重要說明：\n" +
                "- 第一個員工使用主郵件地址: %s\n" +
                "- 其他員工使用 Gmail 別名功能（如 %s+emp2@gmail.com）\n" +
                "- 所有郵件都會自動轉發到你的主信箱！\n" +
                "- 現在所有員工的忘記密碼郵件都會送達你的信箱！",
                updatedCount, newEmail, updateLog.toString(), newEmail, newEmail.split("@")[0]
            ));
            
        } catch (Exception e) {
            log.error("批量更新員工郵件地址失敗", e);
            model.addAttribute("error", "批量更新失敗: " + e.getMessage());
        }
        
        // 重新載入員工列表
        List<EmployeeEntity> employees = employeeRepository.findAll();
        model.addAttribute("employees", employees);
        model.addAttribute("targetEmail", newEmail);
        
        return "test/employee-data-management";
    }
    
    /**
     * 驗證郵件格式
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }
}