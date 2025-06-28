package com.eatfast.test.Autowired;

import com.eatfast.common.enums.EmployeeRole;
import com.eatfast.common.enums.Gender;
import com.eatfast.employee.dto.CreateEmployeeRequest;
import com.eatfast.employee.dto.EmployeeDto;
import com.eatfast.employee.dto.UpdateEmployeeRequest;
import com.eatfast.employee.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * [可自定義的類別名稱]: Demo_EmployeeControllerTest
 * 開發階段專用的 Employee CRUD 功能測試控制器。
 * 目的: 提供一個簡單的網址入口，讓開發者能快速觸發後端 EmployeeService 的各項功能，
 * 並在 IDE 的控制台 (Console) 觀察執行結果，以驗證商業邏輯的正確性。
 *
 * - @Controller: 標記此類別為 Spring 的 Controller。
 * - @Autowired: 自動注入 EmployeeService 的實例。
 */
@Controller
public class Demo_EmployeeControllerTest {

    // [不可變動的關鍵字]: @Autowired
    // [說明]: Spring 會自動尋找 EmployeeService 的實作類別 (EmployeeServiceImpl) 並注入。
    @Autowired
    private EmployeeService employeeService;

    /**
     * [不可變動的關鍵字]: @RequestMapping, @ResponseBody
     * [說明]: 處理對 "/testEmployee" 路徑的請求。
     * - @ResponseBody: 讓 Spring 直接回傳 String，而非尋找 View 頁面。
     *
     * @return 一個表示測試已執行的簡單字串。
     */
    @RequestMapping("/testEmployee")
    @ResponseBody
    public String testEmployeeCrud() {
        /*
         * ==================================================================================
         * ● 測試 EmployeeService 的 CRUD 功能
         * ==================================================================================
         * 【操作方式】
         * 1. 確保您的 Spring Boot 應用程式正在執行。
         * 2. 在下方的測試區塊中，一次只「取消註解」一個您想測試的功能。
         * 3. 開啟瀏覽器，輸入網址: http://localhost:8080/testEmployee
         * 4. 按下 Enter 執行請求。
         * 5. 回到 Eclipse IDE，查看下方 Console 視窗的輸出日誌。
         * ==================================================================================
         */

        // ● 查詢 - 全部 (Read All)
        // ==================================================================================
        System.out.println("---------- 【測試開始】: 查詢所有員工 ----------");
        List<EmployeeDto> employees = employeeService.findAllEmployees();
        if (employees.isEmpty()) {
            System.out.println("資料庫中沒有員工資料。");
        } else {
            employees.forEach(employee -> System.out.println("查詢結果: " + employee.getUsername() + " (ID: " + employee.getEmployeeId() + ")"));
        }
        System.out.println("---------- 【測試結束】: 查詢所有員工 ----------");
        return "測試 (查詢全部) 完成，請查看 Console。";


        // ● 查詢 - 根據 ID (Read by ID)
        // ==================================================================================
//        System.out.println("---------- 【測試開始】: 根據 ID 查詢員工 ----------");
//        try {
//            // 請將 1L 替換為您資料庫中已存在的員工 ID
//            Long employeeIdToFind = 2L;
//            EmployeeDto employee = employeeService.findEmployeeById(employeeIdToFind);
//            System.out.println("查詢成功: " + employee.getUsername() + ", 角色: " + employee.getRole());
//        } catch (Exception e) {
//            System.err.println("查詢失敗: " + e.getMessage());
//        }
//        System.out.println("---------- 【測試結束】: 根據 ID 查詢員工 ----------");
//        return "測試 (根據 ID 查詢) 完成，請查看 Console。";


        // ● 新增 (Create)
        // ==================================================================================
//        System.out.println("---------- 【測試開始】: 新增員工 ----------");
//        try {
//            CreateEmployeeRequest newEmployeeRequest = new CreateEmployeeRequest();
//            newEmployeeRequest.setUsername("測試職員");
//            newEmployeeRequest.setAccount("test.staff.01"); // 每次測試請使用不同帳號
//            newEmployeeRequest.setPassword("a-very-strong-password");
//            newEmployeeRequest.setEmail("test.staff.01@example.com"); // 每次測試請使用不同Email
//            newEmployeeRequest.setPhone("0988-777-666");
//            newEmployeeRequest.setRole(EmployeeRole.STAFF);
//            newEmployeeRequest.setGender(Gender.F);
//            newEmployeeRequest.setNationalId("F234567890"); // 每次測試請使用不同ID
//            newEmployeeRequest.setStoreId(1L); // 確保 ID=1 的門市存在
//
//            EmployeeDto createdEmployee = employeeService.createEmployee(newEmployeeRequest);
//            System.out.println("新增成功! 新員工 ID: " + createdEmployee.getEmployeeId() + ", 姓名: " + createdEmployee.getUsername());
//        } catch (Exception e) {
//            System.err.println("新增失敗: " + e.getMessage());
//        }
//        System.out.println("---------- 【測試結束】: 新增員工 ----------");
//        return "測試 (新增) 完成，請查看 Console。";


        // ● 修改 (Update)
        // ==================================================================================
//        System.out.println("---------- 【測試開始】: 修改員工 ----------");
//        try {
//            // 請先確認您要修改的員工 ID 存在於資料庫中
//            Long employeeIdToUpdate = 1L;
//            UpdateEmployeeRequest updateRequest = new UpdateEmployeeRequest();
//            updateRequest.setUsername("林一郎 (已更新)");
//            updateRequest.setPhone("0911-000-222"); // 更新電話
//            updateRequest.setRole(EmployeeRole.HEADQUARTERS_ADMIN);
//
//            EmployeeDto updatedEmployee = employeeService.updateEmployee(employeeIdToUpdate, updateRequest);
//            System.out.println("修改成功! 員工 ID: " + updatedEmployee.getEmployeeId() + ", 新姓名: " + updatedEmployee.getUsername() + ", 新電話: " + updatedEmployee.getPhone());
//        } catch (Exception e) {
//            System.err.println("修改失敗: " + e.getMessage());
//        }
//        System.out.println("---------- 【測試結束】: 修改員工 ----------");
//        return "測試 (修改) 完成，請查看 Console。";


        // ● 刪除 (Delete)
        // ==================================================================================
//        System.out.println("---------- 【測試開始】: 刪除員工 ----------");
//        try {
//            // 注意：刪除是永久性操作！請提供一個用於測試、可被刪除的員工 ID。
//            // 建議先用「新增」功能建立一筆資料，再用其 ID 來測試刪除。
//            Long employeeIdToDelete = 13L; 
//            employeeService.deleteEmployee(employeeIdToDelete);
//            System.out.println("刪除 ID 為 " + employeeIdToDelete + " 的員工成功！");
//        } catch (Exception e) {
//            System.err.println("刪除失敗: " + e.getMessage());
//        }
//        System.out.println("---------- 【測試結束】: 刪除員工 ----------");
//        return "測試 (刪除) 完成，請查看 Console。";
    }
}
