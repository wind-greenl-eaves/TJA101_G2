package com.eatfast.employee.controller;

import com.eatfast.employee.dto.EmployeeDto;
import com.eatfast.employee.service.EmployeeService;
import com.eatfast.store.dto.StoreDto; // 假設您有一個 StoreDto
import com.eatfast.store.service.StoreService; // 假設您有一個 StoreService

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * [可自定義的類別名稱]: EmployeeViewController
 * 員工模組的視圖控制器 (View Controller)。
 * 職責: 專門處理所有導向到「員工管理」相關前端頁面 (HTML) 的請求。
 */
@Controller
@RequestMapping("/employee") // 基礎路徑，所有員工頁面都將以 /employee 開頭
public class EmployeeViewController {

    // --- 依賴注入 ---
    private final EmployeeService employeeService;
    private final StoreService storeService; // 注入 StoreService 以獲取門市列表

    @Autowired
    public EmployeeViewController(EmployeeService employeeService, StoreService storeService) {
        this.employeeService = employeeService;
        this.storeService = storeService;
    }

    /**
     * [功能]: 導向到「員工查詢選擇頁」。
     * [路徑]: GET /employee/select_page
     * [配對]: index.html 中的「進入員工管理系統」按鈕。
     * @return 視圖名稱，指向 "templates/back-end/employee/select_page_employee.html"。
     */
    @GetMapping("/select_page")
    public String showSelectPage() {
        return "back-end/employee/select_page_employee";
    }

    /**
     * [功能]: 導向到「查詢單一員工結果頁」。
     * [路徑]: GET /employee/listOne
     * [配對]: 由 select_page_employee.html 中的查詢表單提交。
     * @param employeeId 從請求參數中獲取的員工 ID。
     * @param model 用於將查詢結果傳遞給視圖。
     * @return 視圖名稱，指向 "templates/back-end/employee/listOneEmployee.html"。
     */
    @GetMapping("/listOne")
    public String showOneEmployee(@RequestParam("employeeId") Long employeeId, Model model) {
        EmployeeDto employeeDto = employeeService.findEmployeeById(employeeId);
        model.addAttribute("employee", employeeDto);
        return "back-end/employee/listOneEmployee"; // 假設您有這個顯示單一員工的頁面
    }

    /**
     * [功能]: 導向到「查詢所有員工列表頁」。
     * [路徑]: GET /employee/listAll
     * [配對]: 由 select_page_employee.html 中的「查詢所有員工資料」連結觸發。
     * @param model 用於將查詢結果傳遞給視圖。
     * @return 視圖名稱，指向 "templates/back-end/employee/listAllEmployees.html"。
     */
    @GetMapping("/listAll")
    public String showAllEmployees(Model model) {
        List<EmployeeDto> employeeList = employeeService.findAllEmployees();
        model.addAttribute("employeeList", employeeList);
        return "back-end/employee/listAllEmployees";
    }

    /**
     * [功能]: 導向到「新增員工」的表單頁面。
     * [路徑]: GET /employee/add
     * [配對]: 由 select_page_employee.html 中的「新增員工資料」連結觸發。
     * @param model 用於將「門市列表」傳遞給前端，以生成下拉式選單。
     * @return 視圖名稱，指向 "templates/back-end/employee/addEmployee.html"。
     */
    @GetMapping("/add")
    public String showAddEmployeePage(Model model) {
        // 為了讓新增頁面可以選擇所屬門市，我們需要從 StoreService 獲取所有門市列表
        List<StoreDto> storeList = storeService.findAllStores();
        model.addAttribute("storeList", storeList);
        return "back-end/employee/addEmployee";
    }

    /**
     * [功能]: 導向到「修改員工」的表單頁面。
     * [路徑]: GET /employee/edit/{id}
     * [配對]: 通常由員工列表頁 (listAllEmployees.html) 中的「修改」按鈕觸發。
     * @param id 要修改的員工 ID，從 URL 路徑中獲取。
     * @param model 用於將該員工的現有資料，以及所有門市的列表傳遞給視圖。
     * @return 視圖名稱，指向 "templates/back-end/employee/update_employee_input.html"。
     */
    @GetMapping("/edit/{id}")
    public String showEditEmployeePage(@PathVariable("id") Long id, Model model) {
        // 1. 獲取要修改的員工的現有資料
        EmployeeDto employeeDto = employeeService.findEmployeeById(id);
        model.addAttribute("employee", employeeDto);

        // 2. 獲取所有門市列表，以便在表單中可以修改所屬門市
        List<StoreDto> storeList = storeService.findAllStores();
        model.addAttribute("storeList", storeList);

        return "back-end/employee/update_employee_input"; // 假設您有這個修改員工的頁面
    }
}
