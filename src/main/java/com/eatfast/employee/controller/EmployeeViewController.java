package com.eatfast.employee.controller;

import com.eatfast.common.enums.AccountStatus;
import com.eatfast.common.enums.EmployeeRole;
import com.eatfast.employee.dto.EmployeeDTO;
import com.eatfast.employee.service.EmployeeService;
import com.eatfast.store.dto.StoreDto;
import com.eatfast.store.service.StoreService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String showSelectPage(Model model) {
        // [不可變動的關鍵字]: model.addAttribute
        // 說明: 將後端資料傳遞給前端 Thymeleaf 模板。
        
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
     * 【方法更新】: 此方法現在僅作為一個跳轉入口。
     * 真正的查詢邏輯已轉移到 `listAllEmployees.html` 頁面的 JavaScript 中，
     * 它會根據 URL 的查詢參數動態呼叫後端 API。
     */
    @GetMapping("/listAll")
    public String showAllEmployees() {
        // 不再需要於此處查詢資料，交由前端 JS 處理。
        return "back-end/employee/listAllEmployees";
    }

    @GetMapping("/add")
    public String showAddEmployeePage(Model model) {
        List<StoreDto> storeList = storeService.findAllStores();
        model.addAttribute("storeList", storeList);
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
