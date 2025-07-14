package com.eatfast.employee.controller;

import com.eatfast.employee.dto.EmployeeDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 員工頁面控制器
 * 處理員工管理相關的頁面路由
 */
@Controller
@RequestMapping("/employee")
public class EmployeePageController {

    /**
     * 新增員工頁面
     * 路徑: GET /employee/addEmployee
     */
    @GetMapping("/addEmployee")
    public String addEmployeePage(HttpSession session, Model model) {
        // 檢查登入狀態
        EmployeeDTO currentEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
        if (currentEmployee == null) {
            return "redirect:/employee/login";
        }
        
        // 添加當前員工資訊到模型
        model.addAttribute("currentEmployee", currentEmployee);
        
        // 返回新增員工頁面
        return "back-end/employee/addEmployee";
    }

    /**
     * 臨時路由：處理錯誤的 applicationList 跳轉
     * 這是為了解決瀏覽器緩存導致的 404 錯誤
     * 正確的做法應該是清除瀏覽器緩存
     */
    @GetMapping("/applicationList")
    public String handleApplicationListRedirect(HttpSession session) {
        // 檢查登入狀態
        EmployeeDTO currentEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
        if (currentEmployee == null) {
            return "redirect:/employee/login";
        }
        
        // 重定向到正確的管理首頁
        return "redirect:/employee/select_page";
    }

    // 注意：移除了 listAllEmployees 方法以避免與 EmployeeViewController 的 URL 映射衝突
    // 員工列表功能現在由 EmployeeViewController.showAllEmployees() 處理
}