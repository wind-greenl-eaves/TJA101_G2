// ================================================================
// 檔案名稱: EmployeeLoginRequest.java
// 功能說明: 員工登入請求資料傳輸物件
// 架構層級: 數據傳輸層 (Data Transfer Layer)
// 配對關係:
//   - 控制器: EmployeeLoginController.processLogin() 方法接收
//   - 服務層: EmployeeService.authenticateEmployee() 方法處理
//   - 前端表單: login.html 表單數據綁定
//   - Session: 登入成功後存儲到 HttpSession
// 安全設計: 密碼字段在 toString() 中被遮罩保護
// 驗證策略: 基礎的非空驗證和長度限制
// ================================================================
package com.eatfast.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 員工登入請求 DTO
 * 用於接收員工登入表單資料
 */
public class EmployeeLoginRequest {

    /** 員工登入帳號 */
    @NotBlank(message = "員工帳號不可為空")
    @Size(max = 50, message = "帳號長度不可超過 50 個字元")
    private String account;

    /** 員工登入密碼 */
    @NotBlank(message = "登入密碼不可為空")
    @Size(max = 255, message = "密碼長度不可超過 255 個字元")
    private String password;

    // ================================================================
    //             建構子
    // ================================================================
    public EmployeeLoginRequest() {}

    public EmployeeLoginRequest(String account, String password) {
        this.account = account;
        this.password = password;
    }

    // ================================================================
    //             Getters and Setters
    // ================================================================
    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "EmployeeLoginRequest{" +
                "account='" + account + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}