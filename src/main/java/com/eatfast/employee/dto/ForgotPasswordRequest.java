// ================================================================
// 檔案名稱: ForgotPasswordRequest.java
// 功能說明: 忘記密碼請求資料傳輸物件
// 架構層級: 數據傳輸層 (Data Transfer Layer)
// 配對關係:
//   - 控制器: EmployeeLoginController.processForgotPassword() 方法接收
//   - 服務層: EmployeeService.processForgotPassword() 方法處理
//   - 前端表單: forgot-password.html 表單數據綁定
//   - 郵件服務: 與 MailService 配合發送重設密碼郵件
// 安全設計: 支持帳號或郵件兩種方式查找員工
// 業務邏輯: 生成臨時密碼並透過郵件通知員工
// ================================================================
package com.eatfast.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 忘記密碼請求 DTO
 * 用於接收忘記密碼表單資料
 */
public class ForgotPasswordRequest {

    /** 員工帳號或電子郵件 */
    @NotBlank(message = "員工帳號或電子郵件不可為空")
    @Size(max = 100, message = "輸入長度不可超過 100 個字元")
    private String accountOrEmail;

    // ================================================================
    //             建構子
    // ================================================================
    public ForgotPasswordRequest() {}

    public ForgotPasswordRequest(String accountOrEmail) {
        this.accountOrEmail = accountOrEmail;
    }

    // ================================================================
    //             Getters and Setters
    // ================================================================
    public String getAccountOrEmail() {
        return accountOrEmail;
    }

    public void setAccountOrEmail(String accountOrEmail) {
        this.accountOrEmail = accountOrEmail;
    }

    @Override
    public String toString() {
        return "ForgotPasswordRequest{" +
                "accountOrEmail='" + accountOrEmail + '\'' +
                '}';
    }
}