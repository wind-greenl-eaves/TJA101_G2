// ================================================================
// 檔案名稱: EmployeeViewController.java
// 功能說明: 員工模組視圖控制器 - 專門處理頁面顯示與導航
// 架構層級: 視圖控制器層 (View Controller Layer)
// 配對關係:
//   - 服務層: EmployeeService - 獲取業務數據
//   - 視圖層: templates/back-end/employee/*.html - 對應的 Thymeleaf 模板
//   - API控制器: EmployeeController - RESTful API 提供者
//   - 權限控制: 基於 Session 進行角色權限檢查
// 設計模式:
//   - MVC Pattern (Model-View-Controller)
//   - Page Controller Pattern
//   - Role-Based Access Control (RBAC)
// 頁面導航:
//   - /employee/select_page → 員工管理首頁
//   - /employee/add → 新增員工頁面
//   - /employee/listAll → 員工列表頁面
//   - /employee/edit/{id} → 編輯員工頁面
// ================================================================
package com.eatfast.employee.controller;

import com.eatfast.common.enums.AccountStatus;
import com.eatfast.common.enums.EmployeeRole;
import com.eatfast.employee.dto.EmployeeDTO;
import com.eatfast.employee.service.EmployeeService;
import com.eatfast.store.dto.StoreDto;
import com.eatfast.store.service.StoreService;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 員工模組的視圖控制器 (View Controller)。
 */
@Controller
@RequestMapping("/employee")
public class EmployeeViewController {

    private final EmployeeService employeeService;
    private final StoreService storeService;

    @Autowired
    public EmployeeViewController(EmployeeService employeeService, StoreService storeService) {
        this.employeeService = employeeService;
        this.storeService = storeService;
    }

    /**
     * 【方法更新】: 導向到「員工查詢選擇頁」，並為複合查詢準備必要資料。
     * [路徑]: GET /employee/select_page
     * [配對]: index.html 中的「進入員工管理系統」按鈕。
     */
    @GetMapping("/select_page")
    public String showSelectPage(Model model, HttpSession session,
                                  @RequestParam(value = "welcome", required = false) String welcome) {
        // [不可變動的關鍵字]: model.addAttribute
        // 說明: 將後端資料傳遞給前端 Thymeleaf 模板。

        // 檢查是否有登入的員工資訊
        EmployeeDTO loggedInEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
        String employeeName = (String) session.getAttribute("employeeName");

        // 如果有 welcome 參數或者是剛登入的狀態，顯示歡迎訊息
        if ("true".equals(welcome) || (loggedInEmployee != null && employeeName != null)) {
            model.addAttribute("showWelcome", true);
            model.addAttribute("welcomeMessage", "歡迎回來，" + (employeeName != null ? employeeName : "訪客") + "！");
            model.addAttribute("loggedInEmployee", loggedInEmployee);
        }

        // 1. 傳遞所有可選的「員工角色」
        model.addAttribute("roles", EmployeeRole.values());
        // 2. 傳遞所有可選的「帳號狀態」
        model.addAttribute("statuses", AccountStatus.values());
        // 3. 傳遞所有可選的「門市列表」
        model.addAttribute("stores", storeService.findAllStores());

        return "back-end/employee/select_page_employee";
    }

    @GetMapping("/listOne")
    public String showOneEmployee(@RequestParam("employeeId") Long employeeId, Model model) {
        EmployeeDTO employeeDto = employeeService.findEmployeeById(employeeId);
        model.addAttribute("employee", employeeDto);
        return "back-end/employee/listOneEmployee";
    }

    /**
     * 【方法更新】: 此方法現在加入權限控制。
     * 總部管理員可以看到所有員工，門市經理只能看到自己門市的員工，一般員工無權限訪問。
     */
    @GetMapping("/listAll")
    public String showAllEmployees(HttpSession session, RedirectAttributes redirectAttributes) {
        // 權限檢查：獲取當前登入員工資訊
        EmployeeDTO currentEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
        if (currentEmployee == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "請重新登入");
            return "redirect:/employee/login";
        }

        // 根據員工角色進行權限控制
        EmployeeRole currentRole = currentEmployee.getRole();
        
        switch (currentRole) {
            case HEADQUARTERS_ADMIN:
                // 總部管理員：可以查看所有員工
                break;
                
            case MANAGER:
                // 門市經理：可以查看，但前端會限制只顯示自己門市的員工
                break;
                
            case STAFF:
                // 一般員工：無權限查看員工資料
                redirectAttributes.addFlashAttribute("errorMessage", "權限不足：您沒有權限查看員工列表");
                return "redirect:/employee/select_page";
                
            default:
                redirectAttributes.addFlashAttribute("errorMessage", "權限不足");
                return "redirect:/employee/select_page";
        }
        
        // 真正的查詢邏輯已轉移到 `listAllEmployees.html` 頁面的 JavaScript 中，
        // 它會根據 URL 的查詢參數動態呼叫後端 API。
        return "back-end/employee/listAllEmployees";
    }

    @GetMapping("/add")
    public String showAddEmployeePage(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        // 權限檢查：獲取當前登入員工資訊
        EmployeeDTO currentEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
        if (currentEmployee == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "請重新登入");
            return "redirect:/employee/login";
        }

        // 根據員工角色進行權限控制
        EmployeeRole currentRole = currentEmployee.getRole();
        
        switch (currentRole) {
            case HEADQUARTERS_ADMIN:
                // 總部管理員：可以新增員工，需要傳遞所有門市列表
                List<StoreDto> storeList = storeService.findAllStores();
                model.addAttribute("storeList", storeList);
                break;
                
            case MANAGER:
                // 門市經理：可以新增員工，但只能新增到自己的門市
                // 不需要傳遞門市列表，前端會自動帶入經理的門市
                break;
                
            case STAFF:
                // 一般員工：無權限新增員工
                redirectAttributes.addFlashAttribute("errorMessage", "權限不足：您無法新增員工");
                return "redirect:/employee/select_page";
                
            default:
                redirectAttributes.addFlashAttribute("errorMessage", "未知的員工角色");
                return "redirect:/employee/select_page";
        }
        
        return "back-end/employee/addEmployee";
    }

    @GetMapping("/edit/{id}")
    public String showEditEmployeePage(@PathVariable("id") Long id, Model model) {
        EmployeeDTO employeeDto = employeeService.findEmployeeById(id);
        model.addAttribute("employee", employeeDto);
        List<StoreDto> storeList = storeService.findAllStores();
        model.addAttribute("storeList", storeList);
        return "back-end/employee/update_employee_input";
    }
}